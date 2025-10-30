package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.reverie_dreams.Touhou;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;

public class ConfigurationCarverInit {

    public static void init() {

    }

    public static void bootstrap(BootstrapContext<ConfiguredWorldCarver<?>> context) {

    }

    private static ResourceKey<ConfiguredWorldCarver<?>> getOrCreateRegistryKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_CARVER, Touhou.id(name));
    }
}
