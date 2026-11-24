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
    public static ItemDelegate GREEN_TEA = registerBeverageItem("green_tea", 0, 0.0f);
    public static ItemDelegate FRUITY_HIGH_BALL = registerBeverageItem("fruity_high_ball", 1, 0.6f);
    public static ItemDelegate FRUITY_SOUR = registerBeverageItem("fruity_sour", 1, 0.4f);
    public static ItemDelegate QI = registerBeverageItem("qi", 1, 0.4f);
    public static ItemDelegate BEER = registerBeverageItem("beer", 1, 0.6f);
    public static ItemDelegate SUN_MOON_STAR = registerBeverageItem("sun_moon_star", 0, 0.4f);
    public static ItemDelegate PLUM_WINE = registerBeverageItem("plum_wine", 1, 0.8f);
    public static ItemDelegate TENGU_DANCE = registerBeverageItem("tengu_dance", 0, 0.2f);
    public static ItemDelegate SCARLET_DEVIL = registerBeverageItem("scarlet_devil", 1, 0.6f);
    public static ItemDelegate GODS_WHEAT = registerBeverageItem("gods_wheat", 0, 0.0f);
    public static ItemDelegate OTTER_FESTIVAL = registerBeverageItem("otter_festival", 0, 0.4f);
    public static ItemDelegate DAWN = registerBeverageItem("dawn", 0, 0.0f);
    public static ItemDelegate SPARROW_SAKE = registerBeverageItem("sparrow_sake", 0, 0.4f);
    public static ItemDelegate SCARLET_DEVIL_MANSION_BLACK_TEA = registerBeverageItem("scarlet_devil_mansion_black_tea", 0, 0.0f);
    public static ItemDelegate AFFGADO = registerBeverageItem("affgado", 3, 2.0f);
    public static ItemDelegate RED_MIST = registerBeverageItem("red_mist", 0, 0.4f);
    public static ItemDelegate NEGRONI = registerBeverageItem("negroni", 0, 0.2f);
    public static ItemDelegate GODFATHER = registerBeverageItem("godfather", 0, 0.2f);
    public static ItemDelegate BLESSING_WIND = registerBeverageItem("blessing_wind", 2, 1.2f);
    public static ItemDelegate WINTER_BREW = registerBeverageItem("winter_brew", 1, 0.6f);
    public static ItemDelegate FOURTEENTH_NIGHT = registerBeverageItem("fourteenth_night", 1, 0.4f);
    public static ItemDelegate FIRE_RAT_FUR = registerBeverageItem("fire_rat_fur", 0, 0.0f);
    public static ItemDelegate GYOKURO_TEA = registerBeverageItem("gyokuro_tea", 0, 0.0f);
    public static ItemDelegate MOON_ROCKET = registerBeverageItem("moon_rocket", 0, 0.0f);
    public static ItemDelegate MILK = registerBeverageItem("milk", beverageClearEffectFactory(2, 1.6f), new Item.Properties());
    public static ItemDelegate RED_GRAPEFRUIT_JUICE = registerBeverageItem("red_grapefruit_juice", 2, 0.8f);
    public static ItemDelegate SODA = registerBeverageItem("soda", 1, 0.2f);
    public static ItemDelegate ICEBERG_MAPLE_FROZEN_LEMON = registerBeverageItem("iceberg_maple_frozen_lemon", 2, 0.6f);
    public static ItemDelegate BIG_POPSICLE = registerBeverageItem("big_popsicle", beverageEatFactory(2, 0.6f), new Item.Properties());

    // DLC1
    public static ItemDelegate DAIGINJO = registerBeverageItem("daiginjo", 0, 0.4f);
    public static ItemDelegate COFFEE = registerBeverageItem("coffee", 0, 0.0f);
    public static ItemDelegate FAIRY_RAIN = registerBeverageItem("fairy_rain", 1, 0.4f);
    public static ItemDelegate PALEO_CREAMY_SMOOTHIE = registerBeverageItem("paleo_creamy_smoothie", 4, 3.0f);
    public static ItemDelegate ORDINARY_FITNESS_TEA = registerBeverageItem("ordinary_fitness_tea", 0, 0.0f);

    // DLC2
    public static ItemDelegate DEMON_SLAYER = registerBeverageItem("demon_slayer", 0, 0.0f);
    public static ItemDelegate QI_HEALTH = registerBeverageItem("qi_health", 1, 0.4f);
    public static ItemDelegate KOMEIJI_ICE_CREAM = registerBeverageItem("komeiji_ice_cream", 4, 2.8f);

    // DLC3
    public static ItemDelegate MANGO_POMELO_SAGO = registerBeverageItem("mango_pomelo_sago", 4, 3.2f);
    public static ItemDelegate QILIN = registerBeverageItem("qilin", 1, 0.8f);

    // DLC4
    public static ItemDelegate HEAVEN_AND_EARTH_ARE_USELESS = registerBeverageItem("heaven_and_earth_are_useless", 0, 0.0f);
    public static ItemDelegate DRUNK_ACTOR = registerBeverageItem("drunk_actor", 1, 0.4f);

    // DLC5
    public static ItemDelegate DAUGHTER_OF_THE_SEA = registerBeverageItem("daughter_of_the_sea", 0, 0.2f);
    public static ItemDelegate DEMONIC_COFFEE = registerBeverageItem("demonic_coffee", 2, 1.4f);
    public static ItemDelegate MOJITO_BURST_BALL = registerBeverageItem("mojito_burst_ball", 1, 0.2f);
    public static ItemDelegate SPACE_BEER = registerBeverageItem("space_beer", 1, 0.8f);
    public static ItemDelegate SATELLITE_ICED_COFFEE = registerBeverageItem("satellite_iced_coffee", 0, 0.0f);

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

    private static ItemDelegate registerBeverageItem(String name, int nutrition, float saturation) {
        return registerBeverageItem(name, beverageFactory(nutrition, saturation), new Item.Properties());
    }

    public static Function<Item.Properties, Item> beverageFactory(int nutrition, float saturation) {
        return props -> new Item(props.component(RDDataComponentTypes.DRINK_ITEM_TYPE.value(), Unit.INSTANCE)
                .food(new FoodProperties(nutrition, saturation, true), Consumable.builder()
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

    public static Function<Item.Properties, Item> beverageEatFactory(int nutrition, float saturation) {
        return props -> new Item(props.component(RDDataComponentTypes.DRINK_ITEM_TYPE.value(), Unit.INSTANCE)
                .food(new FoodProperties(nutrition, saturation, true), Consumable.builder()
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

    public static Function<Item.Properties, Item> beverageClearEffectFactory(int nutrition, float saturation) {
        return props -> new Item(props.component(RDDataComponentTypes.DRINK_ITEM_TYPE.value(), Unit.INSTANCE)
                .food(new FoodProperties(nutrition, saturation, true), Consumable.builder()
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
