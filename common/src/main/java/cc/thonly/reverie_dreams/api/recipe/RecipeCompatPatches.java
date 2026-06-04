package cc.thonly.reverie_dreams.api.recipe;

import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.Identifier;

import java.util.Map;

@Slf4j
@SuppressWarnings("unchecked")
public class RecipeCompatPatches {

    public static synchronized <R extends BaseRecipe> PatchBuilder<R> getOrCreateBuilder(BaseRecipeType<R> baseRecipeType) {
        return (PatchBuilder<R>) PatchBuilder.INSTANCE.computeIfAbsent(baseRecipeType, (x) -> new PatchBuilder<>(baseRecipeType));
    }

    public static synchronized void removeAll(BaseRecipeType<?> recipeType) {
        PatchBuilder<?> builder = getOrCreateBuilder(recipeType);
        Map<Identifier, BaseRecipe> registries = builder.getRegistries();
        registries.clear();
    }

    public static synchronized void apply(BaseRecipeType<?> recipeType) {
        PatchBuilder<?> builder = getOrCreateBuilder(recipeType);
        Map<Identifier, BaseRecipe> registries = builder.getRegistries();
        for (Map.Entry<Identifier, BaseRecipe> registry : registries.entrySet()) {
            log.info("Registered compatibility recipe {}", registry.getKey().toString());
            recipeType.add(registry.getKey(), registry.getValue());
        }
    }

}
