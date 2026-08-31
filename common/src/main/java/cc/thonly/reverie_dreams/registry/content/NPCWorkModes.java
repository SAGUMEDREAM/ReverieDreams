package cc.thonly.reverie_dreams.registry.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

public class NPCWorkModes {
    public static final NPCWorkMode COMBAT = register(ReverieDreams.id("combat"),
            new NPCWorkMode("combat", Items.IRON_SWORD)
    );
    public static final NPCWorkMode FARM = register(ReverieDreams.id("farm"),
            new NPCWorkMode("farm", Items.WHEAT_SEEDS)
    );
    public static final NPCWorkMode BREED = register(ReverieDreams.id("breed"),
            new NPCWorkMode("breed", Items.WHEAT)
    );
    public static final NPCWorkMode SHEEP_SHEARING = register(ReverieDreams.id("sheep_shearing"),
            new NPCWorkMode("sheep_shearing", Items.SHEARS)
    );
    public static final NPCWorkMode SMELT = register(ReverieDreams.id("smelt"),
            new NPCWorkMode("smelt", Items.FURNACE)
    );
    public static final NPCWorkMode CHEST_CLASSIFICATION = register(ReverieDreams.id("chest_classification"),
            new NPCWorkMode("chest_classification", Items.CHEST)
    );
    public static final NPCWorkMode PLAYING_MUSIC = register(ReverieDreams.id("playing_music"),
            new NPCWorkMode("playing_music", Items.NOTE_BLOCK)
    );
    public static final NPCWorkMode FISHING = register(ReverieDreams.id("fishing"),
            new NPCWorkMode("fishing", Items.FISHING_ROD)
    );
    public static final NPCWorkMode CUSTOMER = register(ReverieDreams.id("customer"),
            new NPCWorkMode("customer", Items.COOKED_BEEF)
                    .withStop((npc, mode) -> {
                        npc.stopRiding();
                    })
    );
    public static NPCWorkMode CREATE_FLY_HAND_CRANK;
    public static NPCWorkMode POLYFACTORY_HAND_CRANK;

    public static NPCWorkMode fromInt(Integer rawId) {
        return BuiltInRegistryProviders.NPC_WORK_MODE.byId(rawId);
    }

    public static NPCWorkMode register(Identifier id, NPCWorkMode npcWorkMode) {
        return BuiltInRegistryProviders.register(BuiltInRegistryProviders.NPC_WORK_MODE, id, npcWorkMode);
    }

    public static NPCWorkMode get(Identifier id) {
        return BuiltInRegistryProviders.NPC_WORK_MODE.getValue(id);
    }

    public static void bootstrap(RegistryProvider<NPCWorkMode> registry) {

    }

}
