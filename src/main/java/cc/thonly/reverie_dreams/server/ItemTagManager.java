package cc.thonly.reverie_dreams.server;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.item.Item;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemTagManager {
    private static final ItemTagManager INSTANCE = new ItemTagManager();
    private final Map<TagKey<Item>, Set<Item>> SET_MAP = new Object2ObjectLinkedOpenHashMap<>();
    private final Map<Item, Set<TagKey<Item>>> ITEM_2_TAG_KEY = new Object2ObjectLinkedOpenHashMap<>();
    private ItemTagManager() {
    }

    public void clearTags() {
        SET_MAP.clear();
        ITEM_2_TAG_KEY.clear();
    }

    public void load(MinecraftServer server) {
        this.clearTags();
        DynamicRegistryManager.Immutable registryManager = server.getRegistryManager();
        Registry<Item> registry = registryManager.getOrThrow(RegistryKeys.ITEM);
        Stream<RegistryEntryList.Named<Item>> namedStream = registry.streamTags();
        for (RegistryEntryList.Named<Item> registryEntries : namedStream.toList()) {
            TagKey<Item> tag = registryEntries.getTag();
            Set<Item> items = SET_MAP.computeIfAbsent(tag, tagKey -> new LinkedHashSet<>());
            List<RegistryEntry<Item>> entries = registryEntries.entries;
            if (entries != null) {
                for (RegistryEntry<Item> itemRegistryEntry : entries.stream().toList()) {
                    Item item = itemRegistryEntry.value();
                    Set<TagKey<Item>> tagKeys = ITEM_2_TAG_KEY.computeIfAbsent(item, i -> new HashSet<>());
                    tagKeys.add(tag);
                }
                Set<Item> collect = entries.stream().map(RegistryEntry::value).collect(Collectors.toSet());
                items.addAll(collect);
            }
        }
    }

    public boolean isEmpty() {
        return SET_MAP.isEmpty() || ITEM_2_TAG_KEY.isEmpty();
    }

    public boolean contains(TagKey<Item> tagKey, Item item) {
        return ITEM_2_TAG_KEY.getOrDefault(item, Set.of()).contains(tagKey);
    }

    public Set<TagKey<Item>> getTakKey(Item item) {
        return ITEM_2_TAG_KEY.getOrDefault(item, Set.of());
    }

    public static ItemTagManager getInstance() {
        return INSTANCE;
    }
}
