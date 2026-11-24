package cc.thonly.reverie_dreams.block.kitchen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Model-only registry entry. Has no BlockItem; the plate retains the actual food stack.
 */
public class CuisineBlock extends Block {
    public static final MapCodec<CuisineBlock> CODEC = simpleCodec(CuisineBlock::new);
    public static final double PLATE_SURFACE = 1.0 / 16.0;

    /**
     * Keep square platters inside the plate at every saved orientation.
     */
    public static float displayYaw(float yaw) {
        return Math.round(yaw / 90.0f) * 90.0f;
    }

    public CuisineBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Block.box(2, 0, 2, 14, 8, 14);
    }
}
