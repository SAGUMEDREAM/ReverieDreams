package cc.thonly.reverie_dreams.registry.content.skin;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import net.minecraft.resources.ResourceLocation;

public abstract class SkinTypes {
    private static final RegistryHandler<SkinType> REGISTRY = RegistryHandlers.SKIN_TYPE;

    public static SkinType register(SkinType skin) {
        return register(skin.getId(), skin);
    }

    public static SkinType register(String name, SkinType skin) {
        return register(ReverieDreams.id(name), skin);
    }

    public static SkinType register(ResourceLocation id, SkinType skin) {
        return RegistryHandlers.register(REGISTRY, id, skin);
    }
}
