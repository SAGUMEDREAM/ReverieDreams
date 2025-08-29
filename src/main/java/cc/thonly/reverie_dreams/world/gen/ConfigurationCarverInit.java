package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.reverie_dreams.Touhou;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.gen.carver.ConfiguredCarver;

public class ConfigurationCarverInit {

    public static void init() {

    }

    public static void bootstrap(Registerable<ConfiguredCarver<?>> context) {

    }

    private static RegistryKey<ConfiguredCarver<?>> getOrCreateRegistryKey(String name) {
        return RegistryKey.of(RegistryKeys.CONFIGURED_CARVER, Touhou.id(name));
    }
}
