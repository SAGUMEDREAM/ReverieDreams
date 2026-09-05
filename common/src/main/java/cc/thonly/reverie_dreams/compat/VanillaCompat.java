package cc.thonly.reverie_dreams.compat;

import cc.thonly.reverie_dreams.api.recipe.PatchBuilder;
import cc.thonly.reverie_dreams.api.recipe.RecipeCompatPatches;
import cc.thonly.reverie_dreams.api.recipe.callback.RecipeCompatPatchesCallback;
import cc.thonly.reverie_dreams.api.registry.callback.FoodPropertiesLoaderCallback;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Set;

public class VanillaCompat {
    public static void bootstrap() {
        RecipeCompatPatchesCallback.EVENT.register(() -> {
            PatchBuilder<KitchenRecipe> builder = RecipeCompatPatches.getOrCreateBuilder(RecipeManager.KITCHEN_TYPE);
            builder.add(Items.BROWN_MUSHROOM, Items.RED_MUSHROOM);
            builder.add(Items.EGG, Items.BROWN_EGG);
            builder.add(Items.EGG, Items.BLUE_EGG);
            builder.add(Items.BEEF, Items.COOKED_BEEF);
            builder.add(Items.CHICKEN, Items.COOKED_CHICKEN);
            builder.add(Items.MUTTON, Items.COOKED_MUTTON);
            builder.add(Items.PORKCHOP, Items.COOKED_PORKCHOP);
            builder.add(Items.POTATO, Items.BAKED_POTATO);
        });
        FoodPropertiesLoaderCallback.EVENT.register(ctx -> {
            FoodProperty property = ctx.getProperty();
            Set<Item> items = ctx.getItems();
            if (property.equals(FoodProperties.TOXIC)) {
                items.add(Items.POISONOUS_POTATO);
            }
            if (property.equals(FoodProperties.BIZARRE)) {
                items.add(Items.ROTTEN_FLESH);
            }
            if (property.equals(FoodProperties.RAW)) {
                items.add(Items.BROWN_EGG);
                items.add(Items.BLUE_EGG);
                items.add(Items.TURTLE_EGG);
                items.add(Items.SNIFFER_EGG);
            }
            if (property.is(FoodProperties.GOURMET)) {
                items.add(Items.TURTLE_EGG);
                items.add(Items.SNIFFER_EGG);
            }
            if (property.is(FoodProperties.AQUATIC_PRODUCTS)) {
                items.add(Items.TURTLE_EGG);
                items.add(Items.SNIFFER_EGG);
            }
            if (property.equals(FoodProperties.VEGETARIAN)) {
                items.add(Items.CARROT);
                items.add(Items.BEETROOT);
                items.add(Items.DRIED_KELP);
            }
            if (property.equals(FoodProperties.SALTY)) {
                items.add(Items.SEA_PICKLE);
            }
            if (property.equals(FoodProperties.MEAT)) {
                items.add(Items.CHICKEN);
                items.add(Items.COOKED_CHICKEN);
                items.add(Items.RABBIT);
                items.add(Items.COOKED_RABBIT);
                items.add(Items.MUTTON);
                items.add(Items.COOKED_MUTTON);
                items.add(Items.COOKED_PORKCHOP);
            }
            if (property.equals(FoodProperties.FRUITY)) {
                items.add(Items.APPLE);
                items.add(Items.GOLDEN_APPLE);
                items.add(Items.ENCHANTED_GOLDEN_APPLE);
                items.add(Items.MELON);
                items.add(Items.SWEET_BERRIES);
                items.add(Items.GLOW_BERRIES);
                items.add(Items.CHORUS_FRUIT);
            }
            if (property.equals(FoodProperties.SWEET)) {
                items.add(Items.SWEET_BERRIES);
                items.add(Items.GLOW_BERRIES);
                items.add(Items.CAKE);
                items.add(Items.COOKIE);
                items.add(Items.SUGAR);
                items.add(Items.SUGAR_CANE);
            }
            if (property.equals(FoodProperties.DREAMLIKE)) {
                items.add(Items.GLOW_BERRIES);
                items.add(Items.GOLDEN_APPLE);
                items.add(Items.ENCHANTED_GOLDEN_APPLE);
            }
            if (property.equals(FoodProperties.AQUATIC_PRODUCTS)) {
                items.add(Items.COD);
                items.add(Items.SALMON);
                items.add(Items.TROPICAL_FISH);
            }
        });
    }
}
