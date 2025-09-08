package cc.thonly.reverie_dreams.danmaku;

import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.minecraft.util.Identifier;

import java.util.Map;

public class DanmakuShapes {
    public static void bootstrap(IntrinsicalRegister<DanmakuShape> registry) {
        for (Map.Entry<Identifier, DanmakuType> mapEntry : RegistryManager.DANMAKU_TYPE.entrySet()) {
            Identifier key = mapEntry.getKey();
            DanmakuType type = mapEntry.getValue();
            RegistryManager.register(registry, key, new DanmakuShape(type));
        }
    }
}
