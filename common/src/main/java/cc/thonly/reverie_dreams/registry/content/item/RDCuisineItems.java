package cc.thonly.reverie_dreams.registry.content.item;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.registry.AliasManager;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.delegate.ItemDelegate;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Unit;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

@SuppressWarnings("SpellCheckingInspection")
public class RDCuisineItems {
    public static final List<ItemDelegate> CUISINE_ITEMS = new LinkedList<>();

    // DLC0
    public static final ItemDelegate SEAFOOD_MISO_SOUP = registerFoodItem("seafood_miso_soup", 2, 1.4f);
    public static final ItemDelegate TOFU_MISO = registerFoodItem("tofu_miso", 4, 3.2f);
    public static final ItemDelegate STRENGTH_SOUP = registerFoodItem("strength_soup", 7, 7.6f);
    public static final ItemDelegate PORK_AND_TROUT_SMOKED = registerFoodItem("pork_and_trout_smoked", 8, 10.4f);
    public static final ItemDelegate GRILLED_HAGFISH = registerFoodItem("grilled_hagfish", 5, 6.0f);
    public static final ItemDelegate ENERGY_STRING = registerFoodItem("energy_string", 7, 8.8f);
    public static final ItemDelegate TWO_HEAVENS_ONE_STYLE = registerFoodItem("two_heavens_one_style", 9, 11.4f);
    public static final ItemDelegate RICE_BALL = registerFoodItem("rice_ball", 3, 2.4f);
    public static final ItemDelegate GRILLED_PORK_RICE_BALLS = registerFoodItem("grilled_pork_rice_balls", 6, 6.8f);
    public static final ItemDelegate WARM_RICE_BALL = registerFoodItem("warm_rice_ball", 6, 6.4f);
    public static final ItemDelegate FAILING_SAKURA_SNOW = registerFoodItem("failing_sakura_snow", 5, 5.6f);
    public static final ItemDelegate FRIED_PORK_SHREDS = registerFoodItem("fried_pork_shreds", 6, 7.2f);
    public static final ItemDelegate COLD_TOFU = registerFoodItem("cold_tofu", 3, 3.4f);
    public static final ItemDelegate BRAISED_EEL = registerFoodItem("braised_eel", 7, 8.6f);
    public static final ItemDelegate POTATO_CROQUETTES = registerFoodItem("potato_croquettes", 4, 4.8f);
    public static final ItemDelegate GAME_SOUP = registerFoodItem("game_soup", 9, 11.2f);
    public static final ItemDelegate PORK_RICE = registerFoodItem("pork_rice", 7, 8.0f);
    public static final ItemDelegate BEEF_RICE = registerFoodItem("beef_rice", 8, 9.6f);
    public static final ItemDelegate FRIED_HAGFISH = registerFoodItem("fried_hagfish", 6, 7.4f);
    public static final ItemDelegate VEGETABLE_SPECIAL = registerFoodItem("vegetable_special", 4, 3.8f);
    public static final ItemDelegate SNOW_WHITE = registerFoodItem("snow_white", 7, 8.0f);
    public static final ItemDelegate TOFU_POT = registerFoodItem("tofu_pot", 4, 4.8f);
    public static final ItemDelegate ZHAJI = registerFoodItem("zhaji", 7, 7.6f);
    public static final ItemDelegate SASHIMI_PLATTER = registerFoodItem("sashimi_platter", 7, 8.0f);
    public static final ItemDelegate GRAND_BANQUET = registerFoodItem("grand_banquet", 11, 15.0f);
    public static final ItemDelegate TONKOTSU_RAMEN = registerFoodItem("tonkotsu_ramen", 9, 11.0f);
    public static final ItemDelegate MAGMA = registerFoodItem("magma", 11, 14.2f);
    public static final ItemDelegate DEEP_FRIED_CICADA_SHELLS = registerFoodItem("deep_fried_cicada_shells", 3, 3.0f);
    public static final ItemDelegate DEW_BOILED_EGGS = registerFoodItem("dew_boiled_eggs", 3, 3.2f);
    public static final ItemDelegate UDUMBARA_CAKE = registerFoodItem("udumbara_cake", 3, 2.8f);
    public static final ItemDelegate BEAR_PAW = registerFoodItem("bear_paw", 9, 10.8f);
    public static final ItemDelegate SECRET_DRIED_FISH = registerFoodItem("secret_dried_fish", 4, 5.2f);
    public static final ItemDelegate COLD_DISH_CARVING = registerFoodItem("cold_dish_carving", 2, 1.2f);
    public static final ItemDelegate PEACH_BLOSSOM_SOUP = registerFoodItem("peach_blossom_soup", 3, 2.0f);
    public static final ItemDelegate ARCTIC_SWEET_SHRIMP_AND_PEACH_SALAD = registerFoodItem("arctic_sweet_shrimp_and_peach_salad", 5, 4.8f);
    public static final ItemDelegate FRIED_TOFU = registerFoodItem("fried_tofu", 5, 6.2f);
    public static final ItemDelegate POETRY_AND_GINKGO = registerFoodItem("poetry_and_ginkgo", 4, 4.2f);
    public static final ItemDelegate REAL_SEAFOOD_MISO_SOUP = registerFoodItem("real_seafood_miso_soup", 6, 6.8f);
    public static final ItemDelegate ROASTED_MUSHROOMS = registerFoodItem("roasted_mushrooms", 3, 2.6f);
    public static final ItemDelegate COOKING_TOFU = registerFoodItem("cooking_tofu", 4, 4.2f);
    public static final ItemDelegate FRIED_PORK_CUTLET = registerFoodItem("fried_pork_cutlet", 7, 9.0f);
    public static final ItemDelegate BUTTER_STEAK = registerFoodItem("butter_steak", 8, 12.4f);
    public static final ItemDelegate RISOTTO = registerFoodItem("risotto", 8, 9.6f);
    public static final ItemDelegate BEEF_WELLINGTON = registerFoodItem("beef_wellington", 12, 16.0f);
    public static final ItemDelegate EGGS_BENEDICT = registerFoodItem("eggs_benedict", 7, 8.4f);
    public static final ItemDelegate HOT_WAFFLES = registerFoodItem("hot_waffles", 6, 5.4f);
    public static final ItemDelegate SCONES = registerFoodItem("scones", 4, 3.6f);
    public static final ItemDelegate PAN_FRIED_SALMON = registerFoodItem("pan_fried_salmon", 7, 9.2f);
    public static final ItemDelegate CREAM_STEW = registerFoodItem("cream_stew", 6, 7.2f);
    public static final ItemDelegate HONEY_BBQ_PORK = registerFoodItem("honey_bbq_pork", 7, 9.2f);
    public static final ItemDelegate GINKGO_AND_RADISH_PORK_RIB_SOUP = registerFoodItem("ginkgo_and_radish_pork_rib_soup", 7, 8.6f);
    public static final ItemDelegate TAKETORIHIME = registerFoodItem("taketorihime", 11, 14.0f);
    public static final ItemDelegate PHOENIX = registerFoodItem("phoenix", 10, 11.8f);
    public static final ItemDelegate MOONLIGHT_DUMPLINGS = registerFoodItem("moonlight_dumplings", 4, 3.6f);
    public static final ItemDelegate MOCHI = registerFoodItem("mochi", 3, 2.6f);
    public static final ItemDelegate WHITE_PEACH_EIGHT_BRIDGE = registerFoodItem("white_peach_eight_bridge", 4, 3.2f);
    public static final ItemDelegate MOON_LOVERS = registerFoodItem("moon_lovers", 4, 3.4f);
    public static final ItemDelegate PIG_DEER_BUTTERFLY = registerFoodItem("pig_deer_butterfly", 9, 11.4f);
    public static final ItemDelegate FLOWING_WATER_NOODLES = registerFoodItem("flowing_water_noodles", 4, 3.6f);
    public static final ItemDelegate BAMBOO_SHOOTS_FRIED_MEAT = registerFoodItem("bamboo_shoots_fried_meat", 7, 8.8f);
    public static final ItemDelegate BAMBOO_STEAMED_EGG = registerFoodItem("bamboo_steamed_egg", 5, 5.8f);
    public static final ItemDelegate HORAI_DAMA_NO_EDA = registerFoodItem("horai-dama_no_eda", 11, 14.2f);
    public static final ItemDelegate STINKY_TOFU = registerFoodItem("stinky_tofu", 4, 4.8f);
    public static final ItemDelegate COLORFUL_JADE_FRIED_BUNS = registerFoodItem("colorful_jade_fried_buns", 7, 8.2f);
    public static final ItemDelegate MAPO_TOFU = registerFoodItem("mapo_tofu", 7, 8.4f);
    public static final ItemDelegate BOILED_FISH = registerFoodItem("boiled_fish", 6, 7.6f);
    public static final ItemDelegate MOON_CAKE = registerFoodItem("moon_cake", 5, 5.6f);
    public static final ItemDelegate MAOYU_TRICOLOR_ICE_CREAM = registerFoodItem("maoyu_tricolor_ice_cream", 4, 3.6f);
    public static final ItemDelegate MAOYU_LAVA_TOFU = registerFoodItem("maoyu_lava_tofu", 8, 9.4f);
    public static final ItemDelegate SCARLET_DEVILS_CAKE = registerFoodItem("scarlet_devils_cake", 6, 5.4f);
    public static final ItemDelegate UNCONSCIOUS_MONSTER_MOUSSE = registerFoodItem("unconscious_monster_mousse", 5, 4.6f);
    public static final ItemDelegate DUMPLING = registerFoodItem("dumpling", 4, 4.2f);
    public static final ItemDelegate GLUTINOUS_RICE_BALLS = registerFoodItem("glutinous_rice_balls", 4, 3.6f);

