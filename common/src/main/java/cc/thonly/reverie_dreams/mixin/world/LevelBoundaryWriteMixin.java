package cc.thonly.reverie_dreams.mixin.world;

import cc.thonly.reverie_dreams.world.gensokyo.BoundaryActionContext;
import cc.thonly.reverie_dreams.world.gensokyo.GensokyoBoundary;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Reject any block write outside the Gensokyo map while a player action is
 * running. This covers indirect writes from items such as buckets, bone meal,
 * hoes, and block placement, while leaving world generation untouched.
 */
@SuppressWarnings("ConstantValue")
@Mixin(Level.class)
public abstract class LevelBoundaryWriteMixin {
    @Inject(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void reverie_dream$protectPlayerWrites(
            BlockPos pos, BlockState state, int flags,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (((Object) this) instanceof ServerLevel serverLevel
                && BoundaryActionContext.active()
                && GensokyoBoundary.isOutsideMap(serverLevel, pos)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void reverie_dream$protectPlayerWritesWithRecursion(
            BlockPos pos, BlockState state, int flags, int recursion,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (((Object) this) instanceof ServerLevel serverLevel
                && BoundaryActionContext.active()
                && GensokyoBoundary.isOutsideMap(serverLevel, pos)) {
            cir.setReturnValue(false);
        }
    }
}