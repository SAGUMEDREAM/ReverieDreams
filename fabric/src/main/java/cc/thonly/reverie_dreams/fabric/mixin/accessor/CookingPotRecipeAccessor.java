package cc.thonly.reverie_dreams.fabric.mixin.accessor;

import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import java.util.List;

@Pseudo
@Mixin(CookingPotRecipe.class)
public interface CookingPotRecipeAccessor {
    @Accessor("inputItems")
    void reverie_dreams$setInputItems(List<Ingredient> inputItems);

    @Accessor("inputItems")
    List<Ingredient> reverie_dreams$getInputItems();
}
