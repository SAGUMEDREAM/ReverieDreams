package cc.thonly.reverie_dreams.mixin.client;

import cc.thonly.reverie_dreams.client.renderer.item.FumoHeldItemRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemModelResolver.class)
public class FumoItemModelResolverMixin {
    @Inject(method = "updateForTopItem", at = @At("TAIL"))
    private void reverie_dreams$scaleHeldFumo(ItemStackRenderState output, ItemStack stack, ItemDisplayContext context,
                                              @Nullable Level level, @Nullable ItemOwner owner, int seed, CallbackInfo ci) {
        FumoHeldItemRenderer.apply(output, stack, context);
    }
}
