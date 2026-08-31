package cc.thonly.reverie_dreams.fabric.mixin.client;

import cc.thonly.reverie_dreams.client.CapturedEntity;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Unique
    private static final Map<EntityRenderState, CapturedEntity> CAPTURED_ENTITIES = Collections.synchronizedMap(new IdentityHashMap<>());

//    @Inject(method = "extractEntity", at = @At("RETURN"))
//    private <E extends Entity> void reverie_dreams$captureEntity(E entity,
//                                                                 float partialTicks,
//                                                                 CallbackInfoReturnable<EntityRenderState> cir) {
////        EntityRenderState state = cir.getReturnValue();
////        if (state != null) {
////            int packedLight = ((EntityRenderDispatcher)(Object)this).getPackedLightCoords(entity, partialTicks);
////            CAPTURED_ENTITIES.put(state, new CapturedEntity(entity, partialTicks, packedLight));
////        }
//    }
}
