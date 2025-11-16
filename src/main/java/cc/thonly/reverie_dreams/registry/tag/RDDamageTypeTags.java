package cc.thonly.reverie_dreams.registry.tag;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public class RDDamageTypeTags {
    public static final TagKey<DamageType> DANMAKU_HIT = of("danmaku_hit");

    private static TagKey<DamageType> of(String id) {
        return TagKey.create(Registries.DAMAGE_TYPE, ReverieDreams.id(id));
    }
}
