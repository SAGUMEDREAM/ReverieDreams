package cc.thonly.reverie_dreams.datagen;

import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.reverie_dreams.block.BlockTypeGroup;
import cc.thonly.reverie_dreams.block.CropBlockCreator;
import cc.thonly.reverie_dreams.block.WoodCreator;
import cc.thonly.reverie_dreams.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.fumo.Fumo;
import cc.thonly.reverie_dreams.fumo.Fumos;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.item.ItemTypeGroup;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.item.base.*;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.data.tag.ProvidedTagBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {

    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        // === 基础工具方法 ===
        BiConsumer<TagKey<Item>, Collection<? extends Item>> addAll = (tag, items) -> valueLookupBuilder(tag).add(items.toArray(Item[]::new));
        Supplier<List<Item>> allDanmakuItemGetter = () -> {
            IntrinsicalRegister<DanmakuType> registry = RegistryManager.DANMAKU_TYPE;
            Stream<Item> itemStream = registry.values().stream().map(DanmakuType::getItem);
            return itemStream.toList();
        };
        Supplier<List<Item>> allToolGetter = () -> {
            List<Item> list = new ArrayList<>();
            list.add(ModItems.NUE_TRIDENT);
            list.addAll(allDanmakuItemGetter.get());
            return list;
        };
        List<Item> allTool = allToolGetter.get();

        // === 通用 Tag ===
        valueLookupBuilder(ModTags.ItemTypeTag.EMPTY).add(Items.BEDROCK).add(Items.BARRIER);
        addAll.accept(ModTags.ItemTypeTag.FUMO, Fumos.getView().stream().map(Fumo::item).toList());
        addAll.accept(ItemTags.CREEPER_DROP_MUSIC_DISCS, AlbumItem.ITEMS);

        // === 工具类 Tag ===
        addAll.accept(ItemTags.SWORDS, ItemTypeGroup.SWORD.items());
        addAll.accept(ItemTags.PICKAXES, ItemTypeGroup.PICKAXES.items());
        addAll.accept(ItemTags.AXES, ItemTypeGroup.AXES.items());
        addAll.accept(ItemTags.SHOVELS, ItemTypeGroup.SHOVELS.items());
        addAll.accept(ItemTags.HOES, ItemTypeGroup.HOES.items());
        addAll.accept(ItemTags.TRIDENT_ENCHANTABLE, allTool);
        addAll.accept(ItemTags.DURABILITY_ENCHANTABLE, allTool);
        addAll.accept(ItemTags.DURABILITY_ENCHANTABLE, List.of(ModItems.TENGU_SHIELD));
        addAll.accept(ConventionalItemTags.SHIELD_TOOLS, List.of(ModItems.TENGU_SHIELD));

        // === 盔甲类 Tag ===
        addAll.accept(ItemTags.HEAD_ARMOR, ArmorItem.HEAD_ITEMS);
        addAll.accept(ItemTags.CHEST_ARMOR, ArmorItem.CHEST_ITEMS);
        addAll.accept(ItemTags.LEG_ARMOR, ArmorItem.LEG_ITEMS);
        addAll.accept(ItemTags.FOOT_ARMOR, ArmorItem.FEET_ITEMS);
        addAll.accept(ModTags.ItemTypeTag.ARMOR, ArmorItem.ITEMS);

        // === 工具材料 ===
        valueLookupBuilder(ModTags.ItemTypeTag.SILVER_ARMOR).add(ModItems.SILVER_HELMET, ModItems.SILVER_CHESTPLATE, ModItems.SILVER_LEGGINGS, ModItems.SILVER_BOOTS);
        valueLookupBuilder(ModTags.ItemTypeTag.SILVER_TOOL_MATERIALS).add(ModItems.SILVER_INGOT);
        valueLookupBuilder(ModTags.ItemTypeTag.MAGIC_ICE_ARMOR).add(ModItems.MAGIC_ICE_HELMET, ModItems.MAGIC_ICE_CHESTPLATE, ModItems.MAGIC_ICE_LEGGINGS, ModItems.MAGIC_ICE_BOOTS);
        valueLookupBuilder(ModTags.ItemTypeTag.MAGIC_ICE_TOOL_MATERIALS).add(ModItems.ICE_SCALES);
        valueLookupBuilder(ModTags.ItemTypeTag.MAID_ARMOR).add(ModItems.MAID_HAIRBAND, ModItems.MAID_UPPER_SKIRT, ModItems.MAID_LOWER_SKIRT, ModItems.MAID_SHOE);

        // === 自定义方块 ===
        valueLookupBuilder(ItemTags.PLANKS).add(WoodCreator.INSTANCES.stream().map(ins->ins.planks().asItem()));
        valueLookupBuilder(ModTags.ItemTypeTag.ORB_BLOCK).add(
                ModBlocks.RED_ORB_BLOCK.asItem(),
                ModBlocks.YELLOW_ORB_BLOCK.asItem(),
                ModBlocks.BLUE_ORB_BLOCK.asItem(),
                ModBlocks.GREEN_ORB_BLOCK.asItem(),
                ModBlocks.PURPLE_ORB_BLOCK.asItem()
        );
        valueLookupBuilder(ModTags.ItemTypeTag.POWER_BLOCK).add(ModBlocks.POWER_BLOCK.asItem());
        valueLookupBuilder(ModTags.ItemTypeTag.POINT_BLOCK).add(ModBlocks.POINT_BLOCK.asItem());
        valueLookupBuilder(ModTags.ItemTypeTag.SILVER_BLOCK).add(ModBlocks.SILVER_BLOCK.asItem());
        valueLookupBuilder(ModTags.ItemTypeTag.VAISRAVANAS_PAGODA).add(Items.BLAZE_POWDER);
        valueLookupBuilder(ModTags.ItemTypeTag.INGREDIENT_ITEM).add(MIItems.INGREDIENTS);
        valueLookupBuilder(ModTags.ItemTypeTag.FOOD_ITEM).add(MIItems.FOOD_ITEMS);
        valueLookupBuilder(ModTags.ItemTypeTag.DRINK_ITEM).add(MIItems.DRINK_ITEMS);

        valueLookupBuilder(ModTags.ItemTypeTag.ROLE_TAME_FOOD)
                .add(Items.CAKE);

        // === 兼容物品 ===
        valueLookupBuilder(ConventionalItemTags.FOODS).add(MIItems.FOOD_ITEMS);
        valueLookupBuilder(ModTags.ItemTypeTag.PEACH).add(MIItems.PEACH);

        // === 方块物品分类 ===
        Map<TagKey<Item>, Collection<? extends ItemConvertible>> blockItemGroups = Map.of(
                ItemTags.FENCES, BlockTypeGroup.FENCE.items(),
                ItemTags.FENCE_GATES, BlockTypeGroup.FENCE_GATE.items(),
                ItemTags.WALLS, BlockTypeGroup.WALL.items(),
                ItemTags.STAIRS, BlockTypeGroup.STAIR.items(),
                ItemTags.SLABS, BlockTypeGroup.SLAB.items(),
                ItemTags.BUTTONS, BlockTypeGroup.BUTTON.items(),
                ItemTags.TRAPDOORS, BlockTypeGroup.TRAPDOOR.items(),
                ItemTags.DOORS, BlockTypeGroup.DOOR.items(),
                ItemTags.LEAVES, BlockTypeGroup.LEAVES.items()
        );
        blockItemGroups.forEach((tag, list) -> {
            ProvidedTagBuilder<Item, Item> builder = valueLookupBuilder(tag);
            list.forEach(item -> builder.add(item.asItem()));
        });

        // === 种子 ===
        ProvidedTagBuilder<Item, Item> seeds = valueLookupBuilder(ConventionalItemTags.SEEDS);
        ProvidedTagBuilder<Item, Item> villagerPlantableSeeds = valueLookupBuilder(ItemTags.VILLAGER_PLANTABLE_SEEDS);
        for (var entry : CropBlockCreator.getViews()) {
            Item seed = entry.getValue().getSeed();
            villagerPlantableSeeds.add(seed);
            seeds.add(seed);
        }

        ProvidedTagBuilder<Item, Item> pigFoods = valueLookupBuilder(ItemTags.PIG_FOOD);
        pigFoods.add(MIItems.WHITE_RADISH);

        ProvidedTagBuilder<Item, Item> rabbitFoods = valueLookupBuilder(ItemTags.RABBIT_FOOD);
        rabbitFoods.add(MIItems.WHITE_RADISH);

        // === 模组兼容扩展 ===
        this.configureCompat(wrapperLookup);
    }


    protected void configureCompat(RegistryWrapper.WrapperLookup wrapperLookup) {
        // Farmer'delight
        ProvidedTagBuilder<Item, Item> onion = valueLookupCommon("crops/onion");
        ProvidedTagBuilder<Item, Item> tomatoCrop = valueLookupCommon("crops/tomato");
        ProvidedTagBuilder<Item, Item> cabbage = valueLookupCommon("crops/cabbage");
        ProvidedTagBuilder<Item, Item> rawSalmon = valueLookupCommon("foods/raw_salmon");
        ProvidedTagBuilder<Item, Item> rawFish = valueLookupCommon("foods/raw_fish");
        ProvidedTagBuilder<Item, Item> tomatoFood = valueLookupCommon("foods/tomato");

        onion.add(MIItems.ONION);
        tomatoCrop.add(MIItems.TOMATO);
        rawSalmon.add(MIItems.SALMON);
        rawFish.add(MIItems.SALMON, MIItems.HAGFISH, MIItems.TUNA, MIItems.SUPREME_TUNA);
        tomatoFood.add(MIItems.TOMATO);

        ProvidedTagBuilder<Item, Item> meals = valueLookupFarmerDelight("meals");
        meals.add(
                MIItems.BLACK_PORK,
                MIItems.VENISON,
                MIItems.WAGYU_BEEF,
                MIItems.WILD_BOAR_MEAT
        );
    }

    private ProvidedTagBuilder<Item, Item> valueLookupFarmerDelight(String name) {
        return valueLookupBuilder(TagKey.of(RegistryKeys.ITEM, Identifier.of("farmersdelight", name)));
    }

    private ProvidedTagBuilder<Item, Item> valueLookupCommon(String name) {
        return valueLookupBuilder(TagKey.of(RegistryKeys.ITEM, Identifier.of("c", name)));
    }

}
