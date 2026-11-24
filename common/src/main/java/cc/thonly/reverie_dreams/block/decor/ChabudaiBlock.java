package cc.thonly.reverie_dreams.block.decor;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import cc.thonly.reverie_dreams.block.entity.BoardGameBlockEntity;
import cc.thonly.reverie_dreams.board.BoardGames;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * A framed low table. Rendered parts and collision use the same geometry.
 */
public class ChabudaiBlock extends GroupedDecorBlock implements EntityBlock {
    public static final MapCodec<ChabudaiBlock> CODEC = simpleCodec(ChabudaiBlock::new);
    public static final BooleanProperty HAS_TEA_SET = BooleanProperty.create("has_tea_set");
    public static final EnumProperty<Direction> TEA_FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final double TOP_SURFACE = 7.9 / 16.0;

    public record Part(double x1, double y1, double z1, double x2, double y2, double z2, String texture) {
        public VoxelShape shape() {
            return Block.box(x1, y1, z1, x2, y2, z2);
        }
    }

    private static final List<List<Part>> PARTS = IntStream.range(0, 16).mapToObj(ChabudaiBlock::buildParts).toList();
    private static final VoxelShape[] SHAPES = IntStream.range(0, 16).mapToObj(mask -> {
        VoxelShape shape = Shapes.empty();
        for (Part part : PARTS.get(mask))
            shape = Shapes.or(shape, part.shape());
        return shape.optimize();
    }).toArray(VoxelShape[]::new);
    private static final Map<Direction, VoxelShape[]> TEA_OUTLINES = buildTeaOutlines();
    private static final VoxelShape[] BOARD_OUTLINES = IntStream.range(0, 16).mapToObj(mask -> CachedDecorShape.of(
            Shapes.or(SHAPES[mask], Block.box(1, TOP_SURFACE * 16, 1, 15, TOP_SURFACE * 16 + 1, 15)))).toArray(VoxelShape[]::new);

