package cc.thonly.reverie_dreams.datagen;

import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.block.WoodCreator;
import cc.thonly.reverie_dreams.item.ModItems;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.data.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.resource.featuretoggle.FeatureSet;

import java.util.*;

public class ModRecipeGenerator extends RecipeGenerator {
    public static ImmutableList<ItemConvertible> SILVER = ImmutableList.of(ModBlocks.SILVER_ORE.asItem(), ModBlocks.DEEPSLATE_SILVER_ORE.asItem(), ModItems.RAW_SILVER);

    protected ModRecipeGenerator(RegistryWrapper.WrapperLookup registries, RecipeExporter exporter) {
        super(registries, exporter);
    }

    @Override
    public void generate() {

        // 入门书
        createShaped(RecipeCategory.MISC, ModItems.TOUHOU_HELPER)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .input('X', ModItems.POWER)
                .input('#', Items.BOOK)
                .criterion("has_diamond", conditionsFromItem(Items.DIAMOND))
                .offerTo(exporter, getRecipeName(ModItems.TOUHOU_HELPER));

        // Point / 块
        offerIngotToBlockRecipe(exporter, ModItems.POINT, ModBlocks.POINT_BLOCK.asItem());
        offerBlockToIngotRecipe(exporter, ModBlocks.POINT_BLOCK.asItem(), ModItems.POINT);

        // Power / 块
        offerIngotToBlockRecipe(exporter, ModItems.POWER, ModBlocks.POWER_BLOCK.asItem());
        offerBlockToIngotRecipe(exporter, ModBlocks.POWER_BLOCK.asItem(), ModItems.POWER);

        // 速度羽毛
        createShaped(RecipeCategory.DECORATIONS, ModItems.SPEED_FEATHER)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .input('X', Items.FEATHER)
                .input('#', Items.DIAMOND)
                .criterion("has_diamond", conditionsFromItem(Items.DIAMOND))
                .offerTo(exporter, getRecipeName(ModItems.SPEED_FEATHER));

        // 梦境枕头
        createShaped(RecipeCategory.DECORATIONS, ModItems.DREAM_PILLOW)
                .pattern("XXX")
                .pattern("Y#Y")
                .input('X', Items.PINK_WOOL)
                .input('Y', Items.GOLD_INGOT)
                .input('#', Items.EMERALD)
                .criterion("has_emerald", conditionsFromItem(Items.EMERALD))
                .offerTo(exporter, getRecipeName(ModItems.DREAM_PILLOW));

        // Bomb
        offerIngotToBlockRecipe(exporter, ModItems.UPGRADED_HEALTH_FRAGMENT, ModItems.UPGRADED_HEALTH);
        createShaped(RecipeCategory.MISC, ModItems.UPGRADED_HEALTH, 2)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .input('X', ModBlocks.POWER_BLOCK)
                .input('#', ModItems.UPGRADED_HEALTH_FRAGMENT)
                .criterion("has_health_fragment", conditionsFromItem(ModItems.UPGRADED_HEALTH_FRAGMENT))
                .offerTo(exporter, getRecipeName(ModItems.UPGRADED_HEALTH_FRAGMENT) + "_copy");

        // 残机
        offerIngotToBlockRecipe(exporter, ModItems.BOMB_FRAGMENT, ModItems.BOMB);
        createShaped(RecipeCategory.MISC, ModItems.BOMB_FRAGMENT, 2)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .input('X', ModBlocks.POINT_BLOCK)
                .input('#', ModItems.BOMB_FRAGMENT)
                .criterion("has_bomb_fragment", conditionsFromItem(ModItems.BOMB_FRAGMENT))
                .offerTo(exporter, getRecipeName(ModItems.BOMB_FRAGMENT) + "_copy");

        // 空白角色卡
        createShaped(RecipeCategory.MISC, ModItems.ROLE_CARD)
                .pattern(" R ")
                .pattern("R#R")
                .pattern(" R ")
                .input('R', Items.REDSTONE)
                .input('#', Items.PAPER)
                .criterion("has_paper", conditionsFromItem(Items.PAPER))
                .offerTo(exporter, getRecipeName(ModItems.ROLE_CARD));

        // 魔理沙的帽子
        createShaped(RecipeCategory.DECORATIONS, ModBlocks.MARISA_HAT_BLOCK)
                .pattern(" X ")
                .pattern("X#X")
                .pattern("XYX")
                .input('X', Items.WHITE_WOOL)
                .input('Y', Items.LEATHER)
                .input('#', Items.BLACK_WOOL)
                .criterion("has_diamond", conditionsFromItem(Items.DIAMOND))
                .offerTo(exporter, getRecipeName(ModBlocks.MARISA_HAT_BLOCK));

        // Fumo销售许可
        createShaped(RecipeCategory.MISC, ModItems.FUMO_LICENSE)
                .pattern("YXY")
                .pattern("X#X")
                .pattern("YXY")
                .input('X', Items.WHITE_WOOL)
                .input('Y', Items.DIAMOND)
                .input('#', Items.PAPER)
                .criterion("has_wool", conditionsFromItem(Items.WHITE_WOOL))
                .offerTo(exporter, getRecipeName(ModItems.FUMO_LICENSE));

        // 烟火之星
        createShaped(RecipeCategory.MISC, Items.FIREWORK_STAR)
                .pattern(" # ")
                .pattern("#X#")
                .pattern(" # ")
                .input('#', Items.GUNPOWDER)
                .input('X', Items.SAND)
                .criterion("has_sand", conditionsFromItem(Items.GUNPOWDER))
                .offerTo(exporter, "rd_provided_" + getRecipeName(Items.FIREWORK_STAR));

        this.generateWoodCreator(ModBlocks.SPIRITUAL);
        this.generateWoodCreator(MIBlocks.LEMON);
        this.generateWoodCreator(MIBlocks.GINKGO);
        this.generateDecorativeBlock();

        this.generateWorkBlock();
        this.generateOrb();
        this.generateSilver();
        this.generateMaid();
        this.generateMagicIce();
        this.generateMusicBlock();
        this.generateIngredient();
        this.generateMIPlant2Ingredient();
        this.generateMICookRecipe();
    }

