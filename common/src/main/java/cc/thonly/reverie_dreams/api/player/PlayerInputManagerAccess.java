package cc.thonly.reverie_dreams.api.player;

import cc.thonly.reverie_dreams.server.InputKey;
import cc.thonly.reverie_dreams.server.input.ServerPlayerInputManagerAccess;
import cc.thonly.reverie_dreams.util.PlatformContext;
import net.minecraft.server.level.ServerPlayer;

public interface PlayerInputManagerAccess {
    default boolean isKeyPressed(ServerPlayer player, InputKey key) {
        return false;
    }

    default boolean isKeyDown(ServerPlayer player, InputKey key) {
        return false;
    }

    default PlayerInputManagerAccess reload() {
        return null;
    }

    static PlayerInputManagerAccess polymerAccess() {
        return ServerPlayerInputManagerAccess.getInstance();
    }

    static PlayerInputManagerAccess access() {
        return ServerPlayerInputManagerAccess.getInstance();
    }

    static PlayerInputManagerAccess autoAccess() {
        if (PlatformContext.hasPolymer()) {
            return ServerPlayerInputManagerAccess.getInstance();
        }
        return cc.thonly.reverie_dreams.server.input.PlayerInputManagerAccess.getInstance();
    }
}
