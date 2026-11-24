package cc.thonly.reverie_dreams.mixin.world;

import cc.thonly.reverie_dreams.world.gensokyo.GensokyoBoundary;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps blasts from carving into the protected ring outside the fixed Gensokyo
 * map - the barrier wall and the border mountains behind it.
 *
 * <p>The position list is filtered <em>before</em> any block is touched, so a
 * protected block neither disappears nor drops its loot. Blocking the write
 * later would have been too late: vanilla collects the drops before it clears
 * the block, which would let TNT at the edge duplicate the border terrain.</p>
 */
@Mixin(ServerExplosion.class)
public abstract class ServerExplosionBoundaryMixin {
    @Inject(method = "calculateExplodedPositions", at = @At("RETURN"), cancellable = true)
    private void reverie_dream$keepBlastInsideMap(CallbackInfoReturnable<List<BlockPos>> cir) {
        List<BlockPos> positions = cir.getReturnValue();
        if (positions == null || positions.isEmpty()) {
            return;
        }
        ServerExplosion explosion = (ServerExplosion) (Object) this;
        List<BlockPos> kept = new ArrayList<>(positions.size());
        boolean removed = false;
        for (BlockPos pos : positions) {
            if (GensokyoBoundary.isOutsideMap(explosion.level(), pos)) {
                removed = true;
            } else {
                kept.add(pos);
            }
        }
        if (removed) {
            cir.setReturnValue(kept);
        }
    }
}
