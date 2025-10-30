package cc.thonly.reverie_dreams.util.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class EntityUtil {
    public static boolean isInTag(RegistryAccess registryManager, Entity entity, TagKey<EntityType<?>> tagKey) {
        Registry<EntityType<?>> registry = registryManager.lookupOrThrow(Registries.ENTITY_TYPE);
        Iterable<Holder<EntityType<?>>> registryEntries = registry.getTagOrEmpty(tagKey);
        for (Holder<EntityType<?>> registryEntry : registryEntries) {
            if (registryEntry.value() == entity.getType()) {
                return true;
            }
        }
        return false;
    }
}
