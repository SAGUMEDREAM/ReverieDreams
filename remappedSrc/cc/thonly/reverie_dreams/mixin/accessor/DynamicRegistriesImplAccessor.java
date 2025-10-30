package cc.thonly.reverie_dreams.mixin.accessor;

import net.fabricmc.fabric.impl.registry.sync.DynamicRegistriesImpl;
import net.minecraft.resources.RegistryDataLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = DynamicRegistriesImpl.class, remap = false)
public interface DynamicRegistriesImplAccessor {
    @Accessor("DYNAMIC_REGISTRIES")
    static List<RegistryDataLoader.RegistryData<?>> getDynamicRegistryList() {
        throw new AssertionError();
    }
}
