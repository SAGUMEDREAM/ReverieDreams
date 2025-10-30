package cc.thonly.mystias_izakaya.datagen;

import cc.thonly.mystias_izakaya.component.CraftingConflict;
import cc.thonly.mystias_izakaya.datagen.generator.CraftingConflictProvider;
import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.mystias_izakaya.registry.FoodProperties;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MICraftingConflictProvider extends CraftingConflictProvider {

    public MICraftingConflictProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    @Override
    protected void configured() {
        this.registerEntry(CraftingConflict.of(MIItems.SEAFOOD_MISO_SOUP, List.of(FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(MIItems.TOFU_MISO, List.of(FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(MIItems.STRENGTH_SOUP, List.of(FoodProperties.PETITE, FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(MIItems.GRILLED_HAGFISH, List.of(FoodProperties.MEAT, FoodProperties.VEGETARIAN)));
        this.registerEntry(CraftingConflict.of(MIItems.TWO_HEAVENS_ONE_STYLE, List.of(FoodProperties.HOMESTYLE)));
        this.registerEntry(CraftingConflict.of(MIItems.FAILING_SAKURA_SNOW, List.of(FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(MIItems.POTATO_CROQUETTES, List.of(FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(MIItems.FRIED_HAGFISH, List.of(FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(MIItems.VEGETABLE_SPECIAL, List.of(FoodProperties.MEAT, FoodProperties.AQUATIC_PRODUCTS, FoodProperties.FIERY)));
        this.registerEntry(CraftingConflict.of(MIItems.SASHIMI_PLATTER, List.of(FoodProperties.FIERY)));
        this.registerEntry(CraftingConflict.of(MIItems.GRAND_BANQUET, List.of(FoodProperties.HOMESTYLE)));
        this.registerEntry(CraftingConflict.of(MIItems.MAGMA, List.of(FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(MIItems.DEW_BOILED_EGGS, List.of(FoodProperties.MEAT, FoodProperties.AQUATIC_PRODUCTS, FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(MIItems.UDUMBARA_CAKE, List.of(FoodProperties.MEAT, FoodProperties.AQUATIC_PRODUCTS)));
        this.registerEntry(CraftingConflict.of(MIItems.COLD_DISH_CARVING, List.of(FoodProperties.MEAT, FoodProperties.FIERY)));
        this.registerEntry(CraftingConflict.of(MIItems.ARCTIC_SWEET_SHRIMP_AND_PEACH_SALAD, List.of(FoodProperties.MEAT, FoodProperties.SALTY)));
        this.registerEntry(CraftingConflict.of(MIItems.POETRY_AND_GINKGO, List.of(FoodProperties.SALTY)));
        this.registerEntry(CraftingConflict.of(MIItems.BUTTER_STEAK, List.of(FoodProperties.SWEET, FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(MIItems.RISOTTO, List.of(FoodProperties.SWEET)));
        this.registerEntry(CraftingConflict.of(MIItems.BEEF_WELLINGTON, List.of(FoodProperties.SWEET, FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(MIItems.EGGS_BENEDICT, List.of(FoodProperties.SWEET, FoodProperties.FRUITY)));
        this.registerEntry(CraftingConflict.of(MIItems.MOONLIGHT_DUMPLINGS, List.of(FoodProperties.MEAT, FoodProperties.AQUATIC_PRODUCTS, FoodProperties.SALTY, FoodProperties.UMAMI)));
        this.registerEntry(CraftingConflict.of(MIItems.MOCHI, List.of(FoodProperties.MEAT, FoodProperties.AQUATIC_PRODUCTS, FoodProperties.SALTY, FoodProperties.UMAMI)));
        this.registerEntry(CraftingConflict.of(MIItems.WHITE_PEACH_EIGHT_BRIDGE, List.of(FoodProperties.MEAT, FoodProperties.AQUATIC_PRODUCTS, FoodProperties.SALTY, FoodProperties.UMAMI)));
        this.registerEntry(CraftingConflict.of(MIItems.MOON_LOVERS, List.of(FoodProperties.MEAT, FoodProperties.AQUATIC_PRODUCTS, FoodProperties.VEGETARIAN)));
        this.registerEntry(CraftingConflict.of(MIItems.FLOWING_WATER_NOODLES, List.of(FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(MIItems.STINKY_TOFU, List.of(FoodProperties.SWEET, FoodProperties.FRUITY)));
        this.registerEntry(CraftingConflict.of(MIItems.BOILED_FISH, List.of(FoodProperties.MEAT, FoodProperties.SWEET, FoodProperties.COOL, FoodProperties.FRUITY)));
        this.registerEntry(CraftingConflict.of(MIItems.FRIED_SHRIMP_TEMPURA, List.of(FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(MIItems.CRISP_CYCLONE, List.of(FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(MIItems.LOOKING_UP_AT_THE_CEILING_FRUIT_PIE, List.of(FoodProperties.MEAT)));
        this.registerEntry(CraftingConflict.of(MIItems.LION_HEAD, List.of(FoodProperties.AQUATIC_PRODUCTS, FoodProperties.FILLING)));
        this.registerEntry(CraftingConflict.of(MIItems.GIANT_TAMAGOYAKI, List.of(FoodProperties.GOOD_WITH_ALCOHOL)));
        this.registerEntry(CraftingConflict.of(MIItems.SAKURA_PUDDING, List.of(FoodProperties.GREASY, FoodProperties.SALTY)));
        this.registerEntry(CraftingConflict.of(MIItems.REFRESHING_PUDDING, List.of(FoodProperties.GREASY, FoodProperties.SALTY)));
        this.registerEntry(CraftingConflict.of(MIItems.BURNT_PUDDING, List.of(FoodProperties.GREASY, FoodProperties.SALTY)));
        this.registerEntry(CraftingConflict.of(MIItems.CAT_FOOD, List.of(FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(MIItems.CHEESE_EGG, List.of(FoodProperties.MEAT)));
        this.registerEntry(CraftingConflict.of(MIItems.HELL_THRILL_WARNING, List.of(FoodProperties.VEGETARIAN, FoodProperties.LIGHT)));
        this.registerEntry(CraftingConflict.of(MIItems.SWEET_POTATO, List.of(FoodProperties.GOOD_WITH_ALCOHOL, FoodProperties.LIGHT)));
        this.registerEntry(CraftingConflict.of(MIItems.SKINNY_HORSE_DUMPLING, List.of(FoodProperties.MEAT)));
        this.registerEntry(CraftingConflict.of(MIItems.FRIGHT_ADVENTURE, List.of(FoodProperties.HOMESTYLE)));
        this.registerEntry(CraftingConflict.of(MIItems.BISCAY_BISCUITS, List.of(FoodProperties.SOUR)));
        this.registerEntry(CraftingConflict.of(MIItems.PIRATE_BACON, List.of(FoodProperties.SOUR)));
        this.registerEntry(CraftingConflict.of(MIItems.LUOHAN_VEGETARIAN, List.of(FoodProperties.MEAT, FoodProperties.SPICY)));
        this.registerEntry(CraftingConflict.of(MIItems.YUNSHAN_COTTON_CANDY, List.of(FoodProperties.MEAT, FoodProperties.SALTY)));
        this.registerEntry(CraftingConflict.of(MIItems.HOLY_WHITE_LOTUS_SEED_CAKE, List.of(FoodProperties.SALTY)));
        this.registerEntry(CraftingConflict.of(MIItems.GENSOKYO_STAR_LOTUS_SHIP, List.of(FoodProperties.BIZARRE)));
        this.registerEntry(CraftingConflict.of(MIItems.SHIRAGA_SADAMATSU, List.of(FoodProperties.SOUR)));
        this.registerEntry(CraftingConflict.of(MIItems.CANDIED_SWEET_POTATO, List.of(FoodProperties.RAW)));
        this.registerEntry(CraftingConflict.of(MIItems.PAN_FRIED_MUSHROOM_MEAT_ROLL, List.of(FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(MIItems.FRIED_TOMATO_STRIPS, List.of(FoodProperties.AQUATIC_PRODUCTS)));
        this.registerEntry(CraftingConflict.of(MIItems.DORAYAKI, List.of(FoodProperties.MEAT)));
        this.registerEntry(CraftingConflict.of(MIItems.THE_BEAUTY_OF_HAN_PALACE, List.of(FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(MIItems.BAMBOO_TUBE_STEAMED_PORK, List.of(FoodProperties.RAW)));
        this.registerEntry(CraftingConflict.of(MIItems.GREEN_BAMBOO_WELCOMES_SPRING, List.of(FoodProperties.BIZARRE, FoodProperties.MUSHROOMS)));
        this.registerEntry(CraftingConflict.of(MIItems.STEAMED_EGG_WITH_SEA_URCHIN, List.of(FoodProperties.MOUNTAIN_DELICACY)));
        this.registerEntry(CraftingConflict.of(MIItems.FANTASY_IS_ALL_THE_RAGE, List.of(FoodProperties.LIGHT, FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(MIItems.FLOWERS_BIRDS_WIND_AND_MOON, List.of(FoodProperties.GREASY, FoodProperties.FIERY)));
        this.registerEntry(CraftingConflict.of(MIItems.THE_DREAM, List.of(FoodProperties.MEAT, FoodProperties.AQUATIC_PRODUCTS, FoodProperties.GOOD_WITH_ALCOHOL)));
        this.registerEntry(CraftingConflict.of(MIItems.A_LITTLE_SWEET_POISON, List.of(FoodProperties.MEAT)));
        this.registerEntry(CraftingConflict.of(MIItems.BAMBOO_TUBE_ROASTED_DRUNKEN_SHRIMP, List.of(FoodProperties.FILLING)));
        this.registerEntry(CraftingConflict.of(MIItems.BEEF_HOT_POT, List.of(FoodProperties.COOL, FoodProperties.DREAMLIKE)));
        this.registerEntry(CraftingConflict.of(MIItems.CAT_KULULI, List.of(FoodProperties.COOL, FoodProperties.BIZARRE)));
        this.registerEntry(CraftingConflict.of(MIItems.CAT_PIZZA, List.of(FoodProperties.COOL)));
        this.registerEntry(CraftingConflict.of(MIItems.CATS_PLAYING_IN_WATER, List.of(FoodProperties.GOOD_WITH_ALCOHOL, FoodProperties.FIERY)));
        this.registerEntry(CraftingConflict.of(MIItems.RAPUNZEL, List.of(FoodProperties.BIZARRE)));
        this.registerEntry(CraftingConflict.of(MIItems.SEA_URCHIN_SHINGEN_PANCAKE, List.of(FoodProperties.FILLING)));
        this.registerEntry(CraftingConflict.of(MIItems.MAD_HATTER_TEA_PARTY, List.of(FoodProperties.GREASY)));
        this.registerEntry(CraftingConflict.of(MIItems.PEACH_BLOSSOM_GLAZE_ROLL, List.of(FoodProperties.MEAT, FoodProperties.FILLING, FoodProperties.BIZARRE, FoodProperties.MUSHROOMS)));
        this.registerEntry(CraftingConflict.of(MIItems.MOONLIGHT_OVER_LOTUS_POND, List.of(FoodProperties.MEAT, FoodProperties.GOOD_WITH_ALCOHOL, FoodProperties.FIERY, FoodProperties.BIZARRE)));
        this.registerEntry(CraftingConflict.of(MIItems.LONGYIN_PEACH, List.of(FoodProperties.MEAT, FoodProperties.OCEAN_FLAVOR, FoodProperties.RAW, FoodProperties.BIZARRE, FoodProperties.RAW)));
        this.registerEntry(CraftingConflict.of(MIItems.MOLECULAR_EGG, List.of(FoodProperties.MOUNTAIN_DELICACY)));
        this.registerEntry(CraftingConflict.of(MIItems.THE_SOURCE_OF_LIFE, List.of(FoodProperties.MOUNTAIN_DELICACY)));
        this.registerEntry(CraftingConflict.of(MIItems.THE_MARS, List.of(FoodProperties.MOUNTAIN_DELICACY)));
    }

    @Override
    public String getName() {
        return "CraftingConflict";
    }
}
