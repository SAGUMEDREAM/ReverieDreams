package cc.thonly.reverie_dreams.entity.interfaces;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import net.minecraft.world.entity.LivingEntity;

public interface Yousei {
    default void onEntityTick() {
        if (!(this instanceof BaseNPCLikeEntity pThis)) {
            return;
        }

        LivingEntity target = pThis.getTarget();
        if (target == null) {
            return;
        }

        if (pThis.distanceToSqr(target) > 32 * 32) {
            pThis.setTarget(null);
        }
    }

}
