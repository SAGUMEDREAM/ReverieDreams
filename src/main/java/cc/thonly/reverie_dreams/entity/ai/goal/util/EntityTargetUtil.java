package cc.thonly.reverie_dreams.entity.ai.goal.util;

import cc.thonly.reverie_dreams.entity.npc.NPCEntityImpl;
import cc.thonly.reverie_dreams.entity.npc.NPCStates;
import cc.thonly.reverie_dreams.entity.npc.NPCWorkMode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;

import java.util.Objects;

public class EntityTargetUtil {
    public static boolean canAttack(Entity target, NPCEntityImpl maid) {
        if (target instanceof NPCEntityImpl otherMaid && Objects.equals(otherMaid.getOwnerUuid(), maid.getOwnerUuid()))
            return false;
        if (target instanceof EnderDragonEntity)
            return false;
        return true;
    }

    public static boolean isThisWorkMode(NPCEntityImpl maid, NPCWorkMode mode) {
        return maid.isTamed() && maid.getNpcState() == NPCStates.WORKING && maid.getWorkMode() == mode;
    }
}
