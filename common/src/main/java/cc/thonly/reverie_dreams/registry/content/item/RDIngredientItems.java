package cc.thonly.reverie_dreams.registry.content.item;

import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import net.blay09.mods.balm.world.item.BalmItemRegistrar;
import net.blay09.mods.balm.world.item.DeferredItem;
import net.minecraft.util.Unit;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class RDIngredientItems {
    public static final List<DeferredItem> INGREDIENTS = new ArrayList<>();
    public static final List<DeferredItem> FISHING = new ArrayList<>();

    public static DeferredItem BAMBOO_SHOOTS;
    public static DeferredItem BLACK_PORK;
    public static DeferredItem BLACK_SALT;
    public static DeferredItem BROCCOLI;
    public static DeferredItem BUTTER;
    public static DeferredItem CAPSAICIN;
    public static DeferredItem CHEESE;
    public static DeferredItem CHESTNUT;
    public static DeferredItem CHILI;
    public static DeferredItem CICADA_SHELL;
    public static DeferredItem CRAB;
    public static DeferredItem CREAM;
    public static DeferredItem CUCUMBER;
    public static DeferredItem DEW;
    public static DeferredItem FICUS_MICROCARPA;
    public static DeferredItem FLOUR;
    public static DeferredItem FLOWERS;
    public static DeferredItem GINKGO;
    public static DeferredItem GRAPE;
    public static DeferredItem HAGFISH;
    public static DeferredItem LEMON;
    public static DeferredItem LOTUS_NUTS;
    public static DeferredItem MOONFLOWER;
    public static DeferredItem OCTOPUS;
    public static DeferredItem ONION;
    public static DeferredItem PEACH;
    public static DeferredItem PINE_NUT;
    public static DeferredItem PLUM;
    public static DeferredItem PUFF_YO_FRUIT;
    public static DeferredItem RED_BEANS;
    public static DeferredItem SALMON;
    public static DeferredItem SEA_URCHIN;
    public static DeferredItem SHRIMP;
    public static DeferredItem STICKY_RICE;
    public static DeferredItem SUPREME_TUNA;
    public static DeferredItem SWEET_POTATO;
    public static DeferredItem TOFU;
    public static DeferredItem TOMATO;
    public static DeferredItem TOON;
    public static DeferredItem TREMELLA;
    public static DeferredItem TROUT;
    public static DeferredItem TRUFFLE;
    public static DeferredItem TUNA;
    public static DeferredItem TWIN_LOTUS;
    public static DeferredItem UDUMBARA;
    public static DeferredItem VENISON;
    public static DeferredItem WAGYU_BEEF;
    public static DeferredItem WHITE_RADISH;
    public static DeferredItem WILD_BOAR_MEAT;

    public static void initialize(BalmItemRegistrar balmItemRegistrar) {
        BAMBOO_SHOOTS = registerIngredient(balmItemRegistrar, "ingredient/bamboo_shoots", ingredientFactory(), new Item.Properties());
        BLACK_PORK = registerIngredient(balmItemRegistrar, "ingredient/black_pork", ingredientFactory(), new Item.Properties());
        BLACK_SALT = registerIngredient(balmItemRegistrar, "ingredient/black_salt", ingredientFactory(), new Item.Properties());
        BROCCOLI = registerIngredient(balmItemRegistrar, "ingredient/broccoli", ingredientFactory(), new Item.Properties());
        BUTTER = registerIngredient(balmItemRegistrar, "ingredient/butter", ingredientFactory(), new Item.Properties());
        CAPSAICIN = registerIngredient(balmItemRegistrar, "ingredient/capsaicin", ingredientFactory(), new Item.Properties());
        CHEESE = registerIngredient(balmItemRegistrar, "ingredient/cheese", ingredientFactory(), new Item.Properties());
        CHESTNUT = registerIngredient(balmItemRegistrar, "ingredient/chestnut", ingredientFactory(), new Item.Properties());
        CHILI = registerIngredient(balmItemRegistrar, "ingredient/chili", ingredientFactory(), new Item.Properties());
        CICADA_SHELL = registerIngredient(balmItemRegistrar, "ingredient/cicada_shell", ingredientFactory(), new Item.Properties());
        CRAB = registerIngredient(balmItemRegistrar, "ingredient/crab", ingredientFactory(), new Item.Properties());
        CREAM = registerIngredient(balmItemRegistrar, "ingredient/cream", ingredientFactory(), new Item.Properties());
        CUCUMBER = registerIngredient(balmItemRegistrar, "ingredient/cucumber", ingredientFactory(), new Item.Properties());
        DEW = registerIngredient(balmItemRegistrar, "ingredient/dew", ingredientFactory(), new Item.Properties());
        FICUS_MICROCARPA = registerIngredient(balmItemRegistrar, "ingredient/ficus_microcarpa", ingredientFactory(), new Item.Properties());
        FLOUR = registerIngredient(balmItemRegistrar, "ingredient/flour", ingredientFactory(), new Item.Properties());
        FLOWERS = registerIngredient(balmItemRegistrar, "ingredient/flowers", ingredientFactory(), new Item.Properties());
        GINKGO = registerIngredient(balmItemRegistrar, "ingredient/ginkgo", ingredientFactory(), new Item.Properties());
        GRAPE = registerIngredient(balmItemRegistrar, "ingredient/grape", ingredientFactory(), new Item.Properties());
        HAGFISH = registerIngredient(balmItemRegistrar, "ingredient/hagfish", ingredientFactory(), new Item.Properties());
        LEMON = registerIngredient(balmItemRegistrar, "ingredient/lemon", ingredientFactory(), new Item.Properties());
        LOTUS_NUTS = registerIngredient(balmItemRegistrar, "ingredient/lotus_nuts", ingredientFactory(), new Item.Properties());
        MOONFLOWER = registerIngredient(balmItemRegistrar, "ingredient/moonflower", ingredientFactory(), new Item.Properties());
        OCTOPUS = registerIngredient(balmItemRegistrar, "ingredient/octopus", ingredientFactory(), new Item.Properties());
        ONION = registerIngredient(balmItemRegistrar, "ingredient/onion", ingredientFactory(), new Item.Properties());
        PEACH = registerIngredient(balmItemRegistrar, "ingredient/peach", ingredientFactory(), new Item.Properties());
        PINE_NUT = registerIngredient(balmItemRegistrar, "ingredient/pine_nut", ingredientFactory(), new Item.Properties());
        PLUM = registerIngredient(balmItemRegistrar, "ingredient/plum", ingredientFactory(), new Item.Properties());
        PUFF_YO_FRUIT = registerIngredient(balmItemRegistrar, "ingredient/puff_yo_fruit", ingredientFactory(), new Item.Properties());
        RED_BEANS = registerIngredient(balmItemRegistrar, "ingredient/red_beans", ingredientFactory(), new Item.Properties());
        SALMON = registerIngredient(balmItemRegistrar, "ingredient/salmon", ingredientFactory(), new Item.Properties());
        SEA_URCHIN = registerIngredient(balmItemRegistrar, "ingredient/sea_urchin", ingredientFactory(), new Item.Properties());
        SHRIMP = registerIngredient(balmItemRegistrar, "ingredient/shrimp", ingredientFactory(), new Item.Properties());
        STICKY_RICE = registerIngredient(balmItemRegistrar, "ingredient/sticky_rice", ingredientFactory(), new Item.Properties());
        SUPREME_TUNA = registerIngredient(balmItemRegistrar, "ingredient/supreme_tuna", ingredientFactory(), new Item.Properties());
        SWEET_POTATO = registerIngredient(balmItemRegistrar, "ingredient/sweet_potato", ingredientFactory(), new Item.Properties());
        TOFU = registerIngredient(balmItemRegistrar, "ingredient/tofu", ingredientFactory(), new Item.Properties());
        TOMATO = registerIngredient(balmItemRegistrar, "ingredient/tomato", ingredientFactory(), new Item.Properties());
        TOON = registerIngredient(balmItemRegistrar, "ingredient/toon", ingredientFactory(), new Item.Properties());
        TREMELLA = registerIngredient(balmItemRegistrar, "ingredient/tremella", ingredientFactory(), new Item.Properties());
        TROUT = registerIngredient(balmItemRegistrar, "ingredient/trout", ingredientFactory(), new Item.Properties());
        TRUFFLE = registerIngredient(balmItemRegistrar, "ingredient/truffle", ingredientFactory(), new Item.Properties());
        TUNA = registerIngredient(balmItemRegistrar, "ingredient/tuna", ingredientFactory(), new Item.Properties());
        TWIN_LOTUS = registerIngredient(balmItemRegistrar, "ingredient/twin_lotus", ingredientFactory(), new Item.Properties());
        UDUMBARA = registerIngredient(balmItemRegistrar, "ingredient/udumbara", ingredientFactory(), new Item.Properties());
        VENISON = registerIngredient(balmItemRegistrar, "ingredient/venison", ingredientFactory(), new Item.Properties());
        WAGYU_BEEF = registerIngredient(balmItemRegistrar, "ingredient/wagyu_beef", ingredientFactory(), new Item.Properties());
        WHITE_RADISH = registerIngredient(balmItemRegistrar, "ingredient/white_radish", ingredientFactory(), new Item.Properties());
        WILD_BOAR_MEAT = registerIngredient(balmItemRegistrar, "ingredient/wild_boar_meat", ingredientFactory(), new Item.Properties());

        FISHING.addAll(Arrays.asList(SHRIMP, CRAB, SALMON, TROUT, TUNA, SUPREME_TUNA));
    }

    public static DeferredItem registerIngredient(BalmItemRegistrar balmItemRegistrar, String name, Function<Item.Properties, Item> factory, Item.Properties settings) {
        DeferredItem item = RDItems.registerSimpleItem(balmItemRegistrar, name, factory, settings);
//        registerComponent(item);

        INGREDIENTS.add(item);
        return item;
    }

//    public static void registerComponent(Item item) {
//        DefaultItemComponentEvents.MODIFY.register(context -> {
//            context.modify(item, builder -> builder.set(RDDataComponents.INGREDIENT_ITEM_TYPE, Unit.INSTANCE));
//        });
//    }

    public static Function<Item.Properties, Item> ingredientFactory() {
        return props -> new Item(props.component(RDDataComponents.INGREDIENT_ITEM_TYPE.value(), Unit.INSTANCE)
                .food(new FoodProperties.Builder().nutrition(2).saturationModifier(2).build()));
    }

}
