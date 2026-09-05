package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.block.bundle.DecorativeBlockBundle;
import cc.thonly.reverie_dreams.block.bundle.WoodBundle;
import cc.thonly.reverie_dreams.fabric.datagen.entry.ShapedStackRecipeBuilder;
import cc.thonly.reverie_dreams.mixin.accessor.RecipeProviderAccessor;
import cc.thonly.reverie_dreams.registry.content.RDEnchantments;
import cc.thonly.reverie_dreams.registry.content.block.RDKitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDCropBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.util.item.ItemStackTemplateHelper;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import cc.thonly.keine.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImplRecipeGenerator extends RecipeProvider {
    public static final ImmutableList<ItemLike> SILVER = ImmutableList.of(RDBlocks.SILVER_ORE.asItem(), RDBlocks.DEEPSLATE_SILVER_ORE.asItem(), RDItems.RAW_SILVER);
    public static final ImmutableList<ItemLike> MOON_IRON = ImmutableList.of(RDBlocks.MOON_IRON_ORE.asItem());
    public static final ImmutableList<ItemLike> MOON_GOLD = ImmutableList.of(RDBlocks.MOON_GOLD_ORE.asItem());
    public static final ImmutableList<ItemLike> MOON_DIAMOND = ImmutableList.of(RDBlocks.MOON_DIAMOND_ORE.asItem());
    public static final ImmutableList<ItemLike> MOON_QUARTZ = ImmutableList.of(RDBlocks.MOON_QUARTZ_ORE.asItem());
    public static final ImmutableList<ItemLike> DREAM = ImmutableList.of(RDBlocks.DREAM_CRYSTAL_ORE.asItem());

    protected ImplRecipeGenerator(HolderLookup.Provider registries, RecipeOutput exporter) {
        super(registries, exporter);
    }

    @Override
    public void buildRecipes() {
        // 入门书
        shaped(RecipeCategory.MISC, RDItems.GUIDEBOOK)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .define('X', RDItems.POWER)
                .define('#', Items.BOOK)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(output, getSimpleRecipeName(RDItems.GUIDEBOOK));

        // Point / 块
        offerIngotToBlockRecipe(output, RDItems.POINT.asItem(), RDBlocks.POINT_BLOCK.asItem());
        offerBlockToIngotRecipe(output, RDBlocks.POINT_BLOCK.asItem(), RDItems.POINT.asItem());

        // Power / 块
        offerIngotToBlockRecipe(output, RDItems.POWER.asItem(), RDBlocks.POWER_BLOCK.asItem());
        offerBlockToIngotRecipe(output, RDBlocks.POWER_BLOCK.asItem(), RDItems.POWER.asItem());

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
        offer1To4Recipe(output, RDItems.UPGRADED_HEALTH.asItem(), RDItems.UPGRADED_HEALTH_FRAGMENT.asItem());
        offer4To1Recipe(output, RDItems.UPGRADED_HEALTH_FRAGMENT.asItem(), RDItems.UPGRADED_HEALTH.asItem());
        shaped(RecipeCategory.MISC, RDItems.UPGRADED_HEALTH_FRAGMENT, 2)
                .pattern(" X ")
                .pattern("X#X")
                .pattern(" X ")
                .define('X', RDBlocks.POWER_BLOCK)
                .define('#', RDItems.UPGRADED_HEALTH_FRAGMENT)
                .unlockedBy("has_health_fragment", has(RDItems.UPGRADED_HEALTH_FRAGMENT))
                .save(output, getSimpleRecipeName(RDItems.UPGRADED_HEALTH_FRAGMENT) + "_copy");

        // Bomb
        offer1To4Recipe(output, RDItems.BOMB.asItem(), RDItems.BOMB_FRAGMENT.asItem());
        offer4To1Recipe(output, RDItems.BOMB_FRAGMENT.asItem(), RDItems.BOMB.asItem());
        shaped(RecipeCategory.MISC, RDItems.BOMB_FRAGMENT, 2)
                .pattern(" X ")
                .pattern("X#X")
                .pattern(" X ")
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
                .define('X', RDWoodBlocks.SPIRITUAL_BUNDLE.planks())
                .define('Y', RDWoodBlocks.SPIRITUAL_BUNDLE.slab())
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

        // 稻草人
        shaped(RecipeCategory.MISC, RDItems.SCARECROW)
                .pattern("#A#")
                .pattern("#X#")
                .pattern("#A#")
                .define('A', Items.REDSTONE_TORCH)
                .define('X', Items.REDSTONE)
                .define('#', Items.WHEAT)
                .unlockedBy("has_wheat", has(Items.WHEAT))
                .save(output, getSimpleRecipeName(RDItems.SCARECROW));

        // 快速食谱
        shaped(RecipeCategory.MISC, RDItems.FAST_RECIPE_BOOK)
                .pattern("FRP")
                .pattern("RXR")
                .pattern("PRP")
                .define('X', Items.BOOK)
                .define('R', Items.REDSTONE)
                .define('F', Items.FEATHER)
                .define('P', RDItems.POWER)
                .unlockedBy("has_book", has(Items.BOOK))
                .save(output, getSimpleRecipeName(RDItems.FAST_RECIPE_BOOK));
        // 魂符
        shaped(RecipeCategory.MISC, RDItems.SOUL_CARD)
                .pattern("###")
                .pattern("#X#")
                .pattern("###")
                .define('X', Items.PAPER)
                .define('#', Items.SOUL_SAND)
                .unlockedBy("has_paper", has(Items.PAPER))
                .save(output, getSimpleRecipeName(RDItems.SOUL_CARD));
        // 自定义皮肤选择器
        shaped(RecipeCategory.MISC, RDItems.CUSTOM_SKIN_SELECTOR)
                .pattern("###")
                .pattern("#X#")
                .pattern("###")
                .define('X', Items.STICK)
                .define('#', ConventionalItemTags.DYES)
                .unlockedBy("has_dye", has(ConventionalItemTags.DYES))
                .save(output, getSimpleRecipeName(RDItems.CUSTOM_SKIN_SELECTOR));
        // 桌子
        shaped(RecipeCategory.MISC, RDBlocks.TABLE)
                .pattern("###")
                .pattern("A A")
                .pattern("X X")
                .define('#', ItemTags.PLANKS)
                .define('A', ItemTags.WOODEN_SLABS)
                .define('X', Items.STICK)
                .unlockedBy("has_stick", has(Items.STICK))
                .save(output, getSimpleRecipeName(RDBlocks.TABLE));
        // 椅子
        shaped(RecipeCategory.MISC, RDBlocks.CHAIR)
                .pattern("###")
                .pattern("# #")
                .define('#', ItemTags.WOODEN_SLABS)
                .unlockedBy("has_slab", has(ItemTags.WOODEN_SLABS))
                .save(output, getSimpleRecipeName(RDBlocks.CHAIR));
        // 酿酒桶
        shaped(RecipeCategory.DECORATIONS, RDBlocks.BREWING_BARREL)
                .pattern("# #")
                .pattern("#C#")
                .pattern("# #")
                .define('#', ItemTags.WOODEN_SLABS)
                .define('C', Items.BARREL)
                .unlockedBy("has_barrel", has(Items.BARREL))
                .save(output, getSimpleRecipeName(RDBlocks.BREWING_BARREL));
        // 支票
        shaped(RecipeCategory.TOOLS, RDItems.CHEQUE)
                .pattern("  G")
                .pattern("## ")
                .pattern("## ")
                .define('#', Items.PAPER)
                .define('G', RDItems.GOLD_COIN)
                .unlockedBy("has_gold_coin", has(RDItems.GOLD_COIN))
                .save(output, getSimpleRecipeName(RDItems.CHEQUE));
        // 橱柜
        shaped(RecipeCategory.DECORATIONS, RDBlocks.CUPBOARD)
                .pattern("###")
                .pattern("#C#")
                .pattern("#G#")
                .define('#', ItemTags.WOODEN_SLABS)
                .define('C', Items.CHEST)
                .define('G', Items.GLASS)
                .unlockedBy("has_chest", has(Items.CHEST))
                .save(output, getSimpleRecipeName(RDBlocks.CUPBOARD));
        // 制冰机
        shaped(RecipeCategory.DECORATIONS, RDBlocks.ICE_MAKING_MACHINE)
                .pattern("ICI")
                .pattern("IBI")
                .pattern("CWC")
                .define('I', Items.IRON_INGOT)
                .define('C', Items.COPPER_INGOT)
                .define('B', Items.BLUE_ICE)
                .define('W', Items.BUCKET)
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(output, getSimpleRecipeName(RDBlocks.ICE_MAKING_MACHINE));

        this.buildWoodBundle(RDWoodBlocks.SPIRITUAL_BUNDLE);
        this.buildWoodBundle(RDWoodBlocks.LEMON_BUNDLE);
        this.buildWoodBundle(RDWoodBlocks.GINKGO_BUNDLE);
        this.buildWoodBundle(RDWoodBlocks.PEACH_BUNDLE);
        shapeless(RecipeCategory.BUILDING_BLOCKS, RDWoodBlocks.SPIRITUAL_BUNDLE.strippedLog())
                .requires(RDWoodBlocks.BLESSED_SPIRITUAL_LOG)
                .unlockedBy("has_blessed_spiritual_log", has(RDWoodBlocks.BLESSED_SPIRITUAL_LOG))
                .save(output, "cutting_" + getSimpleRecipeName(RDWoodBlocks.BLESSED_SPIRITUAL_LOG));
        shaped(RecipeCategory.BUILDING_BLOCKS, RDWoodBlocks.BLESSED_SPIRITUAL_LOG, 1)
                .pattern("Y")
                .pattern("X")
                .define('Y', Items.PAPER)
                .define('X', RDWoodBlocks.SPIRITUAL_BUNDLE.strippedLog())
                .unlockedBy("has_paper", has(Items.PAPER))
                .save(output, getSimpleRecipeName(RDWoodBlocks.BLESSED_SPIRITUAL_LOG));
        // 附魔桃子
        ItemStackTemplate peachStack = new ItemStackTemplate(RDIngredientItems.PEACH.asItem());
        HolderLookup.RegistryLookup<Enchantment> enchantmentRegistryLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        Holder.Reference<Enchantment> powerful = enchantmentRegistryLookup.getOrThrow(RDEnchantments.POWERFUL);
        ItemStackTemplateHelper.modify(peachStack, (template, modifier) -> modifier.enchant(powerful, 1));
        shapedItemStack(RecipeCategory.MISC, peachStack)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .define('X', Items.GOLD_NUGGET)
                .define('#', RDIngredientItems.PEACH)
                .unlockedBy("has_gold_nugget", has(Items.GOLD_NUGGET))
                .save(output, getSimpleRecipeName(RDIngredientItems.PEACH));
        // 鲜花
        shaped(RecipeCategory.FOOD, RDIngredientItems.FLOWERS, 4)
                .pattern("XXX")
                .pattern("X X")
                .pattern("XXX")
                .define('X', ItemTags.FLOWERS)
                .unlockedBy("has_flowers", has(ItemTags.FLOWERS))
                .save(output, getSimpleRecipeName(RDIngredientItems.FLOWERS));
        this.buildDecorativeBlock();

        this.buildWorkBlock();
        this.buildOrb();
        this.buildSilver();
        this.buildMaid();
        this.buildMagicIce();
        this.buildDream();
        this.buildMusicBlock();
        this.buildIngredient();
        this.buildPlant2Ingredient();
        this.buildBaseKitchenBlockRecipe();
        this.buildKitchenBlockUpgradeRecipes();
        this.buildRedstones();
    }

    private void buildWoodBundle(WoodBundle creator) {
        HolderGetter<Item> itemImpl = this.registries.lookupOrThrow(Registries.ITEM);
        Block log = creator.log().asBlock();
        Block wood = creator.wood().asBlock();
        Block strippedLog = creator.strippedLog().asBlock();
        Block strippedWood = creator.strippedWood().asBlock();
        Block planks = creator.planks().asBlock();
        Block stair = creator.stairs().asBlock();
        Block slab = creator.slab().asBlock();
        Block door = creator.door().asBlock();
        Block trapdoor = creator.trapdoor().asBlock();
        Block fence = creator.fence().asBlock();
        Block fenceGate = creator.fenceGate().asBlock();
        Block button = creator.button().asBlock();

        BlockFamily family = new BlockFamily.Builder(planks)
                .stairs(stair)
                .slab(slab)
                .fence(fence)
                .fenceGate(fenceGate)
                .button(button)
                .door(door)
                .trapdoor(trapdoor)
                .getFamily();
        generateRecipes(family, FeatureFlagSet.of());

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
                              .unlockedBy("has_wood", has(strippedWood))
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

    private void buildIngredient() {
        shaped(RecipeCategory.FOOD, RDIngredientItems.CHEESE, 2)
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

        oreSmelting(List.of(RDIngredientItems.BLACK_PORK, RDIngredientItems.WILD_BOAR_MEAT), RecipeCategory.FOOD, Items.COOKED_PORKCHOP.asItem(), 0.7F, 160, "food");
    }

    private void buildDecorativeBlock() {
        this.offerDecorativeBlockBundleRecipe(RDBlocks.ICE_SCALES, RDItems.ICE_SCALES);
        this.offerDecorativeBlockBundleRecipe(RDBlocks.DREAM_STONE, RDBlocks.DREAM_STONE.block());
        this.offerDecorativeBlockBundleRecipe(RDBlocks.DREAM_STONE_BRICK, RDBlocks.DREAM_STONE_BRICK.block());
        this.offerDecorativeBlockBundleRecipe(RDBlocks.MOON_STONE, RDBlocks.MOON_STONE.block());
        this.offerDecorativeBlockBundleRecipe(RDBlocks.MOON_STONE_BRICK, RDBlocks.MOON_STONE_BRICK.block());
    }

    private void offerDecorativeBlockBundleRecipe(DecorativeBlockBundle bundle, ItemLike material) {
        Identifier id = BuiltInRegistries.ITEM.getKey(material.asItem());
        ItemLike start = bundle.base() == null ? material : bundle.base();
        if (start != bundle.block()) {
            this.shaped(RecipeCategory.DECORATIONS, bundle.block(), 2)
                .pattern("XX")
                .pattern("XX")
                .define('X', start)
                .unlockedBy("has_" + id.getPath(), this.has(start))
                .save(this.output, RecipeProvider.getSimpleRecipeName(bundle.block()));
        }
        this.slab(RecipeCategory.BUILDING_BLOCKS, bundle.slab(), material);
        this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, bundle.slab(), bundle.block(), 2);
        this.stairBuilder(bundle.stair(), Ingredient.of(material))
            .unlockedBy("has_" + id.getPath(), this.has(material))
            .save(this.output, RecipeProvider.getSimpleRecipeName(bundle.stair()));
        this.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, bundle.stair(), bundle.block());
        this.wall(RecipeCategory.BUILDING_BLOCKS, bundle.wall(), material);
    }

    private void buildMusicBlock() {
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

    private void buildWorkBlock() {
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

    private void buildOrb() {
        // 宝玉 / 宝玉块
        offerIngotToBlockRecipe(output, RDItems.RED_ORB.asItem(), RDBlocks.RED_ORB_BLOCK.asItem());
        offerBlockToIngotRecipe(output, RDBlocks.RED_ORB_BLOCK.asItem(), RDItems.RED_ORB.asItem());

        offerIngotToBlockRecipe(output, RDItems.YELLOW_ORB.asItem(), RDBlocks.YELLOW_ORB_BLOCK.asItem());
        offerBlockToIngotRecipe(output, RDBlocks.YELLOW_ORB_BLOCK.asItem(), RDItems.YELLOW_ORB.asItem());

        offerIngotToBlockRecipe(output, RDItems.BLUE_ORB.asItem(), RDBlocks.BLUE_ORB_BLOCK.asItem());
        offerBlockToIngotRecipe(output, RDBlocks.BLUE_ORB_BLOCK.asItem(), RDItems.BLUE_ORB.asItem());

        offerIngotToBlockRecipe(output, RDItems.GREEN_ORB.asItem(), RDBlocks.GREEN_ORB_BLOCK.asItem());
        offerBlockToIngotRecipe(output, RDBlocks.GREEN_ORB_BLOCK.asItem(), RDItems.GREEN_ORB.asItem());

        offerIngotToBlockRecipe(output, RDItems.PURPLE_ORB.asItem(), RDBlocks.PURPLE_ORB_BLOCK.asItem());
        offerBlockToIngotRecipe(output, RDBlocks.PURPLE_ORB_BLOCK.asItem(), RDItems.PURPLE_ORB.asItem());

    }

    private void buildSilver() {
        // 银武器/工具
        offerSwordRecipe(output, RDItems.SILVER_SWORD.asItem(), RDItems.SILVER_INGOT.asItem());
        offerPickaxeRecipe(output, RDItems.SILVER_PICKAXE.asItem(), RDItems.SILVER_INGOT.asItem());
        offerAxeRecipe(output, RDItems.SILVER_AXE.asItem(), RDItems.SILVER_INGOT.asItem());
        offerShovelRecipe(output, RDItems.SILVER_SHOVEL.asItem(), RDItems.SILVER_INGOT.asItem());
        offerHoeRecipe(output, RDItems.SILVER_HOE.asItem(), RDItems.SILVER_INGOT.asItem());
        offerSpearRecipe(output, RDItems.SILVER_SPEAR.asItem(), RDItems.SILVER_INGOT.asItem());

        // 银盔甲
        offerHelmetRecipe(output, RDItems.SILVER_HELMET.asItem(), RDItems.SILVER_INGOT.asItem());
        offerChestplateRecipe(output, RDItems.SILVER_CHESTPLATE.asItem(), RDItems.SILVER_INGOT.asItem());
        offerLeggingsRecipe(output, RDItems.SILVER_LEGGINGS.asItem(), RDItems.SILVER_INGOT.asItem());
        offerBootsRecipe(output, RDItems.SILVER_BOOTS.asItem(), RDItems.SILVER_INGOT.asItem());

        // 银粒 / 锭 / 块
        offerIngotToBlockRecipe(output, RDItems.SILVER_NUGGET.asItem(), RDItems.SILVER_INGOT.asItem());
        offerBlockToIngotRecipe(output, RDItems.SILVER_INGOT.asItem(), RDItems.SILVER_NUGGET.asItem());
        offerIngotToBlockRecipe(output, RDItems.SILVER_INGOT.asItem(), RDBlocks.SILVER_BLOCK.asItem());
        offerBlockToIngotRecipe(output, RDBlocks.SILVER_BLOCK.asItem(), RDItems.SILVER_INGOT.asItem());

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

        oreSmelting(MOON_IRON, RecipeCategory.MISC, Items.IRON_INGOT, 0.7F, 250, "iron_ingot");
        oreBlasting(MOON_IRON, RecipeCategory.MISC, Items.IRON_INGOT, 0.7F, 250, "iron_ingot");

        oreSmelting(MOON_GOLD, RecipeCategory.MISC, Items.GOLD_INGOT, 0.7F, 250, "gold_ingot");
        oreBlasting(MOON_GOLD, RecipeCategory.MISC, Items.GOLD_INGOT, 0.7F, 250, "gold_ingot");

        oreSmelting(MOON_DIAMOND, RecipeCategory.MISC, Items.DIAMOND, 0.7F, 250, "diamond");
        oreBlasting(MOON_DIAMOND, RecipeCategory.MISC, Items.DIAMOND, 0.7F, 250, "diamond");

        oreSmelting(MOON_QUARTZ, RecipeCategory.MISC, Items.QUARTZ, 0.7F, 250, "quartz");
        oreBlasting(MOON_QUARTZ, RecipeCategory.MISC, Items.QUARTZ, 0.7F, 250, "quartz");

    }

    private void buildMaid() {
        Map<Item, Item> itemItemMap = new HashMap<>(
                Map.of(
                        RDItems.SILVER_HELMET.asItem(), RDItems.MAID_HAIRBAND.asItem(),
                        RDItems.SILVER_CHESTPLATE.asItem(), RDItems.MAID_UPPER_SKIRT.asItem(),
                        RDItems.SILVER_LEGGINGS.asItem(), RDItems.MAID_LOWER_SKIRT.asItem(),
                        RDItems.SILVER_BOOTS.asItem(), RDItems.MAID_SHOE.asItem()
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

    private void buildMagicIce() {
        oreSmelting(List.of(RDBlocks.MAGIC_ICE_BLOCK.asItem()), RecipeCategory.MISC, RDItems.ICE_SCALES, 0.7F, 140, "silver_ingot");
        oreBlasting(List.of(RDBlocks.MAGIC_ICE_BLOCK.asItem()), RecipeCategory.MISC, RDItems.ICE_SCALES, 0.7F, 70, "silver_ingot");
        // 魔法冰
        shaped(RecipeCategory.DECORATIONS, RDBlocks.MAGIC_ICE_BLOCK, 8)
                .pattern("XXX")
                .pattern("X#X")
                .pattern("XXX")
                .define('#', RDItems.POWER)
                .define('X', Items.ICE)
                .unlockedBy("has_ice", has(Items.ICE))
                .save(output, getSimpleRecipeName(RDBlocks.MAGIC_ICE_BLOCK));

        // 冰武器/工具
        offerSwordRecipe(output, RDItems.MAGIC_ICE_SWORD.asItem(), RDItems.ICE_SCALES.asItem());
        offerPickaxeRecipe(output, RDItems.MAGIC_ICE_PICKAXE.asItem(), RDItems.ICE_SCALES.asItem());
        offerAxeRecipe(output, RDItems.MAGIC_ICE_AXE.asItem(), RDItems.ICE_SCALES.asItem());
        offerShovelRecipe(output, RDItems.MAGIC_ICE_SHOVEL.asItem(), RDItems.ICE_SCALES.asItem());
        offerHoeRecipe(output, RDItems.MAGIC_ICE_HOE.asItem(), RDItems.ICE_SCALES.asItem());
        offerSpearRecipe(output, RDItems.MAGIC_ICE_SPEAR.asItem(), RDItems.ICE_SCALES.asItem());

        // 冰盔甲
        offerHelmetRecipe(output, RDItems.MAGIC_ICE_HELMET.asItem(), RDBlocks.MAGIC_ICE_BLOCK.asItem());
        offerChestplateRecipe(output, RDItems.MAGIC_ICE_CHESTPLATE.asItem(), RDBlocks.MAGIC_ICE_BLOCK.asItem());
        offerLeggingsRecipe(output, RDItems.MAGIC_ICE_LEGGINGS.asItem(), RDBlocks.MAGIC_ICE_BLOCK.asItem());
        offerBootsRecipe(output, RDItems.MAGIC_ICE_BOOTS.asItem(), RDBlocks.MAGIC_ICE_BLOCK.asItem());

    }

    private void buildDream() {
        // 梦境水晶武器/工具
        offerSwordRecipe(output, RDItems.DREAM_SWORD.asItem(), RDItems.DREAM_CRYSTAL_FRAGMENT.asItem());
        offerPickaxeRecipe(output, RDItems.DREAM_PICKAXE.asItem(), RDItems.DREAM_CRYSTAL_FRAGMENT.asItem());
        offerAxeRecipe(output, RDItems.DREAM_AXE.asItem(), RDItems.DREAM_CRYSTAL_FRAGMENT.asItem());
        offerShovelRecipe(output, RDItems.DREAM_SHOVEL.asItem(), RDItems.DREAM_CRYSTAL_FRAGMENT.asItem());
        offerHoeRecipe(output, RDItems.DREAM_HOE.asItem(), RDItems.DREAM_CRYSTAL_FRAGMENT.asItem());
        offerSpearRecipe(output, RDItems.DREAM_SPEAR.asItem(), RDItems.DREAM_CRYSTAL_FRAGMENT.asItem());

        // 梦境水晶盔甲
        offerHelmetRecipe(output, RDItems.DREAM_HELMET.asItem(), RDItems.DREAM_CRYSTAL_FRAGMENT.asItem());
        offerChestplateRecipe(output, RDItems.DREAM_CHESTPLATE.asItem(), RDItems.DREAM_CRYSTAL_FRAGMENT.asItem());
        offerLeggingsRecipe(output, RDItems.DREAM_LEGGINGS.asItem(), RDItems.DREAM_CRYSTAL_FRAGMENT.asItem());
        offerBootsRecipe(output, RDItems.DREAM_BOOTS.asItem(), RDItems.DREAM_CRYSTAL_FRAGMENT.asItem());

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
        offerHelmetRecipe(output, RDItems.WATER_PROOF_HAT.asItem(), RDItems.WATERPROOF_LEATHER.asItem());
        offerChestplateRecipe(output, RDItems.WATER_PROOF_CLOTHING.asItem(), RDItems.WATERPROOF_LEATHER.asItem());
        offerLeggingsRecipe(output, RDItems.WATER_PROOF_LEGGINGS.asItem(), RDItems.WATERPROOF_LEATHER.asItem());
        offerBootsRecipe(output, RDItems.WATER_PROOF_BOOTS.asItem(), RDItems.WATERPROOF_LEATHER.asItem());

        // 烧梦境水晶矿
        oreSmelting(DREAM, RecipeCategory.MISC, RDItems.DREAM_CRYSTAL_FRAGMENT, 0.7F, 250, "dream_ingot");
        oreBlasting(DREAM, RecipeCategory.MISC, RDItems.DREAM_CRYSTAL_FRAGMENT, 0.7F, 250, "dream_ingot");
    }

    private void buildPlant2Ingredient() {
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

    private void buildBaseKitchenBlockRecipe() {
        // 厨具
        shaped(RecipeCategory.DECORATIONS, RDKitchenBlocks.COOKING_POT)
                .pattern(" Y ")
                .pattern("X X")
                .pattern("XXX")
                .define('X', Items.IRON_INGOT)
                .define('Y', Items.IRON_NUGGET)
                .unlockedBy("always", has(Items.AIR))
                .save(output, getSimpleRecipeName(RDKitchenBlocks.COOKING_POT));

        shaped(RecipeCategory.DECORATIONS, RDKitchenBlocks.CUTTING_BOARD)
                .pattern(" Y ")
                .pattern("XXX")
                .define('X', Items.OAK_SLAB)
                .define('Y', Items.IRON_SWORD)
                .unlockedBy("always", has(Items.AIR))
                .save(output, getSimpleRecipeName(RDKitchenBlocks.CUTTING_BOARD));

        shaped(RecipeCategory.DECORATIONS, RDKitchenBlocks.FRYING_PAN)
                .pattern(" XX")
                .pattern(" XX")
                .pattern("Y  ")
                .define('X', Items.IRON_INGOT)
                .define('Y', Items.IRON_NUGGET)
                .unlockedBy("always", has(Items.AIR))
                .save(output, getSimpleRecipeName(RDKitchenBlocks.FRYING_PAN));

        shaped(RecipeCategory.DECORATIONS, RDKitchenBlocks.GRILL)
                .pattern("YYY")
                .pattern("X X")
                .pattern("XXX")
                .define('X', Items.IRON_INGOT)
                .define('Y', Items.IRON_NUGGET)
                .unlockedBy("always", has(Items.AIR))
                .save(output, getSimpleRecipeName(RDKitchenBlocks.GRILL));

        shaped(RecipeCategory.DECORATIONS, RDKitchenBlocks.STEAMER)
                .pattern("YYY")
                .pattern("X X")
                .pattern("XXX")
                .define('X', Items.IRON_INGOT)
                .define('Y', Items.OAK_SLAB)
                .unlockedBy("always", has(Items.AIR))
                .save(output, getSimpleRecipeName(RDKitchenBlocks.STEAMER));
        shaped(RecipeCategory.DECORATIONS, RDBlocks.PLATE)
                .pattern("YXY")
                .pattern(" Y ")
                .define('X', Items.ITEM_FRAME)
                .define('Y', Items.QUARTZ)
                .unlockedBy("always", has(Items.AIR))
                .save(output, getSimpleRecipeName(RDBlocks.PLATE));
    }

    private void buildKitchenBlockUpgradeRecipes() {
        Map<Block, Block> mystiaUpgrade = new Object2ObjectOpenHashMap<>();
        mystiaUpgrade.put(RDKitchenBlocks.COOKING_POT.asBlock(), RDKitchenBlocks.MYSTIA_COOKING_POT.asBlock());
        mystiaUpgrade.put(RDKitchenBlocks.CUTTING_BOARD.asBlock(), RDKitchenBlocks.MYSTIA_CUTTING_BOARD.asBlock());
        mystiaUpgrade.put(RDKitchenBlocks.FRYING_PAN.asBlock(), RDKitchenBlocks.MYSTIA_FRYING_PAN.asBlock());
        mystiaUpgrade.put(RDKitchenBlocks.GRILL.asBlock(), RDKitchenBlocks.MYSTIA_GRILL.asBlock());
        mystiaUpgrade.put(RDKitchenBlocks.STEAMER.asBlock(), RDKitchenBlocks.MYSTIA_STEAMER.asBlock());
        this.offerUpgradeRecipes(mystiaUpgrade, Items.FEATHER);

        Map<Block, Block> superUpgrade = new Object2ObjectOpenHashMap<>();
        superUpgrade.put(RDKitchenBlocks.COOKING_POT.asBlock(), RDKitchenBlocks.SUPER_COOKING_POT.asBlock());
        superUpgrade.put(RDKitchenBlocks.CUTTING_BOARD.asBlock(), RDKitchenBlocks.SUPER_CUTTING_BOARD.asBlock());
        superUpgrade.put(RDKitchenBlocks.FRYING_PAN.asBlock(), RDKitchenBlocks.SUPER_FRYING_PAN.asBlock());
        superUpgrade.put(RDKitchenBlocks.GRILL.asBlock(), RDKitchenBlocks.SUPER_GRILL.asBlock());
        superUpgrade.put(RDKitchenBlocks.STEAMER.asBlock(), RDKitchenBlocks.SUPER_STEAMER.asBlock());
        this.offerUpgradeRecipes(superUpgrade, Items.GOLD_INGOT);

        Map<Block, Block> extremeUpgrade = new Object2ObjectOpenHashMap<>();
        extremeUpgrade.put(RDKitchenBlocks.COOKING_POT.asBlock(), RDKitchenBlocks.EXTREME_COOKING_POT.asBlock());
        extremeUpgrade.put(RDKitchenBlocks.CUTTING_BOARD.asBlock(), RDKitchenBlocks.EXTREME_CUTTING_BOARD.asBlock());
        extremeUpgrade.put(RDKitchenBlocks.FRYING_PAN.asBlock(), RDKitchenBlocks.EXTREME_FRYING_PAN.asBlock());
        extremeUpgrade.put(RDKitchenBlocks.GRILL.asBlock(), RDKitchenBlocks.EXTREME_GRILL.asBlock());
        extremeUpgrade.put(RDKitchenBlocks.STEAMER.asBlock(), RDKitchenBlocks.EXTREME_STEAMER.asBlock());
        this.offerUpgradeRecipes(extremeUpgrade, Items.DIAMOND);

        Map<Block, Block> nukeUpgrade = new Object2ObjectOpenHashMap<>();
        nukeUpgrade.put(RDKitchenBlocks.COOKING_POT.asBlock(), RDKitchenBlocks.NUKE_COOKING_POT.asBlock());
        nukeUpgrade.put(RDKitchenBlocks.CUTTING_BOARD.asBlock(), RDKitchenBlocks.NUKE_CUTTING_BOARD.asBlock());
        nukeUpgrade.put(RDKitchenBlocks.FRYING_PAN.asBlock(), RDKitchenBlocks.NUKE_FRYING_PAN.asBlock());
        nukeUpgrade.put(RDKitchenBlocks.GRILL.asBlock(), RDKitchenBlocks.NUKE_GRILL.asBlock());
        nukeUpgrade.put(RDKitchenBlocks.STEAMER.asBlock(), RDKitchenBlocks.NUKE_STEAMER.asBlock());
        this.offerUpgradeRecipes(nukeUpgrade, Items.NETHER_BRICKS);
    }

    private void buildRedstones() {
        shaped(RecipeCategory.REDSTONE, RDBlocks.RAIL_CONTROLLER_BLOCK)
                .pattern(" X ")
                .pattern("Y#Y")
                .pattern(" X ")
                .define('X', Items.REDSTONE)
                .define('Y', Items.LEVER)
                .define('#', Items.RAIL)
                .unlockedBy("has_rail", has(Items.RAIL))
                .save(this.output, getSimpleRecipeName(RDBlocks.RAIL_CONTROLLER_BLOCK));
        shaped(RecipeCategory.REDSTONE, RDBlocks.SIGNAL_RAIL_BLOCK)
                .pattern("XYX")
                .pattern("X#X")
                .pattern("XXX")
                .define('X', Items.REDSTONE)
                .define('Y', ItemTags.SIGNS)
                .define('#', Items.RAIL)
                .unlockedBy("has_rail", has(Items.RAIL))
                .save(this.output, getSimpleRecipeName(RDBlocks.SIGNAL_RAIL_BLOCK));
        shaped(RecipeCategory.REDSTONE, RDBlocks.SIGNAL_DELAYER_BLOCK)
                .pattern("###")
                .pattern("XYX")
                .pattern("###")
                .define('X', Items.REDSTONE)
                .define('Y', Items.CLOCK)
                .define('#', Items.COBBLESTONE)
                .unlockedBy("has_rail", has(Items.CLOCK))
                .save(this.output, getSimpleRecipeName(RDBlocks.SIGNAL_DELAYER_BLOCK));
        shaped(RecipeCategory.REDSTONE, RDBlocks.REMOTE_CLIENT)
                .pattern("#X#")
                .pattern("###")
                .define('X', Items.REDSTONE)
                .define('#', Items.IRON_INGOT)
                .unlockedBy("has_rail", has(Items.REDSTONE))
                .save(this.output, getSimpleRecipeName(RDBlocks.REMOTE_CLIENT));
        shaped(RecipeCategory.REDSTONE, RDBlocks.REMOTE_SERVER)
                .pattern("#X#")
                .pattern("###")
                .define('X', Items.REDSTONE_TORCH)
                .define('#', Items.IRON_INGOT)
                .unlockedBy("has_rail", has(Items.REDSTONE_TORCH))
                .save(this.output, getSimpleRecipeName(RDBlocks.REMOTE_SERVER));
        shaped(RecipeCategory.REDSTONE, RDBlocks.SPEAKER)
                .pattern("X#")
                .pattern("#Y")
                .define('X', Items.NOTE_BLOCK)
                .define('Y', Items.REDSTONE)
                .define('#', Items.IRON_INGOT)
                .unlockedBy("has_rail", has(Items.NOTE_BLOCK))
                .save(this.output, getSimpleRecipeName(RDBlocks.SPEAKER));
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

    private void offerSpearRecipe(RecipeOutput exporter, Item result, Item ingot) {
        shaped(RecipeCategory.COMBAT, result)
                .pattern("  X")
                .pattern(" # ")
                .pattern("#  ")
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

    public ShapedStackRecipeBuilder shapedItemStack(RecipeCategory recipeCategory, ItemStackTemplate template) {
        return ShapedStackRecipeBuilder.shaped(((RecipeProviderAccessor) this).reverie_dreams$getItems(), recipeCategory, template.create());
    }

    public ShapedStackRecipeBuilder shapedItemStack(RecipeCategory recipeCategory, ItemStackTemplate template, int i) {
        ItemStackTemplateHelper.modify(template, (template1, modifier) -> {
            modifier.setCount(i);
        });
        return ShapedStackRecipeBuilder.shaped(((RecipeProviderAccessor) this).reverie_dreams$getItems(), recipeCategory, template.create());
    }
}
