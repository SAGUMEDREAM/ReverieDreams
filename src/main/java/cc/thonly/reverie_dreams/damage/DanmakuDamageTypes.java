package cc.thonly.reverie_dreams.damage;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.minecraft.entity.damage.DamageTypes;

public class DanmakuDamageTypes {
    public static final DanmakuDamageType GENERIC_TYPE = RegistryManager.register(RegistryManager.DANMAKU_DAMAGE_TYPE, Touhou.id("generic"), new DanmakuDamageType(DamageTypes.GENERIC));

    public static void bootstrap(IntrinsicalRegister<DanmakuDamageType> registry) {

    }
}
