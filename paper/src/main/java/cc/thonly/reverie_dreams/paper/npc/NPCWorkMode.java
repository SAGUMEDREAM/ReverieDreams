package cc.thonly.reverie_dreams.paper.npc;

import cc.thonly.reverie_dreams.paper.util.Ids;

/** Original ten mode IDs; capability flags describe implemented runtime, never silently fall back. */
public enum NPCWorkMode {
    COMBAT("combat", true), FARM("farm", true), BREED("breed", true), SHEEP_SHEARING("sheep_shearing", true),
    SMELT("smelt", true), CHEST_CLASSIFICATION("chest_classification", true), PLAYING_MUSIC("playing_music", false),
    HONEYCOMB_HARVESTING("honeycomb_harvesting", true), FISHING("fishing", false), CUSTOMER("customer", false);
    private final String path; private final boolean implemented;
    NPCWorkMode(String path, boolean implemented) { this.path = path; this.implemented = implemented; }
    public String sourceId() { return Ids.MOD_ID + ":" + path; }
    public boolean production(){return this==FARM||this==BREED||this==SHEEP_SHEARING||this==SMELT||this==HONEYCOMB_HARVESTING||this==FISHING;}
    public boolean implemented() { return implemented; }
    public static NPCWorkMode fromId(String id) {
        for (var mode : values()) if (mode.sourceId().equals(id) || mode.path.equals(id)) return mode;
        throw new IllegalArgumentException("Unknown source NPC work mode " + id);
    }
}
