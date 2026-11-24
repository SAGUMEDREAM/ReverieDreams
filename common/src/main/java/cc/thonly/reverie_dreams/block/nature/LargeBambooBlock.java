package cc.thonly.reverie_dreams.block.nature;

import cc.thonly.reverie_dreams.registry.content.block.RDBambooBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

/**
 * An independent species: never changes vanilla bamboo, its tags, loot or growth callbacks.
 */
public final class LargeBambooBlock extends VegetationBlock implements BonemealableBlock {
    public static final MapCodec<LargeBambooBlock> CODEC = simpleCodec(LargeBambooBlock::new);
    public static final EnumProperty<BambooLeaves> LEAVES = BlockStateProperties.BAMBOO_LEAVES;
    public static final BooleanProperty GROWING = BooleanProperty.create("growing");
    public static final int MAX_GROWN_HEIGHT = 16;
    private static final VoxelShape STEM = box(5, 0, 5, 11, 16, 11);

    public LargeBambooBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LEAVES, BambooLeaves.LARGE).setValue(GROWING, true));
    }

    @Override
    public MapCodec<LargeBambooBlock> codec() {
        return CODEC;
    }

    public static boolean soil(BlockState state) {
        return state.is(BlockTags.SUBSTRATE_OVERWORLD) || state.is(Blocks.SAND) || state.is(Blocks.RED_SAND);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(this) || soil(level.getBlockState(pos.below()));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        return context.getLevel().getFluidState(pos).isEmpty() && canSurvive(defaultBlockState(), context.getLevel(), pos) ? defaultBlockState() : null;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return STEM;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return STEM;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighbourPos, BlockState neighbour, RandomSource random) {
        if (!canSurvive(state, level, pos))
            ticks.scheduleTick(pos, this, 1);
        if (direction == Direction.UP)
            return state.setValue(GROWING, !neighbour.is(this)).setValue(LEAVES,
                    neighbour.is(this) ? (neighbour.getValue(LEAVES) == BambooLeaves.LARGE ? BambooLeaves.SMALL : BambooLeaves.NONE) : BambooLeaves.LARGE);
        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canSurvive(state, level, pos))
            level.destroyBlock(pos, true);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(GROWING);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(5) == 0 && level.getRawBrightness(pos.above(), 0) >= 9)
            grow(level, pos);
    }

    public static int heightBelow(LevelReader level, BlockPos pos, Block block) {
        int height = 1;
        while (height < 32 && level.getBlockState(pos.below(height)).is(block))
            height++;
        return height;
    }

    private BlockPos top(LevelReader level, BlockPos pos) {
        BlockPos top = pos;
        for (int i = 0; i < 31 && level.getBlockState(top.above()).is(this); i++)
            top = top.above();
        return top;
    }

    public boolean grow(ServerLevel level, BlockPos pos) {
        BlockPos top = top(level, pos);
        BlockState current = level.getBlockState(top);
        if (!current.is(this))
            return false;
        if (heightBelow(level, top, this) >= MAX_GROWN_HEIGHT) {
            level.setBlock(top, current.setValue(GROWING, false), Block.UPDATE_CLIENTS);
            return false;
        }
        if (!clear(level, top.above()))
            return false;
        level.setBlock(top, current.setValue(GROWING, false).setValue(LEAVES, BambooLeaves.SMALL), Block.UPDATE_CLIENTS);
        if (level.getBlockState(top.below()).is(this))
            level.setBlock(top.below(), level.getBlockState(top.below()).setValue(LEAVES, BambooLeaves.NONE), Block.UPDATE_CLIENTS);
        level.setBlock(top.above(), defaultBlockState(), Block.UPDATE_ALL);
        return true;
    }

    public static boolean clear(LevelReader level, BlockPos pos) {
        return !level.isOutsideBuildHeight(pos) && level.getBlockState(pos).isAir() && level.getFluidState(pos).isEmpty();
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        BlockPos top = top(level, pos);
        return heightBelow(level, top, this) < MAX_GROWN_HEIGHT && clear(level, top.above());
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        grow(level, pos);
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(stack.getItem() instanceof HoeItem) || !soil(level.getBlockState(pos.below())))
            return InteractionResult.PASS;
        if (level instanceof ServerLevel server) {
            level.removeBlock(pos, false);
            popResource(level, pos, new ItemStack(RDBambooBlocks.LARGE_BAMBOO_SHOOT));
            stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEAVES, GROWING);
    }
}
