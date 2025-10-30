package cc.thonly.reverie_dreams.compat;

import cc.thonly.mystias_izakaya.recipe.MiRecipeManager;
import cc.thonly.mystias_izakaya.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.api.RecipeCompatPatchesCallback;
import cc.thonly.reverie_dreams.api.RecipeCompatPatchesImpl;
import net.minecraft.world.item.Items;

public class VanillaCompat {
    // 修补模组内配方兼容性
    public static void bootstrap() {
        RecipeCompatPatchesCallback.EVENT.register(() -> {
            RecipeCompatPatchesImpl.Builder<KitchenRecipe> builder = RecipeCompatPatchesImpl.getOrCreateBuilder(MiRecipeManager.KITCHEN_RECIPE);
            builder.add(Items.BROWN_MUSHROOM, Items.RED_MUSHROOM);
        });
    }
}
