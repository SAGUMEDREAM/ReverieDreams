package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.block.entity.StrengthenTableBlockEntity;
import cc.thonly.reverie_dreams.gui.block.StrengthTableGui;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class StrengthenTableBlock extends BaseEntityBlock {

    public StrengthenTableBlock(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            player.swing(player.getUsedItemHand(), true);
            StrengthenTableBlockEntity blockEntity = (StrengthenTableBlockEntity) world.getBlockEntity(pos);
            StrengthTableGui gui = new StrengthTableGui(serverPlayer, blockEntity, false);
            gui.open();
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof StrengthenTableBlockEntity STBE) {
            SimpleContainer inventory = STBE.getInventory();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
                world.addFreshEntity(itemEntity);
            }
        }
        return super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(StrengthenTableBlock::new);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StrengthenTableBlockEntity(pos, state);
    }
}
