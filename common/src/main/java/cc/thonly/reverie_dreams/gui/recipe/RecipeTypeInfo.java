package cc.thonly.reverie_dreams.gui.recipe;

import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RecipeTypeInfo {
    private final String name;
    private final BaseRecipeType<?> recipeType;
    private final GuiStackBuilder getter;
}
