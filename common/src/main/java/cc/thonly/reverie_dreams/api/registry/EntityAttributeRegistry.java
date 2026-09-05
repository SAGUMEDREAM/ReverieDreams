package cc.thonly.reverie_dreams.api.registry;

import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.delegate.RegistryDelegate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EntityAttributeRegistry {
    public static final List<Entry<?>> ENTRIES = new CopyOnWriteArrayList<>();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T extends Entity> void register(RegistryDelegate<EntityType<T>> entityType, RDEntityTypes.CreateAttributesBuilderFunction function) {
        ENTRIES.add(new Entry<>((RegistryDelegate) entityType, function));
    }

    public record Entry<T extends LivingEntity>(RegistryDelegate<EntityType<T>> entityType,
                                                RDEntityTypes.CreateAttributesBuilderFunction function) {

    }
}
