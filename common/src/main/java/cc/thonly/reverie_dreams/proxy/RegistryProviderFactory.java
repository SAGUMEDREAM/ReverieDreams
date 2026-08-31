package cc.thonly.reverie_dreams.proxy;

import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.function.Function;

public interface RegistryProviderFactory extends Function<ResourceKey<? extends Registry<?>>, RegistryProvider<?>> {

}