    // DLC1
    public static final ItemDelegate FRIED_SHRIMP_TEMPURA = registerFoodItem("fried_shrimp_tempura", 5, 6.4f);
    public static final ItemDelegate GOLDEN_CRISPY_FISH_CAKE = registerFoodItem("golden_crispy_fish_cake", 6, 7.6f);
    public static final ItemDelegate ALL_MEAT_FEAST = registerFoodItem("all_meat_feast", 12, 16.0f);
    public static final ItemDelegate PICKLED_CUCUMBERS = registerFoodItem("pickled_cucumbers", 2, 1.2f);
    public static final ItemDelegate BAKED_CRAB_WITH_CREAM = registerFoodItem("baked_crab_with_cream", 7, 9.4f);
    public static final ItemDelegate PSEUDO_JIRITAMA = registerFoodItem("pseudo_jiritama", 4, 5.4f);
    public static final ItemDelegate OKONOMIYAKI = registerFoodItem("okonomiyaki", 6, 6.2f);
    public static final ItemDelegate TAKOYAKI = registerFoodItem("takoyaki", 6, 6.8f);
    public static final ItemDelegate SEA_URCHIN_SASHIMI = registerFoodItem("sea_urchin_sashimi", 3, 3.4f);
    public static final ItemDelegate MUSHROOM_MEAT_SLICES = registerFoodItem("mushroom_meat_slices", 7, 8.4f);
    public static final ItemDelegate SECRET_MUSHROOM_CASSEROLE = registerFoodItem("secret_mushroom_casserole", 6, 6.6f);
    public static final ItemDelegate MUSHROOM_GIRLS_DANCE_STEW = registerFoodItem("mushroom_girls_dance_stew", 8, 10.0f);
    public static final ItemDelegate MILKY_MUSHROOM_SOUP = registerFoodItem("milky_mushroom_soup", 6, 7.2f);
    public static final ItemDelegate ORDINARY_SMALL_CAKE = registerFoodItem("ordinary_small_cake", 5, 4.6f);
    public static final ItemDelegate SEVEN_COLORED_YOKAN = registerFoodItem("seven_colored_yokan", 4, 3.2f);
    public static final ItemDelegate NIGIRI_SUSHI = registerFoodItem("nigiri_sushi", 7, 8.4f);
    public static final ItemDelegate PUMPKIN_SHRIMP_CAKE = registerFoodItem("pumpkin_shrimp_cake", 8, 8.6f);
    public static final ItemDelegate GENSOKYO_BUDDHA_JUMPS_OVER_THE_WALL = registerFoodItem("gensokyo_buddha_jumps_over_the_wall", 12, 16.0f);

