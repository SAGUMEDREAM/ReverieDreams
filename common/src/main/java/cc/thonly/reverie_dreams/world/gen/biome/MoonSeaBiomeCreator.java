package cc.thonly.reverie_dreams.world.gen.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.world.attribute.AmbientMoodSettings;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class MoonSeaBiomeCreator {
    public static Biome createMoonSea(
            HolderGetter<PlacedFeature> placedFeatureLookup,
            HolderGetter<ConfiguredWorldCarver<?>> configuredCarverLookup
    ) {
        MobSpawnSettings.Builder mobs = new MobSpawnSettings.Builder();
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(
                placedFeatureLookup,
                configuredCarverLookup
        );
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)

                // 雾颜色
                .temperature(0.5F)
                .downfall(0F)

                .specialEffects(
                        new BiomeSpecialEffects.Builder()
                                .waterColor(0x303080)
                                .build()
                )
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 0x151020)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 0x101040)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 0x000000)
                .setAttribute(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                .mobSpawnSettings(
                        mobs.build()
                )

                .generationSettings(
                        generation.build()
                )

                .build();
    }
}
