package cc.thonly.reverie_dreams.entity.villager;

import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;

import java.util.Map;

@Getter
public class SellInfo {
    public static final Codec<SellInfo> CODEC = Codec.unboundedMap(
            Codec.LONG,
            Codec.unboundedMap(ItemStackWrapper.CODEC, Codec.INT)
    ).xmap(SellInfo::new, SellInfo::getData);

    // Seed -> Item -> Amount;
    private final Map<Long ,Map<ItemStackWrapper, Integer>> data;
    public SellInfo(Map<Long ,Map<ItemStackWrapper, Integer>> data) {
        this.data = new Object2ObjectOpenHashMap<>();
        data.forEach((key, value) -> {
            Map<ItemStackWrapper, Integer> copiedInner = new Object2ObjectOpenHashMap<>(value);
            this.data.put(key, copiedInner);
        });
    }

    public void sell(long seed, ItemStackWrapper wrapper) {
        Map<ItemStackWrapper, Integer> archiveMap = this.data.computeIfAbsent(seed, x -> new Object2ObjectOpenHashMap<>());
        wrapper = ItemStackWrapper.findEquivalentKey(archiveMap, wrapper);
        Integer number = archiveMap.getOrDefault(wrapper, 0);
        archiveMap.put(wrapper, ++number);
    }

    public int getSellArchive(long seed, ItemStackWrapper wrapper) {
        Map<ItemStackWrapper, Integer> archiveMap = this.data.get(seed);
        if (archiveMap == null) {
            return 0;
        }
        wrapper = ItemStackWrapper.findEquivalentKey(archiveMap, wrapper);
        return archiveMap.getOrDefault(wrapper, 0);
    }
}
