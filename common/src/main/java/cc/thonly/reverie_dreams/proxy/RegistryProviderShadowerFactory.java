package cc.thonly.reverie_dreams.proxy;

import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.function.BiFunction;

public interface RegistryProviderShadowerFactory extends BiFunction<ResourceKey<? extends Registry<?>>, RegistryProvider<?>, RegistryProvider<?>> {

}