package cc.thonly.reverie_dreams.datagen.tag;

import cc.thonly.reverie_dreams.block.BlockTypeGroup;
import cc.thonly.reverie_dreams.block.creator.CropBlockCreator;
import cc.thonly.reverie_dreams.block.creator.WoodCreator;
import cc.thonly.reverie_dreams.data.FumoType;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.item.ItemTypeGroup;
import cc.thonly.reverie_dreams.item.base.AlbumItem;
import cc.thonly.reverie_dreams.item.base.ArmorItem;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.FumoTypes;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDDrinkItems;
import cc.thonly.reverie_dreams.registry.content.item.RDFoodItems;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import cc.thonly.reverie_dreams.registry.tag.FarmersDelightCommonItemTags;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ItemTagProvider extends FabricTagProvider.ItemTagProvider {

    public ItemTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        // === 基础工具方法 ===
        BiConsumer<TagKey<Item>, Collection<? extends Item>> addAll = (tag, items) -> getOrCreateTagBuilder(tag).add(items.toArray(Item[]::new));
        Supplier<List<Item>> allDanmakuItemGetter = () -> {
            RegistryHandler<DanmakuType> registry = RegistryHandlers.DANMAKU_TYPE;
            Stream<Item> itemStream = registry.values().stream().map(DanmakuType::getItem);
            return itemStream.toList();
        };
        Supplier<List<Item>> allToolGetter = () -> {
            List<Item> list = new ArrayList<>();
            list.add(RDItems.NUE_TRIDENT);
            list.addAll(allDanmakuItemGetter.get());
            return list;
        };
        List<Item> allTool = allToolGetter.get();

        // === 通用 Tag ===
        getOrCreateTagBuilder(RDItemTags.EMPTY).add(Items.BEDROCK).add(Items.BARRIER);
        addAll.accept(RDItemTags.FUMO, FumoTypes.getView().stream().map(FumoType::item).toList());
        addAll.accept(ItemTags.CREEPER_DROP_MUSIC_DISCS, AlbumItem.ITEMS);

        // === 工具类 Tag ===
        addAll.accept(ItemTags.SWORDS, ItemTypeGroup.SWORD.items());
        addAll.accept(ItemTags.PICKAXES, ItemTypeGroup.PICKAXES.items());
        addAll.accept(ItemTags.AXES, ItemTypeGroup.AXES.items());
        addAll.accept(ItemTags.SHOVELS, ItemTypeGroup.SHOVELS.items());
        addAll.accept(ItemTags.HOES, ItemTypeGroup.HOES.items());
        addAll.accept(ItemTags.TRIDENT_ENCHANTABLE, allTool);
        addAll.accept(ItemTags.DURABILITY_ENCHANTABLE, allTool);
        addAll.accept(ItemTags.DURABILITY_ENCHANTABLE, List.of(RDItems.TENGU_SHIELD));
        addAll.accept(ConventionalItemTags.SHIELD_TOOLS, List.of(RDItems.TENGU_SHIELD));

        // === 盔甲类 Tag ===
        addAll.accept(ItemTags.HEAD_ARMOR, ArmorItem.HEAD_ITEMS);
        addAll.accept(ItemTags.CHEST_ARMOR, ArmorItem.CHEST_ITEMS);
        addAll.accept(ItemTags.LEG_ARMOR, ArmorItem.LEG_ITEMS);
        addAll.accept(ItemTags.FOOT_ARMOR, ArmorItem.FEET_ITEMS);
        addAll.accept(RDItemTags.ARMOR, ArmorItem.ITEMS);

        // === 工具材料 ===
        getOrCreateTagBuilder(RDItemTags.SILVER_ITEM).add(
                RDItems.SILVER_AXE,
                RDItems.SILVER_BOOTS,
                RDItems.SILVER_CHESTPLATE,
                RDItems.SILVER_LEGGINGS,
                RDItems.SILVER_COIN,
                RDItems.SILVER_HELMET,
                RDItems.SILVER_NUGGET,
                RDItems.SILVER_PICKAXE,
                RDItems.SILVER_SHOVEL,
                RDItems.SILVER_SWORD,
                RDItems.SILVER_HOE,
                RDItems.SILVER_INGOT,
                RDBlocks.SILVER_BLOCK.asItem(),
                RDBlocks.SILVER_CHEST_BLOCK.chestBlock().asItem(),
                RDBlocks.SILVER_ORE.asItem(),
                RDBlocks.DEEPSLATE_SILVER_ORE.asItem()
        );
        getOrCreateTagBuilder(RDItemTags.SILVER_ARMOR).add(RDItems.SILVER_HELMET, RDItems.SILVER_CHESTPLATE, RDItems.SILVER_LEGGINGS, RDItems.SILVER_BOOTS);
        getOrCreateTagBuilder(RDItemTags.SILVER_TOOL_MATERIALS).add(RDItems.SILVER_INGOT);
        getOrCreateTagBuilder(RDItemTags.MAGIC_ICE_ARMOR).add(RDItems.MAGIC_ICE_HELMET, RDItems.MAGIC_ICE_CHESTPLATE, RDItems.MAGIC_ICE_LEGGINGS, RDItems.MAGIC_ICE_BOOTS);
        getOrCreateTagBuilder(RDItemTags.MAGIC_ICE_TOOL_MATERIALS).add(RDItems.ICE_SCALES);
        getOrCreateTagBuilder(RDItemTags.MAID_ARMOR).add(RDItems.MAID_HAIRBAND, RDItems.MAID_UPPER_SKIRT, RDItems.MAID_LOWER_SKIRT, RDItems.MAID_SHOE);
        getOrCreateTagBuilder(RDItemTags.DREAM_ARMOR).add(RDItems.DREAM_HELMET).add(RDItems.DREAM_CHESTPLATE).add(RDItems.DREAM_LEGGINGS).add(RDItems.DREAM_BOOTS);
        getOrCreateTagBuilder(RDItemTags.DREAM_TOOL_MATERIALS).add(RDItems.DREAM_CRYSTAL_FRAGMENT);

        // === 弹幕 ===
        var danmaku = getOrCreateTagBuilder(RDItemTags.DANMAKU_ITEM);
        for (DanmakuType danmakuType : RegistryHandlers.DANMAKU_TYPE) {
            danmaku.add(danmakuType.getItem());
        }
        danmaku.add(RDItems.KNIFE);

        // === 自定义方块 ===
        FabricTagProvider<Item>.FabricTagBuilder planks = getOrCreateTagBuilder(ItemTags.PLANKS);
        WoodCreator.INSTANCES.stream().map(ins -> ins.planks().asItem()).forEach(planks::add);

        getOrCreateTagBuilder(RDItemTags.ORB_BLOCK).add(
                RDBlocks.RED_ORB_BLOCK.asItem(),
                RDBlocks.YELLOW_ORB_BLOCK.asItem(),
                RDBlocks.BLUE_ORB_BLOCK.asItem(),
                RDBlocks.GREEN_ORB_BLOCK.asItem(),
                RDBlocks.PURPLE_ORB_BLOCK.asItem()
        );
        getOrCreateTagBuilder(RDItemTags.POWER_BLOCK).add(RDBlocks.POWER_BLOCK.asItem());
        getOrCreateTagBuilder(RDItemTags.POINT_BLOCK).add(RDBlocks.POINT_BLOCK.asItem());
        getOrCreateTagBuilder(RDItemTags.SILVER_BLOCK).add(RDBlocks.SILVER_BLOCK.asItem());
        getOrCreateTagBuilder(RDItemTags.VAISRAVANAS_PAGODA).add(Items.BLAZE_POWDER);
        FabricTagProvider<Item>.FabricTagBuilder ingredientItems = getOrCreateTagBuilder(RDItemTags.INGREDIENT_ITEM);
        RDIngredientItems.INGREDIENTS.forEach(ingredientItems::add);
        FabricTagProvider<Item>.FabricTagBuilder foodItems = getOrCreateTagBuilder(RDItemTags.FOOD_ITEM);
        RDFoodItems.FOOD_ITEMS.forEach(foodItems::add);
        FabricTagProvider<Item>.FabricTagBuilder drinkItems = getOrCreateTagBuilder(RDItemTags.DRINK_ITEM);
        RDDrinkItems.DRINK_ITEMS.forEach(drinkItems::add);

        getOrCreateTagBuilder(RDItemTags.ROLE_TAME_FOOD)
                .add(Items.CAKE)
                .add(RDFoodItems.ORDINARY_SMALL_CAKE)
                .add(RDFoodItems.SCARLET_DEVILS_CAKE);

        // === 兼容物品 ===
        FabricTagProvider<Item>.FabricTagBuilder foods = getOrCreateTagBuilder(ConventionalItemTags.FOODS);
        for (Item foodItem : RDFoodItems.FOOD_ITEMS) {
            foods.add(foodItem);
        }
        getOrCreateTagBuilder(RDItemTags.PEACH).add(RDIngredientItems.PEACH);
        getOrCreateTagBuilder(RDItemTags.REPLACEABLE_BLANK_PHOTOS).add(RDItems.EMPTY_PHOTO);

        getOrCreateTagBuilder(RDItemTags.MUSICAL_INSTRUMENTS).add(RDItems.KEYBOARD, RDItems.VIOLIN, RDItems.TRUMPET);
        getOrCreateTagBuilder(RDItemTags.COINS).add(RDItems.COPPER_COIN, RDItems.SILVER_COIN, RDItems.GOLD_COIN);

        // === 方块物品分类 ===
        Map<TagKey<Item>, Collection<? extends ItemLike>> blockItemGroups = Map.of(
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
            var builder = getOrCreateTagBuilder(tag);
            list.forEach(item -> builder.add(item.asItem()));
        });

        // === 种子 ===
        var seeds = getOrCreateTagBuilder(ConventionalItemTags.SEEDS);
        var villagerPlantableSeeds = getOrCreateTagBuilder(ItemTags.VILLAGER_PLANTABLE_SEEDS);
        for (var entry : CropBlockCreator.getViews()) {
            Item seed = entry.getValue().getSeed();
            villagerPlantableSeeds.add(seed);
            seeds.add(seed);
        }

        var pigFoods = getOrCreateTagBuilder(ItemTags.PIG_FOOD);
        pigFoods.add(RDIngredientItems.WHITE_RADISH);

        var rabbitFoods = getOrCreateTagBuilder(ItemTags.RABBIT_FOOD);
        rabbitFoods.add(RDIngredientItems.WHITE_RADISH);


        // === 模组兼容扩展 ===
        this.configureCompat(wrapperLookup);
    }


    protected void configureCompat(HolderLookup.Provider wrapperLookup) {
        // Farmer'delight
        var onion = valueLookupCommon("crops/onion");
        var tomatoCrop = valueLookupCommon("crops/tomato");
        var cabbage = valueLookupCommon("crops/cabbage");
        var rawSalmon = valueLookupCommon("foods/raw_salmon");
        var rawFish = valueLookupCommon("foods/raw_fish");
        var tomatoFood = valueLookupCommon("foods/tomato");

        onion.add(RDIngredientItems.ONION);
        tomatoCrop.add(RDIngredientItems.TOMATO);
        rawSalmon.add(RDIngredientItems.SALMON);
        rawFish.add(RDIngredientItems.SALMON, RDIngredientItems.HAGFISH, RDIngredientItems.TUNA, RDIngredientItems.SUPREME_TUNA);
        tomatoFood.add(RDIngredientItems.TOMATO);

        var meals = valueLookupFarmerDelight("meals");
        meals.add(
                RDIngredientItems.BLACK_PORK,
                RDIngredientItems.VENISON,
                RDIngredientItems.WAGYU_BEEF,
                RDIngredientItems.WILD_BOAR_MEAT
        );

        getOrCreateTagBuilder(FarmersDelightCommonItemTags.FOODS_TOMATO).add(RDIngredientItems.TOMATO);
        getOrCreateTagBuilder(FarmersDelightCommonItemTags.FOODS_ONION).add(RDIngredientItems.ONION);
    }

    private FabricTagProvider<Item>.FabricTagBuilder valueLookupFarmerDelight(String name) {
        return getOrCreateTagBuilder(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("farmersdelight", name)));
    }

    private FabricTagProvider<Item>.FabricTagBuilder valueLookupCommon(String name) {
        return getOrCreateTagBuilder(TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", name)));
    }

}
