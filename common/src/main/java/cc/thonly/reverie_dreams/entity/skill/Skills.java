package cc.thonly.reverie_dreams.entity.skill;

import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;

public class Skills {
    public static final IceFairySkill ICE_FAIRY_SKILL = new IceFairySkill();


    public static void bootstrap(RegistryProvider<Skill<?>> registry) {
        // SkillContainer 会把技能 ID 写进实体 NBT，必须在注册表里有对应项才能正确读回
        BuiltInRegistryProviders.registerForBuiltin(registry, ICE_FAIRY_SKILL.id(), ICE_FAIRY_SKILL);
    }
}
