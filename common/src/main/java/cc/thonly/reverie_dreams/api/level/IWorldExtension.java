package cc.thonly.reverie_dreams.api.level;

import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.Optional;

public interface IWorldExtension {

    static IWorldExtension get(Level level) {
        return (IWorldExtension) level;
    }

    static ResourceKey<Level> reverie_dreams$getDreamWorldKey() {
        return RDBuiltinLevels.getDreamWorld();
    }

    static ResourceKey<Level> reverie_dreams$getMoonKey() {
        return RDBuiltinLevels.getMoon();
    }

    static boolean reverie_dreams$isInWorld(Entity entity, ResourceKey<Level> registryKey) {
        RegistryAccess registryManager = entity.registryAccess();
        Optional<Registry<Level>> lookupOptional = registryManager.lookup(Registries.DIMENSION);
        if (lookupOptional.isEmpty()) {
            return false;
        }
        Registry<Level> worldLookup = lookupOptional.get();
        Level world = worldLookup.getValue(registryKey);
        return reverie_dreams$isInWorld(entity, world);
    }

    static boolean reverie_dreams$isInWorld(Entity entity, Level world) {
        if (world == null) {
            return false;
        }
        return Objects.equals(world, entity.level());
    }
}
