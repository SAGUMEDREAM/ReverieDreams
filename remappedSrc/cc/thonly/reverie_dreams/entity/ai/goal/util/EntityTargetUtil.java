package cc.thonly.reverie_dreams.entity.ai.goal.util;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCStates;
import cc.thonly.reverie_dreams.entity.npc.NPCWorkMode;
import java.util.Objects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

public class EntityTargetUtil {
    public static boolean canAttack(Entity target, BaseNPCLikeEntity maid) {
        if (target instanceof BaseNPCLikeEntity otherMaid && Objects.equals(otherMaid.getOwnerUuid(), maid.getOwnerUuid()))
            return false;
        if (target instanceof EnderDragon)
            return false;
        return true;
    }

    public static boolean isThisWorkMode(BaseNPCLikeEntity roleEntity, NPCWorkMode mode) {
        return roleEntity.isTame() && roleEntity.getNpcState() == NPCStates.WORKING && roleEntity.getWorkMode() == mode;
    }
}
