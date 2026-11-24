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

import net.minecraft.core.Direction;

public class FryingPan extends AbstractKitchenwareBlock {
    public FryingPan(DoubleUnaryOperator bonusOperator, Double failureProbability, Properties settings) {
        super(bonusOperator, failureProbability, new Vector3f(1.0f), new Vec3(0, 0, 0), settings);
    }

    public static final VoxelShape SHAPE = KitchenwareShapes.outline("frying_pan", Direction.NORTH);

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return KitchenwareShapes.outline(state, "frying_pan");
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return !PlatformContext.hasPolymer() ? getShape(state, level, pos, context) : NONE;
    }
}
