package cc.thonly.reverie_dreams.interfaces;

import cc.thonly.reverie_dreams.entity.npc.NPCRoleEntityImpl;

public interface IExperienceOrbEntity {
    void setNPCTarget(NPCRoleEntityImpl npcRoleEntity);
    NPCRoleEntityImpl getNPCTarget();
}
