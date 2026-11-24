package cc.thonly.reverie_dreams.world.gensokyo;

/**
 * Marks the current server thread as executing a player block interaction, so
 * the Level.setBlock guard can distinguish player-caused writes from world
 * generation and non-player block changes.
 */
public final class BoundaryActionContext {
    private static final ThreadLocal<Boolean> PLAYER_ACTION = ThreadLocal.withInitial(() -> false);

    private BoundaryActionContext() {
    }

    public static void enter() {
        PLAYER_ACTION.set(true);
    }

    public static void exit() {
        PLAYER_ACTION.remove();
    }

    public static boolean active() {
        return Boolean.TRUE.equals(PLAYER_ACTION.get());
    }
}