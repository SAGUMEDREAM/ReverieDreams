package cc.thonly.reverie_dreams.datagen;

import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.block.WoodCreator;
import cc.thonly.reverie_dreams.item.ModItems;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public class ModRecipeGenerator extends RecipeProvider {
    public static ImmutableList<ItemLike> SILVER = ImmutableList.of(ModBlocks.SILVER_ORE.asItem(), ModBlocks.DEEPSLATE_SILVER_ORE.asItem(), ModItems.RAW_SILVER);
    public static ImmutableList<ItemLike> DREAM = ImmutableList.of(ModBlocks.DREAM_CRYSTAL_ORE.asItem());

    protected ModRecipeGenerator(HolderLookup.Provider registries, RecipeOutput exporter) {
        super(registries, exporter);
    }

    @Override
    public void buildRecipes() {
        // 入门书
        shaped(RecipeCategory.MISC, ModItems.TOUHOU_HELPER)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .define('X', ModItems.POWER)
                .define('#', Items.BOOK)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(output, getSimpleRecipeName(ModItems.TOUHOU_HELPER));

        // Point / 块
        offerIngotToBlockRecipe(output, ModItems.POINT, ModBlocks.POINT_BLOCK.asItem());
        offerBlockToIngotRecipe(output, ModBlocks.POINT_BLOCK.asItem(), ModItems.POINT);

        // Power / 块
        offerIngotToBlockRecipe(output, ModItems.POWER, ModBlocks.POWER_BLOCK.asItem());
        offerBlockToIngotRecipe(output, ModBlocks.POWER_BLOCK.asItem(), ModItems.POWER);

        // 速度羽毛
        shaped(RecipeCategory.DECORATIONS, ModItems.SPEED_FEATHER)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .define('X', Items.FEATHER)
                .define('#', Items.DIAMOND)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(output, getSimpleRecipeName(ModItems.SPEED_FEATHER));

        // 梦境枕头
        shaped(RecipeCategory.DECORATIONS, ModItems.DREAM_PILLOW)
                .pattern("XXX")
                .pattern("Y#Y")
                .define('X', Items.PINK_WOOL)
                .define('Y', Items.GOLD_INGOT)
                .define('#', Items.EMERALD)
                .unlockedBy("has_emerald", has(Items.EMERALD))
                .save(output, getSimpleRecipeName(ModItems.DREAM_PILLOW));

        // 残机
        offer1To4Recipe(output, ModItems.UPGRADED_HEALTH, ModItems.UPGRADED_HEALTH_FRAGMENT);
        shaped(RecipeCategory.MISC, ModItems.UPGRADED_HEALTH, 2)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .define('X', ModBlocks.POWER_BLOCK)
                .define('#', ModItems.UPGRADED_HEALTH_FRAGMENT)
                .unlockedBy("has_health_fragment", has(ModItems.UPGRADED_HEALTH_FRAGMENT))
                .save(output, getSimpleRecipeName(ModItems.UPGRADED_HEALTH_FRAGMENT) + "_copy");

        // Bomb
        offer1To4Recipe(output, ModItems.BOMB, ModItems.BOMB_FRAGMENT);
        shaped(RecipeCategory.MISC, ModItems.BOMB_FRAGMENT, 2)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .define('X', ModBlocks.POINT_BLOCK)
                .define('#', ModItems.BOMB_FRAGMENT)
                .unlockedBy("has_bomb_fragment", has(ModItems.BOMB_FRAGMENT))
                .save(output, getSimpleRecipeName(ModItems.BOMB_FRAGMENT) + "_copy");

        // 弹幕创作模板
        shaped(RecipeCategory.MISC, ModItems.DANMAKU_SHAPE_CREATOR)
                .pattern("RRR")
                .pattern("R#R")
                .pattern("RRR")
                .define('R', ModItems.POWER)
                .define('#', Items.WRITABLE_BOOK)
                .unlockedBy("has_power", has(ModItems.POWER))
                .save(output, getSimpleRecipeName(ModItems.DANMAKU_SHAPE_CREATOR));

        // 空白角色卡
        shaped(RecipeCategory.MISC, ModItems.ROLE_CARD)
                .pattern(" R ")
                .pattern("R#R")
                .pattern(" R ")
                .define('R', Items.REDSTONE)
                .define('#', Items.PAPER)
                .unlockedBy("has_paper", has(Items.PAPER))
                .save(output, getSimpleRecipeName(ModItems.ROLE_CARD));

        // 魔理沙的帽子
        shaped(RecipeCategory.DECORATIONS, ModBlocks.MARISA_HAT_BLOCK)
                .pattern(" X ")
                .pattern("X#X")
                .pattern("XYX")
                .define('X', Items.WHITE_WOOL)
                .define('Y', Items.LEATHER)
                .define('#', Items.BLACK_WOOL)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(output, getSimpleRecipeName(ModBlocks.MARISA_HAT_BLOCK));

        // 塞钱箱
        shaped(RecipeCategory.MISC, ModBlocks.CASH_BOX_BLOCK)
                .pattern("YYY")
                .pattern("X#X")
                .pattern("YYY")
                .define('X', ModBlocks.SPIRITUAL.planks())
                .define('Y', ModBlocks.SPIRITUAL.slab())
                .define('#', Items.CHEST)
                .unlockedBy("has_wool", has(Items.CHEST))
                .save(output, getSimpleRecipeName(ModBlocks.CASH_BOX_BLOCK));

        // 防撞桶
        shaped(RecipeCategory.MISC, ModBlocks.ANTI_COLLISION_BARREL)
                .pattern("YXY")
                .pattern("Z#Z")
                .pattern("YXY")
                .define('X', Items.YELLOW_WOOL)
                .define('Y', Items.RED_WOOL)
                .define('Z', Items.WHITE_WOOL)
                .define('#', Items.IRON_BLOCK)
                .unlockedBy("has_iron_block", has(Items.IRON_BLOCK))
                .save(output, getSimpleRecipeName(ModBlocks.ANTI_COLLISION_BARREL));
        // 轮椅
        shaped(RecipeCategory.MISC, ModBlocks.WHEEL_CHAIR)
                .pattern("Z  ")
                .pattern("X# ")
                .pattern("YYY")
                .define('#', Items.IRON_INGOT)
                .define('X', Items.IRON_BLOCK)
                .define('Y', Items.RESIN_BRICK)
                .define('Z', Items.BLACK_WOOL)
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(output, getSimpleRecipeName(ModBlocks.WHEEL_CHAIR));

        // 木板箱
        shaped(RecipeCategory.MISC, ModBlocks.WOODEN_BOX.chestBlock())
                .pattern("YZY")
                .pattern("X#X")
                .pattern("YZY")
                .define('#', Items.CHEST)
                .define('X', Items.OAK_SLAB)
                .define('Y', Items.OAK_PLANKS)
                .define('Z', Items.STICK)
                .unlockedBy("has_stick", has(Items.STICK))
                .save(output, getSimpleRecipeName(ModBlocks.WOODEN_BOX.chestBlock()));

        // Fumo销售许可
        shaped(RecipeCategory.MISC, ModItems.FUMO_LICENSE)
                .pattern("YXY")
                .pattern("X#X")
                .pattern("YXY")
                .define('X', Items.WHITE_WOOL)
                .define('Y', Items.DIAMOND)
                .define('#', Items.PAPER)
                .unlockedBy("has_wool", has(Items.WHITE_WOOL))
                .save(output, getSimpleRecipeName(ModItems.FUMO_LICENSE));

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
        shaped(RecipeCategory.MISC, ModItems.DANMAKU_CORE)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .define('#', Items.FIREWORK_STAR)
                .define('X', Items.IRON_NUGGET)
                .unlockedBy("has_sand", has(Items.FIREWORK_STAR))
                .save(output, getSimpleRecipeName(ModItems.DANMAKU_CORE));

        this.generateWoodCreator(ModBlocks.SPIRITUAL);
        this.generateWoodCreator(MIBlocks.LEMON);
        this.generateWoodCreator(MIBlocks.GINKGO);
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
        shaped(RecipeCategory.FOOD, MIItems.CHEESE)
                .pattern("##")
                .pattern("##")
                .define('#', Items.MILK_BUCKET)
                .unlockedBy("has_milk", has(Items.MILK_BUCKET))
                .save(output, getSimpleRecipeName(MIItems.CHEESE));
        shaped(RecipeCategory.FOOD, MIItems.BUTTER)
                .pattern("#")
                .pattern("#")
                .pattern("X")
                .define('#', Items.MILK_BUCKET)
                .define('X', Items.BOWL)
                .unlockedBy("has_milk", has(MIItems.FLOUR))
                .save(output, getSimpleRecipeName(MIItems.BUTTER));
        shaped(RecipeCategory.FOOD, MIItems.FLOUR)
                .pattern("##")
                .pattern("##")
                .define('#', Items.WHEAT)
                .unlockedBy("has_wheat", has(Items.WHEAT))
                .save(output, getSimpleRecipeName(MIItems.FLOUR));
        shaped(RecipeCategory.FOOD, MIItems.TOFU)
                .pattern("##")
                .pattern("##")
                .define('#', MIBlocks.SOY_BEANS.getSeed())
                .unlockedBy("has_soy_beans", has(MIBlocks.SOY_BEANS.getSeed()))
                .save(output, getSimpleRecipeName(MIItems.TOFU));
        shaped(RecipeCategory.FOOD, MIItems.CAPSAICIN)
                .pattern("#")
                .pattern("X")
                .define('#', MIItems.CHILI)
                .define('X', Items.GLASS_BOTTLE)
                .unlockedBy("has_chili", has(MIItems.CHILI))
                .save(output, getSimpleRecipeName(MIItems.CAPSAICIN));
        shaped(RecipeCategory.FOOD, MIItems.CREAM)
                .pattern("#")
                .pattern("#")
                .pattern("X")
                .define('#', Items.MILK_BUCKET)
                .define('X', Items.GLASS_BOTTLE)
                .unlockedBy("has_chili", has(MIItems.CHILI))
                .save(output, getSimpleRecipeName(MIItems.CREAM));

        oreSmelting(List.of(MIItems.BLACK_PORK, MIItems.WILD_BOAR_MEAT), RecipeCategory.MISC, Items.COOKED_PORKCHOP, 0.7F, 160, "food");

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
        shaped(RecipeCategory.REDSTONE, ModBlocks.MUSIC_BLOCK)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .define('X', Items.EMERALD)
                .define('#', Items.NOTE_BLOCK)
                .unlockedBy("has_emerald", has(Items.EMERALD))
                .save(output, getSimpleRecipeName(ModBlocks.MUSIC_BLOCK));
        // 小提琴
        shaped(RecipeCategory.TOOLS, ModItems.VIOLIN)
                .pattern(" X ")
                .pattern("XYX")
                .pattern("X#X")
                .define('X', Items.SPRUCE_PLANKS)
                .define('Y', Items.STRING)
                .define('#', ModBlocks.MUSIC_BLOCK)
                .unlockedBy("has_music_block", has(ModBlocks.MUSIC_BLOCK))
                .save(output, getSimpleRecipeName(ModItems.VIOLIN));
        // 键盘
        shaped(RecipeCategory.TOOLS, ModItems.KEYBOARD)
                .pattern("XYX")
                .pattern("YXY")
                .pattern("Z#W")
                .define('X', Items.BLACK_WOOL)
                .define('Y', Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
                .define('Z', Items.STONE_BUTTON)
                .define('W', Items.REDSTONE)
                .define('#', ModBlocks.MUSIC_BLOCK)
                .unlockedBy("has_music_block", has(ModBlocks.MUSIC_BLOCK))
                .save(output, getSimpleRecipeName(ModItems.KEYBOARD));
        // 小号
        shaped(RecipeCategory.TOOLS, ModItems.TRUMPET)
                .pattern("XXX")
                .pattern("XY#")
                .pattern("ZYX")
                .define('X', Items.GOLD_INGOT)
                .define('Y', Items.STONE_BUTTON)
                .define('Z', Items.REDSTONE)
                .define('#', ModBlocks.MUSIC_BLOCK)
                .unlockedBy("has_music_block", has(ModBlocks.MUSIC_BLOCK))
                .save(output, getSimpleRecipeName(ModItems.TRUMPET));
    }

    private void generateWorkBlock() {
        // 弹幕工作台
        shaped(RecipeCategory.DECORATIONS, ModBlocks.DANMAKU_CRAFTING_TABLE)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .define('X', Items.REDSTONE)
                .define('#', Items.CRAFTING_TABLE)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(output, getSimpleRecipeName(ModBlocks.DANMAKU_CRAFTING_TABLE));

        // 幻想乡祭坛
        shaped(RecipeCategory.DECORATIONS, ModBlocks.GENSOKYO_ALTAR)
                .pattern("X")
                .pattern("#")
                .define('X', Items.EMERALD_BLOCK)
                .define('#', Items.ENCHANTING_TABLE)
                .unlockedBy("has_emerald", has(Items.EMERALD_BLOCK))
                .save(output, getSimpleRecipeName(ModBlocks.GENSOKYO_ALTAR));

        // 强化台
        shaped(RecipeCategory.DECORATIONS, ModBlocks.STRENGTH_TABLE)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .define('X', ModItems.SILVER_INGOT)
                .define('#', Items.ENCHANTING_TABLE)
                .unlockedBy("has_silver", has(ModItems.SILVER_INGOT))
                .save(output, getSimpleRecipeName(ModBlocks.STRENGTH_TABLE));
    }

    private void generateOrb() {
        // 宝玉 / 宝玉块
        offerIngotToBlockRecipe(output, ModItems.RED_ORB, ModBlocks.RED_ORB_BLOCK.asItem());
        offerBlockToIngotRecipe(output, ModBlocks.RED_ORB_BLOCK.asItem(), ModItems.RED_ORB);

        offerIngotToBlockRecipe(output, ModItems.YELLOW_ORB, ModBlocks.YELLOW_ORB_BLOCK.asItem());
        offerBlockToIngotRecipe(output, ModBlocks.YELLOW_ORB_BLOCK.asItem(), ModItems.YELLOW_ORB);

        offerIngotToBlockRecipe(output, ModItems.BLUE_ORB, ModBlocks.BLUE_ORB_BLOCK.asItem());
        offerBlockToIngotRecipe(output, ModBlocks.BLUE_ORB_BLOCK.asItem(), ModItems.BLUE_ORB);

        offerIngotToBlockRecipe(output, ModItems.GREEN_ORB, ModBlocks.GREEN_ORB_BLOCK.asItem());
        offerBlockToIngotRecipe(output, ModBlocks.GREEN_ORB_BLOCK.asItem(), ModItems.GREEN_ORB);

        offerIngotToBlockRecipe(output, ModItems.PURPLE_ORB, ModBlocks.PURPLE_ORB_BLOCK.asItem());
        offerBlockToIngotRecipe(output, ModBlocks.PURPLE_ORB_BLOCK.asItem(), ModItems.PURPLE_ORB);

    }

    private void generateSilver() {
        // 银武器/工具
        offerSwordRecipe(output, ModItems.SILVER_SWORD, ModItems.SILVER_INGOT);
        offerPickaxeRecipe(output, ModItems.SILVER_PICKAXE, ModItems.SILVER_INGOT);
        offerAxeRecipe(output, ModItems.SILVER_AXE, ModItems.SILVER_INGOT);
        offerShovelRecipe(output, ModItems.SILVER_SHOVEL, ModItems.SILVER_INGOT);
        offerHoeRecipe(output, ModItems.SILVER_HOE, ModItems.SILVER_INGOT);

        // 银盔甲
        offerHelmetRecipe(output, ModItems.SILVER_HELMET, ModItems.SILVER_INGOT);
        offerChestplateRecipe(output, ModItems.SILVER_CHESTPLATE, ModItems.SILVER_INGOT);
        offerLeggingsRecipe(output, ModItems.SILVER_LEGGINGS, ModItems.SILVER_INGOT);
        offerBootsRecipe(output, ModItems.SILVER_BOOTS, ModItems.SILVER_INGOT);

        // 银粒 / 锭 / 块
        offerIngotToBlockRecipe(output, ModItems.SILVER_NUGGET, ModItems.SILVER_INGOT);
        offerBlockToIngotRecipe(output, ModItems.SILVER_INGOT, ModItems.SILVER_NUGGET);
        offerIngotToBlockRecipe(output, ModItems.SILVER_INGOT, ModBlocks.SILVER_BLOCK.asItem());
        offerBlockToIngotRecipe(output, ModBlocks.SILVER_BLOCK.asItem(), ModItems.SILVER_INGOT);

        shaped(RecipeCategory.DECORATIONS, ModBlocks.SILVER_CHEST_BLOCK.chestBlock())
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .define('X', ModItems.SILVER_INGOT)
                .define('#', Items.CHEST)
                .unlockedBy("has_silver", has(ModItems.SILVER_INGOT))
                .save(output, getSimpleRecipeName(ModBlocks.SILVER_CHEST_BLOCK.chestBlock()));

        // 烧银矿
        oreSmelting(SILVER, RecipeCategory.MISC, ModItems.SILVER_INGOT, 0.7F, 250, "silver_ingot");
        oreBlasting(SILVER, RecipeCategory.MISC, ModItems.SILVER_INGOT, 0.7F, 250, "silver_ingot");

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
        oreSmelting(List.of(ModBlocks.MAGIC_ICE_BLOCK.asItem()), RecipeCategory.MISC, ModItems.ICE_SCALES, 0.7F, 140, "silver_ingot");
        oreBlasting(List.of(ModBlocks.MAGIC_ICE_BLOCK.asItem()), RecipeCategory.MISC, ModItems.ICE_SCALES, 0.7F, 70, "silver_ingot");
        // 魔法冰
        shaped(RecipeCategory.DECORATIONS, ModBlocks.MAGIC_ICE_BLOCK)
                .pattern("XX")
                .pattern("XX")
                .define('X', Items.ICE)
                .unlockedBy("has_ice", has(Items.ICE))
                .save(output, getSimpleRecipeName(ModBlocks.MAGIC_ICE_BLOCK));

        // 冰武器/工具
        offerSwordRecipe(output, ModItems.MAGIC_ICE_SWORD, ModItems.ICE_SCALES);
        offerPickaxeRecipe(output, ModItems.MAGIC_ICE_PICKAXE, ModItems.ICE_SCALES);
        offerAxeRecipe(output, ModItems.MAGIC_ICE_AXE, ModItems.ICE_SCALES);
        offerShovelRecipe(output, ModItems.MAGIC_ICE_SHOVEL, ModItems.ICE_SCALES);
        offerHoeRecipe(output, ModItems.MAGIC_ICE_HOE, ModItems.ICE_SCALES);

        // 冰盔甲
        offerHelmetRecipe(output, ModItems.MAGIC_ICE_HELMET, ModBlocks.MAGIC_ICE_BLOCK.asItem());
        offerChestplateRecipe(output, ModItems.MAGIC_ICE_CHESTPLATE, ModBlocks.MAGIC_ICE_BLOCK.asItem());
        offerLeggingsRecipe(output, ModItems.MAGIC_ICE_LEGGINGS, ModBlocks.MAGIC_ICE_BLOCK.asItem());
        offerBootsRecipe(output, ModItems.MAGIC_ICE_BOOTS, ModBlocks.MAGIC_ICE_BLOCK.asItem());

    }

    private void generateDream() {
        // 梦境水晶武器/工具
        offerSwordRecipe(output, ModItems.DREAM_SWORD, ModItems.DREAM_CRYSTAL_FRAGMENT);
        offerPickaxeRecipe(output, ModItems.DREAM_PICKAXE, ModItems.DREAM_CRYSTAL_FRAGMENT);
        offerAxeRecipe(output, ModItems.DREAM_AXE, ModItems.DREAM_CRYSTAL_FRAGMENT);
        offerShovelRecipe(output, ModItems.DREAM_SHOVEL, ModItems.DREAM_CRYSTAL_FRAGMENT);
        offerHoeRecipe(output, ModItems.DREAM_HOE, ModItems.DREAM_CRYSTAL_FRAGMENT);

        // 梦境水晶盔甲
        offerHelmetRecipe(output, ModItems.DREAM_HELMET, ModItems.DREAM_CRYSTAL_FRAGMENT);
        offerChestplateRecipe(output, ModItems.DREAM_CHESTPLATE, ModItems.DREAM_CRYSTAL_FRAGMENT);
        offerLeggingsRecipe(output, ModItems.DREAM_LEGGINGS, ModItems.DREAM_CRYSTAL_FRAGMENT);
        offerBootsRecipe(output, ModItems.DREAM_BOOTS, ModItems.DREAM_CRYSTAL_FRAGMENT);

        // 烧梦境水晶矿
        oreSmelting(DREAM, RecipeCategory.MISC, ModItems.DREAM_CRYSTAL_FRAGMENT, 0.7F, 250, "dream_ingot");
        oreBlasting(DREAM, RecipeCategory.MISC, ModItems.DREAM_CRYSTAL_FRAGMENT, 0.7F, 250, "dream_ingot");

    }

    private void generateMIPlant2Ingredient() {
        shaped(RecipeCategory.FOOD, MIItems.UDUMBARA)
                .pattern("#")
                .define('#', MIBlocks.UDUMBARA_FLOWER)
                .unlockedBy("always", has(Items.AIR))
                .save(output, getSimpleRecipeName(MIItems.UDUMBARA));
        shaped(RecipeCategory.FOOD, MIItems.TREMELLA)
                .pattern("#")
                .define('#', MIBlocks.TREMELLA)
                .unlockedBy("always", has(Items.AIR))
                .save(output, getSimpleRecipeName(MIItems.TREMELLA));
    }

    private void generateMICookRecipe() {
        // 厨具
        shaped(RecipeCategory.DECORATIONS, MIBlocks.COOKING_POT)
                .pattern(" Y ")
                .pattern("X X")
                .pattern("XXX")
                .define('X', Items.IRON_INGOT)
                .define('Y', Items.IRON_NUGGET)
                .unlockedBy("always", has(Items.AIR))
                .save(output, getSimpleRecipeName(MIBlocks.COOKING_POT));

        shaped(RecipeCategory.DECORATIONS, MIBlocks.CUTTING_BOARD)
                .pattern(" Y ")
                .pattern("XXX")
                .define('X', Items.OAK_SLAB)
                .define('Y', Items.IRON_SWORD)
                .unlockedBy("always", has(Items.AIR))
                .save(output, getSimpleRecipeName(MIBlocks.CUTTING_BOARD));

        shaped(RecipeCategory.DECORATIONS, MIBlocks.FRYING_PAN)
                .pattern(" XX")
                .pattern(" XX")
                .pattern("Y  ")
                .define('X', Items.IRON_INGOT)
                .define('Y', Items.IRON_NUGGET)
                .unlockedBy("always", has(Items.AIR))
                .save(output, getSimpleRecipeName(MIBlocks.FRYING_PAN));

        shaped(RecipeCategory.DECORATIONS, MIBlocks.GRILL)
                .pattern("YYY")
                .pattern("X X")
                .pattern("XXX")
                .define('X', Items.IRON_INGOT)
                .define('Y', Items.IRON_NUGGET)
                .unlockedBy("always", has(Items.AIR))
                .save(output, getSimpleRecipeName(MIBlocks.GRILL));

        shaped(RecipeCategory.DECORATIONS, MIBlocks.STEAMER)
                .pattern("YYY")
                .pattern("X X")
                .pattern("XXX")
                .define('X', Items.IRON_INGOT)
                .define('Y', Items.OAK_SLAB)
                .unlockedBy("always", has(Items.AIR))
                .save(output, getSimpleRecipeName(MIBlocks.STEAMER));
        shaped(RecipeCategory.DECORATIONS, MIBlocks.ITEM_DISPLAY)
                .pattern("YXY")
                .pattern(" Y ")
                .define('X', Items.ITEM_FRAME)
                .define('Y', Items.QUARTZ)
                .unlockedBy("always", has(Items.AIR))
                .save(output, getSimpleRecipeName(MIBlocks.ITEM_DISPLAY));

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

        Map<Block, Block> nukeUpgrade = new Object2ObjectOpenHashMap<>();
        nukeUpgrade.put(MIBlocks.COOKING_POT, MIBlocks.NUKE_COOKING_POT);
        nukeUpgrade.put(MIBlocks.CUTTING_BOARD, MIBlocks.NUKE_CUTTING_BOARD);
        nukeUpgrade.put(MIBlocks.FRYING_PAN, MIBlocks.NUKE_FRYING_PAN);
        nukeUpgrade.put(MIBlocks.GRILL, MIBlocks.NUKE_GRILL);
        nukeUpgrade.put(MIBlocks.STEAMER, MIBlocks.NUKE_STEAMER);
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
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(input);
        shaped(RecipeCategory.BUILDING_BLOCKS, export)
                .pattern("XX")
                .pattern("XX")
                .define('X', input)
                .unlockedBy("has_" + id.getPath(), has(input))
                .save(exporter, getSimpleRecipeName(export));
    }

    private void offer1To4Recipe(RecipeOutput exporter, Item input, Item export) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(input);
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


}
