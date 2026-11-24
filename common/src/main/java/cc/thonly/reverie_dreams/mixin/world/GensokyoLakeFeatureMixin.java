package cc.thonly.reverie_dreams.mixin.world;

import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoChunkGenerator;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import cc.thonly.reverie_dreams.world.gen.feature.LakeFeature;

/**
 * Authored fixed-map lakes/volcanoes come from map columns, never this feature.
 */
@Mixin({net.minecraft.world.level.levelgen.feature.LakeFeature.class,
        LakeFeature.class})
public abstract class GensokyoLakeFeatureMixin {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void reverie_dream$fixedMapLake(FeaturePlaceContext<?> context, CallbackInfoReturnable<Boolean> cir) {
        if (context.level().getLevel().getChunkSource().getGenerator() instanceof GensokyoChunkGenerator)
            cir.setReturnValue(false);
    }
}
