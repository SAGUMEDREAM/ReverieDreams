package cc.thonly.reverie_dreams.recipe.type;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.entry.BarrelRecipe;
import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.List;

public class BarrelRecipeType extends BaseRecipeType<BarrelRecipe> {
    private static BarrelRecipeType INSTANCE;
    public BarrelRecipeType() {
        INSTANCE = this;
    }

    public static synchronized BarrelRecipeType getInstance() {
        return INSTANCE;
    }

    @Override
    public void reload(ResourceManager manager) {

    }

    @Override
    public void bootstrap() {

    }

    @Override
    public List<BarrelRecipe> getMatches(List<IngredientStack> stackList) {
        return List.of();
    }

    @Override
    public Boolean isMatch(IngredientStack input, IngredientStack recipe) {
        return false;
    }

    @Override
    public Codec<BarrelRecipe> getCodec() {
        return BarrelRecipe.CODEC;
    }

    @Override
    public String getTypeId() {
        return "barrel";
    }

    @Override
    public Identifier getId() {
        return ReverieDreams.id(this.getTypeId());
    }
}
