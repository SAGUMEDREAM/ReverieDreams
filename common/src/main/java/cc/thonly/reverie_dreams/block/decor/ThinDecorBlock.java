package cc.thonly.reverie_dreams.block.decor;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/**
 * A small, facing-sensitive decorative block. The supplied shape is authored
 * facing north (the thin edge lies at z=0..depth), then rotated for the other
 * three horizontal directions.
 */
public class ThinDecorBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<ThinDecorBlock> CODEC = simpleCodec(ThinDecorBlock::new);
    private static final double MIN_SELECTION_THICKNESS = 4.0 / 16.0;

    private final Map<Direction, VoxelShape> rotatedShapes;
    private final boolean hasCollision;
    private final boolean mountedOnClickedFace;

    public ThinDecorBlock(Properties properties) {
        this(properties, Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 1.0), true);
    }

    public ThinDecorBlock(Properties properties, VoxelShape northShape, boolean hasCollision) {
        this(properties, northShape, hasCollision, false);
    }

    /**
     * @param mountedOnClickedFace when true the panel hugs the face that was
     *                             clicked (walls, ceilings) instead of turning
     *                             its back to the player; used by noren, sudare
     *                             and hanging scrolls.
     */
    public ThinDecorBlock(Properties properties, VoxelShape northShape, boolean hasCollision,
                          boolean mountedOnClickedFace) {
        super(properties);
        this.rotatedShapes = Shapes.rotateHorizontal(northShape);
        this.hasCollision = hasCollision;
        this.mountedOnClickedFace = mountedOnClickedFace;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        Direction clicked = context.getClickedFace();
        if (this.mountedOnClickedFace && clicked.getAxis().isHorizontal()) {
            // The model is authored at the block edge the decoration hangs
            // from, so it has to face away from the clicked block.
            facing = clicked.getOpposite();
        }
        return this.defaultBlockState().setValue(FACING, facing);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return selectionShape(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.hasCollision ? rotatedShape(state.getValue(FACING)) : Shapes.empty();
    }

    /**
     * The default visual shape is the collision shape. Thin decorations such
     * as noren and kakejiku intentionally have no collision, so keep a visual
     * shape for visual queries instead of falling back to an empty shape.
     */
    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return rotatedShape(state.getValue(FACING));
    }

    /**
     * One-pixel-thick models are difficult to hit with the crosshair. The
     * outline/target shape is expanded to four pixels along the thin axis,
     * while the collision shape stays true to the model.
     */
    private VoxelShape selectionShape(Direction facing) {
        VoxelShape shape = rotatedShape(facing);
        double minX = shape.min(Direction.Axis.X);
        double minY = shape.min(Direction.Axis.Y);
        double minZ = shape.min(Direction.Axis.Z);
        double maxX = shape.max(Direction.Axis.X);
        double maxY = shape.max(Direction.Axis.Y);
        double maxZ = shape.max(Direction.Axis.Z);
        if (maxX - minX < MIN_SELECTION_THICKNESS) {
            double center = (minX + maxX) * 0.5;
            minX = Math.max(0.0, center - MIN_SELECTION_THICKNESS * 0.5);
            maxX = Math.min(1.0, center + MIN_SELECTION_THICKNESS * 0.5);
        }
        if (maxY - minY < MIN_SELECTION_THICKNESS) {
            double center = (minY + maxY) * 0.5;
            minY = Math.max(0.0, center - MIN_SELECTION_THICKNESS * 0.5);
            maxY = Math.min(1.0, center + MIN_SELECTION_THICKNESS * 0.5);
        }
        if (maxZ - minZ < MIN_SELECTION_THICKNESS) {
            double center = (minZ + maxZ) * 0.5;
            minZ = Math.max(0.0, center - MIN_SELECTION_THICKNESS * 0.5);
            maxZ = Math.min(1.0, center + MIN_SELECTION_THICKNESS * 0.5);
        }
        return Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * The shape is authored facing north, so reuse vanilla's horizontal
     * rotation: {@code VoxelShape} coordinates are already normalized to 0..1,
     * which made the old hand-rolled pixel-based {@link Block#box} math collapse
     * the shape for every facing other than north.
     */
    private VoxelShape rotatedShape(Direction facing) {
        return this.rotatedShapes.get(facing);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }
}
