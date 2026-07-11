package cc.thonly.reverie_dreams.registry.content.item;

import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.impl.ItemDelegate;
import net.minecraft.util.Unit;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class RDFoodItems {
    public static final List<ItemDelegate> FOOD_ITEMS = new LinkedList<>();

    // 食物
    public static final ItemDelegate ALL_MEAT_FEAST = registerFoodItem("food/all_meat_feast", foodFactory(), new Item.Properties());
    public static final ItemDelegate ARCTIC_SWEET_SHRIMP_AND_PEACH_SALAD = registerFoodItem("food/arctic_sweet_shrimp_and_peach_salad", foodFactory(), new Item.Properties());
    public static final ItemDelegate ASSORTED_TEMPURA = registerFoodItem("food/assorted_tempura", foodFactory(), new Item.Properties());
    public static final ItemDelegate A_LITTLE_SWEET_POISON = registerFoodItem("food/a_little_sweet_poison", foodFactory(), new Item.Properties());
    public static final ItemDelegate BAKED_CRAB_WITH_CREAM = registerFoodItem("food/baked_crab_with_cream", foodFactory(), new Item.Properties());
    public static final ItemDelegate BAKED_SWEET_POTATOES = registerFoodItem("food/baked_sweet_potatoes", foodFactory(), new Item.Properties());
    public static final ItemDelegate BAMBOO_SHOOTS_FRIED_MEAT = registerFoodItem("food/bamboo_shoots_fried_meat", foodFactory(), new Item.Properties());
    public static final ItemDelegate BAMBOO_SHOOTS_STEWED_IN_STONE_POT = registerFoodItem("food/bamboo_shoots_stewed_in_stone_pot", foodFactory(), new Item.Properties());
    public static final ItemDelegate BAMBOO_STEAMED_EGG = registerFoodItem("food/bamboo_steamed_egg", foodFactory(), new Item.Properties());
    public static final ItemDelegate BAMBOO_TUBE_ROASTED_DRUNKEN_SHRIMP = registerFoodItem("food/bamboo_tube_roasted_drunken_shrimp", foodFactory(), new Item.Properties());
    public static final ItemDelegate BAMBOO_TUBE_STEAMED_PORK = registerFoodItem("food/bamboo_tube_steamed_pork", foodFactory(), new Item.Properties());
    public static final ItemDelegate BEAR_PAW = registerFoodItem("food/bear_paw", foodFactory(), new Item.Properties());
    public static final ItemDelegate BEEF_HOT_POT = registerFoodItem("food/beef_hot_pot", foodFactory(), new Item.Properties());
    public static final ItemDelegate BEEF_RICE = registerFoodItem("food/beef_rice", foodFactory(), new Item.Properties());
    public static final ItemDelegate BEEF_WELLINGTON = registerFoodItem("food/beef_wellington", foodFactory(), new Item.Properties());
    public static final ItemDelegate BEETLE_STEAMED_CAKE = registerFoodItem("food/beetle_steamed_cake", foodFactory(), new Item.Properties());
    public static final ItemDelegate BISCAY_BISCUITS = registerFoodItem("food/biscay_biscuits", foodFactory(), new Item.Properties());
    public static final ItemDelegate BOILED_FISH = registerFoodItem("food/boiled_fish", foodFactory(), new Item.Properties());
    public static final ItemDelegate BRAISED_EEL = registerFoodItem("food/braised_eel", foodFactory(), new Item.Properties());
    public static final ItemDelegate BRAISED_PORK_WITH_PEACH = registerFoodItem("food/braised_pork_with_peach", foodFactory(), new Item.Properties());
    public static final ItemDelegate BURNT_PUDDING = registerFoodItem("food/burnt_pudding", foodFactory(), new Item.Properties());
    public static final ItemDelegate BUTTER_STEAK = registerFoodItem("food/butter_steak", foodFactory(), new Item.Properties());
    public static final ItemDelegate CANDIED_CHESTNUTS = registerFoodItem("food/candied_chestnuts", foodFactory(), new Item.Properties());
    public static final ItemDelegate CANDIED_SWEET_POTATO = registerFoodItem("food/candied_sweet_potato", foodFactory(), new Item.Properties());
    public static final ItemDelegate CATS_PLAYING_IN_WATER = registerFoodItem("food/cats_playing_in_water", foodFactory(), new Item.Properties());
    public static final ItemDelegate CAT_FOOD = registerFoodItem("food/cat_food", foodFactory(), new Item.Properties());
    public static final ItemDelegate CAT_KULULI = registerFoodItem("food/cat_kululi", foodFactory(), new Item.Properties());
    public static final ItemDelegate CAT_PIZZA = registerFoodItem("food/cat_pizza", foodFactory(), new Item.Properties());
    public static final ItemDelegate CHEESE_EGG = registerFoodItem("food/cheese_egg", foodFactory(), new Item.Properties());
    public static final ItemDelegate COLD_DISH_CARVING = registerFoodItem("food/cold_dish_carving", foodFactory(), new Item.Properties());
    public static final ItemDelegate COLD_TOFU = registerFoodItem("food/cold_tofu", foodFactory(), new Item.Properties());
    public static final ItemDelegate COLORFUL_JADE_FRIED_BUNS = registerFoodItem("food/colorful_jade_fried_buns", foodFactory(), new Item.Properties());
    public static final ItemDelegate COOKING_TOFU = registerFoodItem("food/cooking_tofu", foodFactory(), new Item.Properties());
    public static final ItemDelegate CREAM_STEW = registerFoodItem("food/cream_stew", foodFactory(), new Item.Properties());
    public static final ItemDelegate CRISP_CYCLONE = registerFoodItem("food/crisp_cyclone", foodFactory(), new Item.Properties());
    public static final ItemDelegate DARK_CUISINE = registerFoodItem("food/dark_cuisine", foodFactory(), new Item.Properties());
    public static final ItemDelegate DEEP_FRIED_CICADA_SHELLS = registerFoodItem("food/deep_fried_cicada_shells", foodFactory(), new Item.Properties());
    public static final ItemDelegate DEPRESSED_CHEESE_STICKS = registerFoodItem("food/depressed_cheese_sticks", foodFactory(), new Item.Properties());
    public static final ItemDelegate DEW_BOILED_EGGS = registerFoodItem("food/dew_boiled_eggs", foodFactory(), new Item.Properties());
    public static final ItemDelegate DORAYAKI = registerFoodItem("food/dorayaki", foodFactory(), new Item.Properties());
    public static final ItemDelegate DUMPLING = registerFoodItem("food/dumpling", foodFactory(), new Item.Properties());
    public static final ItemDelegate EEL_EGG_DONBURI = registerFoodItem("food/eel_egg_donburi", foodFactory(), new Item.Properties());
    public static final ItemDelegate EGGS_BENEDICT = registerFoodItem("food/eggs_benedict", foodFactory(), new Item.Properties());
    public static final ItemDelegate ENERGY_STRING = registerFoodItem("food/energy_string", foodFactory(), new Item.Properties());
    public static final ItemDelegate FAILING_SAKURA_SNOW = registerFoodItem("food/failing_sakura_snow", foodFactory(), new Item.Properties());
    public static final ItemDelegate FANTASY_IS_ALL_THE_RAGE = registerFoodItem("food/fantasy_is_all_the_rage", foodFactory(), new Item.Properties());
    public static final ItemDelegate FISH_LEAPS_OVER_DRAGON_GATE = registerFoodItem("food/fish_leaps_over_dragon_gate", foodFactory(), new Item.Properties());
    public static final ItemDelegate FLOWERS_BIRDS_WIND_AND_MOON = registerFoodItem("food/flowers_birds_wind_and_moon", foodFactory(), new Item.Properties());
    public static final ItemDelegate FLOWING_WATER_NOODLES = registerFoodItem("food/flowing_water_noodles", foodFactory(), new Item.Properties());
    public static final ItemDelegate FRIED_HAGFISH = registerFoodItem("food/fried_hagfish", foodFactory(), new Item.Properties());
    public static final ItemDelegate FRIED_PORK_CUTLET = registerFoodItem("food/fried_pork_cutlet", foodFactory(), new Item.Properties());
    public static final ItemDelegate FRIED_PORK_SHREDS = registerFoodItem("food/fried_pork_shreds", foodFactory(), new Item.Properties());
    public static final ItemDelegate FRIED_SHRIMP_TEMPURA = registerFoodItem("food/fried_shrimp_tempura", foodFactory(), new Item.Properties());
    public static final ItemDelegate FRIED_TOFU = registerFoodItem("food/fried_tofu", foodFactory(), new Item.Properties());
    public static final ItemDelegate FRIED_TOMATO_STRIPS = registerFoodItem("food/fried_tomato_strips", foodFactory(), new Item.Properties());
    public static final ItemDelegate FRIGHT_ADVENTURE = registerFoodItem("food/fright_adventure", foodFactory(), new Item.Properties());
    public static final ItemDelegate GAME_SOUP = registerFoodItem("food/game_soup", foodFactory(), new Item.Properties());
    public static final ItemDelegate GENSOKYO_BUDDHA_JUMPS_OVER_THE_WALL = registerFoodItem("food/gensokyo_buddha_jumps_over_the_wall", foodFactory(), new Item.Properties());
    public static final ItemDelegate GENSOKYO_STAR_LOTUS_SHIP = registerFoodItem("food/gensokyo_star_lotus_ship", foodFactory(), new Item.Properties());
    public static final ItemDelegate GIANT_TAMAGOYAKI = registerFoodItem("food/giant_tamagoyaki", foodFactory(), new Item.Properties());
    public static final ItemDelegate GINKGO_AND_RADISH_PORK_RIB_SOUP = registerFoodItem("food/ginkgo_and_radish_pork_rib_soup", foodFactory(), new Item.Properties());
    public static final ItemDelegate GLOOMY_FRUIT_PIE = registerFoodItem("food/gloomy_fruit_pie", foodFactory(), new Item.Properties());
    public static final ItemDelegate GLUTINOUS_RICE_BALLS = registerFoodItem("food/glutinous_rice_balls", foodFactory(), new Item.Properties());
    public static final ItemDelegate GOLDEN_CRISPY_FISH_CAKE = registerFoodItem("food/golden_crispy_fish_cake", foodFactory(), new Item.Properties());
    public static final ItemDelegate GRAND_BANQUET = registerFoodItem("food/grand_banquet", foodFactory(), new Item.Properties());
    public static final ItemDelegate GREEN_BAMBOO_WELCOMES_SPRING = registerFoodItem("food/green_bamboo_welcomes_spring", foodFactory(), new Item.Properties());
    public static final ItemDelegate GREEN_FAIRY_MUSHROOM = registerFoodItem("food/green_fairy_mushroom", foodFactory(), new Item.Properties());
    public static final ItemDelegate GRILLED_HAGFISH = registerFoodItem("food/grilled_hagfish", foodFactory(), new Item.Properties());
    public static final ItemDelegate GRILLED_PORK_RICE_BALLS = registerFoodItem("food/grilled_pork_rice_balls", foodFactory(), new Item.Properties());
    public static final ItemDelegate HEART_PORRIDGE_GRUEL = registerFoodItem("food/heart_porridge_gruel", foodFactory(), new Item.Properties());
    public static final ItemDelegate HELL_THRILL_WARNING = registerFoodItem("food/hell_thrill_warning", foodFactory(), new Item.Properties());
    public static final ItemDelegate HOLY_WHITE_LOTUS_SEED_CAKE = registerFoodItem("food/holy_white_lotus_seed_cake", foodFactory(), new Item.Properties());
    public static final ItemDelegate HONEY_BBQ_PORK = registerFoodItem("food/honey_bbq_pork", foodFactory(), new Item.Properties());
    public static final ItemDelegate HORAI_DAMA_NO_EDA = registerFoodItem("food/horai-dama_no_eda", foodFactory(), new Item.Properties());
    public static final ItemDelegate HOT_WAFFLES = registerFoodItem("food/hot_waffles", foodFactory(), new Item.Properties());
    public static final ItemDelegate HULA_SOUP = registerFoodItem("food/hula_soup", foodFactory(), new Item.Properties());
    public static final ItemDelegate LION_HEAD = registerFoodItem("food/lion_head", foodFactory(), new Item.Properties());
    public static final ItemDelegate LONGYIN_PEACH = registerFoodItem("food/longyin_peach", foodFactory(), new Item.Properties());
    public static final ItemDelegate LOOKING_UP_AT_THE_CEILING_FRUIT_PIE = registerFoodItem("food/looking_up_at_the_ceiling_fruit_pie", foodFactory(), new Item.Properties());
    public static final ItemDelegate LOTUS_FISH_RICE_BOWL = registerFoodItem("food/lotus_fish_rice_bowl", foodFactory(), new Item.Properties());
    public static final ItemDelegate LUOHAN_VEGETARIAN = registerFoodItem("food/luohan_vegetarian", foodFactory(), new Item.Properties());
    public static final ItemDelegate MAD_HATTER_TEA_PARTY = registerFoodItem("food/mad_hatter_tea_party", foodFactory(), new Item.Properties());
    public static final ItemDelegate MAGMA = registerFoodItem("food/magma", foodFactory(), new Item.Properties());
    public static final ItemDelegate MAOYU_LAVA_TOFU = registerFoodItem("food/maoyu_lava_tofu", foodFactory(), new Item.Properties());
    public static final ItemDelegate MAOYU_TRICOLOR_ICE_CREAM = registerFoodItem("food/maoyu_tricolor_ice_cream", foodFactory(), new Item.Properties());
    public static final ItemDelegate MAPO_TOFU = registerFoodItem("food/mapo_tofu", foodFactory(), new Item.Properties());
    public static final ItemDelegate MILKY_MUSHROOM_SOUP = registerFoodItem("food/milky_mushroom_soup", foodFactory(), new Item.Properties());
    public static final ItemDelegate MOCHI = registerFoodItem("food/mochi", foodFactory(), new Item.Properties());
    public static final ItemDelegate MOLECULAR_EGG = registerFoodItem("food/molecular_egg", foodFactory(), new Item.Properties());
    public static final ItemDelegate MOONLIGHT_DUMPLINGS = registerFoodItem("food/moonlight_dumplings", foodFactory(), new Item.Properties());
    public static final ItemDelegate MOONLIGHT_OVER_LOTUS_POND = registerFoodItem("food/moonlight_over_lotus_pond", foodFactory(), new Item.Properties());
    public static final ItemDelegate MOON_CAKE = registerFoodItem("food/moon_cake", foodFactory(), new Item.Properties());
    public static final ItemDelegate MOON_LOVERS = registerFoodItem("food/moon_lovers", foodFactory(), new Item.Properties());
    public static final ItemDelegate MUSHROOM_GIRLS_DANCE_STEW = registerFoodItem("food/mushroom_girls_dance_stew", foodFactory(), new Item.Properties());
    public static final ItemDelegate MUSHROOM_MEAT_SLICES = registerFoodItem("food/mushroom_meat_slices", foodFactory(), new Item.Properties());
    public static final ItemDelegate NIGIRI_SUSHI = registerFoodItem("food/nigiri_sushi", foodFactory(), new Item.Properties());
    public static final ItemDelegate OEDO_BOAT_FESTIVAL = registerFoodItem("food/oedo_boat_festival", foodFactory(), new Item.Properties());
    public static final ItemDelegate OKONOMIYAKI = registerFoodItem("food/okonomiyaki", foodFactory(), new Item.Properties());
    public static final ItemDelegate ONE_HIT_KILL = registerFoodItem("food/one_hit_kill", foodFactory(), new Item.Properties());
    public static final ItemDelegate ORDINARY_SMALL_CAKE = registerFoodItem("food/ordinary_small_cake", foodFactory(), new Item.Properties());
    public static final ItemDelegate PAN_FRIED_MUSHROOM_MEAT_ROLL = registerFoodItem("food/pan_fried_mushroom_meat_roll", foodFactory(), new Item.Properties());
    public static final ItemDelegate PAN_FRIED_SALMON = registerFoodItem("food/pan_fried_salmon", foodFactory(), new Item.Properties());
    public static final ItemDelegate PEACH_BLOSSOM_GLAZE_ROLL = registerFoodItem("food/peach_blossom_glaze_roll", foodFactory(), new Item.Properties());
    public static final ItemDelegate PEACH_BLOSSOM_SOUP = registerFoodItem("food/peach_blossom_soup", foodFactory(), new Item.Properties());
    public static final ItemDelegate PHOENIX = registerFoodItem("food/phoenix", foodFactory(), new Item.Properties());
    public static final ItemDelegate PICKLED_CUCUMBERS = registerFoodItem("food/pickled_cucumbers", foodFactory(), new Item.Properties());
    public static final ItemDelegate PIG_DEER_BUTTERFLY = registerFoodItem("food/pig_deer_butterfly", foodFactory(), new Item.Properties());
    public static final ItemDelegate PINE_NUT_CAKE = registerFoodItem("food/pine_nut_cake", foodFactory(), new Item.Properties());
    public static final ItemDelegate PIRATE_BACON = registerFoodItem("food/pirate_bacon", foodFactory(), new Item.Properties());
    public static final ItemDelegate PLUM_TEA_RICE = registerFoodItem("food/plum_tea_rice", foodFactory(), new Item.Properties());
    public static final ItemDelegate POETRY_AND_GINKGO = registerFoodItem("food/poetry_and_ginkgo", foodFactory(), new Item.Properties());
    public static final ItemDelegate POISONOUS_GARDEN = registerFoodItem("food/poisonous_garden", foodFactory(), new Item.Properties());
    public static final ItemDelegate PORK_AND_TROUT_SMOKED = registerFoodItem("food/pork_and_trout_smoked", foodFactory(), new Item.Properties());
    public static final ItemDelegate PORK_RICE = registerFoodItem("food/pork_rice", foodFactory(), new Item.Properties());
    public static final ItemDelegate POTATO_CROQUETTES = registerFoodItem("food/potato_croquettes", foodFactory(), new Item.Properties());
    public static final ItemDelegate PSEUDO_JIRITAMA = registerFoodItem("food/pseudo_jiritama", foodFactory(), new Item.Properties());
    public static final ItemDelegate PUMPKIN_SHRIMP_CAKE = registerFoodItem("food/pumpkin_shrimp_cake", foodFactory(), new Item.Properties());
    public static final ItemDelegate RAPUNZEL = registerFoodItem("food/rapunzel", foodFactory(), new Item.Properties());
    public static final ItemDelegate REAL_SEAFOOD_MISO_SOUP = registerFoodItem("food/real_seafood_miso_soup", foodFactory(), new Item.Properties());
    public static final ItemDelegate RED_BEAN_DAIFUKU = registerFoodItem("food/red_bean_daifuku", foodFactory(), new Item.Properties());
    public static final ItemDelegate REFRESHING_PUDDING = registerFoodItem("food/refreshing_pudding", foodFactory(), new Item.Properties());
    public static final ItemDelegate REVERSING_THE_WORLD = registerFoodItem("food/reversing_the_world", foodFactory(), new Item.Properties());
    public static final ItemDelegate RICE_BALL = registerFoodItem("food/rice_ball", foodFactory(), new Item.Properties());
    public static final ItemDelegate RISOTTO = registerFoodItem("food/risotto", foodFactory(), new Item.Properties());
    public static final ItemDelegate ROASTED_MUSHROOMS = registerFoodItem("food/roasted_mushrooms", foodFactory(), new Item.Properties());
    public static final ItemDelegate SAKURA_PUDDING = registerFoodItem("food/sakura_pudding", foodFactory(), new Item.Properties());
    public static final ItemDelegate SALMON_TEMPURA = registerFoodItem("food/salmon_tempura", foodFactory(), new Item.Properties());
    public static final ItemDelegate SASHIMI_PLATTER = registerFoodItem("food/sashimi_platter", foodFactory(), new Item.Properties());
    public static final ItemDelegate SCARLET_DEVILS_CAKE = registerFoodItem("food/scarlet_devils_cake", foodFactory(), new Item.Properties());
    public static final ItemDelegate SCONES = registerFoodItem("food/scones", foodFactory(), new Item.Properties());
    public static final ItemDelegate SCREAMING_ODEN = registerFoodItem("food/screaming_oden", foodFactory(), new Item.Properties());
    public static final ItemDelegate SEAFOOD_MISO_SOUP = registerFoodItem("food/seafood_miso_soup", foodFactory(), new Item.Properties());
    public static final ItemDelegate SEA_URCHIN_SASHIMI = registerFoodItem("food/sea_urchin_sashimi", foodFactory(), new Item.Properties());
    public static final ItemDelegate SEA_URCHIN_SHINGEN_PANCAKE = registerFoodItem("food/sea_urchin_shingen_pancake", foodFactory(), new Item.Properties());
    public static final ItemDelegate SECRET_DRIED_FISH = registerFoodItem("food/secret_dried_fish", foodFactory(), new Item.Properties());
    public static final ItemDelegate SECRET_MUSHROOM_CASSEROLE = registerFoodItem("food/secret_mushroom_casserole", foodFactory(), new Item.Properties());
    public static final ItemDelegate SEVEN_COLORED_YOKAN = registerFoodItem("food/seven_colored_yokan", foodFactory(), new Item.Properties());
    public static final ItemDelegate SHIRAGA_SADAMATSU = registerFoodItem("food/shiraga_sadamatsu", foodFactory(), new Item.Properties());
    public static final ItemDelegate SKINNY_HORSE_DUMPLING = registerFoodItem("food/skinny_horse_dumpling", foodFactory(), new Item.Properties());
    public static final ItemDelegate SNOW_WHITE = registerFoodItem("food/snow_white", foodFactory(), new Item.Properties());
    public static final ItemDelegate STEAMED_EGG_WITH_SEA_URCHIN = registerFoodItem("food/steamed_egg_with_sea_urchin", foodFactory(), new Item.Properties());
    public static final ItemDelegate STINKY_TOFU = registerFoodItem("food/stinky_tofu", foodFactory(), new Item.Properties());
    public static final ItemDelegate STRENGTH_SOUP = registerFoodItem("food/strength_soup", foodFactory(), new Item.Properties());
    public static final ItemDelegate SUPERME_SEAFOOD_NOODLES = registerFoodItem("food/superme_seafood_noodles", foodFactory(), new Item.Properties());
    public static final ItemDelegate TAICHI_BAGUA_FISH_MAW = registerFoodItem("food/taichi_bagua_fish_maw", foodFactory(), new Item.Properties());
    public static final ItemDelegate TAKETORIHIME = registerFoodItem("food/taketorihime", foodFactory(), new Item.Properties());
    public static final ItemDelegate TAKOYAKI = registerFoodItem("food/takoyaki", foodFactory(), new Item.Properties());
    public static final ItemDelegate THE_BEAUTY_OF_HAN_PALACE = registerFoodItem("food/the_beauty_of_han_palace", foodFactory(), new Item.Properties());
    public static final ItemDelegate THE_DREAM = registerFoodItem("food/the_dream", foodFactory(), new Item.Properties());
    public static final ItemDelegate THE_MARS = registerFoodItem("food/the_mars", foodFactory(), new Item.Properties());
    public static final ItemDelegate THE_SOURCE_OF_LIFE = registerFoodItem("food/the_source_of_life", foodFactory(), new Item.Properties());
    public static final ItemDelegate TIANSHI_BRAISED_CHESTNUT_MUSHROOMS = registerFoodItem("food/tianshi_braised_chestnut_mushrooms", foodFactory(), new Item.Properties());
    public static final ItemDelegate TOFU_MISO = registerFoodItem("food/tofu_miso", foodFactory(), new Item.Properties());
    public static final ItemDelegate TOFU_POT = registerFoodItem("food/tofu_pot", foodFactory(), new Item.Properties());
    public static final ItemDelegate TONKOTSU_RAMEN = registerFoodItem("food/tonkotsu_ramen", foodFactory(), new Item.Properties());
    public static final ItemDelegate TOON_PANCAKES = registerFoodItem("food/toon_pancakes", foodFactory(), new Item.Properties());
    public static final ItemDelegate TWO_HEAVENS_ONE_STYLE = registerFoodItem("food/two_heavens_one_style", foodFactory(), new Item.Properties());
    public static final ItemDelegate UDUMBARA_CAKE = registerFoodItem("food/udumbara_cake", foodFactory(), new Item.Properties());
    public static final ItemDelegate UNCONSCIOUS_MONSTER_MOUSSE = registerFoodItem("food/unconscious_monster_mousse", foodFactory(), new Item.Properties());
    public static final ItemDelegate VEGETABLE_SPECIAL = registerFoodItem("food/vegetable_special", foodFactory(), new Item.Properties());
    public static final ItemDelegate WARM_RICE_BALL = registerFoodItem("food/warm_rice_ball", foodFactory(), new Item.Properties());
    public static final ItemDelegate WHITE_PEACH_EIGHT_BRIDGE = registerFoodItem("food/white_peach_eight_bridge", foodFactory(), new Item.Properties());
    public static final ItemDelegate YUNSHAN_COTTON_CANDY = registerFoodItem("food/yunshan_cotton_candy", foodFactory(), new Item.Properties());
    public static final ItemDelegate ZHAJI = registerFoodItem("food/zhaji", foodFactory(), new Item.Properties());

    public static void initialize() {

    }

    public static ItemDelegate registerFoodItem(String name, Function<Item.Properties, Item> factory, Item.Properties settings) {
        ItemDelegate itemDelegate = RDItems.registerSimpleItem(name, factory, settings);
        FOOD_ITEMS.add(itemDelegate);
        return itemDelegate;
    }

    public static Function<Item.Properties, Item> foodFactory() {
        return props -> new Item(props.component(RDDataComponents.FOOD_ITEM_TYPE.value(), Unit.INSTANCE)
                                      .food(new FoodProperties.Builder().nutrition(2).saturationModifier(2).build()));
    }


}
