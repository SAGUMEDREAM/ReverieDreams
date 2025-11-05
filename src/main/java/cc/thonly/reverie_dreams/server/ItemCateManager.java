package cc.thonly.reverie_dreams.server;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemCateManager {
    private static final ItemCateManager INSTANCE = new ItemCateManager();
    private final Map<TagKey<Item>, Set<Item>> SET_MAP = new Object2ObjectLinkedOpenHashMap<>();
    private final Map<Item, Set<TagKey<Item>>> ITEM_2_TAG_KEY = new Object2ObjectLinkedOpenHashMap<>();
    private ItemCateManager() {
    }

    public void clearTags() {
        SET_MAP.clear();
        ITEM_2_TAG_KEY.clear();
    }

    public void load(MinecraftServer server) {
        this.clearTags();
        RegistryAccess.Frozen registryManager = server.registryAccess();
        Registry<Item> registry = registryManager.lookupOrThrow(Registries.ITEM);
        Stream<HolderSet.Named<Item>> namedStream = registry.getTags();
        for (HolderSet.Named<Item> registryEntries : namedStream.toList()) {
            TagKey<Item> tag = registryEntries.key();
            Set<Item> items = SET_MAP.computeIfAbsent(tag, tagKey -> new LinkedHashSet<>());
            List<Holder<Item>> entries = registryEntries.contents;
            if (entries != null) {
                for (Holder<Item> itemRegistryEntry : entries.stream().toList()) {
                    Item item = itemRegistryEntry.value();
                    Set<TagKey<Item>> tagKeys = ITEM_2_TAG_KEY.computeIfAbsent(item, i -> new HashSet<>());
                    tagKeys.add(tag);
                }
                Set<Item> collect = entries.stream().map(Holder::value).collect(Collectors.toSet());
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

    public static ItemCateManager getInstance() {
        return INSTANCE;
    }
}
