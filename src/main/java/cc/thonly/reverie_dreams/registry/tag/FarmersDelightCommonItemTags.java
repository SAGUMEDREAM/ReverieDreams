package cc.thonly.reverie_dreams.registry.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class FarmersDelightCommonItemTags {

    public static final TagKey<Item> CROPS_CABBAGE = commonItemTag("crops/cabbage");
    public static final TagKey<Item> CROPS_TOMATO = commonItemTag("crops/tomato");
    public static final TagKey<Item> CROPS_ONION = commonItemTag("crops/onion");
    public static final TagKey<Item> CROPS_RICE = commonItemTag("crops/rice");
    public static final TagKey<Item> CROPS_GRAIN = commonItemTag("crops/grain");
    public static final TagKey<Item> FOODS_CABBAGE = commonItemTag("foods/cabbage");
    public static final TagKey<Item> FOODS_TOMATO = commonItemTag("foods/tomato");
    public static final TagKey<Item> FOODS_ONION = commonItemTag("foods/onion");
    public static final TagKey<Item> FOODS_LEAFY_GREEN = commonItemTag("foods/leafy_green");
    public static final TagKey<Item> FOODS_DOUGH = commonItemTag("foods/dough");
    public static final TagKey<Item> FOODS_PASTA = commonItemTag("foods/pasta");

    private static TagKey<Block> commonBlockTag(String path) {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("c", path));
    }

    private static TagKey<Item> commonItemTag(String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
    }
}
