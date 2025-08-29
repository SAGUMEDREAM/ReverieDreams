package cc.thonly.reverie_dreams.mixin.accessor;

import net.fabricmc.fabric.impl.registry.sync.DynamicRegistriesImpl;
import net.minecraft.registry.RegistryLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = DynamicRegistriesImpl.class, remap = false)
public interface DynamicRegistriesImplAccessor {
    @Accessor("DYNAMIC_REGISTRIES")
    static List<RegistryLoader.Entry<?>> getDynamicRegistryList() {
        throw new AssertionError();
    }
}
