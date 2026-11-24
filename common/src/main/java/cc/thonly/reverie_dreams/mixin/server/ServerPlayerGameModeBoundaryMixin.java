package cc.thonly.reverie_dreams.mixin.server;

import cc.thonly.reverie_dreams.world.gensokyo.BoundaryActionContext;
import cc.thonly.reverie_dreams.world.gensokyo.GensokyoBoundary;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Player-only boundary protection: block breaking, block placement/item use,
 * and item use on blocks are all prevented outside the fixed Gensokyo map.
 * The context flag lets Level.setBlock distinguish these actions from world
 * generation and non-player writes.
 */
@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeBoundaryMixin {
    @Shadow
    @Final
    protected ServerLevel level;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void reverie_dream$protectGensokyoDestroy(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (GensokyoBoundary.isOutsideMap(this.level, pos)) {
            cir.setReturnValue(false);
            return;
        }
        BoundaryActionContext.enter();
    }

    @Inject(method = "destroyBlock", at = @At("RETURN"))
    private void reverie_dream$endProtectGensokyoDestroy(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        BoundaryActionContext.exit();
    }

    @Inject(method = "useItem", at = @At("HEAD"))
    private void reverie_dream$enterPlayerItemUse(
            ServerPlayer player, Level level, ItemStack stack, InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        BoundaryActionContext.enter();
    }

    @Inject(method = "useItem", at = @At("RETURN"))
    private void reverie_dream$exitPlayerItemUse(
            ServerPlayer player, Level level, ItemStack stack, InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        BoundaryActionContext.exit();
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void reverie_dream$protectGensokyoUseOn(
            ServerPlayer player, Level level, ItemStack stack, InteractionHand hand,
            BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir
    ) {
        BlockPos clicked = hit.getBlockPos();
        BlockPos placement = clicked.relative(hit.getDirection());
        if (GensokyoBoundary.isOutsideMap(this.level, clicked)
                || GensokyoBoundary.isOutsideMap(this.level, placement)) {
            cir.setReturnValue(InteractionResult.FAIL);
            return;
        }
        BoundaryActionContext.enter();
    }

    @Inject(method = "useItemOn", at = @At("RETURN"))
    private void reverie_dream$exitPlayerUseOn(
            ServerPlayer player, Level level, ItemStack stack, InteractionHand hand,
            BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir
    ) {
        BoundaryActionContext.exit();
    }
}