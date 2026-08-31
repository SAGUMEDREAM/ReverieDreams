package cc.thonly.reverie_dreams.registry.content.item;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.registry.AliasManager;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.delegate.ItemDelegate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class RDBeverageItems {
    public static final List<ItemDelegate> BEVERAGE_ITEMS = new LinkedList<>();

    // DLC0
    public static ItemDelegate GREEN_TEA = registerBeverageItem("green_tea", beverageFactory(), new Item.Properties());
    public static ItemDelegate FRUITY_HIGH_BALL = registerBeverageItem("fruity_high_ball", beverageFactory(), new Item.Properties());
    public static ItemDelegate FRUITY_SOUR = registerBeverageItem("fruity_sour", beverageFactory(), new Item.Properties());
    public static ItemDelegate QI = registerBeverageItem("qi", beverageFactory(), new Item.Properties());
    public static ItemDelegate BEER = registerBeverageItem("beer", beverageFactory(), new Item.Properties());
    public static ItemDelegate SUN_MOON_STAR = registerBeverageItem("sun_moon_star", beverageFactory(), new Item.Properties());
    public static ItemDelegate PLUM_WINE = registerBeverageItem("plum_wine", beverageFactory(), new Item.Properties());
    public static ItemDelegate TENGU_DANCE = registerBeverageItem("tengu_dance", beverageFactory(), new Item.Properties());
    public static ItemDelegate SCARLET_DEVIL = registerBeverageItem("scarlet_devil", beverageFactory(), new Item.Properties());
    public static ItemDelegate GODS_WHEAT = registerBeverageItem("gods_wheat", beverageFactory(), new Item.Properties());
    public static ItemDelegate OTTER_FESTIVAL = registerBeverageItem("otter_festival", beverageFactory(), new Item.Properties());
    public static ItemDelegate DAWN = registerBeverageItem("dawn", beverageFactory(), new Item.Properties());
    public static ItemDelegate SPARROW_SAKE = registerBeverageItem("sparrow_sake", beverageFactory(), new Item.Properties());
    public static ItemDelegate SCARLET_DEVIL_MANSION_BLACK_TEA = registerBeverageItem("scarlet_devil_mansion_black_tea", beverageFactory(), new Item.Properties());
    public static ItemDelegate AFFGADO = registerBeverageItem("affgado", beverageFactory(), new Item.Properties());
    public static ItemDelegate RED_MIST = registerBeverageItem("red_mist", beverageFactory(), new Item.Properties());
    public static ItemDelegate NEGRONI = registerBeverageItem("negroni", beverageFactory(), new Item.Properties());
    public static ItemDelegate GODFATHER = registerBeverageItem("godfather", beverageFactory(), new Item.Properties());
    public static ItemDelegate BLESSING_WIND = registerBeverageItem("blessing_wind", beverageFactory(), new Item.Properties());
    public static ItemDelegate WINTER_BREW = registerBeverageItem("winter_brew", beverageFactory(), new Item.Properties());
    public static ItemDelegate FOURTEENTH_NIGHT = registerBeverageItem("fourteenth_night", beverageFactory(), new Item.Properties());
    public static ItemDelegate FIRE_RAT_FUR = registerBeverageItem("fire_rat_fur", beverageFactory(), new Item.Properties());
    public static ItemDelegate GYOKURO_TEA = registerBeverageItem("gyokuro_tea", beverageFactory(), new Item.Properties());
    public static ItemDelegate MOON_ROCKET = registerBeverageItem("moon_rocket", beverageFactory(), new Item.Properties());
    public static ItemDelegate MILK = registerBeverageItem("milk", beverageClearEffectFactory(), new Item.Properties());
    public static ItemDelegate RED_GRAPEFRUIT_JUICE = registerBeverageItem("red_grapefruit_juice", beverageFactory(), new Item.Properties());
    public static ItemDelegate SODA = registerBeverageItem("soda", beverageFactory(), new Item.Properties());
    public static ItemDelegate ICEBERG_MAPLE_FROZEN_LEMON = registerBeverageItem("iceberg_maple_frozen_lemon", beverageFactory(), new Item.Properties());
    public static ItemDelegate BIG_POPSICLE = registerBeverageItem("big_popsicle", beverageEatFactory(), new Item.Properties());

    // DLC1
    public static ItemDelegate DAIGINJO = registerBeverageItem("daiginjo", beverageFactory(), new Item.Properties());
    public static ItemDelegate COFFEE = registerBeverageItem("coffee", beverageFactory(), new Item.Properties());
    public static ItemDelegate FAIRY_RAIN = registerBeverageItem("fairy_rain", beverageFactory(), new Item.Properties());
    public static ItemDelegate PALEO_CREAMY_SMOOTHIE = registerBeverageItem("paleo_creamy_smoothie", beverageFactory(), new Item.Properties());
    public static ItemDelegate ORDINARY_FITNESS_TEA = registerBeverageItem("ordinary_fitness_tea", beverageFactory(), new Item.Properties());

    // DLC2
    public static ItemDelegate DEMON_SLAYER = registerBeverageItem("demon_slayer", beverageFactory(), new Item.Properties());
    public static ItemDelegate QI_HEALTH = registerBeverageItem("qi_health", beverageFactory(), new Item.Properties());
    public static ItemDelegate KOMEIJI_ICE_CREAM = registerBeverageItem("komeiji_ice_cream", beverageFactory(), new Item.Properties());

    // DLC3
    public static ItemDelegate MANGO_POMELO_SAGO = registerBeverageItem("mango_pomelo_sago", beverageFactory(), new Item.Properties());
    public static ItemDelegate QILIN = registerBeverageItem("qilin", beverageFactory(), new Item.Properties());

    // DLC4
    public static ItemDelegate HEAVEN_AND_EARTH_ARE_USELESS = registerBeverageItem("heaven_and_earth_are_useless", beverageFactory(), new Item.Properties());
    public static ItemDelegate DRUNK_ACTOR = registerBeverageItem("drunk_actor", beverageFactory(), new Item.Properties());

    // DLC5
    public static ItemDelegate DAUGHTER_OF_THE_SEA = registerBeverageItem("daughter_of_the_sea", beverageFactory(), new Item.Properties());
    public static ItemDelegate DEMONIC_COFFEE = registerBeverageItem("demonic_coffee", beverageFactory(), new Item.Properties());
    public static ItemDelegate MOJITO_BURST_BALL = registerBeverageItem("mojito_burst_ball", beverageFactory(), new Item.Properties());
    public static ItemDelegate SPACE_BEER = registerBeverageItem("space_beer", beverageFactory(), new Item.Properties());
    public static ItemDelegate SATELLITE_ICED_COFFEE = registerBeverageItem("satellite_iced_coffee", beverageFactory(), new Item.Properties());

    public static void initialize() {

    }

    public static ItemDelegate registerBeverageItem(String name, Function<Item.Properties, Item> factory, Item.Properties settings) {
        String oldPrefixName = "drink/" + name;
        String prefixName = "beverage/" + name;
        ItemDelegate item = RDItems.registerSimpleItem(prefixName, factory, settings);
        BEVERAGE_ITEMS.add(item);
        AliasManager.register(Registries.ITEM, ReverieDreams.id(oldPrefixName), ReverieDreams.id(prefixName));
        return item;
    }

    public static Function<Item.Properties, Item> beverageFactory() {
        return props -> new Item(props.component(RDDataComponentTypes.DRINK_ITEM_TYPE.value(), Unit.INSTANCE)
                .food(new FoodProperties(0, 3, true), Consumable.builder()
                        .consumeSeconds(1.6F)
                        .animation(ItemUseAnimation.DRINK)
                        .sound(SoundEvents.GENERIC_DRINK)
                        .hasConsumeParticles(false)
                        .build()
                )
                .component(DataComponents.USE_REMAINDER, new UseRemainder(new ItemStackTemplate(Items.GLASS_BOTTLE)))
                .craftRemainder(Items.GLASS_BOTTLE)
        );
    }

    public static Function<Item.Properties, Item> beverageEatFactory() {
        return props -> new Item(props.component(RDDataComponentTypes.DRINK_ITEM_TYPE.value(), Unit.INSTANCE)
                .food(new FoodProperties(0, 3, true), Consumable.builder()
                        .consumeSeconds(1.6F)
                        .animation(ItemUseAnimation.DRINK)
                        .sound(SoundEvents.GENERIC_EAT)
                        .hasConsumeParticles(false)
                        .build()
                )
                .component(DataComponents.USE_REMAINDER, new UseRemainder(new ItemStackTemplate(Items.GLASS_BOTTLE)))
                .craftRemainder(Items.GLASS_BOTTLE)
        );
    }

    public static Function<Item.Properties, Item> beverageClearEffectFactory() {
        return props -> new Item(props.component(RDDataComponentTypes.DRINK_ITEM_TYPE.value(), Unit.INSTANCE)
                .food(new FoodProperties(0, 3, true), Consumable.builder()
                        .consumeSeconds(1.6F)
                        .animation(ItemUseAnimation.DRINK)
                        .sound(SoundEvents.GENERIC_EAT)
                        .hasConsumeParticles(false)
                        .onConsume(ClearAllStatusEffectsConsumeEffect.INSTANCE)
                        .build()
                )
                .component(DataComponents.USE_REMAINDER, new UseRemainder(new ItemStackTemplate(Items.GLASS_BOTTLE)))
                .craftRemainder(Items.GLASS_BOTTLE)
        );
    }

}
