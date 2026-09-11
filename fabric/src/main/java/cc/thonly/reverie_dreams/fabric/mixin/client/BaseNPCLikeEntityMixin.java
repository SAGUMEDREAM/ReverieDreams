package cc.thonly.reverie_dreams.fabric.mixin.client;

import cc.thonly.reverie_dreams.entity.npc.AbstractNPCEntity;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.fabric.integration.sparkle.SparkleClient;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseNPCLikeEntity.class)
public abstract class BaseNPCLikeEntityMixin extends AbstractNPCEntity {
    protected BaseNPCLikeEntityMixin(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "onClientRemoval", at = @At("TAIL"))
    public void reverie_dreams$client$onRemove(CallbackInfo ci) {
        SparkleClient.remove((BaseNPCLikeEntity) (Object) this);
    }
}
