package cc.thonly.reverie_dreams.ai;

/**
 * UI visibility never grants permission to make a paid request.
 */
public final class AIChatPolicy {
    private AIChatPolicy() {
    }

    public static boolean enabled(boolean serverEnabled, boolean playerDisabled, String url, String key, boolean providerAvailable) {
        return serverEnabled && !playerDisabled && providerAvailable && url != null && !url.isBlank() && key != null && !key.isBlank();
    }
}
