package cc.thonly.reverie_dreams.entity.interfaces;

import net.minecraft.world.entity.Entity;

import java.util.Objects;

public interface FriendlyFaction {
    String getFactionId();

    static boolean isFriendLy(Entity entityA, Entity entityB) {
        if (entityA instanceof FriendlyFaction factionEntityA && entityB instanceof FriendlyFaction factionEntityB) {
            return Objects.equals(factionEntityA.getFactionId(), factionEntityB.getFactionId());
        }
        return false;
    }
}