    private void generateWoodCreator(WoodCreator creator) {
        RegistryEntryLookup<Item> itemImpl = this.registries.getOrThrow(RegistryKeys.ITEM);
        Block log = creator.log();
        Block wood = creator.wood();
        Block strippedLog = creator.strippedLog();
        Block strippedWood = creator.strippedWood();
        Block planks = creator.planks();
        Block stair = creator.stair();
        Block slab = creator.slab();
        Block door = creator.door();
//        Block trapdoor = creator.trapdoor();
        Block fence = creator.fence();
        Block fenceGate = creator.fenceGate();
        Block button = creator.button();

        generateFamily(creator.getBlockFamily(), FeatureSet.empty());

        // 原木 -> 木板（shapeless）
        ShapelessRecipeJsonBuilder.create(itemImpl, RecipeCategory.DECORATIONS, planks, 4)
                .input(log)
                .group("planks")
                .criterion("has_log", conditionsFromItem(log))
                .offerTo(exporter, convertBetween(planks, log));

        ShapelessRecipeJsonBuilder.create(itemImpl, RecipeCategory.DECORATIONS, planks, 4)
                .input(wood)
                .group("planks")
                .criterion("has_wood", conditionsFromItem(wood))
                .offerTo(exporter, convertBetween(planks, wood));
        // 去皮木 -> 木板（shapeless）
        ShapelessRecipeJsonBuilder.create(itemImpl, RecipeCategory.DECORATIONS, planks, 4)
                .input(strippedLog)
                .group("planks")
                .criterion("has_log", conditionsFromItem(log))
                .offerTo(exporter, convertBetween(planks, strippedLog));

        ShapelessRecipeJsonBuilder.create(itemImpl, RecipeCategory.DECORATIONS, planks, 4)
                .input(strippedWood)
                .group("planks")
                .criterion("has_wood", conditionsFromItem(wood))
                .offerTo(exporter, convertBetween(planks, strippedWood));

        // 木板 -> 楼梯
        createStairsRecipe(stair, Ingredient.ofItem(planks))
                .criterion("has_planks", conditionsFromItem(planks))
                .offerTo(exporter);

        // 木板 -> 台阶
        createSlabRecipe(RecipeCategory.DECORATIONS, slab, Ingredient.ofItem(planks))
                .criterion("has_planks", conditionsFromItem(planks))
                .offerTo(exporter);

        // 木板 + 棍子 -> 栅栏
        createFenceRecipe(fence, Ingredient.ofItem(planks))
                .criterion("has_planks", conditionsFromItem(planks))
                .offerTo(exporter);

        // 木板 + 棍子 -> 栅栏门
        createFenceGateRecipe(fenceGate, Ingredient.ofItem(planks))
                .criterion("has_planks", conditionsFromItem(planks))
                .offerTo(exporter);

//        // 木板 -> 活板门
//        createTrapdoorRecipe(trapdoor, Ingredient.ofItem(planks))
//                .criterion("has_planks", conditionsFromItem(planks))
//                .offerTo(exporter);

        // 木板 -> 门
        createDoorRecipe(door, Ingredient.ofItem(planks))
                .criterion("has_planks", conditionsFromItem(planks))
                .offerTo(exporter);

        // 木板 -> 按钮
        createButtonRecipe(button, Ingredient.ofItem(planks)).
                group("wooden_button")
                .criterion("has_planks", conditionsFromItem(planks))
                .offerTo(exporter);
    }

