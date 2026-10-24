package cc.thonly.reverie_dreams.api.proxy;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public class PlatformProxyImpl<T> implements PlatformProxy<T> {
    private static final Logger log = LoggerFactory.getLogger(PlatformProxyImpl.class);
    private final List<String> platform;
    @Nullable
    private String clazzName;

    public PlatformProxyImpl(String platform) {
        this.platform = new ArrayList<>(List.of(platform));
    }

    public PlatformProxyImpl(String... platforms) {
        this.platform = new ArrayList<>(List.of(platforms));
    }

    @Override
    public PlatformProxy<T> with(String platform, Supplier<Boolean> condition, String clazzName) {
        if (!condition.get()) {
            return this;
        } else {
            if (this.platform.contains(platform)) {
                this.clazzName = clazzName;
            }

            return this;
        }
    }

    @Override
    public T build() {
        try {
            return (T) Class.forName(this.clazzName).getConstructor().newInstance();
        } catch (Exception var2) {
            return null;
        }
    }

    @Override
    public T buildOrThrow() {
        try {
            return (T) Class.forName(this.clazzName).getConstructor().newInstance();
        } catch (InvocationTargetException | ClassNotFoundException | InstantiationException |
                 IllegalAccessException var2) {
            throw new RuntimeException("Failed to instantiate platform proxy " + this.clazzName, var2);
        } catch (NoSuchMethodException var3) {
            throw new RuntimeException("Failed to instantiate platform proxy, missing no-arg constructor in " + this.clazzName, var3);
        }
    }

    @Nullable
    @Override
    public Optional<T> buildOrNull() {
        if (this.clazzName == null) {
            return Optional.empty();
        } else {
            try {
                T t = (T) Class.forName(this.clazzName).getConstructor().newInstance();
                return Optional.of(t);
            } catch (ClassNotFoundException var2) {
                log.error("Platform proxy class not found: {}", this.clazzName, var2);
            } catch (NoSuchMethodException var3) {
                log.error("Missing no-arg constructor in {}", this.clazzName, var3);
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException var4) {
                log.error("Failed to instantiate platform proxy {}", this.clazzName, var4);
            }

            return Optional.empty();
        }
    }
}