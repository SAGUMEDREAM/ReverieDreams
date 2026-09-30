package cc.thonly.reverie_dreams.paper.util;

import net.momirealms.craftengine.core.util.Key;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

public class Ids {
    public static final String MOD_ID = "reverie_dreams";

    public static Key key(final @NotNull String path) {
        return new Key(MOD_ID, path);
    }

    public static NamespacedKey namespacedKey(String path) {
        return new NamespacedKey(MOD_ID, path);
    }

}
