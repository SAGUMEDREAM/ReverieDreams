package cc.thonly.reverie_dreams.fabric.compat;

import cc.thonly.reverie_dreams.api.recipe.PatchBuilder;
import cc.thonly.reverie_dreams.api.registry.callback.FoodPropertiesLoaderCallback;
import cc.thonly.reverie_dreams.api.recipe.callback.RecipeCompatPatchesCallback;
import cc.thonly.reverie_dreams.api.recipe.RecipeCompatPatches;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import com.axperty.moredelight.registry.ItemRegistry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Set;

public class MoreDelightCompatImpl {
    public static void bootstrap() {
        RecipeCompatPatchesCallback.EVENT.register(() -> {
            PatchBuilder<KitchenRecipe> builder = RecipeCompatPatches.getOrCreateBuilder(RecipeManager.KITCHEN_TYPE);
            builder.add(ItemRegistry.DICED_POTATOES, Items.POTATO);
        });
        FoodPropertiesLoaderCallback.EVENT.register(ctx -> {
            FoodProperty property = ctx.getProperty();
            Set<Item> items = ctx.getItems();
            if (property.equals(FoodProperties.VEGETARIAN)) {
                items.add(ItemRegistry.DICED_POTATOES);
            }
            if (property.equals(FoodProperties.HOMESTYLE)) {
                items.add(ItemRegistry.DICED_POTATOES);
            }
        });
    }
}
