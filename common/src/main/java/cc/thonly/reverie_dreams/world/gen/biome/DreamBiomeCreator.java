package cc.thonly.reverie_dreams.world.gen.biome;

import cc.thonly.reverie_dreams.world.gen.PlacedFeaturesInit;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.attribute.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class DreamBiomeCreator {
    private static Biome createDream(BiomeGenerationSettings.Builder builder) {
        MobSpawnSettings.Builder builder2 = new MobSpawnSettings.Builder();
        builder2.creatureGenerationProbability(0.07F);
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(2.0f)
                .downfall(0.5F)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 12607947)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12607947)
                .setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES, AmbientParticle.of(ParticleTypes.WHITE_ASH, 0.001F))
                .setAttribute(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(15545147)
                        .foliageColorOverride(14687012)
                        .grassColorOverride(15545147)
                        .build())
                .mobSpawnSettings(builder2.build())
                .generationSettings(builder.build())
                .build();
    }
    public static Biome createDream(HolderGetter<PlacedFeature> featureLookup, HolderGetter<ConfiguredWorldCarver<?>> carverLookup) {
        BiomeGenerationSettings.Builder lookupBackedBuilder = new BiomeGenerationSettings.Builder(featureLookup, carverLookup);
        lookupBackedBuilder.addFeature(GenerationStep.Decoration.RAW_GENERATION, PlacedFeaturesInit.DREAM_WORLD_GRID_KEY);
        lookupBackedBuilder.addFeature(GenerationStep.Decoration.RAW_GENERATION, PlacedFeaturesInit.FLOATING_DREAM_STONE_KEY);
        lookupBackedBuilder.addFeature(GenerationStep.Decoration.RAW_GENERATION, PlacedFeaturesInit.FLOATING_DREAM_CRYSTAL_KEY);
        lookupBackedBuilder.addFeature(GenerationStep.Decoration.RAW_GENERATION, PlacedFeaturesInit.FLOATING_DREAM_TRIAL_ROOM_ZOMBIE_KEY);
        lookupBackedBuilder.addFeature(GenerationStep.Decoration.RAW_GENERATION, PlacedFeaturesInit.FLOATING_DREAM_TRIAL_ROOM_SKELETON_KEY);
        lookupBackedBuilder.addFeature(GenerationStep.Decoration.RAW_GENERATION, PlacedFeaturesInit.DREAM_FLOATING_ISLAND_KEY);

        return createDream(lookupBackedBuilder);
    }
}
