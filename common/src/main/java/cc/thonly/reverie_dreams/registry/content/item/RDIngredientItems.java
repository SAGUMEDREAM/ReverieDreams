package cc.thonly.reverie_dreams.registry.content.item;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.registry.AliasManager;
import cc.thonly.reverie_dreams.registry.content.block.RDBambooBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import cc.thonly.reverie_dreams.registry.delegate.ItemDelegate;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Unit;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@Slf4j
@SuppressWarnings({"deprecation", "unused", "SpellCheckingInspection"})
public class RDIngredientItems {
    public static final List<ItemDelegate> INGREDIENTS = new ArrayList<>();
    public static final List<ItemDelegate> EXISTS = new ArrayList<>();
    public static final List<ItemDelegate> FISHING = new ArrayList<>();

    public static final ItemDelegate EGG = addExistedIngredient(Items.EGG);
    public static final ItemDelegate BLUE_EGG = addExistedIngredient(Items.BLUE_EGG);
    public static final ItemDelegate BROWN_EGG = addExistedIngredient(Items.BROWN_EGG);
    public static final ItemDelegate PORKCHOP = addExistedIngredient(Items.PORKCHOP);
    public static final ItemDelegate BEEF = addExistedIngredient(Items.BEEF);
    public static final ItemDelegate VENISON = registerIngredient("ingredient/venison", 3, 1.8f);
    public static final ItemDelegate WILD_BOAR_MEAT = registerIngredient("ingredient/wild_boar_meat", 3, 1.8f);
    public static final ItemDelegate TOFU = registerIngredient("ingredient/tofu", 3, 2.8f);
    public static final ItemDelegate POTATO = addExistedIngredient(Items.POTATO);
    public static final ItemDelegate ONION = registerIngredient("ingredient/onion", 1, 0.4f);
    public static final ItemDelegate PUMPKIN = addExistedIngredient(Items.PUMPKIN);
    public static final ItemDelegate WHITE_RADISH = registerIngredient("ingredient/white_radish", 2, 1.2f);
    public static final ItemDelegate KELP = addExistedIngredient(Items.KELP);
    public static final ItemDelegate TROUT = registerIngredient("ingredient/trout", 2, 0.8f);
    public static final ItemDelegate HAGFISH = registerIngredient("ingredient/hagfish", 2, 1.0f);
    public static final ItemDelegate SALMON = registerIngredient("ingredient/salmon", 2, 0.8f);
    public static final ItemDelegate TUNA = registerIngredient("ingredient/tuna", 2, 1.2f);
    public static final ItemDelegate BLACK_PORK = registerIngredient("ingredient/black_pork", 3, 2.0f);
    public static final ItemDelegate WAGYU_BEEF = registerIngredient("ingredient/wagyu_beef", 3, 2.4f);
    public static final ItemDelegate BROWN_MUSHROOM = addExistedIngredient(Items.BROWN_MUSHROOM);
    public static final ItemDelegate RED_MUSHROOM = addExistedIngredient(Items.RED_MUSHROOM);
    public static final ItemDelegate TRUFFLE = registerIngredient("ingredient/truffle", 1, 0.6f);
    public static final ItemDelegate SUPREME_TUNA = registerIngredient("ingredient/supreme_tuna", 3, 2.2f);
    public static final ItemDelegate PUFFERFISH = addExistedIngredient(Items.PUFFERFISH);
    public static final ItemDelegate PEACH = registerIngredient("ingredient/peach", 4, 2.4f);
    public static final ItemDelegate GINKGO = registerIngredient("ingredient/ginkgo", 1, 0.8f);
    public static final ItemDelegate SHRIMP = registerIngredient("ingredient/shrimp", 2, 0.8f);
    public static final ItemDelegate HONEY_BOTTLE = addExistedIngredient(Items.HONEY_BOTTLE);
    public static final ItemDelegate CICADA_SHELL = registerIngredient("ingredient/cicada_shell", 0, 0.0f);
    public static final ItemDelegate UDUMBARA = registerIngredient("ingredient/udumbara", 0, 0.0f);
    public static final ItemDelegate DEW = registerIngredient("ingredient/dew", 0, 0.0f);
    public static final ItemDelegate BAMBOO_SHOOTS = addExistedIngredient(RDBambooBlocks.LARGE_BAMBOO_SHOOT);
    public static final ItemDelegate BUTTER = registerIngredient("ingredient/butter", ingredientFactory(1, 1.2f), new Item.Properties().usingConvertsTo(Items.BOWL));
    public static final ItemDelegate FLOUR = registerIngredient("ingredient/flour", 0, 0.0f);
    public static final ItemDelegate BAMBOO = addExistedIngredient(Items.BAMBOO);
    public static final ItemDelegate RICE = registerIngredient("ingredient/rice", 1, 0.4f);
    public static final ItemDelegate STICKY_RICE = registerIngredient("ingredient/sticky_rice", 1, 0.4f);
    public static final ItemDelegate SOYBEAN = registerIngredient("ingredient/soybean", 1, 0.4f);
    public static final ItemDelegate SOY_MILK = registerIngredient("ingredient/soy_milk", 1, 0.8f);
    public static final ItemDelegate MOONFLOWER = registerIngredient("ingredient/moonflower", 0, 0.0f);
    public static final ItemDelegate MAGIC_ICE_BLOCK = addExistedIngredient(RDBlocks.MAGIC_ICE_BLOCK);
    public static final ItemDelegate CHILI = registerIngredient("ingredient/chili", 1, 0.2f);
    public static final ItemDelegate GRAPE = registerIngredient("ingredient/grape", 3, 1.8f);

