package cc.thonly.reverie_dreams.registry.content.skin;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import net.minecraft.resources.Identifier;

public abstract class SkinTypes {

    public static SkinType register(SkinType skin) {
        return register(skin.getId(), skin);
    }

    public static SkinType register(String name, SkinType skin) {
        return register(ReverieDreams.id(name), skin);
    }

    public static SkinType register(Identifier id, SkinType skin) {
        return RegistryHandlers.register(RegistryHandlers.SKIN_TYPE, id, skin);
    }
}
