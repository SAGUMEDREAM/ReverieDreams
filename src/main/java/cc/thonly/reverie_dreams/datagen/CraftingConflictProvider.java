package cc.thonly.reverie_dreams.datagen;

import cc.thonly.reverie_dreams.data.CraftingConflict;
import cc.thonly.reverie_dreams.datagen.generator.AbstractCraftingConflictProvider;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.item.RDFoodItems;
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
        this.registerEntry(CraftingConflict.of(RDFoodItems.SEAFOOD_MISO_SOUP, List.of(FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.TOFU_MISO, List.of(FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.STRENGTH_SOUP, List.of(FoodProperties.PETITE, FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.GRILLED_HAGFISH, List.of(FoodProperties.MEAT, FoodProperties.VEGETARIAN)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.TWO_HEAVENS_ONE_STYLE, List.of(FoodProperties.HOMESTYLE)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.FAILING_SAKURA_SNOW, List.of(FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.POTATO_CROQUETTES, List.of(FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.FRIED_HAGFISH, List.of(FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.VEGETABLE_SPECIAL, List.of(FoodProperties.MEAT, FoodProperties.AQUATIC_PRODUCTS, FoodProperties.FIERY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.SASHIMI_PLATTER, List.of(FoodProperties.FIERY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.GRAND_BANQUET, List.of(FoodProperties.HOMESTYLE)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.MAGMA, List.of(FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.DEW_BOILED_EGGS, List.of(FoodProperties.MEAT, FoodProperties.AQUATIC_PRODUCTS, FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.UDUMBARA_CAKE, List.of(FoodProperties.MEAT, FoodProperties.AQUATIC_PRODUCTS)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.COLD_DISH_CARVING, List.of(FoodProperties.MEAT, FoodProperties.FIERY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.ARCTIC_SWEET_SHRIMP_AND_PEACH_SALAD, List.of(FoodProperties.MEAT, FoodProperties.SALTY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.POETRY_AND_GINKGO, List.of(FoodProperties.SALTY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.BUTTER_STEAK, List.of(FoodProperties.SWEET, FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.RISOTTO, List.of(FoodProperties.SWEET)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.BEEF_WELLINGTON, List.of(FoodProperties.SWEET, FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.EGGS_BENEDICT, List.of(FoodProperties.SWEET, FoodProperties.FRUITY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.MOONLIGHT_DUMPLINGS, List.of(FoodProperties.MEAT, FoodProperties.AQUATIC_PRODUCTS, FoodProperties.SALTY, FoodProperties.UMAMI)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.MOCHI, List.of(FoodProperties.MEAT, FoodProperties.AQUATIC_PRODUCTS, FoodProperties.SALTY, FoodProperties.UMAMI)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.WHITE_PEACH_EIGHT_BRIDGE, List.of(FoodProperties.MEAT, FoodProperties.AQUATIC_PRODUCTS, FoodProperties.SALTY, FoodProperties.UMAMI)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.MOON_LOVERS, List.of(FoodProperties.MEAT, FoodProperties.AQUATIC_PRODUCTS, FoodProperties.VEGETARIAN)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.FLOWING_WATER_NOODLES, List.of(FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.STINKY_TOFU, List.of(FoodProperties.SWEET, FoodProperties.FRUITY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.BOILED_FISH, List.of(FoodProperties.MEAT, FoodProperties.SWEET, FoodProperties.COOL, FoodProperties.FRUITY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.FRIED_SHRIMP_TEMPURA, List.of(FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.CRISP_CYCLONE, List.of(FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.LOOKING_UP_AT_THE_CEILING_FRUIT_PIE, List.of(FoodProperties.MEAT)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.LION_HEAD, List.of(FoodProperties.AQUATIC_PRODUCTS, FoodProperties.FILLING)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.GIANT_TAMAGOYAKI, List.of(FoodProperties.GOOD_WITH_ALCOHOL)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.SAKURA_PUDDING, List.of(FoodProperties.GREASY, FoodProperties.SALTY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.REFRESHING_PUDDING, List.of(FoodProperties.GREASY, FoodProperties.SALTY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.BURNT_PUDDING, List.of(FoodProperties.GREASY, FoodProperties.SALTY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.CAT_FOOD, List.of(FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.CHEESE_EGG, List.of(FoodProperties.MEAT)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.HELL_THRILL_WARNING, List.of(FoodProperties.VEGETARIAN, FoodProperties.LIGHT)));
        this.registerEntry(CraftingConflict.of(RDIngredientItems.SWEET_POTATO, List.of(FoodProperties.GOOD_WITH_ALCOHOL, FoodProperties.LIGHT)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.SKINNY_HORSE_DUMPLING, List.of(FoodProperties.MEAT)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.FRIGHT_ADVENTURE, List.of(FoodProperties.HOMESTYLE)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.BISCAY_BISCUITS, List.of(FoodProperties.SOUR)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.PIRATE_BACON, List.of(FoodProperties.SOUR)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.LUOHAN_VEGETARIAN, List.of(FoodProperties.MEAT, FoodProperties.SPICY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.YUNSHAN_COTTON_CANDY, List.of(FoodProperties.MEAT, FoodProperties.SALTY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.HOLY_WHITE_LOTUS_SEED_CAKE, List.of(FoodProperties.SALTY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.GENSOKYO_STAR_LOTUS_SHIP, List.of(FoodProperties.BIZARRE)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.SHIRAGA_SADAMATSU, List.of(FoodProperties.SOUR)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.CANDIED_SWEET_POTATO, List.of(FoodProperties.RAW)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.PAN_FRIED_MUSHROOM_MEAT_ROLL, List.of(FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.FRIED_TOMATO_STRIPS, List.of(FoodProperties.AQUATIC_PRODUCTS)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.DORAYAKI, List.of(FoodProperties.MEAT)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.THE_BEAUTY_OF_HAN_PALACE, List.of(FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.BAMBOO_TUBE_STEAMED_PORK, List.of(FoodProperties.RAW)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.GREEN_BAMBOO_WELCOMES_SPRING, List.of(FoodProperties.BIZARRE, FoodProperties.MUSHROOMS)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.STEAMED_EGG_WITH_SEA_URCHIN, List.of(FoodProperties.MOUNTAIN_DELICACY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.FANTASY_IS_ALL_THE_RAGE, List.of(FoodProperties.LIGHT, FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.FLOWERS_BIRDS_WIND_AND_MOON, List.of(FoodProperties.GREASY, FoodProperties.FIERY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.THE_DREAM, List.of(FoodProperties.MEAT, FoodProperties.AQUATIC_PRODUCTS, FoodProperties.GOOD_WITH_ALCOHOL)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.A_LITTLE_SWEET_POISON, List.of(FoodProperties.MEAT)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.BAMBOO_TUBE_ROASTED_DRUNKEN_SHRIMP, List.of(FoodProperties.FILLING)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.BEEF_HOT_POT, List.of(FoodProperties.COOL, FoodProperties.DREAMLIKE)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.CAT_KULULI, List.of(FoodProperties.COOL, FoodProperties.BIZARRE)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.CAT_PIZZA, List.of(FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.CATS_PLAYING_IN_WATER, List.of(FoodProperties.GOOD_WITH_ALCOHOL, FoodProperties.FIERY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.RAPUNZEL, List.of(FoodProperties.BIZARRE)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.SEA_URCHIN_SHINGEN_PANCAKE, List.of(FoodProperties.FILLING)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.MAD_HATTER_TEA_PARTY, List.of(FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.PEACH_BLOSSOM_GLAZE_ROLL, List.of(FoodProperties.MEAT, FoodProperties.FILLING, FoodProperties.BIZARRE, FoodProperties.MUSHROOMS)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.MOONLIGHT_OVER_LOTUS_POND, List.of(FoodProperties.MEAT, FoodProperties.GOOD_WITH_ALCOHOL, FoodProperties.FIERY, FoodProperties.BIZARRE)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.LONGYIN_PEACH, List.of(FoodProperties.MEAT, FoodProperties.OCEAN_FLAVOR, FoodProperties.RAW, FoodProperties.BIZARRE, FoodProperties.RAW)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.MOLECULAR_EGG, List.of(FoodProperties.MOUNTAIN_DELICACY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.THE_SOURCE_OF_LIFE, List.of(FoodProperties.MOUNTAIN_DELICACY)));
        this.registerEntry(CraftingConflict.of(RDFoodItems.THE_MARS, List.of(FoodProperties.MOUNTAIN_DELICACY)));
    }

    @Override
    public String getName() {
        return "CraftingConflict";
    }
}
