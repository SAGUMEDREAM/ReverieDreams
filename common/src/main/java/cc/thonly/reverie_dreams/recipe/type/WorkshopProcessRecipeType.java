package cc.thonly.reverie_dreams.recipe.type;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.entry.WorkshopProcessRecipe;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import java.util.Comparator;
import java.util.List;
import cc.thonly.reverie_dreams.recipe.RecipeResourceLoader;

/** Uses the mod's existing reload, datapack override and recipe-sync lifecycle. */
public final class WorkshopProcessRecipeType extends BaseRecipeType<WorkshopProcessRecipe> {
    private final String kind;
    public WorkshopProcessRecipeType(String kind){this.kind=kind;}
    @Override public void bootstrap(){}
    @Override public List<WorkshopProcessRecipe> getMatches(List<IngredientStack> stacks){
        if(stacks.isEmpty())return List.of();
        return values().stream().filter(r->r.matches(stacks.getFirst().build()))
                .sorted(Comparator.comparing((WorkshopProcessRecipe r)->r.ingredient().startsWith("#")).thenComparing(r->r.getId().toString())).toList();
    }
    @Override public Boolean isMatch(IngredientStack input,IngredientStack expected){return input.is(expected.getItem())&&input.getCount()>=expected.getCount();}
    @Override public Codec<WorkshopProcessRecipe> getCodec(){return WorkshopProcessRecipe.CODEC;}
    @Override public String getTypeId(){return kind;}
    @Override public Identifier getId(){return ReverieDreams.id(kind);}
    @Override public void reload(ResourceManager manager){
        RecipeResourceLoader.load(manager, kind + "_recipe",
                id -> true, getCodec(), this::add);
    }
}
