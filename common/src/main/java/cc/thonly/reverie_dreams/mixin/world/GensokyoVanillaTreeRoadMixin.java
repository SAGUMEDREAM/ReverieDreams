package cc.thonly.reverie_dreams.mixin.world;

import cc.thonly.reverie_dreams.world.gensokyo.road.GensokyoRoadProtection;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TreeFeature.class)
public abstract class GensokyoVanillaTreeRoadMixin {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void reverie_dream$preflightTree(FeaturePlaceContext<TreeConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
        if (GensokyoRoadProtection.vanillaTreeIntersects(context.level(), context.origin()))
            cir.setReturnValue(false);
    }
}
