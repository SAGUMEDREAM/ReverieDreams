package cc.thonly.reverie_dreams.mixin.world;

import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoChunkGenerator;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.SpringFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.tags.FluidTags;

@Mixin(SpringFeature.class)
public abstract class GensokyoLavaSpringMixin {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void reverie_dream$noRandomLava(FeaturePlaceContext<SpringConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
        if (context.config().state.is(FluidTags.LAVA)
                && context.level().getLevel().getChunkSource().getGenerator() instanceof GensokyoChunkGenerator)
            cir.setReturnValue(false);
    }
}
