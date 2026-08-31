package cc.thonly.reverie_dreams.block.cooking;

import cc.thonly.reverie_dreams.util.PlatformContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

import java.util.function.DoubleUnaryOperator;

public class CookingPot extends AbstractKitchenwareBlock {
    public CookingPot(DoubleUnaryOperator bonusOperator, Double failureProbability, Properties settings) {
        super(bonusOperator, failureProbability, new Vector3f(2.0f), new Vec3(0, 0, 0), settings);
    }

    public static final VoxelShape SHAPE = Block.box(
            2, 0, 2,
            14, 8, 14
    );

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return !PlatformContext.hasPolymer() ? SHAPE : NONE;
    }
}
