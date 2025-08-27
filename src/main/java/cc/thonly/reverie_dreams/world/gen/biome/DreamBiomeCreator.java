package cc.thonly.reverie_dreams.world.gen.biome;

import cc.thonly.reverie_dreams.world.gen.PlacedFeaturesInit;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.world.biome.*;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.feature.PlacedFeature;

public class DreamBiomeCreator {
    private static Biome createDream(GenerationSettings.LookupBackedBuilder builder) {
        SpawnSettings.Builder builder2 = new SpawnSettings.Builder();
        builder2.creatureSpawnProbability(0.07F);
        return new Biome.Builder()
                .precipitation(false)
                .temperature(-0.5F)
                .downfall(0.5F)
                .effects(new BiomeEffects.Builder()
                        .waterColor(15545147)
                        .waterFogColor(12607947)
                        .fogColor(12607947)
                        .skyColor(0)
                        .foliageColor(14687012)
                        .grassColor(15545147)
                        .particleConfig(new BiomeParticleConfig(ParticleTypes.WHITE_ASH, 0.001F))
                        .moodSound(BiomeMoodSound.CAVE)
                        .build())
                .spawnSettings(builder2.build())
                .generationSettings(builder.build())
                .build();
    }
    public static Biome createDream(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        lookupBackedBuilder.feature(GenerationStep.Feature.RAW_GENERATION, PlacedFeaturesInit.DREAM_WORLD_GRID_KEY);
        lookupBackedBuilder.feature(GenerationStep.Feature.RAW_GENERATION, PlacedFeaturesInit.FLOATING_DREAM_STONE_KEY);
        lookupBackedBuilder.feature(GenerationStep.Feature.RAW_GENERATION, PlacedFeaturesInit.FLOATING_DREAM_CRYSTAL_KEY);
        lookupBackedBuilder.feature(GenerationStep.Feature.RAW_GENERATION, PlacedFeaturesInit.FLOATING_DREAM_TRIAL_ROOM_ZOMBIE_KEY);
        lookupBackedBuilder.feature(GenerationStep.Feature.RAW_GENERATION, PlacedFeaturesInit.FLOATING_DREAM_TRIAL_ROOM_SKELETON_KEY);

        return createDream(lookupBackedBuilder);
    }
}
