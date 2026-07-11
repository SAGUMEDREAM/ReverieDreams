package cc.thonly.reverie_dreams.world.dimension;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.world.gen.ChunkGeneratorSettingsInit;
import cc.thonly.reverie_dreams.world.gen.RDBiomes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

import java.util.ArrayList;
import java.util.Optional;

public class DimensionInit {
    public static final ResourceKey<LevelStem> DREAM_WORLD = getOrCreateRegistryKey("dream_world");
    public static final ResourceKey<LevelStem> THE_MOON = getOrCreateRegistryKey("the_moon");
    public static final ResourceKey<LevelStem> GENSOKYO = getOrCreateRegistryKey("gensokyo");

    public static void bootstrap(BootstrapContext<LevelStem> context) {
        var dimensionTypeLookup = context.lookup(Registries.DIMENSION_TYPE);
        var biomeLookup = context.lookup(Registries.BIOME);
        var chunkGeneratorSettingsLookup = context.lookup(Registries.NOISE_SETTINGS);

        var dreamWorldConfig = new FlatLevelGeneratorSettings(
                Optional.empty(),
                biomeLookup.getOrThrow(RDBiomes.DREAM),
                new ArrayList<>()
        );
        dreamWorldConfig.setDecoration();
        dreamWorldConfig.setAddLakes();
        dreamWorldConfig.getLayersInfo().add(new FlatLayerInfo(4, Blocks.BARRIER.defaultBlockState().getBlock()));

        context.register(DREAM_WORLD, new LevelStem(
                dimensionTypeLookup.getOrThrow(DimensionTypeInit.DREAM_WORLD),
                new FlatLevelSource(
                        dreamWorldConfig
                )
        ));

        context.register(THE_MOON, new LevelStem(
                dimensionTypeLookup.getOrThrow(DimensionTypeInit.THE_MOON),
                new NoiseBasedChunkGenerator(
                        new FixedBiomeSource(
                                biomeLookup.getOrThrow(RDBiomes.THE_MOON)
                        ),
                        chunkGeneratorSettingsLookup.getOrThrow(ChunkGeneratorSettingsInit.MOON)
                )
        ));
    }

    public static void init() {
    }

    public static ResourceKey<LevelStem> getOrCreateRegistryKey(String name) {
        return ResourceKey.create(Registries.LEVEL_STEM, Identifier.fromNamespaceAndPath(ReverieDreams.MOD_ID, name));
    }

}
