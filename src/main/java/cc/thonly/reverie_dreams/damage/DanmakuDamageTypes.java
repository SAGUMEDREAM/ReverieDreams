package cc.thonly.reverie_dreams.damage;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import net.minecraft.resources.ResourceLocation;

public class DanmakuDamageTypes {
    public static final DanmakuDamageType GENERIC = register(ReverieDreams.id("generic"));
    public static final DanmakuDamageType REAL = register(ReverieDreams.id("real"));

    public static DanmakuDamageType register(ResourceLocation id) {
        return RegistryHandlers.register(RegistryHandlers.DANMAKU_DAMAGE_TYPE,
                id,
                new DanmakuDamageType(id)
        );
    }

    public static void bootstrap(RegistryHandler<DanmakuDamageType> registry) {

    }
}
