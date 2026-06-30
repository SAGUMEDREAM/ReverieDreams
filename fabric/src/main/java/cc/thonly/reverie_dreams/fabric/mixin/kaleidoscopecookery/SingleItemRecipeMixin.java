package cc.thonly.reverie_dreams.fabric.mixin.kaleidoscopecookery;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import org.spongepowered.asm.mixin.*;

@Pseudo
@Mixin(SingleItemRecipe.class)
public class SingleItemRecipeMixin {
    @Shadow
    @Mutable
    @Final
    private Ingredient input;
}
