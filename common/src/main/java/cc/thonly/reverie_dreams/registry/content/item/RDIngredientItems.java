package cc.thonly.reverie_dreams.registry.content.item;

import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.delegate.ItemDelegate;
import net.minecraft.util.Unit;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class RDIngredientItems {
    public static final List<ItemDelegate> INGREDIENTS = new ArrayList<>();
    public static final List<ItemDelegate> FISHING = new ArrayList<>();

    public static final ItemDelegate BAMBOO_SHOOTS = registerIngredient("ingredient/bamboo_shoots", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate BLACK_PORK = registerIngredient("ingredient/black_pork", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate BLACK_SALT = registerIngredient("ingredient/black_salt", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate BROCCOLI = registerIngredient("ingredient/broccoli", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate BUTTER = registerIngredient("ingredient/butter", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate CAPSAICIN = registerIngredient("ingredient/capsaicin", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate CHEESE = registerIngredient("ingredient/cheese", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate CHESTNUT = registerIngredient("ingredient/chestnut", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate CHILI = registerIngredient("ingredient/chili", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate CICADA_SHELL = registerIngredient("ingredient/cicada_shell", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate CRAB = registerIngredient("ingredient/crab", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate CREAM = registerIngredient("ingredient/cream", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate CUCUMBER = registerIngredient("ingredient/cucumber", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate DEW = registerIngredient("ingredient/dew", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate FICUS_MICROCARPA = registerIngredient("ingredient/ficus_microcarpa", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate FLOUR = registerIngredient("ingredient/flour", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate FLOWERS = registerIngredient("ingredient/flowers", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate GINKGO = registerIngredient("ingredient/ginkgo", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate GRAPE = registerIngredient("ingredient/grape", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate HAGFISH = registerIngredient("ingredient/hagfish", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate LEMON = registerIngredient("ingredient/lemon", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate LOTUS_NUTS = registerIngredient("ingredient/lotus_nuts", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate MOONFLOWER = registerIngredient("ingredient/moonflower", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate OCTOPUS = registerIngredient("ingredient/octopus", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate ONION = registerIngredient("ingredient/onion", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate PEACH = registerIngredient("ingredient/peach", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate PINE_NUT = registerIngredient("ingredient/pine_nut", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate PLUM = registerIngredient("ingredient/plum", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate PUFF_YO_FRUIT = registerIngredient("ingredient/puff_yo_fruit", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate RED_BEANS = registerIngredient("ingredient/red_beans", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate SALMON = registerIngredient("ingredient/salmon", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate SEA_URCHIN = registerIngredient("ingredient/sea_urchin", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate SHRIMP = registerIngredient("ingredient/shrimp", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate STICKY_RICE = registerIngredient("ingredient/sticky_rice", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate SUPREME_TUNA = registerIngredient("ingredient/supreme_tuna", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate SWEET_POTATO = registerIngredient("ingredient/sweet_potato", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate TOFU = registerIngredient("ingredient/tofu", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate TOMATO = registerIngredient("ingredient/tomato", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate TOON = registerIngredient("ingredient/toon", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate TREMELLA = registerIngredient("ingredient/tremella", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate TROUT = registerIngredient("ingredient/trout", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate TRUFFLE = registerIngredient("ingredient/truffle", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate TUNA = registerIngredient("ingredient/tuna", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate TWIN_LOTUS = registerIngredient("ingredient/twin_lotus", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate UDUMBARA = registerIngredient("ingredient/udumbara", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate VENISON = registerIngredient("ingredient/venison", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate WAGYU_BEEF = registerIngredient("ingredient/wagyu_beef", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate WHITE_RADISH = registerIngredient("ingredient/white_radish", ingredientFactory(), new Item.Properties());
    public static final ItemDelegate WILD_BOAR_MEAT = registerIngredient("ingredient/wild_boar_meat", ingredientFactory(), new Item.Properties());

    public static void initialize() {
        FISHING.addAll(Arrays.asList(SHRIMP, CRAB, SALMON, TROUT, TUNA, SUPREME_TUNA));
    }

    public static ItemDelegate registerIngredient(String name, Function<Item.Properties, Item> factory, Item.Properties settings) {
        ItemDelegate item = RDItems.registerSimpleItem(name, factory, settings);
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
        return props -> new Item(props.component(RDDataComponentTypes.INGREDIENT_ITEM_TYPE.value(), Unit.INSTANCE)
                .food(new FoodProperties.Builder().nutrition(2).saturationModifier(2).build()));
    }

}
