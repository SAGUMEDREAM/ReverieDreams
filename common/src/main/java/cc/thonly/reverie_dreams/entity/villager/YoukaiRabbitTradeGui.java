package cc.thonly.reverie_dreams.entity.villager;

import cc.thonly.reverie_dreams.entity.YoukaiRabbit;
import cc.thonly.reverie_dreams.entity.variant.YoukaiRabbitTradeState;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import eu.pb4.sgui.api.gui.MerchantGui;
import eu.pb4.sgui.impl.virtual.merchant.VirtualTradeOutputSlot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

/** Same merchant GUI/vanilla payment slots as SellerGui, without inheriting WanderingTrader AI. */
public final class YoukaiRabbitTradeGui extends MerchantGui {
    private final YoukaiRabbit rabbit;
    private final ServerPlayer customer;
    private final long day;
    private final Map<MerchantOffer, YoukaiRabbitTradeState.Offer> entries = new IdentityHashMap<>();

    public YoukaiRabbitTradeGui(ServerPlayer player, YoukaiRabbit rabbit) {
        super(player, false);
        this.rabbit = rabbit;
        this.customer = player;
        this.day = rabbit.tradeDay();
        rabbit.tradeState().refresh(this.day);
        this.setTitle(rabbit.getName());
        for (var entry : YoukaiRabbitTradeState.dailyOffers(rabbit.getUUID(), this.day)) {
            ItemLike goods = goods(entry);
            ItemLike coin = entry.currency == YoukaiRabbitTradeState.Currency.COPPER ? RDItems.COPPER_COIN : RDItems.SILVER_COIN;
            ItemCost cost = new ItemCost(entry.buying ? goods : coin, entry.buying ? entry.count : entry.price);
            ItemStack result = new ItemStack(entry.buying ? coin : goods, entry.buying ? entry.price : entry.count);
            MerchantOffer offer = new MerchantOffer(cost, Optional.empty(), result, entry.stock, 0, 0);
            for (int i = 0; i < rabbit.tradeState().sold(entry); i++) offer.increaseUses();
            this.entries.put(offer, entry);
            this.addTrade(offer);
        }
        this.setSlot(2, new VirtualTradeOutputSlot(player, this.merchant, this.merchantInventory, 2, 0, 0) {
            @Override public void onTake(Player player, ItemStack stack) {
                MerchantOffer offer = YoukaiRabbitTradeGui.this.merchantInventory.getActiveOffer();
                int uses = offer == null ? 0 : offer.getUses();
                super.onTake(player, stack);
                if (offer != null && offer.getUses() > uses)
                    YoukaiRabbitTradeGui.this.completed(offer, offer.getUses() - uses);
            }
        });
    }

    private static ItemLike goods(YoukaiRabbitTradeState.Offer offer) {
        return switch (offer) {
            case BAMBOO -> Items.BAMBOO;
            case BAMBOO_SHOOTS -> RDIngredientItems.BAMBOO_SHOOTS;
            case RED_MUSHROOM -> Items.RED_MUSHROOM;
            case BROWN_MUSHROOM -> Items.BROWN_MUSHROOM;
            case PORK -> Items.PORKCHOP;
            case BLACK_PORK -> RDIngredientItems.BLACK_PORK;
            case UDUMBARA_FLOWER -> RDWoodBlocks.UDUMBARA_FLOWER;
            case TRUFFLE -> RDIngredientItems.TRUFFLE;
            case CARROT -> Items.CARROT;
            case MELON_SLICE -> Items.MELON_SLICE;
        };
    }

    private boolean valid() {
        return this.rabbit.isCurrentTrade(this) && this.rabbit.canTradeWith(this.customer) && this.rabbit.tradeDay() == this.day;
    }
    @Override public boolean onTrade(MerchantOffer offer) {
        var entry = this.entries.get(offer);
        return this.valid() && entry != null && offer.getUses() < offer.getMaxUses()
                && this.rabbit.tradeState().available(entry, this.day) && super.onTrade(offer);
    }
    private void completed(MerchantOffer offer, int count) {
        var entry = this.entries.get(offer);
        if (entry == null) return;
        for (int i = 0; i < count; i++) this.rabbit.completeTrade(this.customer, entry, this.day);
    }
    @Override public void onTick() {
        if (!this.valid()) { this.close(); return; }
        super.onTick();
        this.rabbit.getNavigation().stop();
        this.rabbit.getLookControl().setLookAt(this.customer, 30, 30);
    }
    @Override public void onRemoved() {
        super.onRemoved();
        this.rabbit.closeTrade(this);
    }
}