    private void generateIngredient() {
        createShaped(RecipeCategory.FOOD, MIItems.CHEESE)
                .pattern("##")
                .pattern("##")
                .input('#', Items.MILK_BUCKET)
                .criterion("has_milk", conditionsFromItem(Items.MILK_BUCKET))
                .offerTo(exporter, getRecipeName(MIItems.CHEESE));
        createShaped(RecipeCategory.FOOD, MIItems.BUTTER)
                .pattern("#")
                .pattern("#")
                .pattern("X")
                .input('#', Items.MILK_BUCKET)
                .input('X', Items.BOWL)
                .criterion("has_milk", conditionsFromItem(MIItems.FLOUR))
                .offerTo(exporter, getRecipeName(MIItems.BUTTER));
        createShaped(RecipeCategory.FOOD, MIItems.FLOUR)
                .pattern("##")
                .pattern("##")
                .input('#', Items.WHEAT)
                .criterion("has_wheat", conditionsFromItem(Items.WHEAT))
                .offerTo(exporter, getRecipeName(MIItems.FLOUR));
        createShaped(RecipeCategory.FOOD, MIItems.TOFU)
                .pattern("##")
                .pattern("##")
                .input('#', MIBlocks.SOY_BEANS.getSeed())
                .criterion("has_soy_beans", conditionsFromItem(MIBlocks.SOY_BEANS.getSeed()))
                .offerTo(exporter, getRecipeName(MIItems.TOFU));
        createShaped(RecipeCategory.FOOD, MIItems.CAPSAICIN)
                .pattern("#")
                .pattern("X")
                .input('#', MIItems.CHILI)
                .input('X', Items.GLASS_BOTTLE)
                .criterion("has_chili", conditionsFromItem(MIItems.CHILI))
                .offerTo(exporter, getRecipeName(MIItems.CAPSAICIN));
        createShaped(RecipeCategory.FOOD, MIItems.CREAM)
                .pattern("#")
                .pattern("#")
                .pattern("X")
                .input('#', Items.MILK_BUCKET)
                .input('X', Items.GLASS_BOTTLE)
                .criterion("has_chili", conditionsFromItem(MIItems.CHILI))
                .offerTo(exporter, getRecipeName(MIItems.CREAM));

        offerSmelting(List.of(MIItems.BLACK_PORK, MIItems.WILD_BOAR_MEAT), RecipeCategory.MISC, Items.COOKED_PORKCHOP, 0.7F, 160, "food");

//        createShaped(RecipeCategory.FOOD, MIItems.FLOWERS)
//                .pattern("##")
//                .pattern("##")
//                .input('#', ItemTags.FLOWERS)
//                .criterion("has_wheat", conditionsFromItem(Items.WHEAT))
//                .offerTo(exporter, getRecipeName(MIItems.FLOUR));
    }

    private void generateDecorativeBlock() {
        ModBlocks.ICE_SCALES.offerRecipe(this, ModItems.ICE_SCALES);
        ModBlocks.DREAM_STONE.offerRecipe(this, ModBlocks.DREAM_STONE.block().asItem());
        ModBlocks.DREAM_STONE_BRICK.offerRecipe(this, ModBlocks.DREAM_STONE_BRICK.block().asItem());
        ModBlocks.MOON_STONE.offerRecipe(this, ModBlocks.MOON_STONE.block().asItem());
        ModBlocks.MOON_STONE_BRICK.offerRecipe(this, ModBlocks.MOON_STONE_BRICK.block().asItem());
    }

