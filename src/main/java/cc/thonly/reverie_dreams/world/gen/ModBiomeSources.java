package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.reverie_dreams.Touhou;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

public class ModBiomeSources {
    public static final RegistryKey<Biome> THE_MOON = of("the_moon");
    public static void init() {

    }

    public static RegistryKey<Biome> of(String name) {
        return of(Touhou.id(name));
    }

    public static RegistryKey<Biome> of(Identifier id) {
        return RegistryKey.of(RegistryKeys.BIOME, id);
    }

}
