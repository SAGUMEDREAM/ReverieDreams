package cc.thonly.reverie_dreams.datagen;

import cc.thonly.mystias_izakaya.MystiasIzakaya;
import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.mystias_izakaya.recipe.MiRecipeManager;
import cc.thonly.mystias_izakaya.recipe.entry.KitchenRecipe;
import cc.thonly.mystias_izakaya.recipe.type.KitchenRecipeType;
import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.datagen.generator.RecipeTypeProvider;
import cc.thonly.reverie_dreams.effect.ModPotions;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.item.RoleCards;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuRecipe;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuShapeDrawRecipe;
import cc.thonly.reverie_dreams.recipe.entry.GensokyoAltarRecipe;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ModRecipeTypeProvider extends RecipeTypeProvider {
    public final cc.thonly.reverie_dreams.datagen.generator.RecipeTypeProvider.Factory<GensokyoAltarRecipe> gensokyoAltarRecipeFactory = this.getOrCreateFactory(RecipeManager.GENSOKYO_ALTAR, GensokyoAltarRecipe.class);
    public final cc.thonly.reverie_dreams.datagen.generator.RecipeTypeProvider.Factory<DanmakuRecipe> danmakuRecipeFactory = this.getOrCreateFactory(RecipeManager.DANMAKU_TYPE, DanmakuRecipe.class);
    public final cc.thonly.reverie_dreams.datagen.generator.RecipeTypeProvider.Factory<DanmakuShapeDrawRecipe> danmakuShapeDrawRecipeFactory = this.getOrCreateFactory(RecipeManager.DANMAKU_SHAPE_DRAW_TYPE, DanmakuShapeDrawRecipe.class);
    public final cc.thonly.reverie_dreams.datagen.generator.RecipeTypeProvider.Factory<KitchenRecipe> kitchenRecipeFactory = this.getOrCreateFactory(MiRecipeManager.KITCHEN_RECIPE, KitchenRecipe.class);

    public ModRecipeTypeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    @Override
    public void configured() {
        this.generateGensokyoAltarRecipe();
        this.generateRoleCardRecipe();
        this.generateDanmakuRecipe();
        this.generateShapeDraw();
        this.generateKitchenRecipe();
    }

    @SuppressWarnings("SpellCheckingInspection")
    public void generateShapeDraw() {
        DanmakuTypes.AMULET.buildShapeRecipe(this.danmakuShapeDrawRecipeFactory, new String[]{
                "FFFFFF",
                "FFTTFF",
                "FFTTFF",
                "FFTTFF",
                "FFTTFF",
                "FFFFFF"
        });
        DanmakuTypes.ARROWHEAD.buildShapeRecipe(this.danmakuShapeDrawRecipeFactory, new String[]{
                "FFFFFF",
                "FFTTFF",
                "FTTTTF",
                "FTTTTF",
                "FTFFTF",
                "FFFFFF"
        });
        DanmakuTypes.BALL.buildShapeRecipe(this.danmakuShapeDrawRecipeFactory, new String[]{
                "FFFFFF",
                "FFFFFF",
                "FFTTFF",
                "FFTTFF",
                "FFFFFF",
                "FFFFFF"
        });
        DanmakuTypes.BUBBLE.buildShapeRecipe(this.danmakuShapeDrawRecipeFactory, new String[]{
                "FFFFFF",
                "FTTTTF",
                "FTTTTF",
                "FTTTTF",
                "FTTTTF",
                "FFFFFF"
        });
        DanmakuTypes.BULLET.buildShapeRecipe(this.danmakuShapeDrawRecipeFactory, new String[]{
                "FFFFFF",
                "FFTTFF",
                "FTTTTF",
                "FTTTTF",
                "FTTTTF",
                "FFFFFF"
        });
        DanmakuTypes.FIREBALL.buildShapeRecipe(this.danmakuShapeDrawRecipeFactory, new String[]{
                "FFTTFF",
                "FTFFTF",
                "TFTTFT",
                "TFTTFT",
                "FTFFTF",
                "FFTTFF"
        });
        DanmakuTypes.FIREBALL_GLOWY.buildShapeRecipe(this.danmakuShapeDrawRecipeFactory, new String[]{
                "FFTTFF",
                "FTTTTF",
                "TTTTTT",
                "TTTTTT",
                "FTTTTF",
                "FFTTFF"
        });
        DanmakuTypes.KUNAI.buildShapeRecipe(this.danmakuShapeDrawRecipeFactory, new String[]{
                "FFTTFF",
                "FTTTTF",
                "FFTTFF",
                "FFTTFF",
                "FTFFTF",
                "FFFFFF"
        });
        DanmakuTypes.RICE.buildShapeRecipe(this.danmakuShapeDrawRecipeFactory, new String[]{
                "FFTTFF",
                "FTTTTF",
                "FTTTTF",
                "FTTTTF",
                "FTTTTF",
                "FFTTFF"
        });
        DanmakuTypes.STAR.buildShapeRecipe(this.danmakuShapeDrawRecipeFactory, new String[]{
                "FFFFFF",
                "FFTTFF",
                "TTTTTT",
                "FFTTFF",
                "FTFFTF",
                "FFFFFF"
        });

    }

    public void generateRoleCardRecipe() {
        RoleCards.PROTAGONIST_GROUP
                .createRecipeBuilder()
                .itemStack(ItemStackWrapper.of(Items.REDSTONE_BLOCK, 2), ItemStackWrapper.of(Items.OBSIDIAN, 5))
                .build()
                .apply(this.gensokyoAltarRecipeFactory);
        RoleCards.KOUMAKYOU
                .createRecipeBuilder()
                .itemStack(ItemStackWrapper.of(Items.SOUL_SAND, 12), ItemStackWrapper.of(Items.NETHERRACK, 12))
                .build()
                .apply(this.gensokyoAltarRecipeFactory);
        RoleCards.YOUYOUMU
                .createRecipeBuilder()
                .itemStack(ItemStackWrapper.of(Items.CHERRY_LEAVES, 26), ItemStackWrapper.of(Items.IRON_SWORD, 1))
                .build()
                .apply(this.gensokyoAltarRecipeFactory);
        RoleCards.EIYASHOU
                .createRecipeBuilder()
                .itemStack(ItemStackWrapper.of(Items.BAMBOO, 50), ItemStackWrapper.of(Items.END_STONE, 24))
                .build()
                .apply(this.gensokyoAltarRecipeFactory);
        RoleCards.KAEIZUKA
                .createRecipeBuilder()
                .itemStack(ItemStackWrapper.of(Items.DANDELION, 30), ItemStackWrapper.of(Items.ALLIUM, 30))
                .build()
                .apply(this.gensokyoAltarRecipeFactory);
        RoleCards.FUUJINROKU
                .createRecipeBuilder()
                .itemStack(ItemStackWrapper.of(Items.LEAF_LITTER, 40), ItemStackWrapper.of(Items.STONE, 40))
                .build()
                .apply(this.gensokyoAltarRecipeFactory);
        RoleCards.CHIREIDEN
                .createRecipeBuilder()
                .itemStack(ItemStackWrapper.of(Items.ROSE_BUSH, 40), ItemStackWrapper.of(Items.NETHERRACK, 45))
                .build()
                .apply(this.gensokyoAltarRecipeFactory);
        RoleCards.SEIRENSEN
                .createRecipeBuilder()
                .itemStack(ItemStackWrapper.of(Items.GOLD_INGOT, 28), ItemStackWrapper.of(Items.BIRCH_BOAT, 1))
                .build()
                .apply(this.gensokyoAltarRecipeFactory);
        RoleCards.SHINREIBYOU
                .createRecipeBuilder()
                .itemStack(ItemStackWrapper.of(Items.SOUL_SAND, 38), ItemStackWrapper.of(Items.ROTTEN_FLESH, 18))
                .build()
                .apply(this.gensokyoAltarRecipeFactory);
        RoleCards.KISHINJOU
                .createRecipeBuilder()
                .itemStack(ItemStackWrapper.of(Items.BLAZE_ROD, 26), ItemStackWrapper.of(Items.NETHER_BRICKS, 30))
                .build()
                .apply(this.gensokyoAltarRecipeFactory);
        RoleCards.KANJUDEN
                .createRecipeBuilder()
                .itemStack(ItemStackWrapper.of(Items.END_STONE, 32), ItemStackWrapper.of(Items.NETHERRACK, 32))
                .build()
                .apply(this.gensokyoAltarRecipeFactory);
        RoleCards.TENKUUSHOU
                .createRecipeBuilder()
                .itemStack(ItemStackWrapper.of(Items.GRASS_BLOCK, 29), ItemStackWrapper.of(Items.LEAF_LITTER, 43))
                .build()
                .apply(this.gensokyoAltarRecipeFactory);
        RoleCards.KIKEIJUU
                .createRecipeBuilder()
                .itemStack(ItemStackWrapper.of(Items.DIRT, 44), ItemStackWrapper.of(Items.BLAZE_POWDER, 30))
                .build()
                .apply(this.gensokyoAltarRecipeFactory);
        RoleCards.KOURYUUDOU
                .createRecipeBuilder()
                .itemStack(ItemStackWrapper.of(Items.GOLD_INGOT, 31), ItemStackWrapper.of(Items.DIAMOND, 23))
                .build()
                .apply(this.gensokyoAltarRecipeFactory);
        RoleCards.JUUOUEN
                .createRecipeBuilder()
                .itemStack(ItemStackWrapper.of(Items.LEATHER, 26), ItemStackWrapper.of(Items.PORKCHOP, 20))
                .build()
                .apply(this.gensokyoAltarRecipeFactory);
        RoleCards.KINJOUKYOU
                .createRecipeBuilder()
                .itemStack(ItemStackWrapper.of(Items.STONE, 31), ItemStackWrapper.of(Items.GOLD_INGOT, 38))
                .build()
                .apply(this.gensokyoAltarRecipeFactory);
        RoleCards.SANGETSUSEI
                .createRecipeBuilder()
                .itemStack(ItemStackWrapper.of(Items.END_STONE, 28), ItemStackWrapper.of(Items.GLOWSTONE, 26))
                .build()
                .apply(this.gensokyoAltarRecipeFactory);
        RoleCards.HIFUU
                .createRecipeBuilder()
                .itemStack(ItemStackWrapper.of(Items.BOOK, 19), ItemStackWrapper.of(Items.ENDER_EYE, 20))
                .build()
                .apply(this.gensokyoAltarRecipeFactory);
        RoleCards.TASOGARE_FURONTIA
                .createRecipeBuilder()
                .itemStack(ItemStackWrapper.of(Items.GLASS_BOTTLE, 20), ItemStackWrapper.of(MIItems.PEACH, 15))
                .build()
                .apply(this.gensokyoAltarRecipeFactory);

    }

    public void generateGensokyoAltarRecipe() {
        this.gensokyoAltarRecipeFactory.register(ModItems.HORAI_DAMA_NO_EDA,
                new GensokyoAltarRecipe(
                        this.ofItem(Items.DIAMOND_BLOCK),
                        List.of(
                                this.ofItem(ModItems.RED_ORB, 10), this.ofItem(ModItems.BLUE_ORB, 10), this.ofItem(ModItems.YELLOW_ORB, 10),
                                this.ofEmpty(), this.ofItem(ModItems.GREEN_ORB, 10),
                                this.ofItem(Items.STICK), this.ofEmpty(), this.ofItem(ModItems.PURPLE_ORB, 10)
                        ),
                        this.ofItem(ModItems.HORAI_DAMA_NO_EDA)
                )
        );
        this.gensokyoAltarRecipeFactory.register(ModItems.CROSSING_CHISEL, new GensokyoAltarRecipe(
                this.ofItem(Items.GOLDEN_HOE),
                List.of(
                        this.ofItem(Items.ENDER_PEARL, 2), this.ofItem(Items.GOLD_BLOCK), this.ofItem(Items.ENDER_PEARL, 4),
                        this.ofItem(Items.GOLD_INGOT, 4), this.ofItem(Items.GOLD_INGOT, 4),
                        this.ofItem(Items.ENDER_PEARL, 8), this.ofItem(Items.GOLD_BLOCK), this.ofItem(Items.ENDER_PEARL, 16)
                ),
                this.ofItem(ModItems.CROSSING_CHISEL)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.GAP_BALL, new GensokyoAltarRecipe(
                this.ofItem(Items.COMPASS),
                List.of(
                        this.ofItem(Items.PURPLE_DYE, 2), this.ofItem(Items.ENDER_PEARL, 6), this.ofItem(Items.REDSTONE_BLOCK, 1),
                        this.ofItem(Items.MAGENTA_DYE, 2), this.ofItem(Items.COPPER_INGOT),
                        this.ofItem(Items.COPPER_INGOT, 6), this.ofItem(Items.COPPER_INGOT, 7), this.ofItem(Items.COPPER_INGOT, 8)
                ),
                this.ofItem(ModItems.GAP_BALL)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.BAGUA_FURNACE, new GensokyoAltarRecipe(
                this.ofItem(Items.COMPASS),
                List.of(
                        this.ofItem(Items.REDSTONE_BLOCK, 8), this.ofItem(Items.IRON_INGOT, 12), this.ofItem(Items.COAL_BLOCK),
                        this.ofItem(Items.NETHERITE_INGOT, 4), this.ofItem(Items.COPPER_INGOT, 16),
                        this.ofItem(Items.COAL_BLOCK), this.ofItem(Items.IRON_INGOT, 12), this.ofItem(Items.REDSTONE_BLOCK, 8)
                ),
                this.ofItem(ModItems.BAGUA_FURNACE)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.TIME_STOP_CLOCK, new GensokyoAltarRecipe(
                this.ofItem(Items.CLOCK),
                List.of(
                        this.ofItem(Items.PURPLE_DYE, 4), this.ofItem(Items.REDSTONE_BLOCK, 2), this.ofItem(Items.PURPLE_DYE, 4),
                        this.ofItem(Items.GOLD_INGOT, 5), this.ofItem(Items.GOLD_INGOT, 5),
                        this.ofItem(Items.PURPLE_DYE, 4), this.ofItem(Items.REDSTONE_BLOCK, 2), this.ofItem(Items.PURPLE_DYE, 4)
                ),
                this.ofItem(ModItems.TIME_STOP_CLOCK)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.MAPLE_LEAF_FAN, new GensokyoAltarRecipe(
                this.ofItem(Items.OAK_LEAVES, 48),
                List.of(
                        this.ofItem(Items.WIND_CHARGE, 16), this.ofItem(Items.GOLD_INGOT, 9), this.ofItem(Items.WIND_CHARGE, 16),
                        this.ofItem(Items.GOLD_INGOT, 9), this.ofItem(Items.GOLD_INGOT, 9),
                        this.ofItem(Items.BREEZE_ROD, 2), this.ofItem(Items.GOLD_INGOT, 9), this.ofItem(Items.WIND_CHARGE, 16)
                ),
                this.ofItem(ModItems.MAPLE_LEAF_FAN)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.EARPHONE, new GensokyoAltarRecipe(
                this.ofItem(Items.IRON_HELMET),
                List.of(
                        this.ofItem(Items.AMETHYST_SHARD, 5), this.ofItem(Items.GOLD_INGOT, 6), this.ofItem(Items.AMETHYST_SHARD, 5),
                        this.ofItem(Items.SCULK_SENSOR, 9), this.ofItem(Items.SCULK_SENSOR, 9),
                        this.ofEmpty(), this.ofItem(Items.REDSTONE, 18), this.ofEmpty()
                ),
                this.ofItem(ModItems.EARPHONE)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.KOISHI_HAT, new GensokyoAltarRecipe(
                this.ofItem(Items.IRON_HELMET),
                List.of(
                        this.ofItem(Items.BLACK_DYE, 2), this.ofItem(Items.IRON_INGOT, 1), this.ofItem(Items.BLACK_DYE, 2),
                        this.ofItem(Items.IRON_INGOT, 1), this.ofItem(Items.IRON_INGOT, 1),
                        this.ofItem(Items.YELLOW_DYE, 3), this.ofItem(Items.BLACK_DYE, 2), this.ofItem(Items.YELLOW_DYE, 3)
                ),
                this.ofItem(ModItems.KOISHI_HAT)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.HAKUREI_CANE, new GensokyoAltarRecipe(
                this.ofItem(Items.WOODEN_SWORD),
                List.of(
                        this.ofEmpty(), this.ofItem(Items.PAPER, 6), this.ofItem(Items.PAPER, 6),
                        this.ofItem(Items.IRON_INGOT, 1), this.ofItem(Items.PAPER, 6),
                        this.ofItem(Items.STICK, 1), this.ofEmpty(), this.ofItem(Items.PAPER, 3)
                ),
                this.ofItem(ModItems.HAKUREI_CANE)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.WIND_BLESSING_CANE, new GensokyoAltarRecipe(
                this.ofItem(Items.WOODEN_SWORD),
                List.of(
                        this.ofEmpty(), this.ofItem(Items.PAPER, 5), this.ofItem(Items.PAPER, 7),
                        this.ofItem(Items.WIND_CHARGE, 12), this.ofItem(Items.PAPER, 5),
                        this.ofItem(Items.STICK, 1), this.ofEmpty(), this.ofItem(Items.PAPER, 2)
                ),
                this.ofItem(ModItems.WIND_BLESSING_CANE)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.MAGIC_BROOM, new GensokyoAltarRecipe(
                this.ofItem(Items.REDSTONE_BLOCK, 3),
                List.of(
                        this.ofItem(Items.HAY_BLOCK, 2), this.ofItem(Items.LEAD), this.ofItem(Items.SADDLE),
                        this.ofItem(Items.HAY_BLOCK, 2), this.ofItem(Items.STICK, 3),
                        this.ofItem(Items.HAY_BLOCK, 2), this.ofItem(Items.SLIME_BALL), this.ofEmpty()
                ),
                this.ofItem(ModItems.MAGIC_BROOM)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.KNIFE, new GensokyoAltarRecipe(
                this.ofItem(ModBlocks.SILVER_BLOCK, 2),
                List.of(
                        this.ofItem(ModItems.SILVER_SWORD), this.ofEmpty(), this.ofItem(ModItems.SILVER_INGOT, 3),
                        this.ofItem(ModItems.SILVER_SWORD), this.ofEmpty(),
                        this.ofItem(Items.STICK, 3), this.ofEmpty(), this.ofEmpty()
                ),
                this.ofItem(ModItems.KNIFE)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.ROKANKEN, new GensokyoAltarRecipe(
                this.ofItem(Items.IRON_SWORD),
                List.of(
                        this.ofItem(Items.SOUL_SAND, 7), this.ofItem(Items.SOUL_SAND, 8), this.ofItem(ModItems.SILVER_INGOT, 12),
                        this.ofItem(Items.SOUL_SAND, 6), this.ofEmpty(),
                        this.ofItem(Items.STRIPPED_CHERRY_LOG, 4), this.ofEmpty(), this.ofEmpty()
                ),
                this.ofItem(ModItems.ROKANKEN)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.HAKUROKEN, new GensokyoAltarRecipe(
                this.ofItem(Items.IRON_SWORD),
                List.of(
                        this.ofItem(Items.SOUL_SAND, 8), this.ofItem(Items.SOUL_SAND, 9), this.ofItem(ModItems.SILVER_INGOT, 12),
                        this.ofItem(Items.SOUL_SAND, 7), this.ofEmpty(),
                        this.ofItem(Items.STRIPPED_CHERRY_LOG, 4), this.ofEmpty(), this.ofItem(ModItems.SPEED_FEATHER, 4)
                ),
                this.ofItem(ModItems.HAKUROKEN)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.PAPILIO_PATTERN_FAN, new GensokyoAltarRecipe(
                this.ofItem(Items.IRON_INGOT, 1),
                List.of(
                        this.ofEmpty(), this.ofItem(Items.BLUE_WOOL, 3), this.ofItem(Items.PURPLE_WOOL, 3),
                        this.ofItem(Items.SOUL_SAND, 9), this.ofItem(Items.PINK_WOOL, 3),
                        this.ofItem(Items.STRIPPED_CHERRY_LOG, 5), this.ofItem(Items.CHERRY_SAPLING), this.ofEmpty()
                ),
                this.ofItem(ModItems.PAPILIO_PATTERN_FAN)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.GUNGNIR, new GensokyoAltarRecipe(
                this.ofItem(Items.DIAMOND_AXE),
                List.of(
                        this.ofItem(Items.DIAMOND, 2), this.ofItem(Items.IRON_INGOT, 5), this.ofItem(Items.REDSTONE_BLOCK, 2),
                        this.ofItem(Items.SOUL_SAND, 9), this.ofItem(Items.IRON_INGOT, 5),
                        this.ofItem(Items.BREEZE_ROD, 5), this.ofItem(Items.SOUL_SAND, 6), this.ofEmpty()
                ),
                this.ofItem(ModItems.GUNGNIR)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.LEVATIN, new GensokyoAltarRecipe(
                this.ofItem(Items.NETHERITE_HOE),
                List.of(
                        this.ofEmpty(), this.ofItem(Items.GOLD_INGOT, 5), this.ofItem(Items.NETHERITE_INGOT, 1),
                        this.ofItem(Items.REDSTONE, 3), this.ofItem(Items.IRON_INGOT, 5),
                        this.ofItem(Items.BLAZE_ROD, 4), this.ofItem(Items.SOUL_SAND, 4), this.ofItem(Items.COAL, 11)
                ),
                this.ofItem(ModItems.LEVATIN)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.IBUKIHO, new GensokyoAltarRecipe(
                this.ofItem(Items.HONEY_BOTTLE),
                List.of(
                        this.ofItem(Items.GOLD_INGOT, 8), this.ofItem(Items.GOLD_INGOT, 5), this.ofItem(Items.SUGAR, 1),
                        this.ofItem(Items.REDSTONE, 8), this.ofItem(Items.GHAST_TEAR, 8),
                        this.ofItem(Items.DIAMOND, 4), this.ofItem(Items.BLAZE_ROD, 8), this.ofItem(Items.FERMENTED_SPIDER_EYE, 8)
                ),
                this.ofItem(ModItems.IBUKIHO)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.SWORD_OF_HISOU, new GensokyoAltarRecipe(
                this.ofItem(Items.GOLDEN_SWORD),
                List.of(
                        this.ofItem(Items.GOLD_INGOT, 7), this.ofItem(Items.DIAMOND, 5), this.ofItem(Items.COPPER_INGOT, 2),
                        this.ofItem(Items.REDSTONE, 6), this.ofItem(Items.REDSTONE, 6),
                        this.ofItem(Items.BLAZE_ROD, 4), this.ofItem(MIItems.PEACH, 7), this.ofItem(Items.APPLE, 6)
                ),
                this.ofItem(ModItems.SWORD_OF_HISOU)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.MANPOZUCHI, new GensokyoAltarRecipe(
                this.ofItem(Items.MACE),
                List.of(
                        this.ofItem(Items.GOLD_INGOT, 2), this.ofItem(Items.END_ROD, 1), this.ofItem(Items.IRON_INGOT, 2),
                        this.ofItem(Items.REDSTONE, 8), this.ofItem(Items.COPPER_INGOT, 7),
                        this.ofItem(Items.IRON_NUGGET, 18), this.ofItem(Items.DIAMOND, 4), this.ofItem(Items.GOLD_NUGGET, 18)
                ),
                this.ofItem(ModItems.MANPOZUCHI)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.NUE_TRIDENT, new GensokyoAltarRecipe(
                this.ofItem(Items.TRIDENT),
                List.of(
                        this.ofEmpty(), this.ofEmpty(), this.ofItem(Items.COMPASS),
                        this.ofItem(Items.HEART_OF_THE_SEA), this.ofItem(Items.COD, 3),
                        this.ofItem(Items.NETHERITE_INGOT), this.ofItem(Items.OAK_LOG, 8), this.ofEmpty()
                ),
                this.ofItem(ModItems.NUE_TRIDENT)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.TREASURE_HUNTING_ROD, new GensokyoAltarRecipe(
                this.ofItem(Items.DIAMOND_PICKAXE),
                List.of(
                        this.ofItem(Items.IRON_INGOT, 5), this.ofItem(Items.IRON_INGOT, 6), this.ofItem(Items.RABBIT_FOOT, 9),
                        this.ofItem(Items.NETHERITE_INGOT), this.ofItem(Items.GOLD_INGOT, 10),
                        this.ofItem(Items.NETHERITE_INGOT), this.ofItem(Items.REDSTONE, 8), this.ofItem(Items.STONE_BUTTON, 1)
                ),
                this.ofItem(ModItems.TREASURE_HUNTING_ROD)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.TRUMPET_GUN, new GensokyoAltarRecipe(
                this.ofItem(Items.CROSSBOW),
                List.of(
                        this.ofItem(ModBlocks.POWER_BLOCK, 5), this.ofItem(Items.FERMENTED_SPIDER_EYE, 2), this.ofItem(Items.RABBIT_FOOT, 5),
                        this.ofItem(ModBlocks.POWER_BLOCK, 4), this.ofItem(Items.GOLD_INGOT, 5),
                        this.ofItem(Items.HONEYCOMB), this.ofItem(Items.IRON_NUGGET, 3), this.ofItem(Items.BLAZE_ROD, 3)
                ),
                this.ofItem(ModItems.TRUMPET_GUN)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.DEATH_SCYTHE, new GensokyoAltarRecipe(
                this.ofItem(Items.NETHERITE_HOE),
                List.of(
                        this.ofItem(Items.SOUL_SAND, 32), this.ofItem(Items.SOUL_SAND, 16), this.ofItem(Items.SOUL_SAND, 8),
                        this.ofItem(Items.NETHERITE_INGOT, 1), this.ofItem(Items.DIAMOND, 5),
                        this.ofItem(Items.ROTTEN_FLESH, 8), this.ofItem(Items.BONE, 7)
                ),
                this.ofItem(ModItems.DEATH_SCYTHE)
        ));
        ItemStack kanjuKusuri = ModPotions.createStack(ModPotions.KANJU_KUSURI_POTION);
        this.gensokyoAltarRecipeFactory.register(Touhou.id("kanju_kusuri"), new GensokyoAltarRecipe(
                this.ofItem(Items.GLASS_BOTTLE),
                List.of(
                        this.ofItem(Items.SOUL_SAND, 20), this.ofItem(Items.SAND, 20), this.ofItem(Items.NETHER_WART, 5),
                        this.ofItem(Items.BLAZE_POWDER, 8), this.ofItem(Items.GOLDEN_APPLE, 1),
                        this.ofItem(Items.GOLDEN_CARROT, 2), this.ofItem(ModItems.SILVER_INGOT, 4), this.ofItem(Items.ENDER_PEARL, 1)
                ),
                this.ofItem(kanjuKusuri)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.CURSED_DECOY_DOLl, new GensokyoAltarRecipe(
                        this.ofItem(Items.ARMOR_STAND),
                        List.of(
                                this.ofItem(Items.SOUL_SAND, 6), this.ofItem(Items.SOUL_SAND, 6), this.ofItem(Items.SOUL_SAND, 6),
                                this.ofItem(Items.SOUL_SAND, 6), this.ofItem(Items.SOUL_SAND, 6),
                                this.ofItem(Items.SOUL_SAND, 6), this.ofItem(Items.SOUL_SAND, 6), this.ofItem(Items.SOUL_SAND, 6)
                        ),
                        this.ofItem(ModItems.CURSED_DECOY_DOLl)
                )
        );
        this.gensokyoAltarRecipeFactory.register(ModItems.VAISRAVANAS_PAGODA, new GensokyoAltarRecipe(
                this.ofItem(ModBlocks.POWER_BLOCK, 10),
                List.of(
                        this.ofItem(Items.STONE, 15), this.ofItem(Items.GOLD_INGOT, 20), this.ofItem(Items.STONE, 15),
                        this.ofItem(Items.COPPER_INGOT, 10), this.ofItem(Items.BLAZE_POWDER, 8),
                        this.ofItem(Items.STONE, 15), this.ofItem(Items.IRON_INGOT, 20), this.ofItem(Items.STONE, 15)
                ),
                this.ofItem(ModItems.VAISRAVANAS_PAGODA)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.TENGU_SHIELD, new GensokyoAltarRecipe(
                this.ofItem(Items.SHIELD, 1),
                List.of(
                        this.ofItem(Items.FEATHER, 3), this.ofItem(Items.FEATHER, 5), this.ofItem(Items.FEATHER, 2),
                        this.ofItem(ModItems.SILVER_INGOT, 7), this.ofItem(ModItems.SILVER_INGOT, 7),
                        this.ofItem(Items.DIAMOND, 3), this.ofItem(Items.IRON_INGOT, 6), this.ofItem(Items.BONE, 3)
                ),
                this.ofItem(ModItems.TENGU_SHIELD)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.TENGU_CAMERA, new GensokyoAltarRecipe(
                this.ofItem(Items.IRON_BLOCK, 1),
                List.of(
                        this.ofItem(Items.GLOWSTONE_DUST, 8), this.ofItem(Items.STONE_BUTTON, 2), this.ofItem(Items.GLOWSTONE_DUST, 8),
                        this.ofItem(ModItems.SILVER_INGOT, 6), this.ofItem(Items.IRON_INGOT, 6),
                        this.ofItem(Items.REDSTONE, 23), this.ofItem(Items.IRON_INGOT, 6), this.ofItem(Items.REDSTONE, 13)
                ),
                this.ofItem(ModItems.TENGU_CAMERA)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.BAD_APPLE, new GensokyoAltarRecipe(
                this.ofItem(Items.GOLDEN_APPLE, 1),
                List.of(
                        this.ofItem(Items.BLACK_DYE, 2), this.ofItem(ModItems.POWER, 11), this.ofItem(Items.BLACK_DYE, 2),
                        this.ofItem(Items.REDSTONE, 6), this.ofItem(Items.REDSTONE, 6),
                        this.ofItem(Items.BLACK_DYE, 2), this.ofItem(ModItems.POINT, 8), this.ofItem(Items.BLACK_DYE, 2)
                ),
                this.ofItem(ModItems.BAD_APPLE)
        ));
        this.gensokyoAltarRecipeFactory.register(ModItems.EXORCISM_PAPER, new GensokyoAltarRecipe(
                this.ofItem(Items.PAPER, 6),
                List.of(
                        this.ofItem(Items.BONE, 12), this.ofItem(Items.ROTTEN_FLESH, 12), this.ofItem(Items.RED_DYE, 3),
                        this.ofItem(Items.PAPER, 6), this.ofItem(Items.PAPER, 8),
                        this.ofItem(Items.PAPER, 6), this.ofItem(Items.PAPER, 8), this.ofItem(Items.REDSTONE, 7)
                ),
                this.ofItem(ModItems.EXORCISM_PAPER, 4)
        ));
    }

    public void generateKitchenRecipe() {
        ResourceLocation cookingPot = KitchenRecipeType.KitchenType.COOKING_POT.toId();
        ResourceLocation grill = KitchenRecipeType.KitchenType.GRILL.toId();
        ResourceLocation cuttingBoard = KitchenRecipeType.KitchenType.CUTTING_BOARD.toId();
        ResourceLocation streamer = KitchenRecipeType.KitchenType.STREAMER.toId();
        ResourceLocation fryingPan = KitchenRecipeType.KitchenType.FRYING_PAN.toId();

        // 煮锅
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("seafood_miso_soup"), new KitchenRecipe(
                cookingPot,
                List.of(
                        this.ofItem(Items.KELP)
                ),
                this.ofItem(MIItems.SEAFOOD_MISO_SOUP),
                6.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("tofu_miso"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.TOFU),
                this.ofItem(MIItems.TOFU_MISO),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("strength_soup"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.WILD_BOAR_MEAT, Items.KELP),
                this.ofItem(MIItems.STRENGTH_SOUP),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("game_soup"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.POTATO, Items.PUMPKIN, MIItems.BLACK_PORK),
                this.ofItem(MIItems.GAME_SOUP),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("pork_rice"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.PORKCHOP),
                this.ofItem(MIItems.PORK_RICE),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("beef_rice"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.BEEF),
                this.ofItem(MIItems.BEEF_RICE),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("snow_white"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.PUFFERFISH, MIItems.HAGFISH, Items.KELP),
                this.ofItem(MIItems.SNOW_WHITE),
                12.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("tofu_pot"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.TOFU),
                this.ofItem(MIItems.TOFU_POT),
                5.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("zhaji"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.KELP, MIItems.TOFU, MIItems.TROUT),
                this.ofItem(MIItems.ZHAJI),
                5.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("grand_banquet"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.BLACK_PORK, MIItems.WAGYU_BEEF, MIItems.PUFF_YO_FRUIT),
                this.ofItem(MIItems.GRAND_BANQUET),
                10.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("tonkotsu_ramen"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.PORKCHOP, Items.EGG, Items.KELP),
                this.ofItem(MIItems.TONKOTSU_RAMEN),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("magma"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.BEEF, MIItems.WAGYU_BEEF, MIItems.PUFF_YO_FRUIT, MIItems.TRUFFLE),
                this.ofItem(MIItems.MAGMA),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("peach_blossom_soup"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.PEACH, ModBlocks.MAGIC_ICE_BLOCK.asItem(), MIItems.DEW),
                this.ofItem(MIItems.PEACH_BLOSSOM_SOUP),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("real_seafood_miso_soup"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.SALMON, MIItems.TROUT),
                this.ofItem(MIItems.REAL_SEAFOOD_MISO_SOUP),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("cooking_tofu"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.TOFU),
                this.ofItem(MIItems.COOKING_TOFU),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("ginko_and_radish_pork_rib_soup"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.GINKGO, MIItems.WHITE_RADISH, Items.PORKCHOP),
                this.ofItem(MIItems.GINKGO_AND_RADISH_PORK_RIB_SOUP),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("boiled_fish"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.TROUT, MIItems.CHILI),
                this.ofItem(MIItems.BOILED_FISH),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("dumpling"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.FLOUR),
                this.ofItem(MIItems.DUMPLING),
                5.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("glutinous_rice_balls"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.STICKY_RICE),
                this.ofItem(MIItems.GLUTINOUS_RICE_BALLS),
                5.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("pseudo_jiritama"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.VENISON, MIItems.TRUFFLE, MIItems.CICADA_SHELL),
                this.ofItem(MIItems.PSEUDO_JIRITAMA),
                12.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("secret_mushroom_casserole"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.TRUFFLE, Items.BROWN_MUSHROOM, MIItems.DEW),
                this.ofItem(MIItems.SECRET_MUSHROOM_CASSEROLE),
                9.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("mushroom_girls_dance_stew"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.BROWN_MUSHROOM, MIItems.SHRIMP, MIItems.OCTOPUS, MIItems.CHILI),
                this.ofItem(MIItems.MUSHROOM_GIRLS_DANCE_STEW),
                14.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("milky_mushroom_soup"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.BROWN_MUSHROOM, Items.POTATO, MIItems.CREAM),
                this.ofItem(MIItems.MILKY_MUSHROOM_SOUP),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("gensokyo_buddha_jumps_over_the_wall"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.WAGYU_BEEF, MIItems.SUPREME_TUNA, MIItems.BLACK_PORK, Items.PUFFERFISH, MIItems.TRUFFLE),
                this.ofItem(MIItems.GENSOKYO_BUDDHA_JUMPS_OVER_THE_WALL),
                18.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("screaming_oden"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.CHILI, MIItems.CHILI, Items.BEEF, MIItems.WHITE_RADISH, MIItems.TOFU),
                this.ofItem(MIItems.SCREAMING_ODEN),
                12.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("lion_head"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.BEEF),
                this.ofItem(MIItems.LION_HEAD),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("luohan_vegetarian"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.UDUMBARA, MIItems.BAMBOO_SHOOTS, MIItems.TRUFFLE, MIItems.PINE_NUT, MIItems.LOTUS_NUTS),
                this.ofItem(MIItems.LUOHAN_VEGETARIAN),
                12.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("taichi_bagua_fish_maw"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.SUPREME_TUNA, Items.BROWN_MUSHROOM, MIItems.WHITE_RADISH, Items.EGG, MIItems.GINKGO),
                this.ofItem(MIItems.TAICHI_BAGUA_FISH_MAW),
                14.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("tianshi_braised_chestnut_mushrooms"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.CHESTNUT, Items.BROWN_MUSHROOM, MIItems.TRUFFLE),
                this.ofItem(MIItems.TIANSHI_BRAISED_CHESTNUT_MUSHROOMS),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("the_beauty_of_han_palace"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.HAGFISH, MIItems.TOFU, MIItems.CRAB, Items.BAMBOO, MIItems.DEW),
                this.ofItem(MIItems.THE_BEAUTY_OF_HAN_PALACE),
                12.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("bamboo_shoots_stewed_in_stone_pot"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.BAMBOO, MIItems.BAMBOO_SHOOTS, Items.BEEF),
                this.ofItem(MIItems.BAMBOO_SHOOTS_STEWED_IN_STONE_POT),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("plum_tea_rice"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.PLUM, Items.KELP),
                this.ofItem(MIItems.PLUM_TEA_RICE),
                4.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("green_fairy_mushroom"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.TOON, Items.BROWN_MUSHROOM),
                this.ofItem(MIItems.GREEN_FAIRY_MUSHROOM),
                6.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("poisonous_garden"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.PUFFERFISH, MIItems.PLUM, MIItems.HAGFISH, MIItems.GINKGO),
                this.ofItem(MIItems.POISONOUS_GARDEN),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("beef_hot_pot"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.CHILI, MIItems.WHITE_RADISH, MIItems.TRUFFLE, Items.BEEF, MIItems.WAGYU_BEEF),
                this.ofItem(MIItems.BEEF_HOT_POT),
                5.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("sea_urchin_shingen_pancake"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.SEA_URCHIN, MIItems.TUNA, MIItems.TREMELLA, MIItems.DEW),
                this.ofItem(MIItems.SEA_URCHIN_SHINGEN_PANCAKE),
                12.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("heart_porridge_gruel"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.TREMELLA, MIItems.LOTUS_NUTS),
                this.ofItem(MIItems.HEART_PORRIDGE_GRUEL),
                5.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("superme_seafood_noodles"), new KitchenRecipe(
                cookingPot,
                this.ofList(MIItems.SUPREME_TUNA, Items.KELP, MIItems.OCTOPUS, MIItems.CRAB, MIItems.SHRIMP),
                this.ofItem(MIItems.SUPERME_SEAFOOD_NOODLES),
                12.0
        ));
        // 烧烤架
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("pork_and_trout_smoked"), new KitchenRecipe(
                grill,
                this.ofList(MIItems.TROUT, Items.PORKCHOP),
                this.ofItem(MIItems.PORK_AND_TROUT_SMOKED),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("grilled_hagfish"), new KitchenRecipe(
                grill,
                this.ofList(MIItems.HAGFISH),
                this.ofItem(MIItems.GRILLED_HAGFISH),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("energy_string"), new KitchenRecipe(
                grill,
                this.ofList(Items.BEEF, MIItems.ONION, Items.PUMPKIN),
                this.ofItem(MIItems.ENERGY_STRING),
                12.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("two_heavens_one_style"), new KitchenRecipe(
                grill,
                this.ofList(MIItems.BLACK_PORK, MIItems.WILD_BOAR_MEAT),
                this.ofItem(MIItems.TWO_HEAVENS_ONE_STYLE),
                18.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("roasted_mushrooms"), new KitchenRecipe(
                grill,
                this.ofList(Items.BROWN_MUSHROOM),
                this.ofItem(MIItems.ROASTED_MUSHROOMS),
                6.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("honey_bbq_pork"), new KitchenRecipe(
                grill,
                this.ofList(Items.PORKCHOP, Items.HONEY_BOTTLE),
                this.ofItem(MIItems.HONEY_BBQ_PORK),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("phoenix"), new KitchenRecipe(
                grill,
                this.ofList(MIItems.FLOUR, Items.HONEY_BOTTLE, Items.POTATO, MIItems.ONION, MIItems.WHITE_RADISH),
                this.ofItem(MIItems.PHOENIX),
                12.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("horai_dama_no_rda"), new KitchenRecipe(
                grill,
                this.ofList(Items.BAMBOO, Items.PORKCHOP, MIItems.SALMON, MIItems.WAGYU_BEEF, MIItems.VENISON),
                this.ofItem(MIItems.HORAI_DAMA_NO_EDA),
                13.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("all_meat_feast"), new KitchenRecipe(
                grill,
                this.ofList(MIItems.WILD_BOAR_MEAT, MIItems.VENISON, MIItems.BLACK_PORK, MIItems.WAGYU_BEEF),
                this.ofItem(MIItems.ALL_MEAT_FEAST),
                14.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("one_hit_kill"), new KitchenRecipe(
                grill,
                this.ofList(MIItems.WILD_BOAR_MEAT, MIItems.VENISON, MIItems.ONION),
                this.ofItem(MIItems.ONE_HIT_KILL),
                9.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("baked_sweet_potatoes"), new KitchenRecipe(
                grill,
                this.ofList(MIItems.SWEET_POTATO),
                this.ofItem(MIItems.BAKED_SWEET_POTATOES),
                6.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("biscay_biscuits"), new KitchenRecipe(
                grill,
                this.ofList(MIItems.FLOUR, MIItems.CHEESE),
                this.ofItem(MIItems.BISCAY_BISCUITS),
                5.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("pirate_bacon"), new KitchenRecipe(
                grill,
                this.ofList(Items.BEEF, MIItems.BLACK_SALT, MIItems.CHILI, Items.HONEY_BOTTLE),
                this.ofItem(MIItems.PIRATE_BACON),
                9.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("fantasy_is_all_the_rage"), new KitchenRecipe(
                grill,
                this.ofList(MIItems.ONION, MIItems.WILD_BOAR_MEAT, Items.BEEF, MIItems.TRUFFLE, MIItems.TOMATO),
                this.ofItem(MIItems.FANTASY_IS_ALL_THE_RAGE),
                18.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("cat_kululi"), new KitchenRecipe(
                grill,
                this.ofList(Items.COCOA_BEANS, MIItems.FLOUR, Items.EGG),
                this.ofItem(MIItems.CAT_KULULI),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("cat_pizza"), new KitchenRecipe(
                grill,
                this.ofList(Items.BROWN_MUSHROOM, MIItems.ONION, MIItems.BROCCOLI, MIItems.WILD_BOAR_MEAT),
                this.ofItem(MIItems.CAT_PIZZA),
                10.0
        ));
        // 料理台
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("rice_ball"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.KELP),
                this.ofItem(MIItems.RICE_BALL),
                5.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("grilled_pork_rice_balls"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.PORKCHOP),
                this.ofItem(MIItems.GRILLED_PORK_RICE_BALLS),
                6.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("warm_rice_ball"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.ONION, MIItems.TROUT),
                this.ofItem(MIItems.WARM_RICE_BALL),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("failing_sakura_snow"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.SUPREME_TUNA),
                this.ofItem(MIItems.FAILING_SAKURA_SNOW),
                12.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("cold_tofu"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.WHITE_RADISH, MIItems.TOFU),
                this.ofItem(MIItems.COLD_TOFU),
                5.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("vegetable_special"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.POTATO, MIItems.ONION, Items.PUMPKIN),
                this.ofItem(MIItems.VEGETABLE_SPECIAL),
                5.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("sashimi_platter"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.SALMON, MIItems.TUNA),
                this.ofItem(MIItems.SASHIMI_PLATTER),
                5.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("secret_dried_fish"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.TROUT),
                this.ofItem(MIItems.SECRET_DRIED_FISH),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("cold_dish_carving"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.WHITE_RADISH),
                this.ofItem(MIItems.COLD_DISH_CARVING),
                5.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("arctic_sweet_shrimp_and_peach_salad"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.PEACH, ModBlocks.MAGIC_ICE_BLOCK.asItem(), MIItems.SHRIMP),
                this.ofItem(MIItems.ARCTIC_SWEET_SHRIMP_AND_PEACH_SALAD),
                10.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("moonlight_dumplings"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.MOONFLOWER, MIItems.STICKY_RICE),
                this.ofItem(MIItems.MOONLIGHT_DUMPLINGS),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("mochi"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.STICKY_RICE),
                this.ofItem(MIItems.MOCHI),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("white_peach_eight_bridge"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.STICKY_RICE, MIItems.PEACH),
                this.ofItem(MIItems.WHITE_PEACH_EIGHT_BRIDGE),
                5.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("moon_lover"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.BUTTER, MIItems.FLOUR, Items.EGG, MIItems.MOONFLOWER),
                this.ofItem(MIItems.MOON_LOVERS),
                10.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("flowing_water_noodles"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.FLOUR, Items.BAMBOO),
                this.ofItem(MIItems.FLOWING_WATER_NOODLES),
                10.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("maoyu_tricolor_ice_cream"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.DEW, MIItems.TOFU, Items.HONEY_BOTTLE, Items.EGG),
                this.ofItem(MIItems.MAOYU_TRICOLOR_ICE_CREAM),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("maoyu_lava_tofu"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.TOFU, MIItems.CHILI, Items.BEEF, MIItems.ONION),
                this.ofItem(MIItems.MAOYU_LAVA_TOFU),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("scarlet_devils_cake"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.DEW, Items.PUMPKIN, Items.POTATO, Items.HONEY_BOTTLE),
                this.ofItem(MIItems.SCARLET_DEVILS_CAKE),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("unconscious_monster_mousse"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.TOFU, MIItems.BUTTER, Items.HONEY_BOTTLE, MIItems.ONION),
                this.ofItem(MIItems.UNCONSCIOUS_MONSTER_MOUSSE),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("pickled_cucumbers"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.CUCUMBER, MIItems.BLACK_SALT),
                this.ofItem(MIItems.PICKLED_CUCUMBERS),
                6.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("sea_urchin_sashimi"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.SEA_URCHIN, MIItems.DEW),
                this.ofItem(MIItems.SEA_URCHIN_SASHIMI),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("nigiri_sushi"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.SALMON, MIItems.TUNA),
                this.ofItem(MIItems.NIGIRI_SUSHI),
                6.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("gloomy_fruit_pie"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.LEMON, MIItems.GRAPE, MIItems.CHEESE),
                this.ofItem(MIItems.GLOOMY_FRUIT_PIE),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("crisp_cyclone"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.FLOUR, Items.HONEY_BOTTLE, MIItems.CICADA_SHELL),
                this.ofItem(MIItems.CRISP_CYCLONE),
                5.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("oedo_boat_festival"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.SALMON, MIItems.TUNA, MIItems.SUPREME_TUNA, MIItems.TROUT, ModBlocks.MAGIC_ICE_BLOCK.asItem()),
                this.ofItem(MIItems.OEDO_BOAT_FESTIVAL),
                24.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("cat_food"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.TROUT, MIItems.DEW, MIItems.FLOUR),
                this.ofItem(MIItems.CAT_FOOD),
                5.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("skinny_horse_dumpling"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.STICKY_RICE, MIItems.STICKY_RICE),
                this.ofItem(MIItems.SKINNY_HORSE_DUMPLING),
                9.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("gensokyo_star_lotus_ship"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.PUMPKIN, MIItems.LOTUS_NUTS, MIItems.TUNA, MIItems.TWIN_LOTUS, MIItems.MOONFLOWER),
                this.ofItem(MIItems.GENSOKYO_STAR_LOTUS_SHIP),
                13.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("candied_chestnuts"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.HONEY_BOTTLE, MIItems.CHESTNUT),
                this.ofItem(MIItems.CANDIED_CHESTNUTS),
                6.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("reversing_the_world"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.BAMBOO, MIItems.FLOWERS, MIItems.PLUM, MIItems.BLACK_PORK, MIItems.TRUFFLE),
                this.ofItem(MIItems.REVERSING_THE_WORLD),
                12.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("red_bean_daifuku"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.RED_BEANS, MIItems.STICKY_RICE),
                this.ofItem(MIItems.RED_BEAN_DAIFUKU),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("bamboo_tube_roasted_drunken_shrimp"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.BAMBOO, MIItems.SHRIMP, MIItems.BROCCOLI),
                this.ofItem(MIItems.BAMBOO_TUBE_ROASTED_DRUNKEN_SHRIMP),
                5.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("cats_playing_in_water"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.PEACH, MIItems.FICUS_MICROCARPA, MIItems.CREAM, MIItems.FLOUR, Items.COCOA_BEANS),
                this.ofItem(MIItems.CATS_PLAYING_IN_WATER),
                12.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("moonlight_over_lotus_pond"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(MIItems.GRAPE, MIItems.FICUS_MICROCARPA, MIItems.CREAM, MIItems.TREMELLA),
                this.ofItem(MIItems.MOONLIGHT_OVER_LOTUS_POND),
                12.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("longyin_peach"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.COCOA_BEANS, MIItems.PEACH, MIItems.PEACH, MIItems.PEACH, MIItems.PEACH),
                this.ofItem(MIItems.LONGYIN_PEACH),
                18.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("molecular_egg"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.COCOA_BEANS, Items.PUMPKIN, MIItems.CREAM),
                this.ofItem(MIItems.MOLECULAR_EGG),
                18.0
        ));
        // 蒸锅
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("dew_boiled_eggs"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.DEW, Items.EGG),
                this.ofItem(MIItems.DEW_BOILED_EGGS),
                3.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("udumbara_cake"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.UDUMBARA, MIItems.DEW),
                this.ofItem(MIItems.UDUMBARA_CAKE),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("bear_paw"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.BLACK_PORK, MIItems.BAMBOO_SHOOTS, Items.PUFFERFISH),
                this.ofItem(MIItems.BEAR_PAW),
                12.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("poetry_and_ginkgo"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.GINKGO, Items.HONEY_BOTTLE),
                this.ofItem(MIItems.POETRY_AND_GINKGO),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("risotto"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.ONION, Items.BROWN_MUSHROOM, MIItems.BAMBOO_SHOOTS, MIItems.BUTTER),
                this.ofItem(MIItems.RISOTTO),
                6.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("scones"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.BUTTER, MIItems.FLOUR),
                this.ofItem(MIItems.SCONES),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("cream_stew"), new KitchenRecipe(
                streamer,
                this.ofList(Items.BROWN_MUSHROOM, MIItems.ONION, MIItems.BUTTER),
                this.ofItem(MIItems.CREAM_STEW),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("taketorihime"), new KitchenRecipe(
                streamer,
                this.ofList(Items.BAMBOO, MIItems.BAMBOO_SHOOTS, MIItems.TRUFFLE, MIItems.GINKGO, MIItems.BLACK_PORK),
                this.ofItem(MIItems.TAKETORIHIME),
                12.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("pig_deer_butterfly"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.WILD_BOAR_MEAT, MIItems.VENISON, MIItems.MOONFLOWER),
                this.ofItem(MIItems.PIG_DEER_BUTTERFLY),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("bamboo_steamed_egg"), new KitchenRecipe(
                streamer,
                this.ofList(Items.BAMBOO, Items.EGG, Items.KELP, Items.BROWN_MUSHROOM),
                this.ofItem(MIItems.BAMBOO_STEAMED_EGG),
                6.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("moon_cake"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.MOONFLOWER, MIItems.FLOUR),
                this.ofItem(MIItems.MOON_CAKE),
                10.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("ordinary_small_cake"), new KitchenRecipe(
                streamer,
                this.ofList(Items.EGG, MIItems.GRAPE, MIItems.CREAM),
                this.ofItem(MIItems.ORDINARY_SMALL_CAKE),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("seven_colored_yokan"), new KitchenRecipe(
                streamer,
                this.ofList(Items.KELP, MIItems.GRAPE, MIItems.DEW, MIItems.UDUMBARA),
                this.ofItem(MIItems.SEVEN_COLORED_YOKAN),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("pumpkin_shrimp_cake"), new KitchenRecipe(
                streamer,
                this.ofList(Items.PUMPKIN, MIItems.SHRIMP, MIItems.TOFU),
                this.ofItem(MIItems.PUMPKIN_SHRIMP_CAKE),
                9.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("depressed_cheese_sticks"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.CHEESE, MIItems.GINKGO, MIItems.GINKGO),
                this.ofItem(MIItems.DEPRESSED_CHEESE_STICKS),
                6.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("looking_up_at_the_ceiling_fruit_pie"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.TROUT, MIItems.FLOUR, MIItems.PEACH),
                this.ofItem(MIItems.LOOKING_UP_AT_THE_CEILING_FRUIT_PIE),
                9.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("beetle_steamed_cake"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.FLOUR, MIItems.BLACK_PORK, Items.HONEY_BOTTLE, MIItems.CICADA_SHELL),
                this.ofItem(MIItems.BEETLE_STEAMED_CAKE),
                12.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("sakura_pudding"), new KitchenRecipe(
                streamer,
                this.ofList(Items.HONEY_BOTTLE, MIItems.PEACH),
                this.ofItem(MIItems.SAKURA_PUDDING),
                6.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("refreshing_pudding"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.GRAPE, MIItems.GRAPE, MIItems.LEMON),
                this.ofItem(MIItems.REFRESHING_PUDDING),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("burnt_pudding"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.GRAPE, Items.HONEY_BOTTLE, MIItems.LEMON, MIItems.LEMON),
                this.ofItem(MIItems.BURNT_PUDDING),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("fish_leaps_over_dragon_gate"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.SUPREME_TUNA, MIItems.VENISON, Items.HONEY_BOTTLE, MIItems.MOONFLOWER, MIItems.TRUFFLE),
                this.ofItem(MIItems.FISH_LEAPS_OVER_DRAGON_GATE),
                12.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("fright_adventure"), new KitchenRecipe(
                streamer,
                this.ofList(Items.BROWN_MUSHROOM, MIItems.UDUMBARA, Items.HONEY_BOTTLE, MIItems.CREAM),
                this.ofItem(MIItems.FRIGHT_ADVENTURE),
                12.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("holy_white_lotus_seed_cake"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.GINKGO, MIItems.LOTUS_NUTS, MIItems.FLOUR, MIItems.BUTTER),
                this.ofItem(MIItems.HOLY_WHITE_LOTUS_SEED_CAKE),
                10.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("pine_nut_cake"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.STICKY_RICE, MIItems.PINE_NUT),
                this.ofItem(MIItems.PINE_NUT_CAKE),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("shiraga_sadamatsu"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.VENISON, MIItems.GINKGO, MIItems.PINE_NUT),
                this.ofItem(MIItems.SHIRAGA_SADAMATSU),
                12.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("lotus_fish_rice_bowl"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.SUPREME_TUNA, MIItems.TWIN_LOTUS, MIItems.LOTUS_NUTS, MIItems.DEW),
                this.ofItem(MIItems.LOTUS_FISH_RICE_BOWL),
                11.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("bamboo_tube_steamed_pork"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.BAMBOO_SHOOTS, MIItems.DEW, MIItems.BLACK_PORK),
                this.ofItem(MIItems.BAMBOO_TUBE_STEAMED_PORK),
                9.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("green_bamboo_welcomes_spring"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.CUCUMBER, Items.EGG, MIItems.WHITE_RADISH, MIItems.VENISON, MIItems.MOONFLOWER),
                this.ofItem(MIItems.GREEN_BAMBOO_WELCOMES_SPRING),
                14.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("steamed_egg_with_sea_urchin"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.SEA_URCHIN, Items.EGG),
                this.ofItem(MIItems.STEAMED_EGG_WITH_SEA_URCHIN),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("flowers_birds_wind_and_moon"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.FLOWERS, MIItems.MOONFLOWER, MIItems.CREAM),
                this.ofItem(MIItems.FLOWERS_BIRDS_WIND_AND_MOON),
                9.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("the_dream"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.FLOWERS, MIItems.UDUMBARA, MIItems.MOONFLOWER, MIItems.DEW, MIItems.CREAM),
                this.ofItem(MIItems.THE_DREAM),
                12.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("a_little_sweet_poison"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.UDUMBARA, MIItems.CREAM, MIItems.GRAPE, MIItems.GINKGO),
                this.ofItem(MIItems.A_LITTLE_SWEET_POISON),
                10.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("rapunzel"), new KitchenRecipe(
                streamer,
                this.ofList(Items.PUMPKIN, MIItems.SHRIMP),
                this.ofItem(MIItems.RAPUNZEL),
                5.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("mad_hatter_tea_party"), new KitchenRecipe(
                streamer,
                this.ofList(Items.COCOA_BEANS, MIItems.CREAM, MIItems.FLOUR, Items.BROWN_MUSHROOM_BLOCK, MIItems.BROCCOLI),
                this.ofItem(MIItems.MAD_HATTER_TEA_PARTY),
                15.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("peach_blossom_glaze_roll"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.PEACH, MIItems.RED_BEANS, MIItems.FICUS_MICROCARPA),
                this.ofItem(MIItems.PEACH_BLOSSOM_GLAZE_ROLL),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("the_source_of_life"), new KitchenRecipe(
                streamer,
                this.ofList(Items.COCOA_BEANS, MIItems.TREMELLA, Items.PUMPKIN, MIItems.DEW),
                this.ofItem(MIItems.THE_SOURCE_OF_LIFE),
                13.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("the_mars"), new KitchenRecipe(
                streamer,
                this.ofList(MIItems.FICUS_MICROCARPA, MIItems.GRAPE, MIItems.CRAB, MIItems.DEW),
                this.ofItem(MIItems.THE_MARS),
                24.0
        ));
        // 炒锅
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("fried_pork_shreds"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.PORKCHOP),
                this.ofItem(MIItems.FRIED_PORK_SHREDS),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("braised_eel"), new KitchenRecipe(
                fryingPan,
                this.ofList(MIItems.ONION, MIItems.HAGFISH),
                this.ofItem(MIItems.BRAISED_EEL),
                6.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("potato_croquettes"), new KitchenRecipe(
                fryingPan,
                this.ofList(MIItems.HAGFISH),
                this.ofItem(MIItems.FRIED_HAGFISH),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("deep_fried_cicada_shells"), new KitchenRecipe(
                fryingPan,
                this.ofList(MIItems.TOFU, MIItems.CICADA_SHELL),
                this.ofItem(MIItems.DEEP_FRIED_CICADA_SHELLS),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("fried_pork_cutlet"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.PORKCHOP),
                this.ofItem(MIItems.FRIED_PORK_CUTLET),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("butter_steak"), new KitchenRecipe(
                fryingPan,
                this.ofList(MIItems.WAGYU_BEEF),
                this.ofItem(MIItems.BUTTER_STEAK),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("beef_wellington"), new KitchenRecipe(
                fryingPan,
                this.ofList(MIItems.WAGYU_BEEF, MIItems.FLOUR, Items.EGG, MIItems.BUTTER, MIItems.TRUFFLE),
                this.ofItem(MIItems.BEEF_WELLINGTON),
                14.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("eggs_benedict"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.EGG, MIItems.BAMBOO_SHOOTS, MIItems.BUTTER, MIItems.FLOUR),
                this.ofItem(MIItems.EGGS_BENEDICT),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("hot_waffles"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.HONEY_BOTTLE, MIItems.FLOUR, Items.EGG),
                this.ofItem(MIItems.HOT_WAFFLES),
                9.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("pan_fried_salmon"), new KitchenRecipe(
                fryingPan,
                this.ofList(MIItems.SALMON, MIItems.BAMBOO_SHOOTS),
                this.ofItem(MIItems.PAN_FRIED_SALMON),
                10.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("bamboo_shoots_fried_meat"), new KitchenRecipe(
                fryingPan,
                this.ofList(MIItems.BAMBOO_SHOOTS, Items.PORKCHOP),
                this.ofItem(MIItems.BAMBOO_SHOOTS_FRIED_MEAT),
                10.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("stinky_tofu"), new KitchenRecipe(
                fryingPan,
                this.ofList(MIItems.TOFU, MIItems.CHILI),
                this.ofItem(MIItems.STINKY_TOFU),
                5.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("colorful_jade_fried_buns"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.BROWN_MUSHROOM, MIItems.BLACK_PORK),
                this.ofItem(MIItems.COLORFUL_JADE_FRIED_BUNS),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("mapo_tofu"), new KitchenRecipe(
                fryingPan,
                this.ofList(MIItems.TOFU, Items.PORKCHOP, MIItems.CHILI),
                this.ofItem(MIItems.MAPO_TOFU),
                6.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("fried_shrimp_tempura"), new KitchenRecipe(
                fryingPan,
                this.ofList(MIItems.SHRIMP, MIItems.FLOUR),
                this.ofItem(MIItems.FRIED_SHRIMP_TEMPURA),
                6.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("golden_crispy_fish_cake"), new KitchenRecipe(
                fryingPan,
                this.ofList(MIItems.TROUT, MIItems.FLOUR, Items.HONEY_BOTTLE),
                this.ofItem(MIItems.GOLDEN_CRISPY_FISH_CAKE),
                9.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("baked_crab_with_cream"), new KitchenRecipe(
                fryingPan,
                this.ofList(MIItems.CREAM, MIItems.CRAB),
                this.ofItem(MIItems.BAKED_CRAB_WITH_CREAM),
                12.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("okonomiyaki"), new KitchenRecipe(
                fryingPan,
                this.ofList(MIItems.FLOUR, Items.EGG, MIItems.WHITE_RADISH),
                this.ofItem(MIItems.OKONOMIYAKI),
                6.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("takoyaki"), new KitchenRecipe(
                fryingPan,
                this.ofList(MIItems.FLOUR, Items.KELP, MIItems.OCTOPUS),
                this.ofItem(MIItems.TAKOYAKI),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("mushroom_meat_slices"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.BROWN_MUSHROOM, Items.PORKCHOP),
                this.ofItem(MIItems.MUSHROOM_MEAT_SLICES),
                6.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("giant_tamagoyaki"), new KitchenRecipe(
                fryingPan,
                this.ofList(MIItems.FLOUR, MIItems.FLOUR, Items.EGG, Items.EGG),
                this.ofItem(MIItems.GIANT_TAMAGOYAKI),
                12.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("salmon_tempura"), new KitchenRecipe(
                fryingPan,
                this.ofList(MIItems.SALMON, MIItems.BUTTER, Items.EGG, MIItems.FLOUR),
                this.ofItem(MIItems.SALMON_TEMPURA),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("cheese_egg"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.EGG, MIItems.CHEESE),
                this.ofItem(MIItems.CHEESE_EGG),
                6.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("hell_thrill_warning"), new KitchenRecipe(
                fryingPan,
                this.ofList(MIItems.CHILI, MIItems.CHILI, MIItems.CHILI, MIItems.CHEESE, Items.BEEF),
                this.ofItem(MIItems.HELL_THRILL_WARNING),
                12.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("yunshan_cotton_candy"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.HONEY_BOTTLE, MIItems.PEACH),
                this.ofItem(MIItems.YUNSHAN_COTTON_CANDY),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("candied_sweet_potato"), new KitchenRecipe(
                fryingPan,
                this.ofList(MIItems.SWEET_POTATO, Items.HONEY_BOTTLE),
                this.ofItem(MIItems.CANDIED_SWEET_POTATO),
                6.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("pan_fried_mushroom_meat_roll"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.PORKCHOP, Items.BROWN_MUSHROOM, MIItems.TRUFFLE),
                this.ofItem(MIItems.PAN_FRIED_MUSHROOM_MEAT_ROLL),
                9.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("assorted_tempura"), new KitchenRecipe(
                fryingPan,
                this.ofList(MIItems.BLACK_PORK, MIItems.TRUFFLE, MIItems.HAGFISH, MIItems.MOONFLOWER),
                this.ofItem(MIItems.ASSORTED_TEMPURA),
                7.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("fried_tomato_strips"), new KitchenRecipe(
                fryingPan,
                this.ofList(MIItems.TOMATO, Items.POTATO),
                this.ofItem(MIItems.FRIED_TOMATO_STRIPS),
                6.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("braised_pork_with_peach"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.HONEY_BOTTLE, MIItems.PEACH, Items.PORKCHOP),
                this.ofItem(MIItems.BRAISED_PORK_WITH_PEACH),
                8.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("dorayaki"), new KitchenRecipe(
                fryingPan,
                this.ofList(MIItems.RED_BEANS, Items.EGG, MIItems.FLOUR),
                this.ofItem(MIItems.DORAYAKI),
                6.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("toon_pancakes"), new KitchenRecipe(
                fryingPan,
                this.ofList(MIItems.TWIN_LOTUS, Items.EGG),
                this.ofItem(MIItems.TOON_PANCAKES),
                6.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("eel_egg_donburi"), new KitchenRecipe(
                fryingPan,
                this.ofList(MIItems.HAGFISH, Items.EGG),
                this.ofItem(MIItems.EEL_EGG_DONBURI),
                5.0
        ));
        this.kitchenRecipeFactory.register(MystiasIzakaya.id("hula_soup"), new KitchenRecipe(
                fryingPan,
                this.ofList(MIItems.CHILI, Items.BEEF, MIItems.TOFU),
                this.ofItem(MIItems.HULA_SOUP),
                8.0
        ));
    }

    public void generateDanmakuRecipe() {
        Stream<DanmakuType> stream = RegistryManager.DANMAKU_TYPE.stream();
        stream.forEach(value -> {
            ResourceLocation key = RegistryManager.DANMAKU_TYPE.getKey(value);
            if (!DanmakuTypes.UNLIST.contains(value)) {
                for (Tuple<Item, ItemStack> pair : value.getColorPairs()) {
                    Item dye = pair.getA();
                    ItemStack result = pair.getB();
                    Item item = result.getItem();
                    ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
                    ResourceLocation dyeId = BuiltInRegistries.ITEM.getKey(dye);
                    ResourceLocation registryKey = ResourceLocation.fromNamespaceAndPath(itemId.getNamespace(), itemId.getPath() + "_dye_" + dyeId.getPath());
                    DanmakuRecipe recipe = new DanmakuRecipe(
                            new ItemStackWrapper(new ItemStack(dye, 4)),
                            new ItemStackWrapper(new ItemStack(ModItems.DANMAKU_CORE, 4)),
                            new ItemStackWrapper(new ItemStack(ModItems.POWER, 35)),
                            new ItemStackWrapper(new ItemStack(ModItems.POINT, 35)),
                            new ItemStackWrapper(value.toShape().getItemStack()),
                            new ItemStackWrapper(result)
                    );
                    this.danmakuRecipeFactory.register(registryKey, recipe);
                }
            }
        });
    }

    @Override
    public String getName() {
        return "Recipe Types";
    }
}
