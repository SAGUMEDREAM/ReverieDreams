package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class NPCWorkModes {
    public static final IntrinsicalRegister<NPCWorkMode> REGISTRY = RegistryManager.NPC_WORK_MODE;
    public static final NPCWorkMode COMBAT = register(Touhou.id("combat"), new NPCWorkMode("combat", Items.IRON_SWORD));
    public static final NPCWorkMode FARM = register(Touhou.id("farm"), new NPCWorkMode("farm", Items.WHEAT_SEEDS));
    public static final NPCWorkMode BREED = register(Touhou.id("breed"), new NPCWorkMode("breed", Items.WHEAT));
    public static final NPCWorkMode SMELT = register(Touhou.id("smelt"), new NPCWorkMode("smelt", Items.FURNACE));

    public static NPCWorkMode fromInt(Integer rawId) {
        return REGISTRY.get(rawId);
    }

    public static NPCWorkMode register(Identifier id, NPCWorkMode npcWorkMode) {
        return RegistryManager.register(REGISTRY, id, npcWorkMode);
    }

    public static NPCWorkMode get(Identifier id) {
        return REGISTRY.get(id);
    }

    public static void bootstrap(IntrinsicalRegister<NPCWorkMode> registry) {

    }

}