    // DLC2
    public static final ItemDelegate DEPRESSED_CHEESE_STICKS = registerFoodItem("depressed_cheese_sticks", 4, 4.8f);
    public static final ItemDelegate GLOOMY_FRUIT_PIE = registerFoodItem("gloomy_fruit_pie", 5, 4.8f);
    public static final ItemDelegate SCREAMING_ODEN = registerFoodItem("screaming_oden", 9, 10.8f);
    public static final ItemDelegate CRISP_CYCLONE = registerFoodItem("crisp_cyclone", 4, 3.6f);
    public static final ItemDelegate LOOKING_UP_AT_THE_CEILING_FRUIT_PIE = registerFoodItem("looking_up_at_the_ceiling_fruit_pie", 6, 6.8f);
    public static final ItemDelegate BEETLE_STEAMED_CAKE = registerFoodItem("beetle_steamed_cake", 7, 7.8f);
    public static final ItemDelegate LION_HEAD = registerFoodItem("lion_head", 6, 7.6f);
    public static final ItemDelegate GIANT_TAMAGOYAKI = registerFoodItem("giant_tamagoyaki", 8, 8.4f);
    public static final ItemDelegate OEDO_BOAT_FESTIVAL = registerFoodItem("oedo_boat_festival", 11, 13.4f);
    public static final ItemDelegate SAKURA_PUDDING = registerFoodItem("sakura_pudding", 3, 2.4f);
    public static final ItemDelegate REFRESHING_PUDDING = registerFoodItem("refreshing_pudding", 2, 1.6f);
    public static final ItemDelegate BURNT_PUDDING = registerFoodItem("burnt_pudding", 3, 2.4f);
    public static final ItemDelegate CAT_FOOD = registerFoodItem("cat_food", 5, 5.4f);
    public static final ItemDelegate SALMON_TEMPURA = registerFoodItem("salmon_tempura", 7, 8.8f);
    public static final ItemDelegate FISH_LEAPS_OVER_DRAGON_GATE = registerFoodItem("fish_leaps_over_dragon_gate", 11, 14.4f);
    public static final ItemDelegate CHEESE_EGG = registerFoodItem("cheese_egg", 6, 7.4f);
    public static final ItemDelegate ONE_HIT_KILL = registerFoodItem("one_hit_kill", 9, 11.4f);
    public static final ItemDelegate HELL_THRILL_WARNING = registerFoodItem("hell_thrill_warning", 9, 10.8f);

