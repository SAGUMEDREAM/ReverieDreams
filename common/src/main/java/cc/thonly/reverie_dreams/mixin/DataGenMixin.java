package cc.thonly.reverie_dreams.mixin;

import cc.thonly.reverie_dreams.util.PlatformContext;
import net.minecraft.data.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class DataGenMixin {
    @Inject(method = "main", at = @At("HEAD"), remap = false)
    private static void reverie_dreams$$main(String[] args, CallbackInfo ci) {
        PlatformContext.IS_DATAGEN_MODE = true;
    }
}
