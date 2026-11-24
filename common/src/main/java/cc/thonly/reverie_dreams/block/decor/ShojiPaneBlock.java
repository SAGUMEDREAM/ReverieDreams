package cc.thonly.reverie_dreams.block.decor;

import cc.thonly.reverie_dreams.block.base.PaneBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A shoji screen keeps the pane connection logic (so screens line up into a
 * continuous wall) but renders and collides as a full sheet of paper instead
 * of vanilla's open bars.
 *
 * <p>The shape mirrors the block models: each connected side gets a half sheet
 * running from the block edge to the centre, two connected sides of the same
 * axis join into a full sheet, and a screen without connections falls back to
 * one full sheet.</p>
 */
public class ShojiPaneBlock extends PaneBlock {
    public static final MapCodec<ShojiPaneBlock> CODEC = simpleCodec(ShojiPaneBlock::new);

    private static final VoxelShape PANEL_X = Block.box(7.0, 0.0, 0.0, 9.0, 16.0, 16.0);
    private static final VoxelShape PANEL_Z = Block.box(0.0, 0.0, 7.0, 16.0, 16.0, 9.0);
    private static final VoxelShape HALF_NORTH = Block.box(7.0, 0.0, 0.0, 9.0, 16.0, 8.0);
    private static final VoxelShape HALF_SOUTH = Block.box(7.0, 0.0, 8.0, 9.0, 16.0, 16.0);
    private static final VoxelShape HALF_EAST = Block.box(8.0, 0.0, 7.0, 16.0, 16.0, 9.0);
    private static final VoxelShape HALF_WEST = Block.box(0.0, 0.0, 7.0, 8.0, 16.0, 9.0);

    public ShojiPaneBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends PaneBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return panelShape(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return panelShape(state);
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return panelShape(state);
    }

    private static VoxelShape panelShape(BlockState state) {
        boolean north = state.getValue(NORTH);
        boolean east = state.getValue(EAST);
        boolean south = state.getValue(SOUTH);
        boolean west = state.getValue(WEST);
        VoxelShape shape = Shapes.empty();
        if (north && south) {
            shape = Shapes.or(shape, PANEL_X);
        } else if (north) {
            shape = Shapes.or(shape, HALF_NORTH);
        } else if (south) {
            shape = Shapes.or(shape, HALF_SOUTH);
        }
        if (east && west) {
            shape = Shapes.or(shape, PANEL_Z);
        } else if (east) {
            shape = Shapes.or(shape, HALF_EAST);
        } else if (west) {
            shape = Shapes.or(shape, HALF_WEST);
        }
        if (shape.isEmpty()) {
            return PANEL_Z;
        }
        return shape;
    }
}