    // DLC3
    public static final ItemDelegate BAKED_SWEET_POTATOES = registerFoodItem("baked_sweet_potatoes", 5, 6.0f);
    public static final ItemDelegate SKINNY_HORSE_DUMPLING = registerFoodItem("skinny_horse_dumpling", 4, 4.2f);
    public static final ItemDelegate FRIGHT_ADVENTURE = registerFoodItem("fright_adventure", 5, 5.0f);
    public static final ItemDelegate BISCAY_BISCUITS = registerFoodItem("biscay_biscuits", 5, 5.8f);
    public static final ItemDelegate PIRATE_BACON = registerFoodItem("pirate_bacon", 8, 10.8f);
    public static final ItemDelegate LUOHAN_VEGETARIAN = registerFoodItem("luohan_vegetarian", 9, 10.8f);
    public static final ItemDelegate YUNSHAN_COTTON_CANDY = registerFoodItem("yunshan_cotton_candy", 2, 0.8f);
    public static final ItemDelegate HOLY_WHITE_LOTUS_SEED_CAKE = registerFoodItem("holy_white_lotus_seed_cake", 5, 5.8f);
    public static final ItemDelegate GENSOKYO_STAR_LOTUS_SHIP = registerFoodItem("gensokyo_star_lotus_ship", 10, 11.8f);
    public static final ItemDelegate PINE_NUT_CAKE = registerFoodItem("pine_nut_cake", 5, 5.8f);
    public static final ItemDelegate SHIRAGA_SADAMATSU = registerFoodItem("shiraga_sadamatsu", 8, 10.2f);
    public static final ItemDelegate TAICHI_BAGUA_FISH_MAW = registerFoodItem("taichi_bagua_fish_maw", 9, 10.8f);
    public static final ItemDelegate CANDIED_CHESTNUTS = registerFoodItem("candied_chestnuts", 4, 3.6f);
    public static final ItemDelegate TIANSHI_BRAISED_CHESTNUT_MUSHROOMS = registerFoodItem("tianshi_braised_chestnut_mushrooms", 7, 7.8f);
    public static final ItemDelegate LOTUS_FISH_RICE_BOWL = registerFoodItem("lotus_fish_rice_bowl", 8, 9.8f);
    public static final ItemDelegate CANDIED_SWEET_POTATO = registerFoodItem("candied_sweet_potato", 5, 5.6f);
    public static final ItemDelegate PAN_FRIED_MUSHROOM_MEAT_ROLL = registerFoodItem("pan_fried_mushroom_meat_roll", 8, 10.0f);
    public static final ItemDelegate ASSORTED_TEMPURA = registerFoodItem("assorted_tempura", 9, 11.8f);

