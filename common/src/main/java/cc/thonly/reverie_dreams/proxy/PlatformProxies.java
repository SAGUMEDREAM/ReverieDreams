package cc.thonly.reverie_dreams.proxy;

import cc.thonly.keine.api.proxy.PlatformProxy;

import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class PlatformProxies {
    public static final Optional<RegistryProviderFactory> REGISTRY_PROVIDER_FACTORY =
            PlatformProxy.<RegistryProviderFactory>builder()
                    .withFabric("cc.thonly.reverie_dreams.fabric.impl.FabricRegistryProviderFactory")
                    .withNeoForge("cc.thonly.reverie_dreams.neoforge.impl.NeoRegistryProviderFactory")
                    .buildOrNull();
    public static final Optional<MergeRegistryProviderFactory> MERGE_REGISTRY_PROVIDER_FACTORY =
            PlatformProxy.<MergeRegistryProviderFactory>builder()
                    .withFabric("cc.thonly.reverie_dreams.fabric.impl.FabricMergeRegistryProviderFactory")
                    .withNeoForge("cc.thonly.reverie_dreams.neoforge.impl.NeoMergeRegistryProviderFactory")
                    .buildOrNull();
    public static final Optional<RegistryProviderShadowerFactory> REGISTRY_PROVIDER_SHADOWER_FACTORY =
            PlatformProxy.<RegistryProviderShadowerFactory>builder()
                    .withFabric("cc.thonly.reverie_dreams.fabric.impl.FabricRegistryProviderShadowerFactory")
                    .withNeoForge("cc.thonly.reverie_dreams.neoforge.impl.NeoRegistryProviderShadowerFactory")
                    .buildOrNull();

    public static void initialize() {

    }

    public static <T, R> void access(Optional<T> method, Function<T, R> logic) {
        method.map((Function<T, Object>) logic::apply);
    }
}
