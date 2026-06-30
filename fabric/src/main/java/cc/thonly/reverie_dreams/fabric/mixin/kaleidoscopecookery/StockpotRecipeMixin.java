package cc.thonly.reverie_dreams.fabric.mixin.kaleidoscopecookery;

import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.*;

@Pseudo
@Mixin(StockpotRecipe.class)
public class StockpotRecipeMixin {
    @Final
    @Shadow
    @Mutable
    private NonNullList<Ingredient> ingredients;
}