    private static Map<Direction, VoxelShape[]> buildTeaOutlines() {
        Map<Direction, VoxelShape[]> outlines = new EnumMap<>(Direction.class);
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            VoxelShape tea = TeaSetBlock.outline(facing).move(0, TOP_SURFACE, 0);
            VoxelShape[] shapes = new VoxelShape[16];
            for (int mask = 0; mask < shapes.length; mask++)
                shapes[mask] = CachedDecorShape.of(Shapes.or(SHAPES[mask], tea));
            outlines.put(facing, shapes);
        }
        return outlines;
    }

    public ChabudaiBlock(Properties properties) {
        super(properties, SHAPES[0], true, false);
        registerDefaultState(defaultBlockState().setValue(HAS_TEA_SET, false).setValue(TEA_FACING, Direction.NORTH));
    }

    public static List<Part> parts(int mask) {
        return PARTS.get(mask & 15);
    }

    private static List<Part> buildParts(int mask) {
        boolean n = (mask & 1) != 0, e = (mask & 2) != 0, s = (mask & 4) != 0, w = (mask & 8) != 0;
        List<Part> parts = new ArrayList<>();
        // Aprons end at the inner leg faces; overlapping their outer faces causes z-fighting.
        parts.add(new Part(0, 6, 0, 16, 7.75, 16, "trim"));
        parts.add(new Part(w ? 0 : .75, 7.75, n ? 0 : .75, e ? 16 : 15.25, TOP_SURFACE * 16, s ? 16 : 15.25, "top"));
        if (!n) {
            parts.add(new Part(0, 7.75, 0, 16, 8, .75, "trim"));
            parts.add(new Part(w ? 0 : 3, 4.5, 1, e ? 16 : 13, 6, 2, "side"));
        }
        if (!s) {
            parts.add(new Part(0, 7.75, 15.25, 16, 8, 16, "trim"));
            parts.add(new Part(w ? 0 : 3, 4.5, 14, e ? 16 : 13, 6, 15, "side"));
        }
        if (!w) {
            parts.add(new Part(0, 7.75, n ? 0 : .75, .75, 8, s ? 16 : 15.25, "trim"));
            parts.add(new Part(1, 4.5, n ? 0 : 3, 2, 6, s ? 16 : 13, "side"));
        }
        if (!e) {
            parts.add(new Part(15.25, 7.75, n ? 0 : .75, 16, 8, s ? 16 : 15.25, "trim"));
            parts.add(new Part(14, 4.5, n ? 0 : 3, 15, 6, s ? 16 : 13, "side"));
        }
        if (!n && !w)
            leg(parts, 1, 1);
        if (!n && !e)
            leg(parts, 13, 1);
        if (!s && !w)
            leg(parts, 1, 13);
        if (!s && !e)
            leg(parts, 13, 13);
        return List.copyOf(parts);
    }

    private static void leg(List<Part> parts, double x, double z) {
        parts.add(new Part(x, 2.5, z, x + 2, 6, z + 2, "trim"));
        parts.add(new Part(x + .25, .5, z + .25, x + 1.75, 2.5, z + 1.75, "side"));
        parts.add(new Part(x, 0, z, x + 2, .5, z + 2, "trim"));
    }

    public static int mask(BlockState state) {
        return (state.getValue(NORTH) ? 1 : 0) | (state.getValue(EAST) ? 2 : 0)
                | (state.getValue(SOUTH) ? 4 : 0) | (state.getValue(WEST) ? 8 : 0);
    }

    private static VoxelShape shapeFor(BlockState state) {
        return SHAPES[mask(state)];
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Picking and outline rendering query this every frame; never merge voxel meshes here.
        if (level.getBlockEntity(pos) instanceof BoardGameBlockEntity board && board.game() != null)
            return BOARD_OUTLINES[mask(state)];
        return state.getValue(HAS_TEA_SET) ? TEA_OUTLINES.get(state.getValue(TEA_FACING))[mask(state)] : shapeFor(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HAS_TEA_SET, TEA_FACING);
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                       Player player, InteractionHand hand, BlockHitResult hit) {
        if (BoardGames.isBoard(stack))
            return BoardGames.attach(level, pos, player, hand, hit);
        if (!stack.is(RDBlocks.TEA_SET.asItem()) || hit.getDirection() != Direction.UP)
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        if (state.getValue(HAS_TEA_SET) || (level.getBlockEntity(pos) instanceof BoardGameBlockEntity board && board.game() != null)
                || !player.mayUseItemAt(pos, hit.getDirection(), stack))
            return InteractionResult.FAIL;
        if (!level.isClientSide()) {
            if (!level.setBlock(pos, state.setValue(HAS_TEA_SET, true).setValue(TEA_FACING,
                    player.getDirection().getOpposite()), Block.UPDATE_ALL))
                return InteractionResult.FAIL;
            if (!player.getAbilities().instabuild)
                stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof BoardGameBlockEntity board && board.game() != null) {
            if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty() && !player.isSpectator() && player.mayBuild()) {
                if (!level.isClientSide() && board.detach())
                    return InteractionResult.SUCCESS_SERVER;
                if (level.isClientSide())
                    return InteractionResult.SUCCESS;
            }
            return BoardGames.openAt(level, pos, player);
        }
        if (!state.getValue(HAS_TEA_SET) || !player.isShiftKeyDown() || !player.getMainHandItem().isEmpty())
            return InteractionResult.PASS;
        if (!level.isClientSide() && level.setBlock(pos, state.setValue(HAS_TEA_SET, false), Block.UPDATE_ALL))
            popResource(level, pos, new ItemStack(RDBlocks.TEA_SET.asItem()));
        return InteractionResult.SUCCESS;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BoardGameBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return BoardGameBlockEntity.ticker(level, type);
    }
}
