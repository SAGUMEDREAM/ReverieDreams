package cc.thonly.reverie_dreams.block.kitchen;

import cc.thonly.reverie_dreams.entity.SeatEntity;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public class ChairBlock extends Block {
    public static final VoxelShape SHAPE = Block.column(14.0, 0, 8);
    public static final BooleanProperty OCCUPIED = BooleanProperty.create("occupied");

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
        if (!(state.getBlock() instanceof ChairBlock)) {
            return false;
        }

        if (!state.hasProperty(OCCUPIED)) {
            return false;
        }

        if (state.getValue(OCCUPIED) && unlockIfFree(state, level, pos)) {
            state = level.getBlockState(pos);
        }

        if (state.getValue(OCCUPIED)) {
            return false;
        }

        SeatEntity chair = new SeatEntity(level);

        chair.setPos(
                pos.getX() + 0.5,
                pos.getY(),
                pos.getZ() + 0.5
        );

        level.addFreshEntity(chair);

        if (!livingEntity.startRiding(chair)) {
            chair.discard();
            return false;
        }

        level.setBlock(
                pos,
                state.setValue(OCCUPIED, true),
                Block.UPDATE_ALL
        );

        return true;
    }

    public static boolean ejectSeat(
            BlockState state,
            Level level,
            BlockPos pos
    ) {
        if (!(state.getBlock() instanceof ChairBlock)) {
            return false;
        }

        AABB box = new AABB(pos).inflate(0.25D);

        List<SeatEntity> entities = level.getEntitiesOfClass(
                SeatEntity.class,
                box
        );

        if (entities.isEmpty()) {
            if (state.getValue(OCCUPIED)) {
                level.setBlock(
                        pos,
                        state.setValue(OCCUPIED, false),
                        Block.UPDATE_ALL
                );
            }
            return false;
        }

        boolean ejected = false;

        for (SeatEntity seat : entities) {
            if (!seat.getPassengers().isEmpty()) {
                seat.ejectPassengers();
                ejected = true;
            }
        }

        return ejected;
    }

    public static boolean unlockIfFree(
            BlockState state,
            Level level,
            BlockPos pos
    ) {
        if (!state.hasProperty(OCCUPIED)
                || !state.getValue(OCCUPIED)) {
            return false;
        }

        if (hasSeatEntity(level, pos)) {
            return false;
        }

        level.setBlock(
                pos,
                state.setValue(OCCUPIED, false),
                Block.UPDATE_ALL
        );

        return true;
    }

    private static boolean hasSeatEntity(
            Level level,
            BlockPos pos
    ) {
        AABB box = new AABB(pos).inflate(1);

        List<SeatEntity> entities =
                level.getEntitiesOfClass(
                        SeatEntity.class,
                        box
                );

        return !entities.isEmpty();
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
            this.updateState(state, level, pos);

            if (state.getValue(OCCUPIED)) {
                if (unlockIfFree(state, level, pos)) {
                    state = level.getBlockState(pos);
                }
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

    private void updateState(BlockState state, Level level, BlockPos pos) {
        if (hasSeatEntity(level, pos)) {
            level.setBlock(
                    pos,
                    state.setValue(OCCUPIED, true),
                    Block.UPDATE_ALL
            );
        }
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(OCCUPIED);
    }
}