package cc.thonly.reverie_dreams.inf;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.Optional;

public interface IWorld {
    public ResourceKey<Level> reverie_dreams$getDreamWorldKey();

    public ResourceKey<Level> reverie_dreams$getMoonKey();

    public static boolean reverie_dreams$isInWorld(Entity entity, ResourceKey<Level> registryKey) {
        RegistryAccess registryManager = entity.registryAccess();
        Optional<Registry<Level>> lookupOptional = registryManager.lookup(Registries.DIMENSION);
        if (lookupOptional.isEmpty()) {
            return false;
        }
        Registry<Level> worldLookup = lookupOptional.get();
        Level world = worldLookup.getValue(registryKey);
        return reverie_dreams$isInWorld(entity, world);
    }

    public static boolean reverie_dreams$isInWorld(Entity entity, Level world) {
        if (world == null) {
            return false;
        }
        return Objects.equals(world, entity.level());
    }
}
