package cc.thonly.reverie_dreams.api.registry;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AliasManager {
    private static final AliasManager INSTANCE = new AliasManager();
    private final Map<ResourceKey<? extends Registry<?>>, Map<Identifier, Identifier>> keyMap = new Object2ObjectLinkedOpenHashMap<>(128);

    public static <T> void register(ResourceKey<? extends Registry<T>> key, Identifier oldId, Identifier newId) {
        INSTANCE.add(key, oldId, newId);
    }

    public <T> void add(ResourceKey<? extends Registry<T>> key, Identifier oldId, Identifier newId) {
        Map<ResourceKey<? extends Registry<?>>, Map<Identifier, Identifier>> map = INSTANCE.keyMap;
        Map<Identifier, Identifier> idKey = map.computeIfAbsent(key, _ -> new HashMap<>());
        idKey.put(oldId, newId);
    }

    public static <T> Registrar get(ResourceKey<? extends Registry<T>> key) {
        Map<Identifier, Identifier> idMap = INSTANCE.keyMap.computeIfAbsent(key, _ -> new HashMap<>());
        return idMap::put;
    }

    public static <T> void execute(ResourceKey<? extends Registry<T>> key, Executor<T> function) {
        Map<Identifier, Identifier> idMap = INSTANCE.keyMap.get(key);
        if (idMap == null) {
            return;
        }
        function.run(idMap);
    }

    public static AliasManager getManager() {
        return INSTANCE;
    }

    public interface Registrar {
        void addAlias(Identifier oldId, Identifier newId);
    }

    public interface Executor<T> {
        void run(Map<Identifier, Identifier> map);
    }
}
