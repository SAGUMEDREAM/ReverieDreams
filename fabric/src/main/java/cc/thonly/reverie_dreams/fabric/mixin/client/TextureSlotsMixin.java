package cc.thonly.reverie_dreams.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import org.slf4j.Logger;


@Mixin(TextureSlots.Resolver.class)
public class TextureSlotsMixin {
    @WrapOperation(
            method = "resolve",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"
            )
    )
    private void reverie_dreams$muteWarn(Logger logger, String msg, Object a, Object b, Operation<Void> original) {
        if (a instanceof String strId && !strId.contains("polymerify")) {
            original.call(logger, msg, a, b);
        }
    }

}
