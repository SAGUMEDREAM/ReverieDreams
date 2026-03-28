package cc.thonly.reverie_dreams.registry.content.item;

import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.util.Unit;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class RDIngredientItems {
    public static final List<Item> INGREDIENTS = new LinkedList<>();

    public static final Item BAMBOO_SHOOTS = registerIngredient("ingredient/bamboo_shoots", Item::new, new Item.Properties());
    public static final Item BLACK_PORK = registerIngredient("ingredient/black_pork", Item::new, new Item.Properties());
    public static final Item BLACK_SALT = registerIngredient("ingredient/black_salt", Item::new, new Item.Properties());
    public static final Item BROCCOLI = registerIngredient("ingredient/broccoli", Item::new, new Item.Properties());
    public static final Item BUTTER = registerIngredient("ingredient/butter", Item::new, new Item.Properties());
    public static final Item CAPSAICIN = registerIngredient("ingredient/capsaicin", Item::new, new Item.Properties());
    public static final Item CHEESE = registerIngredient("ingredient/cheese", Item::new, new Item.Properties());
    public static final Item CHESTNUT = registerIngredient("ingredient/chestnut", Item::new, new Item.Properties());
    public static final Item CHILI = registerIngredient("ingredient/chili", Item::new, new Item.Properties());
    public static final Item CICADA_SHELL = registerIngredient("ingredient/cicada_shell", Item::new, new Item.Properties());
    public static final Item CRAB = registerIngredient("ingredient/crab", Item::new, new Item.Properties());
    public static final Item CREAM = registerIngredient("ingredient/cream", Item::new, new Item.Properties());
    public static final Item CUCUMBER = registerIngredient("ingredient/cucumber", Item::new, new Item.Properties());
    public static final Item DEW = registerIngredient("ingredient/dew", Item::new, new Item.Properties());
    public static final Item FICUS_MICROCARPA = registerIngredient("ingredient/ficus_microcarpa", Item::new, new Item.Properties());
    public static final Item FLOUR = registerIngredient("ingredient/flour", Item::new, new Item.Properties());
    public static final Item FLOWERS = registerIngredient("ingredient/flowers", Item::new, new Item.Properties());
    public static final Item GINKGO = registerIngredient("ingredient/ginkgo", Item::new, new Item.Properties());
    public static final Item GRAPE = registerIngredient("ingredient/grape", Item::new, new Item.Properties());
    public static final Item HAGFISH = registerIngredient("ingredient/hagfish", Item::new, new Item.Properties());
    public static final Item LEMON = registerIngredient("ingredient/lemon", Item::new, new Item.Properties());
    public static final Item LOTUS_NUTS = registerIngredient("ingredient/lotus_nuts", Item::new, new Item.Properties());
    public static final Item MOONFLOWER = registerIngredient("ingredient/moonflower", Item::new, new Item.Properties());
    public static final Item OCTOPUS = registerIngredient("ingredient/octopus", Item::new, new Item.Properties());
    public static final Item ONION = registerIngredient("ingredient/onion", Item::new, new Item.Properties());
    public static final Item PEACH = registerIngredient("ingredient/peach", Item::new, new Item.Properties());
    public static final Item PINE_NUT = registerIngredient("ingredient/pine_nut", Item::new, new Item.Properties());
    public static final Item PLUM = registerIngredient("ingredient/plum", Item::new, new Item.Properties());
    public static final Item PUFF_YO_FRUIT = registerIngredient("ingredient/puff_yo_fruit", Item::new, new Item.Properties());
    public static final Item RED_BEANS = registerIngredient("ingredient/red_beans", Item::new, new Item.Properties());
    public static final Item SALMON = registerIngredient("ingredient/salmon", Item::new, new Item.Properties());
    public static final Item SEA_URCHIN = registerIngredient("ingredient/sea_urchin", Item::new, new Item.Properties());
    public static final Item SHRIMP = registerIngredient("ingredient/shrimp", Item::new, new Item.Properties());
    public static final Item STICKY_RICE = registerIngredient("ingredient/sticky_rice", Item::new, new Item.Properties());
    public static final Item SUPREME_TUNA = registerIngredient("ingredient/supreme_tuna", Item::new, new Item.Properties());
    public static final Item SWEET_POTATO = registerIngredient("ingredient/sweet_potato", Item::new, new Item.Properties());
    public static final Item TOFU = registerIngredient("ingredient/tofu", Item::new, new Item.Properties());
    public static final Item TOMATO = registerIngredient("ingredient/tomato", Item::new, new Item.Properties());
    public static final Item TOON = registerIngredient("ingredient/toon", Item::new, new Item.Properties());
    public static final Item TREMELLA = registerIngredient("ingredient/tremella", Item::new, new Item.Properties());
    public static final Item TROUT = registerIngredient("ingredient/trout", Item::new, new Item.Properties());
    public static final Item TRUFFLE = registerIngredient("ingredient/truffle", Item::new, new Item.Properties());
    public static final Item TUNA = registerIngredient("ingredient/tuna", Item::new, new Item.Properties());
    public static final Item TWIN_LOTUS = registerIngredient("ingredient/twin_lotus", Item::new, new Item.Properties());
    public static final Item UDUMBARA = registerIngredient("ingredient/udumbara", Item::new, new Item.Properties());
    public static final Item VENISON = registerIngredient("ingredient/venison", Item::new, new Item.Properties());
    public static final Item WAGYU_BEEF = registerIngredient("ingredient/wagyu_beef", Item::new, new Item.Properties());
    public static final Item WHITE_RADISH = registerIngredient("ingredient/white_radish", Item::new, new Item.Properties());
    public static final Item WILD_BOAR_MEAT = registerIngredient("ingredient/wild_boar_meat", Item::new, new Item.Properties());

    public static Item registerIngredient(String name, Function<Item.Properties, Item> factory, Item.Properties settings) {
        Item item = RDItems.registerSimpleItem(name, factory, settings.food(new FoodProperties.Builder().nutrition(2).saturationModifier(2).build()));
        registerComponent(item);
        INGREDIENTS.add(item);
        return item;
    }

    public static void registerComponent(Item item) {
        DefaultItemComponentEvents.MODIFY.register(context -> {
            context.modify(item, builder -> builder.set(RDDataComponents.INGREDIENT_ITEM_TYPE, Unit.INSTANCE));
        });
    }

    public static void registerItems() {

    }
}
