package cc.thonly.reverie_dreams.registry.content.item;

import cc.thonly.reverie_dreams.item.base.IngredientItem;
import net.minecraft.world.item.Item;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

public class RDIngredientItems {
    public static final List<Item> INGREDIENTS = new LinkedList<>();

    public static final Item BAMBOO_SHOOTS = registerIngredient("ingredient/bamboo_shoots", IngredientItem::new, new Item.Properties());
    public static final Item BLACK_PORK = registerIngredient("ingredient/black_pork", IngredientItem::new, new Item.Properties());
    public static final Item BLACK_SALT = registerIngredient("ingredient/black_salt", IngredientItem::new, new Item.Properties());
    public static final Item BROCCOLI = registerIngredient("ingredient/broccoli", IngredientItem::new, new Item.Properties());
    public static final Item BUTTER = registerIngredient("ingredient/butter", IngredientItem::new, new Item.Properties());
    public static final Item CAPSAICIN = registerIngredient("ingredient/capsaicin", IngredientItem::new, new Item.Properties());
    public static final Item CHEESE = registerIngredient("ingredient/cheese", IngredientItem::new, new Item.Properties());
    public static final Item CHESTNUT = registerIngredient("ingredient/chestnut", IngredientItem::new, new Item.Properties());
    public static final Item CHILI = registerIngredient("ingredient/chili", IngredientItem::new, new Item.Properties());
    public static final Item CICADA_SHELL = registerIngredient("ingredient/cicada_shell", IngredientItem::new, new Item.Properties());
    public static final Item CRAB = registerIngredient("ingredient/crab", IngredientItem::new, new Item.Properties());
    public static final Item CREAM = registerIngredient("ingredient/cream", IngredientItem::new, new Item.Properties());
    public static final Item CUCUMBER = registerIngredient("ingredient/cucumber", IngredientItem::new, new Item.Properties());
    public static final Item DEW = registerIngredient("ingredient/dew", IngredientItem::new, new Item.Properties());
    public static final Item FICUS_MICROCARPA = registerIngredient("ingredient/ficus_microcarpa", IngredientItem::new, new Item.Properties());
    public static final Item FLOUR = registerIngredient("ingredient/flour", IngredientItem::new, new Item.Properties());
    public static final Item FLOWERS = registerIngredient("ingredient/flowers", IngredientItem::new, new Item.Properties());
    public static final Item GINKGO = registerIngredient("ingredient/ginkgo", IngredientItem::new, new Item.Properties());
    public static final Item GRAPE = registerIngredient("ingredient/grape", IngredientItem::new, new Item.Properties());
    public static final Item HAGFISH = registerIngredient("ingredient/hagfish", IngredientItem::new, new Item.Properties());
    public static final Item LEMON = registerIngredient("ingredient/lemon", IngredientItem::new, new Item.Properties());
    public static final Item LOTUS_NUTS = registerIngredient("ingredient/lotus_nuts", IngredientItem::new, new Item.Properties());
    public static final Item MOONFLOWER = registerIngredient("ingredient/moonflower", IngredientItem::new, new Item.Properties());
    public static final Item OCTOPUS = registerIngredient("ingredient/octopus", IngredientItem::new, new Item.Properties());
    public static final Item ONION = registerIngredient("ingredient/onion", IngredientItem::new, new Item.Properties());
    public static final Item PEACH = registerIngredient("ingredient/peach", IngredientItem::new, new Item.Properties());
    public static final Item PINE_NUT = registerIngredient("ingredient/pine_nut", IngredientItem::new, new Item.Properties());
    public static final Item PLUM = registerIngredient("ingredient/plum", IngredientItem::new, new Item.Properties());
    public static final Item PUFF_YO_FRUIT = registerIngredient("ingredient/puff_yo_fruit", IngredientItem::new, new Item.Properties());
    public static final Item RED_BEANS = registerIngredient("ingredient/red_beans", IngredientItem::new, new Item.Properties());
    public static final Item SALMON = registerIngredient("ingredient/salmon", IngredientItem::new, new Item.Properties());
    public static final Item SEA_URCHIN = registerIngredient("ingredient/sea_urchin", IngredientItem::new, new Item.Properties());
    public static final Item SHRIMP = registerIngredient("ingredient/shrimp", IngredientItem::new, new Item.Properties());
    public static final Item STICKY_RICE = registerIngredient("ingredient/sticky_rice", IngredientItem::new, new Item.Properties());
    public static final Item SUPREME_TUNA = registerIngredient("ingredient/supreme_tuna", IngredientItem::new, new Item.Properties());
    public static final Item SWEET_POTATO = registerIngredient("ingredient/sweet_potato", IngredientItem::new, new Item.Properties());
    public static final Item TOFU = registerIngredient("ingredient/tofu", IngredientItem::new, new Item.Properties());
    public static final Item TOMATO = registerIngredient("ingredient/tomato", IngredientItem::new, new Item.Properties());
    public static final Item TOON = registerIngredient("ingredient/toon", IngredientItem::new, new Item.Properties());
    public static final Item TREMELLA = registerIngredient("ingredient/tremella", IngredientItem::new, new Item.Properties());
    public static final Item TROUT = registerIngredient("ingredient/trout", IngredientItem::new, new Item.Properties());
    public static final Item TRUFFLE = registerIngredient("ingredient/truffle", IngredientItem::new, new Item.Properties());
    public static final Item TUNA = registerIngredient("ingredient/tuna", IngredientItem::new, new Item.Properties());
    public static final Item TWIN_LOTUS = registerIngredient("ingredient/twin_lotus", IngredientItem::new, new Item.Properties());
    public static final Item UDUMBARA = registerIngredient("ingredient/udumbara", IngredientItem::new, new Item.Properties());
    public static final Item VENISON = registerIngredient("ingredient/venison", IngredientItem::new, new Item.Properties());
    public static final Item WAGYU_BEEF = registerIngredient("ingredient/wagyu_beef", IngredientItem::new, new Item.Properties());
    public static final Item WHITE_RADISH = registerIngredient("ingredient/white_radish", IngredientItem::new, new Item.Properties());
    public static final Item WILD_BOAR_MEAT = registerIngredient("ingredient/wild_boar_meat", IngredientItem::new, new Item.Properties());

    public static Item registerIngredient(String name, Function<Item.Properties, Item> factory, Item.Properties settings) {
        Item item = RDItems.registerSimpleItem(name, factory, settings);
        INGREDIENTS.add(item);
        return item;
    }

    public static void registerItems() {

    }
}
