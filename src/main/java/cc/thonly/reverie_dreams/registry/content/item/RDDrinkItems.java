package cc.thonly.reverie_dreams.registry.content.item;

import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class RDDrinkItems {
    public static final List<Item> DRINK_ITEMS = new LinkedList<>();

    public static final Item AFFGADO = registerDrinkItem("drink/affgado", Item::new, new Item.Properties());
    public static final Item BEER = registerDrinkItem("drink/beer", Item::new, new Item.Properties());
    public static final Item BIG_POPSICLE = registerDrinkItem("drink/big_popsicle", Item::new, new Item.Properties());
    public static final Item BLESSING_WIND = registerDrinkItem("drink/blessing_wind", Item::new, new Item.Properties());
    public static final Item COFFEE = registerDrinkItem("drink/coffee", Item::new, new Item.Properties());
    public static final Item DAIGINJO = registerDrinkItem("drink/daiginjo", Item::new, new Item.Properties());
    public static final Item DAUGHTER_OF_THE_SEA = registerDrinkItem("drink/daughter_of_the_sea", Item::new, new Item.Properties());
    public static final Item DAWN = registerDrinkItem("drink/dawn", Item::new, new Item.Properties());
    public static final Item DEMONIC_COFFEE = registerDrinkItem("drink/demonic_coffee", Item::new, new Item.Properties());
    public static final Item DEMON_SLAYER = registerDrinkItem("drink/demon_slayer", Item::new, new Item.Properties());
    public static final Item DRUNK_ACTOR = registerDrinkItem("drink/drunk_actor", Item::new, new Item.Properties());
    public static final Item FAIRY_RAIN = registerDrinkItem("drink/fairy_rain", Item::new, new Item.Properties());
    public static final Item FIRE_RAT_FUR = registerDrinkItem("drink/fire_rat_fur", Item::new, new Item.Properties());
    public static final Item FOURTEENTH_NIGHT = registerDrinkItem("drink/fourteenth_night", Item::new, new Item.Properties());
    public static final Item FRUITY_HIGH_BALL = registerDrinkItem("drink/fruity_high_ball", Item::new, new Item.Properties());
    public static final Item FRUITY_SOUR = registerDrinkItem("drink/fruity_sour", Item::new, new Item.Properties());
    public static final Item GODFATHER = registerDrinkItem("drink/godfather", Item::new, new Item.Properties());
    public static final Item GODS_WHEAT = registerDrinkItem("drink/gods_wheat", Item::new, new Item.Properties());
    public static final Item GREEN_TEA = registerDrinkItem("drink/green_tea", Item::new, new Item.Properties());
    public static final Item GYOKURO_TEA = registerDrinkItem("drink/gyokuro_tea", Item::new, new Item.Properties());
    public static final Item HEAVEN_AND_EARTH_ARE_USELESS = registerDrinkItem("drink/heaven_and_earth_are_useless", Item::new, new Item.Properties());
    public static final Item ICEBERG_MAPLE_FROZEN_LEMON = registerDrinkItem("drink/iceberg_maple_frozen_lemon", Item::new, new Item.Properties());
    public static final Item KOMEIJI_ICE_CREAM = registerDrinkItem("drink/komeiji_ice_cream", Item::new, new Item.Properties());
    public static final Item MANGO_POMELO_SAGO = registerDrinkItem("drink/mango_pomelo_sago", Item::new, new Item.Properties());
    public static final Item MILK = registerDrinkItem("drink/milk", Item::new, new Item.Properties());
    public static final Item MOJITO_BURST_BALL = registerDrinkItem("drink/mojito_burst_ball", Item::new, new Item.Properties());
    public static final Item MOON_ROCKET = registerDrinkItem("drink/moon_rocket", Item::new, new Item.Properties());
    public static final Item NEGRONI = registerDrinkItem("drink/negroni", Item::new, new Item.Properties());
    public static final Item ORDINARY_FITNESS_TEA = registerDrinkItem("drink/ordinary_fitness_tea", Item::new, new Item.Properties());
    public static final Item OTTER_FESTIVAL = registerDrinkItem("drink/otter_festival", Item::new, new Item.Properties());
    public static final Item PALEO_CREAMY_SMOOTHIE = registerDrinkItem("drink/paleo_creamy_smoothie", Item::new, new Item.Properties());
    public static final Item PLUM_WINE = registerDrinkItem("drink/plum_wine", Item::new, new Item.Properties());
    public static final Item QI = registerDrinkItem("drink/qi", Item::new, new Item.Properties());
    public static final Item QILIN = registerDrinkItem("drink/qilin", Item::new, new Item.Properties());
    public static final Item QI_HEALTH = registerDrinkItem("drink/qi_health", Item::new, new Item.Properties());
    public static final Item RED_GRAPEFRUIT_JUICE = registerDrinkItem("drink/red_grapefruit_juice", Item::new, new Item.Properties());
    public static final Item RED_MIST = registerDrinkItem("drink/red_mist", Item::new, new Item.Properties());
    public static final Item SATELLITE_ICED_COFFEE = registerDrinkItem("drink/satellite_iced_coffee", Item::new, new Item.Properties());
    public static final Item SCARLET_DEVIL = registerDrinkItem("drink/scarlet_devil", Item::new, new Item.Properties());
    public static final Item SCARLET_DEVIL_MANSION_BLACK_TEA = registerDrinkItem("drink/scarlet_devil_mansion_black_tea", Item::new, new Item.Properties());
    public static final Item SODA = registerDrinkItem("drink/soda", Item::new, new Item.Properties());
    public static final Item SPACE_BEER = registerDrinkItem("drink/space_beer", Item::new, new Item.Properties());
    public static final Item SPARROW_SAKE = registerDrinkItem("drink/sparrow_sake", Item::new, new Item.Properties());
    public static final Item SUN_MOON_STAR = registerDrinkItem("drink/sun_moon_star", Item::new, new Item.Properties());
    public static final Item TENGU_DANCE = registerDrinkItem("drink/tengu_dance", Item::new, new Item.Properties());
    public static final Item WINTER_BREW = registerDrinkItem("drink/winter_brew", Item::new, new Item.Properties());

    public static Item registerDrinkItem(String name, Function<Item.Properties, Item> factory, Item.Properties settings) {
        Item item = RDItems.registerSimpleItem(name, factory, settings.food(new FoodProperties(0, 3, true)));
        registerComponent(item);
        DRINK_ITEMS.add(item);
        return item;
    }

    public static void registerComponent(Item item) {
        DefaultItemComponentEvents.MODIFY.register(context -> {
            context.modify(item, builder -> builder.set(RDDataComponents.DRINK_ITEM_TYPE, Unit.INSTANCE));
        });
    }

    public static void registerItems() {

    }
}
