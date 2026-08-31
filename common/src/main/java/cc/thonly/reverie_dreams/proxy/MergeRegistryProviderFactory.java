package cc.thonly.reverie_dreams.proxy;

import cc.thonly.reverie_dreams.registry.impl.MergeRegistry;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("rawtypes")
public interface MergeRegistryProviderFactory extends BiFunction<ResourceKey<? extends Registry<?>>, List<Registry>, MergeRegistry<?>> {

}
