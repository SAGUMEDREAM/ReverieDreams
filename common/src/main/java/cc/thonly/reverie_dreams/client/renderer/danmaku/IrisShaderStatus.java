package cc.thonly.reverie_dreams.client.renderer.danmaku;

import cc.thonly.reverie_dreams.ReverieDreamsClient;
import dev.architectury.platform.Platform;

import java.lang.reflect.Method;
import java.util.function.BooleanSupplier;

/**
 * Keeps the Iris API optional. Check once per world view, not for each projectile.
 */
final class IrisShaderStatus {
    private static BooleanSupplier active = locateApi();

    private IrisShaderStatus() {
    }

    static boolean active() {
        return active.getAsBoolean();
    }

    private static BooleanSupplier locateApi() {
        if (!Platform.isModLoaded("iris")) {
            return () -> false;
        }
        try {
            Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object instance = api.getMethod("getInstance").invoke(null);
            Method status = api.getMethod("isShaderPackInUse");
            return () -> {
                try {
                    return Boolean.TRUE.equals(status.invoke(instance));
                } catch (ReflectiveOperationException | LinkageError ex) {
                    return fallback(ex);
                }
            };
        } catch (ReflectiveOperationException | LinkageError ex) {
            fallback(ex);
            return () -> true;
        }
    }

    private static boolean fallback(Throwable failure) {
        ReverieDreamsClient.LOGGER.warn("Iris shader status is unavailable; using stock orb rendering", failure);
        active = () -> true;
        return true;
    }
}
