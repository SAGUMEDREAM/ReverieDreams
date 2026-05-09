package cc.thonly.reverie_dreams.entity.villager;

import cc.thonly.reverie_dreams.item.IngredientStack;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;

import java.util.Map;

@Getter
public class SellInfo {
    public static final Codec<SellInfo> CODEC = Codec.unboundedMap(
            Codec.LONG,
            Codec.unboundedMap(IngredientStack.CODEC, Codec.INT)
    ).xmap(SellInfo::new, SellInfo::getData);

    // Seed -> Item -> Amount;
    private final Map<Long ,Map<IngredientStack, Integer>> data;
    public SellInfo(Map<Long ,Map<IngredientStack, Integer>> data) {
        this.data = new Object2ObjectOpenHashMap<>();
        data.forEach((key, value) -> {
            Map<IngredientStack, Integer> copiedInner = new Object2ObjectOpenHashMap<>(value);
            this.data.put(key, copiedInner);
        });
    }

    public void sell(long seed, IngredientStack wrapper) {
        Map<IngredientStack, Integer> archiveMap = this.data.computeIfAbsent(seed, x -> new Object2ObjectOpenHashMap<>());
        wrapper = IngredientStack.findEquivalentKey(archiveMap, wrapper);
        Integer number = archiveMap.getOrDefault(wrapper, 0);
        archiveMap.put(wrapper, ++number);
    }

    public int getSellArchive(long seed, IngredientStack wrapper) {
        Map<IngredientStack, Integer> archiveMap = this.data.get(seed);
        if (archiveMap == null) {
            return 0;
        }
        wrapper = IngredientStack.findEquivalentKey(archiveMap, wrapper);
        return archiveMap.getOrDefault(wrapper, 0);
    }
}
