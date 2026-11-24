package cc.thonly.reverie_dreams.mixin.client;

import cc.thonly.reverie_dreams.client.pak.PakSkinPacks;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Stream;

/**
 * The encrypted skins stay active but are not offered as a user-selectable pack.
 */
@Mixin(PackSelectionModel.class)
public class PakPackSelectionMixin {
    @Inject(method = "getSelected", at = @At("RETURN"), cancellable = true)
    private void reverie_dreams$hidePakPack(CallbackInfoReturnable<Stream<PackSelectionModel.Entry>> cir) {
        cir.setReturnValue(cir.getReturnValue().filter(entry -> !PakSkinPacks.PACK_ID.equals(entry.getId())));
    }
}
