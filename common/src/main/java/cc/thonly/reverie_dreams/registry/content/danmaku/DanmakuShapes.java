package cc.thonly.reverie_dreams.registry.content.danmaku;

import cc.thonly.reverie_dreams.data.danmaku.DanmakuShape;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

import java.util.Map;

public class DanmakuShapes {
    public static void bootstrap(RegistryImpl<DanmakuShape> registry) {
        for (Map.Entry<ResourceKey<DanmakuType>, DanmakuType> mapEntry : RegistryImpls.DANMAKU_TYPE.entrySet()) {
            Identifier key = mapEntry.getKey().identifier();
            DanmakuType type = mapEntry.getValue();
            RegistryImpls.register(registry, key, new DanmakuShape(type));
        }
    }
}
