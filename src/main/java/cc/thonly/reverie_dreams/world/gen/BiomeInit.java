package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.world.gen.biome.DreamBiomeCreator;
import cc.thonly.reverie_dreams.world.gen.biome.TheMoonBiomeCreator;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.PlacedFeature;


public class BiomeInit {
    public static final RegistryKey<Biome> THE_MOON = getOrCreateRegistryKey("the_moon");
    public static final RegistryKey<Biome> DREAM = getOrCreateRegistryKey("dream");

    public static void init() {

    }

    public static void bootstrap(Registerable<Biome> context) {
        RegistryEntryLookup<PlacedFeature> placedFeatureLookup = context.getRegistryLookup(RegistryKeys.PLACED_FEATURE);
        RegistryEntryLookup<ConfiguredCarver<?>> configuredCarverLookup = context.getRegistryLookup(RegistryKeys.CONFIGURED_CARVER);
        context.register(DREAM, DreamBiomeCreator.createDream(
                placedFeatureLookup, configuredCarverLookup
        ));
        context.register(THE_MOON, TheMoonBiomeCreator.createTheMoon(
                placedFeatureLookup, configuredCarverLookup
        ));
    }

    public static RegistryKey<Biome> getOrCreateRegistryKey(String name) {
        return getOrCreateRegistryKey(Touhou.id(name));
    }

    public static RegistryKey<Biome> getOrCreateRegistryKey(Identifier id) {
        return RegistryKey.of(RegistryKeys.BIOME, id);
    }

}
