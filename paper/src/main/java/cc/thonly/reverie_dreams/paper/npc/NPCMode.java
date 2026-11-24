package cc.thonly.reverie_dreams.paper.npc;

import cc.thonly.reverie_dreams.paper.util.Ids;

public enum NPCMode {
    NORMAL(Ids.MOD_ID + ":normal"), FOLLOW(Ids.MOD_ID + ":follow"), STAY(Ids.MOD_ID + ":no_walk"), WORKING(Ids.MOD_ID + ":working");

    private final String sourceId;
    NPCMode(String sourceId) { this.sourceId = sourceId; }
    public String sourceId() { return sourceId; }

    public static NPCMode fromSourceId(String sourceId) {
        for (NPCMode mode : values()) if (mode.sourceId.equals(sourceId)) return mode;
        throw new IllegalArgumentException("Unsupported NPC mode: " + sourceId);
    }
}
