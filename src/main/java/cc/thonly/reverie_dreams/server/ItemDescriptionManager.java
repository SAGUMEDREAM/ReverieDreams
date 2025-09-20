package cc.thonly.reverie_dreams.server;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.text.MutableText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ItemDescriptionManager {
    private static final Map<Item, List<MutableText>> REGISTRIES = new Object2ObjectOpenHashMap<>();

    public static void bootstrap() {

    }

    public static void register(Item item, MutableText... texts) {
        List<MutableText> registry = REGISTRIES.computeIfAbsent(item, i -> new ArrayList<>());
        registry.addAll(Arrays.asList(texts));
    }

    public static void register(Item item, List<MutableText> texts) {
        List<MutableText> registry = REGISTRIES.computeIfAbsent(item, i -> new ArrayList<>());
        registry.addAll(texts);
    }

    public static List<MutableText> getDescription(Item item) {
        return REGISTRIES.getOrDefault(item, new ArrayList<>());
    }
}
