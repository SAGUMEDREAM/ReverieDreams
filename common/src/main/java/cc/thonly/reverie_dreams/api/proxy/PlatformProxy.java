
package cc.thonly.reverie_dreams.api.proxy;

import dev.architectury.platform.Platform;

import java.util.Optional;
import java.util.function.Supplier;

public interface PlatformProxy<T> {

    String FABRIC = "fabric";
    String FORGE = "forge";
    String POLYMER = "polymer";
    String NEOFORGE = "neoforge";

    static <T> PlatformProxy<T> builder() {
        String type;

        if (Platform.isFabric()) {
            type = FABRIC;
        } else if (Platform.isNeoForge()) {
            type = NEOFORGE;
        } else {
            type = FORGE;
        }

        return FactoryHolder.get().create(type);
    }

    static void setFactory(ProxyFactory factory) {
        FactoryHolder.factory = factory;
    }

    @FunctionalInterface
    interface ProxyFactory {
        <T> PlatformProxy<T> create(String... platforms);
    }

    final class FactoryHolder {
        private static volatile ProxyFactory factory;

        private static ProxyFactory get() {
            ProxyFactory current = factory;
            if (current == null) {
                synchronized (FactoryHolder.class) {
                    current = factory;
                    if (current == null) {
                        current = new DefaultProxyFactory();
                        factory = current;
                    }
                }
            }
            return current;
        }

        // Loaded lazily by name: this holder can run during Minecraft bootstrap,
        // before mod initializers have registered the loader-side factory.
        private static final class DefaultProxyFactory implements ProxyFactory {
            @Override
            @SuppressWarnings("unchecked")
            public <T> PlatformProxy<T> create(String... platforms) {
                try {
                    return (PlatformProxy<T>) Class.forName("cc.thonly.reverie_dreams.proxy.PlatformProxyImpl")
                            .getMethod("create", String[].class)
                            .invoke(null, (Object) platforms);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Platform proxy implementation is not available", e);
                }
            }
        }
    }

    default PlatformProxy<T> with(String platform, String clazzName) {
        return this.with(platform, () -> true, clazzName);
    }

    PlatformProxy<T> with(
            String platform,
            Supplier<Boolean> condition,
            String clazzName
    );

    T build();

    T buildOrThrow();

    Optional<T> buildOrNull();

    default PlatformProxy<T> withFabric(String clazzName) {
        return this.with(FABRIC, clazzName);
    }

    default PlatformProxy<T> withForge(String clazzName) {
        return this.with(FORGE, clazzName);
    }

    default PlatformProxy<T> withPolymer(String clazzName) {
        return this.with(
                FABRIC,
                () -> Platform.isModLoaded(POLYMER),
                clazzName
        );
    }

    default PlatformProxy<T> withNeoForge(String clazzName) {
        return this.with(NEOFORGE, clazzName);
    }
}
