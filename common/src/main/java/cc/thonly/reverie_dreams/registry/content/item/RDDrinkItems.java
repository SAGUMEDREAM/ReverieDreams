package cc.thonly.reverie_dreams.registry.content.item;

import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.impl.ItemDelegate;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class RDDrinkItems {
    public static final List<ItemDelegate> DRINK_ITEMS = new LinkedList<>();

    public static ItemDelegate AFFGADO = registerDrinkItem( "drink/affgado", drinkFactory(), new Item.Properties());
    public static ItemDelegate BEER = registerDrinkItem( "drink/beer", drinkFactory(), new Item.Properties());
    public static ItemDelegate BIG_POPSICLE = registerDrinkItem( "drink/big_popsicle", drinkFactory(), new Item.Properties());
    public static ItemDelegate BLESSING_WIND = registerDrinkItem( "drink/blessing_wind", drinkFactory(), new Item.Properties());
    public static ItemDelegate COFFEE = registerDrinkItem( "drink/coffee", drinkFactory(), new Item.Properties());
    public static ItemDelegate DAIGINJO = registerDrinkItem( "drink/daiginjo", drinkFactory(), new Item.Properties());
    public static ItemDelegate DAUGHTER_OF_THE_SEA = registerDrinkItem( "drink/daughter_of_the_sea", drinkFactory(), new Item.Properties());
    public static ItemDelegate DAWN = registerDrinkItem( "drink/dawn", drinkFactory(), new Item.Properties());
    public static ItemDelegate DEMONIC_COFFEE = registerDrinkItem( "drink/demonic_coffee", drinkFactory(), new Item.Properties());
    public static ItemDelegate DEMON_SLAYER = registerDrinkItem( "drink/demon_slayer", drinkFactory(), new Item.Properties());
    public static ItemDelegate DRUNK_ACTOR = registerDrinkItem( "drink/drunk_actor", drinkFactory(), new Item.Properties());
    public static ItemDelegate FAIRY_RAIN = registerDrinkItem( "drink/fairy_rain", drinkFactory(), new Item.Properties());
    public static ItemDelegate FIRE_RAT_FUR = registerDrinkItem( "drink/fire_rat_fur", drinkFactory(), new Item.Properties());
    public static ItemDelegate FOURTEENTH_NIGHT = registerDrinkItem( "drink/fourteenth_night", drinkFactory(), new Item.Properties());
    public static ItemDelegate FRUITY_HIGH_BALL = registerDrinkItem( "drink/fruity_high_ball", drinkFactory(), new Item.Properties());
    public static ItemDelegate FRUITY_SOUR = registerDrinkItem( "drink/fruity_sour", drinkFactory(), new Item.Properties());
    public static ItemDelegate GODFATHER = registerDrinkItem( "drink/godfather", drinkFactory(), new Item.Properties());
    public static ItemDelegate GODS_WHEAT = registerDrinkItem( "drink/gods_wheat", drinkFactory(), new Item.Properties());
    public static ItemDelegate GREEN_TEA = registerDrinkItem( "drink/green_tea", drinkFactory(), new Item.Properties());
    public static ItemDelegate GYOKURO_TEA = registerDrinkItem( "drink/gyokuro_tea", drinkFactory(), new Item.Properties());
    public static ItemDelegate HEAVEN_AND_EARTH_ARE_USELESS = registerDrinkItem( "drink/heaven_and_earth_are_useless", drinkFactory(), new Item.Properties());
    public static ItemDelegate ICEBERG_MAPLE_FROZEN_LEMON = registerDrinkItem( "drink/iceberg_maple_frozen_lemon", drinkFactory(), new Item.Properties());
    public static ItemDelegate KOMEIJI_ICE_CREAM = registerDrinkItem( "drink/komeiji_ice_cream", drinkFactory(), new Item.Properties());
    public static ItemDelegate MANGO_POMELO_SAGO = registerDrinkItem( "drink/mango_pomelo_sago", drinkFactory(), new Item.Properties());
    public static ItemDelegate MILK = registerDrinkItem( "drink/milk", drinkFactory(), new Item.Properties());
    public static ItemDelegate MOJITO_BURST_BALL = registerDrinkItem( "drink/mojito_burst_ball", drinkFactory(), new Item.Properties());
    public static ItemDelegate MOON_ROCKET = registerDrinkItem( "drink/moon_rocket", drinkFactory(), new Item.Properties());
    public static ItemDelegate NEGRONI = registerDrinkItem( "drink/negroni", drinkFactory(), new Item.Properties());
    public static ItemDelegate ORDINARY_FITNESS_TEA = registerDrinkItem( "drink/ordinary_fitness_tea", drinkFactory(), new Item.Properties());
    public static ItemDelegate OTTER_FESTIVAL = registerDrinkItem( "drink/otter_festival", drinkFactory(), new Item.Properties());
    public static ItemDelegate PALEO_CREAMY_SMOOTHIE = registerDrinkItem( "drink/paleo_creamy_smoothie", drinkFactory(), new Item.Properties());
    public static ItemDelegate PLUM_WINE = registerDrinkItem( "drink/plum_wine", drinkFactory(), new Item.Properties());
    public static ItemDelegate QI = registerDrinkItem( "drink/qi", drinkFactory(), new Item.Properties());
    public static ItemDelegate QILIN = registerDrinkItem( "drink/qilin", drinkFactory(), new Item.Properties());
    public static ItemDelegate QI_HEALTH = registerDrinkItem( "drink/qi_health", drinkFactory(), new Item.Properties());
    public static ItemDelegate RED_GRAPEFRUIT_JUICE = registerDrinkItem( "drink/red_grapefruit_juice", drinkFactory(), new Item.Properties());
    public static ItemDelegate RED_MIST = registerDrinkItem( "drink/red_mist", drinkFactory(), new Item.Properties());
    public static ItemDelegate SATELLITE_ICED_COFFEE = registerDrinkItem( "drink/satellite_iced_coffee", drinkFactory(), new Item.Properties());
    public static ItemDelegate SCARLET_DEVIL = registerDrinkItem( "drink/scarlet_devil", drinkFactory(), new Item.Properties());
    public static ItemDelegate SCARLET_DEVIL_MANSION_BLACK_TEA = registerDrinkItem( "drink/scarlet_devil_mansion_black_tea", drinkFactory(), new Item.Properties());
    public static ItemDelegate SODA = registerDrinkItem( "drink/soda", drinkFactory(), new Item.Properties());
    public static ItemDelegate SPACE_BEER = registerDrinkItem( "drink/space_beer", drinkFactory(), new Item.Properties());
    public static ItemDelegate SPARROW_SAKE = registerDrinkItem( "drink/sparrow_sake", drinkFactory(), new Item.Properties());
    public static ItemDelegate SUN_MOON_STAR = registerDrinkItem( "drink/sun_moon_star", drinkFactory(), new Item.Properties());
    public static ItemDelegate TENGU_DANCE = registerDrinkItem( "drink/tengu_dance", drinkFactory(), new Item.Properties());
    public static ItemDelegate WINTER_BREW = registerDrinkItem( "drink/winter_brew", drinkFactory(), new Item.Properties());

    public static void initialize() {

    }

    public static ItemDelegate registerDrinkItem( String name, Function<Item.Properties, Item> factory, Item.Properties settings) {
        ItemDelegate item = RDItems.registerSimpleItem( name, factory, settings);
        DRINK_ITEMS.add(item);
        return item;
    }

    public static Function<Item.Properties, Item> drinkFactory() {
        return props -> new Item(props.component(RDDataComponents.DRINK_ITEM_TYPE.value(), Unit.INSTANCE).food(new FoodProperties(0, 3, true), Consumable.builder().consumeSeconds(1.6F).animation(ItemUseAnimation.DRINK).sound(SoundEvents.GENERIC_DRINK).hasConsumeParticles(false).build()));
    }

}
