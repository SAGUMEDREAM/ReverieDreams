package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.reverie_dreams.Touhou;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.source.FixedBiomeSource;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorLayer;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;

import java.util.ArrayList;
import java.util.Optional;

public class DimensionInit {
    public static final RegistryKey<DimensionOptions> DREAM_WORLD = getOrCreateRegistryKey("dream_world");
    public static final RegistryKey<DimensionOptions> THE_MOON = getOrCreateRegistryKey("the_moon");

    public static void bootstrap(Registerable<DimensionOptions> context) {
        var dimensionTypeLookup = context.getRegistryLookup(RegistryKeys.DIMENSION_TYPE);
        var biomeLookup = context.getRegistryLookup(RegistryKeys.BIOME);
        var chunkGeneratorSettingsLookup = context.getRegistryLookup(RegistryKeys.CHUNK_GENERATOR_SETTINGS);

        var dreamWorldConfig = new FlatChunkGeneratorConfig(
                Optional.empty(),
                biomeLookup.getOrThrow(BiomeInit.DREAM),
                new ArrayList<>()
        );
        dreamWorldConfig.enableFeatures();
        dreamWorldConfig.enableLakes();
        dreamWorldConfig.getLayers().add(new FlatChunkGeneratorLayer(4, Blocks.BARRIER.getDefaultState().getBlock()));

        context.register(DREAM_WORLD, new DimensionOptions(
                dimensionTypeLookup.getOrThrow(DimensionTypeInit.DREAM_WORLD),
                new FlatChunkGenerator(
                        dreamWorldConfig
                )
        ));

        context.register(THE_MOON, new DimensionOptions(
                dimensionTypeLookup.getOrThrow(DimensionTypeInit.THE_MOON),
                new NoiseChunkGenerator(
                        new FixedBiomeSource(
                                biomeLookup.getOrThrow(BiomeInit.THE_MOON)
                        ),
                        chunkGeneratorSettingsLookup.getOrThrow(ChunkGeneratorSettingsInit.MOON)
                )
        ));
    }

    public static void init() {
    }

    public static RegistryKey<DimensionOptions> getOrCreateRegistryKey(String name) {
        return RegistryKey.of(RegistryKeys.DIMENSION, Identifier.of(Touhou.MOD_ID, name));
    }

}
