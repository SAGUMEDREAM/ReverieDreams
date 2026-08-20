package cc.thonly.reverie_dreams.registry.content.danmaku;

import cc.thonly.reverie_dreams.data.danmaku.DanmakuShape;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.Map;

public class DanmakuShapes {
    public static void bootstrap(RegistryProvider<DanmakuShape> registry) {
        for (Map.Entry<ResourceKey<DanmakuType>, DanmakuType> mapEntry : BuiltInRegistryProviders.DANMAKU_TYPE.entrySet()) {
            Identifier key = mapEntry.getKey().identifier();
            DanmakuType type = mapEntry.getValue();
            BuiltInRegistryProviders.register(registry, key, new DanmakuShape(type));
        }
    }
}
