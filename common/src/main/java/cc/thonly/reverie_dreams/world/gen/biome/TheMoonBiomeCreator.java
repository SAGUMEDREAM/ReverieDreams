package cc.thonly.reverie_dreams.world.gen.biome;

import cc.thonly.reverie_dreams.world.gen.RDBuiltinPlacedFeatures;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.attribute.AmbientParticle;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class TheMoonBiomeCreator {
    public static Biome createMoonBiome(BiomeGenerationSettings.Builder builder, boolean basalt, boolean ice, boolean sea) {
        MobSpawnSettings.Builder builder2 = new MobSpawnSettings.Builder();
        builder2.creatureGenerationProbability(0.07F);
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(ice ? 0.15f : 0.5f)
                .downfall(0.5F)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, sea ? 0x153148 : 0xD9E0E8)
                .setAttribute(EnvironmentAttributes.FOG_COLOR, ice ? 0x141414 : sea ? 0x303030 : basalt ? 0x454545 : 0x707070)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 0)
                .setAttribute(EnvironmentAttributes.SKY_LIGHT_FACTOR, ice ? 0.12f : 0.5f)
                .setAttribute(EnvironmentAttributes.AMBIENT_PARTICLES, AmbientParticle.of(ice ? ParticleTypes.SNOWFLAKE : ParticleTypes.WHITE_ASH, 0.001F))
                .setAttribute(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(sea ? 0x405A88 : 0x303840)
                        .foliageColorOverride(16777215)
                        .grassColorOverride(6908265)
                        .build())
                .mobSpawnSettings(builder2.build())
                .generationSettings(builder.build()).build();
    }

    public static Biome createTheMoon(HolderGetter<PlacedFeature> featureLookup, HolderGetter<ConfiguredWorldCarver<?>> carverLookup) {
        BiomeGenerationSettings.Builder lookupBackedBuilder = new BiomeGenerationSettings.Builder(featureLookup, carverLookup);
        // The generator cuts craters before structures; the old deferred excavation could erase buildings.
        return createMoonBiome(lookupBackedBuilder, false, false, false);
    }
}
