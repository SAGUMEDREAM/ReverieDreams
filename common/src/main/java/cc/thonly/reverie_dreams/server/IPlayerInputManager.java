package cc.thonly.reverie_dreams.server;

import cc.thonly.reverie_dreams.server.input.PlayerInputManager;
import cc.thonly.reverie_dreams.server.input.ServerPlayerInputManager;
import cc.thonly.reverie_dreams.util.PlatformContext;
import net.minecraft.server.level.ServerPlayer;

public interface IPlayerInputManager {
    default boolean isKeyPressed(ServerPlayer player, InputKey key) {
        return false;
    }

    default boolean isKeyDown(ServerPlayer player, InputKey key) {
        return false;
    }

    default IPlayerInputManager reload() {
        return null;
    }

    static IPlayerInputManager polymerAccess() {
        return ServerPlayerInputManager.getInstance();
    }

    static IPlayerInputManager access() {
        return ServerPlayerInputManager.getInstance();
    }

    static IPlayerInputManager autoAccess() {
        if (PlatformContext.hasPolymer()) {
            return ServerPlayerInputManager.getInstance();
        }
        return PlayerInputManager.getInstance();
    }
}
