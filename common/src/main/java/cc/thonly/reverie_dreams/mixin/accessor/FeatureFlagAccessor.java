package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagUniverse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FeatureFlag.class)
public interface FeatureFlagAccessor {
    @Invoker("<init>")
    static FeatureFlag newInit(FeatureFlagUniverse universe, int bit) {
        return null;
    }
}
