package cc.thonly.reverie_dreams.fabric.mixin.client;

import cc.thonly.reverie_dreams.logger.MyFilteredLogger;
import net.minecraft.client.resources.model.ModelManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelManager.class)
public class ModelManagerMixin {
    @Shadow
    @Mutable
    @Final
    private static Logger LOGGER;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void reverie_dreams$replaceLogger(CallbackInfo ci) {
        LOGGER = new MyFilteredLogger(LOGGER);
    }

}
