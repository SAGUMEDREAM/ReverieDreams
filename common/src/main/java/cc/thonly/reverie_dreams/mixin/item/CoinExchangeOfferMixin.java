package cc.thonly.reverie_dreams.mixin.item;

import cc.thonly.reverie_dreams.util.item.CoinValues;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Fixed-rate conversion, including saved legacy offers and hero discounts.
 */
@Mixin(MerchantOffer.class)
public abstract class CoinExchangeOfferMixin {
    @Shadow
    @Final
    @Mutable
    private ItemCost baseCostA;
    @Shadow
    @Final
    private Optional<ItemCost> costB;
    @Shadow
    @Final
    @Mutable
    private ItemStack result;
    @Shadow
    @Final
    @Mutable
    private float priceMultiplier;
    @Shadow
    private int specialPriceDiff;
    @Unique
    private boolean reverieDreams$fixedExchange;

    @Unique
    private static int reverieDreams$value(Item item) {
        var id = BuiltInRegistries.ITEM.getKey(item);
        return CoinValues.denomination(id.getNamespace(), id.getPath());
    }

    @Inject(method = "<init>(Lnet/minecraft/world/item/trading/ItemCost;Ljava/util/Optional;Lnet/minecraft/world/item/ItemStack;IIZIIFI)V", at = @At("TAIL"))
    private void reverieDreams$normalizeExchange(ItemCost cost, Optional<ItemCost> second, ItemStack output,
                                                 int uses, int maxUses, boolean rewardExp, int discount, int demand, float multiplier, int xp, CallbackInfo ci) {
        int inputValue = reverieDreams$value(baseCostA.item().value());
        int outputValue = reverieDreams$value(result.getItem());
        if (costB.isPresent() || !CoinValues.adjacent(inputValue, outputValue))
            return;
        reverieDreams$fixedExchange = true;
        baseCostA = new ItemCost(baseCostA.item(), inputValue < outputValue ? CoinValues.EXCHANGE_RATIO : 1, baseCostA.components());
        result = result.copyWithCount(inputValue < outputValue ? 1 : CoinValues.EXCHANGE_RATIO);
        priceMultiplier = 0;
        specialPriceDiff = 0;
    }

    @Inject(method = {"addToSpecialPriceDiff", "setSpecialPriceDiff"}, at = @At("TAIL"))
    private void reverieDreams$keepFixedRate(int difference, CallbackInfo ci) {
        if (reverieDreams$fixedExchange)
            specialPriceDiff = 0;
    }
}
