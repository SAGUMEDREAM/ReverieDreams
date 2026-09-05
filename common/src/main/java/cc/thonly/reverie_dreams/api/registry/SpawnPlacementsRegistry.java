package cc.thonly.reverie_dreams.api.registry;

import net.minecraft.world.entity.*;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public class SpawnPlacementsRegistry {
    public static final List<Entry<?>> ENTRIES = new CopyOnWriteArrayList<>();
    public static <T extends Mob> void register(Supplier<? extends EntityType<T>> type, SpawnPlacementType spawnPlacement, Heightmap.Types heightmapType, SpawnPlacements.SpawnPredicate<T> spawnPredicate) {
        ENTRIES.add(new Entry<>(type, spawnPlacement, heightmapType, spawnPredicate));
    }

    public record Entry<T extends Mob>(Supplier<? extends EntityType<T>> type,
                                              SpawnPlacementType spawnPlacement,
                                              Heightmap.Types heightmapType,
                                              SpawnPlacements.SpawnPredicate<T> spawnPredicate) {

    }
}
