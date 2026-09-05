package cc.thonly.reverie_dreams.fabric.compat;

import cc.thonly.reverie_dreams.api.recipe.PatchBuilder;
import cc.thonly.reverie_dreams.api.recipe.RecipeCompatPatches;
import cc.thonly.reverie_dreams.api.recipe.callback.RecipeCompatPatchesCallback;
import cc.thonly.reverie_dreams.api.registry.callback.FoodPropertiesLoaderCallback;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import com.phoen1x.items.ODItems;
import net.minecraft.world.item.Item;

import java.util.Set;

public class OceansdelightCompatImpl {
    public static void bootstrap() {
        RecipeCompatPatchesCallback.EVENT.register(() -> {
            PatchBuilder<KitchenRecipe> builder = RecipeCompatPatches.getOrCreateBuilder(RecipeManager.KITCHEN_TYPE);

        });
        FoodPropertiesLoaderCallback.EVENT.register(ctx -> {
            FoodProperty property = ctx.getProperty();
            Set<Item> items = ctx.getItems();
            if (property.is(FoodProperties.UMAMI)) {
                items.add(ODItems.TENTACLES);
                items.add(ODItems.CUT_TENTACLES);
                items.add(ODItems.SQUID_RINGS);
                items.add(ODItems.GUARDIAN);
                items.add(ODItems.GUARDIAN_TAIL);
                items.add(ODItems.ELDER_GUARDIAN_SLICE);
                items.add(ODItems.FUGU_SLICE);
            }
            if (property.is(FoodProperties.AQUATIC_PRODUCTS)) {
                items.add(ODItems.TENTACLES);
                items.add(ODItems.CUT_TENTACLES);
                items.add(ODItems.SQUID_RINGS);
                items.add(ODItems.FUGU_SLICE);
            }
        });
    }
}
