package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.entity.skin.RoleSkin;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.minecraft.util.Identifier;

public abstract class AbstractSkinBootstrap {
    private static final IntrinsicalRegister<RoleSkin> REGISTRY = RegistryManager.ROLE_SKIN;

    public static RoleSkin register(RoleSkin skin) {
        return register(skin.getId(), skin);
    }

    public static RoleSkin register(String name, RoleSkin skin) {
        return register(Touhou.id(name), skin);
    }

    public static RoleSkin register(Identifier id, RoleSkin skin) {
        return RegistryManager.register(REGISTRY, id, skin);
    }
}
