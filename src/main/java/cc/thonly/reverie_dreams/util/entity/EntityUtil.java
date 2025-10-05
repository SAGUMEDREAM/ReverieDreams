package cc.thonly.reverie_dreams.util.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;

public class EntityUtil {
    public static boolean isInTag(DynamicRegistryManager registryManager, Entity entity, TagKey<EntityType<?>> tagKey) {
        Registry<EntityType<?>> registry = registryManager.getOrThrow(RegistryKeys.ENTITY_TYPE);
        Iterable<RegistryEntry<EntityType<?>>> registryEntries = registry.iterateEntries(tagKey);
        for (RegistryEntry<EntityType<?>> registryEntry : registryEntries) {
            if (registryEntry.value() == entity.getType()) {
                return true;
            }
        }
        return false;
    }
}
