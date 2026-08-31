package cc.thonly.reverie_dreams.api.entity;

import net.minecraft.world.entity.Entity;

public interface ExclusiveEntityBlock {
    Entity reverie_dreams$getEntity();

    void reverie_dreams$setEntity(Entity entity);

    default boolean reverie_dreams$isOccupied() {
        return reverie_dreams$getEntity() != null;
    }

    default boolean reverie_dreams$isOccupiedBy(Entity entity) {
        return reverie_dreams$getEntity() == entity;
    }

    default boolean reverie_dreams$tryOccupy(Entity entity) {
        Entity current = reverie_dreams$getEntity();
        if (current == null || current == entity) {
            reverie_dreams$setEntity(entity);
            return true;
        }
        return false;
    }

    default void reverie_dreams$release(Entity entity) {
        if (reverie_dreams$isOccupiedBy(entity)) {
            reverie_dreams$setEntity(null);
        }
    }


}
