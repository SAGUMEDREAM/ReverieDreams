package cc.thonly.reverie_dreams.world.gen.biome;

import cc.thonly.reverie_dreams.world.gen.PlacedFeaturesInit;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.world.biome.*;
import net.minecraft.world.gen.GenerationStep.Feature;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.PlacedFeature;

public class TheMoonBiomeCreator {
    private static Biome createMoonBiome(GenerationSettings.LookupBackedBuilder builder) {
        SpawnSettings.Builder builder2 = new SpawnSettings.Builder();
        builder2.creatureSpawnProbability(0.07F);
        return new Biome.Builder()
                .precipitation(false)
                .temperature(2.0f)
                .downfall(0.5F)
                .effects(new BiomeEffects.Builder()
                        .waterColor(0)
                        .waterFogColor(16777215)
                        .fogColor(10518688)
                        .skyColor(0)
                        .foliageColor(16777215)
                        .grassColor(6908265)
                        .particleConfig(new BiomeParticleConfig(ParticleTypes.WHITE_ASH, 0.001F))
                        .moodSound(BiomeMoodSound.CAVE)
                        .build())
                .spawnSettings(builder2.build())
                .generationSettings(builder.build()).build();
    }

    public static Biome createTheMoon(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        lookupBackedBuilder.feature(Feature.RAW_GENERATION, PlacedFeaturesInit.CRATER_MEGA_KEY);
        lookupBackedBuilder.feature(Feature.RAW_GENERATION, PlacedFeaturesInit.CRATER_LARGE_KEY);
        lookupBackedBuilder.feature(Feature.RAW_GENERATION, PlacedFeaturesInit.CRATER_SMALL_KEY);
        return createMoonBiome(lookupBackedBuilder);
    }
}