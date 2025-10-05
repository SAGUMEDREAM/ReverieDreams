package cc.thonly.reverie_dreams.mixin.registry;

import net.minecraft.registry.DynamicRegistryManager;
import org.spongepowered.asm.mixin.*;

@Mixin(DynamicRegistryManager.ImmutableImpl.class)
public abstract class DynamicRegistryManagerMixin implements DynamicRegistryManager {

}
