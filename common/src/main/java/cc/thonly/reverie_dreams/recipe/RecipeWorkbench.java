package cc.thonly.reverie_dreams.recipe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
public class RecipeWorkbench<R extends BaseRecipe> {
    private final String name;
    private final List<Block> block;
    private final BaseRecipeType<R> recipeType;
    private final SaveFunction<R> function;

    public RecipeWorkbench(String name, Block block, BaseRecipeType<R> recipeType, SaveFunction<R> function) {
        this.name = name;
        this.block = new ArrayList<>(List.of(block));
        this.recipeType = recipeType;
        this.function = function;
    }

    public interface SaveFunction<R extends BaseRecipe> {
        ItemStack save(RegistryAccess registryAccess, Identifier recipeId, RecipeWorkbench<R> self);
    }
}
