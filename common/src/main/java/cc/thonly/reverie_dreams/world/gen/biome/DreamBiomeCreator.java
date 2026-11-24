package cc.thonly.reverie_dreams.world.gen.biome;

import cc.thonly.reverie_dreams.world.gen.RDBuiltinPlacedFeatures;
import cc.thonly.reverie_dreams.world.dream.DreamWorldBootstrap;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.attribute.AmbientParticle;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class DreamBiomeCreator {
    public static final int DREAM_GRASS_COLOR = 0x83AD58;
    public static final int DREAM_WATER_COLOR = 0xED9BC8;
    public static final int DREAM_WATER_FOG_COLOR = 0xC86AA4;

    private static Biome createDream(BiomeGenerationSettings.Builder builder) {
        MobSpawnSettings.Builder builder2 = new MobSpawnSettings.Builder();
        builder2.creatureGenerationProbability(0.07F);
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(2.0f)
                .downfall(0.5F)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, DREAM_WATER_FOG_COLOR)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 12607947)
                .setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES, AmbientParticle.of(ParticleTypes.WHITE_ASH, 0.001F))
                .setAttribute(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(DREAM_WATER_COLOR)
                        .foliageColorOverride(14687012)
                        .grassColorOverride(DREAM_GRASS_COLOR)
                        .build())
                .mobSpawnSettings(builder2.build())
                .generationSettings(builder.build())
                .build();
    }

    public static Biome createDream(HolderGetter<PlacedFeature> featureLookup, HolderGetter<ConfiguredWorldCarver<?>> carverLookup) {
        BiomeGenerationSettings.Builder lookupBackedBuilder = new BiomeGenerationSettings.Builder(featureLookup, carverLookup);
        lookupBackedBuilder.addFeature(GenerationStep.Decoration.RAW_GENERATION, RDBuiltinPlacedFeatures.DREAM_WORLD_GRID_KEY);
        lookupBackedBuilder.addFeature(GenerationStep.Decoration.RAW_GENERATION, RDBuiltinPlacedFeatures.FLOATING_DREAM_STONE_KEY);
        lookupBackedBuilder.addFeature(GenerationStep.Decoration.RAW_GENERATION, RDBuiltinPlacedFeatures.FLOATING_DREAM_CRYSTAL_KEY);
        lookupBackedBuilder.addFeature(GenerationStep.Decoration.RAW_GENERATION, DreamWorldBootstrap.ARCHIPELAGO_PLACED);

        return createDream(lookupBackedBuilder);
    }
}
