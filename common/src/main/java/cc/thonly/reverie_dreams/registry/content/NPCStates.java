package cc.thonly.reverie_dreams.registry.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.npc.NPCState;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

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

    public static NPCState get(Identifier id) {
        return RegistryImpls.NPC_STATE.getValue(id);
    }

    public static NPCState fromInt(Integer rawId) {
        return DEFAULT_RAW_ID2STATE.getOrDefault(rawId, RegistryImpls.NPC_STATE.byId(rawId));
    }

    public static NPCState register(Identifier id, NPCState npcState) {
        return RegistryImpls.register(RegistryImpls.NPC_STATE, id, npcState);
    }

    public static void bootstrap(RegistryImpl<NPCState> registry) {

    }
}
