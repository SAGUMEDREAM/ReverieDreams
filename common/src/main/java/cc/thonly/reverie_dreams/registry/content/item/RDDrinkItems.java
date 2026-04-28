package cc.thonly.reverie_dreams.registry.content.item;

import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import net.blay09.mods.balm.world.item.BalmItemRegistrar;
import net.blay09.mods.balm.world.item.DeferredItem;
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
    public static final List<DeferredItem> DRINK_ITEMS = new LinkedList<>();

    public static DeferredItem AFFGADO;
    public static DeferredItem BEER;
    public static DeferredItem BIG_POPSICLE;
    public static DeferredItem BLESSING_WIND;
    public static DeferredItem COFFEE;
    public static DeferredItem DAIGINJO;
    public static DeferredItem DAUGHTER_OF_THE_SEA;
    public static DeferredItem DAWN;
    public static DeferredItem DEMONIC_COFFEE;
    public static DeferredItem DEMON_SLAYER;
    public static DeferredItem DRUNK_ACTOR;
    public static DeferredItem FAIRY_RAIN;
    public static DeferredItem FIRE_RAT_FUR;
    public static DeferredItem FOURTEENTH_NIGHT;
    public static DeferredItem FRUITY_HIGH_BALL;
    public static DeferredItem FRUITY_SOUR;
    public static DeferredItem GODFATHER;
    public static DeferredItem GODS_WHEAT;
    public static DeferredItem GREEN_TEA;
    public static DeferredItem GYOKURO_TEA;
    public static DeferredItem HEAVEN_AND_EARTH_ARE_USELESS;
    public static DeferredItem ICEBERG_MAPLE_FROZEN_LEMON;
    public static DeferredItem KOMEIJI_ICE_CREAM;
    public static DeferredItem MANGO_POMELO_SAGO;
    public static DeferredItem MILK;
    public static DeferredItem MOJITO_BURST_BALL;
    public static DeferredItem MOON_ROCKET;
    public static DeferredItem NEGRONI;
    public static DeferredItem ORDINARY_FITNESS_TEA;
    public static DeferredItem OTTER_FESTIVAL;
    public static DeferredItem PALEO_CREAMY_SMOOTHIE;
    public static DeferredItem PLUM_WINE;
    public static DeferredItem QI;
    public static DeferredItem QILIN;
    public static DeferredItem QI_HEALTH;
    public static DeferredItem RED_GRAPEFRUIT_JUICE;
    public static DeferredItem RED_MIST;
    public static DeferredItem SATELLITE_ICED_COFFEE;
    public static DeferredItem SCARLET_DEVIL;
    public static DeferredItem SCARLET_DEVIL_MANSION_BLACK_TEA;
    public static DeferredItem SODA;
    public static DeferredItem SPACE_BEER;
    public static DeferredItem SPARROW_SAKE;
    public static DeferredItem SUN_MOON_STAR;
    public static DeferredItem TENGU_DANCE;
    public static DeferredItem WINTER_BREW;

    public static void initialize(BalmItemRegistrar balmItemRegistrar) {
        AFFGADO = registerDrinkItem(balmItemRegistrar, "drink/affgado", drinkFactory(), new Item.Properties());
        BEER = registerDrinkItem(balmItemRegistrar, "drink/beer", drinkFactory(), new Item.Properties());
        BIG_POPSICLE = registerDrinkItem(balmItemRegistrar, "drink/big_popsicle", drinkFactory(), new Item.Properties());
        BLESSING_WIND = registerDrinkItem(balmItemRegistrar, "drink/blessing_wind", drinkFactory(), new Item.Properties());
        COFFEE = registerDrinkItem(balmItemRegistrar, "drink/coffee", drinkFactory(), new Item.Properties());
        DAIGINJO = registerDrinkItem(balmItemRegistrar, "drink/daiginjo", drinkFactory(), new Item.Properties());
        DAUGHTER_OF_THE_SEA = registerDrinkItem(balmItemRegistrar, "drink/daughter_of_the_sea", drinkFactory(), new Item.Properties());
        DAWN = registerDrinkItem(balmItemRegistrar, "drink/dawn", drinkFactory(), new Item.Properties());
        DEMONIC_COFFEE = registerDrinkItem(balmItemRegistrar, "drink/demonic_coffee", drinkFactory(), new Item.Properties());
        DEMON_SLAYER = registerDrinkItem(balmItemRegistrar, "drink/demon_slayer", drinkFactory(), new Item.Properties());
        DRUNK_ACTOR = registerDrinkItem(balmItemRegistrar, "drink/drunk_actor", drinkFactory(), new Item.Properties());
        FAIRY_RAIN = registerDrinkItem(balmItemRegistrar, "drink/fairy_rain", drinkFactory(), new Item.Properties());
        FIRE_RAT_FUR = registerDrinkItem(balmItemRegistrar, "drink/fire_rat_fur", drinkFactory(), new Item.Properties());
        FOURTEENTH_NIGHT = registerDrinkItem(balmItemRegistrar, "drink/fourteenth_night", drinkFactory(), new Item.Properties());
        FRUITY_HIGH_BALL = registerDrinkItem(balmItemRegistrar, "drink/fruity_high_ball", drinkFactory(), new Item.Properties());
        FRUITY_SOUR = registerDrinkItem(balmItemRegistrar, "drink/fruity_sour", drinkFactory(), new Item.Properties());
        GODFATHER = registerDrinkItem(balmItemRegistrar, "drink/godfather", drinkFactory(), new Item.Properties());
        GODS_WHEAT = registerDrinkItem(balmItemRegistrar, "drink/gods_wheat", drinkFactory(), new Item.Properties());
        GREEN_TEA = registerDrinkItem(balmItemRegistrar, "drink/green_tea", drinkFactory(), new Item.Properties());
        GYOKURO_TEA = registerDrinkItem(balmItemRegistrar, "drink/gyokuro_tea", drinkFactory(), new Item.Properties());
        HEAVEN_AND_EARTH_ARE_USELESS = registerDrinkItem(balmItemRegistrar, "drink/heaven_and_earth_are_useless", drinkFactory(), new Item.Properties());
        ICEBERG_MAPLE_FROZEN_LEMON = registerDrinkItem(balmItemRegistrar, "drink/iceberg_maple_frozen_lemon", drinkFactory(), new Item.Properties());
        KOMEIJI_ICE_CREAM = registerDrinkItem(balmItemRegistrar, "drink/komeiji_ice_cream", drinkFactory(), new Item.Properties());
        MANGO_POMELO_SAGO = registerDrinkItem(balmItemRegistrar, "drink/mango_pomelo_sago", drinkFactory(), new Item.Properties());
        MILK = registerDrinkItem(balmItemRegistrar, "drink/milk", drinkFactory(), new Item.Properties());
        MOJITO_BURST_BALL = registerDrinkItem(balmItemRegistrar, "drink/mojito_burst_ball", drinkFactory(), new Item.Properties());
        MOON_ROCKET = registerDrinkItem(balmItemRegistrar, "drink/moon_rocket", drinkFactory(), new Item.Properties());
        NEGRONI = registerDrinkItem(balmItemRegistrar, "drink/negroni", drinkFactory(), new Item.Properties());
        ORDINARY_FITNESS_TEA = registerDrinkItem(balmItemRegistrar, "drink/ordinary_fitness_tea", drinkFactory(), new Item.Properties());
        OTTER_FESTIVAL = registerDrinkItem(balmItemRegistrar, "drink/otter_festival", drinkFactory(), new Item.Properties());
        PALEO_CREAMY_SMOOTHIE = registerDrinkItem(balmItemRegistrar, "drink/paleo_creamy_smoothie", drinkFactory(), new Item.Properties());
        PLUM_WINE = registerDrinkItem(balmItemRegistrar, "drink/plum_wine", drinkFactory(), new Item.Properties());
        QI = registerDrinkItem(balmItemRegistrar, "drink/qi", drinkFactory(), new Item.Properties());
        QILIN = registerDrinkItem(balmItemRegistrar, "drink/qilin", drinkFactory(), new Item.Properties());
        QI_HEALTH = registerDrinkItem(balmItemRegistrar, "drink/qi_health", drinkFactory(), new Item.Properties());
        RED_GRAPEFRUIT_JUICE = registerDrinkItem(balmItemRegistrar, "drink/red_grapefruit_juice", drinkFactory(), new Item.Properties());
        RED_MIST = registerDrinkItem(balmItemRegistrar, "drink/red_mist", drinkFactory(), new Item.Properties());
        SATELLITE_ICED_COFFEE = registerDrinkItem(balmItemRegistrar, "drink/satellite_iced_coffee", drinkFactory(), new Item.Properties());
        SCARLET_DEVIL = registerDrinkItem(balmItemRegistrar, "drink/scarlet_devil", drinkFactory(), new Item.Properties());
        SCARLET_DEVIL_MANSION_BLACK_TEA = registerDrinkItem(balmItemRegistrar, "drink/scarlet_devil_mansion_black_tea", drinkFactory(), new Item.Properties());
        SODA = registerDrinkItem(balmItemRegistrar, "drink/soda", drinkFactory(), new Item.Properties());
        SPACE_BEER = registerDrinkItem(balmItemRegistrar, "drink/space_beer", drinkFactory(), new Item.Properties());
        SPARROW_SAKE = registerDrinkItem(balmItemRegistrar, "drink/sparrow_sake", drinkFactory(), new Item.Properties());
        SUN_MOON_STAR = registerDrinkItem(balmItemRegistrar, "drink/sun_moon_star", drinkFactory(), new Item.Properties());
        TENGU_DANCE = registerDrinkItem(balmItemRegistrar, "drink/tengu_dance", drinkFactory(), new Item.Properties());
        WINTER_BREW = registerDrinkItem(balmItemRegistrar, "drink/winter_brew", drinkFactory(), new Item.Properties());
    }

    public static DeferredItem registerDrinkItem(BalmItemRegistrar balmItemRegistrar, String name, Function<Item.Properties, Item> factory, Item.Properties settings) {
        DeferredItem item = RDItems.registerSimpleItem(balmItemRegistrar, name, factory, settings);
        DRINK_ITEMS.add(item);
        return item;
    }

    public static Function<Item.Properties, Item> drinkFactory() {
        return props -> new Item(props.component(RDDataComponents.DRINK_ITEM_TYPE.value(), Unit.INSTANCE).food(new FoodProperties(0, 3, true), Consumable.builder().consumeSeconds(1.6F).animation(ItemUseAnimation.DRINK).sound(SoundEvents.GENERIC_DRINK).hasConsumeParticles(false).build()));
    }

}
