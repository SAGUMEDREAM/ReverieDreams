package cc.thonly.reverie_dreams.api.registry;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.function.BiConsumer;

public class EntityDataSerializerProviders {
    private static final EntityDataSerializerProviders INSTANCE = new EntityDataSerializerProviders();
    private final Map<Identifier, EntityDataSerializer<?>> registry = new Object2ObjectLinkedOpenHashMap<>(16);

    public static void register(Identifier id, EntityDataSerializer<?> serializer) {
        INSTANCE.add(id, serializer);
    }

    public void add(Identifier id, EntityDataSerializer<?> serializer) {
        this.registry.put(id, serializer);
    }

    public void forEach(BiConsumer<Identifier, EntityDataSerializer<?>> function) {
        this.registry.forEach(function);
    }

    public static EntityDataSerializerProviders get() {
        return INSTANCE;
    }
}
