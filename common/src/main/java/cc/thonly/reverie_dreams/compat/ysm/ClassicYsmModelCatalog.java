package cc.thonly.reverie_dreams.compat.ysm;

import lombok.extern.slf4j.Slf4j;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Fox and Sparkle 1.x expose a server catalog through these methods. Sparkle 2.x
 * removed them, so link to them only when a classic provider is installed.
 */
@Slf4j
public final class ClassicYsmModelCatalog {
    private ClassicYsmModelCatalog() {
    }

    public static void bootstrap() throws ReflectiveOperationException {
        Method connected = method("network.NetworkHandler", "isPlayerConnected", ServerPlayer.class);
        Method network = method("core.config.ConfigPolicies", "network");
        Method canSwitch = network.getReturnType().getMethod("canSwitchModel");
        Method service = method("core.compat.api.CompatServices", "maidModelService");
        Class<?> catalogType = service.getReturnType();
        Method contains = catalogType.getMethod("containsModel", String.class);
        Method authorized = catalogType.getMethod("isAuthorized", String.class, Player.class);
        Method resolve = catalogType.getMethod("resolveTextureOrDefault", String.class, String.class);

        NPCModelSelection.useClassicCatalog(player -> Boolean.TRUE.equals(invoke(connected, null, player)),
                (player, model, texture) -> {
                    Object policy = invoke(network, null);
                    if (policy == null || !Boolean.TRUE.equals(invoke(canSwitch, policy))) {
                        return null;
                    }
                    Object catalog = invoke(service, null);
                    if (catalog == null || !Boolean.TRUE.equals(invoke(contains, catalog, model))
                            || !Boolean.TRUE.equals(invoke(authorized, catalog, model, player))) {
                        return null;
                    }
                    return (String) invoke(resolve, catalog, model, texture);
                });
    }

    private static Method method(String type, String name, Class<?>... parameters) throws ReflectiveOperationException {
        return Class.forName("com.micaftic.morpher." + type, false, ClassicYsmModelCatalog.class.getClassLoader())
                .getMethod(name, parameters);
    }

    private static @Nullable Object invoke(Method method, @Nullable Object target, Object... arguments) {
        try {
            return method.invoke(target, arguments);
        } catch (ReflectiveOperationException e) {
            log.warn("Classic model provider call failed: {}", method.getName(), e);
            return null;
        }
    }
}
