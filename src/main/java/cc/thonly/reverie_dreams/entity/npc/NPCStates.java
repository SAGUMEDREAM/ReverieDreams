package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public class NPCStates {
    public static final NPCState FOLLOW = register(ReverieDreams.id("follow"), new NPCState("follow"));
    public static final NPCState NORMAL = register(ReverieDreams.id("normal"), new NPCState("normal"));
    public static final NPCState NO_WALK = register(ReverieDreams.id("no_walk"), new NPCState("no_walk"));
    public static final NPCState SNAKING = register(ReverieDreams.id("sneaking"), new NPCState("sneaking"));
    public static final NPCState SEATED = register(ReverieDreams.id("seated"), new NPCState("seated"));
    public static final NPCState WORKING = register(ReverieDreams.id("working"), new NPCState("working"));
    public static final Map<Integer, NPCState> DEFAULT_RAW_ID2STATE = new HashMap<>(
            Map.of(
                    0, FOLLOW,
                    1, NORMAL,
                    2, NO_WALK,
                    3, SNAKING,
                    4, SEATED,
                    5, WORKING
            )
    );

    public static NPCState get(ResourceLocation id) {
        return RegistryManager.NPC_STATE.getValue(id);
    }

    public static NPCState fromInt(Integer rawId) {
        return DEFAULT_RAW_ID2STATE.getOrDefault(rawId, RegistryManager.NPC_STATE.byId(rawId));
    }

    public static NPCState register(ResourceLocation id, NPCState npcState) {
        return RegistryManager.register(RegistryManager.NPC_STATE, id, npcState);
    }

    public static void bootstrap(IntrinsicalRegister<NPCState> registry) {

    }
}
