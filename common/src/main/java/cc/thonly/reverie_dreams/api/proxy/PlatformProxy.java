
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

        return new PlatformProxyImpl<>(type);
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
