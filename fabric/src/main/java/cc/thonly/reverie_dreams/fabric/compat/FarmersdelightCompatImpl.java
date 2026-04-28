package cc.thonly.reverie_dreams.fabric.compat;

import cc.thonly.reverie_dreams.api.registry.FoodPropertiesLoaderCallback;
import cc.thonly.reverie_dreams.api.recipe.RecipeCompatPatchesCallback;
import cc.thonly.reverie_dreams.api.recipe.RecipeCompatPatchesImpl;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.Set;

public class FarmersdelightCompatImpl {
    public static void bootstrap() {
        RecipeCompatPatchesCallback.EVENT.register(() -> {
            RecipeCompatPatchesImpl.Builder<KitchenRecipe> builder = RecipeCompatPatchesImpl.getOrCreateBuilder(RecipeManager.KITCHEN_TYPE);
            builder.add(RDIngredientItems.TOMATO, ModItems.TOMATO.get());
            builder.add(RDIngredientItems.ONION, ModItems.ONION.get());
            builder.add(Items.PORKCHOP, ModItems.BACON.get());
            builder.add(Items.PUMPKIN, ModItems.PUMPKIN_SLICE.get());
            builder.add(Items.MUTTON, ModItems.MUTTON_CHOPS.get());
            builder.add(Items.COD, ModItems.COD_SLICE.get());
            builder.add(Items.SALMON, ModItems.SALMON_SLICE.get());
            builder.add(Items.BEEF, ModItems.MINCED_BEEF.get());
            builder.add(RDIngredientItems.WAGYU_BEEF, ModItems.MINCED_BEEF.get());
        });
        FoodPropertiesLoaderCallback.EVENT.register(ctx -> {
            FoodProperty property = ctx.getProperty();
            Set<Item> items = ctx.getItems();
            if (property.equals(FoodProperties.VEGETARIAN)) {
                items.add(ModItems.CABBAGE.get());
                items.add(ModItems.CABBAGE_LEAF.get());
                items.add(ModItems.TOMATO.get());
                items.add(ModItems.ONION.get());
            }
            if (property.equals(FoodProperties.UMAMI)) {
                items.add(ModItems.ONION.get());
            }
            if (property.equals(FoodProperties.AQUATIC_PRODUCTS)) {
                items.add(ModItems.COD_SLICE.get());
                items.add(ModItems.SALMON_SLICE.get());
            }
        });
    }
}
