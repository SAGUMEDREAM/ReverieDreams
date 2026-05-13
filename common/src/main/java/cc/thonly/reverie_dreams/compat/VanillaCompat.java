package cc.thonly.reverie_dreams.compat;

import cc.thonly.reverie_dreams.api.recipe.RecipeCompatPatchesImpl;
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
            RecipeCompatPatchesImpl.Builder<KitchenRecipe> builder = RecipeCompatPatchesImpl.getOrCreateBuilder(RecipeManager.KITCHEN_TYPE);
            builder.add(Items.BROWN_MUSHROOM, Items.RED_MUSHROOM);
            builder.add(Items.EGG, Items.BROWN_EGG);
            builder.add(Items.EGG, Items.BLUE_EGG);
        });
        FoodPropertiesLoaderCallback.EVENT.register(ctx -> {
            FoodProperty property = ctx.getProperty();
            Set<Item> items = ctx.getItems();
            if (property.equals(FoodProperties.RAW)) {
                items.add(Items.BROWN_EGG);
                items.add(Items.BLUE_EGG);
            }
            if (property.equals(FoodProperties.VEGETARIAN)) {
                items.add(Items.CARROT);
                items.add(Items.BEETROOT);
            }
            if (property.equals(FoodProperties.SALTY)) {
                items.add(Items.SEA_PICKLE);
            }
            if (property.equals(FoodProperties.MEAT)) {
                items.add(Items.CHICKEN);
                items.add(Items.RABBIT);
                items.add(Items.MUTTON);
            }
            if (property.equals(FoodProperties.FRUITY)) {
                items.add(Items.APPLE);
                items.add(Items.GOLDEN_APPLE);
                items.add(Items.ENCHANTED_GOLDEN_APPLE);
                items.add(Items.MELON);
                items.add(Items.SWEET_BERRIES);
                items.add(Items.GLOW_BERRIES);
            }
            if (property.equals(FoodProperties.SWEET)) {
                items.add(Items.SWEET_BERRIES);
                items.add(Items.GLOW_BERRIES);
            }
            if (property.equals(FoodProperties.DREAMLIKE)) {
                items.add(Items.GLOW_BERRIES);
                items.add(Items.GOLDEN_APPLE);
                items.add(Items.ENCHANTED_GOLDEN_APPLE);
            }
        });
    }
}
