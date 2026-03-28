package cc.thonly.reverie_dreams.registry.content.item;

import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class RDFoodItems {

    public static final List<Item> FOOD_ITEMS = new LinkedList<>();

    // 食物
    public static final Item ALL_MEAT_FEAST = registerFoodItem("food/all_meat_feast", Item::new, new Item.Properties());
    public static final Item ARCTIC_SWEET_SHRIMP_AND_PEACH_SALAD = registerFoodItem("food/arctic_sweet_shrimp_and_peach_salad", Item::new, new Item.Properties());
    public static final Item ASSORTED_TEMPURA = registerFoodItem("food/assorted_tempura", Item::new, new Item.Properties());
    public static final Item A_LITTLE_SWEET_POISON = registerFoodItem("food/a_little_sweet_poison", Item::new, new Item.Properties());
    public static final Item BAKED_CRAB_WITH_CREAM = registerFoodItem("food/baked_crab_with_cream", Item::new, new Item.Properties());
    public static final Item BAKED_SWEET_POTATOES = registerFoodItem("food/baked_sweet_potatoes", Item::new, new Item.Properties());
    public static final Item BAMBOO_SHOOTS_FRIED_MEAT = registerFoodItem("food/bamboo_shoots_fried_meat", Item::new, new Item.Properties());
    public static final Item BAMBOO_SHOOTS_STEWED_IN_STONE_POT = registerFoodItem("food/bamboo_shoots_stewed_in_stone_pot", Item::new, new Item.Properties());
    public static final Item BAMBOO_STEAMED_EGG = registerFoodItem("food/bamboo_steamed_egg", Item::new, new Item.Properties());
    public static final Item BAMBOO_TUBE_ROASTED_DRUNKEN_SHRIMP = registerFoodItem("food/bamboo_tube_roasted_drunken_shrimp", Item::new, new Item.Properties());
    public static final Item BAMBOO_TUBE_STEAMED_PORK = registerFoodItem("food/bamboo_tube_steamed_pork", Item::new, new Item.Properties());
    public static final Item BEAR_PAW = registerFoodItem("food/bear_paw", Item::new, new Item.Properties());
    public static final Item BEEF_HOT_POT = registerFoodItem("food/beef_hot_pot", Item::new, new Item.Properties());
    public static final Item BEEF_RICE = registerFoodItem("food/beef_rice", Item::new, new Item.Properties());
    public static final Item BEEF_WELLINGTON = registerFoodItem("food/beef_wellington", Item::new, new Item.Properties());
    public static final Item BEETLE_STEAMED_CAKE = registerFoodItem("food/beetle_steamed_cake", Item::new, new Item.Properties());
    public static final Item BISCAY_BISCUITS = registerFoodItem("food/biscay_biscuits", Item::new, new Item.Properties());
    public static final Item BOILED_FISH = registerFoodItem("food/boiled_fish", Item::new, new Item.Properties());
    public static final Item BRAISED_EEL = registerFoodItem("food/braised_eel", Item::new, new Item.Properties());
    public static final Item BRAISED_PORK_WITH_PEACH = registerFoodItem("food/braised_pork_with_peach", Item::new, new Item.Properties());
    public static final Item BURNT_PUDDING = registerFoodItem("food/burnt_pudding", Item::new, new Item.Properties());
    public static final Item BUTTER_STEAK = registerFoodItem("food/butter_steak", Item::new, new Item.Properties());
    public static final Item CANDIED_CHESTNUTS = registerFoodItem("food/candied_chestnuts", Item::new, new Item.Properties());
    public static final Item CANDIED_SWEET_POTATO = registerFoodItem("food/candied_sweet_potato", Item::new, new Item.Properties());
    public static final Item CATS_PLAYING_IN_WATER = registerFoodItem("food/cats_playing_in_water", Item::new, new Item.Properties());
    public static final Item CAT_FOOD = registerFoodItem("food/cat_food", Item::new, new Item.Properties());
    public static final Item CAT_KULULI = registerFoodItem("food/cat_kululi", Item::new, new Item.Properties());
    public static final Item CAT_PIZZA = registerFoodItem("food/cat_pizza", Item::new, new Item.Properties());
    public static final Item CHEESE_EGG = registerFoodItem("food/cheese_egg", Item::new, new Item.Properties());
    public static final Item COLD_DISH_CARVING = registerFoodItem("food/cold_dish_carving", Item::new, new Item.Properties());
    public static final Item COLD_TOFU = registerFoodItem("food/cold_tofu", Item::new, new Item.Properties());
    public static final Item COLORFUL_JADE_FRIED_BUNS = registerFoodItem("food/colorful_jade_fried_buns", Item::new, new Item.Properties());
    public static final Item COOKING_TOFU = registerFoodItem("food/cooking_tofu", Item::new, new Item.Properties());
    public static final Item CREAM_STEW = registerFoodItem("food/cream_stew", Item::new, new Item.Properties());
    public static final Item CRISP_CYCLONE = registerFoodItem("food/crisp_cyclone", Item::new, new Item.Properties());
    public static final Item DARK_CUISINE = registerFoodItem("food/dark_cuisine", Item::new, new Item.Properties());
    public static final Item DEEP_FRIED_CICADA_SHELLS = registerFoodItem("food/deep_fried_cicada_shells", Item::new, new Item.Properties());
    public static final Item DEPRESSED_CHEESE_STICKS = registerFoodItem("food/depressed_cheese_sticks", Item::new, new Item.Properties());
    public static final Item DEW_BOILED_EGGS = registerFoodItem("food/dew_boiled_eggs", Item::new, new Item.Properties());
    public static final Item DORAYAKI = registerFoodItem("food/dorayaki", Item::new, new Item.Properties());
    public static final Item DUMPLING = registerFoodItem("food/dumpling", Item::new, new Item.Properties());
    public static final Item EEL_EGG_DONBURI = registerFoodItem("food/eel_egg_donburi", Item::new, new Item.Properties());
    public static final Item EGGS_BENEDICT = registerFoodItem("food/eggs_benedict", Item::new, new Item.Properties());
    public static final Item ENERGY_STRING = registerFoodItem("food/energy_string", Item::new, new Item.Properties());
    public static final Item FAILING_SAKURA_SNOW = registerFoodItem("food/failing_sakura_snow", Item::new, new Item.Properties());
    public static final Item FANTASY_IS_ALL_THE_RAGE = registerFoodItem("food/fantasy_is_all_the_rage", Item::new, new Item.Properties());
    public static final Item FISH_LEAPS_OVER_DRAGON_GATE = registerFoodItem("food/fish_leaps_over_dragon_gate", Item::new, new Item.Properties());
    public static final Item FLOWERS_BIRDS_WIND_AND_MOON = registerFoodItem("food/flowers_birds_wind_and_moon", Item::new, new Item.Properties());
    public static final Item FLOWING_WATER_NOODLES = registerFoodItem("food/flowing_water_noodles", Item::new, new Item.Properties());
    public static final Item FRIED_HAGFISH = registerFoodItem("food/fried_hagfish", Item::new, new Item.Properties());
    public static final Item FRIED_PORK_CUTLET = registerFoodItem("food/fried_pork_cutlet", Item::new, new Item.Properties());
    public static final Item FRIED_PORK_SHREDS = registerFoodItem("food/fried_pork_shreds", Item::new, new Item.Properties());
    public static final Item FRIED_SHRIMP_TEMPURA = registerFoodItem("food/fried_shrimp_tempura", Item::new, new Item.Properties());
    public static final Item FRIED_TOFU = registerFoodItem("food/fried_tofu", Item::new, new Item.Properties());
    public static final Item FRIED_TOMATO_STRIPS = registerFoodItem("food/fried_tomato_strips", Item::new, new Item.Properties());
    public static final Item FRIGHT_ADVENTURE = registerFoodItem("food/fright_adventure", Item::new, new Item.Properties());
    public static final Item GAME_SOUP = registerFoodItem("food/game_soup", Item::new, new Item.Properties());
    public static final Item GENSOKYO_BUDDHA_JUMPS_OVER_THE_WALL = registerFoodItem("food/gensokyo_buddha_jumps_over_the_wall", Item::new, new Item.Properties());
    public static final Item GENSOKYO_STAR_LOTUS_SHIP = registerFoodItem("food/gensokyo_star_lotus_ship", Item::new, new Item.Properties());
    public static final Item GIANT_TAMAGOYAKI = registerFoodItem("food/giant_tamagoyaki", Item::new, new Item.Properties());
    public static final Item GINKGO_AND_RADISH_PORK_RIB_SOUP = registerFoodItem("food/ginkgo_and_radish_pork_rib_soup", Item::new, new Item.Properties());
    public static final Item GLOOMY_FRUIT_PIE = registerFoodItem("food/gloomy_fruit_pie", Item::new, new Item.Properties());
    public static final Item GLUTINOUS_RICE_BALLS = registerFoodItem("food/glutinous_rice_balls", Item::new, new Item.Properties());
    public static final Item GOLDEN_CRISPY_FISH_CAKE = registerFoodItem("food/golden_crispy_fish_cake", Item::new, new Item.Properties());
    public static final Item GRAND_BANQUET = registerFoodItem("food/grand_banquet", Item::new, new Item.Properties());
    public static final Item GREEN_BAMBOO_WELCOMES_SPRING = registerFoodItem("food/green_bamboo_welcomes_spring", Item::new, new Item.Properties());
    public static final Item GREEN_FAIRY_MUSHROOM = registerFoodItem("food/green_fairy_mushroom", Item::new, new Item.Properties());
    public static final Item GRILLED_HAGFISH = registerFoodItem("food/grilled_hagfish", Item::new, new Item.Properties());
    public static final Item GRILLED_PORK_RICE_BALLS = registerFoodItem("food/grilled_pork_rice_balls", Item::new, new Item.Properties());
    public static final Item HEART_PORRIDGE_GRUEL = registerFoodItem("food/heart_porridge_gruel", Item::new, new Item.Properties());
    public static final Item HELL_THRILL_WARNING = registerFoodItem("food/hell_thrill_warning", Item::new, new Item.Properties());
    public static final Item HOLY_WHITE_LOTUS_SEED_CAKE = registerFoodItem("food/holy_white_lotus_seed_cake", Item::new, new Item.Properties());
    public static final Item HONEY_BBQ_PORK = registerFoodItem("food/honey_bbq_pork", Item::new, new Item.Properties());
    public static final Item HORAI_DAMA_NO_EDA = registerFoodItem("food/horai-dama_no_eda", Item::new, new Item.Properties());
    public static final Item HOT_WAFFLES = registerFoodItem("food/hot_waffles", Item::new, new Item.Properties());
    public static final Item HULA_SOUP = registerFoodItem("food/hula_soup", Item::new, new Item.Properties());
    public static final Item LION_HEAD = registerFoodItem("food/lion_head", Item::new, new Item.Properties());
    public static final Item LONGYIN_PEACH = registerFoodItem("food/longyin_peach", Item::new, new Item.Properties());
    public static final Item LOOKING_UP_AT_THE_CEILING_FRUIT_PIE = registerFoodItem("food/looking_up_at_the_ceiling_fruit_pie", Item::new, new Item.Properties());
    public static final Item LOTUS_FISH_RICE_BOWL = registerFoodItem("food/lotus_fish_rice_bowl", Item::new, new Item.Properties());
    public static final Item LUOHAN_VEGETARIAN = registerFoodItem("food/luohan_vegetarian", Item::new, new Item.Properties());
    public static final Item MAD_HATTER_TEA_PARTY = registerFoodItem("food/mad_hatter_tea_party", Item::new, new Item.Properties());
    public static final Item MAGMA = registerFoodItem("food/magma", Item::new, new Item.Properties());
    public static final Item MAOYU_LAVA_TOFU = registerFoodItem("food/maoyu_lava_tofu", Item::new, new Item.Properties());
    public static final Item MAOYU_TRICOLOR_ICE_CREAM = registerFoodItem("food/maoyu_tricolor_ice_cream", Item::new, new Item.Properties());
    public static final Item MAPO_TOFU = registerFoodItem("food/mapo_tofu", Item::new, new Item.Properties());
    public static final Item MILKY_MUSHROOM_SOUP = registerFoodItem("food/milky_mushroom_soup", Item::new, new Item.Properties());
    public static final Item MOCHI = registerFoodItem("food/mochi", Item::new, new Item.Properties());
    public static final Item MOLECULAR_EGG = registerFoodItem("food/molecular_egg", Item::new, new Item.Properties());
    public static final Item MOONLIGHT_DUMPLINGS = registerFoodItem("food/moonlight_dumplings", Item::new, new Item.Properties());
    public static final Item MOONLIGHT_OVER_LOTUS_POND = registerFoodItem("food/moonlight_over_lotus_pond", Item::new, new Item.Properties());
    public static final Item MOON_CAKE = registerFoodItem("food/moon_cake", Item::new, new Item.Properties());
    public static final Item MOON_LOVERS = registerFoodItem("food/moon_lovers", Item::new, new Item.Properties());
    public static final Item MUSHROOM_GIRLS_DANCE_STEW = registerFoodItem("food/mushroom_girls_dance_stew", Item::new, new Item.Properties());
    public static final Item MUSHROOM_MEAT_SLICES = registerFoodItem("food/mushroom_meat_slices", Item::new, new Item.Properties());
    public static final Item NIGIRI_SUSHI = registerFoodItem("food/nigiri_sushi", Item::new, new Item.Properties());
    public static final Item OEDO_BOAT_FESTIVAL = registerFoodItem("food/oedo_boat_festival", Item::new, new Item.Properties());
    public static final Item OKONOMIYAKI = registerFoodItem("food/okonomiyaki", Item::new, new Item.Properties());
    public static final Item ONE_HIT_KILL = registerFoodItem("food/one_hit_kill", Item::new, new Item.Properties());
    public static final Item ORDINARY_SMALL_CAKE = registerFoodItem("food/ordinary_small_cake", Item::new, new Item.Properties());
    public static final Item PAN_FRIED_MUSHROOM_MEAT_ROLL = registerFoodItem("food/pan_fried_mushroom_meat_roll", Item::new, new Item.Properties());
    public static final Item PAN_FRIED_SALMON = registerFoodItem("food/pan_fried_salmon", Item::new, new Item.Properties());
    public static final Item PEACH_BLOSSOM_GLAZE_ROLL = registerFoodItem("food/peach_blossom_glaze_roll", Item::new, new Item.Properties());
    public static final Item PEACH_BLOSSOM_SOUP = registerFoodItem("food/peach_blossom_soup", Item::new, new Item.Properties());
    public static final Item PHOENIX = registerFoodItem("food/phoenix", Item::new, new Item.Properties());
    public static final Item PICKLED_CUCUMBERS = registerFoodItem("food/pickled_cucumbers", Item::new, new Item.Properties());
    public static final Item PIG_DEER_BUTTERFLY = registerFoodItem("food/pig_deer_butterfly", Item::new, new Item.Properties());
    public static final Item PINE_NUT_CAKE = registerFoodItem("food/pine_nut_cake", Item::new, new Item.Properties());
    public static final Item PIRATE_BACON = registerFoodItem("food/pirate_bacon", Item::new, new Item.Properties());
    public static final Item PLUM_TEA_RICE = registerFoodItem("food/plum_tea_rice", Item::new, new Item.Properties());
    public static final Item POETRY_AND_GINKGO = registerFoodItem("food/poetry_and_ginkgo", Item::new, new Item.Properties());
    public static final Item POISONOUS_GARDEN = registerFoodItem("food/poisonous_garden", Item::new, new Item.Properties());
    public static final Item PORK_AND_TROUT_SMOKED = registerFoodItem("food/pork_and_trout_smoked", Item::new, new Item.Properties());
    public static final Item PORK_RICE = registerFoodItem("food/pork_rice", Item::new, new Item.Properties());
    public static final Item POTATO_CROQUETTES = registerFoodItem("food/potato_croquettes", Item::new, new Item.Properties());
    public static final Item PSEUDO_JIRITAMA = registerFoodItem("food/pseudo_jiritama", Item::new, new Item.Properties());
    public static final Item PUMPKIN_SHRIMP_CAKE = registerFoodItem("food/pumpkin_shrimp_cake", Item::new, new Item.Properties());
    public static final Item RAPUNZEL = registerFoodItem("food/rapunzel", Item::new, new Item.Properties());
    public static final Item REAL_SEAFOOD_MISO_SOUP = registerFoodItem("food/real_seafood_miso_soup", Item::new, new Item.Properties());
    public static final Item RED_BEAN_DAIFUKU = registerFoodItem("food/red_bean_daifuku", Item::new, new Item.Properties());
    public static final Item REFRESHING_PUDDING = registerFoodItem("food/refreshing_pudding", Item::new, new Item.Properties());
    public static final Item REVERSING_THE_WORLD = registerFoodItem("food/reversing_the_world", Item::new, new Item.Properties());
    public static final Item RICE_BALL = registerFoodItem("food/rice_ball", Item::new, new Item.Properties());
    public static final Item RISOTTO = registerFoodItem("food/risotto", Item::new, new Item.Properties());
    public static final Item ROASTED_MUSHROOMS = registerFoodItem("food/roasted_mushrooms", Item::new, new Item.Properties());
    public static final Item SAKURA_PUDDING = registerFoodItem("food/sakura_pudding", Item::new, new Item.Properties());
    public static final Item SALMON_TEMPURA = registerFoodItem("food/salmon_tempura", Item::new, new Item.Properties());
    public static final Item SASHIMI_PLATTER = registerFoodItem("food/sashimi_platter", Item::new, new Item.Properties());
    public static final Item SCARLET_DEVILS_CAKE = registerFoodItem("food/scarlet_devils_cake", Item::new, new Item.Properties());
    public static final Item SCONES = registerFoodItem("food/scones", Item::new, new Item.Properties());
    public static final Item SCREAMING_ODEN = registerFoodItem("food/screaming_oden", Item::new, new Item.Properties());
    public static final Item SEAFOOD_MISO_SOUP = registerFoodItem("food/seafood_miso_soup", Item::new, new Item.Properties());
    public static final Item SEA_URCHIN_SASHIMI = registerFoodItem("food/sea_urchin_sashimi", Item::new, new Item.Properties());
    public static final Item SEA_URCHIN_SHINGEN_PANCAKE = registerFoodItem("food/sea_urchin_shingen_pancake", Item::new, new Item.Properties());
    public static final Item SECRET_DRIED_FISH = registerFoodItem("food/secret_dried_fish", Item::new, new Item.Properties());
    public static final Item SECRET_MUSHROOM_CASSEROLE = registerFoodItem("food/secret_mushroom_casserole", Item::new, new Item.Properties());
    public static final Item SEVEN_COLORED_YOKAN = registerFoodItem("food/seven_colored_yokan", Item::new, new Item.Properties());
    public static final Item SHIRAGA_SADAMATSU = registerFoodItem("food/shiraga_sadamatsu", Item::new, new Item.Properties());
    public static final Item SKINNY_HORSE_DUMPLING = registerFoodItem("food/skinny_horse_dumpling", Item::new, new Item.Properties());
    public static final Item SNOW_WHITE = registerFoodItem("food/snow_white", Item::new, new Item.Properties());
    public static final Item STEAMED_EGG_WITH_SEA_URCHIN = registerFoodItem("food/steamed_egg_with_sea_urchin", Item::new, new Item.Properties());
    public static final Item STINKY_TOFU = registerFoodItem("food/stinky_tofu", Item::new, new Item.Properties());
    public static final Item STRENGTH_SOUP = registerFoodItem("food/strength_soup", Item::new, new Item.Properties());
    public static final Item SUPERME_SEAFOOD_NOODLES = registerFoodItem("food/superme_seafood_noodles", Item::new, new Item.Properties());
    public static final Item TAICHI_BAGUA_FISH_MAW = registerFoodItem("food/taichi_bagua_fish_maw", Item::new, new Item.Properties());
    public static final Item TAKETORIHIME = registerFoodItem("food/taketorihime", Item::new, new Item.Properties());
    public static final Item TAKOYAKI = registerFoodItem("food/takoyaki", Item::new, new Item.Properties());
    public static final Item THE_BEAUTY_OF_HAN_PALACE = registerFoodItem("food/the_beauty_of_han_palace", Item::new, new Item.Properties());
    public static final Item THE_DREAM = registerFoodItem("food/the_dream", Item::new, new Item.Properties());
    public static final Item THE_MARS = registerFoodItem("food/the_mars", Item::new, new Item.Properties());
    public static final Item THE_SOURCE_OF_LIFE = registerFoodItem("food/the_source_of_life", Item::new, new Item.Properties());
    public static final Item TIANSHI_BRAISED_CHESTNUT_MUSHROOMS = registerFoodItem("food/tianshi_braised_chestnut_mushrooms", Item::new, new Item.Properties());
    public static final Item TOFU_MISO = registerFoodItem("food/tofu_miso", Item::new, new Item.Properties());
    public static final Item TOFU_POT = registerFoodItem("food/tofu_pot", Item::new, new Item.Properties());
    public static final Item TONKOTSU_RAMEN = registerFoodItem("food/tonkotsu_ramen", Item::new, new Item.Properties());
    public static final Item TOON_PANCAKES = registerFoodItem("food/toon_pancakes", Item::new, new Item.Properties());
    public static final Item TWO_HEAVENS_ONE_STYLE = registerFoodItem("food/two_heavens_one_style", Item::new, new Item.Properties());
    public static final Item UDUMBARA_CAKE = registerFoodItem("food/udumbara_cake", Item::new, new Item.Properties());
    public static final Item UNCONSCIOUS_MONSTER_MOUSSE = registerFoodItem("food/unconscious_monster_mousse", Item::new, new Item.Properties());
    public static final Item VEGETABLE_SPECIAL = registerFoodItem("food/vegetable_special", Item::new, new Item.Properties());
    public static final Item WARM_RICE_BALL = registerFoodItem("food/warm_rice_ball", Item::new, new Item.Properties());
    public static final Item WHITE_PEACH_EIGHT_BRIDGE = registerFoodItem("food/white_peach_eight_bridge", Item::new, new Item.Properties());
    public static final Item YUNSHAN_COTTON_CANDY = registerFoodItem("food/yunshan_cotton_candy", Item::new, new Item.Properties());
    public static final Item ZHAJI = registerFoodItem("food/zhaji", Item::new, new Item.Properties());

    public static Item registerFoodItem(String name, Function<Item.Properties, Item> factory, Item.Properties settings) {
        Item item = RDItems.registerSimpleItem(name, factory, settings.food(new FoodProperties.Builder().nutrition(2).saturationModifier(2).build()));
        registerComponent(item);
        FOOD_ITEMS.add(item);
        return item;
    }

    public static void registerComponent(Item item) {
        DefaultItemComponentEvents.MODIFY.register(context -> {
            context.modify(item, builder -> builder.set(RDDataComponents.FOOD_ITEM_TYPE, Unit.INSTANCE));
        });
    }

    public static void registerItems() {

    }
}
