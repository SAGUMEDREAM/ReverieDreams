package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.data.CraftingConflict;
import cc.thonly.reverie_dreams.fabric.datagen.generator.AbstractCraftingConflictProvider;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.item.RDCuisineItems;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CraftingConflictProvider extends AbstractCraftingConflictProvider {

    public CraftingConflictProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    @Override
    protected void configured() {
        this.registerEntry(CraftingConflict.of(RDCuisineItems.SEAFOOD_MISO_SOUP, List.of(FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.TOFU_MISO, List.of(FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.STRENGTH_SOUP, List.of(FoodProperties.PETITE, FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.GRILLED_HAGFISH, List.of(FoodProperties.MEAT, FoodProperties.VEGETARIAN)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.TWO_HEAVENS_ONE_STYLE, List.of(FoodProperties.HOMESTYLE)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.FAILING_SAKURA_SNOW, List.of(FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.POTATO_CROQUETTES, List.of(FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.FRIED_HAGFISH, List.of(FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.VEGETABLE_SPECIAL, List.of(FoodProperties.MEAT, FoodProperties.AQUATIC_PRODUCTS, FoodProperties.FIERY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.SASHIMI_PLATTER, List.of(FoodProperties.FIERY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.GRAND_BANQUET, List.of(FoodProperties.HOMESTYLE)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.MAGMA, List.of(FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.DEW_BOILED_EGGS, List.of(FoodProperties.MEAT, FoodProperties.AQUATIC_PRODUCTS, FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.UDUMBARA_CAKE, List.of(FoodProperties.MEAT, FoodProperties.AQUATIC_PRODUCTS)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.COLD_DISH_CARVING, List.of(FoodProperties.MEAT, FoodProperties.FIERY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.ARCTIC_SWEET_SHRIMP_AND_PEACH_SALAD, List.of(FoodProperties.MEAT, FoodProperties.SALTY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.POETRY_AND_GINKGO, List.of(FoodProperties.SALTY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.BUTTER_STEAK, List.of(FoodProperties.SWEET, FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.RISOTTO, List.of(FoodProperties.SWEET)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.BEEF_WELLINGTON, List.of(FoodProperties.SWEET, FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.EGGS_BENEDICT, List.of(FoodProperties.SWEET, FoodProperties.FRUITY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.MOONLIGHT_DUMPLINGS, List.of(FoodProperties.MEAT, FoodProperties.AQUATIC_PRODUCTS, FoodProperties.SALTY, FoodProperties.UMAMI)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.MOCHI, List.of(FoodProperties.MEAT, FoodProperties.AQUATIC_PRODUCTS, FoodProperties.SALTY, FoodProperties.UMAMI)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.WHITE_PEACH_EIGHT_BRIDGE, List.of(FoodProperties.MEAT, FoodProperties.AQUATIC_PRODUCTS, FoodProperties.SALTY, FoodProperties.UMAMI)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.MOON_LOVERS, List.of(FoodProperties.MEAT, FoodProperties.AQUATIC_PRODUCTS, FoodProperties.VEGETARIAN)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.FLOWING_WATER_NOODLES, List.of(FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.STINKY_TOFU, List.of(FoodProperties.SWEET, FoodProperties.FRUITY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.BOILED_FISH, List.of(FoodProperties.MEAT, FoodProperties.SWEET, FoodProperties.COOL, FoodProperties.FRUITY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.FRIED_SHRIMP_TEMPURA, List.of(FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.CRISP_CYCLONE, List.of(FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.LOOKING_UP_AT_THE_CEILING_FRUIT_PIE, List.of(FoodProperties.MEAT)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.LION_HEAD, List.of(FoodProperties.AQUATIC_PRODUCTS, FoodProperties.FILLING)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.GIANT_TAMAGOYAKI, List.of(FoodProperties.GOOD_WITH_ALCOHOL)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.SAKURA_PUDDING, List.of(FoodProperties.GREASY, FoodProperties.SALTY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.REFRESHING_PUDDING, List.of(FoodProperties.GREASY, FoodProperties.SALTY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.BURNT_PUDDING, List.of(FoodProperties.GREASY, FoodProperties.SALTY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.CAT_FOOD, List.of(FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.CHEESE_EGG, List.of(FoodProperties.MEAT)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.HELL_THRILL_WARNING, List.of(FoodProperties.VEGETARIAN, FoodProperties.LIGHT)));
        this.registerEntry(CraftingConflict.of(RDIngredientItems.SWEET_POTATO, List.of(FoodProperties.GOOD_WITH_ALCOHOL, FoodProperties.LIGHT)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.SKINNY_HORSE_DUMPLING, List.of(FoodProperties.MEAT)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.FRIGHT_ADVENTURE, List.of(FoodProperties.HOMESTYLE)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.BISCAY_BISCUITS, List.of(FoodProperties.SOUR)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.PIRATE_BACON, List.of(FoodProperties.SOUR)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.LUOHAN_VEGETARIAN, List.of(FoodProperties.MEAT, FoodProperties.SPICY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.YUNSHAN_COTTON_CANDY, List.of(FoodProperties.MEAT, FoodProperties.SALTY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.HOLY_WHITE_LOTUS_SEED_CAKE, List.of(FoodProperties.SALTY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.GENSOKYO_STAR_LOTUS_SHIP, List.of(FoodProperties.BIZARRE)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.SHIRAGA_SADAMATSU, List.of(FoodProperties.SOUR)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.CANDIED_SWEET_POTATO, List.of(FoodProperties.RAW)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.PAN_FRIED_MUSHROOM_MEAT_ROLL, List.of(FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.FRIED_TOMATO_STRIPS, List.of(FoodProperties.AQUATIC_PRODUCTS)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.DORAYAKI, List.of(FoodProperties.MEAT)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.THE_BEAUTY_OF_HAN_PALACE, List.of(FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.BAMBOO_TUBE_STEAMED_PORK, List.of(FoodProperties.RAW)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.GREEN_BAMBOO_WELCOMES_SPRING, List.of(FoodProperties.BIZARRE, FoodProperties.MUSHROOMS)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.STEAMED_EGG_WITH_SEA_URCHIN, List.of(FoodProperties.MOUNTAIN_DELICACY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.FANTASY_IS_ALL_THE_RAGE, List.of(FoodProperties.LIGHT, FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.FLOWERS_BIRDS_WIND_AND_MOON, List.of(FoodProperties.GREASY, FoodProperties.FIERY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.THE_DREAM, List.of(FoodProperties.MEAT, FoodProperties.AQUATIC_PRODUCTS, FoodProperties.GOOD_WITH_ALCOHOL)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.A_LITTLE_SWEET_POISON, List.of(FoodProperties.MEAT)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.BAMBOO_TUBE_ROASTED_DRUNKEN_SHRIMP, List.of(FoodProperties.FILLING)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.BEEF_HOT_POT, List.of(FoodProperties.COOL, FoodProperties.DREAMLIKE)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.CAT_KULULI, List.of(FoodProperties.COOL, FoodProperties.BIZARRE)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.CAT_PIZZA, List.of(FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.CATS_PLAYING_IN_WATER, List.of(FoodProperties.GOOD_WITH_ALCOHOL, FoodProperties.FIERY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.RAPUNZEL, List.of(FoodProperties.BIZARRE)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.SEA_URCHIN_SHINGEN_PANCAKE, List.of(FoodProperties.FILLING)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.MAD_HATTER_TEA_PARTY, List.of(FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.PEACH_BLOSSOM_GLAZE_ROLL, List.of(FoodProperties.MEAT, FoodProperties.FILLING, FoodProperties.BIZARRE, FoodProperties.MUSHROOMS)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.MOONLIGHT_OVER_LOTUS_POND, List.of(FoodProperties.MEAT, FoodProperties.GOOD_WITH_ALCOHOL, FoodProperties.FIERY, FoodProperties.BIZARRE)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.LONGYIN_PEACH, List.of(FoodProperties.MEAT, FoodProperties.OCEAN_FLAVOR, FoodProperties.RAW, FoodProperties.BIZARRE, FoodProperties.RAW)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.MOLECULAR_EGG, List.of(FoodProperties.MOUNTAIN_DELICACY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.THE_SOURCE_OF_LIFE, List.of(FoodProperties.MOUNTAIN_DELICACY)));
        this.registerEntry(CraftingConflict.of(RDCuisineItems.THE_MARS, List.of(FoodProperties.MOUNTAIN_DELICACY)));
    }

    @Override
    public String getName() {
        return "CraftingConflict";
    }
}
