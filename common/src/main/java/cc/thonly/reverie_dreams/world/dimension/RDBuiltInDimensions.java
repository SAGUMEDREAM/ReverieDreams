package cc.thonly.reverie_dreams.world.dimension;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapData;
import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapLoader;
import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoBiomeSource;
import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoChunkGenerator;
import cc.thonly.reverie_dreams.world.gen.RDBuiltinChunkGeneratorSettings;
import cc.thonly.reverie_dreams.world.gen.RDBuiltinBiomes;
import cc.thonly.reverie_dreams.world.moon.MoonBiomes;
import cc.thonly.reverie_dreams.world.moon.MoonBiomeSource;
import cc.thonly.reverie_dreams.world.moon.MoonChunkGenerator;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RDBuiltInDimensions {
    public static final ResourceKey<LevelStem> DREAM_WORLD = getOrCreateRegistryKey("dream_world");
    public static final ResourceKey<LevelStem> THE_MOON = getOrCreateRegistryKey("the_moon");
    public static final ResourceKey<LevelStem> GENSOKYO = getOrCreateRegistryKey("gensokyo");

    public static void bootstrap(BootstrapContext<LevelStem> context) {
        // 数据生成阶段顺便校验地图数据，避免 dimension JSON 生成了但运行时数据是坏的。
        GensokyoMapData gensokyoMap = GensokyoMapLoader.get(GensokyoChunkGenerator.DEFAULT_MAP);

        var dimensionTypeLookup = context.lookup(Registries.DIMENSION_TYPE);
        var biomeLookup = context.lookup(Registries.BIOME);
        var chunkGeneratorSettingsLookup = context.lookup(Registries.NOISE_SETTINGS);

        var dreamWorldConfig = new FlatLevelGeneratorSettings(
                Optional.empty(),
                biomeLookup.getOrThrow(RDBuiltinBiomes.DREAM),
                new ArrayList<>()
        );
        dreamWorldConfig.setDecoration();
        dreamWorldConfig.setAddLakes();
        dreamWorldConfig.getLayersInfo().add(new FlatLayerInfo(4, Blocks.BARRIER.defaultBlockState().getBlock()));

        context.register(DREAM_WORLD, new LevelStem(
                dimensionTypeLookup.getOrThrow(RDBuiltInDimensionTypes.DREAM_WORLD),
                new FlatLevelSource(
                        dreamWorldConfig
                )
        ));

        context.register(THE_MOON, new LevelStem(
                dimensionTypeLookup.getOrThrow(RDBuiltInDimensionTypes.THE_MOON),
                new MoonChunkGenerator(
                        new MoonBiomeSource(biomeLookup.getOrThrow(RDBuiltinBiomes.THE_MOON),
                                biomeLookup.getOrThrow(MoonBiomes.BASALT_MARE),
                                biomeLookup.getOrThrow(MoonBiomes.SHADOW_ICE_CRATER),
                                biomeLookup.getOrThrow(RDBuiltinBiomes.MOON_SEA)),
                        chunkGeneratorSettingsLookup.getOrThrow(RDBuiltinChunkGeneratorSettings.MOON)
                )
        ));

        context.register(GENSOKYO, new LevelStem(
                dimensionTypeLookup.getOrThrow(RDBuiltInDimensionTypes.GENSOKYO),
                new GensokyoChunkGenerator(
                        new GensokyoBiomeSource(
                                GensokyoChunkGenerator.DEFAULT_MAP,
                                gensokyoMap.biomePalette().stream()
                                        .map(id -> biomeLookup.getOrThrow(ResourceKey.create(Registries.BIOME, id)))
                                        .map(holder -> (Holder<Biome>) holder)
                                        .toList()
                        ),
                        GensokyoChunkGenerator.DEFAULT_MAP
                )
        ));
    }

    public static void init() {
    }

    public static ResourceKey<LevelStem> getOrCreateRegistryKey(String name) {
        return ResourceKey.create(Registries.LEVEL_STEM, Identifier.fromNamespaceAndPath(ReverieDreams.MOD_ID, name));
    }

}
