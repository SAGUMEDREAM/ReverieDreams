package cc.thonly.reverie_dreams.block.decor.living;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Locale;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

/**
 * Ceiling decorations use scheduled ticks only while ringing or for a sparse chime.
 */
public class HangingOrnamentBlock extends HorizontalDirectionalBlock {
    public enum Kind implements StringRepresentable {
        BELL, ROPE, CHIME, HOOK;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final MapCodec<HangingOrnamentBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            StringRepresentable.fromEnum(Kind::values).fieldOf("ornament").forGetter(block -> block.kind),
            propertiesCodec()).apply(instance, HangingOrnamentBlock::new));
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty END = BooleanProperty.create("end");
    private final Kind kind;

    public HangingOrnamentBlock(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false).setValue(END, true));
    }

    public Kind kind() {
        return kind;
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, END);
    }

    private boolean sameChain(BlockState state) {
        return state.getBlock() instanceof HangingOrnamentBlock ornament && ornament.kind == kind;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState above = level.getBlockState(pos.above());
        if (kind == Kind.ROPE && above.getBlock() instanceof HangingOrnamentBlock ornament)
            return ornament.kind == Kind.BELL || ornament.kind == Kind.ROPE;
        if (kind == Kind.HOOK && sameChain(above))
            return true;
        return above.isFaceSturdy(level, pos.above(), Direction.DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(END, !sameChain(context.getLevel().getBlockState(context.getClickedPos().below())));
        return canSurvive(state, context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        if (direction == Direction.UP && !canSurvive(state, level, pos))
            return Blocks.AIR.defaultBlockState();
        if (direction == Direction.DOWN)
            return state.setValue(END, !sameChain(neighbor));
        return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return JapaneseLivingShapes.shape(kind.getSerializedName() + (state.getValue(END) ? "_end" : "_middle"), state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                       BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        // Extension items must reach placement instead of consuming the click to ring.
        if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof HangingOrnamentBlock)
            return InteractionResult.PASS;
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (kind == Kind.HOOK)
            return InteractionResult.PASS;
        if (!level.isClientSide()) {
            if (kind == Kind.CHIME) {
                // Every click rings once; the toggle controls only scheduled ambient ringing.
                boolean muted = !state.getValue(POWERED);
                if (!level.setBlock(pos, state.setValue(POWERED, muted), 3))
                    return InteractionResult.FAIL;
                chime(level, pos);
                if (!muted) {
                    level.scheduleTick(pos, this, 160 + level.getRandom().nextInt(240));
                }
            } else {
                BlockPos bellPos = pos;
                for (int i = 0; i < 32; i++, bellPos = bellPos.above()) {
                    BlockState candidate = level.getBlockState(bellPos);
                    if (!(candidate.getBlock() instanceof HangingOrnamentBlock ornament))
                        break;
                    if (ornament.kind == Kind.BELL) {
                        ornament.ring(candidate, level, bellPos);
                        break;
                    }
                    if (ornament.kind != Kind.ROPE)
                        break;
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    private void ring(BlockState state, Level level, BlockPos pos) {
        if (state.getValue(POWERED))
            return;
        level.playSound(null, pos, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 1.5f, 0.85f);
        level.setBlock(pos, state.setValue(POWERED, true), 3);
        level.updateNeighborsAt(pos, this);
        level.scheduleTick(pos, this, 20);
    }

    private static void chime(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 0.3f, 1.8f);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        if (kind == Kind.CHIME && !old.is(this))
            level.scheduleTick(pos, this, 160 + level.getRandom().nextInt(240));
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (kind == Kind.BELL && state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, false), 3);
            level.updateNeighborsAt(pos, this);
        } else if (kind == Kind.CHIME && !state.getValue(POWERED)) {
            chime(level, pos);
            level.scheduleTick(pos, this, 160 + random.nextInt(240));
        }
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return kind == Kind.BELL;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return kind == Kind.BELL && state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }
}
