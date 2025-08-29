package cc.thonly.mystias_izakaya.block.kitchenware;

import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.function.DoubleUnaryOperator;

public class CuttingBoard extends AbstractKitchenwareBlock {
    public CuttingBoard(DoubleUnaryOperator bonusOperator, Double failureProbability, Settings settings) {
        super(bonusOperator, failureProbability, new Vector3f(2.0f), new Vec3d(0, 0, 0), settings);
    }
}
