package cc.thonly.reverie_dreams.mixin.world;

import cc.thonly.reverie_dreams.world.gensokyo.road.GensokyoRoadProtection;
import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldGenRegion.class)
public abstract class GensokyoRoadRegionMixin {
    @Inject(method = "ensureCanWrite", at = @At("HEAD"), cancellable = true)
    private void reverie_dream$roadSpace(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        var region=(WorldGenRegion)(Object)this;
        if(region.getLevel().getChunkSource().getGenerator() instanceof GensokyoChunkGenerator generator
                &&generator.authoredStructures(region.getLevel()).reserved(pos)){cir.setReturnValue(false);return;}
        if (GensokyoRoadProtection.reserved((WorldGenRegion) (Object) this, pos)) cir.setReturnValue(false);
    }
}