    // DLC4
    public static final ItemDelegate FRIED_TOMATO_STRIPS = registerFoodItem("fried_tomato_strips", 4, 4.0f);
    public static final ItemDelegate BRAISED_PORK_WITH_PEACH = registerFoodItem("braised_pork_with_peach", 8, 10.2f);
    public static final ItemDelegate REVERSING_THE_WORLD = registerFoodItem("reversing_the_world", 9, 10.8f);
    public static final ItemDelegate RED_BEAN_DAIFUKU = registerFoodItem("red_bean_daifuku", 4, 4.0f);
    public static final ItemDelegate DORAYAKI = registerFoodItem("dorayaki", 5, 4.6f);
    public static final ItemDelegate THE_BEAUTY_OF_HAN_PALACE = registerFoodItem("the_beauty_of_han_palace", 9, 10.8f);
    public static final ItemDelegate BAMBOO_SHOOTS_STEWED_IN_STONE_POT = registerFoodItem("bamboo_shoots_stewed_in_stone_pot", 8, 9.6f);
    public static final ItemDelegate BAMBOO_TUBE_STEAMED_PORK = registerFoodItem("bamboo_tube_steamed_pork", 8, 10.2f);
    public static final ItemDelegate GREEN_BAMBOO_WELCOMES_SPRING = registerFoodItem("green_bamboo_welcomes_spring", 9, 10.6f);
    public static final ItemDelegate PLUM_TEA_RICE = registerFoodItem("plum_tea_rice", 5, 4.4f);
    public static final ItemDelegate STEAMED_EGG_WITH_SEA_URCHIN = registerFoodItem("steamed_egg_with_sea_urchin", 5, 6.0f);
    public static final ItemDelegate FANTASY_IS_ALL_THE_RAGE = registerFoodItem("fantasy_is_all_the_rage", 11, 14.0f);
    public static final ItemDelegate GREEN_FAIRY_MUSHROOM = registerFoodItem("green_fairy_mushroom", 3, 2.6f);
    public static final ItemDelegate FLOWERS_BIRDS_WIND_AND_MOON = registerFoodItem("flowers_birds_wind_and_moon", 5, 4.8f);
    public static final ItemDelegate THE_DREAM = registerFoodItem("the_dream", 7, 6.4f);
    public static final ItemDelegate TOON_PANCAKES = registerFoodItem("toon_pancakes", 5, 5.2f);
    public static final ItemDelegate POISONOUS_GARDEN = registerFoodItem("poisonous_garden", 5, 4.0f);
    public static final ItemDelegate A_LITTLE_SWEET_POISON = registerFoodItem("a_little_sweet_poison", 4, 3.6f);

