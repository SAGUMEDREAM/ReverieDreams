package cc.thonly.reverie_dreams.datagen;

import cc.thonly.reverie_dreams.block.creator.WoodCreator;
import cc.thonly.reverie_dreams.datagen.entry.ShapedStackRecipeBuilder;
import cc.thonly.reverie_dreams.mixin.accessor.RecipeProviderAccessor;
import cc.thonly.reverie_dreams.registry.content.RDEnchantments;
import cc.thonly.reverie_dreams.registry.content.block.KitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDCropBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.*;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImplRecipeGenerator extends RecipeProvider {
    public static ImmutableList<ItemLike> SILVER = ImmutableList.of(RDBlocks.SILVER_ORE.asItem(), RDBlocks.DEEPSLATE_SILVER_ORE.asItem(), RDItems.RAW_SILVER);
    public static ImmutableList<ItemLike> DREAM = ImmutableList.of(RDBlocks.DREAM_CRYSTAL_ORE.asItem());

    protected ImplRecipeGenerator(HolderLookup.Provider registries, RecipeOutput exporter) {
        super(registries, exporter);
    }

    @Override
    public void buildRecipes() {
        // 入门书
        shaped(RecipeCategory.MISC, RDItems.TOUHOU_HELPER)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .define('X', RDItems.POWER)
                .define('#', Items.BOOK)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(output, getSimpleRecipeName(RDItems.TOUHOU_HELPER));

        // Point / 块
        offerIngotToBlockRecipe(output, RDItems.POINT, RDBlocks.POINT_BLOCK.asItem());
        offerBlockToIngotRecipe(output, RDBlocks.POINT_BLOCK.asItem(), RDItems.POINT);

        // Power / 块
        offerIngotToBlockRecipe(output, RDItems.POWER, RDBlocks.POWER_BLOCK.asItem());
        offerBlockToIngotRecipe(output, RDBlocks.POWER_BLOCK.asItem(), RDItems.POWER);

        // 速度羽毛
        shaped(RecipeCategory.DECORATIONS, RDItems.SPEED_FEATHER)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .define('X', Items.FEATHER)
                .define('#', Items.DIAMOND)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(output, getSimpleRecipeName(RDItems.SPEED_FEATHER));

        // 梦境枕头
        shaped(RecipeCategory.DECORATIONS, RDItems.DREAM_PILLOW)
                .pattern("XXX")
                .pattern("Y#Y")
                .define('X', Items.PINK_WOOL)
                .define('Y', Items.GOLD_INGOT)
                .define('#', Items.EMERALD)
                .unlockedBy("has_emerald", has(Items.EMERALD))
                .save(output, getSimpleRecipeName(RDItems.DREAM_PILLOW));

        // 残机
        offer1To4Recipe(output, RDItems.UPGRADED_HEALTH, RDItems.UPGRADED_HEALTH_FRAGMENT);
        shaped(RecipeCategory.MISC, RDItems.UPGRADED_HEALTH, 2)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .define('X', RDBlocks.POWER_BLOCK)
                .define('#', RDItems.UPGRADED_HEALTH_FRAGMENT)
                .unlockedBy("has_health_fragment", has(RDItems.UPGRADED_HEALTH_FRAGMENT))
                .save(output, getSimpleRecipeName(RDItems.UPGRADED_HEALTH_FRAGMENT) + "_copy");

        // Bomb
        offer1To4Recipe(output, RDItems.BOMB, RDItems.BOMB_FRAGMENT);
        shaped(RecipeCategory.MISC, RDItems.BOMB_FRAGMENT, 2)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .define('X', RDBlocks.POINT_BLOCK)
                .define('#', RDItems.BOMB_FRAGMENT)
                .unlockedBy("has_bomb_fragment", has(RDItems.BOMB_FRAGMENT))
                .save(output, getSimpleRecipeName(RDItems.BOMB_FRAGMENT) + "_copy");

        // 弹幕创作模板
        shaped(RecipeCategory.MISC, RDItems.DANMAKU_SHAPE_CREATOR)
                .pattern("RRR")
                .pattern("R#R")
                .pattern("RRR")
                .define('R', RDItems.POWER)
                .define('#', Items.WRITABLE_BOOK)
                .unlockedBy("has_power", has(RDItems.POWER))
                .save(output, getSimpleRecipeName(RDItems.DANMAKU_SHAPE_CREATOR));

        // 空白角色卡
        shaped(RecipeCategory.MISC, RDItems.ROLE_CARD)
                .pattern(" R ")
                .pattern("R#R")
                .pattern(" R ")
                .define('R', Items.REDSTONE)
                .define('#', Items.PAPER)
                .unlockedBy("has_paper", has(Items.PAPER))
                .save(output, getSimpleRecipeName(RDItems.ROLE_CARD));

        // 魔理沙的帽子
        shaped(RecipeCategory.DECORATIONS, RDBlocks.MARISA_HAT_BLOCK)
                .pattern(" X ")
                .pattern("X#X")
                .pattern("XYX")
                .define('X', Items.WHITE_WOOL)
                .define('Y', Items.LEATHER)
                .define('#', Items.BLACK_WOOL)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(output, getSimpleRecipeName(RDBlocks.MARISA_HAT_BLOCK));

        // 塞钱箱
        shaped(RecipeCategory.MISC, RDBlocks.CASH_BOX_BLOCK)
                .pattern("YYY")
                .pattern("X#X")
                .pattern("YYY")
                .define('X', RDWoodBlocks.SPIRITUAL.planks())
                .define('Y', RDWoodBlocks.SPIRITUAL.slab())
                .define('#', Items.CHEST)
                .unlockedBy("has_wool", has(Items.CHEST))
                .save(output, getSimpleRecipeName(RDBlocks.CASH_BOX_BLOCK));

        // 防撞桶
        shaped(RecipeCategory.MISC, RDBlocks.ANTI_COLLISION_BARREL)
                .pattern("YXY")
                .pattern("Z#Z")
                .pattern("YXY")
                .define('X', Items.YELLOW_WOOL)
                .define('Y', Items.RED_WOOL)
                .define('Z', Items.WHITE_WOOL)
                .define('#', Items.IRON_BLOCK)
                .unlockedBy("has_iron_block", has(Items.IRON_BLOCK))
                .save(output, getSimpleRecipeName(RDBlocks.ANTI_COLLISION_BARREL));
        // 轮椅
        shaped(RecipeCategory.MISC, RDBlocks.WHEEL_CHAIR)
                .pattern("Z  ")
                .pattern("X# ")
                .pattern("YYY")
                .define('#', Items.IRON_INGOT)
                .define('X', Items.IRON_BLOCK)
                .define('Y', Items.RESIN_BRICK)
                .define('Z', Items.BLACK_WOOL)
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(output, getSimpleRecipeName(RDBlocks.WHEEL_CHAIR));

        // 木板箱
        shaped(RecipeCategory.MISC, RDBlocks.WOODEN_BOX.chestBlock())
                .pattern("YZY")
                .pattern("X#X")
                .pattern("YZY")
                .define('#', Items.CHEST)
                .define('X', Items.OAK_SLAB)
                .define('Y', Items.OAK_PLANKS)
                .define('Z', Items.STICK)
                .unlockedBy("has_stick", has(Items.STICK))
                .save(output, getSimpleRecipeName(RDBlocks.WOODEN_BOX.chestBlock()));

        // Fumo销售许可
        shaped(RecipeCategory.MISC, RDItems.FUMO_LICENSE)
                .pattern("YXY")
                .pattern("X#X")
                .pattern("YXY")
                .define('X', Items.WHITE_WOOL)
                .define('Y', Items.DIAMOND)
                .define('#', Items.PAPER)
                .unlockedBy("has_wool", has(Items.WHITE_WOOL))
                .save(output, getSimpleRecipeName(RDItems.FUMO_LICENSE));

        // 烟火之星
        shaped(RecipeCategory.MISC, Items.FIREWORK_STAR)
                .pattern("###")
                .pattern("#X#")
                .pattern("###")
                .define('#', Items.GUNPOWDER)
                .define('X', Items.SAND)
                .unlockedBy("has_sand", has(Items.GUNPOWDER))
                .save(output, "rd_provided_" + getSimpleRecipeName(Items.FIREWORK_STAR));

        // 弹幕核心
        shaped(RecipeCategory.MISC, RDItems.DANMAKU_CORE)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .define('#', Items.FIREWORK_STAR)
                .define('X', Items.IRON_NUGGET)
                .unlockedBy("has_sand", has(Items.FIREWORK_STAR))
                .save(output, getSimpleRecipeName(RDItems.DANMAKU_CORE));

        // 空照片
        shaped(RecipeCategory.MISC, RDItems.EMPTY_PHOTO, 2)
                .pattern("YYY")
                .pattern("###")
                .pattern("XXX")
                .define('#', Items.PAPER)
                .define('Y', Items.GLASS_PANE)
                .define('X', Items.IRON_NUGGET)
                .unlockedBy("has_paper", has(Items.PAPER))
                .save(output, getSimpleRecipeName(RDItems.EMPTY_PHOTO));

        this.generateWoodCreator(RDWoodBlocks.SPIRITUAL);
        shapeless(RecipeCategory.BUILDING_BLOCKS, RDWoodBlocks.SPIRITUAL.strippedLog())
                .requires(RDWoodBlocks.BLESSED_SPIRITUAL_LOG)
                .unlockedBy("has_blessed_spiritual_log", has(RDWoodBlocks.BLESSED_SPIRITUAL_LOG))
                .save(output, "cutting_" + getSimpleRecipeName(RDWoodBlocks.BLESSED_SPIRITUAL_LOG));
        shaped(RecipeCategory.BUILDING_BLOCKS, RDWoodBlocks.BLESSED_SPIRITUAL_LOG, 1)
                .pattern("Y")
                .pattern("X")
                .define('Y', Items.PAPER)
                .define('X', RDWoodBlocks.SPIRITUAL.strippedLog())
                .unlockedBy("has_paper", has(Items.PAPER))
                .save(output, getSimpleRecipeName(RDWoodBlocks.BLESSED_SPIRITUAL_LOG));

        // 附魔桃子
        ItemStack peachStack = RDIngredientItems.PEACH.getDefaultInstance();
        HolderLookup.RegistryLookup<Enchantment> enchantmentRegistryLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        Holder.Reference<Enchantment> powerful = enchantmentRegistryLookup.getOrThrow(RDEnchantments.POWERFUL);
        peachStack.enchant(powerful, 1);
        shapedItemStack(RecipeCategory.MISC, peachStack)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .define('X', Items.GOLD_NUGGET)
                .define('#', RDIngredientItems.PEACH)
                .unlockedBy("has_gold_nugget", has(Items.GOLD_NUGGET))
                .save(output, getSimpleRecipeName(RDIngredientItems.PEACH));

        this.generateWoodCreator(RDWoodBlocks.LEMON);
        this.generateWoodCreator(RDWoodBlocks.GINKGO);
        this.generateDecorativeBlock();

        this.generateWorkBlock();
        this.generateOrb();
        this.generateSilver();
        this.generateMaid();
        this.generateMagicIce();
        this.generateDream();
        this.generateMusicBlock();
        this.generateIngredient();
        this.generateMIPlant2Ingredient();
        this.generateMICookRecipe();
    }

    private void generateWoodCreator(WoodCreator creator) {
        HolderGetter<Item> itemImpl = this.registries.lookupOrThrow(Registries.ITEM);
        Block log = creator.log();
        Block wood = creator.wood();
        Block strippedLog = creator.strippedLog();
        Block strippedWood = creator.strippedWood();
        Block planks = creator.planks();
        Block stair = creator.stairs();
        Block slab = creator.slab();
        Block door = creator.door();
        Block trapdoor = creator.trapdoor();
        Block fence = creator.fence();
        Block fenceGate = creator.fenceGate();
        Block button = creator.button();

        generateRecipes(creator.getBlockFamily(), FeatureFlagSet.of());

        // 原木 -> 木板（shapeless）
        ShapelessRecipeBuilder.shapeless(itemImpl, RecipeCategory.DECORATIONS, planks, 4)
                .requires(log)
                .group("planks")
                .unlockedBy("has_log", has(log))
                .save(output, getConversionRecipeName(planks, log));

        ShapelessRecipeBuilder.shapeless(itemImpl, RecipeCategory.DECORATIONS, planks, 4)
                .requires(wood)
                .group("planks")
                .unlockedBy("has_wood", has(wood))
                .save(output, getConversionRecipeName(planks, wood));
        // 去皮木 -> 木板（shapeless）
        ShapelessRecipeBuilder.shapeless(itemImpl, RecipeCategory.DECORATIONS, planks, 4)
                .requires(strippedLog)
                .group("planks")
                .unlockedBy("has_log", has(log))
                .save(output, getConversionRecipeName(planks, strippedLog));

        ShapelessRecipeBuilder.shapeless(itemImpl, RecipeCategory.DECORATIONS, planks, 4)
                .requires(strippedWood)
                .group("planks")
                .unlockedBy("has_wood", has(wood))
                .save(output, getConversionRecipeName(planks, strippedWood));

        // 木板 -> 楼梯
        stairBuilder(stair, Ingredient.of(planks))
                .unlockedBy("has_planks", has(planks))
                .save(output);

        // 木板 -> 台阶
        slabBuilder(RecipeCategory.DECORATIONS, slab, Ingredient.of(planks))
                .unlockedBy("has_planks", has(planks))
                .save(output);

        // 木板 + 棍子 -> 栅栏
        fenceBuilder(fence, Ingredient.of(planks))
                .unlockedBy("has_planks", has(planks))
                .save(output);

        // 木板 + 棍子 -> 栅栏门
        fenceGateBuilder(fenceGate, Ingredient.of(planks))
                .unlockedBy("has_planks", has(planks))
                .save(output);

        // 木板 -> 活板门
        trapdoorBuilder(trapdoor, Ingredient.of(planks))
                .unlockedBy("has_planks", has(planks))
                .save(output);

        // 木板 -> 门
        doorBuilder(door, Ingredient.of(planks))
                .unlockedBy("has_planks", has(planks))
                .save(output);

        // 木板 -> 按钮
        buttonBuilder(button, Ingredient.of(planks)).
                group("wooden_button")
                .unlockedBy("has_planks", has(planks))
                .save(output);
    }

    private void generateIngredient() {
        shaped(RecipeCategory.FOOD, RDIngredientItems.CHEESE)
                .pattern("##")
                .pattern("##")
                .define('#', Items.MILK_BUCKET)
                .unlockedBy("has_milk", has(Items.MILK_BUCKET))
                .save(output, getSimpleRecipeName(RDIngredientItems.CHEESE));
        shaped(RecipeCategory.FOOD, RDIngredientItems.BUTTER)
                .pattern("#")
                .pattern("#")
                .pattern("X")
                .define('#', Items.MILK_BUCKET)
                .define('X', Items.BOWL)
                .unlockedBy("has_milk", has(RDIngredientItems.FLOUR))
                .save(output, getSimpleRecipeName(RDIngredientItems.BUTTER));
        shaped(RecipeCategory.FOOD, RDIngredientItems.FLOUR)
                .pattern("##")
                .pattern("##")
                .define('#', Items.WHEAT)
                .unlockedBy("has_wheat", has(Items.WHEAT))
                .save(output, getSimpleRecipeName(RDIngredientItems.FLOUR));
        shaped(RecipeCategory.FOOD, RDIngredientItems.TOFU)
                .pattern("##")
                .pattern("##")
                .define('#', RDCropBlocks.SOY_BEANS.getSeed())
                .unlockedBy("has_soy_beans", has(RDCropBlocks.SOY_BEANS.getSeed()))
                .save(output, getSimpleRecipeName(RDIngredientItems.TOFU));
        shaped(RecipeCategory.FOOD, RDIngredientItems.CAPSAICIN)
                .pattern("#")
                .pattern("X")
                .define('#', RDIngredientItems.CHILI)
                .define('X', Items.GLASS_BOTTLE)
                .unlockedBy("has_chili", has(RDIngredientItems.CHILI))
                .save(output, getSimpleRecipeName(RDIngredientItems.CAPSAICIN));
        shaped(RecipeCategory.FOOD, RDIngredientItems.CREAM)
                .pattern("#")
                .pattern("#")
                .pattern("X")
                .define('#', Items.MILK_BUCKET)
                .define('X', Items.GLASS_BOTTLE)
                .unlockedBy("has_chili", has(RDIngredientItems.CHILI))
                .save(output, getSimpleRecipeName(RDIngredientItems.CREAM));

        oreSmelting(List.of(RDIngredientItems.BLACK_PORK, RDIngredientItems.WILD_BOAR_MEAT), RecipeCategory.MISC, Items.COOKED_PORKCHOP, 0.7F, 160, "food");

//        createShaped(RecipeCategory.FOOD, MIItems.FLOWERS)
//                .pattern("##")
//                .pattern("##")
//                .input('#', ItemTags.FLOWERS)
//                .criterion("has_wheat", conditionsFromItem(Items.WHEAT))
//                .offerTo(exporter, getRecipeName(MIItems.FLOUR));
    }

    private void generateDecorativeBlock() {
        RDBlocks.ICE_SCALES.offerRecipe(this, RDItems.ICE_SCALES);
        RDBlocks.DREAM_STONE.offerRecipe(this, RDBlocks.DREAM_STONE.block().asItem());
        RDBlocks.DREAM_STONE_BRICK.offerRecipe(this, RDBlocks.DREAM_STONE_BRICK.block().asItem());
        RDBlocks.MOON_STONE.offerRecipe(this, RDBlocks.MOON_STONE.block().asItem());
        RDBlocks.MOON_STONE_BRICK.offerRecipe(this, RDBlocks.MOON_STONE_BRICK.block().asItem());
    }

    private void generateMusicBlock() {
        // 音乐盒
        shaped(RecipeCategory.REDSTONE, RDBlocks.MUSIC_BLOCK)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .define('X', Items.EMERALD)
                .define('#', Items.NOTE_BLOCK)
                .unlockedBy("has_emerald", has(Items.EMERALD))
                .save(output, getSimpleRecipeName(RDBlocks.MUSIC_BLOCK));
        // 小提琴
        shaped(RecipeCategory.TOOLS, RDItems.VIOLIN)
                .pattern(" X ")
                .pattern("XYX")
                .pattern("X#X")
                .define('X', Items.SPRUCE_PLANKS)
                .define('Y', Items.STRING)
                .define('#', RDBlocks.MUSIC_BLOCK)
                .unlockedBy("has_music_block", has(RDBlocks.MUSIC_BLOCK))
                .save(output, getSimpleRecipeName(RDItems.VIOLIN));
        // 键盘
        shaped(RecipeCategory.TOOLS, RDItems.KEYBOARD)
                .pattern("XYX")
                .pattern("YXY")
                .pattern("Z#W")
                .define('X', Items.BLACK_WOOL)
                .define('Y', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .define('Z', Items.STONE_BUTTON)
                .define('W', Items.REDSTONE)
                .define('#', RDBlocks.MUSIC_BLOCK)
                .unlockedBy("has_music_block", has(RDBlocks.MUSIC_BLOCK))
                .save(output, getSimpleRecipeName(RDItems.KEYBOARD));
        // 小号
        shaped(RecipeCategory.TOOLS, RDItems.TRUMPET)
                .pattern("XXX")
                .pattern("XY#")
                .pattern("ZYX")
                .define('X', Items.GOLD_INGOT)
                .define('Y', Items.STONE_BUTTON)
                .define('Z', Items.REDSTONE)
                .define('#', RDBlocks.MUSIC_BLOCK)
                .unlockedBy("has_music_block", has(RDBlocks.MUSIC_BLOCK))
                .save(output, getSimpleRecipeName(RDItems.TRUMPET));
    }

    private void generateWorkBlock() {
        // 弹幕工作台
        shaped(RecipeCategory.DECORATIONS, RDBlocks.DANMAKU_CRAFTING_TABLE)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .define('X', Items.REDSTONE)
                .define('#', Items.CRAFTING_TABLE)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(output, getSimpleRecipeName(RDBlocks.DANMAKU_CRAFTING_TABLE));

        // 幻想乡祭坛
        shaped(RecipeCategory.DECORATIONS, RDBlocks.GENSOKYO_ALTAR)
                .pattern("X")
                .pattern("#")
                .define('X', Items.EMERALD_BLOCK)
                .define('#', Items.ENCHANTING_TABLE)
                .unlockedBy("has_emerald", has(Items.EMERALD_BLOCK))
                .save(output, getSimpleRecipeName(RDBlocks.GENSOKYO_ALTAR));

        // 强化台
        shaped(RecipeCategory.DECORATIONS, RDBlocks.STRENGTH_TABLE)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .define('X', RDItems.SILVER_INGOT)
                .define('#', Items.ENCHANTING_TABLE)
                .unlockedBy("has_silver", has(RDItems.SILVER_INGOT))
                .save(output, getSimpleRecipeName(RDBlocks.STRENGTH_TABLE));
    }

    private void generateOrb() {
        // 宝玉 / 宝玉块
        offerIngotToBlockRecipe(output, RDItems.RED_ORB, RDBlocks.RED_ORB_BLOCK.asItem());
        offerBlockToIngotRecipe(output, RDBlocks.RED_ORB_BLOCK.asItem(), RDItems.RED_ORB);

        offerIngotToBlockRecipe(output, RDItems.YELLOW_ORB, RDBlocks.YELLOW_ORB_BLOCK.asItem());
        offerBlockToIngotRecipe(output, RDBlocks.YELLOW_ORB_BLOCK.asItem(), RDItems.YELLOW_ORB);

        offerIngotToBlockRecipe(output, RDItems.BLUE_ORB, RDBlocks.BLUE_ORB_BLOCK.asItem());
        offerBlockToIngotRecipe(output, RDBlocks.BLUE_ORB_BLOCK.asItem(), RDItems.BLUE_ORB);

        offerIngotToBlockRecipe(output, RDItems.GREEN_ORB, RDBlocks.GREEN_ORB_BLOCK.asItem());
        offerBlockToIngotRecipe(output, RDBlocks.GREEN_ORB_BLOCK.asItem(), RDItems.GREEN_ORB);

        offerIngotToBlockRecipe(output, RDItems.PURPLE_ORB, RDBlocks.PURPLE_ORB_BLOCK.asItem());
        offerBlockToIngotRecipe(output, RDBlocks.PURPLE_ORB_BLOCK.asItem(), RDItems.PURPLE_ORB);

    }

    private void generateSilver() {
        // 银武器/工具
        offerSwordRecipe(output, RDItems.SILVER_SWORD, RDItems.SILVER_INGOT);
        offerPickaxeRecipe(output, RDItems.SILVER_PICKAXE, RDItems.SILVER_INGOT);
        offerAxeRecipe(output, RDItems.SILVER_AXE, RDItems.SILVER_INGOT);
        offerShovelRecipe(output, RDItems.SILVER_SHOVEL, RDItems.SILVER_INGOT);
        offerHoeRecipe(output, RDItems.SILVER_HOE, RDItems.SILVER_INGOT);

        // 银盔甲
        offerHelmetRecipe(output, RDItems.SILVER_HELMET, RDItems.SILVER_INGOT);
        offerChestplateRecipe(output, RDItems.SILVER_CHESTPLATE, RDItems.SILVER_INGOT);
        offerLeggingsRecipe(output, RDItems.SILVER_LEGGINGS, RDItems.SILVER_INGOT);
        offerBootsRecipe(output, RDItems.SILVER_BOOTS, RDItems.SILVER_INGOT);

        // 银粒 / 锭 / 块
        offerIngotToBlockRecipe(output, RDItems.SILVER_NUGGET, RDItems.SILVER_INGOT);
        offerBlockToIngotRecipe(output, RDItems.SILVER_INGOT, RDItems.SILVER_NUGGET);
        offerIngotToBlockRecipe(output, RDItems.SILVER_INGOT, RDBlocks.SILVER_BLOCK.asItem());
        offerBlockToIngotRecipe(output, RDBlocks.SILVER_BLOCK.asItem(), RDItems.SILVER_INGOT);

        shaped(RecipeCategory.DECORATIONS, RDBlocks.SILVER_CHEST_BLOCK.chestBlock())
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .define('X', RDItems.SILVER_INGOT)
                .define('#', Items.CHEST)
                .unlockedBy("has_silver", has(RDItems.SILVER_INGOT))
                .save(output, getSimpleRecipeName(RDBlocks.SILVER_CHEST_BLOCK.chestBlock()));

        // 烧银矿
        oreSmelting(SILVER, RecipeCategory.MISC, RDItems.SILVER_INGOT, 0.7F, 250, "silver_ingot");
        oreBlasting(SILVER, RecipeCategory.MISC, RDItems.SILVER_INGOT, 0.7F, 250, "silver_ingot");

    }

    private void generateMaid() {
        Map<Item, Item> itemItemMap = new HashMap<>(
                Map.of(
                        RDItems.SILVER_HELMET, RDItems.MAID_HAIRBAND,
                        RDItems.SILVER_CHESTPLATE, RDItems.MAID_UPPER_SKIRT,
                        RDItems.SILVER_LEGGINGS, RDItems.MAID_LOWER_SKIRT,
                        RDItems.SILVER_BOOTS, RDItems.MAID_SHOE
                )
        );
        for (Map.Entry<Item, Item> itemItemEntry : itemItemMap.entrySet()) {
            Item left = itemItemEntry.getKey();
            Item right = itemItemEntry.getValue();
            shaped(RecipeCategory.REDSTONE, right)
                    .pattern("XXX")
                    .pattern("X#X")
                    .pattern("XXX")
                    .define('X', ItemTags.WOOL)
                    .define('#', left)
                    .unlockedBy("has_wool", has(ItemTags.WOOL))
                    .save(output, getSimpleRecipeName(right));
        }
    }

    private void generateMagicIce() {
        oreSmelting(List.of(RDBlocks.MAGIC_ICE_BLOCK.asItem()), RecipeCategory.MISC, RDItems.ICE_SCALES, 0.7F, 140, "silver_ingot");
        oreBlasting(List.of(RDBlocks.MAGIC_ICE_BLOCK.asItem()), RecipeCategory.MISC, RDItems.ICE_SCALES, 0.7F, 70, "silver_ingot");
        // 魔法冰
        shaped(RecipeCategory.DECORATIONS, RDBlocks.MAGIC_ICE_BLOCK)
                .pattern("XX")
                .pattern("XX")
                .define('X', Items.ICE)
                .unlockedBy("has_ice", has(Items.ICE))
                .save(output, getSimpleRecipeName(RDBlocks.MAGIC_ICE_BLOCK));

        // 冰武器/工具
        offerSwordRecipe(output, RDItems.MAGIC_ICE_SWORD, RDItems.ICE_SCALES);
        offerPickaxeRecipe(output, RDItems.MAGIC_ICE_PICKAXE, RDItems.ICE_SCALES);
        offerAxeRecipe(output, RDItems.MAGIC_ICE_AXE, RDItems.ICE_SCALES);
        offerShovelRecipe(output, RDItems.MAGIC_ICE_SHOVEL, RDItems.ICE_SCALES);
        offerHoeRecipe(output, RDItems.MAGIC_ICE_HOE, RDItems.ICE_SCALES);

        // 冰盔甲
        offerHelmetRecipe(output, RDItems.MAGIC_ICE_HELMET, RDBlocks.MAGIC_ICE_BLOCK.asItem());
        offerChestplateRecipe(output, RDItems.MAGIC_ICE_CHESTPLATE, RDBlocks.MAGIC_ICE_BLOCK.asItem());
        offerLeggingsRecipe(output, RDItems.MAGIC_ICE_LEGGINGS, RDBlocks.MAGIC_ICE_BLOCK.asItem());
        offerBootsRecipe(output, RDItems.MAGIC_ICE_BOOTS, RDBlocks.MAGIC_ICE_BLOCK.asItem());

    }

    private void generateDream() {
        // 梦境水晶武器/工具
        offerSwordRecipe(output, RDItems.DREAM_SWORD, RDItems.DREAM_CRYSTAL_FRAGMENT);
        offerPickaxeRecipe(output, RDItems.DREAM_PICKAXE, RDItems.DREAM_CRYSTAL_FRAGMENT);
        offerAxeRecipe(output, RDItems.DREAM_AXE, RDItems.DREAM_CRYSTAL_FRAGMENT);
        offerShovelRecipe(output, RDItems.DREAM_SHOVEL, RDItems.DREAM_CRYSTAL_FRAGMENT);
        offerHoeRecipe(output, RDItems.DREAM_HOE, RDItems.DREAM_CRYSTAL_FRAGMENT);

        // 梦境水晶盔甲
        offerHelmetRecipe(output, RDItems.DREAM_HELMET, RDItems.DREAM_CRYSTAL_FRAGMENT);
        offerChestplateRecipe(output, RDItems.DREAM_CHESTPLATE, RDItems.DREAM_CRYSTAL_FRAGMENT);
        offerLeggingsRecipe(output, RDItems.DREAM_LEGGINGS, RDItems.DREAM_CRYSTAL_FRAGMENT);
        offerBootsRecipe(output, RDItems.DREAM_BOOTS, RDItems.DREAM_CRYSTAL_FRAGMENT);

        // 防水衣
        shaped(RecipeCategory.MISC, RDItems.WATERPROOF_LEATHER)
                .define('X', Items.LEATHER)
                .define('Y', Items.TURTLE_SCUTE)
                .define('R', Items.REDSTONE)
                .define('E', Items.HONEYCOMB)
                .unlockedBy("has_leather", has(Items.LEATHER))
                .pattern("RYR")
                .pattern("RXR")
                .pattern("RER")
                .save(output);
        offerHelmetRecipe(output, RDItems.WATER_PROOF_HAT, RDItems.WATERPROOF_LEATHER);
        offerChestplateRecipe(output, RDItems.WATER_PROOF_CLOTHING, RDItems.WATERPROOF_LEATHER);
        offerLeggingsRecipe(output, RDItems.WATER_PROOF_LEGGINGS, RDItems.WATERPROOF_LEATHER);
        offerBootsRecipe(output, RDItems.WATER_PROOF_BOOTS, RDItems.WATERPROOF_LEATHER);

        // 烧梦境水晶矿
        oreSmelting(DREAM, RecipeCategory.MISC, RDItems.DREAM_CRYSTAL_FRAGMENT, 0.7F, 250, "dream_ingot");
        oreBlasting(DREAM, RecipeCategory.MISC, RDItems.DREAM_CRYSTAL_FRAGMENT, 0.7F, 250, "dream_ingot");

    }

    private void generateMIPlant2Ingredient() {
        shaped(RecipeCategory.FOOD, RDIngredientItems.UDUMBARA)
                .pattern("#")
                .define('#', RDWoodBlocks.UDUMBARA_FLOWER)
                .unlockedBy("always", has(Items.AIR))
                .save(output, getSimpleRecipeName(RDIngredientItems.UDUMBARA));
        shaped(RecipeCategory.FOOD, RDIngredientItems.TREMELLA)
                .pattern("#")
                .define('#', RDWoodBlocks.TREMELLA)
                .unlockedBy("always", has(Items.AIR))
                .save(output, getSimpleRecipeName(RDIngredientItems.TREMELLA));
    }

    private void generateMICookRecipe() {
        // 厨具
        shaped(RecipeCategory.DECORATIONS, KitchenBlocks.COOKING_POT)
                .pattern(" Y ")
                .pattern("X X")
                .pattern("XXX")
                .define('X', Items.IRON_INGOT)
                .define('Y', Items.IRON_NUGGET)
                .unlockedBy("always", has(Items.AIR))
                .save(output, getSimpleRecipeName(KitchenBlocks.COOKING_POT));

        shaped(RecipeCategory.DECORATIONS, KitchenBlocks.CUTTING_BOARD)
                .pattern(" Y ")
                .pattern("XXX")
                .define('X', Items.OAK_SLAB)
                .define('Y', Items.IRON_SWORD)
                .unlockedBy("always", has(Items.AIR))
                .save(output, getSimpleRecipeName(KitchenBlocks.CUTTING_BOARD));

        shaped(RecipeCategory.DECORATIONS, KitchenBlocks.FRYING_PAN)
                .pattern(" XX")
                .pattern(" XX")
                .pattern("Y  ")
                .define('X', Items.IRON_INGOT)
                .define('Y', Items.IRON_NUGGET)
                .unlockedBy("always", has(Items.AIR))
                .save(output, getSimpleRecipeName(KitchenBlocks.FRYING_PAN));

        shaped(RecipeCategory.DECORATIONS, KitchenBlocks.GRILL)
                .pattern("YYY")
                .pattern("X X")
                .pattern("XXX")
                .define('X', Items.IRON_INGOT)
                .define('Y', Items.IRON_NUGGET)
                .unlockedBy("always", has(Items.AIR))
                .save(output, getSimpleRecipeName(KitchenBlocks.GRILL));

        shaped(RecipeCategory.DECORATIONS, KitchenBlocks.STEAMER)
                .pattern("YYY")
                .pattern("X X")
                .pattern("XXX")
                .define('X', Items.IRON_INGOT)
                .define('Y', Items.OAK_SLAB)
                .unlockedBy("always", has(Items.AIR))
                .save(output, getSimpleRecipeName(KitchenBlocks.STEAMER));
        shaped(RecipeCategory.DECORATIONS, RDBlocks.ITEM_DISPLAY)
                .pattern("YXY")
                .pattern(" Y ")
                .define('X', Items.ITEM_FRAME)
                .define('Y', Items.QUARTZ)
                .unlockedBy("always", has(Items.AIR))
                .save(output, getSimpleRecipeName(RDBlocks.ITEM_DISPLAY));

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
        mystiaUpgrade.put(KitchenBlocks.COOKING_POT, KitchenBlocks.MYSTIA_COOKING_POT);
        mystiaUpgrade.put(KitchenBlocks.CUTTING_BOARD, KitchenBlocks.MYSTIA_CUTTING_BOARD);
        mystiaUpgrade.put(KitchenBlocks.FRYING_PAN, KitchenBlocks.MYSTIA_FRYING_PAN);
        mystiaUpgrade.put(KitchenBlocks.GRILL, KitchenBlocks.MYSTIA_GRILL);
        mystiaUpgrade.put(KitchenBlocks.STEAMER, KitchenBlocks.MYSTIA_STEAMER);
        this.offerUpgradeRecipes(mystiaUpgrade, Items.FEATHER);

        Map<Block, Block> superUpgrade = new Object2ObjectOpenHashMap<>();
        superUpgrade.put(KitchenBlocks.COOKING_POT, KitchenBlocks.SUPER_COOKING_POT);
        superUpgrade.put(KitchenBlocks.CUTTING_BOARD, KitchenBlocks.SUPER_CUTTING_BOARD);
        superUpgrade.put(KitchenBlocks.FRYING_PAN, KitchenBlocks.SUPER_FRYING_PAN);
        superUpgrade.put(KitchenBlocks.GRILL, KitchenBlocks.SUPER_GRILL);
        superUpgrade.put(KitchenBlocks.STEAMER, KitchenBlocks.SUPER_STEAMER);
        this.offerUpgradeRecipes(superUpgrade, Items.GOLD_INGOT);

        Map<Block, Block> extremeUpgrade = new Object2ObjectOpenHashMap<>();
        extremeUpgrade.put(KitchenBlocks.COOKING_POT, KitchenBlocks.EXTREME_COOKING_POT);
        extremeUpgrade.put(KitchenBlocks.CUTTING_BOARD, KitchenBlocks.EXTREME_CUTTING_BOARD);
        extremeUpgrade.put(KitchenBlocks.FRYING_PAN, KitchenBlocks.EXTREME_FRYING_PAN);
        extremeUpgrade.put(KitchenBlocks.GRILL, KitchenBlocks.EXTREME_GRILL);
        extremeUpgrade.put(KitchenBlocks.STEAMER, KitchenBlocks.EXTREME_STEAMER);
        this.offerUpgradeRecipes(extremeUpgrade, Items.DIAMOND);

        Map<Block, Block> nukeUpgrade = new Object2ObjectOpenHashMap<>();
        nukeUpgrade.put(KitchenBlocks.COOKING_POT, KitchenBlocks.NUKE_COOKING_POT);
        nukeUpgrade.put(KitchenBlocks.CUTTING_BOARD, KitchenBlocks.NUKE_CUTTING_BOARD);
        nukeUpgrade.put(KitchenBlocks.FRYING_PAN, KitchenBlocks.NUKE_FRYING_PAN);
        nukeUpgrade.put(KitchenBlocks.GRILL, KitchenBlocks.NUKE_GRILL);
        nukeUpgrade.put(KitchenBlocks.STEAMER, KitchenBlocks.NUKE_STEAMER);
        this.offerUpgradeRecipes(nukeUpgrade, Items.NETHER_BRICKS);


    }

    private void offerUpgradeRecipes(Map<Block, Block> blockBlockMap, Item upgradeMaterial) {
        Set<Map.Entry<Block, Block>> entries = blockBlockMap.entrySet();
        for (Map.Entry<Block, Block> entry : entries) {
            Block key = entry.getKey();
            Block value = entry.getValue();
            shaped(RecipeCategory.DECORATIONS, value)
                    .pattern("YYY")
                    .pattern("YXY")
                    .pattern("YYY")
                    .define('X', key)
                    .define('Y', upgradeMaterial)
                    .unlockedBy("always", has(Items.AIR))
                    .save(output, getSimpleRecipeName(value));
        }
    }

    private void offerSwordRecipe(RecipeOutput exporter, Item result, Item ingot) {
        shaped(RecipeCategory.COMBAT, result)
                .pattern("X")
                .pattern("X")
                .pattern("#")
                .define('X', ingot)
                .define('#', Items.STICK)
                .unlockedBy("has_ingot", has(ingot))
                .save(exporter, getSimpleRecipeName(result));
    }

    private void offerPickaxeRecipe(RecipeOutput exporter, Item result, Item ingot) {
        shaped(RecipeCategory.TOOLS, result)
                .pattern("XXX")
                .pattern(" # ")
                .pattern(" # ")
                .define('X', ingot)
                .define('#', Items.STICK)
                .unlockedBy("has_ingot", has(ingot))
                .save(exporter, getSimpleRecipeName(result));
    }

    private void offerAxeRecipe(RecipeOutput exporter, Item result, Item ingot) {
        shaped(RecipeCategory.TOOLS, result)
                .pattern("XX")
                .pattern("X#")
                .pattern(" #")
                .define('X', ingot)
                .define('#', Items.STICK)
                .unlockedBy("has_ingot", has(ingot))
                .save(exporter, getSimpleRecipeName(result));
    }

    private void offerShovelRecipe(RecipeOutput exporter, Item result, Item ingot) {
        shaped(RecipeCategory.TOOLS, result)
                .pattern("X")
                .pattern("#")
                .pattern("#")
                .define('X', ingot)
                .define('#', Items.STICK)
                .unlockedBy("has_ingot", has(ingot))
                .save(exporter, getSimpleRecipeName(result));
    }

    private void offerHoeRecipe(RecipeOutput exporter, Item result, Item ingot) {
        shaped(RecipeCategory.TOOLS, result)
                .pattern("XX")
                .pattern(" #")
                .pattern(" #")
                .define('X', ingot)
                .define('#', Items.STICK)
                .unlockedBy("has_ingot", has(ingot))
                .save(exporter, getSimpleRecipeName(result));
    }

    private void offerHelmetRecipe(RecipeOutput exporter, Item result, Item ingot) {
        shaped(RecipeCategory.COMBAT, result)
                .pattern("XXX")
                .pattern("X X")
                .define('X', ingot)
                .unlockedBy("has_ingot", has(ingot))
                .save(exporter, getSimpleRecipeName(result));
    }

    private void offerChestplateRecipe(RecipeOutput exporter, Item result, Item ingot) {
        shaped(RecipeCategory.COMBAT, result)
                .pattern("X X")
                .pattern("XXX")
                .pattern("XXX")
                .define('X', ingot)
                .unlockedBy("has_ingot", has(ingot))
                .save(exporter, getSimpleRecipeName(result));
    }

    private void offerLeggingsRecipe(RecipeOutput exporter, Item result, Item ingot) {
        shaped(RecipeCategory.COMBAT, result)
                .pattern("XXX")
                .pattern("X X")
                .pattern("X X")
                .define('X', ingot)
                .unlockedBy("has_ingot", has(ingot))
                .save(exporter, getSimpleRecipeName(result));
    }

    private void offerBootsRecipe(RecipeOutput exporter, Item result, Item ingot) {
        shaped(RecipeCategory.COMBAT, result)
                .pattern("X X")
                .pattern("X X")
                .define('X', ingot)
                .unlockedBy("has_ingot", has(ingot))
                .save(exporter, getSimpleRecipeName(result));
    }

    private void offer4To1Recipe(RecipeOutput exporter, Item input, Item export) {
        Identifier id = BuiltInRegistries.ITEM.getKey(input);
        shaped(RecipeCategory.BUILDING_BLOCKS, export)
                .pattern("XX")
                .pattern("XX")
                .define('X', input)
                .unlockedBy("has_" + id.getPath(), has(input))
                .save(exporter, getSimpleRecipeName(export));
    }

    private void offer1To4Recipe(RecipeOutput exporter, Item input, Item export) {
        Identifier id = BuiltInRegistries.ITEM.getKey(input);
        shaped(RecipeCategory.BUILDING_BLOCKS, export, 4)
                .pattern("X")
                .define('X', input)
                .unlockedBy("has_" + id.getPath(), has(input))
                .save(exporter, getSimpleRecipeName(export));
    }

    private void offerIngotToBlockRecipe(RecipeOutput exporter, Item ingot, Item block) {
        shaped(RecipeCategory.BUILDING_BLOCKS, block)
                .pattern("XXX")
                .pattern("XXX")
                .pattern("XXX")
                .define('X', ingot)
                .unlockedBy("has_ingot", has(ingot))
                .save(exporter, getSimpleRecipeName(block));
    }

    private void offerBlockToIngotRecipe(RecipeOutput exporter, Item block, Item ingot) {
        shapeless(RecipeCategory.MISC, ingot, 9)
                .requires(block)
                .unlockedBy("has_block", has(block))
                .save(exporter, getSimpleRecipeName(ingot) + "_from_block");
    }

    public ShapedStackRecipeBuilder shapedItemStack(RecipeCategory recipeCategory, ItemStack itemStack) {
        return ShapedStackRecipeBuilder.shaped(((RecipeProviderAccessor) this).reverie_dreams$getItems(), recipeCategory, itemStack);
    }

    public ShapedStackRecipeBuilder shapedItemStack(RecipeCategory recipeCategory, ItemStack itemStack, int i) {
        itemStack.setCount(i);
        return ShapedStackRecipeBuilder.shaped(((RecipeProviderAccessor) this).reverie_dreams$getItems(), recipeCategory, itemStack);
    }
}
