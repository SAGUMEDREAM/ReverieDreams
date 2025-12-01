package cc.thonly.reverie_dreams.datafixer;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;

import java.util.Map;

public class DataFixerContentManager {
    public static final Map<Registry<?>, Map<ResourceLocation, ResourceLocation>> ENTRIES = new Object2ObjectOpenHashMap<>();
    public static void bootstrap() {

    }

    public static <V> Tuple<ResourceLocation, ResourceLocation> addRedirect(Registry<V> registry, ResourceLocation old, ResourceLocation newId) {
        Map<ResourceLocation, ResourceLocation> entries = ENTRIES.computeIfAbsent(registry, (pair) -> new Object2ObjectOpenHashMap<>());
        entries.put(old, newId);
        return new Tuple<>(old, newId);
    }
}
