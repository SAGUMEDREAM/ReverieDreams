package cc.thonly.reverie_dreams.api.player;

import java.util.function.Supplier;

public final class PlayerComponentManagerAccess {
    private static volatile Supplier<PlayerComponentManager> serverAccess;
    private static volatile Supplier<PlayerComponentManager> clientAccess;

    private PlayerComponentManagerAccess() {
    }

    public static void registerServer(Supplier<PlayerComponentManager> supplier) {
        serverAccess = supplier;
    }

    public static void registerClient(Supplier<PlayerComponentManager> supplier) {
        clientAccess = supplier;
    }

    static PlayerComponentManager serverAccess() {
        return require(serverAccess, "server");
    }

    static PlayerComponentManager clientAccess() {
        return require(clientAccess, "client");
    }

    private static PlayerComponentManager require(Supplier<PlayerComponentManager> supplier, String side) {
        if (supplier == null) {
            throw new IllegalStateException("Player component manager access for the " + side + " side is not registered");
        }
        return supplier.get();
    }
}
