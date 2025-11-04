package cc.thonly.polymer.entity;

import cc.thonly.polymer.entity.bil.BlockbenchEntityHolder;
import de.tomalbrc.bil.api.AnimatedEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public interface TickHolderEntity {
    List<TickHolderEntity> LIST = new ArrayList<>();
    Map<Entity, BlockbenchEntityHolder<?,?>> ELEMENT_BINDS = new WeakHashMap<>();

    static void addTickHolder(TickHolderEntity entity) {
        LIST.add(entity);
    }

    static void addElementBind(Entity entity, BlockbenchEntityHolder<?,?> holder) {
        ELEMENT_BINDS.put(entity, holder);
    }

    static void tick() {
        LIST.removeIf(e -> {
            LivingEntity entity = e.getEntity();
            if (entity == null || entity.isRemoved()) return true;
            e.onTick();
            return false;
        });
    }

    LivingEntity getEntity();

    default void onTick() {

    }
}
