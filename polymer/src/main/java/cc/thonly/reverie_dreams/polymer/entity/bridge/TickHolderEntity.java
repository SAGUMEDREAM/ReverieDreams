package cc.thonly.reverie_dreams.polymer.entity.bridge;

import cc.thonly.reverie_dreams.polymer.entity.holder.OverlayEntityHolder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface TickHolderEntity {
    List<TickHolderEntity> LIST = new ArrayList<>();
    Map<Entity, OverlayEntityHolder<?,?>> ELEMENT_BINDS = new Object2ObjectOpenHashMap<>();

    static void addTickHolder(TickHolderEntity entity) {
        LIST.add(entity);
    }

    static void addElementBind(Entity entity, OverlayEntityHolder<?,?> holder) {
        ELEMENT_BINDS.put(entity, holder);
    }

    static void tick(MinecraftServer server) {
        LIST.removeIf(e -> {
            LivingEntity entity = e.getSource();
            if (entity == null || entity.isRemoved()
                    || !(entity.level() instanceof ServerLevel level) || level.getServer() != server) {
                ELEMENT_BINDS.remove(entity);
                return true;
            }
            e.onTick();
            return false;
        });
    }

    LivingEntity getSource();

    default void onTick() {

    }
}
