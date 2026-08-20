package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.fabric.datagen.generator.AbstractRecipeTypeProvider;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.BarrelRecipe;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class BarrelRecipeTypeProvider extends AbstractRecipeTypeProvider {
    private final Factory<BarrelRecipe> factory = this.getOrCreateFactory(RecipeManager.BARREL_RECIPE, BarrelRecipe.class);

    public BarrelRecipeTypeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    @Override
    public void configured(HolderLookup.Provider provider) {

    }

    @Override
    public String getName() {
        return "Barrel Recipe Provider";
    }
}
