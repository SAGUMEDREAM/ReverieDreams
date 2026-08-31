package cc.thonly.reverie_dreams.fabric.datagen.tag;

import cc.thonly.reverie_dreams.block.BlockTypeGroup;
import cc.thonly.reverie_dreams.block.bundle.CropBlockBundle;
import cc.thonly.reverie_dreams.block.bundle.WoodBundle;
import cc.thonly.reverie_dreams.data.FumoType;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.item.ItemTypeGroup;
import cc.thonly.reverie_dreams.item.base.AlbumItem;
import cc.thonly.reverie_dreams.item.base.ArmorItem;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.FumoTypes;
import cc.thonly.reverie_dreams.registry.content.block.RDKitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDCropBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDBeverageItems;
import cc.thonly.reverie_dreams.registry.content.item.RDCuisineItems;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings("deprecation")
public class ItemTagProvider extends FabricTagsProvider.ItemTagsProvider {

    public ItemTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        // 基础工具方法
        BiConsumer<TagKey<Item>, Collection<? extends Item>> addAll = (tag, items) -> valueLookupBuilder(tag).add(items.toArray(Item[]::new));
        Supplier<List<Item>> allDanmakuItemGetter = () -> {
            RegistryProvider<DanmakuType> registry = BuiltInRegistryProviders.DANMAKU_TYPE;
            Stream<Item> itemStream = registry.values().stream().map(DanmakuType::getItemHolder).map(ItemLike::asItem);
            return itemStream.toList();
        };
        Supplier<List<Item>> allToolGetter = () -> {
            List<Item> list = new ArrayList<>();
            list.add(RDItems.NUE_TRIDENT.asItem());
            list.addAll(allDanmakuItemGetter.get());
            return list;
        };
        TagAppender<Item, Item> logs = valueLookupBuilder(ItemTags.LOGS);
        TagAppender<Item, Item> strippedLogs = valueLookupBuilder(ConventionalItemTags.STRIPPED_LOGS);
        TagAppender<Item, Item> strippedWoods = valueLookupBuilder(ConventionalItemTags.STRIPPED_WOODS);
        TagAppender<Item, Item> planks = valueLookupBuilder(ItemTags.PLANKS);
        TagAppender<Item, Item> buttons = valueLookupBuilder(ItemTags.BUTTONS);
        TagAppender<Item, Item> fences = valueLookupBuilder(ItemTags.FENCES);
        TagAppender<Item, Item> fenceGates = valueLookupBuilder(ItemTags.FENCE_GATES);
        TagAppender<Item, Item> slabs = valueLookupBuilder(ItemTags.SLABS);
        TagAppender<Item, Item> doors = valueLookupBuilder(ItemTags.DOORS);
        TagAppender<Item, Item> trapdoors = valueLookupBuilder(ItemTags.TRAPDOORS);
        Consumer<WoodBundle> woodBundleFunction = (bundle) -> {
            logs.add(bundle.log().asItem());
            strippedLogs.add(bundle.strippedLog().asItem());
            strippedWoods.add(bundle.strippedWood().asItem());
            planks.add(bundle.log().asItem());
            buttons.add(bundle.button().asItem());
            fences.add(bundle.fence().asItem());
            fenceGates.add(bundle.fenceGate().asItem());
            slabs.add(bundle.slab().asItem());
            doors.add(bundle.door().asItem());
            trapdoors.add(bundle.trapdoor().asItem());
        };
        for (WoodBundle instance : WoodBundle.INSTANCES) {
            woodBundleFunction.accept(instance);
        }
        strippedLogs.add(RDWoodBlocks.BLESSED_SPIRITUAL_LOG.asItem());

        List<Item> allTool = allToolGetter.get();

        // 通用 Tag
        valueLookupBuilder(RDItemTags.EMPTY).add(Items.BEDROCK).add(Items.BARRIER);
        addAll.accept(RDItemTags.FUMO, FumoTypes.getView().stream().map(FumoType::item).toList());
        addAll.accept(ItemTags.CREEPER_DROP_MUSIC_DISCS, AlbumItem.ITEMS);

        // 工具类 Tag
        addAll.accept(ItemTags.SWORDS, ItemTypeGroup.SWORD.items());
        addAll.accept(ItemTags.PICKAXES, ItemTypeGroup.PICKAXES.items());
        addAll.accept(ItemTags.AXES, ItemTypeGroup.AXES.items());
        addAll.accept(ItemTags.SHOVELS, ItemTypeGroup.SHOVELS.items());
        addAll.accept(ItemTags.HOES, ItemTypeGroup.HOES.items());
        addAll.accept(ItemTags.SPEARS, ItemTypeGroup.SPEARS.items());
        addAll.accept(ItemTags.TRIDENT_ENCHANTABLE, allTool);
        addAll.accept(ItemTags.DURABILITY_ENCHANTABLE, allTool);
        addAll.accept(ItemTags.DURABILITY_ENCHANTABLE, List.of(RDItems.TENGU_SHIELD.asItem()));
        addAll.accept(ItemTags.DURABILITY_ENCHANTABLE, List.of(RDItems.IRON_BAR.asItem()));
        addAll.accept(ConventionalItemTags.SHIELD_TOOLS, List.of(RDItems.TENGU_SHIELD.asItem()));

        // 盔甲类 Tag
        addAll.accept(ItemTags.HEAD_ARMOR, ArmorItem.HEAD_ITEMS);
        addAll.accept(ItemTags.CHEST_ARMOR, ArmorItem.CHEST_ITEMS);
        addAll.accept(ItemTags.LEG_ARMOR, ArmorItem.LEG_ITEMS);
        addAll.accept(ItemTags.FOOT_ARMOR, ArmorItem.FEET_ITEMS);
        addAll.accept(RDItemTags.ARMOR, ArmorItem.ITEMS);

