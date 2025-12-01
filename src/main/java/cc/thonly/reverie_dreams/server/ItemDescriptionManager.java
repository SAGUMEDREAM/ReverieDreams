package cc.thonly.reverie_dreams.server;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ItemDescriptionManager {
    private static final Map<Item, List<MutableComponent>> REGISTRIES = new Object2ObjectOpenHashMap<>();

    public static void bootstrap() {

    }

    public static void register(Item item, MutableComponent... texts) {
        List<MutableComponent> registry = REGISTRIES.computeIfAbsent(item, i -> new ArrayList<>());
        registry.addAll(Arrays.asList(texts));
    }

    public static void register(Item item, List<MutableComponent> texts) {
        List<MutableComponent> registry = REGISTRIES.computeIfAbsent(item, i -> new ArrayList<>());
        registry.addAll(texts);
    }

    public static List<MutableComponent> getDescription(Item item) {
        return REGISTRIES.getOrDefault(item, new ArrayList<>());
    }
}
