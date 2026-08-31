package cc.thonly.reverie_dreams.fabric.mixin.accessor;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SingleItemRecipe.class)
public interface SingleItemRecipeAccessor {
    @Accessor("input")
    Ingredient reverie_dreams$getInput();

    @Accessor("input")
    void reverie_dreams$setInput(Ingredient input);
}
