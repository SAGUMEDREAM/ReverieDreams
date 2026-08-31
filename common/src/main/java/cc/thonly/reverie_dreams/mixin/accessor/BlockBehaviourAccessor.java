package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@SuppressWarnings("UnusedReturnValue")
@Mixin(BlockBehaviour.class)
public interface BlockBehaviourAccessor {
    @Invoker("useItemOn")
    InteractionResult reverie_dreams$useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult);

    @Invoker("useWithoutItem")
    InteractionResult reverie_dreams$useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult);
}
