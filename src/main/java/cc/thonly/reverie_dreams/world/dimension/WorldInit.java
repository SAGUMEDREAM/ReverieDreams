package cc.thonly.reverie_dreams.world.dimension;

import cc.thonly.reverie_dreams.Touhou;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class WorldInit {
    public static final RegistryKey<World> DREAM_WORLD = getOrCreateRegistryKey("dream_world");
    public static final RegistryKey<World> THE_MOON = getOrCreateRegistryKey("the_moon");

    public static void bootstrap(Registerable<World> context) {

    }

    public static void register() {

    }

    public static void init() {

    }

    public static RegistryKey<World> getOrCreateRegistryKey(String name) {
        return RegistryKey.of(RegistryKeys.WORLD, Identifier.of(Touhou.MOD_ID, name));
    }

    public static RegistryKey<World> getDreamWorld() {
        return DREAM_WORLD;
    }

    public static RegistryKey<World> getMoon() {
        return THE_MOON;
    }
}
