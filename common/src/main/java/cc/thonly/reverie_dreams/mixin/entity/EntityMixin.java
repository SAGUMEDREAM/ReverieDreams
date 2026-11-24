package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.item.armor.WaterproofArmor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    /**
     * 整套防水衣只屏蔽“淋雨”判定，不影响游泳、水中呼吸和灭火逻辑。
     */
    @Inject(method = "isInWaterOrRain", at = @At("HEAD"), cancellable = true)
    public void modifyInWaterOrRain(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = ((Entity) (Object) this);
        if (entity instanceof LivingEntity livingEntity
                && WaterproofArmor.hasFullSet(livingEntity)
                && !entity.isInWater()
                && entity.level().isRainingAt(entity.blockPosition())) {
            cir.setReturnValue(false);
        }
    }
}
