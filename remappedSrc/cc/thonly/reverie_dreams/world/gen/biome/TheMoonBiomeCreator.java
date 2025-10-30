package cc.thonly.reverie_dreams.world.gen.biome;

import cc.thonly.reverie_dreams.world.gen.PlacedFeaturesInit;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.biome.*;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class TheMoonBiomeCreator {
    private static Biome createMoonBiome(BiomeGenerationSettings.Builder builder) {
        MobSpawnSettings.Builder builder2 = new MobSpawnSettings.Builder();
        builder2.creatureGenerationProbability(0.07F);
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(2.0f)
                .downfall(0.5F)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(0)
                        .waterFogColor(16777215)
                        .fogColor(10518688)
                        .skyColor(0)
                        .foliageColorOverride(16777215)
                        .grassColorOverride(6908265)
                        .ambientParticle(new AmbientParticleSettings(ParticleTypes.WHITE_ASH, 0.001F))
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .build())
                .mobSpawnSettings(builder2.build())
                .generationSettings(builder.build()).build();
    }

    public static Biome createTheMoon(HolderGetter<PlacedFeature> featureLookup, HolderGetter<ConfiguredWorldCarver<?>> carverLookup) {
        BiomeGenerationSettings.Builder lookupBackedBuilder = new BiomeGenerationSettings.Builder(featureLookup, carverLookup);
        lookupBackedBuilder.addFeature(Decoration.RAW_GENERATION, PlacedFeaturesInit.CRATER_MEGA_KEY);
        lookupBackedBuilder.addFeature(Decoration.RAW_GENERATION, PlacedFeaturesInit.CRATER_LARGE_KEY);
        lookupBackedBuilder.addFeature(Decoration.RAW_GENERATION, PlacedFeaturesInit.CRATER_SMALL_KEY);
        return createMoonBiome(lookupBackedBuilder);
    }
}