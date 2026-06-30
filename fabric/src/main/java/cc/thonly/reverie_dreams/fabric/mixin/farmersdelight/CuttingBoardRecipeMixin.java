package cc.thonly.reverie_dreams.fabric.mixin.farmersdelight;

import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.*;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;

import java.util.List;

@Pseudo
@Mixin(CuttingBoardRecipe.class)
public class CuttingBoardRecipeMixin {
    @Final
    @Shadow
    @Mutable
    private Ingredient input;
}
