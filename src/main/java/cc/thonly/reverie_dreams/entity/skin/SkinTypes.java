package cc.thonly.reverie_dreams.entity.skin;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.minecraft.util.Identifier;

public abstract class SkinTypes {
    private static final IntrinsicalRegister<SkinType> REGISTRY = RegistryManager.SKIN_TYPE;

    public static SkinType register(SkinType skin) {
        return register(skin.getId(), skin);
    }

    public static SkinType register(String name, SkinType skin) {
        return register(Touhou.id(name), skin);
    }

    public static SkinType register(Identifier id, SkinType skin) {
        return RegistryManager.register(REGISTRY, id, skin);
    }
}
