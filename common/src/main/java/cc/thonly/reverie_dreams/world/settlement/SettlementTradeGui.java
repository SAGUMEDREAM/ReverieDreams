package cc.thonly.reverie_dreams.world.settlement;

import cc.thonly.reverie_dreams.entity.HumanEntity;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.gui.MerchantGui;
import eu.pb4.sgui.impl.virtual.merchant.VirtualTradeOutputSlot;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.Optional;

/**
 * A single settlement offer rendered with the vanilla merchant payment slots.
 */
final class SettlementTradeGui extends MerchantGui {
    private static final int OUTPUT_SLOT = 2;

    private final ServerPlayer customer;
    private final HumanEntity resident;
    private final MerchantOffer merchantOffer;
    private final boolean physicalStock;

    SettlementTradeGui(ServerPlayer customer, HumanEntity resident, SettlementTrade offer) {
        super(customer, false);
        this.customer = customer;
        this.resident = resident;
        this.physicalStock = offer.stockroom().isPresent();

        ItemStack payment = offer.payment();
        ItemCost cost = new ItemCost(payment.getItem().builtInRegistryHolder(), payment.getCount(),
                DataComponentExactPredicate.allOf(payment.getComponents()));
        this.merchantOffer = new MerchantOffer(cost, Optional.empty(), offer.result(), offer.stock(), 0, 0);
        for (int i = 0; i < offer.uses(); i++) {
            this.merchantOffer.increaseUses();
        }

        this.setTitle(resident.getName());
        this.addTrade(this.merchantOffer);
        this.setSlot(OUTPUT_SLOT, new VirtualTradeOutputSlot(customer, this.merchant, this.merchantInventory,
                OUTPUT_SLOT, 0, 0) {
            @Override
            public boolean mayPickup(Player player) {
                MerchantOffer active = SettlementTradeGui.this.merchantInventory.getActiveOffer();
                return SettlementTradeGui.this.canTake(active, player) && super.mayPickup(player);
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                MerchantOffer active = SettlementTradeGui.this.merchantInventory.getActiveOffer();
                int usesBefore = active == null ? -1 : active.getUses();
                ItemStack payment = SettlementTradeGui.this.paymentBatch();
                super.onTake(player, stack);
                int usesAfter = active == null ? -1 : active.getUses();
                if (active != null && usesAfter > usesBefore) {
                    SettlementTradeSession.complete(SettlementTradeGui.this.resident, SettlementTradeGui.this.customer,
                            SettlementTradeGui.this, usesAfter - usesBefore, payment);
                }
            }
        });
    }

    private boolean canTake(MerchantOffer offer, Player player) {
        return offer == this.merchantOffer && player == this.customer
                && !this.merchantOffer.isOutOfStock()
                && SettlementTradeSession.canTake(this.resident, this.customer, this, this.paymentBatch());
    }

    private ItemStack paymentBatch() {
        if (!this.physicalStock)
            return ItemStack.EMPTY;
        ItemStack first = this.merchantInventory.getItem(0);
        ItemStack second = this.merchantInventory.getItem(1);
        if (!first.isEmpty() && !second.isEmpty())
            return ItemStack.EMPTY;
        ItemStack supplied = first.isEmpty() ? second : first;
        var cost = this.merchantOffer.getItemCostA();
        return supplied.getCount() >= cost.count() && cost.test(supplied)
                ? supplied.copyWithCount(cost.count()) : ItemStack.EMPTY;
    }

    @Override
    public boolean onAnyClick(int index, ClickType type, ContainerInput action) {
        if (index == OUTPUT_SLOT) {
            return type == ClickType.MOUSE_LEFT
                    && action == ContainerInput.PICKUP
                    && this.customer.containerMenu.getCarried().isEmpty()
                    && this.canTake(this.merchantInventory.getActiveOffer(), this.customer)
                    && super.onAnyClick(index, type, action);
        }
        return super.onAnyClick(index, type, action);
    }

    @Override
    public boolean onTrade(MerchantOffer offer) {
        return this.canTake(offer, this.customer) && super.onTrade(offer);
    }

    @Override
    public void onTick() {
        if (!SettlementTradeSession.valid(this.resident, this.customer, this)) {
            this.close();
            return;
        }
        super.onTick();
        this.resident.getLookControl().setLookAt(this.customer, 30, 30);
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        SettlementTradeSession.close(this.resident, this);
    }
}
