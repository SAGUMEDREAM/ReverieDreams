package cc.thonly.reverie_dreams.interfaces;

import cc.thonly.reverie_dreams.entity.npc.NPCRoleEntity;

public interface IExperienceOrbEntity {
    void setNPCTarget(NPCRoleEntity npcRoleEntity);
    NPCRoleEntity getNPCTarget();
}
