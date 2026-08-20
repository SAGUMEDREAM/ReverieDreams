package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.world.gen.biome.DreamBiomeCreator;
import cc.thonly.reverie_dreams.world.gen.biome.MoonSeaBiomeCreator;
import cc.thonly.reverie_dreams.world.gen.biome.TheMoonBiomeCreator;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;


public class RDBuiltinBiomes {
    public static final ResourceKey<Biome> THE_MOON = getOrCreateRegistryKey("the_moon");
    public static final ResourceKey<Biome> MOON_SEA = getOrCreateRegistryKey("moon_sea");
    public static final ResourceKey<Biome> DREAM = getOrCreateRegistryKey("dream");

    public static void init() {

    }

    public static void bootstrap(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> placedFeatureLookup = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> configuredCarverLookup = context.lookup(Registries.CONFIGURED_CARVER);
        context.register(DREAM, DreamBiomeCreator.createDream(
                placedFeatureLookup, configuredCarverLookup
        ));
        context.register(THE_MOON, TheMoonBiomeCreator.createTheMoon(
                placedFeatureLookup, configuredCarverLookup
        ));
        context.register(MOON_SEA, MoonSeaBiomeCreator.createMoonSea(
                placedFeatureLookup, configuredCarverLookup
        ));

    }

    public static ResourceKey<Biome> getOrCreateRegistryKey(String name) {
        return getOrCreateRegistryKey(ReverieDreams.id(name));
    }

    public static ResourceKey<Biome> getOrCreateRegistryKey(Identifier id) {
        return ResourceKey.create(Registries.BIOME, id);
    }

}
