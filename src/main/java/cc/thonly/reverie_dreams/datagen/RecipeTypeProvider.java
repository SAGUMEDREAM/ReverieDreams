package cc.thonly.reverie_dreams.datagen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.datagen.generator.AbstractRecipeTypeProvider;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuRecipe;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuShapeDrawRecipe;
import cc.thonly.reverie_dreams.recipe.entry.GensokyoAltarRecipe;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.recipe.type.KitchenRecipeType;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.RoleCards;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.effect.RDPotions;
import cc.thonly.reverie_dreams.registry.content.item.RDFoodItems;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class RecipeTypeProvider extends AbstractRecipeTypeProvider {
    public final Factory<GensokyoAltarRecipe> gensokyoAltarRecipeFactory = this.getOrCreateFactory(RecipeManager.GENSOKYO_ALTAR, GensokyoAltarRecipe.class);
    public final Factory<DanmakuRecipe> danmakuRecipeFactory = this.getOrCreateFactory(RecipeManager.DANMAKU_TYPE, DanmakuRecipe.class);
    public final Factory<DanmakuShapeDrawRecipe> danmakuShapeDrawRecipeFactory = this.getOrCreateFactory(RecipeManager.DANMAKU_SHAPE_DRAW_TYPE, DanmakuShapeDrawRecipe.class);
    public final Factory<KitchenRecipe> kitchenRecipeFactory = this.getOrCreateFactory(RecipeManager.KITCHEN_TYPE, KitchenRecipe.class);

    public RecipeTypeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> future) {
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
        DanmakuTypes.NOTE.buildShapeRecipe(this.danmakuShapeDrawRecipeFactory, new String[]{
                "FFFFTF",
                "FFFFTF",
                "FFFFTF",
                "FTTFTF",
                "FTTFFF",
                "FTTFFF"
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
                .itemStack(ItemStackWrapper.of(Items.GLASS_BOTTLE, 20), ItemStackWrapper.of(RDIngredientItems.PEACH, 15))
                .build()
                .apply(this.gensokyoAltarRecipeFactory);

    }

    public void generateGensokyoAltarRecipe() {
        this.gensokyoAltarRecipeFactory.register(RDItems.HORAI_DAMA_NO_EDA,
                new GensokyoAltarRecipe(
                        this.ofItem(Items.DIAMOND_BLOCK),
                        List.of(
                                this.ofItem(RDItems.RED_ORB, 10), this.ofItem(RDItems.BLUE_ORB, 10), this.ofItem(RDItems.YELLOW_ORB, 10),
                                this.ofEmpty(), this.ofItem(RDItems.GREEN_ORB, 10),
                                this.ofItem(Items.STICK), this.ofEmpty(), this.ofItem(RDItems.PURPLE_ORB, 10)
                        ),
                        this.ofItem(RDItems.HORAI_DAMA_NO_EDA)
                )
        );
        this.gensokyoAltarRecipeFactory.register(RDItems.CROSSING_CHISEL, new GensokyoAltarRecipe(
                this.ofItem(Items.GOLDEN_HOE),
                List.of(
                        this.ofItem(Items.ENDER_PEARL, 2), this.ofItem(Items.GOLD_BLOCK), this.ofItem(Items.ENDER_PEARL, 4),
                        this.ofItem(Items.GOLD_INGOT, 4), this.ofItem(Items.GOLD_INGOT, 4),
                        this.ofItem(Items.ENDER_PEARL, 8), this.ofItem(Items.GOLD_BLOCK), this.ofItem(Items.ENDER_PEARL, 16)
                ),
                this.ofItem(RDItems.CROSSING_CHISEL)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.GAP_BALL, new GensokyoAltarRecipe(
                this.ofItem(Items.COMPASS),
                List.of(
                        this.ofItem(Items.PURPLE_DYE, 2), this.ofItem(Items.ENDER_PEARL, 6), this.ofItem(Items.REDSTONE_BLOCK, 1),
                        this.ofItem(Items.MAGENTA_DYE, 2), this.ofItem(Items.COPPER_INGOT),
                        this.ofItem(Items.COPPER_INGOT, 6), this.ofItem(Items.COPPER_INGOT, 7), this.ofItem(Items.COPPER_INGOT, 8)
                ),
                this.ofItem(RDItems.GAP_BALL)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.BAGUA_FURNACE, new GensokyoAltarRecipe(
                this.ofItem(Items.COMPASS),
                List.of(
                        this.ofItem(Items.REDSTONE_BLOCK, 8), this.ofItem(Items.IRON_INGOT, 12), this.ofItem(Items.COAL_BLOCK),
                        this.ofItem(Items.NETHERITE_INGOT, 4), this.ofItem(Items.COPPER_INGOT, 16),
                        this.ofItem(Items.COAL_BLOCK), this.ofItem(Items.IRON_INGOT, 12), this.ofItem(Items.REDSTONE_BLOCK, 8)
                ),
                this.ofItem(RDItems.BAGUA_FURNACE)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.TIME_STOP_CLOCK, new GensokyoAltarRecipe(
                this.ofItem(Items.CLOCK),
                List.of(
                        this.ofItem(Items.PURPLE_DYE, 4), this.ofItem(Items.REDSTONE_BLOCK, 2), this.ofItem(Items.PURPLE_DYE, 4),
                        this.ofItem(Items.GOLD_INGOT, 5), this.ofItem(Items.GOLD_INGOT, 5),
                        this.ofItem(Items.PURPLE_DYE, 4), this.ofItem(Items.REDSTONE_BLOCK, 2), this.ofItem(Items.PURPLE_DYE, 4)
                ),
                this.ofItem(RDItems.TIME_STOP_CLOCK)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.MAPLE_LEAF_FAN, new GensokyoAltarRecipe(
                this.ofItem(Items.OAK_LEAVES, 48),
                List.of(
                        this.ofItem(Items.WIND_CHARGE, 16), this.ofItem(Items.GOLD_INGOT, 9), this.ofItem(Items.WIND_CHARGE, 16),
                        this.ofItem(Items.GOLD_INGOT, 9), this.ofItem(Items.GOLD_INGOT, 9),
                        this.ofItem(Items.BREEZE_ROD, 2), this.ofItem(Items.GOLD_INGOT, 9), this.ofItem(Items.WIND_CHARGE, 16)
                ),
                this.ofItem(RDItems.MAPLE_LEAF_FAN)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.EARPHONE, new GensokyoAltarRecipe(
                this.ofItem(Items.IRON_HELMET),
                List.of(
                        this.ofItem(Items.AMETHYST_SHARD, 5), this.ofItem(Items.GOLD_INGOT, 6), this.ofItem(Items.AMETHYST_SHARD, 5),
                        this.ofItem(Items.SCULK_SENSOR, 9), this.ofItem(Items.SCULK_SENSOR, 9),
                        this.ofEmpty(), this.ofItem(Items.REDSTONE, 18), this.ofEmpty()
                ),
                this.ofItem(RDItems.EARPHONE)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.KOISHI_HAT, new GensokyoAltarRecipe(
                this.ofItem(Items.IRON_HELMET),
                List.of(
                        this.ofItem(Items.BLACK_DYE, 2), this.ofItem(Items.IRON_INGOT, 1), this.ofItem(Items.BLACK_DYE, 2),
                        this.ofItem(Items.IRON_INGOT, 1), this.ofItem(Items.IRON_INGOT, 1),
                        this.ofItem(Items.YELLOW_DYE, 3), this.ofItem(Items.BLACK_DYE, 2), this.ofItem(Items.YELLOW_DYE, 3)
                ),
                this.ofItem(RDItems.KOISHI_HAT)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.HAKUREI_CANE, new GensokyoAltarRecipe(
                this.ofItem(Items.WOODEN_SWORD),
                List.of(
                        this.ofEmpty(), this.ofItem(Items.PAPER, 6), this.ofItem(Items.PAPER, 6),
                        this.ofItem(Items.IRON_INGOT, 1), this.ofItem(Items.PAPER, 6),
                        this.ofItem(Items.STICK, 1), this.ofEmpty(), this.ofItem(Items.PAPER, 3)
                ),
                this.ofItem(RDItems.HAKUREI_CANE)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.WIND_BLESSING_CANE, new GensokyoAltarRecipe(
                this.ofItem(Items.WOODEN_SWORD),
                List.of(
                        this.ofEmpty(), this.ofItem(Items.PAPER, 5), this.ofItem(Items.PAPER, 7),
                        this.ofItem(Items.WIND_CHARGE, 12), this.ofItem(Items.PAPER, 5),
                        this.ofItem(Items.STICK, 1), this.ofEmpty(), this.ofItem(Items.PAPER, 2)
                ),
                this.ofItem(RDItems.WIND_BLESSING_CANE)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.MAGIC_BROOM, new GensokyoAltarRecipe(
                this.ofItem(Items.REDSTONE_BLOCK, 3),
                List.of(
                        this.ofItem(Items.HAY_BLOCK, 2), this.ofItem(Items.LEAD), this.ofItem(Items.SADDLE),
                        this.ofItem(Items.HAY_BLOCK, 2), this.ofItem(Items.STICK, 3),
                        this.ofItem(Items.HAY_BLOCK, 2), this.ofItem(Items.SLIME_BALL), this.ofEmpty()
                ),
                this.ofItem(RDItems.MAGIC_BROOM)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.KNIFE, new GensokyoAltarRecipe(
                this.ofItem(RDBlocks.SILVER_BLOCK, 2),
                List.of(
                        this.ofItem(RDItems.SILVER_SWORD), this.ofEmpty(), this.ofItem(RDItems.SILVER_INGOT, 3),
                        this.ofItem(RDItems.SILVER_SWORD), this.ofEmpty(),
                        this.ofItem(Items.STICK, 3), this.ofEmpty(), this.ofEmpty()
                ),
                this.ofItem(RDItems.KNIFE)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.ROKANKEN, new GensokyoAltarRecipe(
                this.ofItem(Items.IRON_SWORD),
                List.of(
                        this.ofItem(Items.SOUL_SAND, 7), this.ofItem(Items.SOUL_SAND, 8), this.ofItem(RDItems.SILVER_INGOT, 12),
                        this.ofItem(Items.SOUL_SAND, 6), this.ofEmpty(),
                        this.ofItem(Items.STRIPPED_CHERRY_LOG, 4), this.ofEmpty(), this.ofEmpty()
                ),
                this.ofItem(RDItems.ROKANKEN)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.HAKUROKEN, new GensokyoAltarRecipe(
                this.ofItem(Items.IRON_SWORD),
                List.of(
                        this.ofItem(Items.SOUL_SAND, 8), this.ofItem(Items.SOUL_SAND, 9), this.ofItem(RDItems.SILVER_INGOT, 12),
                        this.ofItem(Items.SOUL_SAND, 7), this.ofEmpty(),
                        this.ofItem(Items.STRIPPED_CHERRY_LOG, 4), this.ofEmpty(), this.ofItem(RDItems.SPEED_FEATHER, 4)
                ),
                this.ofItem(RDItems.HAKUROKEN)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.PAPILIO_PATTERN_FAN, new GensokyoAltarRecipe(
                this.ofItem(Items.IRON_INGOT, 1),
                List.of(
                        this.ofEmpty(), this.ofItem(Items.BLUE_WOOL, 3), this.ofItem(Items.PURPLE_WOOL, 3),
                        this.ofItem(Items.SOUL_SAND, 9), this.ofItem(Items.PINK_WOOL, 3),
                        this.ofItem(Items.STRIPPED_CHERRY_LOG, 5), this.ofItem(Items.CHERRY_SAPLING), this.ofEmpty()
                ),
                this.ofItem(RDItems.PAPILIO_PATTERN_FAN)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.GUNGNIR, new GensokyoAltarRecipe(
                this.ofItem(Items.DIAMOND_AXE),
                List.of(
                        this.ofItem(Items.DIAMOND, 2), this.ofItem(Items.IRON_INGOT, 5), this.ofItem(Items.REDSTONE_BLOCK, 2),
                        this.ofItem(Items.SOUL_SAND, 9), this.ofItem(Items.IRON_INGOT, 5),
                        this.ofItem(Items.BREEZE_ROD, 5), this.ofItem(Items.SOUL_SAND, 6), this.ofEmpty()
                ),
                this.ofItem(RDItems.GUNGNIR)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.LEVATIN, new GensokyoAltarRecipe(
                this.ofItem(Items.NETHERITE_HOE),
                List.of(
                        this.ofEmpty(), this.ofItem(Items.GOLD_INGOT, 5), this.ofItem(Items.NETHERITE_INGOT, 1),
                        this.ofItem(Items.REDSTONE, 3), this.ofItem(Items.IRON_INGOT, 5),
                        this.ofItem(Items.BLAZE_ROD, 4), this.ofItem(Items.SOUL_SAND, 4), this.ofItem(Items.COAL, 11)
                ),
                this.ofItem(RDItems.LEVATIN)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.IBUKIHO, new GensokyoAltarRecipe(
                this.ofItem(Items.HONEY_BOTTLE),
                List.of(
                        this.ofItem(Items.GOLD_INGOT, 8), this.ofItem(Items.GOLD_INGOT, 5), this.ofItem(Items.SUGAR, 1),
                        this.ofItem(Items.REDSTONE, 8), this.ofItem(Items.GHAST_TEAR, 8),
                        this.ofItem(Items.DIAMOND, 4), this.ofItem(Items.BLAZE_ROD, 8), this.ofItem(Items.FERMENTED_SPIDER_EYE, 8)
                ),
                this.ofItem(RDItems.IBUKIHO)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.SWORD_OF_HISOU, new GensokyoAltarRecipe(
                this.ofItem(Items.COPPER_SWORD),
                List.of(
                        this.ofItem(Items.GOLD_INGOT, 7), this.ofItem(Items.DIAMOND, 5), this.ofItem(Items.COPPER_INGOT, 2),
                        this.ofItem(Items.REDSTONE, 6), this.ofItem(Items.REDSTONE, 6),
                        this.ofItem(Items.BLAZE_ROD, 4), this.ofItem(RDIngredientItems.PEACH, 7), this.ofItem(Items.APPLE, 6)
                ),
                this.ofItem(RDItems.SWORD_OF_HISOU)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.MANPOZUCHI, new GensokyoAltarRecipe(
                this.ofItem(Items.MACE),
                List.of(
                        this.ofItem(Items.GOLD_INGOT, 2), this.ofItem(Items.END_ROD, 1), this.ofItem(Items.IRON_INGOT, 2),
                        this.ofItem(Items.REDSTONE, 8), this.ofItem(Items.COPPER_INGOT, 7),
                        this.ofItem(Items.IRON_NUGGET, 18), this.ofItem(Items.DIAMOND, 4), this.ofItem(Items.GOLD_NUGGET, 18)
                ),
                this.ofItem(RDItems.MANPOZUCHI)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.NUE_TRIDENT, new GensokyoAltarRecipe(
                this.ofItem(Items.TRIDENT),
                List.of(
                        this.ofEmpty(), this.ofEmpty(), this.ofItem(Items.COMPASS),
                        this.ofItem(Items.HEART_OF_THE_SEA), this.ofItem(Items.COD, 3),
                        this.ofItem(Items.NETHERITE_INGOT), this.ofItem(Items.OAK_LOG, 8), this.ofEmpty()
                ),
                this.ofItem(RDItems.NUE_TRIDENT)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.TREASURE_HUNTING_ROD, new GensokyoAltarRecipe(
                this.ofItem(Items.DIAMOND_PICKAXE),
                List.of(
                        this.ofItem(Items.IRON_INGOT, 5), this.ofItem(Items.IRON_INGOT, 6), this.ofItem(Items.RABBIT_FOOT, 9),
                        this.ofItem(Items.NETHERITE_INGOT), this.ofItem(Items.GOLD_INGOT, 10),
                        this.ofItem(Items.NETHERITE_INGOT), this.ofItem(Items.REDSTONE, 8), this.ofItem(Items.STONE_BUTTON, 1)
                ),
                this.ofItem(RDItems.TREASURE_HUNTING_ROD)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.TRUMPET_GUN, new GensokyoAltarRecipe(
                this.ofItem(Items.CROSSBOW),
                List.of(
                        this.ofItem(RDBlocks.POWER_BLOCK, 5), this.ofItem(Items.FERMENTED_SPIDER_EYE, 2), this.ofItem(Items.RABBIT_FOOT, 5),
                        this.ofItem(RDBlocks.POWER_BLOCK, 4), this.ofItem(Items.GOLD_INGOT, 5),
                        this.ofItem(Items.HONEYCOMB), this.ofItem(Items.IRON_NUGGET, 3), this.ofItem(Items.BLAZE_ROD, 3)
                ),
                this.ofItem(RDItems.TRUMPET_GUN)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.DEATH_SCYTHE, new GensokyoAltarRecipe(
                this.ofItem(Items.NETHERITE_HOE),
                List.of(
                        this.ofItem(Items.SOUL_SAND, 32), this.ofItem(Items.SOUL_SAND, 16), this.ofItem(Items.SOUL_SAND, 8),
                        this.ofItem(Items.NETHERITE_INGOT, 1), this.ofItem(Items.DIAMOND, 5),
                        this.ofItem(Items.ROTTEN_FLESH, 8), this.ofItem(Items.BONE, 7)
                ),
                this.ofItem(RDItems.DEATH_SCYTHE)
        ));
        ItemStack kanjuKusuri = RDPotions.createStack(RDPotions.KANJU_KUSURI_POTION);
        this.gensokyoAltarRecipeFactory.register(ReverieDreams.id("kanju_kusuri"), new GensokyoAltarRecipe(
                this.ofItem(Items.GLASS_BOTTLE),
                List.of(
                        this.ofItem(Items.SOUL_SAND, 20), this.ofItem(Items.SAND, 20), this.ofItem(Items.NETHER_WART, 5),
                        this.ofItem(Items.BLAZE_POWDER, 8), this.ofItem(Items.GOLDEN_APPLE, 1),
                        this.ofItem(Items.GOLDEN_CARROT, 2), this.ofItem(RDItems.SILVER_INGOT, 4), this.ofItem(Items.ENDER_PEARL, 1)
                ),
                this.ofItem(kanjuKusuri)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.CURSED_DECOY_DOLl, new GensokyoAltarRecipe(
                        this.ofItem(Items.ARMOR_STAND),
                        List.of(
                                this.ofItem(Items.SOUL_SAND, 6), this.ofItem(Items.SOUL_SAND, 6), this.ofItem(Items.SOUL_SAND, 6),
                                this.ofItem(Items.SOUL_SAND, 6), this.ofItem(Items.SOUL_SAND, 6),
                                this.ofItem(Items.SOUL_SAND, 6), this.ofItem(Items.SOUL_SAND, 6), this.ofItem(Items.SOUL_SAND, 6)
                        ),
                        this.ofItem(RDItems.CURSED_DECOY_DOLl)
                )
        );
        this.gensokyoAltarRecipeFactory.register(RDItems.VAISRAVANAS_PAGODA, new GensokyoAltarRecipe(
                this.ofItem(RDBlocks.POWER_BLOCK, 10),
                List.of(
                        this.ofItem(Items.STONE, 15), this.ofItem(Items.GOLD_INGOT, 20), this.ofItem(Items.STONE, 15),
                        this.ofItem(Items.COPPER_INGOT, 10), this.ofItem(Items.BLAZE_POWDER, 8),
                        this.ofItem(Items.STONE, 15), this.ofItem(Items.IRON_INGOT, 20), this.ofItem(Items.STONE, 15)
                ),
                this.ofItem(RDItems.VAISRAVANAS_PAGODA)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.TENGU_SHIELD, new GensokyoAltarRecipe(
                this.ofItem(Items.SHIELD, 1),
                List.of(
                        this.ofItem(Items.FEATHER, 3), this.ofItem(Items.FEATHER, 5), this.ofItem(Items.FEATHER, 2),
                        this.ofItem(RDItems.SILVER_INGOT, 7), this.ofItem(RDItems.SILVER_INGOT, 7),
                        this.ofItem(Items.DIAMOND, 3), this.ofItem(Items.IRON_INGOT, 6), this.ofItem(Items.BONE, 3)
                ),
                this.ofItem(RDItems.TENGU_SHIELD)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.TENGU_CAMERA, new GensokyoAltarRecipe(
                this.ofItem(Items.IRON_BLOCK, 1),
                List.of(
                        this.ofItem(Items.GLOWSTONE_DUST, 8), this.ofItem(Items.STONE_BUTTON, 2), this.ofItem(Items.GLOWSTONE_DUST, 8),
                        this.ofItem(RDItems.SILVER_INGOT, 6), this.ofItem(Items.IRON_INGOT, 6),
                        this.ofItem(Items.REDSTONE, 23), this.ofItem(Items.IRON_INGOT, 6), this.ofItem(Items.REDSTONE, 13)
                ),
                this.ofItem(RDItems.TENGU_CAMERA)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.BAD_APPLE, new GensokyoAltarRecipe(
                this.ofItem(Items.GOLDEN_APPLE, 1),
                List.of(
                        this.ofItem(Items.BLACK_DYE, 2), this.ofItem(RDItems.POWER, 11), this.ofItem(Items.BLACK_DYE, 2),
                        this.ofItem(Items.REDSTONE, 6), this.ofItem(Items.REDSTONE, 6),
                        this.ofItem(Items.BLACK_DYE, 2), this.ofItem(RDItems.POINT, 8), this.ofItem(Items.BLACK_DYE, 2)
                ),
                this.ofItem(RDItems.BAD_APPLE)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.EXORCISM_PAPER, new GensokyoAltarRecipe(
                this.ofItem(Items.PAPER, 6),
                List.of(
                        this.ofItem(Items.BONE, 12), this.ofItem(Items.ROTTEN_FLESH, 12), this.ofItem(Items.RED_DYE, 3),
                        this.ofItem(Items.PAPER, 6), this.ofItem(Items.PAPER, 8),
                        this.ofItem(Items.PAPER, 6), this.ofItem(Items.PAPER, 8), this.ofItem(Items.REDSTONE, 7)
                ),
                this.ofItem(RDItems.EXORCISM_PAPER, 4)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.YUKA_FLOWER_UMBRELLA, new GensokyoAltarRecipe(
                this.ofItem(Items.IRON_SWORD),
                List.of(
                        this.ofItem(Items.SUNFLOWER, 16), this.ofItem(Items.NETHERITE_INGOT), this.ofItem(Items.WHITE_WOOL, 8),
                        this.ofItem(Items.ROSE_BUSH, 16), this.ofItem(RDIngredientItems.UDUMBARA, 4),
                        this.ofItem(Items.FLINT, 1), this.ofItem(Items.BAMBOO, 2), this.ofItem(RDItems.POWER, 36)
                ),
                this.ofItem(RDItems.YUKA_FLOWER_UMBRELLA)
        ));
        this.gensokyoAltarRecipeFactory.register(RDItems.SATORI_EYE, new GensokyoAltarRecipe(
                this.ofItem(Items.ENDER_EYE),
                List.of(
                        this.ofItem(Items.SPIDER_EYE, 2), this.ofItem(Items.ENDER_PEARL, 2), this.ofItem(Items.GOLD_INGOT, 3),
                        this.ofItem(Items.IRON_INGOT, 5), this.ofItem(Items.IRON_INGOT, 5),
                        this.ofItem(Items.REDSTONE, 4), this.ofItem(RDItems.POINT, 12), this.ofItem(RDItems.POWER, 12)
                ),
                this.ofItem(RDItems.SATORI_EYE)
        ));
    }

    public void generateKitchenRecipe() {
        Identifier cookingPot = KitchenRecipeType.KitchenType.COOKING_POT.toId();
        Identifier grill = KitchenRecipeType.KitchenType.GRILL.toId();
        Identifier cuttingBoard = KitchenRecipeType.KitchenType.CUTTING_BOARD.toId();
        Identifier streamer = KitchenRecipeType.KitchenType.STREAMER.toId();
        Identifier fryingPan = KitchenRecipeType.KitchenType.FRYING_PAN.toId();

        // 煮锅
        this.kitchenRecipeFactory.register(ReverieDreams.id("seafood_miso_soup"), new KitchenRecipe(
                cookingPot,
                List.of(
                        this.ofItem(Items.KELP)
                ),
                this.ofItem(RDFoodItems.SEAFOOD_MISO_SOUP),
                6.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("tofu_miso"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.TOFU),
                this.ofItem(RDFoodItems.TOFU_MISO),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("strength_soup"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.WILD_BOAR_MEAT, Items.KELP),
                this.ofItem(RDFoodItems.STRENGTH_SOUP),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("game_soup"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.POTATO, Items.PUMPKIN, RDIngredientItems.BLACK_PORK),
                this.ofItem(RDFoodItems.GAME_SOUP),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("pork_rice"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.PORKCHOP),
                this.ofItem(RDFoodItems.PORK_RICE),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("beef_rice"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.BEEF),
                this.ofItem(RDFoodItems.BEEF_RICE),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("snow_white"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.PUFFERFISH, RDIngredientItems.HAGFISH, Items.KELP),
                this.ofItem(RDFoodItems.SNOW_WHITE),
                12.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("tofu_pot"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.TOFU),
                this.ofItem(RDFoodItems.TOFU_POT),
                5.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("zhaji"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.KELP, RDIngredientItems.TOFU, RDIngredientItems.TROUT),
                this.ofItem(RDFoodItems.ZHAJI),
                5.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("grand_banquet"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.BLACK_PORK, RDIngredientItems.WAGYU_BEEF, RDIngredientItems.PUFF_YO_FRUIT),
                this.ofItem(RDFoodItems.GRAND_BANQUET),
                10.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("tonkotsu_ramen"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.PORKCHOP, Items.EGG, Items.KELP),
                this.ofItem(RDFoodItems.TONKOTSU_RAMEN),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("magma"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.BEEF, RDIngredientItems.WAGYU_BEEF, RDIngredientItems.PUFF_YO_FRUIT, RDIngredientItems.TRUFFLE),
                this.ofItem(RDFoodItems.MAGMA),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("peach_blossom_soup"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.PEACH, RDBlocks.MAGIC_ICE_BLOCK.asItem(), RDIngredientItems.DEW),
                this.ofItem(RDFoodItems.PEACH_BLOSSOM_SOUP),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("real_seafood_miso_soup"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.SALMON, RDIngredientItems.TROUT),
                this.ofItem(RDFoodItems.REAL_SEAFOOD_MISO_SOUP),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("cooking_tofu"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.TOFU),
                this.ofItem(RDFoodItems.COOKING_TOFU),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("ginko_and_radish_pork_rib_soup"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.GINKGO, RDIngredientItems.WHITE_RADISH, Items.PORKCHOP),
                this.ofItem(RDFoodItems.GINKGO_AND_RADISH_PORK_RIB_SOUP),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("boiled_fish"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.TROUT, RDIngredientItems.CHILI),
                this.ofItem(RDFoodItems.BOILED_FISH),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("dumpling"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.FLOUR),
                this.ofItem(RDFoodItems.DUMPLING),
                5.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("glutinous_rice_balls"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.STICKY_RICE),
                this.ofItem(RDFoodItems.GLUTINOUS_RICE_BALLS),
                5.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("pseudo_jiritama"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.VENISON, RDIngredientItems.TRUFFLE, RDIngredientItems.CICADA_SHELL),
                this.ofItem(RDFoodItems.PSEUDO_JIRITAMA),
                12.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("secret_mushroom_casserole"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.TRUFFLE, Items.BROWN_MUSHROOM, RDIngredientItems.DEW),
                this.ofItem(RDFoodItems.SECRET_MUSHROOM_CASSEROLE),
                9.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("mushroom_girls_dance_stew"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.BROWN_MUSHROOM, RDIngredientItems.SHRIMP, RDIngredientItems.OCTOPUS, RDIngredientItems.CHILI),
                this.ofItem(RDFoodItems.MUSHROOM_GIRLS_DANCE_STEW),
                14.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("milky_mushroom_soup"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.BROWN_MUSHROOM, Items.POTATO, RDIngredientItems.CREAM),
                this.ofItem(RDFoodItems.MILKY_MUSHROOM_SOUP),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("gensokyo_buddha_jumps_over_the_wall"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.WAGYU_BEEF, RDIngredientItems.SUPREME_TUNA, RDIngredientItems.BLACK_PORK, Items.PUFFERFISH, RDIngredientItems.TRUFFLE),
                this.ofItem(RDFoodItems.GENSOKYO_BUDDHA_JUMPS_OVER_THE_WALL),
                18.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("screaming_oden"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.CHILI, RDIngredientItems.CHILI, Items.BEEF, RDIngredientItems.WHITE_RADISH, RDIngredientItems.TOFU),
                this.ofItem(RDFoodItems.SCREAMING_ODEN),
                12.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("lion_head"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.BEEF),
                this.ofItem(RDFoodItems.LION_HEAD),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("luohan_vegetarian"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.UDUMBARA, RDIngredientItems.BAMBOO_SHOOTS, RDIngredientItems.TRUFFLE, RDIngredientItems.PINE_NUT, RDIngredientItems.LOTUS_NUTS),
                this.ofItem(RDFoodItems.LUOHAN_VEGETARIAN),
                12.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("taichi_bagua_fish_maw"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.SUPREME_TUNA, Items.BROWN_MUSHROOM, RDIngredientItems.WHITE_RADISH, Items.EGG, RDIngredientItems.GINKGO),
                this.ofItem(RDFoodItems.TAICHI_BAGUA_FISH_MAW),
                14.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("tianshi_braised_chestnut_mushrooms"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.CHESTNUT, Items.BROWN_MUSHROOM, RDIngredientItems.TRUFFLE),
                this.ofItem(RDFoodItems.TIANSHI_BRAISED_CHESTNUT_MUSHROOMS),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("the_beauty_of_han_palace"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.HAGFISH, RDIngredientItems.TOFU, RDIngredientItems.CRAB, Items.BAMBOO, RDIngredientItems.DEW),
                this.ofItem(RDFoodItems.THE_BEAUTY_OF_HAN_PALACE),
                12.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("bamboo_shoots_stewed_in_stone_pot"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.BAMBOO, RDIngredientItems.BAMBOO_SHOOTS, Items.BEEF),
                this.ofItem(RDFoodItems.BAMBOO_SHOOTS_STEWED_IN_STONE_POT),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("plum_tea_rice"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.PLUM, Items.KELP),
                this.ofItem(RDFoodItems.PLUM_TEA_RICE),
                4.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("green_fairy_mushroom"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.TOON, Items.BROWN_MUSHROOM),
                this.ofItem(RDFoodItems.GREEN_FAIRY_MUSHROOM),
                6.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("poisonous_garden"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.PUFFERFISH, RDIngredientItems.PLUM, RDIngredientItems.HAGFISH, RDIngredientItems.GINKGO),
                this.ofItem(RDFoodItems.POISONOUS_GARDEN),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("beef_hot_pot"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.CHILI, RDIngredientItems.WHITE_RADISH, RDIngredientItems.TRUFFLE, Items.BEEF, RDIngredientItems.WAGYU_BEEF),
                this.ofItem(RDFoodItems.BEEF_HOT_POT),
                5.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("sea_urchin_shingen_pancake"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.SEA_URCHIN, RDIngredientItems.TUNA, RDIngredientItems.TREMELLA, RDIngredientItems.DEW),
                this.ofItem(RDFoodItems.SEA_URCHIN_SHINGEN_PANCAKE),
                12.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("heart_porridge_gruel"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.TREMELLA, RDIngredientItems.LOTUS_NUTS),
                this.ofItem(RDFoodItems.HEART_PORRIDGE_GRUEL),
                5.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("superme_seafood_noodles"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.SUPREME_TUNA, Items.KELP, RDIngredientItems.OCTOPUS, RDIngredientItems.CRAB, RDIngredientItems.SHRIMP),
                this.ofItem(RDFoodItems.SUPERME_SEAFOOD_NOODLES),
                12.0
        ));
        // 烧烤架
        this.kitchenRecipeFactory.register(ReverieDreams.id("pork_and_trout_smoked"), new KitchenRecipe(
                grill,
                this.ofList(RDIngredientItems.TROUT, Items.PORKCHOP),
                this.ofItem(RDFoodItems.PORK_AND_TROUT_SMOKED),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("grilled_hagfish"), new KitchenRecipe(
                grill,
                this.ofList(RDIngredientItems.HAGFISH),
                this.ofItem(RDFoodItems.GRILLED_HAGFISH),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("energy_string"), new KitchenRecipe(
                grill,
                this.ofList(Items.BEEF, RDIngredientItems.ONION, Items.PUMPKIN),
                this.ofItem(RDFoodItems.ENERGY_STRING),
                12.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("two_heavens_one_style"), new KitchenRecipe(
                grill,
                this.ofList(RDIngredientItems.BLACK_PORK, RDIngredientItems.WILD_BOAR_MEAT),
                this.ofItem(RDFoodItems.TWO_HEAVENS_ONE_STYLE),
                18.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("roasted_mushrooms"), new KitchenRecipe(
                grill,
                this.ofList(Items.BROWN_MUSHROOM),
                this.ofItem(RDFoodItems.ROASTED_MUSHROOMS),
                6.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("honey_bbq_pork"), new KitchenRecipe(
                grill,
                this.ofList(Items.PORKCHOP, Items.HONEY_BOTTLE),
                this.ofItem(RDFoodItems.HONEY_BBQ_PORK),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("phoenix"), new KitchenRecipe(
                grill,
                this.ofList(RDIngredientItems.FLOUR, Items.HONEY_BOTTLE, Items.POTATO, RDIngredientItems.ONION, RDIngredientItems.WHITE_RADISH),
                this.ofItem(RDFoodItems.PHOENIX),
                12.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("horai_dama_no_rda"), new KitchenRecipe(
                grill,
                this.ofList(Items.BAMBOO, Items.PORKCHOP, RDIngredientItems.SALMON, RDIngredientItems.WAGYU_BEEF, RDIngredientItems.VENISON),
                this.ofItem(RDFoodItems.HORAI_DAMA_NO_EDA),
                13.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("all_meat_feast"), new KitchenRecipe(
                grill,
                this.ofList(RDIngredientItems.WILD_BOAR_MEAT, RDIngredientItems.VENISON, RDIngredientItems.BLACK_PORK, RDIngredientItems.WAGYU_BEEF),
                this.ofItem(RDFoodItems.ALL_MEAT_FEAST),
                14.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("one_hit_kill"), new KitchenRecipe(
                grill,
                this.ofList(RDIngredientItems.WILD_BOAR_MEAT, RDIngredientItems.VENISON, RDIngredientItems.ONION),
                this.ofItem(RDFoodItems.ONE_HIT_KILL),
                9.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("baked_sweet_potatoes"), new KitchenRecipe(
                grill,
                this.ofList(RDIngredientItems.SWEET_POTATO),
                this.ofItem(RDFoodItems.BAKED_SWEET_POTATOES),
                6.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("biscay_biscuits"), new KitchenRecipe(
                grill,
                this.ofList(RDIngredientItems.FLOUR, RDIngredientItems.CHEESE),
                this.ofItem(RDFoodItems.BISCAY_BISCUITS),
                5.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("pirate_bacon"), new KitchenRecipe(
                grill,
                this.ofList(Items.BEEF, RDIngredientItems.BLACK_SALT, RDIngredientItems.CHILI, Items.HONEY_BOTTLE),
                this.ofItem(RDFoodItems.PIRATE_BACON),
                9.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("fantasy_is_all_the_rage"), new KitchenRecipe(
                grill,
                this.ofList(RDIngredientItems.ONION, RDIngredientItems.WILD_BOAR_MEAT, Items.BEEF, RDIngredientItems.TRUFFLE, RDIngredientItems.TOMATO),
                this.ofItem(RDFoodItems.FANTASY_IS_ALL_THE_RAGE),
                18.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("cat_kululi"), new KitchenRecipe(
                grill,
                this.ofList(Items.COCOA_BEANS, RDIngredientItems.FLOUR, Items.EGG),
                this.ofItem(RDFoodItems.CAT_KULULI),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("cat_pizza"), new KitchenRecipe(
                grill,
                this.ofList(Items.BROWN_MUSHROOM, RDIngredientItems.ONION, RDIngredientItems.BROCCOLI, RDIngredientItems.WILD_BOAR_MEAT),
                this.ofItem(RDFoodItems.CAT_PIZZA),
                10.0
        ));
        // 料理台
        this.kitchenRecipeFactory.register(ReverieDreams.id("rice_ball"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.KELP),
                this.ofItem(RDFoodItems.RICE_BALL),
                5.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("grilled_pork_rice_balls"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.PORKCHOP),
                this.ofItem(RDFoodItems.GRILLED_PORK_RICE_BALLS),
                6.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("warm_rice_ball"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.ONION, RDIngredientItems.TROUT),
                this.ofItem(RDFoodItems.WARM_RICE_BALL),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("failing_sakura_snow"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.SUPREME_TUNA),
                this.ofItem(RDFoodItems.FAILING_SAKURA_SNOW),
                12.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("cold_tofu"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.WHITE_RADISH, RDIngredientItems.TOFU),
                this.ofItem(RDFoodItems.COLD_TOFU),
                5.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("vegetable_special"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.POTATO, RDIngredientItems.ONION, Items.PUMPKIN),
                this.ofItem(RDFoodItems.VEGETABLE_SPECIAL),
                5.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("sashimi_platter"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.SALMON, RDIngredientItems.TUNA),
                this.ofItem(RDFoodItems.SASHIMI_PLATTER),
                5.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("secret_dried_fish"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.TROUT),
                this.ofItem(RDFoodItems.SECRET_DRIED_FISH),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("cold_dish_carving"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.WHITE_RADISH),
                this.ofItem(RDFoodItems.COLD_DISH_CARVING),
                5.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("arctic_sweet_shrimp_and_peach_salad"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.PEACH, RDBlocks.MAGIC_ICE_BLOCK.asItem(), RDIngredientItems.SHRIMP),
                this.ofItem(RDFoodItems.ARCTIC_SWEET_SHRIMP_AND_PEACH_SALAD),
                10.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("moonlight_dumplings"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.MOONFLOWER, RDIngredientItems.STICKY_RICE),
                this.ofItem(RDFoodItems.MOONLIGHT_DUMPLINGS),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("mochi"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.STICKY_RICE),
                this.ofItem(RDFoodItems.MOCHI),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("white_peach_eight_bridge"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.STICKY_RICE, RDIngredientItems.PEACH),
                this.ofItem(RDFoodItems.WHITE_PEACH_EIGHT_BRIDGE),
                5.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("moon_lover"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.BUTTER, RDIngredientItems.FLOUR, Items.EGG, RDIngredientItems.MOONFLOWER),
                this.ofItem(RDFoodItems.MOON_LOVERS),
                10.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("flowing_water_noodles"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.FLOUR, Items.BAMBOO),
                this.ofItem(RDFoodItems.FLOWING_WATER_NOODLES),
                10.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("maoyu_tricolor_ice_cream"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.DEW, RDIngredientItems.TOFU, Items.HONEY_BOTTLE, Items.EGG),
                this.ofItem(RDFoodItems.MAOYU_TRICOLOR_ICE_CREAM),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("maoyu_lava_tofu"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.TOFU, RDIngredientItems.CHILI, Items.BEEF, RDIngredientItems.ONION),
                this.ofItem(RDFoodItems.MAOYU_LAVA_TOFU),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("scarlet_devils_cake"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.DEW, Items.PUMPKIN, Items.POTATO, Items.HONEY_BOTTLE),
                this.ofItem(RDFoodItems.SCARLET_DEVILS_CAKE),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("unconscious_monster_mousse"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.TOFU, RDIngredientItems.BUTTER, Items.HONEY_BOTTLE, RDIngredientItems.ONION),
                this.ofItem(RDFoodItems.UNCONSCIOUS_MONSTER_MOUSSE),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("pickled_cucumbers"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.CUCUMBER, RDIngredientItems.BLACK_SALT),
                this.ofItem(RDFoodItems.PICKLED_CUCUMBERS),
                6.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("sea_urchin_sashimi"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.SEA_URCHIN, RDIngredientItems.DEW),
                this.ofItem(RDFoodItems.SEA_URCHIN_SASHIMI),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("nigiri_sushi"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.SALMON, RDIngredientItems.TUNA),
                this.ofItem(RDFoodItems.NIGIRI_SUSHI),
                6.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("gloomy_fruit_pie"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.LEMON, RDIngredientItems.GRAPE, RDIngredientItems.CHEESE),
                this.ofItem(RDFoodItems.GLOOMY_FRUIT_PIE),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("crisp_cyclone"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.FLOUR, Items.HONEY_BOTTLE, RDIngredientItems.CICADA_SHELL),
                this.ofItem(RDFoodItems.CRISP_CYCLONE),
                5.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("oedo_boat_festival"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.SALMON, RDIngredientItems.TUNA, RDIngredientItems.SUPREME_TUNA, RDIngredientItems.TROUT, RDBlocks.MAGIC_ICE_BLOCK.asItem()),
                this.ofItem(RDFoodItems.OEDO_BOAT_FESTIVAL),
                24.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("cat_food"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.TROUT, RDIngredientItems.DEW, RDIngredientItems.FLOUR),
                this.ofItem(RDFoodItems.CAT_FOOD),
                5.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("skinny_horse_dumpling"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.STICKY_RICE, RDIngredientItems.STICKY_RICE),
                this.ofItem(RDFoodItems.SKINNY_HORSE_DUMPLING),
                9.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("gensokyo_star_lotus_ship"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.PUMPKIN, RDIngredientItems.LOTUS_NUTS, RDIngredientItems.TUNA, RDIngredientItems.TWIN_LOTUS, RDIngredientItems.MOONFLOWER),
                this.ofItem(RDFoodItems.GENSOKYO_STAR_LOTUS_SHIP),
                13.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("candied_chestnuts"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.HONEY_BOTTLE, RDIngredientItems.CHESTNUT),
                this.ofItem(RDFoodItems.CANDIED_CHESTNUTS),
                6.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("reversing_the_world"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.BAMBOO, RDIngredientItems.FLOWERS, RDIngredientItems.PLUM, RDIngredientItems.BLACK_PORK, RDIngredientItems.TRUFFLE),
                this.ofItem(RDFoodItems.REVERSING_THE_WORLD),
                12.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("red_bean_daifuku"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.RED_BEANS, RDIngredientItems.STICKY_RICE),
                this.ofItem(RDFoodItems.RED_BEAN_DAIFUKU),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("bamboo_tube_roasted_drunken_shrimp"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.BAMBOO, RDIngredientItems.SHRIMP, RDIngredientItems.BROCCOLI),
                this.ofItem(RDFoodItems.BAMBOO_TUBE_ROASTED_DRUNKEN_SHRIMP),
                5.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("cats_playing_in_water"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.PEACH, RDIngredientItems.FICUS_MICROCARPA, RDIngredientItems.CREAM, RDIngredientItems.FLOUR, Items.COCOA_BEANS),
                this.ofItem(RDFoodItems.CATS_PLAYING_IN_WATER),
                12.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("moonlight_over_lotus_pond"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.GRAPE, RDIngredientItems.FICUS_MICROCARPA, RDIngredientItems.CREAM, RDIngredientItems.TREMELLA),
                this.ofItem(RDFoodItems.MOONLIGHT_OVER_LOTUS_POND),
                12.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("longyin_peach"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.COCOA_BEANS, RDIngredientItems.PEACH, RDIngredientItems.PEACH, RDIngredientItems.PEACH, RDIngredientItems.PEACH),
                this.ofItem(RDFoodItems.LONGYIN_PEACH),
                18.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("molecular_egg"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.COCOA_BEANS, Items.PUMPKIN, RDIngredientItems.CREAM),
                this.ofItem(RDFoodItems.MOLECULAR_EGG),
                18.0
        ));
        // 蒸锅
        this.kitchenRecipeFactory.register(ReverieDreams.id("dew_boiled_eggs"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.DEW, Items.EGG),
                this.ofItem(RDFoodItems.DEW_BOILED_EGGS),
                3.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("udumbara_cake"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.UDUMBARA, RDIngredientItems.DEW),
                this.ofItem(RDFoodItems.UDUMBARA_CAKE),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("bear_paw"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.BLACK_PORK, RDIngredientItems.BAMBOO_SHOOTS, Items.PUFFERFISH),
                this.ofItem(RDFoodItems.BEAR_PAW),
                12.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("poetry_and_ginkgo"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.GINKGO, Items.HONEY_BOTTLE),
                this.ofItem(RDFoodItems.POETRY_AND_GINKGO),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("risotto"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.ONION, Items.BROWN_MUSHROOM, RDIngredientItems.BAMBOO_SHOOTS, RDIngredientItems.BUTTER),
                this.ofItem(RDFoodItems.RISOTTO),
                6.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("scones"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.BUTTER, RDIngredientItems.FLOUR),
                this.ofItem(RDFoodItems.SCONES),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("cream_stew"), new KitchenRecipe(
                streamer,
                this.ofList(Items.BROWN_MUSHROOM, RDIngredientItems.ONION, RDIngredientItems.BUTTER),
                this.ofItem(RDFoodItems.CREAM_STEW),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("taketorihime"), new KitchenRecipe(
                streamer,
                this.ofList(Items.BAMBOO, RDIngredientItems.BAMBOO_SHOOTS, RDIngredientItems.TRUFFLE, RDIngredientItems.GINKGO, RDIngredientItems.BLACK_PORK),
                this.ofItem(RDFoodItems.TAKETORIHIME),
                12.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("pig_deer_butterfly"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.WILD_BOAR_MEAT, RDIngredientItems.VENISON, RDIngredientItems.MOONFLOWER),
                this.ofItem(RDFoodItems.PIG_DEER_BUTTERFLY),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("bamboo_steamed_egg"), new KitchenRecipe(
                streamer,
                this.ofList(Items.BAMBOO, Items.EGG, Items.KELP, Items.BROWN_MUSHROOM),
                this.ofItem(RDFoodItems.BAMBOO_STEAMED_EGG),
                6.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("moon_cake"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.MOONFLOWER, RDIngredientItems.FLOUR),
                this.ofItem(RDFoodItems.MOON_CAKE),
                10.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("ordinary_small_cake"), new KitchenRecipe(
                streamer,
                this.ofList(Items.EGG, RDIngredientItems.GRAPE, RDIngredientItems.CREAM),
                this.ofItem(RDFoodItems.ORDINARY_SMALL_CAKE),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("seven_colored_yokan"), new KitchenRecipe(
                streamer,
                this.ofList(Items.KELP, RDIngredientItems.GRAPE, RDIngredientItems.DEW, RDIngredientItems.UDUMBARA),
                this.ofItem(RDFoodItems.SEVEN_COLORED_YOKAN),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("pumpkin_shrimp_cake"), new KitchenRecipe(
                streamer,
                this.ofList(Items.PUMPKIN, RDIngredientItems.SHRIMP, RDIngredientItems.TOFU),
                this.ofItem(RDFoodItems.PUMPKIN_SHRIMP_CAKE),
                9.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("depressed_cheese_sticks"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.CHEESE, RDIngredientItems.GINKGO, RDIngredientItems.GINKGO),
                this.ofItem(RDFoodItems.DEPRESSED_CHEESE_STICKS),
                6.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("looking_up_at_the_ceiling_fruit_pie"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.TROUT, RDIngredientItems.FLOUR, RDIngredientItems.PEACH),
                this.ofItem(RDFoodItems.LOOKING_UP_AT_THE_CEILING_FRUIT_PIE),
                9.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("beetle_steamed_cake"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.FLOUR, RDIngredientItems.BLACK_PORK, Items.HONEY_BOTTLE, RDIngredientItems.CICADA_SHELL),
                this.ofItem(RDFoodItems.BEETLE_STEAMED_CAKE),
                12.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("sakura_pudding"), new KitchenRecipe(
                streamer,
                this.ofList(Items.HONEY_BOTTLE, RDIngredientItems.PEACH),
                this.ofItem(RDFoodItems.SAKURA_PUDDING),
                6.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("refreshing_pudding"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.GRAPE, RDIngredientItems.GRAPE, RDIngredientItems.LEMON),
                this.ofItem(RDFoodItems.REFRESHING_PUDDING),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("burnt_pudding"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.GRAPE, Items.HONEY_BOTTLE, RDIngredientItems.LEMON, RDIngredientItems.LEMON),
                this.ofItem(RDFoodItems.BURNT_PUDDING),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("fish_leaps_over_dragon_gate"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.SUPREME_TUNA, RDIngredientItems.VENISON, Items.HONEY_BOTTLE, RDIngredientItems.MOONFLOWER, RDIngredientItems.TRUFFLE),
                this.ofItem(RDFoodItems.FISH_LEAPS_OVER_DRAGON_GATE),
                12.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("fright_adventure"), new KitchenRecipe(
                streamer,
                this.ofList(Items.BROWN_MUSHROOM, RDIngredientItems.UDUMBARA, Items.HONEY_BOTTLE, RDIngredientItems.CREAM),
                this.ofItem(RDFoodItems.FRIGHT_ADVENTURE),
                12.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("holy_white_lotus_seed_cake"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.GINKGO, RDIngredientItems.LOTUS_NUTS, RDIngredientItems.FLOUR, RDIngredientItems.BUTTER),
                this.ofItem(RDFoodItems.HOLY_WHITE_LOTUS_SEED_CAKE),
                10.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("pine_nut_cake"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.STICKY_RICE, RDIngredientItems.PINE_NUT),
                this.ofItem(RDFoodItems.PINE_NUT_CAKE),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("shiraga_sadamatsu"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.VENISON, RDIngredientItems.GINKGO, RDIngredientItems.PINE_NUT),
                this.ofItem(RDFoodItems.SHIRAGA_SADAMATSU),
                12.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("lotus_fish_rice_bowl"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.SUPREME_TUNA, RDIngredientItems.TWIN_LOTUS, RDIngredientItems.LOTUS_NUTS, RDIngredientItems.DEW),
                this.ofItem(RDFoodItems.LOTUS_FISH_RICE_BOWL),
                11.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("bamboo_tube_steamed_pork"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.BAMBOO_SHOOTS, RDIngredientItems.DEW, RDIngredientItems.BLACK_PORK),
                this.ofItem(RDFoodItems.BAMBOO_TUBE_STEAMED_PORK),
                9.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("green_bamboo_welcomes_spring"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.CUCUMBER, Items.EGG, RDIngredientItems.WHITE_RADISH, RDIngredientItems.VENISON, RDIngredientItems.MOONFLOWER),
                this.ofItem(RDFoodItems.GREEN_BAMBOO_WELCOMES_SPRING),
                14.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("steamed_egg_with_sea_urchin"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.SEA_URCHIN, Items.EGG),
                this.ofItem(RDFoodItems.STEAMED_EGG_WITH_SEA_URCHIN),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("flowers_birds_wind_and_moon"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.FLOWERS, RDIngredientItems.MOONFLOWER, RDIngredientItems.CREAM),
                this.ofItem(RDFoodItems.FLOWERS_BIRDS_WIND_AND_MOON),
                9.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("the_dream"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.FLOWERS, RDIngredientItems.UDUMBARA, RDIngredientItems.MOONFLOWER, RDIngredientItems.DEW, RDIngredientItems.CREAM),
                this.ofItem(RDFoodItems.THE_DREAM),
                12.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("a_little_sweet_poison"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.UDUMBARA, RDIngredientItems.CREAM, RDIngredientItems.GRAPE, RDIngredientItems.GINKGO),
                this.ofItem(RDFoodItems.A_LITTLE_SWEET_POISON),
                10.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("rapunzel"), new KitchenRecipe(
                streamer,
                this.ofList(Items.PUMPKIN, RDIngredientItems.SHRIMP),
                this.ofItem(RDFoodItems.RAPUNZEL),
                5.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("mad_hatter_tea_party"), new KitchenRecipe(
                streamer,
                this.ofList(Items.COCOA_BEANS, RDIngredientItems.CREAM, RDIngredientItems.FLOUR, Items.BROWN_MUSHROOM_BLOCK, RDIngredientItems.BROCCOLI),
                this.ofItem(RDFoodItems.MAD_HATTER_TEA_PARTY),
                15.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("peach_blossom_glaze_roll"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.PEACH, RDIngredientItems.RED_BEANS, RDIngredientItems.FICUS_MICROCARPA),
                this.ofItem(RDFoodItems.PEACH_BLOSSOM_GLAZE_ROLL),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("the_source_of_life"), new KitchenRecipe(
                streamer,
                this.ofList(Items.COCOA_BEANS, RDIngredientItems.TREMELLA, Items.PUMPKIN, RDIngredientItems.DEW),
                this.ofItem(RDFoodItems.THE_SOURCE_OF_LIFE),
                13.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("the_mars"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.FICUS_MICROCARPA, RDIngredientItems.GRAPE, RDIngredientItems.CRAB, RDIngredientItems.DEW),
                this.ofItem(RDFoodItems.THE_MARS),
                24.0
        ));
        // 炒锅
        this.kitchenRecipeFactory.register(ReverieDreams.id("fried_pork_shreds"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.PORKCHOP),
                this.ofItem(RDFoodItems.FRIED_PORK_SHREDS),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("braised_eel"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.ONION, RDIngredientItems.HAGFISH),
                this.ofItem(RDFoodItems.BRAISED_EEL),
                6.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("potato_croquettes"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.HAGFISH),
                this.ofItem(RDFoodItems.FRIED_HAGFISH),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("deep_fried_cicada_shells"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.TOFU, RDIngredientItems.CICADA_SHELL),
                this.ofItem(RDFoodItems.DEEP_FRIED_CICADA_SHELLS),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("fried_pork_cutlet"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.PORKCHOP),
                this.ofItem(RDFoodItems.FRIED_PORK_CUTLET),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("butter_steak"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.WAGYU_BEEF),
                this.ofItem(RDFoodItems.BUTTER_STEAK),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("beef_wellington"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.WAGYU_BEEF, RDIngredientItems.FLOUR, Items.EGG, RDIngredientItems.BUTTER, RDIngredientItems.TRUFFLE),
                this.ofItem(RDFoodItems.BEEF_WELLINGTON),
                14.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("eggs_benedict"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.EGG, RDIngredientItems.BAMBOO_SHOOTS, RDIngredientItems.BUTTER, RDIngredientItems.FLOUR),
                this.ofItem(RDFoodItems.EGGS_BENEDICT),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("hot_waffles"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.HONEY_BOTTLE, RDIngredientItems.FLOUR, Items.EGG),
                this.ofItem(RDFoodItems.HOT_WAFFLES),
                9.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("pan_fried_salmon"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.SALMON, RDIngredientItems.BAMBOO_SHOOTS),
                this.ofItem(RDFoodItems.PAN_FRIED_SALMON),
                10.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("bamboo_shoots_fried_meat"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.BAMBOO_SHOOTS, Items.PORKCHOP),
                this.ofItem(RDFoodItems.BAMBOO_SHOOTS_FRIED_MEAT),
                10.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("stinky_tofu"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.TOFU, RDIngredientItems.CHILI),
                this.ofItem(RDFoodItems.STINKY_TOFU),
                5.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("colorful_jade_fried_buns"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.BROWN_MUSHROOM, RDIngredientItems.BLACK_PORK),
                this.ofItem(RDFoodItems.COLORFUL_JADE_FRIED_BUNS),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("mapo_tofu"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.TOFU, Items.PORKCHOP, RDIngredientItems.CHILI),
                this.ofItem(RDFoodItems.MAPO_TOFU),
                6.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("fried_shrimp_tempura"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.SHRIMP, RDIngredientItems.FLOUR),
                this.ofItem(RDFoodItems.FRIED_SHRIMP_TEMPURA),
                6.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("golden_crispy_fish_cake"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.TROUT, RDIngredientItems.FLOUR, Items.HONEY_BOTTLE),
                this.ofItem(RDFoodItems.GOLDEN_CRISPY_FISH_CAKE),
                9.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("baked_crab_with_cream"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.CREAM, RDIngredientItems.CRAB),
                this.ofItem(RDFoodItems.BAKED_CRAB_WITH_CREAM),
                12.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("okonomiyaki"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.FLOUR, Items.EGG, RDIngredientItems.WHITE_RADISH),
                this.ofItem(RDFoodItems.OKONOMIYAKI),
                6.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("takoyaki"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.FLOUR, Items.KELP, RDIngredientItems.OCTOPUS),
                this.ofItem(RDFoodItems.TAKOYAKI),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("mushroom_meat_slices"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.BROWN_MUSHROOM, Items.PORKCHOP),
                this.ofItem(RDFoodItems.MUSHROOM_MEAT_SLICES),
                6.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("giant_tamagoyaki"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.FLOUR, RDIngredientItems.FLOUR, Items.EGG, Items.EGG),
                this.ofItem(RDFoodItems.GIANT_TAMAGOYAKI),
                12.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("salmon_tempura"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.SALMON, RDIngredientItems.BUTTER, Items.EGG, RDIngredientItems.FLOUR),
                this.ofItem(RDFoodItems.SALMON_TEMPURA),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("cheese_egg"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.EGG, RDIngredientItems.CHEESE),
                this.ofItem(RDFoodItems.CHEESE_EGG),
                6.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("hell_thrill_warning"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.CHILI, RDIngredientItems.CHILI, RDIngredientItems.CHILI, RDIngredientItems.CHEESE, Items.BEEF),
                this.ofItem(RDFoodItems.HELL_THRILL_WARNING),
                12.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("yunshan_cotton_candy"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.HONEY_BOTTLE, RDIngredientItems.PEACH),
                this.ofItem(RDFoodItems.YUNSHAN_COTTON_CANDY),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("candied_sweet_potato"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.SWEET_POTATO, Items.HONEY_BOTTLE),
                this.ofItem(RDFoodItems.CANDIED_SWEET_POTATO),
                6.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("pan_fried_mushroom_meat_roll"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.PORKCHOP, Items.BROWN_MUSHROOM, RDIngredientItems.TRUFFLE),
                this.ofItem(RDFoodItems.PAN_FRIED_MUSHROOM_MEAT_ROLL),
                9.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("assorted_tempura"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.BLACK_PORK, RDIngredientItems.TRUFFLE, RDIngredientItems.HAGFISH, RDIngredientItems.MOONFLOWER),
                this.ofItem(RDFoodItems.ASSORTED_TEMPURA),
                7.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("fried_tomato_strips"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.TOMATO, Items.POTATO),
                this.ofItem(RDFoodItems.FRIED_TOMATO_STRIPS),
                6.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("braised_pork_with_peach"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.HONEY_BOTTLE, RDIngredientItems.PEACH, Items.PORKCHOP),
                this.ofItem(RDFoodItems.BRAISED_PORK_WITH_PEACH),
                8.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("dorayaki"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.RED_BEANS, Items.EGG, RDIngredientItems.FLOUR),
                this.ofItem(RDFoodItems.DORAYAKI),
                6.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("toon_pancakes"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.TWIN_LOTUS, Items.EGG),
                this.ofItem(RDFoodItems.TOON_PANCAKES),
                6.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("eel_egg_donburi"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.HAGFISH, Items.EGG),
                this.ofItem(RDFoodItems.EEL_EGG_DONBURI),
                5.0
        ));
        this.kitchenRecipeFactory.register(ReverieDreams.id("hula_soup"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.CHILI, Items.BEEF, RDIngredientItems.TOFU),
                this.ofItem(RDFoodItems.HULA_SOUP),
                8.0
        ));
    }

    public void generateDanmakuRecipe() {
        Stream<DanmakuType> stream = RegistryHandlers.DANMAKU_TYPE.stream();
        stream.forEach(value -> {
            Identifier key = RegistryHandlers.DANMAKU_TYPE.getKey(value);
            if (!value.isDeleteFromList()) {
                for (Tuple<Item, ItemStack> pair : value.getColorPairs()) {
                    Item dye = pair.getA();
                    ItemStack result = pair.getB();
                    Item item = result.getItem();
                    Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
                    Identifier dyeId = BuiltInRegistries.ITEM.getKey(dye);
                    Identifier registryKey = Identifier.fromNamespaceAndPath(itemId.getNamespace(), itemId.getPath() + "_dye_" + dyeId.getPath());
                    DanmakuRecipe recipe = new DanmakuRecipe(
                            new ItemStackWrapper(new ItemStack(dye, 4)),
                            new ItemStackWrapper(new ItemStack(RDItems.DANMAKU_CORE, 4)),
                            new ItemStackWrapper(new ItemStack(RDItems.POWER, 35)),
                            new ItemStackWrapper(new ItemStack(RDItems.POINT, 35)),
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
