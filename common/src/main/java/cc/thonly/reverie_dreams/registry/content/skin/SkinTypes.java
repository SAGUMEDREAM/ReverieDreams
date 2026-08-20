package cc.thonly.reverie_dreams.registry.content.skin;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.skin.CustomType;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class SkinTypes {

    public static SkinType register(SkinType skin) {
        return register(skin.getId(), skin);
    }

    public static SkinType register(String name, SkinType skin) {
        return register(ReverieDreams.id(name), skin);
    }

    public static SkinType register(Identifier id, SkinType skin) {
        return BuiltInRegistryProviders.registerForBuiltin(BuiltInRegistryProviders.SKIN_TYPE, id, skin);
    }

    public static Collection<CustomType> getCustomTypes() {
        List<CustomType> list = new ArrayList<>();
        for (Map.Entry<ResourceKey<SkinType>, SkinType> entry : BuiltInRegistryProviders.SKIN_TYPE_MERGED.entrySet()) {
            SkinType type = entry.getValue();
            if (type instanceof CustomType customType) {
                list.add(customType);
            }
        }
        return list;
    }
}
