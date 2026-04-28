package cc.thonly.reverie_dreams.fabric.compat;

import cc.thonly.reverie_dreams.api.registry.FoodPropertiesLoaderCallback;
import cc.thonly.reverie_dreams.api.recipe.RecipeCompatPatchesCallback;
import cc.thonly.reverie_dreams.api.recipe.RecipeCompatPatchesImpl;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.item.RDFoodItems;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import com.phoen1x.borukvafoodexotic.item.ModItems;
import net.minecraft.world.item.Item;

import java.util.Set;

@SuppressWarnings("unchecked")
public class BorukvaFoodExoticCompatImpl {
    public static void bootstrap() {
        RecipeCompatPatchesCallback.EVENT.register(() -> {
            RecipeCompatPatchesImpl.Builder<KitchenRecipe> builder = RecipeCompatPatchesImpl.getOrCreateBuilder(RecipeManager.KITCHEN_TYPE);
            builder.add(RDIngredientItems.CHILI, ModItems.PEPPER);
            builder.add(RDFoodItems.VEGETABLE_SPECIAL, ModItems.BROCCOLI);
        });
        FoodPropertiesLoaderCallback.EVENT.register(ctx -> {
            FoodProperty property = ctx.getProperty();
            Set<Item> items = ctx.getItems();
            if (property.equals(FoodProperties.FRUITY)) {
                items.add(ModItems.APRICOT);
                items.add(ModItems.PEAR);
                items.add(ModItems.ORANGE);
                items.add(ModItems.PLUM);
                items.add(ModItems.KIWI);
                items.add(ModItems.STRAWBERRY);
            }
            if (property.equals(FoodProperties.VEGETARIAN)) {
                items.add(ModItems.PEAS);
                items.add(ModItems.SPINACH);
                items.add(ModItems.GREEN_BEAN);
                items.add(ModItems.BROCCOLI);
            }
            if (property.equals(FoodProperties.SPICY)) {
                items.add(ModItems.GARLIC);
                items.add(ModItems.PEPPER);
            }
        });
    }
}
