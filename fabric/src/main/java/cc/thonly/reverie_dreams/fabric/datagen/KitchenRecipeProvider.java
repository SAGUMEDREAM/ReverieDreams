package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.fabric.datagen.generator.AbstractRecipeTypeProvider;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.recipe.type.KitchenRecipeType;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDCuisineItems;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class KitchenRecipeProvider extends AbstractRecipeTypeProvider {
    private static final Identifier cookingPot = KitchenRecipeType.TypeInstance.COOKING_POT.toId();
    private static final Identifier grill = KitchenRecipeType.TypeInstance.GRILL.toId();
    private static final Identifier cuttingBoard = KitchenRecipeType.TypeInstance.CUTTING_BOARD.toId();
    private static final Identifier streamer = KitchenRecipeType.TypeInstance.STEAMER.toId();
    private static final Identifier fryingPan = KitchenRecipeType.TypeInstance.FRYING_PAN.toId();
    private final Factory<KitchenRecipe> factory = this.getOrCreateFactory(RecipeManager.KITCHEN_TYPE, KitchenRecipe.class);

    public KitchenRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    @Override
    public void configured(HolderLookup.Provider provider) {
        this.configuredCookingPot();
        this.configuredGrill();
        this.configuredCuttingBoard();
        this.configuredFryingPan();
        this.configuredSteamer();
    }

    // 煮锅
    private void configuredCookingPot() {
        this.factory.register(ReverieDreams.id("seafood_miso_soup"), new KitchenRecipe(
                cookingPot,
                List.of(
                        this.ofItem(Items.KELP)
                ),
                this.ofItem(RDCuisineItems.SEAFOOD_MISO_SOUP),
                6.0
        ));
        this.factory.register(ReverieDreams.id("tofu_miso"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.TOFU),
                this.ofItem(RDCuisineItems.TOFU_MISO),
                7.0
        ));
        this.factory.register(ReverieDreams.id("strength_soup"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.WILD_BOAR_MEAT, Items.KELP),
                this.ofItem(RDCuisineItems.STRENGTH_SOUP),
                7.0
        ));
        this.factory.register(ReverieDreams.id("game_soup"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.POTATO, Items.PUMPKIN, RDIngredientItems.BLACK_PORK),
                this.ofItem(RDCuisineItems.GAME_SOUP),
                7.0
        ));
        this.factory.register(ReverieDreams.id("pork_rice"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.PORKCHOP),
                this.ofItem(RDCuisineItems.PORK_RICE),
                7.0
        ));
        this.factory.register(ReverieDreams.id("beef_rice"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.BEEF),
                this.ofItem(RDCuisineItems.BEEF_RICE),
                7.0
        ));
        this.factory.register(ReverieDreams.id("snow_white"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.PUFFERFISH, RDIngredientItems.HAGFISH, Items.KELP),
                this.ofItem(RDCuisineItems.SNOW_WHITE),
                12.0
        ));
        this.factory.register(ReverieDreams.id("tofu_pot"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.TOFU),
                this.ofItem(RDCuisineItems.TOFU_POT),
                5.0
        ));
        this.factory.register(ReverieDreams.id("zhaji"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.KELP, RDIngredientItems.TOFU, RDIngredientItems.TROUT),
                this.ofItem(RDCuisineItems.ZHAJI),
                5.0
        ));
        this.factory.register(ReverieDreams.id("grand_banquet"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.BLACK_PORK, RDIngredientItems.WAGYU_BEEF, RDIngredientItems.PUFF_YO_FRUIT),
                this.ofItem(RDCuisineItems.GRAND_BANQUET),
                10.0
        ));
        this.factory.register(ReverieDreams.id("tonkotsu_ramen"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.PORKCHOP, Items.EGG, Items.KELP),
                this.ofItem(RDCuisineItems.TONKOTSU_RAMEN),
                8.0
        ));
        this.factory.register(ReverieDreams.id("magma"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.BEEF, RDIngredientItems.WAGYU_BEEF, RDIngredientItems.PUFF_YO_FRUIT, RDIngredientItems.TRUFFLE),
                this.ofItem(RDCuisineItems.MAGMA),
                8.0
        ));
        this.factory.register(ReverieDreams.id("peach_blossom_soup"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.PEACH, RDBlocks.MAGIC_ICE_BLOCK.asItem(), RDIngredientItems.DEW),
                this.ofItem(RDCuisineItems.PEACH_BLOSSOM_SOUP),
                8.0
        ));
        this.factory.register(ReverieDreams.id("real_seafood_miso_soup"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.SALMON, RDIngredientItems.TROUT),
                this.ofItem(RDCuisineItems.REAL_SEAFOOD_MISO_SOUP),
                8.0
        ));
        this.factory.register(ReverieDreams.id("cooking_tofu"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.TOFU),
                this.ofItem(RDCuisineItems.COOKING_TOFU),
                7.0
        ));
        this.factory.register(ReverieDreams.id("ginko_and_radish_pork_rib_soup"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.GINKGO, RDIngredientItems.WHITE_RADISH, Items.PORKCHOP),
                this.ofItem(RDCuisineItems.GINKGO_AND_RADISH_PORK_RIB_SOUP),
                7.0
        ));
        this.factory.register(ReverieDreams.id("boiled_fish"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.TROUT, RDIngredientItems.CHILI),
                this.ofItem(RDCuisineItems.BOILED_FISH),
                8.0
        ));
        this.factory.register(ReverieDreams.id("dumpling"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.FLOUR),
                this.ofItem(RDCuisineItems.DUMPLING),
                5.0
        ));
        this.factory.register(ReverieDreams.id("glutinous_rice_balls"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.STICKY_RICE),
                this.ofItem(RDCuisineItems.GLUTINOUS_RICE_BALLS),
                5.0
        ));
        this.factory.register(ReverieDreams.id("pseudo_jiritama"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.VENISON, RDIngredientItems.TRUFFLE, RDIngredientItems.CICADA_SHELL),
                this.ofItem(RDCuisineItems.PSEUDO_JIRITAMA),
                12.0
        ));
        this.factory.register(ReverieDreams.id("secret_mushroom_casserole"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.TRUFFLE, Items.BROWN_MUSHROOM, RDIngredientItems.DEW),
                this.ofItem(RDCuisineItems.SECRET_MUSHROOM_CASSEROLE),
                9.0
        ));
        this.factory.register(ReverieDreams.id("mushroom_girls_dance_stew"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.BROWN_MUSHROOM, RDIngredientItems.SHRIMP, RDIngredientItems.OCTOPUS, RDIngredientItems.CHILI),
                this.ofItem(RDCuisineItems.MUSHROOM_GIRLS_DANCE_STEW),
                14.0
        ));
        this.factory.register(ReverieDreams.id("milky_mushroom_soup"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.BROWN_MUSHROOM, Items.POTATO, RDIngredientItems.CREAM),
                this.ofItem(RDCuisineItems.MILKY_MUSHROOM_SOUP),
                8.0
        ));
        this.factory.register(ReverieDreams.id("gensokyo_buddha_jumps_over_the_wall"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.WAGYU_BEEF, RDIngredientItems.SUPREME_TUNA, RDIngredientItems.BLACK_PORK, Items.PUFFERFISH, RDIngredientItems.TRUFFLE),
                this.ofItem(RDCuisineItems.GENSOKYO_BUDDHA_JUMPS_OVER_THE_WALL),
                18.0
        ));
        this.factory.register(ReverieDreams.id("screaming_oden"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.CHILI, RDIngredientItems.CHILI, Items.BEEF, RDIngredientItems.WHITE_RADISH, RDIngredientItems.TOFU),
                this.ofItem(RDCuisineItems.SCREAMING_ODEN),
                12.0
        ));
        this.factory.register(ReverieDreams.id("lion_head"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.BEEF),
                this.ofItem(RDCuisineItems.LION_HEAD),
                7.0
        ));
        this.factory.register(ReverieDreams.id("luohan_vegetarian"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.UDUMBARA, RDIngredientItems.BAMBOO_SHOOTS, RDIngredientItems.TRUFFLE, RDIngredientItems.PINE_NUT, RDIngredientItems.LOTUS_NUTS),
                this.ofItem(RDCuisineItems.LUOHAN_VEGETARIAN),
                12.0
        ));
        this.factory.register(ReverieDreams.id("taichi_bagua_fish_maw"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.SUPREME_TUNA, Items.BROWN_MUSHROOM, RDIngredientItems.WHITE_RADISH, Items.EGG, RDIngredientItems.GINKGO),
                this.ofItem(RDCuisineItems.TAICHI_BAGUA_FISH_MAW),
                14.0
        ));
        this.factory.register(ReverieDreams.id("tianshi_braised_chestnut_mushrooms"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.CHESTNUT, Items.BROWN_MUSHROOM, RDIngredientItems.TRUFFLE),
                this.ofItem(RDCuisineItems.TIANSHI_BRAISED_CHESTNUT_MUSHROOMS),
                8.0
        ));
        this.factory.register(ReverieDreams.id("the_beauty_of_han_palace"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.HAGFISH, RDIngredientItems.TOFU, RDIngredientItems.CRAB, Items.BAMBOO, RDIngredientItems.DEW),
                this.ofItem(RDCuisineItems.THE_BEAUTY_OF_HAN_PALACE),
                12.0
        ));
        this.factory.register(ReverieDreams.id("bamboo_shoots_stewed_in_stone_pot"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.BAMBOO, RDIngredientItems.BAMBOO_SHOOTS, Items.BEEF),
                this.ofItem(RDCuisineItems.BAMBOO_SHOOTS_STEWED_IN_STONE_POT),
                7.0
        ));
        this.factory.register(ReverieDreams.id("plum_tea_rice"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.PLUM, Items.KELP),
                this.ofItem(RDCuisineItems.PLUM_TEA_RICE),
                4.0
        ));
        this.factory.register(ReverieDreams.id("green_fairy_mushroom"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.TOON, Items.BROWN_MUSHROOM),
                this.ofItem(RDCuisineItems.GREEN_FAIRY_MUSHROOM),
                6.0
        ));
        this.factory.register(ReverieDreams.id("poisonous_garden"), new KitchenRecipe(
                cookingPot,
                this.ofList(Items.PUFFERFISH, RDIngredientItems.PLUM, RDIngredientItems.HAGFISH, RDIngredientItems.GINKGO),
                this.ofItem(RDCuisineItems.POISONOUS_GARDEN),
                8.0
        ));
        this.factory.register(ReverieDreams.id("beef_hot_pot"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.CHILI, RDIngredientItems.WHITE_RADISH, RDIngredientItems.TRUFFLE, Items.BEEF, RDIngredientItems.WAGYU_BEEF),
                this.ofItem(RDCuisineItems.BEEF_HOT_POT),
                5.0
        ));
        this.factory.register(ReverieDreams.id("sea_urchin_shingen_pancake"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.SEA_URCHIN, RDIngredientItems.TUNA, RDIngredientItems.TREMELLA, RDIngredientItems.DEW),
                this.ofItem(RDCuisineItems.SEA_URCHIN_SHINGEN_PANCAKE),
                12.0
        ));
        this.factory.register(ReverieDreams.id("heart_porridge_gruel"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.TREMELLA, RDIngredientItems.LOTUS_NUTS),
                this.ofItem(RDCuisineItems.HEART_PORRIDGE_GRUEL),
                5.0
        ));
        this.factory.register(ReverieDreams.id("superme_seafood_noodles"), new KitchenRecipe(
                cookingPot,
                this.ofList(RDIngredientItems.SUPREME_TUNA, Items.KELP, RDIngredientItems.OCTOPUS, RDIngredientItems.CRAB, RDIngredientItems.SHRIMP),
                this.ofItem(RDCuisineItems.SUPERME_SEAFOOD_NOODLES),
                12.0
        ));
    }

    private void configuredGrill() {
        // 烧烤架
        this.factory.register(ReverieDreams.id("pork_and_trout_smoked"), new KitchenRecipe(
                grill,
                this.ofList(RDIngredientItems.TROUT, Items.PORKCHOP),
                this.ofItem(RDCuisineItems.PORK_AND_TROUT_SMOKED),
                7.0
        ));
        this.factory.register(ReverieDreams.id("grilled_hagfish"), new KitchenRecipe(
                grill,
                this.ofList(RDIngredientItems.HAGFISH),
                this.ofItem(RDCuisineItems.GRILLED_HAGFISH),
                7.0
        ));
        this.factory.register(ReverieDreams.id("energy_string"), new KitchenRecipe(
                grill,
                this.ofList(Items.BEEF, RDIngredientItems.ONION, Items.PUMPKIN),
                this.ofItem(RDCuisineItems.ENERGY_STRING),
                12.0
        ));
        this.factory.register(ReverieDreams.id("two_heavens_one_style"), new KitchenRecipe(
                grill,
                this.ofList(RDIngredientItems.BLACK_PORK, RDIngredientItems.WILD_BOAR_MEAT),
                this.ofItem(RDCuisineItems.TWO_HEAVENS_ONE_STYLE),
                18.0
        ));
        this.factory.register(ReverieDreams.id("roasted_mushrooms"), new KitchenRecipe(
                grill,
                this.ofList(Items.BROWN_MUSHROOM),
                this.ofItem(RDCuisineItems.ROASTED_MUSHROOMS),
                6.0
        ));
        this.factory.register(ReverieDreams.id("honey_bbq_pork"), new KitchenRecipe(
                grill,
                this.ofList(Items.PORKCHOP, Items.HONEY_BOTTLE),
                this.ofItem(RDCuisineItems.HONEY_BBQ_PORK),
                7.0
        ));
        this.factory.register(ReverieDreams.id("phoenix"), new KitchenRecipe(
                grill,
                this.ofList(RDIngredientItems.FLOUR, Items.HONEY_BOTTLE, Items.POTATO, RDIngredientItems.ONION, RDIngredientItems.WHITE_RADISH),
                this.ofItem(RDCuisineItems.PHOENIX),
                12.0
        ));
        this.factory.register(ReverieDreams.id("horai_dama_no_rda"), new KitchenRecipe(
                grill,
                this.ofList(Items.BAMBOO, Items.PORKCHOP, RDIngredientItems.SALMON, RDIngredientItems.WAGYU_BEEF, RDIngredientItems.VENISON),
                this.ofItem(RDCuisineItems.HORAI_DAMA_NO_EDA),
                13.0
        ));
        this.factory.register(ReverieDreams.id("all_meat_feast"), new KitchenRecipe(
                grill,
                this.ofList(RDIngredientItems.WILD_BOAR_MEAT, RDIngredientItems.VENISON, RDIngredientItems.BLACK_PORK, RDIngredientItems.WAGYU_BEEF),
                this.ofItem(RDCuisineItems.ALL_MEAT_FEAST),
                14.0
        ));
        this.factory.register(ReverieDreams.id("one_hit_kill"), new KitchenRecipe(
                grill,
                this.ofList(RDIngredientItems.WILD_BOAR_MEAT, RDIngredientItems.VENISON, RDIngredientItems.ONION),
                this.ofItem(RDCuisineItems.ONE_HIT_KILL),
                9.0
        ));
        this.factory.register(ReverieDreams.id("baked_sweet_potatoes"), new KitchenRecipe(
                grill,
                this.ofList(RDIngredientItems.SWEET_POTATO),
                this.ofItem(RDCuisineItems.BAKED_SWEET_POTATOES),
                6.0
        ));
        this.factory.register(ReverieDreams.id("biscay_biscuits"), new KitchenRecipe(
                grill,
                this.ofList(RDIngredientItems.FLOUR, RDIngredientItems.CHEESE),
                this.ofItem(RDCuisineItems.BISCAY_BISCUITS),
                5.0
        ));
        this.factory.register(ReverieDreams.id("pirate_bacon"), new KitchenRecipe(
                grill,
                this.ofList(Items.BEEF, RDIngredientItems.BLACK_SALT, RDIngredientItems.CHILI, Items.HONEY_BOTTLE),
                this.ofItem(RDCuisineItems.PIRATE_BACON),
                9.0
        ));
        this.factory.register(ReverieDreams.id("fantasy_is_all_the_rage"), new KitchenRecipe(
                grill,
                this.ofList(RDIngredientItems.ONION, RDIngredientItems.WILD_BOAR_MEAT, Items.BEEF, RDIngredientItems.TRUFFLE, RDIngredientItems.TOMATO),
                this.ofItem(RDCuisineItems.FANTASY_IS_ALL_THE_RAGE),
                18.0
        ));
        this.factory.register(ReverieDreams.id("cat_kululi"), new KitchenRecipe(
                grill,
                this.ofList(Items.COCOA_BEANS, RDIngredientItems.FLOUR, Items.EGG),
                this.ofItem(RDCuisineItems.CAT_KULULI),
                7.0
        ));
        this.factory.register(ReverieDreams.id("cat_pizza"), new KitchenRecipe(
                grill,
                this.ofList(Items.BROWN_MUSHROOM, RDIngredientItems.ONION, RDIngredientItems.BROCCOLI, RDIngredientItems.WILD_BOAR_MEAT),
                this.ofItem(RDCuisineItems.CAT_PIZZA),
                10.0
        ));
    }

    // 料理台
    private void configuredCuttingBoard() {
        this.factory.register(ReverieDreams.id("rice_ball"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.KELP),
                this.ofItem(RDCuisineItems.RICE_BALL),
                5.0
        ));
        this.factory.register(ReverieDreams.id("grilled_pork_rice_balls"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.PORKCHOP),
                this.ofItem(RDCuisineItems.GRILLED_PORK_RICE_BALLS),
                6.0
        ));
        this.factory.register(ReverieDreams.id("warm_rice_ball"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.ONION, RDIngredientItems.TROUT),
                this.ofItem(RDCuisineItems.WARM_RICE_BALL),
                8.0
        ));
        this.factory.register(ReverieDreams.id("failing_sakura_snow"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.SUPREME_TUNA),
                this.ofItem(RDCuisineItems.FAILING_SAKURA_SNOW),
                12.0
        ));
        this.factory.register(ReverieDreams.id("cold_tofu"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.WHITE_RADISH, RDIngredientItems.TOFU),
                this.ofItem(RDCuisineItems.COLD_TOFU),
                5.0
        ));
        this.factory.register(ReverieDreams.id("vegetable_special"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.POTATO, RDIngredientItems.ONION, Items.PUMPKIN),
                this.ofItem(RDCuisineItems.VEGETABLE_SPECIAL),
                5.0
        ));
        this.factory.register(ReverieDreams.id("sashimi_platter"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.SALMON, RDIngredientItems.TUNA),
                this.ofItem(RDCuisineItems.SASHIMI_PLATTER),
                5.0
        ));
        this.factory.register(ReverieDreams.id("secret_dried_fish"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.TROUT),
                this.ofItem(RDCuisineItems.SECRET_DRIED_FISH),
                8.0
        ));
        this.factory.register(ReverieDreams.id("cold_dish_carving"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.WHITE_RADISH),
                this.ofItem(RDCuisineItems.COLD_DISH_CARVING),
                5.0
        ));
        this.factory.register(ReverieDreams.id("arctic_sweet_shrimp_and_peach_salad"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.PEACH, RDBlocks.MAGIC_ICE_BLOCK.asItem(), RDIngredientItems.SHRIMP),
                this.ofItem(RDCuisineItems.ARCTIC_SWEET_SHRIMP_AND_PEACH_SALAD),
                10.0
        ));
        this.factory.register(ReverieDreams.id("moonlight_dumplings"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.MOONFLOWER, RDIngredientItems.STICKY_RICE),
                this.ofItem(RDCuisineItems.MOONLIGHT_DUMPLINGS),
                8.0
        ));
        this.factory.register(ReverieDreams.id("mochi"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.STICKY_RICE),
                this.ofItem(RDCuisineItems.MOCHI),
                7.0
        ));
        this.factory.register(ReverieDreams.id("white_peach_eight_bridge"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.STICKY_RICE, RDIngredientItems.PEACH),
                this.ofItem(RDCuisineItems.WHITE_PEACH_EIGHT_BRIDGE),
                5.0
        ));
        this.factory.register(ReverieDreams.id("moon_lover"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.BUTTER, RDIngredientItems.FLOUR, Items.EGG, RDIngredientItems.MOONFLOWER),
                this.ofItem(RDCuisineItems.MOON_LOVERS),
                10.0
        ));
        this.factory.register(ReverieDreams.id("flowing_water_noodles"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.FLOUR, Items.BAMBOO),
                this.ofItem(RDCuisineItems.FLOWING_WATER_NOODLES),
                10.0
        ));
        this.factory.register(ReverieDreams.id("maoyu_tricolor_ice_cream"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.DEW, RDIngredientItems.TOFU, Items.HONEY_BOTTLE, Items.EGG),
                this.ofItem(RDCuisineItems.MAOYU_TRICOLOR_ICE_CREAM),
                8.0
        ));
        this.factory.register(ReverieDreams.id("maoyu_lava_tofu"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.TOFU, RDIngredientItems.CHILI, Items.BEEF, RDIngredientItems.ONION),
                this.ofItem(RDCuisineItems.MAOYU_LAVA_TOFU),
                8.0
        ));
        this.factory.register(ReverieDreams.id("scarlet_devils_cake"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.DEW, Items.PUMPKIN, Items.POTATO, Items.HONEY_BOTTLE),
                this.ofItem(RDCuisineItems.SCARLET_DEVILS_CAKE),
                8.0
        ));
        this.factory.register(ReverieDreams.id("unconscious_monster_mousse"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.TOFU, RDIngredientItems.BUTTER, Items.HONEY_BOTTLE, RDIngredientItems.ONION),
                this.ofItem(RDCuisineItems.UNCONSCIOUS_MONSTER_MOUSSE),
                8.0
        ));
        this.factory.register(ReverieDreams.id("pickled_cucumbers"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.CUCUMBER, RDIngredientItems.BLACK_SALT),
                this.ofItem(RDCuisineItems.PICKLED_CUCUMBERS),
                6.0
        ));
        this.factory.register(ReverieDreams.id("sea_urchin_sashimi"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.SEA_URCHIN, RDIngredientItems.DEW),
                this.ofItem(RDCuisineItems.SEA_URCHIN_SASHIMI),
                8.0
        ));
        this.factory.register(ReverieDreams.id("nigiri_sushi"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.SALMON, RDIngredientItems.TUNA),
                this.ofItem(RDCuisineItems.NIGIRI_SUSHI),
                6.0
        ));
        this.factory.register(ReverieDreams.id("gloomy_fruit_pie"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.LEMON, RDIngredientItems.GRAPE, RDIngredientItems.CHEESE),
                this.ofItem(RDCuisineItems.GLOOMY_FRUIT_PIE),
                8.0
        ));
        this.factory.register(ReverieDreams.id("crisp_cyclone"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.FLOUR, Items.HONEY_BOTTLE, RDIngredientItems.CICADA_SHELL),
                this.ofItem(RDCuisineItems.CRISP_CYCLONE),
                5.0
        ));
        this.factory.register(ReverieDreams.id("oedo_boat_festival"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.SALMON, RDIngredientItems.TUNA, RDIngredientItems.SUPREME_TUNA, RDIngredientItems.TROUT, RDBlocks.MAGIC_ICE_BLOCK.asItem()),
                this.ofItem(RDCuisineItems.OEDO_BOAT_FESTIVAL),
                24.0
        ));
        this.factory.register(ReverieDreams.id("cat_food"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.TROUT, RDIngredientItems.DEW, RDIngredientItems.FLOUR),
                this.ofItem(RDCuisineItems.CAT_FOOD),
                5.0
        ));
        this.factory.register(ReverieDreams.id("skinny_horse_dumpling"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.STICKY_RICE, RDIngredientItems.STICKY_RICE),
                this.ofItem(RDCuisineItems.SKINNY_HORSE_DUMPLING),
                9.0
        ));
        this.factory.register(ReverieDreams.id("gensokyo_star_lotus_ship"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.PUMPKIN, RDIngredientItems.LOTUS_NUTS, RDIngredientItems.TUNA, RDIngredientItems.TWIN_LOTUS, RDIngredientItems.MOONFLOWER),
                this.ofItem(RDCuisineItems.GENSOKYO_STAR_LOTUS_SHIP),
                13.0
        ));
        this.factory.register(ReverieDreams.id("candied_chestnuts"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.HONEY_BOTTLE, RDIngredientItems.CHESTNUT),
                this.ofItem(RDCuisineItems.CANDIED_CHESTNUTS),
                6.0
        ));
        this.factory.register(ReverieDreams.id("reversing_the_world"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.BAMBOO, RDIngredientItems.FLOWERS, RDIngredientItems.PLUM, RDIngredientItems.BLACK_PORK, RDIngredientItems.TRUFFLE),
                this.ofItem(RDCuisineItems.REVERSING_THE_WORLD),
                12.0
        ));
        this.factory.register(ReverieDreams.id("red_bean_daifuku"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.RED_BEANS, RDIngredientItems.STICKY_RICE),
                this.ofItem(RDCuisineItems.RED_BEAN_DAIFUKU),
                7.0
        ));
        this.factory.register(ReverieDreams.id("bamboo_tube_roasted_drunken_shrimp"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.BAMBOO, RDIngredientItems.SHRIMP, RDIngredientItems.BROCCOLI),
                this.ofItem(RDCuisineItems.BAMBOO_TUBE_ROASTED_DRUNKEN_SHRIMP),
                5.0
        ));
        this.factory.register(ReverieDreams.id("cats_playing_in_water"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.PEACH, RDIngredientItems.FICUS_MICROCARPA, RDIngredientItems.CREAM, RDIngredientItems.FLOUR, Items.COCOA_BEANS),
                this.ofItem(RDCuisineItems.CATS_PLAYING_IN_WATER),
                12.0
        ));
        this.factory.register(ReverieDreams.id("moonlight_over_lotus_pond"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(RDIngredientItems.GRAPE, RDIngredientItems.FICUS_MICROCARPA, RDIngredientItems.CREAM, RDIngredientItems.TREMELLA),
                this.ofItem(RDCuisineItems.MOONLIGHT_OVER_LOTUS_POND),
                12.0
        ));
        this.factory.register(ReverieDreams.id("longyin_peach"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.COCOA_BEANS, RDIngredientItems.PEACH, RDIngredientItems.PEACH, RDIngredientItems.PEACH, RDIngredientItems.PEACH),
                this.ofItem(RDCuisineItems.LONGYIN_PEACH),
                18.0
        ));
        this.factory.register(ReverieDreams.id("molecular_egg"), new KitchenRecipe(
                cuttingBoard,
                this.ofList(Items.COCOA_BEANS, Items.PUMPKIN, RDIngredientItems.CREAM),
                this.ofItem(RDCuisineItems.MOLECULAR_EGG),
                18.0
        ));
    }

    // 蒸锅
    private void configuredSteamer() {
        this.factory.register(ReverieDreams.id("dew_boiled_eggs"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.DEW, Items.EGG),
                this.ofItem(RDCuisineItems.DEW_BOILED_EGGS),
                3.0
        ));
        this.factory.register(ReverieDreams.id("udumbara_cake"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.UDUMBARA, RDIngredientItems.DEW),
                this.ofItem(RDCuisineItems.UDUMBARA_CAKE),
                7.0
        ));
        this.factory.register(ReverieDreams.id("bear_paw"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.BLACK_PORK, RDIngredientItems.BAMBOO_SHOOTS, Items.PUFFERFISH),
                this.ofItem(RDCuisineItems.BEAR_PAW),
                12.0
        ));
        this.factory.register(ReverieDreams.id("poetry_and_ginkgo"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.GINKGO, Items.HONEY_BOTTLE),
                this.ofItem(RDCuisineItems.POETRY_AND_GINKGO),
                8.0
        ));
        this.factory.register(ReverieDreams.id("risotto"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.ONION, Items.BROWN_MUSHROOM, RDIngredientItems.BAMBOO_SHOOTS, RDIngredientItems.BUTTER),
                this.ofItem(RDCuisineItems.RISOTTO),
                6.0
        ));
        this.factory.register(ReverieDreams.id("scones"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.BUTTER, RDIngredientItems.FLOUR),
                this.ofItem(RDCuisineItems.SCONES),
                7.0
        ));
        this.factory.register(ReverieDreams.id("cream_stew"), new KitchenRecipe(
                streamer,
                this.ofList(Items.BROWN_MUSHROOM, RDIngredientItems.ONION, RDIngredientItems.BUTTER),
                this.ofItem(RDCuisineItems.CREAM_STEW),
                7.0
        ));
        this.factory.register(ReverieDreams.id("taketorihime"), new KitchenRecipe(
                streamer,
                this.ofList(Items.BAMBOO, RDIngredientItems.BAMBOO_SHOOTS, RDIngredientItems.TRUFFLE, RDIngredientItems.GINKGO, RDIngredientItems.BLACK_PORK),
                this.ofItem(RDCuisineItems.TAKETORIHIME),
                12.0
        ));
        this.factory.register(ReverieDreams.id("pig_deer_butterfly"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.WILD_BOAR_MEAT, RDIngredientItems.VENISON, RDIngredientItems.MOONFLOWER),
                this.ofItem(RDCuisineItems.PIG_DEER_BUTTERFLY),
                8.0
        ));
        this.factory.register(ReverieDreams.id("bamboo_steamed_egg"), new KitchenRecipe(
                streamer,
                this.ofList(Items.BAMBOO, Items.EGG, Items.KELP, Items.BROWN_MUSHROOM),
                this.ofItem(RDCuisineItems.BAMBOO_STEAMED_EGG),
                6.0
        ));
        this.factory.register(ReverieDreams.id("moon_cake"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.MOONFLOWER, RDIngredientItems.FLOUR),
                this.ofItem(RDCuisineItems.MOON_CAKE),
                10.0
        ));
        this.factory.register(ReverieDreams.id("ordinary_small_cake"), new KitchenRecipe(
                streamer,
                this.ofList(Items.EGG, RDIngredientItems.GRAPE, RDIngredientItems.CREAM),
                this.ofItem(RDCuisineItems.ORDINARY_SMALL_CAKE),
                8.0
        ));
        this.factory.register(ReverieDreams.id("seven_colored_yokan"), new KitchenRecipe(
                streamer,
                this.ofList(Items.KELP, RDIngredientItems.GRAPE, RDIngredientItems.DEW, RDIngredientItems.UDUMBARA),
                this.ofItem(RDCuisineItems.SEVEN_COLORED_YOKAN),
                8.0
        ));
        this.factory.register(ReverieDreams.id("pumpkin_shrimp_cake"), new KitchenRecipe(
                streamer,
                this.ofList(Items.PUMPKIN, RDIngredientItems.SHRIMP, RDIngredientItems.TOFU),
                this.ofItem(RDCuisineItems.PUMPKIN_SHRIMP_CAKE),
                9.0
        ));
        this.factory.register(ReverieDreams.id("depressed_cheese_sticks"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.CHEESE, RDIngredientItems.GINKGO, RDIngredientItems.GINKGO),
                this.ofItem(RDCuisineItems.DEPRESSED_CHEESE_STICKS),
                6.0
        ));
        this.factory.register(ReverieDreams.id("looking_up_at_the_ceiling_fruit_pie"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.TROUT, RDIngredientItems.FLOUR, RDIngredientItems.PEACH),
                this.ofItem(RDCuisineItems.LOOKING_UP_AT_THE_CEILING_FRUIT_PIE),
                9.0
        ));
        this.factory.register(ReverieDreams.id("beetle_steamed_cake"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.FLOUR, RDIngredientItems.BLACK_PORK, Items.HONEY_BOTTLE, RDIngredientItems.CICADA_SHELL),
                this.ofItem(RDCuisineItems.BEETLE_STEAMED_CAKE),
                12.0
        ));
        this.factory.register(ReverieDreams.id("sakura_pudding"), new KitchenRecipe(
                streamer,
                this.ofList(Items.HONEY_BOTTLE, RDIngredientItems.PEACH),
                this.ofItem(RDCuisineItems.SAKURA_PUDDING),
                6.0
        ));
        this.factory.register(ReverieDreams.id("refreshing_pudding"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.GRAPE, RDIngredientItems.GRAPE, RDIngredientItems.LEMON),
                this.ofItem(RDCuisineItems.REFRESHING_PUDDING),
                8.0
        ));
        this.factory.register(ReverieDreams.id("burnt_pudding"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.GRAPE, Items.HONEY_BOTTLE, RDIngredientItems.LEMON, RDIngredientItems.LEMON),
                this.ofItem(RDCuisineItems.BURNT_PUDDING),
                8.0
        ));
        this.factory.register(ReverieDreams.id("fish_leaps_over_dragon_gate"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.SUPREME_TUNA, RDIngredientItems.VENISON, Items.HONEY_BOTTLE, RDIngredientItems.MOONFLOWER, RDIngredientItems.TRUFFLE),
                this.ofItem(RDCuisineItems.FISH_LEAPS_OVER_DRAGON_GATE),
                12.0
        ));
        this.factory.register(ReverieDreams.id("fright_adventure"), new KitchenRecipe(
                streamer,
                this.ofList(Items.BROWN_MUSHROOM, RDIngredientItems.UDUMBARA, Items.HONEY_BOTTLE, RDIngredientItems.CREAM),
                this.ofItem(RDCuisineItems.FRIGHT_ADVENTURE),
                12.0
        ));
        this.factory.register(ReverieDreams.id("holy_white_lotus_seed_cake"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.GINKGO, RDIngredientItems.LOTUS_NUTS, RDIngredientItems.FLOUR, RDIngredientItems.BUTTER),
                this.ofItem(RDCuisineItems.HOLY_WHITE_LOTUS_SEED_CAKE),
                10.0
        ));
        this.factory.register(ReverieDreams.id("pine_nut_cake"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.STICKY_RICE, RDIngredientItems.PINE_NUT),
                this.ofItem(RDCuisineItems.PINE_NUT_CAKE),
                8.0
        ));
        this.factory.register(ReverieDreams.id("shiraga_sadamatsu"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.VENISON, RDIngredientItems.GINKGO, RDIngredientItems.PINE_NUT),
                this.ofItem(RDCuisineItems.SHIRAGA_SADAMATSU),
                12.0
        ));
        this.factory.register(ReverieDreams.id("lotus_fish_rice_bowl"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.SUPREME_TUNA, RDIngredientItems.TWIN_LOTUS, RDIngredientItems.LOTUS_NUTS, RDIngredientItems.DEW),
                this.ofItem(RDCuisineItems.LOTUS_FISH_RICE_BOWL),
                11.0
        ));
        this.factory.register(ReverieDreams.id("bamboo_tube_steamed_pork"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.BAMBOO_SHOOTS, RDIngredientItems.DEW, RDIngredientItems.BLACK_PORK),
                this.ofItem(RDCuisineItems.BAMBOO_TUBE_STEAMED_PORK),
                9.0
        ));
        this.factory.register(ReverieDreams.id("green_bamboo_welcomes_spring"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.CUCUMBER, Items.EGG, RDIngredientItems.WHITE_RADISH, RDIngredientItems.VENISON, RDIngredientItems.MOONFLOWER),
                this.ofItem(RDCuisineItems.GREEN_BAMBOO_WELCOMES_SPRING),
                14.0
        ));
        this.factory.register(ReverieDreams.id("steamed_egg_with_sea_urchin"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.SEA_URCHIN, Items.EGG),
                this.ofItem(RDCuisineItems.STEAMED_EGG_WITH_SEA_URCHIN),
                7.0
        ));
        this.factory.register(ReverieDreams.id("flowers_birds_wind_and_moon"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.FLOWERS, RDIngredientItems.MOONFLOWER, RDIngredientItems.CREAM),
                this.ofItem(RDCuisineItems.FLOWERS_BIRDS_WIND_AND_MOON),
                9.0
        ));
        this.factory.register(ReverieDreams.id("the_dream"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.FLOWERS, RDIngredientItems.UDUMBARA, RDIngredientItems.MOONFLOWER, RDIngredientItems.DEW, RDIngredientItems.CREAM),
                this.ofItem(RDCuisineItems.THE_DREAM),
                12.0
        ));
        this.factory.register(ReverieDreams.id("a_little_sweet_poison"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.UDUMBARA, RDIngredientItems.CREAM, RDIngredientItems.GRAPE, RDIngredientItems.GINKGO),
                this.ofItem(RDCuisineItems.A_LITTLE_SWEET_POISON),
                10.0
        ));
        this.factory.register(ReverieDreams.id("rapunzel"), new KitchenRecipe(
                streamer,
                this.ofList(Items.PUMPKIN, RDIngredientItems.SHRIMP),
                this.ofItem(RDCuisineItems.RAPUNZEL),
                5.0
        ));
        this.factory.register(ReverieDreams.id("mad_hatter_tea_party"), new KitchenRecipe(
                streamer,
                this.ofList(Items.COCOA_BEANS, RDIngredientItems.CREAM, RDIngredientItems.FLOUR, Items.BROWN_MUSHROOM_BLOCK, RDIngredientItems.BROCCOLI),
                this.ofItem(RDCuisineItems.MAD_HATTER_TEA_PARTY),
                15.0
        ));
        this.factory.register(ReverieDreams.id("peach_blossom_glaze_roll"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.PEACH, RDIngredientItems.RED_BEANS, RDIngredientItems.FICUS_MICROCARPA),
                this.ofItem(RDCuisineItems.PEACH_BLOSSOM_GLAZE_ROLL),
                8.0
        ));
        this.factory.register(ReverieDreams.id("the_source_of_life"), new KitchenRecipe(
                streamer,
                this.ofList(Items.COCOA_BEANS, RDIngredientItems.TREMELLA, Items.PUMPKIN, RDIngredientItems.DEW),
                this.ofItem(RDCuisineItems.THE_SOURCE_OF_LIFE),
                13.0
        ));
        this.factory.register(ReverieDreams.id("the_mars"), new KitchenRecipe(
                streamer,
                this.ofList(RDIngredientItems.FICUS_MICROCARPA, RDIngredientItems.GRAPE, RDIngredientItems.CRAB, RDIngredientItems.DEW),
                this.ofItem(RDCuisineItems.THE_MARS),
                24.0
        ));
    }

    // 炒锅
    private void configuredFryingPan() {
        this.factory.register(ReverieDreams.id("fried_pork_shreds"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.PORKCHOP),
                this.ofItem(RDCuisineItems.FRIED_PORK_SHREDS),
                8.0
        ));
        this.factory.register(ReverieDreams.id("braised_eel"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.ONION, RDIngredientItems.HAGFISH),
                this.ofItem(RDCuisineItems.BRAISED_EEL),
                6.0
        ));
        this.factory.register(ReverieDreams.id("fried_hagfish"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.HAGFISH),
                this.ofItem(RDCuisineItems.FRIED_HAGFISH),
                7.0
        ));
        this.factory.register(ReverieDreams.id("fried_tofu"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.TOFU),
                this.ofItem(RDCuisineItems.FRIED_TOFU),
                7.0
        ));
        this.factory.register(ReverieDreams.id("potato_croquettes"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.POTATO),
                this.ofItem(RDCuisineItems.POTATO_CROQUETTES),
                7.0
        ));
        this.factory.register(ReverieDreams.id("deep_fried_cicada_shells"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.TOFU, RDIngredientItems.CICADA_SHELL),
                this.ofItem(RDCuisineItems.DEEP_FRIED_CICADA_SHELLS),
                7.0
        ));
        this.factory.register(ReverieDreams.id("fried_pork_cutlet"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.PORKCHOP),
                this.ofItem(RDCuisineItems.FRIED_PORK_CUTLET),
                7.0
        ));
        this.factory.register(ReverieDreams.id("butter_steak"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.WAGYU_BEEF),
                this.ofItem(RDCuisineItems.BUTTER_STEAK),
                7.0
        ));
        this.factory.register(ReverieDreams.id("beef_wellington"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.WAGYU_BEEF, RDIngredientItems.FLOUR, Items.EGG, RDIngredientItems.BUTTER, RDIngredientItems.TRUFFLE),
                this.ofItem(RDCuisineItems.BEEF_WELLINGTON),
                14.0
        ));
        this.factory.register(ReverieDreams.id("eggs_benedict"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.EGG, RDIngredientItems.BAMBOO_SHOOTS, RDIngredientItems.BUTTER, RDIngredientItems.FLOUR),
                this.ofItem(RDCuisineItems.EGGS_BENEDICT),
                7.0
        ));
        this.factory.register(ReverieDreams.id("hot_waffles"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.HONEY_BOTTLE, RDIngredientItems.FLOUR, Items.EGG),
                this.ofItem(RDCuisineItems.HOT_WAFFLES),
                9.0
        ));
        this.factory.register(ReverieDreams.id("pan_fried_salmon"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.SALMON, RDIngredientItems.BAMBOO_SHOOTS),
                this.ofItem(RDCuisineItems.PAN_FRIED_SALMON),
                10.0
        ));
        this.factory.register(ReverieDreams.id("bamboo_shoots_fried_meat"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.BAMBOO_SHOOTS, Items.PORKCHOP),
                this.ofItem(RDCuisineItems.BAMBOO_SHOOTS_FRIED_MEAT),
                10.0
        ));
        this.factory.register(ReverieDreams.id("stinky_tofu"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.TOFU, RDIngredientItems.CHILI),
                this.ofItem(RDCuisineItems.STINKY_TOFU),
                5.0
        ));
        this.factory.register(ReverieDreams.id("colorful_jade_fried_buns"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.BROWN_MUSHROOM, RDIngredientItems.BLACK_PORK),
                this.ofItem(RDCuisineItems.COLORFUL_JADE_FRIED_BUNS),
                8.0
        ));
        this.factory.register(ReverieDreams.id("mapo_tofu"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.TOFU, Items.PORKCHOP, RDIngredientItems.CHILI),
                this.ofItem(RDCuisineItems.MAPO_TOFU),
                6.0
        ));
        this.factory.register(ReverieDreams.id("fried_shrimp_tempura"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.SHRIMP, RDIngredientItems.FLOUR),
                this.ofItem(RDCuisineItems.FRIED_SHRIMP_TEMPURA),
                6.0
        ));
        this.factory.register(ReverieDreams.id("golden_crispy_fish_cake"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.TROUT, RDIngredientItems.FLOUR, Items.HONEY_BOTTLE),
                this.ofItem(RDCuisineItems.GOLDEN_CRISPY_FISH_CAKE),
                9.0
        ));
        this.factory.register(ReverieDreams.id("baked_crab_with_cream"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.CREAM, RDIngredientItems.CRAB),
                this.ofItem(RDCuisineItems.BAKED_CRAB_WITH_CREAM),
                12.0
        ));
        this.factory.register(ReverieDreams.id("okonomiyaki"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.FLOUR, Items.EGG, RDIngredientItems.WHITE_RADISH),
                this.ofItem(RDCuisineItems.OKONOMIYAKI),
                6.0
        ));
        this.factory.register(ReverieDreams.id("takoyaki"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.FLOUR, Items.KELP, RDIngredientItems.OCTOPUS),
                this.ofItem(RDCuisineItems.TAKOYAKI),
                8.0
        ));
        this.factory.register(ReverieDreams.id("mushroom_meat_slices"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.BROWN_MUSHROOM, Items.PORKCHOP),
                this.ofItem(RDCuisineItems.MUSHROOM_MEAT_SLICES),
                6.0
        ));
        this.factory.register(ReverieDreams.id("giant_tamagoyaki"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.FLOUR, RDIngredientItems.FLOUR, Items.EGG, Items.EGG),
                this.ofItem(RDCuisineItems.GIANT_TAMAGOYAKI),
                12.0
        ));
        this.factory.register(ReverieDreams.id("salmon_tempura"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.SALMON, RDIngredientItems.BUTTER, Items.EGG, RDIngredientItems.FLOUR),
                this.ofItem(RDCuisineItems.SALMON_TEMPURA),
                8.0
        ));
        this.factory.register(ReverieDreams.id("cheese_egg"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.EGG, RDIngredientItems.CHEESE),
                this.ofItem(RDCuisineItems.CHEESE_EGG),
                6.0
        ));
        this.factory.register(ReverieDreams.id("hell_thrill_warning"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.CHILI, RDIngredientItems.CHILI, RDIngredientItems.CHILI, RDIngredientItems.CHEESE, Items.BEEF),
                this.ofItem(RDCuisineItems.HELL_THRILL_WARNING),
                12.0
        ));
        this.factory.register(ReverieDreams.id("yunshan_cotton_candy"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.HONEY_BOTTLE, RDIngredientItems.PEACH),
                this.ofItem(RDCuisineItems.YUNSHAN_COTTON_CANDY),
                8.0
        ));
        this.factory.register(ReverieDreams.id("candied_sweet_potato"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.SWEET_POTATO, Items.HONEY_BOTTLE),
                this.ofItem(RDCuisineItems.CANDIED_SWEET_POTATO),
                6.0
        ));
        this.factory.register(ReverieDreams.id("pan_fried_mushroom_meat_roll"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.PORKCHOP, Items.BROWN_MUSHROOM, RDIngredientItems.TRUFFLE),
                this.ofItem(RDCuisineItems.PAN_FRIED_MUSHROOM_MEAT_ROLL),
                9.0
        ));
        this.factory.register(ReverieDreams.id("assorted_tempura"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.BLACK_PORK, RDIngredientItems.TRUFFLE, RDIngredientItems.HAGFISH, RDIngredientItems.MOONFLOWER),
                this.ofItem(RDCuisineItems.ASSORTED_TEMPURA),
                7.0
        ));
        this.factory.register(ReverieDreams.id("fried_tomato_strips"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.TOMATO, Items.POTATO),
                this.ofItem(RDCuisineItems.FRIED_TOMATO_STRIPS),
                6.0
        ));
        this.factory.register(ReverieDreams.id("braised_pork_with_peach"), new KitchenRecipe(
                fryingPan,
                this.ofList(Items.HONEY_BOTTLE, RDIngredientItems.PEACH, Items.PORKCHOP),
                this.ofItem(RDCuisineItems.BRAISED_PORK_WITH_PEACH),
                8.0
        ));
        this.factory.register(ReverieDreams.id("dorayaki"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.RED_BEANS, Items.EGG, RDIngredientItems.FLOUR),
                this.ofItem(RDCuisineItems.DORAYAKI),
                6.0
        ));
        this.factory.register(ReverieDreams.id("toon_pancakes"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.TWIN_LOTUS, Items.EGG),
                this.ofItem(RDCuisineItems.TOON_PANCAKES),
                6.0
        ));
        this.factory.register(ReverieDreams.id("eel_egg_donburi"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.HAGFISH, Items.EGG),
                this.ofItem(RDCuisineItems.EEL_EGG_DONBURI),
                5.0
        ));
        this.factory.register(ReverieDreams.id("hula_soup"), new KitchenRecipe(
                fryingPan,
                this.ofList(RDIngredientItems.CHILI, Items.BEEF, RDIngredientItems.TOFU),
                this.ofItem(RDCuisineItems.HULA_SOUP),
                8.0
        ));
    }

    @Override
    public String getName() {
        return "Kitchen Recipe Provider";
    }
}