    private void generateMusicBlock() {
        // 音乐盒
        createShaped(RecipeCategory.REDSTONE, ModBlocks.MUSIC_BLOCK)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .input('X', Items.EMERALD)
                .input('#', Items.NOTE_BLOCK)
                .criterion("has_emerald", conditionsFromItem(Items.EMERALD))
                .offerTo(exporter, getRecipeName(ModBlocks.MUSIC_BLOCK));
        // 小提琴
        createShaped(RecipeCategory.TOOLS, ModItems.VIOLIN)
                .pattern(" X ")
                .pattern("XYX")
                .pattern("X#X")
                .input('X', Items.SPRUCE_PLANKS)
                .input('Y', Items.STRING)
                .input('#', ModBlocks.MUSIC_BLOCK)
                .criterion("has_music_block", conditionsFromItem(ModBlocks.MUSIC_BLOCK))
                .offerTo(exporter, getRecipeName(ModItems.VIOLIN));
        // 键盘
        createShaped(RecipeCategory.TOOLS, ModItems.KEYBOARD)
                .pattern("XYX")
                .pattern("YXY")
                .pattern("Z#W")
                .input('X', Items.BLACK_WOOL)
                .input('Y', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .input('Z', Items.STONE_BUTTON)
                .input('W', Items.REDSTONE)
                .input('#', ModBlocks.MUSIC_BLOCK)
                .criterion("has_music_block", conditionsFromItem(ModBlocks.MUSIC_BLOCK))
                .offerTo(exporter, getRecipeName(ModItems.KEYBOARD));
        // 小号
        createShaped(RecipeCategory.TOOLS, ModItems.TRUMPET)
                .pattern("XXX")
                .pattern("XY#")
                .pattern("ZYX")
                .input('X', Items.GOLD_INGOT)
                .input('Y', Items.STONE_BUTTON)
                .input('Z', Items.REDSTONE)
                .input('#', ModBlocks.MUSIC_BLOCK)
                .criterion("has_music_block", conditionsFromItem(ModBlocks.MUSIC_BLOCK))
                .offerTo(exporter, getRecipeName(ModItems.TRUMPET));
    }

    private void generateWorkBlock() {
        // 弹幕工作台
        createShaped(RecipeCategory.DECORATIONS, ModBlocks.DANMAKU_CRAFTING_TABLE)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .input('X', Items.REDSTONE)
                .input('#', Items.CRAFTING_TABLE)
                .criterion("has_redstone", conditionsFromItem(Items.REDSTONE))
                .offerTo(exporter, getRecipeName(ModBlocks.DANMAKU_CRAFTING_TABLE));

        // 幻想乡祭坛
        createShaped(RecipeCategory.DECORATIONS, ModBlocks.GENSOKYO_ALTAR)
                .pattern("X")
                .pattern("#")
                .input('X', Items.EMERALD_BLOCK)
                .input('#', Items.ENCHANTING_TABLE)
                .criterion("has_emerald", conditionsFromItem(Items.EMERALD_BLOCK))
                .offerTo(exporter, getRecipeName(ModBlocks.GENSOKYO_ALTAR));

        // 强化台
        createShaped(RecipeCategory.DECORATIONS, ModBlocks.STRENGTH_TABLE)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .input('X', ModItems.SILVER_INGOT)
                .input('#', Items.ENCHANTING_TABLE)
                .criterion("has_silver", conditionsFromItem(ModItems.SILVER_INGOT))
                .offerTo(exporter, getRecipeName(ModBlocks.STRENGTH_TABLE));
    }

    private void generateOrb() {
        // 宝玉 / 宝玉块
        offerIngotToBlockRecipe(exporter, ModItems.RED_ORB, ModBlocks.RED_ORB_BLOCK.asItem());
        offerBlockToIngotRecipe(exporter, ModBlocks.RED_ORB_BLOCK.asItem(), ModItems.RED_ORB);

        offerIngotToBlockRecipe(exporter, ModItems.YELLOW_ORB, ModBlocks.YELLOW_ORB_BLOCK.asItem());
        offerBlockToIngotRecipe(exporter, ModBlocks.YELLOW_ORB_BLOCK.asItem(), ModItems.YELLOW_ORB);

        offerIngotToBlockRecipe(exporter, ModItems.BLUE_ORB, ModBlocks.BLUE_ORB_BLOCK.asItem());
        offerBlockToIngotRecipe(exporter, ModBlocks.BLUE_ORB_BLOCK.asItem(), ModItems.BLUE_ORB);

        offerIngotToBlockRecipe(exporter, ModItems.GREEN_ORB, ModBlocks.GREEN_ORB_BLOCK.asItem());
        offerBlockToIngotRecipe(exporter, ModBlocks.GREEN_ORB_BLOCK.asItem(), ModItems.GREEN_ORB);

        offerIngotToBlockRecipe(exporter, ModItems.PURPLE_ORB, ModBlocks.PURPLE_ORB_BLOCK.asItem());
        offerBlockToIngotRecipe(exporter, ModBlocks.PURPLE_ORB_BLOCK.asItem(), ModItems.PURPLE_ORB);

    }

    private void generateSilver() {
        // 银武器/工具
        offerSwordRecipe(exporter, ModItems.SILVER_SWORD, ModItems.SILVER_INGOT);
        offerPickaxeRecipe(exporter, ModItems.SILVER_PICKAXE, ModItems.SILVER_INGOT);
        offerAxeRecipe(exporter, ModItems.SILVER_AXE, ModItems.SILVER_INGOT);
        offerShovelRecipe(exporter, ModItems.SILVER_SHOVEL, ModItems.SILVER_INGOT);
        offerHoeRecipe(exporter, ModItems.SILVER_HOE, ModItems.SILVER_INGOT);

        // 银盔甲
        offerHelmetRecipe(exporter, ModItems.SILVER_HELMET, ModItems.SILVER_INGOT);
        offerChestplateRecipe(exporter, ModItems.SILVER_CHESTPLATE, ModItems.SILVER_INGOT);
        offerLeggingsRecipe(exporter, ModItems.SILVER_LEGGINGS, ModItems.SILVER_INGOT);
        offerBootsRecipe(exporter, ModItems.SILVER_BOOTS, ModItems.SILVER_INGOT);

        // 银粒 / 锭 / 块
        offerIngotToBlockRecipe(exporter, ModItems.SILVER_NUGGET, ModItems.SILVER_INGOT);
        offerBlockToIngotRecipe(exporter, ModItems.SILVER_INGOT, ModItems.SILVER_NUGGET);
        offerIngotToBlockRecipe(exporter, ModItems.SILVER_INGOT, ModBlocks.SILVER_BLOCK.asItem());
        offerBlockToIngotRecipe(exporter, ModBlocks.SILVER_BLOCK.asItem(), ModItems.SILVER_INGOT);

        // 烧银矿
        offerSmelting(SILVER, RecipeCategory.MISC, ModItems.SILVER_INGOT, 0.7F, 250, "silver_ingot");
        offerBlasting(SILVER, RecipeCategory.MISC, ModItems.SILVER_INGOT, 0.7F, 250, "silver_ingot");
        offerSmelting(SILVER, RecipeCategory.MISC, ModBlocks.SILVER_ORE, 0.7F, 250, "silver_ingot");
        offerBlasting(SILVER, RecipeCategory.MISC, ModBlocks.SILVER_ORE, 0.7F, 250, "silver_ingot");


    }

    private void generateMaid() {
        Map<Item, Item> itemItemMap = new HashMap<>(
                Map.of(
                        ModItems.SILVER_HELMET, ModItems.MAID_HAIRBAND,
                        ModItems.SILVER_CHESTPLATE, ModItems.MAID_UPPER_SKIRT,
                        ModItems.SILVER_LEGGINGS, ModItems.MAID_LOWER_SKIRT,
                        ModItems.SILVER_BOOTS, ModItems.MAID_SHOE
                )
        );
        for (Map.Entry<Item, Item> itemItemEntry : itemItemMap.entrySet()) {
            Item left = itemItemEntry.getKey();
            Item right = itemItemEntry.getValue();
            createShaped(RecipeCategory.REDSTONE, right)
                    .pattern("XXX")
                    .pattern("X#X")
                    .pattern("XXX")
                    .input('X', ItemTags.WOOL)
                    .input('#', left)
                    .criterion("has_wool", conditionsFromTag(ItemTags.WOOL))
                    .offerTo(exporter, getRecipeName(right));
        }
    }

    private void generateMagicIce() {
        offerSmelting(List.of(ModBlocks.MAGIC_ICE_BLOCK.asItem()), RecipeCategory.MISC, ModItems.ICE_SCALES, 0.7F, 140, "silver_ingot");
        offerBlasting(List.of(ModBlocks.MAGIC_ICE_BLOCK.asItem()), RecipeCategory.MISC, ModItems.ICE_SCALES, 0.7F, 70, "silver_ingot");
        // 魔法冰
        createShaped(RecipeCategory.DECORATIONS, ModBlocks.MAGIC_ICE_BLOCK)
                .pattern("XX")
                .pattern("XX")
                .input('X', Items.ICE)
                .criterion("has_ice", conditionsFromItem(Items.ICE))
                .offerTo(exporter, getRecipeName(ModBlocks.MAGIC_ICE_BLOCK));

        // 冰武器/工具
        offerSwordRecipe(exporter, ModItems.MAGIC_ICE_SWORD, ModItems.ICE_SCALES);
        offerPickaxeRecipe(exporter, ModItems.MAGIC_ICE_PICKAXE, ModItems.ICE_SCALES);
        offerAxeRecipe(exporter, ModItems.MAGIC_ICE_AXE, ModItems.ICE_SCALES);
        offerShovelRecipe(exporter, ModItems.MAGIC_ICE_SHOVEL, ModItems.ICE_SCALES);
        offerHoeRecipe(exporter, ModItems.MAGIC_ICE_HOE, ModItems.ICE_SCALES);

        // 冰盔甲
        offerHelmetRecipe(exporter, ModItems.MAGIC_ICE_HELMET, ModBlocks.MAGIC_ICE_BLOCK.asItem());
        offerChestplateRecipe(exporter, ModItems.MAGIC_ICE_CHESTPLATE, ModBlocks.MAGIC_ICE_BLOCK.asItem());
        offerLeggingsRecipe(exporter, ModItems.MAGIC_ICE_LEGGINGS, ModBlocks.MAGIC_ICE_BLOCK.asItem());
        offerBootsRecipe(exporter, ModItems.MAGIC_ICE_BOOTS, ModBlocks.MAGIC_ICE_BLOCK.asItem());

    }

    private void generateMIPlant2Ingredient() {
        createShaped(RecipeCategory.FOOD, MIItems.UDUMBARA)
                .pattern("#")
                .input('#', MIBlocks.UDUMBARA_FLOWER)
                .criterion("always", conditionsFromItem(Items.AIR))
                .offerTo(exporter, getRecipeName(MIItems.UDUMBARA));
        createShaped(RecipeCategory.FOOD, MIItems.TREMELLA)
                .pattern("#")
                .input('#', MIBlocks.TREMELLA)
                .criterion("always", conditionsFromItem(Items.AIR))
                .offerTo(exporter, getRecipeName(MIItems.TREMELLA));
    }

    private void generateMICookRecipe() {
        // 厨具
        createShaped(RecipeCategory.DECORATIONS, MIBlocks.COOKING_POT)
                .pattern(" Y ")
                .pattern("X X")
                .pattern("XXX")
                .input('X', Items.IRON_INGOT)
                .input('Y', Items.IRON_NUGGET)
                .criterion("always", conditionsFromItem(Items.AIR))
                .offerTo(exporter, getRecipeName(MIBlocks.COOKING_POT));

        createShaped(RecipeCategory.DECORATIONS, MIBlocks.CUTTING_BOARD)
                .pattern(" Y ")
                .pattern("XXX")
                .input('X', Items.OAK_SLAB)
                .input('Y', Items.IRON_SWORD)
                .criterion("always", conditionsFromItem(Items.AIR))
                .offerTo(exporter, getRecipeName(MIBlocks.CUTTING_BOARD));

        createShaped(RecipeCategory.DECORATIONS, MIBlocks.FRYING_PAN)
                .pattern(" XX")
                .pattern(" XX")
                .pattern("Y  ")
                .input('X', Items.IRON_INGOT)
                .input('Y', Items.IRON_NUGGET)
                .criterion("always", conditionsFromItem(Items.AIR))
                .offerTo(exporter, getRecipeName(MIBlocks.FRYING_PAN));

        createShaped(RecipeCategory.DECORATIONS, MIBlocks.GRILL)
                .pattern("YYY")
                .pattern("X X")
                .pattern("XXX")
                .input('X', Items.IRON_INGOT)
                .input('Y', Items.IRON_NUGGET)
                .criterion("always", conditionsFromItem(Items.AIR))
                .offerTo(exporter, getRecipeName(MIBlocks.GRILL));

        createShaped(RecipeCategory.DECORATIONS, MIBlocks.STEAMER)
                .pattern("YYY")
                .pattern("X X")
                .pattern("XXX")
                .input('X', Items.IRON_INGOT)
                .input('Y', Items.OAK_SLAB)
                .criterion("always", conditionsFromItem(Items.AIR))
                .offerTo(exporter, getRecipeName(MIBlocks.STEAMER));
        createShaped(RecipeCategory.DECORATIONS, MIBlocks.ITEM_DISPLAY)
                .pattern("YXY")
                .pattern(" Y ")
                .input('X', Items.ITEM_FRAME)
                .input('Y', Items.QUARTZ)
                .criterion("always", conditionsFromItem(Items.AIR))
                .offerTo(exporter, getRecipeName(MIBlocks.ITEM_DISPLAY));

        this.offerMIUpgradeRecipes();
//        // 能量罩
//        createShaped(RecipeCategory.DECORATIONS, MIBlocks.COOKTOP)
//                .pattern("YYY")
//                .pattern("YXY")
//                .pattern("YYY")
//                .input('X', Items.FURNACE)
//                .input('Y', Items.BRICKS)
//                .criterion("always", conditionsFromItem(Items.AIR))
//                .offerTo(exporter, getRecipeName(MIBlocks.COOKTOP));
    }

    private void offerMIUpgradeRecipes() {
        Map<Block, Block> mystiaUpgrade = new Object2ObjectOpenHashMap<>();
        mystiaUpgrade.put(MIBlocks.COOKING_POT, MIBlocks.MYSTIA_COOKING_POT);
        mystiaUpgrade.put(MIBlocks.CUTTING_BOARD, MIBlocks.MYSTIA_CUTTING_BOARD);
        mystiaUpgrade.put(MIBlocks.FRYING_PAN, MIBlocks.MYSTIA_FRYING_PAN);
        mystiaUpgrade.put(MIBlocks.GRILL, MIBlocks.MYSTIA_GRILL);
        mystiaUpgrade.put(MIBlocks.STEAMER, MIBlocks.MYSTIA_STEAMER);
        this.offerUpgradeRecipes(mystiaUpgrade, Items.FEATHER);

        Map<Block, Block> superUpgrade = new Object2ObjectOpenHashMap<>();
        superUpgrade.put(MIBlocks.COOKING_POT, MIBlocks.SUPER_COOKING_POT);
        superUpgrade.put(MIBlocks.CUTTING_BOARD, MIBlocks.SUPER_CUTTING_BOARD);
        superUpgrade.put(MIBlocks.FRYING_PAN, MIBlocks.SUPER_FRYING_PAN);
        superUpgrade.put(MIBlocks.GRILL, MIBlocks.SUPER_GRILL);
        superUpgrade.put(MIBlocks.STEAMER, MIBlocks.SUPER_STEAMER);
        this.offerUpgradeRecipes(superUpgrade, Items.GOLD_INGOT);

        Map<Block, Block> extremeUpgrade = new Object2ObjectOpenHashMap<>();
        extremeUpgrade.put(MIBlocks.COOKING_POT, MIBlocks.EXTREME_COOKING_POT);
        extremeUpgrade.put(MIBlocks.CUTTING_BOARD, MIBlocks.EXTREME_CUTTING_BOARD);
        extremeUpgrade.put(MIBlocks.FRYING_PAN, MIBlocks.EXTREME_FRYING_PAN);
        extremeUpgrade.put(MIBlocks.GRILL, MIBlocks.EXTREME_GRILL);
        extremeUpgrade.put(MIBlocks.STEAMER, MIBlocks.EXTREME_STEAMER);
        this.offerUpgradeRecipes(extremeUpgrade, Items.DIAMOND);

    }

    private void offerUpgradeRecipes(Map<Block, Block> blockBlockMap, Item upgradeMaterial) {
        Set<Map.Entry<Block, Block>> entries = blockBlockMap.entrySet();
        for (Map.Entry<Block, Block> entry : entries) {
            Block key = entry.getKey();
            Block value = entry.getValue();
            createShaped(RecipeCategory.DECORATIONS, value)
                    .pattern("YYY")
                    .pattern("YXY")
                    .pattern("YYY")
                    .input('X', key)
                    .input('Y', upgradeMaterial)
                    .criterion("always", conditionsFromItem(Items.AIR))
                    .offerTo(exporter, getRecipeName(value));
        }
    }

    private void offerSwordRecipe(RecipeExporter exporter, Item result, Item ingot) {
        createShaped(RecipeCategory.COMBAT, result)
                .pattern("X")
                .pattern("X")
                .pattern("#")
                .input('X', ingot)
                .input('#', Items.STICK)
                .criterion("has_ingot", conditionsFromItem(ingot))
                .offerTo(exporter, getRecipeName(result));
    }

    private void offerPickaxeRecipe(RecipeExporter exporter, Item result, Item ingot) {
        createShaped(RecipeCategory.TOOLS, result)
                .pattern("XXX")
                .pattern(" # ")
                .pattern(" # ")
                .input('X', ingot)
                .input('#', Items.STICK)
                .criterion("has_ingot", conditionsFromItem(ingot))
                .offerTo(exporter, getRecipeName(result));
    }

    private void offerAxeRecipe(RecipeExporter exporter, Item result, Item ingot) {
        createShaped(RecipeCategory.TOOLS, result)
                .pattern("XX")
                .pattern("X#")
                .pattern(" #")
                .input('X', ingot)
                .input('#', Items.STICK)
                .criterion("has_ingot", conditionsFromItem(ingot))
                .offerTo(exporter, getRecipeName(result));
    }

    private void offerShovelRecipe(RecipeExporter exporter, Item result, Item ingot) {
        createShaped(RecipeCategory.TOOLS, result)
                .pattern("X")
                .pattern("#")
                .pattern("#")
                .input('X', ingot)
                .input('#', Items.STICK)
                .criterion("has_ingot", conditionsFromItem(ingot))
                .offerTo(exporter, getRecipeName(result));
    }

    private void offerHoeRecipe(RecipeExporter exporter, Item result, Item ingot) {
        createShaped(RecipeCategory.TOOLS, result)
                .pattern("XX")
                .pattern(" #")
                .pattern(" #")
                .input('X', ingot)
                .input('#', Items.STICK)
                .criterion("has_ingot", conditionsFromItem(ingot))
                .offerTo(exporter, getRecipeName(result));
    }

    private void offerHelmetRecipe(RecipeExporter exporter, Item result, Item ingot) {
        createShaped(RecipeCategory.COMBAT, result)
                .pattern("XXX")
                .pattern("X X")
                .input('X', ingot)
                .criterion("has_ingot", conditionsFromItem(ingot))
                .offerTo(exporter, getRecipeName(result));
    }

    private void offerChestplateRecipe(RecipeExporter exporter, Item result, Item ingot) {
        createShaped(RecipeCategory.COMBAT, result)
                .pattern("X X")
                .pattern("XXX")
                .pattern("XXX")
                .input('X', ingot)
                .criterion("has_ingot", conditionsFromItem(ingot))
                .offerTo(exporter, getRecipeName(result));
    }

    private void offerLeggingsRecipe(RecipeExporter exporter, Item result, Item ingot) {
        createShaped(RecipeCategory.COMBAT, result)
                .pattern("XXX")
                .pattern("X X")
                .pattern("X X")
                .input('X', ingot)
                .criterion("has_ingot", conditionsFromItem(ingot))
                .offerTo(exporter, getRecipeName(result));
    }

    private void offerBootsRecipe(RecipeExporter exporter, Item result, Item ingot) {
        createShaped(RecipeCategory.COMBAT, result)
                .pattern("X X")
                .pattern("X X")
                .input('X', ingot)
                .criterion("has_ingot", conditionsFromItem(ingot))
                .offerTo(exporter, getRecipeName(result));
    }

    private void offerIngotToBlockRecipe(RecipeExporter exporter, Item ingot, Item block) {
        createShaped(RecipeCategory.BUILDING_BLOCKS, block)
                .pattern("XXX")
                .pattern("XXX")
                .pattern("XXX")
                .input('X', ingot)
                .criterion("has_ingot", conditionsFromItem(ingot))
                .offerTo(exporter, getRecipeName(block));
    }

    private void offerBlockToIngotRecipe(RecipeExporter exporter, Item block, Item ingot) {
        createShapeless(RecipeCategory.MISC, ingot, 9)
                .input(block)
                .criterion("has_block", conditionsFromItem(block))
                .offerTo(exporter, getRecipeName(ingot) + "_from_block");
    }


}
