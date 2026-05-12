package cc.thonly.reverie_dreams.fabric.datagen.tag;

import cc.thonly.reverie_dreams.block.BlockTypeGroup;
import cc.thonly.reverie_dreams.block.bundle.CropBlockBundle;
import cc.thonly.reverie_dreams.block.bundle.WoodBundle;
import cc.thonly.reverie_dreams.data.FumoType;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.item.ItemTypeGroup;
import cc.thonly.reverie_dreams.item.base.AlbumItem;
import cc.thonly.reverie_dreams.item.base.ArmorItem;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.FumoTypes;
import cc.thonly.reverie_dreams.registry.content.block.KitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDDrinkItems;
import cc.thonly.reverie_dreams.registry.content.item.RDFoodItems;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import cc.thonly.reverie_dreams.registry.tag.FarmersDelightCommonItemTags;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.Identifier;
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
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ItemTagProvider extends FabricTagsProvider.ItemTagsProvider {

    public ItemTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        // === 基础工具方法 ===
        BiConsumer<TagKey<Item>, Collection<? extends Item>> addAll = (tag, items) -> valueLookupBuilder(tag).add(items.toArray(Item[]::new));
        Supplier<List<Item>> allDanmakuItemGetter = () -> {
            RegistryImpl<DanmakuType> registry = RegistryImpls.DANMAKU_TYPE;
            Stream<Item> itemStream = registry.values().stream().map(DanmakuType::getItemHolder).map(ItemLike::asItem);
            return itemStream.toList();
        };
        Supplier<List<Item>> allToolGetter = () -> {
            List<Item> list = new ArrayList<>();
            list.add(RDItems.NUE_TRIDENT.asItem());
            list.addAll(allDanmakuItemGetter.get());
            return list;
        };
        List<Item> allTool = allToolGetter.get();

        // === 通用 Tag ===
        valueLookupBuilder(RDItemTags.EMPTY).add(Items.BEDROCK).add(Items.BARRIER);
        addAll.accept(RDItemTags.FUMO, FumoTypes.getView().stream().map(FumoType::item).toList());
        addAll.accept(ItemTags.CREEPER_DROP_MUSIC_DISCS, AlbumItem.ITEMS);

        // === 工具类 Tag ===
        addAll.accept(ItemTags.SWORDS, ItemTypeGroup.SWORD.items());
        addAll.accept(ItemTags.PICKAXES, ItemTypeGroup.PICKAXES.items());
        addAll.accept(ItemTags.AXES, ItemTypeGroup.AXES.items());
        addAll.accept(ItemTags.SHOVELS, ItemTypeGroup.SHOVELS.items());
        addAll.accept(ItemTags.HOES, ItemTypeGroup.HOES.items());
        addAll.accept(ItemTags.SPEARS, ItemTypeGroup.SPEARS.items());
        addAll.accept(ItemTags.TRIDENT_ENCHANTABLE, allTool);
        addAll.accept(ItemTags.DURABILITY_ENCHANTABLE, allTool);
        addAll.accept(ItemTags.DURABILITY_ENCHANTABLE, List.of(RDItems.TENGU_SHIELD.asItem()));
        addAll.accept(ConventionalItemTags.SHIELD_TOOLS, List.of(RDItems.TENGU_SHIELD.asItem()));

        // === 盔甲类 Tag ===
        addAll.accept(ItemTags.HEAD_ARMOR, ArmorItem.HEAD_ITEMS);
        addAll.accept(ItemTags.CHEST_ARMOR, ArmorItem.CHEST_ITEMS);
        addAll.accept(ItemTags.LEG_ARMOR, ArmorItem.LEG_ITEMS);
        addAll.accept(ItemTags.FOOT_ARMOR, ArmorItem.FEET_ITEMS);
        addAll.accept(RDItemTags.ARMOR, ArmorItem.ITEMS);

        // === 工具材料 ===
        valueLookupBuilder(RDItemTags.SILVER_ITEM).add(
                RDItems.SILVER_AXE.asItem(),
                RDItems.SILVER_BOOTS.asItem(),
                RDItems.SILVER_CHESTPLATE.asItem(),
                RDItems.SILVER_LEGGINGS.asItem(),
                RDItems.SILVER_COIN.asItem(),
                RDItems.SILVER_HELMET.asItem(),
                RDItems.SILVER_NUGGET.asItem(),
                RDItems.SILVER_PICKAXE.asItem(),
                RDItems.SILVER_SHOVEL.asItem(),
                RDItems.SILVER_SWORD.asItem(),
                RDItems.SILVER_HOE.asItem(),
                RDItems.SILVER_SPEAR.asItem(),
                RDItems.SILVER_INGOT.asItem(),
                RDBlocks.SILVER_BLOCK.asItem(),
                RDBlocks.SILVER_CHEST_BLOCK.chestBlock().asItem(),
                RDBlocks.SILVER_ORE.asItem(),
                RDBlocks.DEEPSLATE_SILVER_ORE.asItem()
        );
        valueLookupBuilder(RDItemTags.SILVER_ARMOR).add(RDItems.SILVER_HELMET.asItem(), RDItems.SILVER_CHESTPLATE.asItem(), RDItems.SILVER_LEGGINGS.asItem(), RDItems.SILVER_BOOTS.asItem());
        valueLookupBuilder(RDItemTags.SILVER_TOOL_MATERIALS).add(RDItems.SILVER_INGOT.asItem());
        valueLookupBuilder(RDItemTags.MAGIC_ICE_ARMOR).add(RDItems.MAGIC_ICE_HELMET.asItem(), RDItems.MAGIC_ICE_CHESTPLATE.asItem(), RDItems.MAGIC_ICE_LEGGINGS.asItem(), RDItems.MAGIC_ICE_BOOTS.asItem());
        valueLookupBuilder(RDItemTags.MAGIC_ICE_TOOL_MATERIALS).add(RDItems.ICE_SCALES.asItem());
        valueLookupBuilder(RDItemTags.MAID_ARMOR).add(RDItems.MAID_HAIRBAND.asItem(), RDItems.MAID_UPPER_SKIRT.asItem(), RDItems.MAID_LOWER_SKIRT.asItem(), RDItems.MAID_SHOE.asItem());
        valueLookupBuilder(RDItemTags.DREAM_ARMOR).add(RDItems.DREAM_HELMET.asItem()).add(RDItems.DREAM_CHESTPLATE.asItem()).add(RDItems.DREAM_LEGGINGS.asItem()).add(RDItems.DREAM_BOOTS.asItem());
        valueLookupBuilder(RDItemTags.DREAM_TOOL_MATERIALS).add(RDItems.DREAM_CRYSTAL_FRAGMENT.asItem());

        // === 弹幕 ===
        TagAppender<Item, Item> danmaku = valueLookupBuilder(RDItemTags.DANMAKU_ITEM);
        for (DanmakuType danmakuType : RegistryImpls.DANMAKU_TYPE) {
            danmaku.add(danmakuType.getItemHolder().asItem());
        }
        danmaku.add(RDItems.KNIFE.asItem());
        TagAppender<Item, Item> danmakuRepairAcceptableItems = valueLookupBuilder(RDItemTags.DANMAKU_REPAIR_ACCEPTABLE_ITEM);
        danmakuRepairAcceptableItems.add(RDItems.POWER.asItem());

        // === 自定义方块 ===
        valueLookupBuilder(ItemTags.PLANKS).addAll(WoodBundle.INSTANCES.stream().map(ins -> ins.planks().asItem()));
        valueLookupBuilder(RDItemTags.ORB_BLOCK).add(
                RDBlocks.RED_ORB_BLOCK.asItem(),
                RDBlocks.YELLOW_ORB_BLOCK.asItem(),
                RDBlocks.BLUE_ORB_BLOCK.asItem(),
                RDBlocks.GREEN_ORB_BLOCK.asItem(),
                RDBlocks.PURPLE_ORB_BLOCK.asItem()
        );
        valueLookupBuilder(RDItemTags.POWER_BLOCK).add(RDBlocks.POWER_BLOCK.asItem());
        valueLookupBuilder(RDItemTags.POINT_BLOCK).add(RDBlocks.POINT_BLOCK.asItem());
        valueLookupBuilder(RDItemTags.SILVER_BLOCK).add(RDBlocks.SILVER_BLOCK.asItem());
        valueLookupBuilder(RDItemTags.VAISRAVANAS_PAGODA).add(Items.BLAZE_POWDER);
        valueLookupBuilder(RDItemTags.INGREDIENT_ITEM).addAll(RDIngredientItems.INGREDIENTS.stream().map(ItemLike::asItem).toList());
        valueLookupBuilder(RDItemTags.FOOD_ITEM).addAll(RDFoodItems.FOOD_ITEMS.stream().map(ItemLike::asItem).toList());
        valueLookupBuilder(RDItemTags.DRINK_ITEM).addAll(RDDrinkItems.DRINK_ITEMS.stream().map(ItemLike::asItem).toList());

        valueLookupBuilder(RDItemTags.ROLE_TAME_FOOD)
                .add(Items.CAKE)
                .add(RDFoodItems.ORDINARY_SMALL_CAKE.asItem())
                .add(RDFoodItems.SCARLET_DEVILS_CAKE.asItem());

        // === 兼容物品 ===
        valueLookupBuilder(ConventionalItemTags.FOODS).addAll(RDFoodItems.FOOD_ITEMS.stream().map(ItemLike::asItem).toList());
        valueLookupBuilder(RDItemTags.PEACH).add(RDIngredientItems.PEACH.asItem());
        valueLookupBuilder(RDItemTags.REPLACEABLE_BLANK_PHOTOS).add(RDItems.EMPTY_PHOTO.asItem());

        valueLookupBuilder(RDItemTags.MUSICAL_INSTRUMENTS).add(RDItems.KEYBOARD.asItem(), RDItems.VIOLIN.asItem(), RDItems.TRUMPET.asItem());
        valueLookupBuilder(RDItemTags.COINS).add(RDItems.COPPER_COIN.asItem(), RDItems.SILVER_COIN.asItem(), RDItems.GOLD_COIN.asItem());

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
            TagAppender<Item, Item> builder = valueLookupBuilder(tag);
            list.forEach(item -> builder.add(item.asItem()));
        });

        // === 种子 ===
        TagAppender<Item, Item> seeds = valueLookupBuilder(ConventionalItemTags.SEEDS);
        TagAppender<Item, Item> villagerPlantableSeeds = valueLookupBuilder(ItemTags.VILLAGER_PLANTABLE_SEEDS);
        for (var entry : CropBlockBundle.getViews()) {
            Item seed = entry.getValue().getSeed().asItem();
            villagerPlantableSeeds.add(seed);
            seeds.add(seed);
        }

        TagAppender<Item, Item> pigFoods = valueLookupBuilder(ItemTags.PIG_FOOD);
        pigFoods.add(RDIngredientItems.WHITE_RADISH.asItem());

        TagAppender<Item, Item> rabbitFoods = valueLookupBuilder(ItemTags.RABBIT_FOOD);
        rabbitFoods.add(RDIngredientItems.WHITE_RADISH.asItem());

        TagAppender<Item, Item> cookingTop = valueLookupBuilder(RDItemTags.COOKING_TOP);
        TagAppender<Item, Item> cuttingBoard = valueLookupBuilder(RDItemTags.CUTTING_BOARD);
        TagAppender<Item, Item> fryingPan = valueLookupBuilder(RDItemTags.FRYING_PAN);
        TagAppender<Item, Item> grill = valueLookupBuilder(RDItemTags.GRILL);
        TagAppender<Item, Item> steamer = valueLookupBuilder(RDItemTags.STEAMER);
        TagAppender<Item, Item> kitchenware = valueLookupBuilder(RDItemTags.KITCHENWARE);
        cookingTop.add(
                KitchenBlocks.COOKING_POT.asItem(),
                KitchenBlocks.MYSTIA_COOKING_POT.asItem(),
                KitchenBlocks.SUPER_COOKING_POT.asItem(),
                KitchenBlocks.EXTREME_COOKING_POT.asItem(),
                KitchenBlocks.NUKE_COOKING_POT.asItem()
        );
        cuttingBoard.add(
                KitchenBlocks.CUTTING_BOARD.asItem(),
                KitchenBlocks.MYSTIA_CUTTING_BOARD.asItem(),
                KitchenBlocks.SUPER_CUTTING_BOARD.asItem(),
                KitchenBlocks.EXTREME_CUTTING_BOARD.asItem(),
                KitchenBlocks.NUKE_CUTTING_BOARD.asItem()
        );
        fryingPan.add(
                KitchenBlocks.FRYING_PAN.asItem(),
                KitchenBlocks.MYSTIA_FRYING_PAN.asItem(),
                KitchenBlocks.SUPER_FRYING_PAN.asItem(),
                KitchenBlocks.EXTREME_FRYING_PAN.asItem(),
                KitchenBlocks.NUKE_FRYING_PAN.asItem()
        );
        grill.add(
                KitchenBlocks.GRILL.asItem(),
                KitchenBlocks.MYSTIA_GRILL.asItem(),
                KitchenBlocks.SUPER_GRILL.asItem(),
                KitchenBlocks.EXTREME_GRILL.asItem(),
                KitchenBlocks.NUKE_GRILL.asItem()
        );
        steamer.add(
                KitchenBlocks.STEAMER.asItem(),
                KitchenBlocks.MYSTIA_STEAMER.asItem(),
                KitchenBlocks.SUPER_STEAMER.asItem(),
                KitchenBlocks.EXTREME_STEAMER.asItem(),
                KitchenBlocks.NUKE_STEAMER.asItem()
        );
        kitchenware.addOptionalTag(RDItemTags.COOKING_TOP);
        kitchenware.addOptionalTag(RDItemTags.CUTTING_BOARD);
        kitchenware.addOptionalTag(RDItemTags.FRYING_PAN);
        kitchenware.addOptionalTag(RDItemTags.GRILL);
        kitchenware.addOptionalTag(RDItemTags.STEAMER);

        valueLookupBuilder(ItemTags.RAILS)
                .add(RDBlocks.RAIL_CONTROLLER_BLOCK.asItem())
                .add(RDBlocks.SIGNAL_RAIL_BLOCK.asItem());

        // === 模组兼容扩展 ===
        this.configureCompat(wrapperLookup);
    }


    protected void configureCompat(HolderLookup.Provider wrapperLookup) {
        // Farmer'delight
        TagAppender<Item, Item> onion = valueLookupCommon("crops/onion");
        TagAppender<Item, Item> tomatoCrop = valueLookupCommon("crops/tomato");
        TagAppender<Item, Item> cabbage = valueLookupCommon("crops/cabbage");
        TagAppender<Item, Item> rawSalmon = valueLookupCommon("foods/raw_salmon");
        TagAppender<Item, Item> rawFish = valueLookupCommon("foods/raw_fish");
        TagAppender<Item, Item> tomatoFood = valueLookupCommon("foods/tomato");

        onion.add(RDIngredientItems.ONION.asItem());
        tomatoCrop.add(RDIngredientItems.TOMATO.asItem());
        rawSalmon.add(RDIngredientItems.SALMON.asItem());
        rawFish.add(RDIngredientItems.SALMON.asItem(), RDIngredientItems.HAGFISH.asItem(), RDIngredientItems.TUNA.asItem(), RDIngredientItems.SUPREME_TUNA.asItem());
        tomatoFood.add(RDIngredientItems.TOMATO.asItem());

        TagAppender<Item, Item> meals = valueLookupFarmerDelight("meals");
        meals.add(
                RDIngredientItems.BLACK_PORK.asItem(),
                RDIngredientItems.VENISON.asItem(),
                RDIngredientItems.WAGYU_BEEF.asItem(),
                RDIngredientItems.WILD_BOAR_MEAT.asItem()
        );

        valueLookupBuilder(FarmersDelightCommonItemTags.FOODS_TOMATO).add(RDIngredientItems.TOMATO.asItem());
        valueLookupBuilder(FarmersDelightCommonItemTags.FOODS_ONION).add(RDIngredientItems.ONION.asItem());
    }

    private TagAppender<Item, Item> valueLookupFarmerDelight(String name) {
        return valueLookupBuilder(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("farmersdelight", name)));
    }

    private TagAppender<Item, Item> valueLookupCommon(String name) {
        return valueLookupBuilder(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", name)));
    }

}
