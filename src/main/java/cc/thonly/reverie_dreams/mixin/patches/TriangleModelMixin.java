package cc.thonly.reverie_dreams.mixin.patches;

import de.tomalbrc.cameraobscura.render.model.triangle.TriangleModel;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TriangleModel.class, remap = false)
public class TriangleModelMixin {
//    @Redirect(method = "intersect", at = @At(
//            value = "INVOKE",
//            target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V")
//    )
//    public void cancelError(Logger instance, String s, Object o) {
//
//    }
}
