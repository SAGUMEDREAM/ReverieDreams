package cc.thonly.reverie_dreams.mixin.booklet;

import eu.pb4.booklet.impl.datagen.DataGenInit;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DataGenInit.class)
@Pseudo
public class DataGenInitMixin {
    @Inject(method = "onInitializeDataGenerator", at = @At("HEAD"), cancellable = true)
    public void cancel(FabricDataGenerator fabricDataGenerator, CallbackInfo ci) {
        ci.cancel();
    }
}
