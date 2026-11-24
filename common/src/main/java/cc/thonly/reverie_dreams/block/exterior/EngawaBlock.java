package cc.thonly.reverie_dreams.block.exterior;

import cc.thonly.reverie_dreams.block.decor.TatamiBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A Japanese veranda platform, 3/16 of a block tall.
 *
 * <p>Adjacent engawa blocks join seamlessly. Unlike tatami there is no 2x2
 * cap, because verandas form long strips along the house edge. The connection
 * flags are shared with {@link TatamiBlock} so the datagen connection block
 * state wheel can dispatch on the same properties.</p>
 */
public class EngawaBlock extends Block {
    public static final MapCodec<EngawaBlock> CODEC = simpleCodec(EngawaBlock::new);

    private static final VoxelShape PLATFORM_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0);

    public EngawaBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(TatamiBlock.NORTH, false)
                .setValue(TatamiBlock.EAST, false)
                .setValue(TatamiBlock.SOUTH, false)
                .setValue(TatamiBlock.WEST, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = this.defaultBlockState();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(pos.relative(direction)).getBlock() == this) {
                state = state.setValue(property(direction), true);
            }
        }
        return state;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        if (direction.getAxis().isHorizontal()) {
            // Engawa always joins adjacent engawa and separates otherwise.
            return state.setValue(property(direction), neighborState.getBlock() == this);
        }
        return state;
    }

    private static BooleanProperty property(Direction direction) {
        return switch (direction) {
            case EAST -> TatamiBlock.EAST;
            case SOUTH -> TatamiBlock.SOUTH;
            case WEST -> TatamiBlock.WEST;
            default -> TatamiBlock.NORTH;
        };
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide() && !oldState.is(state.getBlock())) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos neighborPos = pos.relative(direction);
                BlockState neighbor = level.getBlockState(neighborPos);
                if (neighbor.getBlock() == this && !neighbor.getValue(property(direction.getOpposite()))) {
                    level.setBlock(neighborPos,
                            neighbor.setValue(property(direction.getOpposite()), true),
                            Block.UPDATE_ALL);
                }
            }
        }
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighbor = level.getBlockState(neighborPos);
            if (neighbor.getBlock() != this) {
                continue;
            }
            BlockState updated = neighbor.setValue(property(direction.getOpposite()), false);
            if (updated != neighbor) {
                level.setBlock(neighborPos, updated, Block.UPDATE_ALL);
            }
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return PLATFORM_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return PLATFORM_SHAPE;
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return PLATFORM_SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TatamiBlock.NORTH, TatamiBlock.EAST, TatamiBlock.SOUTH, TatamiBlock.WEST);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }
}