    // DLC001
    public static final ItemDelegate CUCUMBER = registerIngredient("ingredient/cucumber", 2, 0.8f);
    public static final ItemDelegate OCTOPUS = registerIngredient("ingredient/octopus", 2, 1.2f);
    public static final ItemDelegate SEA_URCHIN = registerIngredient("ingredient/sea_urchin", 2, 1.4f);
    public static final ItemDelegate BLACK_SALT = registerIngredient("ingredient/black_salt", 0, 0.0f);
    public static final ItemDelegate CREAM = registerIngredient("ingredient/cream", 1, 1.0f);
    public static final ItemDelegate CRAB = registerIngredient("ingredient/crab", 2, 1.0f);

    // DLC002
    public static final ItemDelegate TWIN_LOTUS = registerIngredient("ingredient/twin_lotus", 0, 0.0f);
    public static final ItemDelegate LEMON = registerIngredient("ingredient/lemon", 1, 0.4f);
    public static final ItemDelegate CHEESE = registerIngredient("ingredient/cheese", 3, 3.2f);

    // DLC003
    public static final ItemDelegate LOTUS_NUTS = registerIngredient("ingredient/lotus_nuts", 2, 1.6f);
    public static final ItemDelegate SWEET_POTATO = registerIngredient("ingredient/sweet_potato", 3, 2.6f);
    public static final ItemDelegate PINE_NUT = registerIngredient("ingredient/pine_nut", 2, 2.4f);
    public static final ItemDelegate CHESTNUT = registerIngredient("ingredient/chestnut", 3, 2.4f);

    // DLC004
    public static final ItemDelegate PLUM = registerIngredient("ingredient/plum", 2, 0.8f);
    public static final ItemDelegate RED_BEANS = registerIngredient("ingredient/red_beans", 1, 0.6f);
    public static final ItemDelegate FLOWERS = registerIngredient("ingredient/flowers", 0, 0.0f);
    public static final ItemDelegate TOON = registerIngredient("ingredient/toon", 1, 0.4f);
    public static final ItemDelegate TOMATO = registerIngredient("ingredient/tomato", 2, 1.2f);

    // DLC005
    public static final ItemDelegate COCOA_BEANS = addExistedIngredient(Items.COCOA_BEANS);
    public static final ItemDelegate BROCCOLI = registerIngredient("ingredient/broccoli", 2, 1.4f);
    public static final ItemDelegate PUFF_YO_FRUIT = registerIngredient("ingredient/puff_yo_fruit", 2, 1.0f);
    public static final ItemDelegate FICUS_MICROCARPA = registerIngredient("ingredient/ficus_microcarpa", 0, 0.0f);
    public static final ItemDelegate TREMELLA = registerIngredient("ingredient/tremella", 1, 0.6f);
    public static final ItemDelegate CAPSAICIN = registerIngredient("ingredient/capsaicin", 0, 0.0f);

    public static void initialize() {
        AliasManager.register(Registries.ITEM, ReverieDreams.id("ingredient/bamboo_shoots"), ReverieDreams.id("large_bamboo_shoot"));
        FISHING.addAll(Arrays.asList(SHRIMP, CRAB, SALMON, TROUT, TUNA, SUPREME_TUNA));
    }

    public static ItemDelegate registerIngredient(String name, Function<Item.Properties, Item> factory, Item.Properties settings) {
        ItemDelegate item = RDItems.registerSimpleItem(name, factory, settings);
//        registerComponent(item);

        INGREDIENTS.add(item);
        return item;
    }

    public static ItemDelegate addExistedIngredient(Item item) {
        ItemDelegate itemDelegate = ItemDelegate.of(item.builtInRegistryHolder());
        EXISTS.add(itemDelegate);
        INGREDIENTS.add(itemDelegate);
        return itemDelegate;
    }

    public static ItemDelegate addExistedIngredient(Holder<Item> item) {
        ItemDelegate itemDelegate = ItemDelegate.of(item);
        EXISTS.add(itemDelegate);
        INGREDIENTS.add(itemDelegate);
        return itemDelegate;
    }

    public static ItemDelegate addExistedIngredient(BlockDelegate blockDelegate) {
        if (blockDelegate.blockItem() == null) {
            log.error("No Holder bound to {} found", blockDelegate);
            return null;
        }
        ItemDelegate itemDelegate = blockDelegate.blockItem();
        EXISTS.add(itemDelegate);
        INGREDIENTS.add(itemDelegate);
        return itemDelegate;
    }

//    public static void registerComponent(Item item) {
//        DefaultItemComponentEvents.MODIFY.register(context -> {
//            context.modify(item, builder -> builder.set(RDDataComponents.INGREDIENT_ITEM_TYPE, Unit.INSTANCE));
//        });
//    }

    private static ItemDelegate registerIngredient(String name, int nutrition, float saturation) {
        return registerIngredient(name, ingredientFactory(nutrition, saturation), new Item.Properties());
    }

    public static Function<Item.Properties, Item> ingredientFactory(int nutrition, float saturation) {
        return props -> new Item(props.component(RDDataComponentTypes.INGREDIENT_ITEM_TYPE.value(), Unit.INSTANCE)
                .food(new FoodProperties(nutrition, saturation, false)));
    }


}
