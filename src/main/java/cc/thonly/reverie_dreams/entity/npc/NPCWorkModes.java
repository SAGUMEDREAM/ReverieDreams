package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

public class NPCWorkModes {
    public static final IntrinsicalRegister<NPCWorkMode> REGISTRY = RegistryManager.NPC_WORK_MODE;
    public static final NPCWorkMode COMBAT = register(ReverieDreams.id("combat"), new NPCWorkMode("combat", Items.IRON_SWORD));
    public static final NPCWorkMode FARM = register(ReverieDreams.id("farm"), new NPCWorkMode("farm", Items.WHEAT_SEEDS));
    public static final NPCWorkMode BREED = register(ReverieDreams.id("breed"), new NPCWorkMode("breed", Items.WHEAT));
    public static final NPCWorkMode SHEEP_SHEARING = register(ReverieDreams.id("sheep_shearing"), new NPCWorkMode("sheep_shearing", Items.SHEARS));
    public static final NPCWorkMode SMELT = register(ReverieDreams.id("smelt"), new NPCWorkMode("smelt", Items.FURNACE));
    public static final NPCWorkMode CHEST_CLASSIFICATION = register(ReverieDreams.id("chest_classification"), new NPCWorkMode("chest_classification", Items.CHEST));
    public static final NPCWorkMode PLAYING_MUSIC = register(ReverieDreams.id("playing_music"), new NPCWorkMode("playing_music", Items.NOTE_BLOCK));

    public static NPCWorkMode fromInt(Integer rawId) {
        return REGISTRY.byId(rawId);
    }

    public static NPCWorkMode register(ResourceLocation id, NPCWorkMode npcWorkMode) {
        return RegistryManager.register(REGISTRY, id, npcWorkMode);
    }

    public static NPCWorkMode get(ResourceLocation id) {
        return REGISTRY.getValue(id);
    }

    public static void bootstrap(IntrinsicalRegister<NPCWorkMode> registry) {

    }

}