        // 工具材料
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
        valueLookupBuilder(RDItemTags.IRON_BAR_MATERIALS).add(Items.IRON_BLOCK);
        valueLookupBuilder(RDItemTags.SILVER_TOOL_MATERIALS).add(RDItems.SILVER_INGOT.asItem());
        valueLookupBuilder(RDItemTags.MAGIC_ICE_ARMOR).add(RDItems.MAGIC_ICE_HELMET.asItem(), RDItems.MAGIC_ICE_CHESTPLATE.asItem(), RDItems.MAGIC_ICE_LEGGINGS.asItem(), RDItems.MAGIC_ICE_BOOTS.asItem());
        valueLookupBuilder(RDItemTags.MAGIC_ICE_TOOL_MATERIALS).add(RDItems.ICE_SCALES.asItem());
        valueLookupBuilder(RDItemTags.MAGIC_ICE_WEAPON).add(RDItems.MAGIC_ICE_AXE.asItem()).add(RDItems.MAGIC_ICE_PICKAXE.asItem()).add(RDItems.MAGIC_ICE_HOE.asItem()).add(RDItems.MAGIC_ICE_SHOVEL.asItem()).add(RDItems.MAGIC_ICE_SWORD.asItem());
        valueLookupBuilder(RDItemTags.MAID_ARMOR).add(RDItems.MAID_HAIRBAND.asItem(), RDItems.MAID_UPPER_SKIRT.asItem(), RDItems.MAID_LOWER_SKIRT.asItem(), RDItems.MAID_SHOE.asItem());
        valueLookupBuilder(RDItemTags.DREAM_ARMOR).add(RDItems.DREAM_HELMET.asItem()).add(RDItems.DREAM_CHESTPLATE.asItem()).add(RDItems.DREAM_LEGGINGS.asItem()).add(RDItems.DREAM_BOOTS.asItem());
        valueLookupBuilder(RDItemTags.DREAM_TOOL_MATERIALS).add(RDItems.DREAM_CRYSTAL_FRAGMENT.asItem());

        // 弹幕
        TagAppender<Item, Item> danmaku = valueLookupBuilder(RDItemTags.DANMAKU_ITEM);
        for (DanmakuType danmakuType : BuiltInRegistryProviders.DANMAKU_TYPE) {
            danmaku.add(danmakuType.getItemHolder().asItem());
        }
        danmaku.add(RDItems.KNIFE.asItem());
        TagAppender<Item, Item> danmakuRepairAcceptableItems = valueLookupBuilder(RDItemTags.DANMAKU_REPAIR_ACCEPTABLE_ITEM);
        danmakuRepairAcceptableItems.add(RDItems.POWER.asItem());

        // 自定义方块
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
        valueLookupBuilder(RDItemTags.INGREDIENT).addAll(RDIngredientItems.INGREDIENTS.stream().map(ItemLike::asItem).toList());
        valueLookupBuilder(RDItemTags.CUISINE).addAll(RDCuisineItems.CUISINE_ITEMS.stream().map(ItemLike::asItem).toList());
        valueLookupBuilder(RDItemTags.FOOD).addOptionalTag(RDItemTags.CUISINE);
        valueLookupBuilder(RDItemTags.BEVERAGE).addAll(RDBeverageItems.BEVERAGE_ITEMS.stream().map(ItemLike::asItem).toList());
        valueLookupBuilder(RDItemTags.DRINK_ITEM).addOptionalTag(RDItemTags.BEVERAGE);

        valueLookupBuilder(RDItemTags.ROLE_TAME_FOOD)
                .add(Items.CAKE)
                .add(RDCuisineItems.ORDINARY_SMALL_CAKE.asItem())
                .add(RDCuisineItems.SCARLET_DEVILS_CAKE.asItem());
        valueLookupBuilder(ConventionalItemTags.ORES).add(RDItems.RAW_SILVER.asItem());
        valueLookupBuilder(RDItemTags.COMMON_SILVER_ORE).add(RDItems.RAW_SILVER.asItem());

        // 兼容物品
        valueLookupBuilder(ConventionalItemTags.FOODS).addOptionalTag(RDItemTags.CUISINE).addAll(RDCuisineItems.CUISINE_ITEMS.stream().map(ItemLike::asItem).toList());
        valueLookupBuilder(RDItemTags.PEACH).add(RDIngredientItems.PEACH.asItem());
        valueLookupBuilder(RDItemTags.REPLACEABLE_BLANK_PHOTOS).add(RDItems.EMPTY_PHOTO.asItem());

        valueLookupBuilder(RDItemTags.MUSICAL_INSTRUMENTS).add(RDItems.KEYBOARD.asItem(), RDItems.VIOLIN.asItem(), RDItems.TRUMPET.asItem());
        valueLookupBuilder(RDItemTags.COIN).add(RDItems.COPPER_COIN.asItem(), RDItems.SILVER_COIN.asItem(), RDItems.GOLD_COIN.asItem());
        valueLookupBuilder(RDItemTags.COMMON_COIN).addOptionalTag(RDItemTags.COIN);

        // 方块物品分类
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

