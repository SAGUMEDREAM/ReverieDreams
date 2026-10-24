package cc.thonly.reverie_dreams.api.registry;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface CompostingChanceRegistry {
    List<Entry> ENTRIES = new ArrayList<>();

    static void registerItem(Holder<Item> item, Float value) {
        ENTRIES.add(new Entry(item, null, null, value));
    }

    static void registerBlock(Holder<Block> block, Float value) {
        ENTRIES.add(new Entry(null, block, null, value));
    }

    static void registerItemTag(TagKey<Item> tagKey, Float value) {
        ENTRIES.add(new Entry(null, null, tagKey, value));
    }

    record Entry(
            @Nullable Holder<Item> item,
            @Nullable Holder<Block> block,
            @Nullable TagKey<Item> tagKey,
            Float value
    ) {
    }
}
