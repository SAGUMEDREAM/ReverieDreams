package cc.thonly.reverie_dreams.mixin.client;

import cc.thonly.reverie_dreams.ReverieDreams;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class StandingAndWallBlockItemMixin {
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    public void cancelPolymerItemPlace(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack itemStack = context.getItemInHand();
        Level level = context.getLevel();
        Identifier polymerIdentifier = PolymerItemUtils.getPolymerIdentifier(itemStack);
        if (itemStack.getItem() instanceof StandingAndWallBlockItem && polymerIdentifier != null && polymerIdentifier.getNamespace().equals(ReverieDreams.MOD_ID)) {
            if (level.isClientSide()) {
                cir.setReturnValue(InteractionResult.FAIL);
            }
        }
    }
}
