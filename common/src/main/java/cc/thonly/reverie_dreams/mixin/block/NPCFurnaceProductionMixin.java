package cc.thonly.reverie_dreams.mixin.block;

import cc.thonly.reverie_dreams.entity.npc.work.NPCFurnaceBatch;
import cc.thonly.reverie_dreams.entity.npc.work.NPCProduction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public class NPCFurnaceProductionMixin implements NPCProduction.FurnaceSource {
    @Unique
    private final NPCFurnaceBatch reverie_dreams$batch = new NPCFurnaceBatch();

    @Override
    public NPCFurnaceBatch reverie_dreams$productionBatch() {
        return reverie_dreams$batch;
    }

    @Inject(method = "setItem", at = @At("HEAD"))
    private void reverie_dreams$inputChanged(int slot, ItemStack stack, CallbackInfo ci) {
        if (slot == 0)
            reverie_dreams$batch.inputChanged();
    }

    @Inject(method = "setRecipeUsed", at = @At("TAIL"))
    private void reverie_dreams$cooked(RecipeHolder<?> recipe, CallbackInfo ci) {
        if (recipe != null)
            reverie_dreams$batch.cooked((AbstractFurnaceBlockEntity) (Object) this, recipe);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void reverie_dreams$save(ValueOutput output, CallbackInfo ci) {
        reverie_dreams$batch.save(output);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void reverie_dreams$load(ValueInput input, CallbackInfo ci) {
        reverie_dreams$batch.load(input);
    }
}
