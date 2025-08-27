package cc.thonly.reverie_dreams.interfaces;

import net.minecraft.entity.Entity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.Optional;

public interface IWorld {
    public RegistryKey<World> getDreamWorld();

    public RegistryKey<World> getMoon();

    public static boolean isInWorld(Entity entity, RegistryKey<World> registryKey) {
        DynamicRegistryManager registryManager = entity.getRegistryManager();
        Optional<Registry<World>> lookupOptional = registryManager.getOptional(RegistryKeys.WORLD);
        if (lookupOptional.isEmpty()) {
            return false;
        }
        Registry<World> worldLookup = lookupOptional.get();
        World world = worldLookup.get(registryKey);
        return isInWorld(entity, world);
    }

    public static boolean isInWorld(Entity entity, World world) {
        if (world == null) {
            return false;
        }
        return Objects.equals(world, entity.getWorld());
    }
}
