package cc.thonly.reverie_dreams.mixin.registry;

import cc.thonly.reverie_dreams.api.registry.flag.FeatureFlagRegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagRegistry;
import org.spongepowered.asm.mixin.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(FeatureFlagRegistry.class)
public class FeatureFlagRegistryMixin implements FeatureFlagRegistryAccess {
    @Mutable
    @Shadow
    @Final
    private Map<Identifier, FeatureFlag> names;

    @Unique
    private void reverie_dreams$preModifyMutable() {
        if (!(this.names instanceof LinkedHashMap<Identifier, FeatureFlag>)) {
            this.names = new LinkedHashMap<>(this.names);
        }
    }
}
