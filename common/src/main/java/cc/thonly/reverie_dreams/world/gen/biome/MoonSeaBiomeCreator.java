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
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(
                placedFeatureLookup,
                configuredCarverLookup
        );
        return TheMoonBiomeCreator.createMoonBiome(generation, false, false, true);
    }
}
