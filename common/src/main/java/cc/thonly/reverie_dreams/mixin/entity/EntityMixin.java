package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.item.armor.WaterproofArmor;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract boolean isAlive();

    @Shadow
    public abstract Level level();

    @Shadow
    public abstract void gameEvent(Holder<GameEvent> event, @Nullable Entity entity);

    @Inject(method = {"isInWater", "isUnderWater", "isInWaterOrRain"}, at = @At("HEAD"), cancellable = true)
    public void modifyInWater(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = ((Entity)(Object) this);
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }
        if (WaterproofArmor.hasEquipment(livingEntity)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "updateInWaterStateAndDoWaterCurrentPushing", at = @At("HEAD"), cancellable = true)
    public void modifyInWaterTick(CallbackInfo ci) {
        Entity entity = ((Entity)(Object) this);
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }
        if (WaterproofArmor.hasEquipment(livingEntity)) {
            ci.cancel();
        }
    }

    @Inject(method = "clearFire", at = @At("HEAD"), cancellable = true)
    public void modifyClearFire(CallbackInfo ci) {
        Entity entity = ((Entity)(Object) this);
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }
        if (WaterproofArmor.hasEquipment(livingEntity)) {
            ci.cancel();
        }
    }
}
