package cc.thonly.reverie_dreams.damage;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.minecraft.world.damagesource.DamageTypes;

public class DanmakuDamageTypes {
    public static final DanmakuDamageType GENERIC = RegistryManager.register(RegistryManager.DANMAKU_DAMAGE_TYPE, ReverieDreams.id("generic"), new DanmakuDamageType(DamageTypes.GENERIC));
    public static final DanmakuDamageType REAL = RegistryManager.register(RegistryManager.DANMAKU_DAMAGE_TYPE, ReverieDreams.id("generic"), new DanmakuDamageType(DamageTypes.GENERIC));

    public static void bootstrap(IntrinsicalRegister<DanmakuDamageType> registry) {

    }
}
