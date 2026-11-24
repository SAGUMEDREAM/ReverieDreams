package cc.thonly.reverie_dreams.recipe.entry;

import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;

/** A single consumed item. Work is clicks for grinding, active ticks for drying. */
public final class WorkshopProcessRecipe extends BaseRecipe {
    public static final Codec<WorkshopProcessRecipe> CODEC=RecordCodecBuilder.create(i->i.group(
            Codec.STRING.fieldOf("ingredient").forGetter(WorkshopProcessRecipe::ingredient),
            IngredientStack.CODEC.fieldOf("output").forGetter(WorkshopProcessRecipe::getOutput),
            Codec.intRange(1,72000).optionalFieldOf("work",8).forGetter(WorkshopProcessRecipe::work)
    ).apply(i,WorkshopProcessRecipe::new));
    private final String ingredient;
    private final IngredientStack output;
    private final int work;
    public WorkshopProcessRecipe(String ingredient,IngredientStack output,int work) {
        Identifier.parse(ingredient.startsWith("#")?ingredient.substring(1):ingredient);
        if(output.isEmpty()||output.getCount()<1||output.getCount()>64||work<1||work>72000)throw new IllegalArgumentException("Invalid workshop recipe");
        this.ingredient=ingredient;this.output=output.copy();this.work=work;
    }
    public String ingredient(){return ingredient;}
    public int work(){return work;}
    @Override public IngredientStack getOutput(){return output.copy();}
    public boolean matches(ItemStack stack){
        if(stack.isEmpty())return false;
        return ingredient.startsWith("#")?stack.is(TagKey.create(Registries.ITEM,Identifier.parse(ingredient.substring(1))))
                :BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(Identifier.parse(ingredient));
    }
}
