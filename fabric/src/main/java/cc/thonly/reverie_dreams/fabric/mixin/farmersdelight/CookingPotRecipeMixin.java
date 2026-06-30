package cc.thonly.reverie_dreams.fabric.mixin.farmersdelight;

import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.*;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import java.util.List;

@Pseudo
@Mixin(CookingPotRecipe.class)
public class CookingPotRecipeMixin {
    @Final
    @Shadow
    @Mutable
    private List<Ingredient> inputItems;

}