    // DLC5
    public static final ItemDelegate EEL_EGG_DONBURI = registerFoodItem("eel_egg_donburi", 8, 9.6f);
    public static final ItemDelegate BAMBOO_TUBE_ROASTED_DRUNKEN_SHRIMP = registerFoodItem("bamboo_tube_roasted_drunken_shrimp", 5, 5.4f);
    public static final ItemDelegate BEEF_HOT_POT = registerFoodItem("beef_hot_pot", 12, 15.6f);
    public static final ItemDelegate CAT_KULULI = registerFoodItem("cat_kululi", 4, 4.0f);
    public static final ItemDelegate CAT_PIZZA = registerFoodItem("cat_pizza", 9, 11.0f);
    public static final ItemDelegate CATS_PLAYING_IN_WATER = registerFoodItem("cats_playing_in_water", 8, 8.4f);
    public static final ItemDelegate RAPUNZEL = registerFoodItem("rapunzel", 7, 7.6f);
    public static final ItemDelegate SEA_URCHIN_SHINGEN_PANCAKE = registerFoodItem("sea_urchin_shingen_pancake", 4, 4.0f);
    public static final ItemDelegate MAD_HATTER_TEA_PARTY = registerFoodItem("mad_hatter_tea_party", 8, 8.0f);
    public static final ItemDelegate PEACH_BLOSSOM_GLAZE_ROLL = registerFoodItem("peach_blossom_glaze_roll", 4, 3.8f);
    public static final ItemDelegate MOONLIGHT_OVER_LOTUS_POND = registerFoodItem("moonlight_over_lotus_pond", 4, 3.4f);
    public static final ItemDelegate LONGYIN_PEACH = registerFoodItem("longyin_peach", 4, 3.2f);
    public static final ItemDelegate MOLECULAR_EGG = registerFoodItem("molecular_egg", 4, 3.8f);
    public static final ItemDelegate THE_SOURCE_OF_LIFE = registerFoodItem("the_source_of_life", 4, 3.6f);
    public static final ItemDelegate THE_MARS = registerFoodItem("the_mars", 5, 5.2f);
    public static final ItemDelegate HEART_PORRIDGE_GRUEL = registerFoodItem("heart_porridge_gruel", 5, 5.6f);
    public static final ItemDelegate HULA_SOUP = registerFoodItem("hula_soup", 7, 7.8f);
    public static final ItemDelegate SUPERME_SEAFOOD_NOODLES = registerFoodItem("superme_seafood_noodles", 12, 15.6f);

    // 未出现在 FOOD_LIST DLC 顺序中的项目
    public static final ItemDelegate DARK_CUISINE = registerFoodItem("dark_cuisine", 1, 0.0f);
    public static void initialize() {

    }

    public static ItemDelegate registerFoodItem(String name, Function<Item.Properties, Item> factory, Item.Properties settings) {
        String prefixName = "cuisine/" + name;
        String oldName = "food/" + name;
        ItemDelegate itemDelegate = RDItems.registerSimpleItem(prefixName, factory, settings);
        CUISINE_ITEMS.add(itemDelegate);
        AliasManager.register(Registries.ITEM, ReverieDreams.id(oldName), ReverieDreams.id(prefixName));
        return itemDelegate;
    }

    private static ItemDelegate registerFoodItem(String name, int nutrition, float saturation) {
        return registerFoodItem(name, foodFactory(nutrition, saturation), new Item.Properties());
    }

    /** Saturation is the actual amount restored, not the builder's multiplier. */
    public static Function<Item.Properties, Item> foodFactory(int nutrition, float saturation) {
        return props -> new Item(props.component(RDDataComponentTypes.FOOD_ITEM_TYPE.value(), Unit.INSTANCE)
                .food(new FoodProperties(nutrition, saturation, false)));
    }


}
