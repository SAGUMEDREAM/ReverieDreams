package cc.thonly.reverie_dreams.entity.villager;

import cc.thonly.reverie_dreams.item.IngredientStack;
import eu.pb4.sgui.api.gui.MerchantGui;
import lombok.Getter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.List;

@Getter
public class SellerGui extends MerchantGui {
    private final AbstractSeller self;

    public SellerGui(ServerPlayer player, AbstractSeller self) {
        super(player, false);
        this.self = self;
        this.init();
    }

    public void init() {
        this.setTitle(this.self.getName());
        List<MerchantOffer> villagerOffers = this.self.getVillagerOffers();
        for (MerchantOffer offer : villagerOffers) {
            this.addTrade(offer);
        }
        if (villagerOffers.isEmpty()) {
            this.self.discard();
            this.close();
        }
    }

    public boolean canTrade(MerchantOffer offer) {
        return offer.getUses() < offer.getMaxUses();
    }

    @Override
    public boolean onTrade(MerchantOffer offer) {
        if (offer.getUses() >= offer.getMaxUses()) {
            return false;
        }

        MerchantOffer before = offer.copy();

        boolean success = super.onTrade(offer);

        if (!success) {
            return false;
        }

        this.self.trade(IngredientStack.of(before.assemble()));
        this.self.notifyTrade(before);

        return true;
    }

    @Override
    public void onOpen() {
        super.onOpen();
        this.self.getSessions().add(this);
    }

    @Override
    public void onTick() {
        super.onTick();
        this.self.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 20, false, false));
    }
}
