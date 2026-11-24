package cc.thonly.reverie_dreams.world.settlement;

import cc.thonly.reverie_dreams.entity.HumanEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/**
 * An explicit exchange between two enrolled residents at a local handoff.
 */
public final class SettlementNPCExchange {
    private static final double MAX_HANDOFF_DISTANCE_SQR = 9.0;

    private SettlementNPCExchange() {
    }

    public static boolean exchange(Mob seller, Mob buyer, BlockPos paymentBox, BlockPos receiveBox) {
        if (!(seller instanceof HumanEntity merchant) || seller == buyer || !(seller.level() instanceof ServerLevel level)
                || buyer.level() != level || !level.getServer().isSameThread()
                || seller.distanceToSqr(buyer) > MAX_HANDOFF_DISTANCE_SQR
                || !ready(level, seller) || !ready(level, buyer))
            return false;

        var data = SettlementPilotData.existingFor(seller);
        if (data == null || data != SettlementPilotData.existingFor(buyer)
                || data.haul(seller.getUUID()) != null || data.haul(buyer.getUUID()) != null)
            return false;
        var sellerMember = data.member(seller.getUUID());
        var buyerMember = data.member(buyer.getUUID());
        var offer = data.trade(merchant.getUUID());
        if (offer == null || sellerMember == null || buyerMember == null
                || !sellerMember.settlement().equals(buyerMember.settlement()))
            return false;

        var layout = data.layoutFor(sellerMember);
        var sellerDuty = data.resolveDuty(sellerMember);
        var buyerDuty = data.resolveDuty(buyerMember);
        var room = offer.stockroom().orElse(null);
        if (room == null || !offer.matches(sellerMember, layout) || sellerDuty == null
                || sellerDuty.behavior() != SettlementPilotData.Behavior.HOLD_POST
                || !sellerDuty.sites().getFirst().equals(offer.site()) || buyerDuty == null
                || !working(level, layout, sellerMember) || !working(level, layout, buyerMember)
                || !at(level, seller, offer.position()))
            return false;

        for (var site : buyerDuty.sites()) {
            BlockPos station = layout.sites().get(site);
            if (station == null || station.equals(offer.position()) || !at(level, buyer, station)
                    || !adjacent(station, paymentBox) || !adjacent(station, receiveBox))
                continue;
            return SettlementChestInventory.exchangeBetween(level, room.goods(), room.till(), paymentBox,
                    receiveBox, offer.result(), offer.payment());
        }
        return false;
    }

    private static boolean ready(ServerLevel level, Mob mob) {
        return !SettlementDutyGoal.isInterrupted(mob) && !mob.isOnFire() && mob.hurtTime == 0
                && !SettlementDutyGoal.hasNearbyFuse(level, mob)
                && (!(mob instanceof HumanEntity human) || !human.settlementTradeSession().isActive());
    }

    private static boolean at(ServerLevel level, Mob mob, BlockPos position) {
        return level.isPositionEntityTicking(position)
                && mob.position().distanceToSqr(Vec3.atBottomCenterOf(position)) <= 0.85 * 0.85;
    }

    private static boolean adjacent(BlockPos station, BlockPos chest) {
        return chest != null && station.getY() == chest.getY() && station.distManhattan(chest) == 1;
    }

    private static boolean working(ServerLevel level, SettlementPilotData.Layout layout, SettlementPilotData.Member member) {
        var routine = layout.routines().get(member.role());
        return routine == null || routine.isWorking(level.getOverworldClockTime());
    }
}
