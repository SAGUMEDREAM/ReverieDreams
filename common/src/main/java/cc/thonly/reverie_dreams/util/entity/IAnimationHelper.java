package cc.thonly.reverie_dreams.util.entity;

import net.minecraft.world.entity.Entity;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.constant.dataticket.DataTicket;

public interface IAnimationHelper {
    static boolean isActuallyMoving(Entity entity) {
        var motion = entity.getDeltaMovement();
        double horizontalSpeedSq = motion.x * motion.x + motion.z * motion.z;
        boolean grounded = entity.onGround() || entity.isInWater();

        return grounded && horizontalSpeedSq > 0.0001;
    }

    static <T extends Entity & GeoEntity,D> void setSyncData(T entity, DataTicket<D> ticket, D data) {
        int id = entity.getId();
        entity.getAnimatableInstanceCache().getManagerForId(id).setAnimatableData(ticket, data);
    }

    static <T extends Entity & GeoEntity,D> D getSyncData(T entity, DataTicket<D> ticket) {
        int id = entity.getId();
        return entity.getAnimatableInstanceCache().getManagerForId(id).getAnimatableData(ticket);
    }

    static IAnimationPreset presets() {
        return IAnimationPreset.getInstance();
    }
}
