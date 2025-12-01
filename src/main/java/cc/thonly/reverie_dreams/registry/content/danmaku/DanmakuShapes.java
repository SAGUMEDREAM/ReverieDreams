package cc.thonly.reverie_dreams.registry.content.danmaku;

import cc.thonly.reverie_dreams.data.danmaku.DanmakuShape;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class DanmakuShapes {
    public static void bootstrap(RegistryHandler<DanmakuShape> registry) {
        for (Map.Entry<ResourceKey<DanmakuType>, DanmakuType> mapEntry : RegistryHandlers.DANMAKU_TYPE.entrySet()) {
            ResourceLocation key = mapEntry.getKey().location();
            DanmakuType type = mapEntry.getValue();
            RegistryHandlers.register(registry, key, new DanmakuShape(type));
        }
    }
}
