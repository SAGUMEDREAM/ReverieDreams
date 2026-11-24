package cc.thonly.reverie_dreams.block.decor.workshop;

import cc.thonly.reverie_dreams.block.entity.WorkshopStationBlockEntity;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.material.PushReaction;

/**
 * A two-block column owns its inventory; joining never moves items between columns.
 */
public class WorkshopRackBlock extends WorkshopStationBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final IntegerProperty WIDTH = IntegerProperty.create("rack_width", 1, 3);
    public static final IntegerProperty INDEX = IntegerProperty.create("rack_index", 0, 2);
    private static final Map<Direction, VoxelShape> SELECTION = Shapes.rotateHorizontal(Block.box(0, 0, 3, 16, 16, 13));
    public static final MapCodec<WorkshopRackBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            StringRepresentable.fromEnum(WorkshopKind::values).fieldOf("kind").forGetter(b -> b.kind),
            propertiesCodec()).apply(i, WorkshopRackBlock::new));

    public WorkshopRackBlock(WorkshopKind kind, Properties properties) {
        super(kind, properties.pushReaction(PushReaction.BLOCK));
        registerDefaultState(defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER).setValue(WIDTH, 1).setValue(INDEX, 0));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HALF, WIDTH, INDEX);
    }

    public static BlockPos inventoryPos(BlockState state, BlockPos pos) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? super.newBlockEntity(pos, state) : null;
    }

    @Override
    protected String model(BlockState state) {
        int width = state.getValue(WIDTH), index = state.getValue(INDEX);
        String section = width == 1 ? "single" : index == 0 ? "left" : index == width - 1 ? "right" : "middle";
        return kind.id() + "_" + state.getValue(HALF).getSerializedName() + "_" + section;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Hung sprites have no entity hitbox. Include their display area so players can click the items.
        return SELECTION.get(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return WorkshopShapes.shape(model(state), state.getValue(FACING));
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getCollisionShape(state, level, pos, context);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos above = context.getClickedPos().above();
        if (above.getY() > level.getMaxY() || !level.getWorldBorder().isWithinBounds(above)
                || !level.getBlockState(above).canBeReplaced(context)
                || context.getPlayer() != null && !context.getPlayer().mayUseItemAt(above, context.getClickedFace(), context.getItemInHand()))
            return null;
        return super.getStateForPlacement(context);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), UPDATE_ALL);
        if (!level.isClientSide())
            join(level, pos);
    }

    private boolean lower(BlockState state, Direction facing) {
        return state.is(this) && state.getValue(HALF) == DoubleBlockHalf.LOWER && state.getValue(FACING) == facing;
    }

    private boolean completeColumn(LevelReader level, BlockPos pos, Direction facing) {
        BlockState upper = level.getBlockState(pos.above());
        return upper.is(this) && upper.getValue(HALF) == DoubleBlockHalf.UPPER && upper.getValue(FACING) == facing;
    }

    private List<BlockPos> group(LevelReader level, BlockPos pos, Direction facing) {
        var state = level.getBlockState(pos);
        if (!lower(state, facing) || !completeColumn(level, pos, facing))
            return List.of();
        Direction right = facing.getClockWise();
        var result = new ArrayList<BlockPos>();
        BlockPos start = pos;
        BlockState first = state;
        for (int n = 0; n < 2 && first.getValue(INDEX) > 0; n++) {
            BlockPos previous = start.relative(right.getOpposite());
            BlockState other = level.getBlockState(previous);
            if (!lower(other, facing) || !completeColumn(level, previous, facing) || other.getValue(WIDTH) != first.getValue(WIDTH)
                    || other.getValue(INDEX) != first.getValue(INDEX) - 1)
                break;
            start = previous;
            first = other;
        }
        result.add(start);
        for (int n = 1; n < 3; n++) {
            BlockPos next = start.relative(right, n);
            BlockState previous = level.getBlockState(result.getLast()), other = level.getBlockState(next);
            if (!lower(other, facing) || !completeColumn(level, next, facing) || other.getValue(WIDTH) != previous.getValue(WIDTH)
                    || other.getValue(INDEX) != previous.getValue(INDEX) + 1 || other.getValue(INDEX) >= other.getValue(WIDTH))
                break;
            result.add(next);
        }
        return result;
    }

    private void resize(Level level, List<BlockPos> columns) {
        if (columns.isEmpty())
            return;
        boolean changed = false;
        // Publish the entire group before neighbor notifications: no transient split or inventory replacement.
        for (int i = 0; i < columns.size(); i++) {
            BlockPos pos = columns.get(i);
            BlockState state = level.getBlockState(pos).setValue(WIDTH, columns.size()).setValue(INDEX, i);
            changed |= level.setBlock(pos, state, UPDATE_CLIENTS);
            BlockState upper = level.getBlockState(pos.above());
            if (upper.is(this) && upper.getValue(HALF) == DoubleBlockHalf.UPPER)
                changed |= level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), UPDATE_CLIENTS);
        }
        if (!changed)
            return;
        for (BlockPos pos : columns) {
            level.updateNeighborsAt(pos, this);
            level.updateNeighborsAt(pos.above(), this);
        }
    }

    private void join(Level level, BlockPos pos) {
        Direction facing = level.getBlockState(pos).getValue(FACING), right = facing.getClockWise();
        List<BlockPos> left = group(level, pos.relative(right.getOpposite()), facing);
        List<BlockPos> next = group(level, pos.relative(right), facing);
        if (!left.isEmpty() && !left.getLast().equals(pos.relative(right.getOpposite())))
            left = List.of();
        if (!next.isEmpty() && !next.getFirst().equals(pos.relative(right)))
            next = List.of();
        var columns = new ArrayList<BlockPos>();
        if (left.size() + next.size() + 1 <= 3) {
            columns.addAll(left);
            columns.add(pos);
            columns.addAll(next);
        } else if (left.size() < 3 && !left.isEmpty()) {
            columns.addAll(left);
            columns.add(pos);
        } else if (next.size() < 3 && !next.isEmpty()) {
            columns.add(pos);
            columns.addAll(next);
        } else
            columns.add(pos);
        resize(level, columns);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER && direction == Direction.DOWN)
            return lower(neighbor, state.getValue(FACING)) ? neighbor.setValue(HALF, DoubleBlockHalf.UPPER) : Blocks.AIR.defaultBlockState();
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER && direction.getAxis().isHorizontal())
            ticks.scheduleTick(pos, this, 1);
        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER)
            resize(level, group(level, pos, state.getValue(FACING)));
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        BlockPos partner = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos.above();
        BlockState other = level.getBlockState(partner);
        if (other.is(this) && other.getValue(HALF) != state.getValue(HALF) && other.getValue(FACING) == state.getValue(FACING))
            level.destroyBlock(partner, other.getValue(HALF) == DoubleBlockHalf.LOWER);
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player.preventsBlockDrops() && state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockPos bottom = pos.below();
            if (lower(level.getBlockState(bottom), state.getValue(FACING)))
                level.setBlock(bottom, Blocks.AIR.defaultBlockState(), UPDATE_ALL | UPDATE_SUPPRESS_DROPS);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        BlockState mirrored = super.mirror(state, mirror);
        return mirror == Mirror.NONE ? mirrored : mirrored.setValue(INDEX, Math.max(0, state.getValue(WIDTH) - 1 - state.getValue(INDEX)));
    }

    /**
     * Existing one-block saves gain an upper half on first normal use if there is room. Never overwrite blocks.
     */
    private void restoreUpper(Level level, BlockPos bottom, Player player) {
        BlockState state = level.getBlockState(bottom), upper = level.getBlockState(bottom.above());
        if (!level.isClientSide() && lower(state, state.getValue(FACING)) && bottom.getY() < level.getMaxY()
                && upper.canBeReplaced() && level.getWorldBorder().isWithinBounds(bottom.above()) && level.mayInteract(player, bottom.above())) {
            level.setBlock(bottom.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), UPDATE_ALL);
            join(level, bottom);
        }
    }

    public InteractionResult take(Level level, BlockPos pos, BlockState state, Player player) {
        if (player.isSpectator() || !player.mayBuild() || !level.mayInteract(player, pos))
            return InteractionResult.FAIL;
        BlockPos bottom = inventoryPos(state, pos);
        if (!level.mayInteract(player, bottom))
            return InteractionResult.FAIL;
        if (!(level.getBlockEntity(bottom) instanceof WorkshopStationBlockEntity host))
            return InteractionResult.FAIL;
        if (host.isEmpty())
            return InteractionResult.PASS;
        if (!level.isClientSide())
            host.take(player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown())
            return hand == InteractionHand.MAIN_HAND ? take(level, pos, state, player) : InteractionResult.CONSUME;
        // Let the held rack place an adjacent column instead of opening the reading dialog.
        if (stack.getItem() instanceof BlockItem item && item.getBlock() == this)
            return InteractionResult.PASS;
        BlockPos bottom = inventoryPos(state, pos);
        if (!level.mayInteract(player, pos) || !level.mayInteract(player, bottom))
            return InteractionResult.FAIL;
        if (!player.isSpectator() && player.mayBuild())
            restoreUpper(level, bottom, player);
        return super.useItemOn(stack, level.getBlockState(bottom), level, bottom, player, hand, hit);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown())
            return take(level, pos, state, player);
        BlockPos bottom = inventoryPos(state, pos);
        if (!level.mayInteract(player, pos) || !level.mayInteract(player, bottom))
            return InteractionResult.FAIL;
        if (!player.isSpectator() && player.mayBuild())
            restoreUpper(level, bottom, player);
        return super.useWithoutItem(level.getBlockState(bottom), level, bottom, player, hit);
    }
}
