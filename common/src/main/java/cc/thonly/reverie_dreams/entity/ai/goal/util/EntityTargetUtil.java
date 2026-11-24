package cc.thonly.reverie_dreams.entity.ai.goal.util;

import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.content.NPCStates;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

import java.util.Objects;

import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import net.minecraft.world.entity.TamableAnimal;

public class EntityTargetUtil {
    public static boolean canAttack(Entity target, BaseNPCLikeEntity maid) {
        if (target == null || !target.isAlive() || target == maid || target == maid.getOwner() || target.level() != maid.level())
            return false;
        if (target instanceof TamableAnimal tame && tame.isTame()
                && Objects.equals(tame.getOwnerReference(), maid.getOwnerReference()))
            return false;
        if (isThisWorkMode(maid, NPCWorkModes.COMBAT)
                && !maid.getWorkController().area().contains(target.blockPosition()))
            return false;
        if (target instanceof BaseNPCLikeEntity otherMaid && Objects.equals(otherMaid.getOwnerUuid(), maid.getOwnerUuid()))
            return false;
        if (target instanceof EnderDragon)
            return false;
        return true;
    }

    public static boolean isThisWorkMode(BaseNPCLikeEntity roleEntity, NPCWorkMode mode) {
        return roleEntity.isTame() && roleEntity.getNPCState() == NPCStates.WORKING && roleEntity.getWorkMode() == mode;
    }
}
