package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.world.gen.biome.DreamBiomeCreator;
import cc.thonly.reverie_dreams.world.gen.biome.MagicForestBiomeCreator;
import cc.thonly.reverie_dreams.world.gen.biome.MapleForestBiomeCreator;
import cc.thonly.reverie_dreams.world.gen.biome.MoonSeaBiomeCreator;
import cc.thonly.reverie_dreams.world.gen.biome.TheMoonBiomeCreator;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestBootstrap;
import cc.thonly.reverie_dreams.world.moon.MoonBiomes;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import cc.thonly.reverie_dreams.world.gen.biome.GensokyoPlainsBiomeCreator;
import cc.thonly.reverie_dreams.world.gensokyo.bamboo.BambooForestBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.landmark.GensokyoLandmarkBootstrap;


public class RDBuiltinBiomes {
    public static final ResourceKey<Biome> THE_MOON = getOrCreateRegistryKey("the_moon");
    public static final ResourceKey<Biome> MOON_SEA = getOrCreateRegistryKey("moon_sea");
    public static final ResourceKey<Biome> DREAM = getOrCreateRegistryKey("dream");
    public static final ResourceKey<Biome> MAPLE_FOREST = getOrCreateRegistryKey("maple_forest");
    public static final ResourceKey<Biome> MAGIC_FOREST = getOrCreateRegistryKey("magic_forest");
    public static final ResourceKey<Biome> GENSOKYO_BAMBOO_FOREST = BambooForestBootstrap.BIOME;
    public static final ResourceKey<Biome> GENSOKYO_PLAINS = getOrCreateRegistryKey("gensokyo_plains");
    public static final ResourceKey<Biome> GENSOKYO_JUNGLE = getOrCreateRegistryKey("gensokyo_jungle");
    public static final ResourceKey<Biome> GENSOKYO_PHANTOM_MEADOW = getOrCreateRegistryKey("gensokyo_phantom_meadow");
    public static final ResourceKey<Biome> GENSOKYO_MUENZUKA = getOrCreateRegistryKey("gensokyo_muenzuka");
    public static final ResourceKey<Biome> GENSOKYO_GARDEN_OF_THE_SUN = getOrCreateRegistryKey("gensokyo_garden_of_the_sun");

    public static void init() {

    }

    public static void bootstrap(BootstrapContext<Biome> context) {
        GensokyoForestBootstrap.biomes(context);
        BambooForestBootstrap.biomes(context);
        GensokyoLandmarkBootstrap.biomes(context);
        HolderGetter<PlacedFeature> placedFeatureLookup = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> configuredCarverLookup = context.lookup(Registries.CONFIGURED_CARVER);
        context.register(GENSOKYO_PLAINS, GensokyoPlainsBiomeCreator.create(
                placedFeatureLookup, configuredCarverLookup));
        context.register(DREAM, DreamBiomeCreator.createDream(
                placedFeatureLookup, configuredCarverLookup
        ));
        context.register(THE_MOON, TheMoonBiomeCreator.createTheMoon(
                placedFeatureLookup, configuredCarverLookup
        ));
        context.register(MOON_SEA, MoonSeaBiomeCreator.createMoonSea(
                placedFeatureLookup, configuredCarverLookup
        ));
        context.register(MoonBiomes.BASALT_MARE, TheMoonBiomeCreator.createMoonBiome(
                new BiomeGenerationSettings.Builder(placedFeatureLookup, configuredCarverLookup), true, false, false));
        context.register(MoonBiomes.SHADOW_ICE_CRATER, TheMoonBiomeCreator.createMoonBiome(
                new BiomeGenerationSettings.Builder(placedFeatureLookup, configuredCarverLookup), false, true, false));
        context.register(MAPLE_FOREST, MapleForestBiomeCreator.createMapleForest(
                placedFeatureLookup, configuredCarverLookup
        ));
        context.register(MAGIC_FOREST, MagicForestBiomeCreator.create(
                placedFeatureLookup, configuredCarverLookup));


    }

    public static ResourceKey<Biome> getOrCreateRegistryKey(String name) {
        return getOrCreateRegistryKey(ReverieDreams.id(name));
    }

    public static ResourceKey<Biome> getOrCreateRegistryKey(Identifier id) {
        return ResourceKey.create(Registries.BIOME, id);
    }

}
