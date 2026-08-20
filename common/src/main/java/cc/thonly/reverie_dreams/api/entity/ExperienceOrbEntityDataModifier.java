package cc.thonly.reverie_dreams.api.entity;

import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;

public interface ExperienceOrbEntityDataModifier {
    void reverie_dreams$setNPCTarget(NPCSimpleEntity npc);

    NPCSimpleEntity reverie_dreams$getNPCTarget();
}
