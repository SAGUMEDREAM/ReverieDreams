package cc.thonly.reverie_dreams.block.redstone;

/**
 * Shared limits for persisted values and configuration dialogs.
 */
public final class RedstoneSettings {
    public static final int MAX_DELAY = 72_000;
    public static final int BROADCAST_COOLDOWN = 100;

    private RedstoneSettings() {
    }

    public static int delay(int ticks) {
        return Math.clamp(ticks, 0, MAX_DELAY);
    }

    public static boolean validChannel(String name, String token) {
        return name.strip().length() <= 30 && token.strip().length() <= 30
                && (name.isBlank() == token.isBlank());
    }

    public static boolean matches(String filter, String cartName) {
        return filter.isEmpty() || filter.equals(cartName);
    }
}
