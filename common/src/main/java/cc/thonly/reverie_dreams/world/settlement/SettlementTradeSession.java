package cc.thonly.reverie_dreams.world.settlement;

import cc.thonly.reverie_dreams.entity.HumanEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Runtime ownership of a single merchant menu.
 */
public final class SettlementTradeSession {
    private @Nullable SettlementTradeGui gui;
    private @Nullable ServerPlayer customer;
    private @Nullable SettlementTrade expected;
    private @Nullable SettlementPilotData data;

    public boolean open(HumanEntity resident, ServerPlayer player) {
        if (this.gui != null || !(resident.level() instanceof ServerLevel level))
            return false;
        var currentData = SettlementPilotData.existingFor(resident);
        var offer = currentData == null ? null : currentData.trade(resident.getUUID());
        if (offer == null || offer.uses() >= offer.stock() || !eligible(level, resident, player, currentData, offer)
                || !canExchange(level, offer, offer.payment())) {
            return false;
        }
        var menu = new SettlementTradeGui(player, resident, offer);
        this.gui = menu;
        this.customer = player;
        this.expected = offer;
        this.data = currentData;
        if (!menu.open()) {
            this.clear();
            return false;
        }
        resident.getNavigation().stop();
        return true;
    }

    public boolean isActive() {
        return this.gui != null;
    }

    public void tick(HumanEntity resident) {
        if (this.gui != null && !valid(resident, this.customer, this.gui))
            this.close();
    }

    public void close() {
        var menu = this.gui;
        this.clear();
        if (menu != null)
            menu.close();
    }

    private void clear() {
        this.gui = null;
        this.customer = null;
        this.expected = null;
        this.data = null;
    }

    static boolean valid(HumanEntity resident, ServerPlayer player, SettlementTradeGui gui) {
        var session = resident.settlementTradeSession();
        var data = session.data;
        var expected = session.expected;
        return session.gui == gui && session.customer == player && data != null && expected != null
                && resident.level() instanceof ServerLevel level && SettlementPilotData.existingFor(resident) == data
                && data.trade(resident.getUUID()) == expected && eligible(level, resident, player, data, expected);
    }

    static boolean canTake(HumanEntity resident, ServerPlayer player, SettlementTradeGui gui, ItemStack payment) {
        return valid(resident, player, gui) && canExchange((ServerLevel) resident.level(),
                resident.settlementTradeSession().expected, payment);
    }

    private static boolean canExchange(ServerLevel level, SettlementTrade offer, ItemStack payment) {
        var room = offer.stockroom().orElse(null);
        return room == null || (!payment.isEmpty() && SettlementChestInventory.canExchange(level,
                room.goods(), offer.result(), room.till(), payment));
    }

    static void complete(HumanEntity resident, ServerPlayer player, SettlementTradeGui gui, int count, ItemStack payment) {
        var session = resident.settlementTradeSession();
        if (!valid(resident, player, gui))
            throw new IllegalStateException("Settlement trade session changed during payment");
        var room = session.expected.stockroom().orElse(null);
        if (room != null) {
            // Slot preflight and vanilla payment run synchronously on this same server thread.
            if (count != 1 || !SettlementChestInventory.exchange((ServerLevel) resident.level(),
                    room.goods(), session.expected.result(), room.till(), payment)) {
                throw new IllegalStateException("Settlement stock changed during payment");
            }
            return;
        }
        session.data.completeTrade(resident.getUUID(), session.expected, count);
        session.expected = session.data.trade(resident.getUUID());
    }

    static void close(HumanEntity resident, SettlementTradeGui gui) {
        var session = resident.settlementTradeSession();
        if (session.gui == gui)
            session.clear();
    }

    private static boolean eligible(ServerLevel level, HumanEntity resident, ServerPlayer player,
                                    SettlementPilotData data, SettlementTrade offer) {
        if (!level.getServer().isSameThread() || !player.isAlive() || player.isSpectator() || player.level() != level
                || SettlementDutyGoal.isInterrupted(resident) || resident.isOnFire() || resident.hurtTime > 0
                || SettlementDutyGoal.hasNearbyFuse(level, resident)
                || resident.distanceToSqr(player) > 64 || !level.isPositionEntityTicking(offer.position())
                || resident.position().distanceToSqr(Vec3.atBottomCenterOf(offer.position())) > 1.0
                || data.haul(resident.getUUID()) != null)
            return false;
        var member = data.member(resident.getUUID());
        var layout = data.layoutFor(member);
        var duty = data.resolveDuty(member);
        if (!offer.matches(member, layout) || duty == null || duty.behavior() != SettlementPilotData.Behavior.HOLD_POST
                || !duty.sites().getFirst().equals(offer.site()))
            return false;
        var routine = layout.routines().get(member.role());
        return routine == null || routine.isWorking(level.getOverworldClockTime());
    }
}