        // 种子
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
                RDKitchenBlocks.COOKING_POT.asItem(),
                RDKitchenBlocks.MYSTIA_COOKING_POT.asItem(),
                RDKitchenBlocks.SUPER_COOKING_POT.asItem(),
                RDKitchenBlocks.EXTREME_COOKING_POT.asItem(),
                RDKitchenBlocks.NUKE_COOKING_POT.asItem()
        );
        cuttingBoard.add(
                RDKitchenBlocks.CUTTING_BOARD.asItem(),
                RDKitchenBlocks.MYSTIA_CUTTING_BOARD.asItem(),
                RDKitchenBlocks.SUPER_CUTTING_BOARD.asItem(),
                RDKitchenBlocks.EXTREME_CUTTING_BOARD.asItem(),
                RDKitchenBlocks.NUKE_CUTTING_BOARD.asItem()
        );
        fryingPan.add(
                RDKitchenBlocks.FRYING_PAN.asItem(),
                RDKitchenBlocks.MYSTIA_FRYING_PAN.asItem(),
                RDKitchenBlocks.SUPER_FRYING_PAN.asItem(),
                RDKitchenBlocks.EXTREME_FRYING_PAN.asItem(),
                RDKitchenBlocks.NUKE_FRYING_PAN.asItem()
        );
        grill.add(
                RDKitchenBlocks.GRILL.asItem(),
                RDKitchenBlocks.MYSTIA_GRILL.asItem(),
                RDKitchenBlocks.SUPER_GRILL.asItem(),
                RDKitchenBlocks.EXTREME_GRILL.asItem(),
                RDKitchenBlocks.NUKE_GRILL.asItem()
        );
        steamer.add(
                RDKitchenBlocks.STEAMER.asItem(),
                RDKitchenBlocks.MYSTIA_STEAMER.asItem(),
                RDKitchenBlocks.SUPER_STEAMER.asItem(),
                RDKitchenBlocks.EXTREME_STEAMER.asItem(),
                RDKitchenBlocks.NUKE_STEAMER.asItem()
        );
        kitchenware.addOptionalTag(RDItemTags.COOKING_TOP);
        kitchenware.addOptionalTag(RDItemTags.CUTTING_BOARD);
        kitchenware.addOptionalTag(RDItemTags.FRYING_PAN);
        kitchenware.addOptionalTag(RDItemTags.GRILL);
        kitchenware.addOptionalTag(RDItemTags.STEAMER);

        valueLookupBuilder(ItemTags.RAILS)
                .add(RDBlocks.RAIL_CONTROLLER_BLOCK.asItem())
                .add(RDBlocks.SIGNAL_RAIL_BLOCK.asItem());

        TagAppender<Item, Item> iceMakingMachineOutput = valueLookupBuilder(RDItemTags.ICE_MAKING_MACHINE_OUTPUT);
        iceMakingMachineOutput.add(Items.ICE);
        iceMakingMachineOutput.add(Items.SNOWBALL);
        iceMakingMachineOutput.add(RDBlocks.MAGIC_ICE_BLOCK.asItem());

        // 模组兼容扩展
        this.configureDLCFoodItem(wrapperLookup);
        this.configureDLCBeverageItem(wrapperLookup);
        this.configureDLCIngredientItem(wrapperLookup);
        this.configureCompat(wrapperLookup);
    }

    protected void configureDLCFoodItem(HolderLookup.Provider wrapperLookup) {
        TagAppender<Item, Item> dlc0 = valueLookupBuilder(RDItemTags.DLC0);
        TagAppender<Item, Item> dlc1 = valueLookupBuilder(RDItemTags.DLC1);
        TagAppender<Item, Item> dlc2 = valueLookupBuilder(RDItemTags.DLC2);
        TagAppender<Item, Item> dlc3 = valueLookupBuilder(RDItemTags.DLC3);
        TagAppender<Item, Item> dlc4 = valueLookupBuilder(RDItemTags.DLC4);
        TagAppender<Item, Item> dlc5 = valueLookupBuilder(RDItemTags.DLC5);
        TagAppender<Item, Item> dlc6 = valueLookupBuilder(RDItemTags.DLC6);

        // DLC0
        dlc0.add(RDCuisineItems.SEAFOOD_MISO_SOUP.asItem());
        dlc0.add(RDCuisineItems.TOFU_MISO.asItem());
        dlc0.add(RDCuisineItems.STRENGTH_SOUP.asItem());
        dlc0.add(RDCuisineItems.PORK_AND_TROUT_SMOKED.asItem());
        dlc0.add(RDCuisineItems.GRILLED_HAGFISH.asItem());
        dlc0.add(RDCuisineItems.ENERGY_STRING.asItem());
        dlc0.add(RDCuisineItems.TWO_HEAVENS_ONE_STYLE.asItem());
        dlc0.add(RDCuisineItems.RICE_BALL.asItem());
        dlc0.add(RDCuisineItems.GRILLED_PORK_RICE_BALLS.asItem());
        dlc0.add(RDCuisineItems.WARM_RICE_BALL.asItem());
        dlc0.add(RDCuisineItems.FAILING_SAKURA_SNOW.asItem());
        dlc0.add(RDCuisineItems.FRIED_PORK_SHREDS.asItem());
        dlc0.add(RDCuisineItems.COLD_TOFU.asItem());
        dlc0.add(RDCuisineItems.BRAISED_EEL.asItem());
        dlc0.add(RDCuisineItems.POTATO_CROQUETTES.asItem());
        dlc0.add(RDCuisineItems.GAME_SOUP.asItem());
        dlc0.add(RDCuisineItems.PORK_RICE.asItem());
        dlc0.add(RDCuisineItems.BEEF_RICE.asItem());
        dlc0.add(RDCuisineItems.FRIED_HAGFISH.asItem());
        dlc0.add(RDCuisineItems.VEGETABLE_SPECIAL.asItem());
        dlc0.add(RDCuisineItems.SNOW_WHITE.asItem());
        dlc0.add(RDCuisineItems.TOFU_POT.asItem());
        dlc0.add(RDCuisineItems.ZHAJI.asItem());
        dlc0.add(RDCuisineItems.SASHIMI_PLATTER.asItem());
        dlc0.add(RDCuisineItems.GRAND_BANQUET.asItem());
        dlc0.add(RDCuisineItems.TONKOTSU_RAMEN.asItem());
        dlc0.add(RDCuisineItems.MAGMA.asItem());
        dlc0.add(RDCuisineItems.DEEP_FRIED_CICADA_SHELLS.asItem());
        dlc0.add(RDCuisineItems.DEW_BOILED_EGGS.asItem());
        dlc0.add(RDCuisineItems.UDUMBARA_CAKE.asItem());
        dlc0.add(RDCuisineItems.BEAR_PAW.asItem());
        dlc0.add(RDCuisineItems.SECRET_DRIED_FISH.asItem());
        dlc0.add(RDCuisineItems.COLD_DISH_CARVING.asItem());
        dlc0.add(RDCuisineItems.PEACH_BLOSSOM_SOUP.asItem());
        dlc0.add(RDCuisineItems.ARCTIC_SWEET_SHRIMP_AND_PEACH_SALAD.asItem());
        dlc0.add(RDCuisineItems.FRIED_TOFU.asItem());
        dlc0.add(RDCuisineItems.POETRY_AND_GINKGO.asItem());
        dlc0.add(RDCuisineItems.REAL_SEAFOOD_MISO_SOUP.asItem());
        dlc0.add(RDCuisineItems.ROASTED_MUSHROOMS.asItem());
        dlc0.add(RDCuisineItems.COOKING_TOFU.asItem());
        dlc0.add(RDCuisineItems.FRIED_PORK_CUTLET.asItem());
        dlc0.add(RDCuisineItems.BUTTER_STEAK.asItem());
        dlc0.add(RDCuisineItems.RISOTTO.asItem());
        dlc0.add(RDCuisineItems.BEEF_WELLINGTON.asItem());
        dlc0.add(RDCuisineItems.EGGS_BENEDICT.asItem());
        dlc0.add(RDCuisineItems.HOT_WAFFLES.asItem());
        dlc0.add(RDCuisineItems.SCONES.asItem());
        dlc0.add(RDCuisineItems.PAN_FRIED_SALMON.asItem());
        dlc0.add(RDCuisineItems.CREAM_STEW.asItem());
        dlc0.add(RDCuisineItems.HONEY_BBQ_PORK.asItem());
        dlc0.add(RDCuisineItems.GINKGO_AND_RADISH_PORK_RIB_SOUP.asItem());
        dlc0.add(RDCuisineItems.TAKETORIHIME.asItem());
        dlc0.add(RDCuisineItems.PHOENIX.asItem());
        dlc0.add(RDCuisineItems.MOONLIGHT_DUMPLINGS.asItem());
        dlc0.add(RDCuisineItems.MOCHI.asItem());
        dlc0.add(RDCuisineItems.WHITE_PEACH_EIGHT_BRIDGE.asItem());
        dlc0.add(RDCuisineItems.MOON_LOVERS.asItem());
        dlc0.add(RDCuisineItems.PIG_DEER_BUTTERFLY.asItem());
        dlc0.add(RDCuisineItems.FLOWING_WATER_NOODLES.asItem());
        dlc0.add(RDCuisineItems.BAMBOO_SHOOTS_FRIED_MEAT.asItem());
        dlc0.add(RDCuisineItems.BAMBOO_STEAMED_EGG.asItem());
        dlc0.add(RDCuisineItems.HORAI_DAMA_NO_EDA.asItem());
        dlc0.add(RDCuisineItems.STINKY_TOFU.asItem());
        dlc0.add(RDCuisineItems.COLORFUL_JADE_FRIED_BUNS.asItem());
        dlc0.add(RDCuisineItems.MAPO_TOFU.asItem());
        dlc0.add(RDCuisineItems.BOILED_FISH.asItem());
        dlc0.add(RDCuisineItems.MOON_CAKE.asItem());
        dlc0.add(RDCuisineItems.MAOYU_TRICOLOR_ICE_CREAM.asItem());
        dlc0.add(RDCuisineItems.MAOYU_LAVA_TOFU.asItem());
        dlc0.add(RDCuisineItems.SCARLET_DEVILS_CAKE.asItem());
        dlc0.add(RDCuisineItems.UNCONSCIOUS_MONSTER_MOUSSE.asItem());
        dlc0.add(RDCuisineItems.DUMPLING.asItem());
        dlc0.add(RDCuisineItems.GLUTINOUS_RICE_BALLS.asItem());

        // DLC1
        dlc1.add(RDCuisineItems.FRIED_SHRIMP_TEMPURA.asItem());
        dlc1.add(RDCuisineItems.GOLDEN_CRISPY_FISH_CAKE.asItem());
        dlc1.add(RDCuisineItems.ALL_MEAT_FEAST.asItem());
        dlc1.add(RDCuisineItems.PICKLED_CUCUMBERS.asItem());
        dlc1.add(RDCuisineItems.BAKED_CRAB_WITH_CREAM.asItem());
        dlc1.add(RDCuisineItems.PSEUDO_JIRITAMA.asItem());
        dlc1.add(RDCuisineItems.OKONOMIYAKI.asItem());
        dlc1.add(RDCuisineItems.TAKOYAKI.asItem());
        dlc1.add(RDCuisineItems.SEA_URCHIN_SASHIMI.asItem());
        dlc1.add(RDCuisineItems.MUSHROOM_MEAT_SLICES.asItem());
        dlc1.add(RDCuisineItems.SECRET_MUSHROOM_CASSEROLE.asItem());
        dlc1.add(RDCuisineItems.MUSHROOM_GIRLS_DANCE_STEW.asItem());
        dlc1.add(RDCuisineItems.MILKY_MUSHROOM_SOUP.asItem());
        dlc1.add(RDCuisineItems.ORDINARY_SMALL_CAKE.asItem());
        dlc1.add(RDCuisineItems.SEVEN_COLORED_YOKAN.asItem());
        dlc1.add(RDCuisineItems.NIGIRI_SUSHI.asItem());
        dlc1.add(RDCuisineItems.PUMPKIN_SHRIMP_CAKE.asItem());
        dlc1.add(RDCuisineItems.GENSOKYO_BUDDHA_JUMPS_OVER_THE_WALL.asItem());

        // DLC2
        dlc2.add(RDCuisineItems.DEPRESSED_CHEESE_STICKS.asItem());
        dlc2.add(RDCuisineItems.GLOOMY_FRUIT_PIE.asItem());
        dlc2.add(RDCuisineItems.SCREAMING_ODEN.asItem());
        dlc2.add(RDCuisineItems.CRISP_CYCLONE.asItem());
        dlc2.add(RDCuisineItems.LOOKING_UP_AT_THE_CEILING_FRUIT_PIE.asItem());
        dlc2.add(RDCuisineItems.BEETLE_STEAMED_CAKE.asItem());
        dlc2.add(RDCuisineItems.LION_HEAD.asItem());
        dlc2.add(RDCuisineItems.GIANT_TAMAGOYAKI.asItem());
        dlc2.add(RDCuisineItems.OEDO_BOAT_FESTIVAL.asItem());
        dlc2.add(RDCuisineItems.SAKURA_PUDDING.asItem());
        dlc2.add(RDCuisineItems.REFRESHING_PUDDING.asItem());
        dlc2.add(RDCuisineItems.BURNT_PUDDING.asItem());
        dlc2.add(RDCuisineItems.CAT_FOOD.asItem());
        dlc2.add(RDCuisineItems.SALMON_TEMPURA.asItem());
        dlc2.add(RDCuisineItems.FISH_LEAPS_OVER_DRAGON_GATE.asItem());
        dlc2.add(RDCuisineItems.CHEESE_EGG.asItem());
        dlc2.add(RDCuisineItems.ONE_HIT_KILL.asItem());
        dlc2.add(RDCuisineItems.HELL_THRILL_WARNING.asItem());

        // DLC3
        dlc3.add(RDCuisineItems.BAKED_SWEET_POTATOES.asItem());
        dlc3.add(RDCuisineItems.SKINNY_HORSE_DUMPLING.asItem());
        dlc3.add(RDCuisineItems.FRIGHT_ADVENTURE.asItem());
        dlc3.add(RDCuisineItems.BISCAY_BISCUITS.asItem());
        dlc3.add(RDCuisineItems.PIRATE_BACON.asItem());
        dlc3.add(RDCuisineItems.LUOHAN_VEGETARIAN.asItem());
        dlc3.add(RDCuisineItems.YUNSHAN_COTTON_CANDY.asItem());
        dlc3.add(RDCuisineItems.HOLY_WHITE_LOTUS_SEED_CAKE.asItem());
        dlc3.add(RDCuisineItems.GENSOKYO_STAR_LOTUS_SHIP.asItem());
        dlc3.add(RDCuisineItems.PINE_NUT_CAKE.asItem());
        dlc3.add(RDCuisineItems.SHIRAGA_SADAMATSU.asItem());
        dlc3.add(RDCuisineItems.TAICHI_BAGUA_FISH_MAW.asItem());
        dlc3.add(RDCuisineItems.CANDIED_CHESTNUTS.asItem());
        dlc3.add(RDCuisineItems.TIANSHI_BRAISED_CHESTNUT_MUSHROOMS.asItem());
        dlc3.add(RDCuisineItems.LOTUS_FISH_RICE_BOWL.asItem());
        dlc3.add(RDCuisineItems.CANDIED_SWEET_POTATO.asItem());
        dlc3.add(RDCuisineItems.PAN_FRIED_MUSHROOM_MEAT_ROLL.asItem());
        dlc3.add(RDCuisineItems.ASSORTED_TEMPURA.asItem());

        // DLC4
        dlc4.add(RDCuisineItems.FRIED_TOMATO_STRIPS.asItem());
        dlc4.add(RDCuisineItems.BRAISED_PORK_WITH_PEACH.asItem());
        dlc4.add(RDCuisineItems.REVERSING_THE_WORLD.asItem());
        dlc4.add(RDCuisineItems.RED_BEAN_DAIFUKU.asItem());
        dlc4.add(RDCuisineItems.DORAYAKI.asItem());
        dlc4.add(RDCuisineItems.THE_BEAUTY_OF_HAN_PALACE.asItem());
        dlc4.add(RDCuisineItems.BAMBOO_SHOOTS_STEWED_IN_STONE_POT.asItem());
        dlc4.add(RDCuisineItems.BAMBOO_TUBE_STEAMED_PORK.asItem());
        dlc4.add(RDCuisineItems.GREEN_BAMBOO_WELCOMES_SPRING.asItem());
        dlc4.add(RDCuisineItems.PLUM_TEA_RICE.asItem());
        dlc4.add(RDCuisineItems.STEAMED_EGG_WITH_SEA_URCHIN.asItem());
        dlc4.add(RDCuisineItems.FANTASY_IS_ALL_THE_RAGE.asItem());
        dlc4.add(RDCuisineItems.GREEN_FAIRY_MUSHROOM.asItem());
        dlc4.add(RDCuisineItems.FLOWERS_BIRDS_WIND_AND_MOON.asItem());
        dlc4.add(RDCuisineItems.THE_DREAM.asItem());
        dlc4.add(RDCuisineItems.TOON_PANCAKES.asItem());
        dlc4.add(RDCuisineItems.POISONOUS_GARDEN.asItem());
        dlc4.add(RDCuisineItems.A_LITTLE_SWEET_POISON.asItem());

        // DLC5
        dlc5.add(RDCuisineItems.EEL_EGG_DONBURI.asItem());
        dlc5.add(RDCuisineItems.BAMBOO_TUBE_ROASTED_DRUNKEN_SHRIMP.asItem());
        dlc5.add(RDCuisineItems.BEEF_HOT_POT.asItem());
        dlc5.add(RDCuisineItems.CAT_KULULI.asItem());
        dlc5.add(RDCuisineItems.CAT_PIZZA.asItem());
        dlc5.add(RDCuisineItems.CATS_PLAYING_IN_WATER.asItem());
        dlc5.add(RDCuisineItems.RAPUNZEL.asItem());
        dlc5.add(RDCuisineItems.SEA_URCHIN_SHINGEN_PANCAKE.asItem());
        dlc5.add(RDCuisineItems.MAD_HATTER_TEA_PARTY.asItem());
        dlc5.add(RDCuisineItems.PEACH_BLOSSOM_GLAZE_ROLL.asItem());
        dlc5.add(RDCuisineItems.MOONLIGHT_OVER_LOTUS_POND.asItem());
        dlc5.add(RDCuisineItems.LONGYIN_PEACH.asItem());
        dlc5.add(RDCuisineItems.MOLECULAR_EGG.asItem());
        dlc5.add(RDCuisineItems.THE_SOURCE_OF_LIFE.asItem());
        dlc5.add(RDCuisineItems.THE_MARS.asItem());
        dlc5.add(RDCuisineItems.HEART_PORRIDGE_GRUEL.asItem());
        dlc5.add(RDCuisineItems.HULA_SOUP.asItem());
        dlc5.add(RDCuisineItems.SUPERME_SEAFOOD_NOODLES.asItem());

        // DLC6
    }

    protected void configureDLCBeverageItem(HolderLookup.Provider wrapperLookup) {
        TagAppender<Item, Item> dlc0 = valueLookupBuilder(RDItemTags.DLC0);
        TagAppender<Item, Item> dlc1 = valueLookupBuilder(RDItemTags.DLC1);
        TagAppender<Item, Item> dlc2 = valueLookupBuilder(RDItemTags.DLC2);
        TagAppender<Item, Item> dlc3 = valueLookupBuilder(RDItemTags.DLC3);
        TagAppender<Item, Item> dlc4 = valueLookupBuilder(RDItemTags.DLC4);
        TagAppender<Item, Item> dlc5 = valueLookupBuilder(RDItemTags.DLC5);
        TagAppender<Item, Item> dlc6 = valueLookupBuilder(RDItemTags.DLC6);

        // DLC0
        dlc0.add(RDBeverageItems.GREEN_TEA.asItem());
        dlc0.add(RDBeverageItems.FRUITY_HIGH_BALL.asItem());
        dlc0.add(RDBeverageItems.FRUITY_SOUR.asItem());
        dlc0.add(RDBeverageItems.QI.asItem());
        dlc0.add(RDBeverageItems.BEER.asItem());
        dlc0.add(RDBeverageItems.SUN_MOON_STAR.asItem());
        dlc0.add(RDBeverageItems.PLUM_WINE.asItem());
        dlc0.add(RDBeverageItems.TENGU_DANCE.asItem());
        dlc0.add(RDBeverageItems.SCARLET_DEVIL.asItem());
        dlc0.add(RDBeverageItems.GODS_WHEAT.asItem());
        dlc0.add(RDBeverageItems.OTTER_FESTIVAL.asItem());
        dlc0.add(RDBeverageItems.DAWN.asItem());
        dlc0.add(RDBeverageItems.SPARROW_SAKE.asItem());
        dlc0.add(RDBeverageItems.SCARLET_DEVIL_MANSION_BLACK_TEA.asItem());
        dlc0.add(RDBeverageItems.AFFGADO.asItem());
        dlc0.add(RDBeverageItems.RED_MIST.asItem());
        dlc0.add(RDBeverageItems.NEGRONI.asItem());
        dlc0.add(RDBeverageItems.GODFATHER.asItem());
        dlc0.add(RDBeverageItems.BLESSING_WIND.asItem());
        dlc0.add(RDBeverageItems.WINTER_BREW.asItem());
        dlc0.add(RDBeverageItems.FOURTEENTH_NIGHT.asItem());
        dlc0.add(RDBeverageItems.FIRE_RAT_FUR.asItem());
        dlc0.add(RDBeverageItems.GYOKURO_TEA.asItem());
        dlc0.add(RDBeverageItems.MOON_ROCKET.asItem());
        dlc0.add(RDBeverageItems.MILK.asItem());
        dlc0.add(RDBeverageItems.RED_GRAPEFRUIT_JUICE.asItem());
        dlc0.add(RDBeverageItems.SODA.asItem());
        dlc0.add(RDBeverageItems.ICEBERG_MAPLE_FROZEN_LEMON.asItem());
        dlc0.add(RDBeverageItems.BIG_POPSICLE.asItem());

        // DLC1
        dlc1.add(RDBeverageItems.DAIGINJO.asItem());
        dlc1.add(RDBeverageItems.COFFEE.asItem());
        dlc1.add(RDBeverageItems.FAIRY_RAIN.asItem());
        dlc1.add(RDBeverageItems.PALEO_CREAMY_SMOOTHIE.asItem());
        dlc1.add(RDBeverageItems.ORDINARY_FITNESS_TEA.asItem());

        // DLC2
        dlc2.add(RDBeverageItems.DEMON_SLAYER.asItem());
        dlc2.add(RDBeverageItems.QI_HEALTH.asItem());
        dlc2.add(RDBeverageItems.KOMEIJI_ICE_CREAM.asItem());

        // DLC3
        dlc3.add(RDBeverageItems.MANGO_POMELO_SAGO.asItem());
        dlc3.add(RDBeverageItems.QILIN.asItem());

        // DLC4
        dlc4.add(RDBeverageItems.HEAVEN_AND_EARTH_ARE_USELESS.asItem());
        dlc4.add(RDBeverageItems.DRUNK_ACTOR.asItem());

        // DLC5
        dlc5.add(RDBeverageItems.DAUGHTER_OF_THE_SEA.asItem());
        dlc5.add(RDBeverageItems.DEMONIC_COFFEE.asItem());
        dlc5.add(RDBeverageItems.MOJITO_BURST_BALL.asItem());
        dlc5.add(RDBeverageItems.SPACE_BEER.asItem());
        dlc5.add(RDBeverageItems.SATELLITE_ICED_COFFEE.asItem());

        // DLC6
    }

    protected void configureDLCIngredientItem(HolderLookup.Provider wrapperLookup) {
        TagAppender<Item, Item> dlc0 = valueLookupBuilder(RDItemTags.DLC0);
        TagAppender<Item, Item> dlc1 = valueLookupBuilder(RDItemTags.DLC1);
        TagAppender<Item, Item> dlc2 = valueLookupBuilder(RDItemTags.DLC2);
        TagAppender<Item, Item> dlc3 = valueLookupBuilder(RDItemTags.DLC3);
        TagAppender<Item, Item> dlc4 = valueLookupBuilder(RDItemTags.DLC4);
        TagAppender<Item, Item> dlc5 = valueLookupBuilder(RDItemTags.DLC5);
        TagAppender<Item, Item> dlc6 = valueLookupBuilder(RDItemTags.DLC6);

        // DLC0
        dlc0.add(RDIngredientItems.EGG.asItem());
        dlc0.add(RDIngredientItems.BLUE_EGG.asItem());
        dlc0.add(RDIngredientItems.BROWN_EGG.asItem());
        dlc0.add(RDIngredientItems.PORKCHOP.asItem());
        dlc0.add(RDIngredientItems.BEEF.asItem());
        dlc0.add(RDIngredientItems.VENISON.asItem());
        dlc0.add(RDIngredientItems.WILD_BOAR_MEAT.asItem());
        dlc0.add(RDIngredientItems.TOFU.asItem());
        dlc0.add(RDIngredientItems.POTATO.asItem());
        dlc0.add(RDIngredientItems.ONION.asItem());
        dlc0.add(RDIngredientItems.PUMPKIN.asItem());
        dlc0.add(RDIngredientItems.WHITE_RADISH.asItem());
        dlc0.add(RDIngredientItems.KELP.asItem());
        dlc0.add(RDIngredientItems.TROUT.asItem());
        dlc0.add(RDIngredientItems.HAGFISH.asItem());
        dlc0.add(RDIngredientItems.SALMON.asItem());
        dlc0.add(RDIngredientItems.TUNA.asItem());
        dlc0.add(RDIngredientItems.BLACK_PORK.asItem());
        dlc0.add(RDIngredientItems.WAGYU_BEEF.asItem());
        dlc0.add(RDIngredientItems.BROWN_MUSHROOM.asItem());
        dlc0.add(RDIngredientItems.RED_MUSHROOM.asItem());
        dlc0.add(RDIngredientItems.TRUFFLE.asItem());
        dlc0.add(RDIngredientItems.SUPREME_TUNA.asItem());
        dlc0.add(RDIngredientItems.PUFFERFISH.asItem());
        dlc0.add(RDIngredientItems.PEACH.asItem());
        dlc0.add(RDIngredientItems.GINKGO.asItem());
        dlc0.add(RDIngredientItems.SHRIMP.asItem());
        dlc0.add(RDIngredientItems.HONEY_BOTTLE.asItem());
        dlc0.add(RDIngredientItems.CICADA_SHELL.asItem());
        dlc0.add(RDIngredientItems.UDUMBARA.asItem());
        dlc0.add(RDIngredientItems.DEW.asItem());
        dlc0.add(RDIngredientItems.BAMBOO_SHOOTS.asItem());
        dlc0.add(RDIngredientItems.BUTTER.asItem());
        dlc0.add(RDIngredientItems.FLOUR.asItem());
        dlc0.add(RDIngredientItems.BAMBOO.asItem());
        dlc0.add(RDIngredientItems.STICKY_RICE.asItem());
        dlc0.add(RDIngredientItems.MOONFLOWER.asItem());
        dlc0.add(RDIngredientItems.MAGIC_ICE_BLOCK.asItem());
        dlc0.add(RDIngredientItems.CHILI.asItem());
        dlc0.add(RDIngredientItems.GRAPE.asItem());

        // DLC1
        dlc1.add(RDIngredientItems.CUCUMBER.asItem());
        dlc1.add(RDIngredientItems.OCTOPUS.asItem());
        dlc1.add(RDIngredientItems.SEA_URCHIN.asItem());
        dlc1.add(RDIngredientItems.BLACK_SALT.asItem());
        dlc1.add(RDIngredientItems.CREAM.asItem());
        dlc1.add(RDIngredientItems.CRAB.asItem());

        // DLC2
        dlc2.add(RDIngredientItems.TWIN_LOTUS.asItem());
        dlc2.add(RDIngredientItems.LEMON.asItem());
        dlc2.add(RDIngredientItems.CHEESE.asItem());

        // DLC3
        dlc3.add(RDIngredientItems.LOTUS_NUTS.asItem());
        dlc3.add(RDIngredientItems.SWEET_POTATO.asItem());
        dlc3.add(RDIngredientItems.PINE_NUT.asItem());
        dlc3.add(RDIngredientItems.CHESTNUT.asItem());

        // DLC4
        dlc4.add(RDIngredientItems.PLUM.asItem());
        dlc4.add(RDIngredientItems.RED_BEANS.asItem());
        dlc4.add(RDIngredientItems.FLOWERS.asItem());
        dlc4.add(RDIngredientItems.TOON.asItem());
        dlc4.add(RDIngredientItems.TOMATO.asItem());

        // DLC5
        dlc5.add(RDIngredientItems.COCOA_BEANS.asItem());
        dlc5.add(RDIngredientItems.BROCCOLI.asItem());
        dlc5.add(RDIngredientItems.PUFF_YO_FRUIT.asItem());
        dlc5.add(RDIngredientItems.FICUS_MICROCARPA.asItem());
        dlc5.add(RDIngredientItems.TREMELLA.asItem());
        dlc5.add(RDIngredientItems.CAPSAICIN.asItem());

        // DLC6
    }


    protected void configureCompat(HolderLookup.Provider wrapperLookup) {
        TagAppender<Item, Item> onion = valueLookupCommon("crops/onion");
        TagAppender<Item, Item> tomatoCrop = valueLookupCommon("crops/tomato");
        TagAppender<Item, Item> cabbage = valueLookupCommon("crops/cabbage");
        TagAppender<Item, Item> chillPepper = valueLookupCommon("crops/chillpepper");
        TagAppender<Item, Item> rawSalmon = valueLookupCommon("foods/raw_salmon");
        TagAppender<Item, Item> rawFish = valueLookupCommon("foods/raw_fish");
        TagAppender<Item, Item> rawBeef = valueLookupCommon("foods/raw_beef");
        TagAppender<Item, Item> rawPork = valueLookupCommon("foods/raw_pork");
        TagAppender<Item, Item> tomatoFood = valueLookupCommon("foods/tomato");
        TagAppender<Item, Item> vegetable = valueLookupCommon("foods/vegetable");
        TagAppender<Item, Item> soup = valueLookupCommon("foods/soup");
        TagAppender<Item, Item> fruit = valueLookupCommon("foods/fruit");
        TagAppender<Item, Item> tofu = valueLookupCommon("foods/tofu");
        TagAppender<Item, Item> chiliPepperSeed = valueLookupCommon("seeds/chilipepper");
        TagAppender<Item, Item> tomatoSeed = valueLookupCommon("seeds/tomato");

        onion.add(RDIngredientItems.ONION.asItem());
        tomatoCrop.add(RDIngredientItems.TOMATO.asItem());
        chillPepper.add(RDIngredientItems.CHILI.asItem());
        rawSalmon.add(RDIngredientItems.SALMON.asItem());
        rawBeef.add(RDIngredientItems.WAGYU_BEEF.asItem());
        rawFish.add(RDIngredientItems.SALMON.asItem(), RDIngredientItems.HAGFISH.asItem(), RDIngredientItems.TUNA.asItem(), RDIngredientItems.SUPREME_TUNA.asItem());
        tomatoFood.add(RDIngredientItems.TOMATO.asItem());
        rawPork.add(RDIngredientItems.BLACK_PORK.asItem())
               .add(RDIngredientItems.WILD_BOAR_MEAT.asItem());

        soup.add(RDCuisineItems.GAME_SOUP.asItem())
            .add(RDCuisineItems.HULA_SOUP.asItem())
            .add(RDCuisineItems.MILKY_MUSHROOM_SOUP.asItem())
            .add(RDCuisineItems.PEACH_BLOSSOM_SOUP.asItem())
            .add(RDCuisineItems.REAL_SEAFOOD_MISO_SOUP.asItem())
            .add(RDCuisineItems.SEAFOOD_MISO_SOUP.asItem())
            .add(RDCuisineItems.STRENGTH_SOUP.asItem())
            .add(RDCuisineItems.GINKGO_AND_RADISH_PORK_RIB_SOUP.asItem());
        fruit.add(RDIngredientItems.FICUS_MICROCARPA.asItem())
             .add(RDIngredientItems.GRAPE.asItem())
             .add(RDIngredientItems.PLUM.asItem())
             .add(RDIngredientItems.PUFF_YO_FRUIT.asItem())
             .add(RDIngredientItems.PEACH.asItem())
             .add(RDIngredientItems.LEMON.asItem());
        tofu.add(RDIngredientItems.TOFU.asItem());
        chiliPepperSeed.add(RDCropBlocks.CHILL.getSeed().asItem());
        tomatoSeed.add(RDCropBlocks.TOMATO.getSeed().asItem());
        FoodProperties.PROPERTY_CACHE.forEach((property, items) -> {
            if (property.is(FoodProperties.VEGETARIAN)) {
                for (Item item : items) {
                    vegetable.addOptional(item);
                }
            }
        });

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
