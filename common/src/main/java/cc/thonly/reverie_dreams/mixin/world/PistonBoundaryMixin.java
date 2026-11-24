package cc.thonly.reverie_dreams.mixin.world;

import cc.thonly.reverie_dreams.world.gensokyo.GensokyoBoundary;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Refuses piston moves that would push, pull or crush a block outside the fixed
 * Gensokyo map - sticky and normal pistons, extending and retracting alike.
 *
 * <p>The check sits on the structure resolver, so the whole move is rejected
 * before a single block is written. Cancelling the individual writes instead
 * would leave the piston half-moved with a dangling moving-block entity, and
 * would still allow a barrier block to be pulled into the map.</p>
 *
 * <p>Both the source positions ({@code toPush}, {@code toDestroy}, the head) and
 * their destinations ({@code pos.relative(pushDirection)}) are checked: a piston
 * one block inside the map can otherwise shove a block straight through the
 * barrier wall.</p>
 */
@Mixin(PistonStructureResolver.class)
public abstract class PistonBoundaryMixin {
    @Shadow
    @Final
    private Level level;
    @Shadow
    @Final
    private BlockPos startPos;
    @Shadow
    @Final
    private Direction pushDirection;
    @Shadow
    @Final
    private List<BlockPos> toPush;
    @Shadow
    @Final
    private List<BlockPos> toDestroy;

    @Inject(method = "resolve", at = @At("RETURN"), cancellable = true)
    private void reverie_dream$refuseBoundaryMoves(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            return;
        }
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (reverie_dream$isOutside(serverLevel, this.startPos)
                || reverie_dream$anyOutside(serverLevel, this.toPush)
                || reverie_dream$anyOutside(serverLevel, this.toDestroy)
                || reverie_dream$anyDestinationOutside(serverLevel, this.toPush, this.pushDirection)) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private static boolean reverie_dream$anyOutside(ServerLevel level, List<BlockPos> positions) {
        for (BlockPos pos : positions) {
            if (reverie_dream$isOutside(level, pos)) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private static boolean reverie_dream$anyDestinationOutside(ServerLevel level, List<BlockPos> positions,
                                                               Direction direction) {
        for (BlockPos pos : positions) {
            if (reverie_dream$isOutside(level, pos.relative(direction))) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private static boolean reverie_dream$isOutside(ServerLevel level, BlockPos pos) {
        return GensokyoBoundary.isOutsideMap(level, pos);
    }
}
