package cc.thonly.reverie_dreams.api.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

@SuppressWarnings("rawtypes")
public class BlockItemTagKey {
    private final TagKey<Item> item;
    private final TagKey<Block> block;

    public BlockItemTagKey(TagKey<Item> item, TagKey<Block> block) {
        this.item = item;
        this.block = block;
    }

    public BlockItemTagKey(Identifier location) {
        this(TagKey.create(Registries.ITEM, location), TagKey.create(Registries.BLOCK, location));
    }

    public BlockItemTagKey(TagKey tagKey) {
        this(tagKey.location());
    }

    public boolean hasItem(Holder<Item> holder) {
        return holder.is(this.item);
    }

    public boolean hasBlock(Holder<Block> holder) {
        return holder.is(this.block);
    }

    public TagKey<Block> block() {
        return this.block;
    }

    public TagKey<Item> item() {
        return this.item;
    }

}
