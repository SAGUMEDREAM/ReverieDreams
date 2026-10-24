package cc.thonly.reverie_dreams.neoforge.mixin;

import cc.thonly.reverie_dreams.neoforge.ReverieDreamsNeoForge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ComposterBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ComposterBlock.class)
public class ComposterBlockMixin {
    @Inject(method = "getValue", at = @At("RETURN"), cancellable = true)
    private static void modifyValue(ItemStack item, CallbackInfoReturnable<Float> cir) {
        Float value = cir.getReturnValue();
        if (value == -1f) {
            Float customValue = ReverieDreamsNeoForge.getCompostValue(item);
            if (customValue != null) {
                cir.setReturnValue(customValue);
            }
        }
    }
}
