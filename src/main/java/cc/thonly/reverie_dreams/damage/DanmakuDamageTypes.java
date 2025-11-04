package cc.thonly.reverie_dreams.damage;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.minecraft.resources.ResourceLocation;

public class DanmakuDamageTypes {
    public static final DanmakuDamageType GENERIC = register(ReverieDreams.id("generic"));
    public static final DanmakuDamageType REAL = register(ReverieDreams.id("real"));

    public static DanmakuDamageType register(ResourceLocation id) {
        return RegistryManager.register(RegistryManager.DANMAKU_DAMAGE_TYPE,
                id,
                new DanmakuDamageType(id)
        );
    }

    public static void bootstrap(IntrinsicalRegister<DanmakuDamageType> registry) {

    }
}
