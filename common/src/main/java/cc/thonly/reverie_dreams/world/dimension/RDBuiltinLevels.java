package cc.thonly.reverie_dreams.world.dimension;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class RDBuiltinLevels {
    public static final ResourceKey<Level> DREAM_WORLD = getOrCreateRegistryKey("dream_world");
    public static final ResourceKey<Level> THE_MOON = getOrCreateRegistryKey("the_moon");

    public static void bootstrap(BootstrapContext<Level> context) {

    }

    public static void register() {

    }

    public static void init() {

    }

    public static ResourceKey<Level> getOrCreateRegistryKey(String name) {
        return ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(ReverieDreams.MOD_ID, name));
    }

    public static ResourceKey<Level> getDreamWorld() {
        return DREAM_WORLD;
    }

    public static ResourceKey<Level> getMoon() {
        return THE_MOON;
    }
}
