package cc.thonly.reverie_dreams.registry.tag;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class RDEntityTypeTags {
    public static final TagKey<EntityType<?>> NPC_ROLE = of("role");

    private static TagKey<EntityType<?>> of(String id) {
        return TagKey.create(Registries.ENTITY_TYPE, ReverieDreams.id(id));
    }

    public static void register() {

    }
}
