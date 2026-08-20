package cc.thonly.reverie_dreams.recipe;

import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.recipe.type.KitchenRecipeType;
import cc.thonly.reverie_dreams.util.LazyList;
import cc.thonly.reverie_dreams.util.network.MCThreadHelper;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeAccess;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class IClientRecipes {
    @Getter
    private final RegistryAccess registryAccess;
    @Getter
    private final RecipeAccess recipeAccess;

    public IClientRecipes() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level != null) {
            this.recipeAccess = level.recipeAccess();
            this.registryAccess = level.registryAccess();
        } else {
            throw new NullPointerException("minecraft world must not be null.");
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseRecipe> BaseRecipeType<T> getRecipeType(Identifier key) {
        return (BaseRecipeType<T>) RecipeManager.RECIPE_TYPES.get(key);
    }

    public <T extends BaseRecipe> List<T> getRecipeTypeList(BaseRecipeType<T> type) {
//        MCThreadHelper.await(type::isAcceptNetworking);
//        return new LazyList<>(type::values);
        return type.values();
    }

    public List<KitchenRecipe> getKitchenRecipeTypeList(KitchenRecipeType.TypeInstance typeInstance) {
        return getRecipeTypeList(RecipeManager.KITCHEN_TYPE).stream()
                                                            .filter(recipe -> Objects.equals(recipe.getTypeInstance(), typeInstance))
                                                            .toList();
    }

    public <T extends BaseRecipe> Map<Identifier, T> getRecipeTypeView(BaseRecipeType<T> type) {
        return type.getRegistryView();
    }

}
