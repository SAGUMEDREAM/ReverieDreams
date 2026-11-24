package cc.thonly.reverie_dreams.block.kitchen;

import cc.thonly.reverie_dreams.block.base.SeatBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChairBlock extends Block implements SeatBlock {
    public static final VoxelShape SHAPE = Block.column(14.0, 0, 8);
    public static final BooleanProperty OCCUPIED = SeatBlock.OCCUPIED;

    public ChairBlock(Properties properties) {
        super(properties);

        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(OCCUPIED, false)
        );
    }

    public static boolean sit(
            BlockState state,
            Level level,
            BlockPos pos,
            LivingEntity livingEntity
    ) {
        return SeatBlock.sit(state, level, pos, livingEntity);
    }

    public static boolean ejectSeat(
            BlockState state,
            Level level,
            BlockPos pos
    ) {
        return SeatBlock.ejectSeat(state, level, pos);
    }

    public static boolean unlockIfFree(
            BlockState state,
            Level level,
            BlockPos pos
    ) {
        return SeatBlock.unlockIfFree(state, level, pos);
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return SHAPE;
    }

    @Override
    public InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (!level.isClientSide()) {
            if (player.isShiftKeyDown()) {
                return ejectSeat(state, level, pos)
                        ? InteractionResult.SUCCESS_SERVER
                        : InteractionResult.FAIL;
            }

            if (state.getValue(OCCUPIED) && unlockIfFree(state, level, pos)) {
                state = level.getBlockState(pos);
            }

            if (state.getValue(OCCUPIED)) {
                return InteractionResult.FAIL;
            }

            return sit(state, level, pos, player)
                    ? InteractionResult.SUCCESS_SERVER
                    : InteractionResult.FAIL;
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(OCCUPIED);
    }
}
