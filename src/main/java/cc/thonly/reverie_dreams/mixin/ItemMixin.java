package cc.thonly.reverie_dreams.mixin;

import cc.thonly.minecraft.api.ItemPostHitCallback;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
public abstract class ItemMixin implements FeatureElement, ItemLike, FabricItem {
    @Inject(method = "hurtEnemy", at = @At("TAIL"))
    public void postHitCallback(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {
        ItemPostHitCallback.EVENT.invoker().postHit(stack, target, attacker);
    }
}
