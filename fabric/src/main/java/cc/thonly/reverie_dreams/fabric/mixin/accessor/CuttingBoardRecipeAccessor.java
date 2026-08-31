package cc.thonly.reverie_dreams.fabric.mixin.accessor;

import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;

@Pseudo
@Mixin(CuttingBoardRecipe.class)
public interface CuttingBoardRecipeAccessor {
    @Accessor("input")
    Ingredient reverie_dreams$getInput();

    @Accessor("input")
    void reverie_dreams$setInput(Ingredient ingredient);
}
