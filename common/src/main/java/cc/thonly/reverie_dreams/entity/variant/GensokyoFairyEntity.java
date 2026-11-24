package cc.thonly.reverie_dreams.entity.variant;

import com.geckolib.animatable.GeoEntity;
import net.minecraft.world.entity.LivingEntity;

public interface GensokyoFairyEntity extends GeoEntity {
    GensokyoFairyActions fairyActions();

    static void beginRangedAttack(LivingEntity entity) {
        if (entity instanceof GensokyoFairyEntity fairy) {
            if (fairy.fairyActions().isActive(GensokyoFairyAnimation.RANGED, entity.tickCount)) return;
            fairy.fairyActions().begin(GensokyoFairyAnimation.RANGED, entity.tickCount);
            if (!entity.level().isClientSide()) {
                entity.level().broadcastEntityEvent(entity, GensokyoFairyActions.RANGED_EVENT);
            }
        }
    }
}
