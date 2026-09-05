package cc.thonly.reverie_dreams.api.client;

import cc.thonly.reverie_dreams.registry.delegate.RegistryDelegate;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
public class EntityRendererRegistry {
    public static final List<Entry<?>> ENTRIES = new ArrayList<>();

    public static <T extends Entity> void register(
            Holder entityType,
            EntityRendererProvider<T> entityRendererProvider
    ) {
        ENTRIES.add(new Entry<>(entityType, entityRendererProvider));
    }

    public static <T extends Entity> void register(
            RegistryDelegate<EntityType<? extends T>> entityType,
            EntityRendererProvider<T> entityRendererProvider
    ) {
        ENTRIES.add(new Entry<>(entityType, entityRendererProvider));
    }

    public record Entry<T extends Entity>(
            Holder<EntityType<? extends T>> entityType,
            EntityRendererProvider<T> entityRendererProvider
    ) {
    }
}