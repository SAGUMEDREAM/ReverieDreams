package cc.thonly.reverie_dreams.danmaku;

import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public class DanmakuShapes {
    public static void bootstrap(IntrinsicalRegister<DanmakuShape> registry) {
        for (Map.Entry<ResourceLocation, DanmakuType> mapEntry : RegistryManager.DANMAKU_TYPE.entrySet()) {
            ResourceLocation key = mapEntry.getKey();
            DanmakuType type = mapEntry.getValue();
            RegistryManager.register(registry, key, new DanmakuShape(type));
        }
    }
}
