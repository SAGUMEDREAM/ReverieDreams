package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.fabric.datagen.generator.AbstractRecipeTypeProvider;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.item.base.RoleCard;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.GensokyoAltarRecipe;
import cc.thonly.reverie_dreams.registry.content.RoleCards;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.effect.RDPotions;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GensokyoAltarRecipeProvider extends AbstractRecipeTypeProvider {
    public final Factory<GensokyoAltarRecipe> factory = this.getOrCreateFactory(RecipeManager.GENSOKYO_ALTAR, GensokyoAltarRecipe.class);

    public GensokyoAltarRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    @Override
    public void configured(HolderLookup.Provider provider) {
        this.configuredRecipe();
        this.configuredRoleCardRecipe();
    }

    private void configuredRecipe() {
        this.factory.register(RDItems.YIN_YANG_ORB,
                new GensokyoAltarRecipe(
                        this.ofItem(Items.DIAMOND),
                        List.of(
                                this.ofItem(Items.BLACK_WOOL), this.ofItem(Items.GOLDEN_APPLE), this.ofItem(Items.WHITE_WOOL),
                                this.ofItem(Items.BLACK_WOOL), this.ofItem(Items.WHITE_WOOL),
                                this.ofItem(Items.BLACK_WOOL), this.ofItem(Items.GOLDEN_APPLE), this.ofItem(Items.WHITE_WOOL)
                        ),
                        this.ofItem(RDItems.YIN_YANG_ORB)
                )
        );
        this.factory.register(RDItems.HORAI_DAMA_NO_EDA,
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
        this.factory.register(RDItems.CROSSING_CHISEL, new GensokyoAltarRecipe(
                this.ofItem(Items.GOLDEN_HOE),
                List.of(
                        this.ofItem(Items.ENDER_PEARL, 2), this.ofItem(Items.GOLD_BLOCK), this.ofItem(Items.ENDER_PEARL, 4),
                        this.ofItem(Items.GOLD_INGOT, 4), this.ofItem(Items.GOLD_INGOT, 4),
                        this.ofItem(Items.ENDER_PEARL, 8), this.ofItem(Items.GOLD_BLOCK), this.ofItem(Items.ENDER_PEARL, 16)
                ),
                this.ofItem(RDItems.CROSSING_CHISEL)
        ));
        this.factory.register(RDItems.GAP_BALL, new GensokyoAltarRecipe(
                this.ofItem(Items.COMPASS),
                List.of(
                        this.ofItem(Items.PURPLE_DYE, 2), this.ofItem(Items.ENDER_PEARL, 6), this.ofItem(Items.REDSTONE_BLOCK, 1),
                        this.ofItem(Items.MAGENTA_DYE, 2), this.ofItem(Items.COPPER_INGOT),
                        this.ofItem(Items.COPPER_INGOT, 6), this.ofItem(Items.COPPER_INGOT, 7), this.ofItem(Items.COPPER_INGOT, 8)
                ),
                this.ofItem(RDItems.GAP_BALL)
        ));
        this.factory.register(RDItems.BAGUA_FURNACE, new GensokyoAltarRecipe(
                this.ofItem(Items.COMPASS),
                List.of(
                        this.ofItem(Items.REDSTONE_BLOCK, 8), this.ofItem(Items.IRON_INGOT, 12), this.ofItem(Items.COAL_BLOCK),
                        this.ofItem(Items.NETHERITE_INGOT, 4), this.ofItem(Items.COPPER_INGOT, 16),
                        this.ofItem(Items.COAL_BLOCK), this.ofItem(Items.IRON_INGOT, 12), this.ofItem(Items.REDSTONE_BLOCK, 8)
                ),
                this.ofItem(RDItems.BAGUA_FURNACE)
        ));
        this.factory.register(RDItems.TIME_STOP_CLOCK, new GensokyoAltarRecipe(
                this.ofItem(Items.CLOCK),
                List.of(
                        this.ofItem(Items.PURPLE_DYE, 4), this.ofItem(Items.REDSTONE_BLOCK, 2), this.ofItem(Items.PURPLE_DYE, 4),
                        this.ofItem(Items.GOLD_INGOT, 5), this.ofItem(Items.GOLD_INGOT, 5),
                        this.ofItem(Items.PURPLE_DYE, 4), this.ofItem(Items.REDSTONE_BLOCK, 2), this.ofItem(Items.PURPLE_DYE, 4)
                ),
                this.ofItem(RDItems.TIME_STOP_CLOCK)
        ));
        this.factory.register(RDItems.MAPLE_LEAF_FAN, new GensokyoAltarRecipe(
                this.ofItem(Items.OAK_LEAVES, 48),
                List.of(
                        this.ofItem(Items.WIND_CHARGE, 16), this.ofItem(Items.GOLD_INGOT, 9), this.ofItem(Items.WIND_CHARGE, 16),
                        this.ofItem(Items.GOLD_INGOT, 9), this.ofItem(Items.GOLD_INGOT, 9),
                        this.ofItem(Items.BREEZE_ROD, 2), this.ofItem(Items.GOLD_INGOT, 9), this.ofItem(Items.WIND_CHARGE, 16)
                ),
                this.ofItem(RDItems.MAPLE_LEAF_FAN)
        ));
        this.factory.register(RDItems.EARPHONE, new GensokyoAltarRecipe(
                this.ofItem(Items.IRON_HELMET),
                List.of(
                        this.ofItem(Items.AMETHYST_SHARD, 5), this.ofItem(Items.GOLD_INGOT, 6), this.ofItem(Items.AMETHYST_SHARD, 5),
                        this.ofItem(Items.SCULK_SENSOR, 9), this.ofItem(Items.SCULK_SENSOR, 9),
                        this.ofEmpty(), this.ofItem(Items.REDSTONE, 18), this.ofEmpty()
                ),
                this.ofItem(RDItems.EARPHONE)
        ));
        this.factory.register(RDItems.KOISHI_HAT, new GensokyoAltarRecipe(
                this.ofItem(Items.IRON_HELMET),
                List.of(
                        this.ofItem(Items.BLACK_DYE, 2), this.ofItem(Items.IRON_INGOT, 1), this.ofItem(Items.BLACK_DYE, 2),
                        this.ofItem(Items.IRON_INGOT, 1), this.ofItem(Items.IRON_INGOT, 1),
                        this.ofItem(Items.YELLOW_DYE, 3), this.ofItem(Items.BLACK_DYE, 2), this.ofItem(Items.YELLOW_DYE, 3)
                ),
                this.ofItem(RDItems.KOISHI_HAT)
        ));
        this.factory.register(RDItems.HAKUREI_CANE, new GensokyoAltarRecipe(
                this.ofItem(Items.WOODEN_SWORD),
                List.of(
                        this.ofEmpty(), this.ofItem(Items.PAPER, 6), this.ofItem(Items.PAPER, 6),
                        this.ofItem(Items.IRON_INGOT, 1), this.ofItem(Items.PAPER, 6),
                        this.ofItem(Items.STICK, 1), this.ofEmpty(), this.ofItem(Items.PAPER, 3)
                ),
                this.ofItem(RDItems.HAKUREI_CANE)
        ));
        this.factory.register(RDItems.WIND_BLESSING_CANE, new GensokyoAltarRecipe(
                this.ofItem(Items.WOODEN_SWORD),
                List.of(
                        this.ofEmpty(), this.ofItem(Items.PAPER, 5), this.ofItem(Items.PAPER, 7),
                        this.ofItem(Items.WIND_CHARGE, 12), this.ofItem(Items.PAPER, 5),
                        this.ofItem(Items.STICK, 1), this.ofEmpty(), this.ofItem(Items.PAPER, 2)
                ),
                this.ofItem(RDItems.WIND_BLESSING_CANE)
        ));
        this.factory.register(RDItems.MAGIC_BROOM, new GensokyoAltarRecipe(
                this.ofItem(Items.REDSTONE_BLOCK, 3),
                List.of(
                        this.ofItem(Items.HAY_BLOCK, 2), this.ofItem(Items.LEAD), this.ofItem(Items.SADDLE),
                        this.ofItem(Items.HAY_BLOCK, 2), this.ofItem(Items.STICK, 3),
                        this.ofItem(Items.HAY_BLOCK, 2), this.ofItem(Items.SLIME_BALL), this.ofEmpty()
                ),
                this.ofItem(RDItems.MAGIC_BROOM)
        ));
        this.factory.register(RDItems.KNIFE, new GensokyoAltarRecipe(
                this.ofItem(RDBlocks.SILVER_BLOCK, 2),
                List.of(
                        this.ofItem(RDItems.SILVER_SWORD), this.ofEmpty(), this.ofItem(RDItems.SILVER_INGOT, 3),
                        this.ofItem(RDItems.SILVER_SWORD), this.ofEmpty(),
                        this.ofItem(Items.STICK, 3), this.ofEmpty(), this.ofEmpty()
                ),
                this.ofItem(RDItems.KNIFE)
        ));
        this.factory.register(RDItems.ROKANKEN, new GensokyoAltarRecipe(
                this.ofItem(Items.IRON_SWORD),
                List.of(
                        this.ofItem(Items.SOUL_SAND, 7), this.ofItem(Items.SOUL_SAND, 8), this.ofItem(RDItems.SILVER_INGOT, 12),
                        this.ofItem(Items.SOUL_SAND, 6), this.ofEmpty(),
                        this.ofItem(Items.STRIPPED_CHERRY_LOG, 4), this.ofEmpty(), this.ofEmpty()
                ),
                this.ofItem(RDItems.ROKANKEN)
        ));
        this.factory.register(RDItems.HAKUROKEN, new GensokyoAltarRecipe(
                this.ofItem(Items.IRON_SWORD),
                List.of(
                        this.ofItem(Items.SOUL_SAND, 8), this.ofItem(Items.SOUL_SAND, 9), this.ofItem(RDItems.SILVER_INGOT, 12),
                        this.ofItem(Items.SOUL_SAND, 7), this.ofEmpty(),
                        this.ofItem(Items.STRIPPED_CHERRY_LOG, 4), this.ofEmpty(), this.ofItem(RDItems.SPEED_FEATHER, 4)
                ),
                this.ofItem(RDItems.HAKUROKEN)
        ));
        this.factory.register(RDItems.PAPILIO_PATTERN_FAN, new GensokyoAltarRecipe(
                this.ofItem(Items.IRON_INGOT, 1),
                List.of(
                        this.ofEmpty(), this.ofItem(Items.BLUE_WOOL, 3), this.ofItem(Items.PURPLE_WOOL, 3),
                        this.ofItem(Items.SOUL_SAND, 9), this.ofItem(Items.PINK_WOOL, 3),
                        this.ofItem(Items.STRIPPED_CHERRY_LOG, 5), this.ofItem(Items.CHERRY_SAPLING), this.ofEmpty()
                ),
                this.ofItem(RDItems.PAPILIO_PATTERN_FAN)
        ));
        this.factory.register(RDItems.GUNGNIR, new GensokyoAltarRecipe(
                this.ofItem(Items.DIAMOND_AXE),
                List.of(
                        this.ofItem(Items.DIAMOND, 2), this.ofItem(Items.IRON_INGOT, 5), this.ofItem(Items.REDSTONE_BLOCK, 2),
                        this.ofItem(Items.SOUL_SAND, 9), this.ofItem(Items.IRON_INGOT, 5),
                        this.ofItem(Items.BREEZE_ROD, 5), this.ofItem(Items.SOUL_SAND, 6), this.ofEmpty()
                ),
                this.ofItem(RDItems.GUNGNIR)
        ));
        this.factory.register(RDItems.LEVATIN, new GensokyoAltarRecipe(
                this.ofItem(Items.NETHERITE_HOE),
                List.of(
                        this.ofEmpty(), this.ofItem(Items.GOLD_INGOT, 5), this.ofItem(Items.NETHERITE_INGOT, 1),
                        this.ofItem(Items.REDSTONE, 3), this.ofItem(Items.IRON_INGOT, 5),
                        this.ofItem(Items.BLAZE_ROD, 4), this.ofItem(Items.SOUL_SAND, 4), this.ofItem(Items.COAL, 11)
                ),
                this.ofItem(RDItems.LEVATIN)
        ));
        this.factory.register(RDItems.IBUKIHO, new GensokyoAltarRecipe(
                this.ofItem(Items.HONEY_BOTTLE),
                List.of(
                        this.ofItem(Items.GOLD_INGOT, 8), this.ofItem(Items.GOLD_INGOT, 5), this.ofItem(Items.SUGAR, 1),
                        this.ofItem(Items.REDSTONE, 8), this.ofItem(Items.GHAST_TEAR, 8),
                        this.ofItem(Items.DIAMOND, 4), this.ofItem(Items.BLAZE_ROD, 8), this.ofItem(Items.FERMENTED_SPIDER_EYE, 8)
                ),
                this.ofItem(RDItems.IBUKIHO)
        ));
        this.factory.register(RDItems.SWORD_OF_HISOU, new GensokyoAltarRecipe(
                this.ofItem(Items.COPPER_SWORD),
                List.of(
                        this.ofItem(Items.GOLD_INGOT, 7), this.ofItem(Items.DIAMOND, 5), this.ofItem(Items.COPPER_INGOT, 2),
                        this.ofItem(Items.REDSTONE, 6), this.ofItem(Items.REDSTONE, 6),
                        this.ofItem(Items.BLAZE_ROD, 4), this.ofItem(RDIngredientItems.PEACH, 7), this.ofItem(Items.APPLE, 6)
                ),
                this.ofItem(RDItems.SWORD_OF_HISOU)
        ));
        this.factory.register(RDItems.MANPOZUCHI, new GensokyoAltarRecipe(
                this.ofItem(Items.MACE),
                List.of(
                        this.ofItem(Items.GOLD_INGOT, 2), this.ofItem(Items.END_ROD, 1), this.ofItem(Items.IRON_INGOT, 2),
                        this.ofItem(Items.REDSTONE, 8), this.ofItem(Items.COPPER_INGOT, 7),
                        this.ofItem(Items.IRON_NUGGET, 18), this.ofItem(Items.DIAMOND, 4), this.ofItem(Items.GOLD_NUGGET, 18)
                ),
                this.ofItem(RDItems.MANPOZUCHI)
        ));
        this.factory.register(RDItems.NUE_TRIDENT, new GensokyoAltarRecipe(
                this.ofItem(Items.TRIDENT),
                List.of(
                        this.ofEmpty(), this.ofEmpty(), this.ofItem(Items.COMPASS),
                        this.ofItem(Items.HEART_OF_THE_SEA), this.ofItem(Items.COD, 3),
                        this.ofItem(Items.NETHERITE_INGOT), this.ofItem(Items.OAK_LOG, 8), this.ofEmpty()
                ),
                this.ofItem(RDItems.NUE_TRIDENT)
        ));
        this.factory.register(RDItems.TREASURE_HUNTING_ROD, new GensokyoAltarRecipe(
                this.ofItem(Items.DIAMOND_PICKAXE),
                List.of(
                        this.ofItem(Items.IRON_INGOT, 5), this.ofItem(Items.IRON_INGOT, 6), this.ofItem(Items.RABBIT_FOOT, 9),
                        this.ofItem(Items.NETHERITE_INGOT), this.ofItem(Items.GOLD_INGOT, 10),
                        this.ofItem(Items.NETHERITE_INGOT), this.ofItem(Items.REDSTONE, 8), this.ofItem(Items.STONE_BUTTON, 1)
                ),
                this.ofItem(RDItems.TREASURE_HUNTING_ROD)
        ));
        this.factory.register(RDItems.TRUMPET_GUN, new GensokyoAltarRecipe(
                this.ofItem(Items.CROSSBOW),
                List.of(
                        this.ofItem(RDBlocks.POWER_BLOCK, 5), this.ofItem(Items.FERMENTED_SPIDER_EYE, 2), this.ofItem(Items.RABBIT_FOOT, 5),
                        this.ofItem(RDBlocks.POWER_BLOCK, 4), this.ofItem(Items.GOLD_INGOT, 5),
                        this.ofItem(Items.HONEYCOMB), this.ofItem(Items.IRON_NUGGET, 3), this.ofItem(Items.BLAZE_ROD, 3)
                ),
                this.ofItem(RDItems.TRUMPET_GUN)
        ));
        this.factory.register(RDItems.DEATH_SCYTHE, new GensokyoAltarRecipe(
                this.ofItem(Items.NETHERITE_HOE),
                List.of(
                        this.ofItem(Items.SOUL_SAND, 32), this.ofItem(Items.SOUL_SAND, 16), this.ofItem(Items.SOUL_SAND, 8),
                        this.ofItem(Items.NETHERITE_INGOT, 1), this.ofItem(Items.DIAMOND, 5),
                        this.ofItem(Items.ROTTEN_FLESH, 8), this.ofItem(Items.BONE, 7)
                ),
                this.ofItem(RDItems.DEATH_SCYTHE)
        ));
        ItemStackTemplate kanjuKusuri = RDPotions.createStackTemplate(RDPotions.KANJU_KUSURI_POTION);
        this.factory.register(ReverieDreams.id("kanju_kusuri"), new GensokyoAltarRecipe(
                this.ofItem(Items.GLASS_BOTTLE),
                List.of(
                        this.ofItem(Items.SOUL_SAND, 20), this.ofItem(Items.SAND, 20), this.ofItem(Items.NETHER_WART, 5),
                        this.ofItem(Items.BLAZE_POWDER, 8), this.ofItem(Items.GOLDEN_APPLE, 1),
                        this.ofItem(Items.GOLDEN_CARROT, 2), this.ofItem(RDItems.SILVER_INGOT, 4), this.ofItem(Items.ENDER_PEARL, 1)
                ),
                this.ofItem(kanjuKusuri)
        ));
        this.factory.register(RDItems.CURSED_DECOY_DOLl, new GensokyoAltarRecipe(
                        this.ofItem(Items.ARMOR_STAND),
                        List.of(
                                this.ofItem(Items.SOUL_SAND, 6), this.ofItem(Items.SOUL_SAND, 6), this.ofItem(Items.SOUL_SAND, 6),
                                this.ofItem(Items.SOUL_SAND, 6), this.ofItem(Items.SOUL_SAND, 6),
                                this.ofItem(Items.SOUL_SAND, 6), this.ofItem(Items.SOUL_SAND, 6), this.ofItem(Items.SOUL_SAND, 6)
                        ),
                        this.ofItem(RDItems.CURSED_DECOY_DOLl)
                )
        );
        this.factory.register(RDItems.VAISRAVANAS_PAGODA, new GensokyoAltarRecipe(
                this.ofItem(RDBlocks.POWER_BLOCK, 10),
                List.of(
                        this.ofItem(Items.STONE, 15), this.ofItem(Items.GOLD_INGOT, 20), this.ofItem(Items.STONE, 15),
                        this.ofItem(Items.COPPER_INGOT, 10), this.ofItem(Items.BLAZE_POWDER, 8),
                        this.ofItem(Items.STONE, 15), this.ofItem(Items.IRON_INGOT, 20), this.ofItem(Items.STONE, 15)
                ),
                this.ofItem(RDItems.VAISRAVANAS_PAGODA)
        ));
        this.factory.register(RDItems.TENGU_SHIELD, new GensokyoAltarRecipe(
                this.ofItem(Items.SHIELD, 1),
                List.of(
                        this.ofItem(Items.FEATHER, 3), this.ofItem(Items.FEATHER, 5), this.ofItem(Items.FEATHER, 2),
                        this.ofItem(RDItems.SILVER_INGOT, 7), this.ofItem(RDItems.SILVER_INGOT, 7),
                        this.ofItem(Items.DIAMOND, 3), this.ofItem(Items.IRON_INGOT, 6), this.ofItem(Items.BONE, 3)
                ),
                this.ofItem(RDItems.TENGU_SHIELD)
        ));
        this.factory.register(RDItems.TENGU_CAMERA, new GensokyoAltarRecipe(
                this.ofItem(Items.IRON_BLOCK, 1),
                List.of(
                        this.ofItem(Items.GLOWSTONE_DUST, 8), this.ofItem(Items.STONE_BUTTON, 2), this.ofItem(Items.GLOWSTONE_DUST, 8),
                        this.ofItem(RDItems.SILVER_INGOT, 6), this.ofItem(Items.IRON_INGOT, 6),
                        this.ofItem(Items.REDSTONE, 23), this.ofItem(Items.IRON_INGOT, 6), this.ofItem(Items.REDSTONE, 13)
                ),
                this.ofItem(RDItems.TENGU_CAMERA)
        ));
        this.factory.register(RDItems.BAD_APPLE, new GensokyoAltarRecipe(
                this.ofItem(Items.GOLDEN_APPLE, 1),
                List.of(
                        this.ofItem(Items.BLACK_DYE, 2), this.ofItem(RDItems.POWER, 11), this.ofItem(Items.BLACK_DYE, 2),
                        this.ofItem(Items.REDSTONE, 6), this.ofItem(Items.REDSTONE, 6),
                        this.ofItem(Items.BLACK_DYE, 2), this.ofItem(RDItems.POINT, 8), this.ofItem(Items.BLACK_DYE, 2)
                ),
                this.ofItem(RDItems.BAD_APPLE)
        ));
        this.factory.register(RDItems.EXORCISM_PAPER, new GensokyoAltarRecipe(
                this.ofItem(Items.PAPER, 6),
                List.of(
                        this.ofItem(Items.BONE, 12), this.ofItem(Items.ROTTEN_FLESH, 12), this.ofItem(Items.RED_DYE, 3),
                        this.ofItem(Items.PAPER, 6), this.ofItem(Items.PAPER, 8),
                        this.ofItem(Items.PAPER, 6), this.ofItem(Items.PAPER, 8), this.ofItem(Items.REDSTONE, 7)
                ),
                this.ofItem(RDItems.EXORCISM_PAPER, 4)
        ));
        this.factory.register(RDItems.YUKA_FLOWER_UMBRELLA, new GensokyoAltarRecipe(
                this.ofItem(Items.IRON_SWORD),
                List.of(
                        this.ofItem(Items.SUNFLOWER, 16), this.ofItem(Items.NETHERITE_INGOT), this.ofItem(Items.WHITE_WOOL, 8),
                        this.ofItem(Items.ROSE_BUSH, 16), this.ofItem(RDIngredientItems.UDUMBARA, 4),
                        this.ofItem(Items.FLINT, 1), this.ofItem(Items.BAMBOO, 2), this.ofItem(RDItems.POWER, 36)
                ),
                this.ofItem(RDItems.YUKA_FLOWER_UMBRELLA)
        ));
        this.factory.register(RDItems.SATORI_EYE, new GensokyoAltarRecipe(
                this.ofItem(Items.ENDER_EYE),
                List.of(
                        this.ofItem(Items.SPIDER_EYE, 2), this.ofItem(Items.ENDER_PEARL, 2), this.ofItem(Items.GOLD_INGOT, 3),
                        this.ofItem(Items.IRON_INGOT, 5), this.ofItem(Items.IRON_INGOT, 5),
                        this.ofItem(Items.REDSTONE, 4), this.ofItem(RDItems.POINT, 12), this.ofItem(RDItems.POWER, 12)
                ),
                this.ofItem(RDItems.SATORI_EYE)
        ));
        this.factory.register(RDItems.LOW_GRAVITY_BOOT, new GensokyoAltarRecipe(
                this.ofItem(Items.IRON_BOOTS),
                List.of(
                        this.ofItem(Items.FEATHER, 3), this.ofItem(Items.FEATHER, 3), this.ofItem(Items.FEATHER, 3),
                        this.ofItem(RDItems.POWER, 20), this.ofItem(RDItems.POINT, 20),
                        this.ofItem(Items.FEATHER, 3), this.ofItem(Items.FEATHER, 3), this.ofItem(Items.FEATHER, 3)
                ),
                this.ofItem(RDItems.LOW_GRAVITY_BOOT)
        ));
        this.factory.register(RDItems.CROWN_OF_THE_UNDERWORLD, new GensokyoAltarRecipe(
                this.ofItem(Items.GOLDEN_HELMET),
                List.of(
                        this.ofItem(Items.CHERRY_LEAVES, 11), this.ofItem(Items.CHERRY_LEAVES, 11), this.ofItem(Items.CHERRY_LEAVES, 11),
                        this.ofItem(RDItems.POWER, 20), this.ofItem(RDItems.POWER, 20),
                        this.ofItem(RDItems.POWER, 20), this.ofItem(Items.WITHER_SKELETON_SKULL, 3), this.ofItem(RDItems.POWER, 20)
                ),
                this.ofItem(RDItems.CROWN_OF_THE_UNDERWORLD)
        ));
    }

    private void configuredRoleCardRecipe() {
        this.registerRoleCard(RoleCards.PROTAGONIST_GROUP
                .createRecipeBuilder()
                .itemStack(IngredientStack.of(Items.REDSTONE_BLOCK, 2), IngredientStack.of(Items.OBSIDIAN, 5))
                .build());
        this.registerRoleCard(RoleCards.KOUMAKYOU
                .createRecipeBuilder()
                .itemStack(IngredientStack.of(Items.SOUL_SAND, 12), IngredientStack.of(Items.NETHERRACK, 12))
                .build());
        this.registerRoleCard(RoleCards.YOUYOUMU
                .createRecipeBuilder()
                .itemStack(IngredientStack.of(Items.CHERRY_LEAVES, 26), IngredientStack.of(Items.IRON_SWORD, 1))
                .build());
        this.registerRoleCard(RoleCards.EIYASHOU
                .createRecipeBuilder()
                .itemStack(IngredientStack.of(Items.BAMBOO, 50), IngredientStack.of(Items.END_STONE, 24))
                .build());
        this.registerRoleCard(RoleCards.KAEIZUKA
                .createRecipeBuilder()
                .itemStack(IngredientStack.of(Items.DANDELION, 30), IngredientStack.of(Items.ALLIUM, 30))
                .build());
        this.registerRoleCard(RoleCards.FUUJINROKU
                .createRecipeBuilder()
                .itemStack(IngredientStack.of(Items.LEAF_LITTER, 40), IngredientStack.of(Items.STONE, 40))
                .build());
        this.registerRoleCard(RoleCards.CHIREIDEN
                .createRecipeBuilder()
                .itemStack(IngredientStack.of(Items.ROSE_BUSH, 40), IngredientStack.of(Items.NETHERRACK, 45))
                .build());
        this.registerRoleCard(RoleCards.SEIRENSEN
                .createRecipeBuilder()
                .itemStack(IngredientStack.of(Items.GOLD_INGOT, 28), IngredientStack.of(Items.BIRCH_BOAT, 1))
                .build());
        this.registerRoleCard(RoleCards.SHINREIBYOU
                .createRecipeBuilder()
                .itemStack(IngredientStack.of(Items.SOUL_SAND, 38), IngredientStack.of(Items.ROTTEN_FLESH, 18))
                .build());
        this.registerRoleCard(RoleCards.KISHINJOU
                .createRecipeBuilder()
                .itemStack(IngredientStack.of(Items.BLAZE_ROD, 26), IngredientStack.of(Items.NETHER_BRICKS, 30))
                .build());
        this.registerRoleCard(RoleCards.KANJUDEN
                .createRecipeBuilder()
                .itemStack(IngredientStack.of(Items.END_STONE, 32), IngredientStack.of(Items.NETHERRACK, 32))
                .build());
        this.registerRoleCard(RoleCards.TENKUUSHOU
                .createRecipeBuilder()
                .itemStack(IngredientStack.of(Items.GRASS_BLOCK, 29), IngredientStack.of(Items.LEAF_LITTER, 43))
                .build());
        this.registerRoleCard(RoleCards.KIKEIJUU
                .createRecipeBuilder()
                .itemStack(IngredientStack.of(Items.DIRT, 44), IngredientStack.of(Items.BLAZE_POWDER, 30))
                .build());
        this.registerRoleCard(RoleCards.KOURYUUDOU
                .createRecipeBuilder()
                .itemStack(IngredientStack.of(Items.GOLD_INGOT, 31), IngredientStack.of(Items.DIAMOND, 23))
                .build());
        this.registerRoleCard(RoleCards.JUUOUEN
                .createRecipeBuilder()
                .itemStack(IngredientStack.of(Items.LEATHER, 26), IngredientStack.of(Items.PORKCHOP, 20))
                .build());
        this.registerRoleCard(RoleCards.KINJOUKYOU
                .createRecipeBuilder()
                .itemStack(IngredientStack.of(Items.STONE, 31), IngredientStack.of(Items.GOLD_INGOT, 38))
                .build());
        this.registerRoleCard(RoleCards.SANGETSUSEI
                .createRecipeBuilder()
                .itemStack(IngredientStack.of(Items.END_STONE, 28), IngredientStack.of(Items.GLOWSTONE, 26))
                .build());
        this.registerRoleCard(RoleCards.HIFUU
                .createRecipeBuilder()
                .itemStack(IngredientStack.of(Items.BOOK, 19), IngredientStack.of(Items.ENDER_EYE, 20))
                .build());
        this.registerRoleCard(RoleCards.TASOGARE_FURONTIA
                .createRecipeBuilder()
                .itemStack(IngredientStack.of(Items.GLASS_BOTTLE, 20), IngredientStack.of(RDIngredientItems.PEACH, 15))
                .build());
    }

    public void registerRoleCard(RoleCard.RecipeBuilder builder) {
        this.factory.register(builder.getRoleCard().getId(), builder.getResult());
    }

    public void registerRoleCard(Identifier identifier, RoleCard.RecipeBuilder builder) {
        this.factory.register(builder.getRoleCard().getId(), builder.getResult());
    }

    @Override
    public String getName() {
        return "Gensokyo Altar Recipe Provider";
    }
}
