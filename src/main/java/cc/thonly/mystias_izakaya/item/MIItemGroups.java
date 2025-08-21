package cc.thonly.mystias_izakaya.item;

import cc.thonly.mystias_izakaya.MystiasIzakaya;
import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.reverie_dreams.block.WoodCreator;
import cc.thonly.reverie_dreams.block.PolymerCropCreator;
import cc.thonly.reverie_dreams.item.ModItems;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.function.Supplier;

public class MIItemGroups {
    public static final RegistryKey<ItemGroup> KITCHENWARE_ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, MystiasIzakaya.id("kitchenware_item_group"));
    public static final RegistryKey<ItemGroup> INGREDIENT_ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, MystiasIzakaya.id("ingredients_item_group"));
    public static final RegistryKey<ItemGroup> SEEDS_ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, MystiasIzakaya.id("seeds_item_group"));
    public static final RegistryKey<ItemGroup> FOOD_ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, MystiasIzakaya.id("food_item_group"));
    public static final RegistryKey<ItemGroup> DRINK_ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, MystiasIzakaya.id("drink_item_group"));
    public static final ItemGroup KITCHENWARE_ITEM_GROUP = PolymerItemGroupUtils.builder()
            .icon(() -> new ItemStack(MIBlocks.COOKING_POT))
            .displayName(Text.translatable("item_group.kitchenware_item_group"))
            .build();
    public static final ItemGroup INGREDIENT_ITEM_GROUP = PolymerItemGroupUtils.builder()
            .icon(() -> new ItemStack(MIItems.BLACK_PORK))
            .displayName(Text.translatable("item_group.ingredients_item_group"))
            .build();
    public static final ItemGroup SEEDS_ITEM_GROUP = PolymerItemGroupUtils.builder()
            .icon(MIItemGroups::getSeedItemIcon)
            .displayName(Text.translatable("item_group.seed_item_group"))
            .build();
    public static final ItemGroup FOOD_ITEM_GROUP = PolymerItemGroupUtils.builder()
            .icon(MIItemGroups::getFoodItemIcon)
            .displayName(Text.translatable("item_group.food_item_group"))
            .build();
    public static final ItemGroup DRINK_ITEM_GROUP = PolymerItemGroupUtils.builder()
            .icon(() -> new ItemStack(MIItems.GREEN_TEA))
            .displayName(Text.translatable("item_group.drink_item_group"))
            .build();

    static {
        PolymerItemGroupUtils.registerPolymerItemGroup(KITCHENWARE_ITEM_GROUP_KEY, KITCHENWARE_ITEM_GROUP);
        PolymerItemGroupUtils.registerPolymerItemGroup(INGREDIENT_ITEM_GROUP_KEY, INGREDIENT_ITEM_GROUP);
        PolymerItemGroupUtils.registerPolymerItemGroup(SEEDS_ITEM_GROUP_KEY, SEEDS_ITEM_GROUP);
        PolymerItemGroupUtils.registerPolymerItemGroup(FOOD_ITEM_GROUP_KEY, FOOD_ITEM_GROUP);
        PolymerItemGroupUtils.registerPolymerItemGroup(DRINK_ITEM_GROUP_KEY, DRINK_ITEM_GROUP);
    }

    public static void registerItemGroups() {
        ItemGroupEvents.modifyEntriesEvent(KITCHENWARE_ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.add(MIBlocks.COOKING_POT);
            itemGroup.add(MIBlocks.CUTTING_BOARD);
            itemGroup.add(MIBlocks.FRYING_PAN);
            itemGroup.add(MIBlocks.GRILL);
            itemGroup.add(MIBlocks.STEAMER);

            itemGroup.add(MIBlocks.MYSTIA_COOKING_POT);
            itemGroup.add(MIBlocks.MYSTIA_CUTTING_BOARD);
            itemGroup.add(MIBlocks.MYSTIA_FRYING_PAN);
            itemGroup.add(MIBlocks.MYSTIA_GRILL);
            itemGroup.add(MIBlocks.MYSTIA_STEAMER);

            itemGroup.add(MIBlocks.SUPER_COOKING_POT);
            itemGroup.add(MIBlocks.SUPER_CUTTING_BOARD);
            itemGroup.add(MIBlocks.SUPER_FRYING_PAN);
            itemGroup.add(MIBlocks.SUPER_GRILL);
            itemGroup.add(MIBlocks.SUPER_STEAMER);

            itemGroup.add(MIBlocks.EXTREME_COOKING_POT);
            itemGroup.add(MIBlocks.EXTREME_CUTTING_BOARD);
            itemGroup.add(MIBlocks.EXTREME_FRYING_PAN);
            itemGroup.add(MIBlocks.EXTREME_GRILL);
            itemGroup.add(MIBlocks.EXTREME_STEAMER);

            itemGroup.add(MIBlocks.ITEM_DISPLAY);

//            itemGroup.add(MIBlocks.COOKTOP);
        });
        ItemGroupEvents.modifyEntriesEvent(INGREDIENT_ITEM_GROUP_KEY).register(itemGroup -> {
            for (Item item : MIItems.INGREDIENTS) {
                itemGroup.add(item);
            }
        });
        ItemGroupEvents.modifyEntriesEvent(SEEDS_ITEM_GROUP_KEY).register(itemGroup -> {
            for (Map.Entry<Identifier, PolymerCropCreator.Instance> view : PolymerCropCreator.getViews()) {
                PolymerCropCreator.Instance instance = view.getValue();
                Item seed = instance.getSeed();
                itemGroup.add(seed);
            }
            itemGroup.add(MIBlocks.UDUMBARA_FLOWER);
            itemGroup.add(MIBlocks.TREMELLA);
            WoodCreator.INSTANCES.forEach((instance)-> itemGroup.add(instance.sapling()));

        });
        ItemGroupEvents.modifyEntriesEvent(FOOD_ITEM_GROUP_KEY).register(itemGroup -> {
            for (Item item : MIItems.FOOD_ITEMS) {
                itemGroup.add(item);
            }
        });
        ItemGroupEvents.modifyEntriesEvent(DRINK_ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.add(Items.BARREL);
            for (Item item : MIItems.DRINK_ITEMS) {
                itemGroup.add(item);
            }
//            itemGroup.add(ModItems.NOT_COMPLETED);
        });
    }

    public static ItemStack getSeedItemIcon() {
        for (Map.Entry<Identifier, PolymerCropCreator.Instance> view : PolymerCropCreator.getViews()) {
            PolymerCropCreator.Instance instance = view.getValue();
            return new ItemStack(instance.getSeed());
        }
        return new ItemStack(Items.WHEAT_SEEDS);
    }

    public static ItemStack getFoodItemIcon() {
        for (Item foodItem : MIItems.FOOD_ITEMS) {
            return new ItemStack(foodItem);
        }
        return new ItemStack(Items.COOKED_BEEF);
    }
}
