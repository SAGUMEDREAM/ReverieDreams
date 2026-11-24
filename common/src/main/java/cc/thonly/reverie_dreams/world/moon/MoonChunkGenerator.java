package cc.thonly.reverie_dreams.world.moon;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.ToDoubleBiFunction;
import java.util.Objects;
import java.util.Set;

/**
 * Retains the previous noise surface as a ceiling, then cuts seeded basins into it.
 */
public final class MoonChunkGenerator extends ChunkGenerator {
    public static final MapCodec<MoonChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            MoonBiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.source),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(MoonChunkGenerator::settings)
    ).apply(i, MoonChunkGenerator::new));
    private final MoonBiomeSource source;
    private final Holder<NoiseGeneratorSettings> settings;
    private final NoiseBasedChunkGenerator baseline;
    private volatile long seed;
    private volatile ToDoubleBiFunction<Integer, Integer> reliefStrength = (x, z) -> 1;

    public MoonChunkGenerator(MoonBiomeSource source, Holder<NoiseGeneratorSettings> settings) {
        super(source);
        this.source = source;
        this.settings = settings;
        this.baseline = new NoiseBasedChunkGenerator(source, settings);
    }

    public Holder<NoiseGeneratorSettings> settings() {
        return settings;
    }

    /**
     * Installed per-world by the migration layer before any new chunks are generated.
     */
    public void setReliefStrength(ToDoubleBiFunction<Integer, Integer> strength) {
        this.reliefStrength = strength;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structures, RandomState random, long seed) {
        this.seed = seed;
        source.bindSeed(seed);
        return super.createState(structures, random, seed);
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState random, StructureManager structures, ChunkAccess chunk) {
        return baseline.fillFromNoise(blender, random, structures, chunk).thenApply(filled -> {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int x = 0; x < 16; x++)
                for (int z = 0; z < 16; z++) {
                    int wx = chunk.getPos().getMinBlockX() + x, wz = chunk.getPos().getMinBlockZ() + z;
                    int oldTop = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
                    MoonTerrain.Sample sample = MoonTerrain.sample(seed, wx, wz);
                    double strength = reliefStrength.applyAsDouble(wx, wz);
                    int floor = sample.floor(oldTop, strength), water = sample.waterTop(oldTop, floor, strength);
                    for (int y = MoonTerrain.MIN_Y; y <= oldTop; y++) {
                        BlockState state = stateAt(chunk.getBlockState(pos.set(wx, y, wz)), wx, y, wz, floor, water, sample, strength);
                        chunk.setBlockState(pos, state, Block.UPDATE_NONE);
                    }
                }
            Heightmap.primeHeightmaps(chunk, Set.of(Heightmap.Types.OCEAN_FLOOR_WG, Heightmap.Types.WORLD_SURFACE_WG));
            return filled;
        });
    }

    private BlockState stateAt(BlockState original, int x, int y, int z, int floor, int water, MoonTerrain.Sample sample, double strength) {
        if (y == MoonTerrain.MIN_Y)
            return Blocks.BEDROCK.defaultBlockState();
        if (y > floor)
            return y <= water ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
        if (strength > 0 && y >= floor - 3) {
            return switch (sample.region()) {
                case BASALT_MARE -> (y == floor ? Blocks.SMOOTH_BASALT : Blocks.BASALT).defaultBlockState();
                case SHADOW_ICE_CRATER ->
                        (sample.iceWeight() > 0.30 ? (y == floor ? Blocks.PACKED_ICE : Blocks.BLUE_ICE) : Blocks.SMOOTH_BASALT).defaultBlockState();
                case SEA_BOUNDARY -> (y == floor ? Blocks.CALCITE : Blocks.SMOOTH_BASALT).defaultBlockState();
                default -> settings.value().defaultBlock();
            };
        }
        if (y < MoonTerrain.LEGACY_MIN_Y)
            return MoonTerrain.cave(seed, x, y, z, floor) ? Blocks.AIR.defaultBlockState() : settings.value().defaultBlock();
        return original.is(Blocks.BEDROCK) ? settings.value().defaultBlock() : original;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        int oldTop = baseline.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, level, random) - 1;
        MoonTerrain.Sample sample = MoonTerrain.sample(seed, x, z);
        double strength = reliefStrength.applyAsDouble(x, z);
        int floor = sample.floor(oldTop, strength), water = sample.waterTop(oldTop, floor, strength);
        return (type.isOpaque().test(Blocks.WATER.defaultBlockState()) ? water : floor) + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        NoiseColumn original = baseline.getBaseColumn(x, z, level, random);
        int oldTop = baseline.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, level, random) - 1;
        MoonTerrain.Sample sample = MoonTerrain.sample(seed, x, z);
        double strength = reliefStrength.applyAsDouble(x, z);
        int floor = sample.floor(oldTop, strength), water = sample.waterTop(oldTop, floor, strength);
        BlockState[] states = new BlockState[MoonTerrain.HEIGHT];
        Arrays.fill(states, Blocks.AIR.defaultBlockState());
        for (int y = MoonTerrain.MIN_Y; y <= oldTop; y++)
            states[y - MoonTerrain.MIN_Y] = stateAt(original.getBlock(y), x, y, z, floor, water, sample, strength);
        return new NoiseColumn(MoonTerrain.MIN_Y, states);
    }

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structures, RandomState random, ChunkAccess chunk) {
        // Surface materials and the new basal bedrock are already applied in fillFromNoise.
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomes, StructureManager structures, ChunkAccess chunk) {
        // The legacy aquifer would fill new deep caves with lava. Carve dry lunar
        // passages here, before decoration, so their walls can receive ore veins.
        var passages = MoonCaves.passages(seed, chunk.getPos().x(), chunk.getPos().z(), (x, z) -> {
            int oldTop = baseline.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, level, random) - 1;
            return MoonTerrain.sample(seed, x, z).floor(oldTop, reliefStrength.applyAsDouble(x, z));
        });
        var pos = new BlockPos.MutableBlockPos();
        Block rock = settings.value().defaultBlock().getBlock();
        for (int x = 0; x < 16; x++)
            for (int z = 0; z < 16; z++) {
                int wx = chunk.getPos().getMinBlockX() + x, wz = chunk.getPos().getMinBlockZ() + z;
                int floor = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
                boolean open = reliefStrength.applyAsDouble(wx, wz) >= 0.999 && MoonCaves.canOpenSurface(seed, wx, wz);
                var cuts = passages.stream().map(p -> p.cut(wx, wz, floor, open)).filter(Objects::nonNull).toList();
                for (int y = MoonTerrain.MIN_Y + 11; y <= floor; y++) {
                    var original = chunk.getBlockState(pos.set(wx, y, wz));
                    if (!original.is(rock) && !original.is(Blocks.BASALT) && !original.is(Blocks.SMOOTH_BASALT)
                            && !original.is(Blocks.CALCITE))
                        continue;
                    boolean carve = MoonTerrain.cave(seed, wx, y, wz, floor);
                    if (!carve)
                        for (var cut : cuts)
                            if (cut.contains(y)) {
                                carve = true;
                                break;
                            }
                    if (carve)
                        chunk.setBlockState(pos, Blocks.CAVE_AIR.defaultBlockState(), Block.UPDATE_NONE);
                }
            }
        Heightmap.primeHeightmaps(chunk, Set.of(Heightmap.Types.OCEAN_FLOOR_WG, Heightmap.Types.WORLD_SURFACE_WG));
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
        baseline.spawnOriginalMobs(level);
    }

    @Override
    public int getMinY() {
        return MoonTerrain.MIN_Y;
    }

    @Override
    public int getGenDepth() {
        return MoonTerrain.HEIGHT;
    }

    @Override
    public int getSeaLevel() {
        return MoonTerrain.SEA_LEVEL;
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        info.add("Moon: " + MoonTerrain.sample(seed, pos.getX(), pos.getZ()).region());
    }
}
