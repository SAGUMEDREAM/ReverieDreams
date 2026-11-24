package cc.thonly.reverie_dreams.paper.npc;

import java.util.UUID;

/** Detached diagnostics; original NPCSnapshot and archive APIs remain unchanged. */
public record NPCWorkSnapshot(UUID entityId, NPCWorkMode workMode, NPCWorkSite site, String status,
                              long completedActions, UUID targetId, boolean working) { }
