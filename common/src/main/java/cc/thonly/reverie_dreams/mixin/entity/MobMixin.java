package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.item.armor.CrownOfTheUnderworldItem;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Mob.class})
public class MobMixin {
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    public void reverie_dreams$ignoreAttack(LivingEntity target, CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (target == null) {
            return;
        }
        if (CrownOfTheUnderworldItem.hasEquipment(target) && mob.is(EntityTypeTags.UNDEAD)) {
            ci.cancel();
        }
    }
}
