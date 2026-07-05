package cc.thonly.reverie_dreams.registry.tag;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class RDItemTags {
    public static final TagKey<Item> EMPTY = of("empty");
    public static final TagKey<Item> FUMO = of("fumo");
    public static final TagKey<Item> ORB_BLOCK = of("orb");
    public static final TagKey<Item> ARMOR = of("armor");
    public static final TagKey<Item> IRON_BAR_MATERIALS = of("iron_bar_materials");
    public static final TagKey<Item> COMMON_SILVER_ORE = of_c("ores/silver");
    public static final TagKey<Item> SILVER_ITEM = of("silver_item");
    public static final TagKey<Item> SILVER_ARMOR = of("silver_armor");
    public static final TagKey<Item> SILVER_TOOL_MATERIALS = of("silver_tool_materials");
    public static final TagKey<Item> MAGIC_ICE_ARMOR = of("magic_ice_armor");
    public static final TagKey<Item> MAGIC_ICE_TOOL_MATERIALS = of("magic_ice_tool_materials");
    public static final TagKey<Item> MAGIC_ICE_WEAPON = of("magic_ice_weapon");
    public static final TagKey<Item> DREAM_ARMOR = of("dream_ice_armor");
    public static final TagKey<Item> DREAM_TOOL_MATERIALS = of("dream_tool_materials");
    public static final TagKey<Item> MAID_ARMOR = of("maid_armor");
    public static final TagKey<Item> POWER_BLOCK = of("power_block");
    public static final TagKey<Item> POINT_BLOCK = of("point_block");
    public static final TagKey<Item> SILVER_BLOCK = of("silver_block");
    public static final TagKey<Item> PEACH = of("peach");
    public static final TagKey<Item> VAISRAVANAS_PAGODA = of("vaisravanas_pagoda_materials");
    public static final TagKey<Item> INGREDIENT_ITEM = of("ingredient_item");
    public static final TagKey<Item> FOOD_ITEM = of("food_item");
    public static final TagKey<Item> DRINK_ITEM = of("drink_item");
    public static final TagKey<Item> ROLE_TAME_FOOD = of("role_tame_food");
    public static final TagKey<Item> DANMAKU_ITEM = of("danmaku_item");
    public static final TagKey<Item> DANMAKU_REPAIR_ACCEPTABLE_ITEM = of("danmaku_repair_acceptable");
    public static final TagKey<Item> REPLACEABLE_BLANK_PHOTOS = of("replaceable_blank_photos");
    public static final TagKey<Item> MUSICAL_INSTRUMENTS = of("musical_instruments");
    public static final TagKey<Item> COIN = of("coin");
    public static final TagKey<Item> COMMON_COIN = of_c("coin");
    public static final TagKey<Item> DLC0 = of("touhou_mystia_dlc/0");
    public static final TagKey<Item> DLC1 = of("touhou_mystia_dlc/1");
    public static final TagKey<Item> DLC2 = of("touhou_mystia_dlc/2");
    public static final TagKey<Item> DLC3 = of("touhou_mystia_dlc/3");
    public static final TagKey<Item> DLC4 = of("touhou_mystia_dlc/4");
    public static final TagKey<Item> DLC5 = of("touhou_mystia_dlc/5");
    public static final TagKey<Item> DLC6 = of("touhou_mystia_dlc/6");
    public static final TagKey<Item> KITCHENWARE = of("kitchenware");
    public static final TagKey<Item> COOKING_TOP = of("cooking_top");
    public static final TagKey<Item> CUTTING_BOARD = of("cutting_board");
    public static final TagKey<Item> FRYING_PAN = of("frying_pan");
    public static final TagKey<Item> GRILL = of("grill");
    public static final TagKey<Item> STEAMER = of("steamer");

    private static TagKey<Item> of(String path) {
        return TagKey.create(Registries.ITEM, ReverieDreams.id(path));
    }
    private static TagKey<Item> of_c(String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
    }

    public static void register() {

    }
}
