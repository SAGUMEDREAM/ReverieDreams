package cc.thonly.reverie_dreams.api.entity.type;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface FriendlyFaction {
    String getFactionId();

    static boolean isFriendLy(Entity entityA, Entity entityB) {
        if (entityA instanceof FriendlyFaction factionEntityA && entityB instanceof FriendlyFaction factionEntityB) {
            return Objects.equals(factionEntityA.getFactionId(), factionEntityB.getFactionId());
        }
        return false;
    }

    /**
     * 同阵营免伤是防止流弹误伤；如果 owner 已经明确把该实体设为目标
     * （或最近攻击/被其攻击），这次命中就是有意的，应当放行。
     */
    static boolean isDirectTarget(@Nullable Entity owner, Entity target) {
        if (owner == null || owner == target) {
            return false;
        }
        if (owner instanceof Mob mob && mob.getTarget() == target) {
            return true;
        }
        if (owner instanceof LivingEntity living) {
            return living.getLastHurtMob() == target || living.getLastHurtByMob() == target;
        }
        return false;
    }
}