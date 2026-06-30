package cc.thonly.reverie_dreams.fabric.mixin.kaleidoscopecookery;

import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.TeapotRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.*;

@Pseudo
@Mixin(TeapotRecipe.class)
public class TeapotRecipeMixin {
    @Final
    @Shadow
    @Mutable
    private Ingredient ingredient;
}
