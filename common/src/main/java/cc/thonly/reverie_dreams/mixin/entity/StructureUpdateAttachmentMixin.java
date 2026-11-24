package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.world.gensokyo.update.StructureUpdateGate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockAttachedEntity.class)
public abstract class StructureUpdateAttachmentMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void reverie$holdAuthoredAttachment(CallbackInfo ci) {
        if (StructureUpdateGate.freezes((Entity) (Object) this))
            ci.cancel();
    }
}
