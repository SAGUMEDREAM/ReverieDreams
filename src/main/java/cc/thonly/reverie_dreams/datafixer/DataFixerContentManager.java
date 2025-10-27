package cc.thonly.reverie_dreams.datafixer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.Map;

public class DataFixerContentManager {
    public static final Map<Registry<?>, Map<Identifier, Identifier>> ENTRIES = new Object2ObjectOpenHashMap<>();
    public static void bootstrap() {

    }

    public static <V> Pair<Identifier, Identifier> addRedirect(Registry<V> registry, Identifier old, Identifier newId) {
        Map<Identifier, Identifier> entries = ENTRIES.computeIfAbsent(registry, (pair) -> new Object2ObjectOpenHashMap<>());
        entries.put(old, newId);
        return new Pair<>(old, newId);
    }
}
