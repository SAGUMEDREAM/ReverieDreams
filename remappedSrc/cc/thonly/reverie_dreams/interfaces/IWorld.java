package cc.thonly.reverie_dreams.interfaces;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public interface IWorld {
    public ResourceKey<Level> getDreamWorld();

    public ResourceKey<Level> getMoon();

    public static boolean isInWorld(Entity entity, ResourceKey<Level> registryKey) {
        RegistryAccess registryManager = entity.registryAccess();
        Optional<Registry<Level>> lookupOptional = registryManager.lookup(Registries.DIMENSION);
        if (lookupOptional.isEmpty()) {
            return false;
        }
        Registry<Level> worldLookup = lookupOptional.get();
        Level world = worldLookup.getValue(registryKey);
        return isInWorld(entity, world);
    }

    public static boolean isInWorld(Entity entity, Level world) {
        if (world == null) {
            return false;
        }
        return Objects.equals(world, entity.level());
    }
}
