package cc.thonly.reverie_dreams.polymer.mixin.client;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.util.PlatformContext;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public class AbstractBlockMixin {
    @Inject(method = "updateShape", cancellable = true, at = @At("HEAD"))
    public void neighborUpdateInject(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random, CallbackInfoReturnable<BlockState> cir) {
        if (world.isClientSide()) {
            if (!PlatformContext.hasPolymer()) {
                return;
            }
            if (Minecraft.getInstance().getSingleplayerServer() != null)
                if (Minecraft.getInstance().getSingleplayerServer().isDedicatedServer())
                    if (ReverieDreams.SERVER_SIDE_BLOCKS.contains(state.getBlock()))
                        cir.setReturnValue(Blocks.AIR.defaultBlockState());
        }
    }

    @Inject(method = "useWithoutItem", cancellable = true, at = @At("HEAD"))
    public void onUseInject(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (world.isClientSide()) {
            if (!PlatformContext.hasPolymer()) {
                return;
            }
            if (Minecraft.getInstance().getSingleplayerServer() != null) {
               if (Minecraft.getInstance().getSingleplayerServer().isDedicatedServer()) {
                    if (ReverieDreams.SERVER_SIDE_BLOCKS.contains(state.getBlock())) {
                        cir.setReturnValue(InteractionResult.FAIL);
                    }
                }
            }

        }
    }
}