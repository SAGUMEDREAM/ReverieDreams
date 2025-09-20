package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.entity.skin.NPCSkin;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.minecraft.util.Identifier;

public abstract class AbstractSkins {
    private static final IntrinsicalRegister<NPCSkin> REGISTRY = RegistryManager.ROLE_SKIN;

    public static NPCSkin register(NPCSkin skin) {
        return register(skin.getId(), skin);
    }

    public static NPCSkin register(String name, NPCSkin skin) {
        return register(Touhou.id(name), skin);
    }

    public static NPCSkin register(Identifier id, NPCSkin skin) {
        return RegistryManager.register(REGISTRY, id, skin);
    }
}
