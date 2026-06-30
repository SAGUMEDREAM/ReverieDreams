package cc.thonly.reverie_dreams.mixin.client;

import cc.thonly.reverie_dreams.client.CapturedEntity;
import cc.thonly.reverie_dreams.client.renderer.entity.EmptyRenderer;
import cc.thonly.reverie_dreams.client.renderer.entity.state.NPCAvatarRenderState;
import cc.thonly.reverie_dreams.util.PlatformContext;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Shadow
    private Map<EntityType<?>, EntityRenderer<?, ?>> renderers;
    @Unique
    private EmptyRenderer reverie_dreams$emptyRenderer = null;

    @SuppressWarnings("unchecked")
    @Inject(method = "getRenderer(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;)Lnet/minecraft/client/renderer/entity/EntityRenderer;",
            at = @At("HEAD"), cancellable = true)
    public <S extends EntityRenderState> void reverie_dreams$getNPCRenderer(S entityRenderState,
                                                                            CallbackInfoReturnable<EntityRenderer<?, ? super S>> cir) {
        if (entityRenderState instanceof NPCAvatarRenderState) {
            cir.setReturnValue((EntityRenderer<?, ? super S>) this.renderers.get(entityRenderState.entityType));
            cir.cancel();
        }
    }

    @Inject(method = "getRenderer(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;)Lnet/minecraft/client/renderer/entity/EntityRenderer;",
            at = @At("RETURN"), cancellable = true)
    public <S extends EntityRenderState> void reverie_dreams$getEmptyRenderer(S entityRenderState,
                                                                              CallbackInfoReturnable<EntityRenderer<?, ? super S>> cir) {
        if (PlatformContext.isDevMode() && cir.getReturnValue() == null) {
            cir.setReturnValue(this.reverie_dreams$getEmptyRenderer());
        }
    }

    @Unique
    private EmptyRenderer reverie_dreams$getEmptyRenderer() {
        if (this.reverie_dreams$emptyRenderer == null) {
            EmptyRenderer result = null;
            for (EntityRenderer<?, ?> value : this.renderers.values()) {
                if (value instanceof EmptyRenderer renderer) {
                    result = renderer;
                    break;
                }
            }
            if (result == null) {
                return null;
            }
            this.reverie_dreams$emptyRenderer = result;
        }
        return this.reverie_dreams$emptyRenderer;
    }
}
