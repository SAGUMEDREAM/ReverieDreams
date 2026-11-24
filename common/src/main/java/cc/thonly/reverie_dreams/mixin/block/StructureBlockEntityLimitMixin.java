package cc.thonly.reverie_dreams.mixin.block;

import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Vanilla clamps a loaded structure block's size to 0..48 and its offset to +-48 in
 * {@code loadAdditional}; the screen itself already accepts any value in this version.
 * Keep those NBT values intact so large saved buildings survive a chunk reload.
 */
@Mixin(StructureBlockEntity.class)
public abstract class StructureBlockEntityLimitMixin {
    @Redirect(
            method = "loadAdditional",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I")
    )
    private static int reverie_dreams$removeStructureSizeLimit(int value, int min, int max) {
        if (max != 48) {
            return Mth.clamp(value, min, max);
        }
        return min == 0 ? Math.max(value, 0) : value;
    }
}
