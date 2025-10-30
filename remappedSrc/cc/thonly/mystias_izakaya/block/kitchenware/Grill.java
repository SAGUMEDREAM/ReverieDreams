package cc.thonly.mystias_izakaya.block.kitchenware;

import org.joml.Vector3f;

import java.util.function.DoubleUnaryOperator;
import net.minecraft.world.phys.Vec3;

public class Grill extends AbstractKitchenwareBlock {
    public Grill(DoubleUnaryOperator bonusOperator, Double failureProbability, Properties settings) {
        super(bonusOperator, failureProbability, new Vector3f(2.0f), new Vec3(0, 0, 0), settings);
    }
}
