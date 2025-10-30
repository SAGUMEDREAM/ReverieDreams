package cc.thonly.reverie_dreams.mixin.client;

import cc.thonly.reverie_dreams.TouhouClient;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoteBlock.class)
public class NoteBlockMixin {
    @Inject(method = "getStateForNeighborUpdate", cancellable = true, at = @At("HEAD"))
    public void neighborUpdateInject(BlockState state, LevelReader world, ScheduledTickAccess tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random, CallbackInfoReturnable<BlockState> cir) {
        if (world.isClientSide()) {
            if (state.getBlock() instanceof PolymerBlock)
                cir.setReturnValue(Blocks.AIR.defaultBlockState());
        }
    }

    @Inject(method = "onUse", cancellable = true, at = @At("HEAD"))
    public void onUseInject(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (world.isClientSide()) {
            if (TouhouClient.SERVER_SIDE_BLOCKS.contains(state.getBlock())) {
                cir.setReturnValue(InteractionResult.FAIL);
            }
        }
    }
}
