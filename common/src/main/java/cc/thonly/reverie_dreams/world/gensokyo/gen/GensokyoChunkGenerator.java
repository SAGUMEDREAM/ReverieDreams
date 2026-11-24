package cc.thonly.reverie_dreams.world.gensokyo.gen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.world.gensokyo.GensokyoFeature;
import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapData;
import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapLoader;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.Strategy;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * 固定地图生成器：地形、流体和群系装饰，并补充适配高地表的矿层。
 *
 * <p>天然洞穴与原版结构保持关闭，为后续地下“旧地狱”留出空间。</p>
 */
public class GensokyoChunkGenerator extends ChunkGenerator {
    public static final Identifier DEFAULT_MAP = ReverieDreams.id("gensokyo");
    /**
     * Natural water is generated as a real water column instead of a single
     * surface block. The map data stores the water surface height; this depth is
     * derived bathymetry (the PNG has no riverbed depth). Shallow shore steps
     * lead into the deeper interior; smooth noise gives the bed gentle relief.
     */
    public static final int WATER_DEPTH = GensokyoRiverbed.MIN_DEPTH;
    /**
     * Lava lakes are generated as a full column, not a one-block surface crust.
     * Keeps the flat lava level contained by the basalt rim in the map data.
     */
    public static final int LAVA_DEPTH = 96;
    /**
     * Border mountain width is derived from map size instead of a fixed 256:
     * min(mapWidth, mapDepth) / 16, clamped to 256..1024.
     */
    public static final int BORDER_MIN_WIDTH = GensokyoBorderTerrain.SETTINGS.minWidth();
    public static final int BORDER_MAX_WIDTH = GensokyoBorderTerrain.SETTINGS.maxWidth();
    public static final int BORDER_WIDTH_DIVISOR = GensokyoBorderTerrain.SETTINGS.widthDivisor();
    /**
     * Compatibility estimate; actual mountain heights are spatially varying.
     */
    @Deprecated
    public static final int BORDER_PLATEAU_OFFSET = (int) Math.round(
            GensokyoBorderTerrain.SETTINGS.mountainOffset() + GensokyoBorderTerrain.SETTINGS.ridgeRelief());

    public static final MapCodec<GensokyoChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.getBiomeSource()),
            Identifier.CODEC.fieldOf("map").forGetter(GensokyoChunkGenerator::mapId)
    ).apply(instance, GensokyoChunkGenerator::new));

    private final Identifier mapId;
    private final ConcurrentHashMap<String, BlockState> borderSurfaceStates = new ConcurrentHashMap<>();
    private volatile GensokyoMapData mapData;
    private volatile GensokyoBorderTerrain borderTerrain;
    private volatile GensokyoStructureGeneration authoredStructures;

    public GensokyoStructureGeneration authoredStructures(ServerLevel level) {
        var value = this.authoredStructures;
        if (value == null)
            synchronized (this) {
                if (this.authoredStructures == null)
                    this.authoredStructures = new GensokyoStructureGeneration(level, this.map());
                value = this.authoredStructures;
            }
        return value;
    }

    public GensokyoChunkGenerator(BiomeSource biomeSource, Identifier mapId) {
        super(biomeSource);
        this.mapId = mapId;
    }

    public Identifier mapId() {
        return this.mapId;
    }

    private GensokyoMapData map() {
        GensokyoMapData data = this.mapData;
        if (data == null) {
            data = GensokyoMapLoader.get(this.mapId);
            this.mapData = data;
        }
        return data;
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> structureSetLookup, RandomState randomState, long seed) {
        // 固定地图建筑由 map anchors 安装；随机结构集暂不启用，避免与指定位置的建筑重叠。
        return ChunkGeneratorStructureState.createForFlat(randomState, seed, this.biomeSource, Stream.empty());
    }

    /**
     * 原版会为每个 section（96 个）逐列调用群系源；这里按 quart 列缓存，只解析 16 列。
     */
    @Override
    public CompletableFuture<ChunkAccess> createBiomes(RandomState randomState, Blender blender,
                                                       StructureManager structureManager, ChunkAccess chunk) {
        if (!(this.biomeSource instanceof GensokyoBiomeSource source)) {
            return super.createBiomes(randomState, blender, structureManager, chunk);
        }
        return CompletableFuture.supplyAsync(() -> {
            chunk.fillBiomesFromNoise(GensokyoBiomeSource.cachedResolver(source), randomState.sampler());
            return chunk;
        }, Util.backgroundExecutor().forName("init_biomes"));
    }

    @Override
    public void createStructures(RegistryAccess registries, ChunkGeneratorStructureState structureState,
                                 StructureManager structureManager, ChunkAccess chunk,
                                 StructureTemplateManager templateManager, ResourceKey<Level> levelKey) {
        if (!GensokyoFeature.isEnabled()) {
            return;
        }
        ChunkPos chunkPos = chunk.getPos();
        if (!this.map().contains(chunkPos.getMiddleBlockX(), chunkPos.getMiddleBlockZ())) {
            return;
        }
        super.createStructures(registries, structureState, structureManager, chunk, templateManager, levelKey);
    }

    @Override
    public Pair<BlockPos, Holder<Structure>> findNearestMapStructure(ServerLevel level, HolderSet<Structure> structures,
                                                                     BlockPos origin, int radius, boolean skipExistingChunks) {
        Pair<BlockPos, Holder<Structure>> result =
                super.findNearestMapStructure(level, structures, origin, radius, skipExistingChunks);
        if (result != null && !this.map().contains(result.getFirst().getX(), result.getFirst().getZ())) {
            return null;
        }
        return result;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        if (!GensokyoFeature.isEnabled()) {
            // GensokyoFeature.ENABLED 关闭时不生成幻想乡地形。
            GensokyoFeature.logDisabledOnce();
            return CompletableFuture.completedFuture(chunk);
        }
        GensokyoMapData map = this.map();
        FillStates states = FillStates.create();
        ChunkPos chunkPos = chunk.getPos();
        int maxY = chunk.getMinY() + chunk.getHeight() - 1;
        int bottom = Math.max(map.minY(), chunk.getMinY());
        Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int firstFullStoneY = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(bottom) + 1);
        int filledStoneEnd = initializeStoneSections(chunk, map, firstFullStoneY, maxY, states.stone());

        for (int localX = 0; localX < 16; localX++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldX = chunkPos.getMinBlockX() + localX;
                int worldZ = chunkPos.getMinBlockZ() + localZ;
                int surfaceY = map.heightAt(worldX, worldZ);
                if (surfaceY == GensokyoMapData.NO_SURFACE) {
                    if (!map.contains(worldX, worldZ))
                        fillBorderTerrain(chunk, pos, localX, localZ, worldX, worldZ, map, maxY, oceanFloor, worldSurface, states);
                    continue;
                }

                BlockState surfaceState = map.surfaceStateAt(worldX, worldZ);
                if (surfaceState.isAir()) {
                    continue;
                }

                int top = Math.min(surfaceY, maxY);
                if (top < bottom) {
                    continue;
                }

                boolean waterColumn = surfaceState.is(Blocks.WATER);
                boolean lavaColumn = surfaceState.is(Blocks.LAVA);
                int fluidDepth = waterColumn ? waterDepthAt(map, worldX, worldZ) : lavaColumn ? LAVA_DEPTH : 0;
                BlockState riverbed = waterColumn ? riverbedStateAt(worldX, worldZ, states) : null;
                BlockState fluidState = waterColumn ? states.water() : lavaColumn ? states.lava() : null;
                int start = bottom;
                if (filledStoneEnd > firstFullStoneY) {
                    setBlock(chunk, localX, bottom, localZ, states.bedrock());
                    for (int y = bottom + 1; y < firstFullStoneY; y++) {
                        setBlock(chunk, localX, y, localZ, states.stone());
                    }
                    start = filledStoneEnd;
                }
                for (int y = start; y <= top; y++) {
                    BlockState state;
                    if (fluidState != null && y > top - fluidDepth) {
                        state = fluidState;
                    } else if (y == top) {
                        state = surfaceState;
                    } else if (y == bottom) {
                        state = states.bedrock();
                    } else if (waterColumn && y >= top - fluidDepth - 1) {
                        state = riverbed;
                    } else if (y >= top - map.dirtDepth() && !surfaceState.is(Blocks.STONE)) {
                        state = states.dirt();
                    } else {
                        state = states.stone();
                    }
                    setBlock(chunk, localX, y, localZ, state);
                }

                int waterTop = Math.min(map.seaLevel(), maxY);
                for (int y = top + 1; y <= waterTop; y++) {
                    setBlock(chunk, localX, y, localZ, states.water());
                }
                int oceanTop = waterColumn ? Math.max(bottom, top - fluidDepth)
                        : lavaColumn ? Math.max(bottom, top - LAVA_DEPTH)
                        : Heightmap.Types.OCEAN_FLOOR_WG.isOpaque().test(surfaceState) ? top : Math.max(bottom, top - 1);
                updateColumnHeightmaps(chunk, pos, localX, localZ, Math.max(top, waterTop), oceanTop, oceanFloor, worldSurface);
            }
        }

        this.map().roads().clearHeadroom(chunk);
        this.applyUndergroundOverrides(chunk);

        return CompletableFuture.completedFuture(chunk);
    }

    /**
     * Installs the uninterrupted stone interval as single-value vanilla palettes.
     */
    private static int initializeStoneSections(ChunkAccess chunk, GensokyoMapData map, int firstY, int maxY, BlockState stone) {
        if (!(chunk instanceof ProtoChunk proto) || proto.getPersistedStatus().isOrAfter(ChunkStatus.NOISE)
                || proto.getBelowZeroRetrogen() != null) {
            return firstY;
        }
        ChunkPos pos = chunk.getPos();
        if (!map.contains(pos.getMinBlockX(), pos.getMinBlockZ())
                || !map.contains(pos.getMaxBlockX(), pos.getMaxBlockZ())) {
            return firstY;
        }

        int end = maxY + 1;
        for (int z = pos.getMinBlockZ(); z <= pos.getMaxBlockZ(); z++) {
            for (int x = pos.getMinBlockX(); x <= pos.getMaxBlockX(); x++) {
                int top = Math.min(map.heightAt(x, z), maxY);
                BlockState surface = map.surfaceStateAt(x, z);
                if (top == GensokyoMapData.NO_SURFACE || surface.isAir())
                    return firstY;
                // The shallowest possible mixed block bounds the stone interval, even on a deep bank.
                int columnEnd = top - map.dirtDepth();
                if (surface.is(Blocks.WATER))
                    columnEnd = Math.min(columnEnd, top - GensokyoRiverbed.MAX_DEPTH - 1);
                if (surface.is(Blocks.LAVA))
                    columnEnd = Math.min(columnEnd, top - LAVA_DEPTH + 1);
                end = Math.min(end, columnEnd);
                if (end - firstY < 16)
                    return firstY;
            }
        }

        LevelChunkSection[] sections = chunk.getSections();
        Strategy<BlockState> strategy = Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY);
        int y = firstY;
        while (y + 16 <= end) {
            int index = chunk.getSectionIndex(y);
            LevelChunkSection prior = sections[index];
            if (!prior.hasOnlyAir())
                break;
            sections[index] = new LevelChunkSection(new PalettedContainer<>(stone, strategy), prior.getBiomes());
            y += 16;
        }
        return y;
    }

    /**
     * 重新应用编辑器的稀疏地下覆盖。原版 feature 可能写入矿石/植被，这里让手工数据优先。
     */
    private void applyUndergroundOverrides(ChunkAccess chunk) {
        GensokyoMapData map = this.map();
        ChunkPos chunkPos = chunk.getPos();
        int maxY = chunk.getMinY() + chunk.getHeight() - 1;
        Heightmap oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (GensokyoMapData.UndergroundBlock override : map.undergroundAt(chunkPos)) {
            int localX = override.x() - chunkPos.getMinBlockX();
            int localZ = override.z() - chunkPos.getMinBlockZ();
            int y = override.y();
            if (localX < 0 || localX >= 16 || localZ < 0 || localZ >= 16
                    || y < chunk.getMinY() || y > maxY) {
                continue;
            }
            setBlockTracked(chunk, pos, localX, y, localZ, override.state(), oceanFloor, worldSurface);
        }
    }

    /**
     * 批量填充直接写 section。NOISE 阶段的 ProtoChunk.setBlockState 会对两张 WG 高度图
     * 逐方块更新；这里跳过，由 {@link #updateColumnHeightmaps} 每列补一次。
     */
    private static void setBlock(ChunkAccess chunk, int localX, int y, int localZ, BlockState state) {
        chunk.getSection(chunk.getSectionIndex(y)).setBlockState(localX, y & 15, localZ, state);
    }

    private static void setBlockTracked(ChunkAccess chunk, BlockPos.MutableBlockPos pos, int localX, int y, int localZ,
                                        BlockState state, Heightmap oceanFloor, Heightmap worldSurface) {
        chunk.setBlockState(pos.set(localX, y, localZ), state, Block.UPDATE_NONE);
        oceanFloor.update(localX, y, localZ, state);
        worldSurface.update(localX, y, localZ, state);
    }

    /**
     * 批量填充的高度图只在每列最高的相关方块处更新一次；调用点传入的
     * block state 必须来自该列已填好的方块，保证与原逐方块更新结果一致。
     */
    private static void updateColumnHeightmaps(ChunkAccess chunk, BlockPos.MutableBlockPos pos,
                                               int localX, int localZ, int worldTop, int oceanTop,
                                               Heightmap oceanFloor, Heightmap worldSurface) {
        worldSurface.update(localX, worldTop, localZ, chunk.getBlockState(pos.set(localX, worldTop, localZ)));
        oceanFloor.update(localX, oceanTop, localZ, chunk.getBlockState(pos.set(localX, oceanTop, localZ)));
    }

    private static BlockState riverbedStateAt(int x, int z, FillStates states) {
        return switch (GensokyoRiverbed.materialAt(x, z)) {
            case 1 -> states.sand();
            case 2 -> states.clay();
            case 3 -> states.stone();
            default -> states.gravel();
        };
    }

    private static int waterDepthAt(GensokyoMapData map, int x, int z) {
        return GensokyoRiverbed.depthAt(x, z,
                (sampleX, sampleZ) -> map.surfaceStateAt(sampleX, sampleZ).is(Blocks.WATER),
                (bankX, bankZ) -> map.contains(bankX, bankZ) && map.heightAt(bankX, bankZ) <= map.heightAt(x, z));
    }

    public GensokyoMapData mapData() {
        return this.map();
    }

    public GensokyoBorderTerrain borderTerrain() {
        var terrain = this.borderTerrain;
        if (terrain == null)
            this.borderTerrain = terrain = new GensokyoBorderTerrain(GensokyoBorderMapView.of(this.map()));
        return terrain;
    }

    private BlockState borderSurface(String id) {
        return this.borderSurfaceStates.computeIfAbsent(id,
                key -> BuiltInRegistries.BLOCK.getValue(Identifier.parse(key)).defaultBlockState());
    }

    private void fillBorderTerrain(ChunkAccess chunk, BlockPos.MutableBlockPos pos,
                                   int localX, int localZ, int worldX, int worldZ,
                                   GensokyoMapData map, int maxY,
                                   Heightmap oceanFloor, Heightmap worldSurface, FillStates states) {
        var column = borderTerrain().sample(worldX, worldZ);
        var surface = borderSurface(column.surface());
        int bottom = Math.max(chunk.getMinY(), map.minY());
        int top = Math.min(maxY, column.visibleY());
        for (int y = bottom; y <= top; y++) {
            setBlock(chunk, localX, y, localZ, borderState(column, surface, y, bottom, states));
        }
        if (top < bottom) {
            return;
        }
        int oceanTop;
        if (column.water()) {
            oceanTop = Heightmap.Types.OCEAN_FLOOR_WG.isOpaque().test(surface)
                    ? column.groundY() : Math.max(bottom, column.groundY() - 2);
        } else {
            oceanTop = Heightmap.Types.OCEAN_FLOOR_WG.isOpaque().test(surface)
                    ? top : Math.max(bottom, top - 1);
        }
        oceanTop = Math.max(bottom, Math.min(oceanTop, top));
        updateColumnHeightmaps(chunk, pos, localX, localZ, top, oceanTop, oceanFloor, worldSurface);
    }

    private static BlockState borderState(GensokyoBorderTerrain.Column column, BlockState surface, int y, int bottom,
                                          FillStates states) {
        if (y == bottom)
            return states.bedrock();
        if (column.water() && y > column.groundY())
            return states.water();
        if (y == column.groundY() || (column.water() && y >= column.groundY() - 1))
            return surface;
        boolean soil = surface.is(Blocks.GRASS_BLOCK) || surface.is(Blocks.PODZOL) || surface.is(Blocks.MYCELIUM)
                || surface.is(Blocks.COARSE_DIRT) || surface.is(Blocks.ROOTED_DIRT) || surface.is(Blocks.MOSS_BLOCK);
        if (soil && y >= column.groundY() - 3)
            return states.dirt();
        return states.stone();
    }

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState random, ChunkAccess chunk) {
        // 表面已经在 fillFromNoise 里填好了。
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager,
                             StructureManager structureManager, ChunkAccess chunk) {
        // P0 不挖洞穴。
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
        // P0 不生成初始生物。
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        if (!GensokyoFeature.isEnabled()) {
            return;
        }
        var authored = this.authoredStructures(level.getLevel());
        // 原版按群系放置树木、花草、矿石等 feature；随后重新应用地下覆盖，保证编辑器数据优先。
        super.applyBiomeDecoration(level, chunk, structureManager);
        GensokyoOreGeneration.place(level, this, chunk.getPos(), this.map());
        this.map().roads().clearHeadroom(chunk);
        this.applyUndergroundOverrides(chunk);
        authored.generate(chunk);
    }

    @Override
    public int getSpawnHeight(LevelHeightAccessor level) {
        return this.map().spawnPos().getY();
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        GensokyoMapData map = this.map();
        int maxY = map.minY() + map.worldHeight() - 1;
        if (!map.contains(x, z)) {
            var column = borderTerrain().sample(x, z);
            boolean floor = type == Heightmap.Types.OCEAN_FLOOR || type == Heightmap.Types.OCEAN_FLOOR_WG;
            return (floor ? column.groundY() : column.visibleY()) + 1;
        }
        int surfaceY = map.heightAt(x, z);
        int roadY = map.roads().pavementY(x, z);
        if (roadY != Short.MIN_VALUE)
            return Math.max(surfaceY, roadY) + 1;
        if (surfaceY == GensokyoMapData.NO_SURFACE) {
            return map.minY();
        }
        if (type == Heightmap.Types.WORLD_SURFACE || type == Heightmap.Types.WORLD_SURFACE_WG) {
            return Math.max(surfaceY + 1, map.seaLevel() + 1);
        }
        if ((type == Heightmap.Types.OCEAN_FLOOR || type == Heightmap.Types.OCEAN_FLOOR_WG)
                && map.surfaceStateAt(x, z).is(Blocks.WATER)) {
            return Math.max(map.minY() + 1, surfaceY - waterDepthAt(map, x, z) + 1);
        }
        return surfaceY + 1;
    }

    @FunctionalInterface
    interface ColumnSpanSink {
        void span(int bottomY, int topY, BlockState state);
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        GensokyoMapData map = this.map();
        int minY = map.minY();
        BlockState[] states = new BlockState[map.worldHeight()];
        Arrays.fill(states, Blocks.AIR.defaultBlockState());
        this.emitColumn(x, z, (bottomY, topY, state) -> Arrays.fill(states, bottomY - minY, topY - minY, state));
        return new NoiseColumn(minY, states);
    }

    /**
     * 共用同一套整列规则：getBaseColumn 填数组，LOD 采样直接拿连续段，避免逐格全高扫描。
     */
    void emitColumn(int x, int z, ColumnSpanSink sink) {
        GensokyoMapData map = this.map();
        FillStates fill = FillStates.create();
        int minY = map.minY();
        int maxY = minY + map.worldHeight() - 1;
        BlockState air = Blocks.AIR.defaultBlockState();
        ColumnRun run = new ColumnRun(sink);

        if (!map.contains(x, z)) {
            var column = borderTerrain().sample(x, z);
            var surface = borderSurface(column.surface());
            int top = Math.min(maxY, column.visibleY());
            if (top < minY) {
                sink.span(minY, maxY + 1, air);
                return;
            }
            int groundY = column.groundY();
            int[] stops = {minY + 1, groundY - 3, groundY - 1, groundY, groundY + 1, top + 1};
            Arrays.sort(stops);
            int start = minY;
            for (int stop : stops) {
                if (start > top)
                    break;
                int end = Math.min(stop, top + 1);
                if (end <= start)
                    continue;
                run.push(start, borderState(column, surface, start, minY, fill));
                start = end;
            }
            run.end(top + 1);
            if (top < maxY)
                sink.span(top + 1, maxY + 1, air);
            return;
        }

        int surfaceY = map.heightAt(x, z);
        BlockState surfaceState = map.surfaceStateAt(x, z);
        if (surfaceY == GensokyoMapData.NO_SURFACE || surfaceState.isAir()) {
            sink.span(minY, maxY + 1, air);
            return;
        }

        boolean waterColumn = surfaceState.is(Blocks.WATER);
        boolean lavaColumn = surfaceState.is(Blocks.LAVA);
        int fluidDepth = waterColumn ? waterDepthAt(map, x, z) : lavaColumn ? LAVA_DEPTH : 0;
        BlockState riverbed = waterColumn ? riverbedStateAt(x, z, fill) : null;
        BlockState fluidState = waterColumn ? fill.water() : lavaColumn ? fill.lava() : null;
        int surfaceTop = Math.min(surfaceY, maxY);
        int dirtDepth = map.dirtDepth();
        boolean dirtLayer = !surfaceState.is(Blocks.STONE);
        int[] stops = {minY + 1, surfaceY - dirtDepth, surfaceY - fluidDepth - 1,
                surfaceY - fluidDepth + 1, surfaceY, surfaceTop + 1};
        Arrays.sort(stops);
        int start = minY;
        for (int stop : stops) {
            if (start > surfaceTop)
                break;
            int end = Math.min(stop, surfaceTop + 1);
            if (end <= start)
                continue;
            run.push(start, columnState(start, minY, surfaceY, surfaceState, waterColumn, riverbed, fluidState,
                    fluidDepth, dirtDepth, dirtLayer, fill));
            start = end;
        }
        run.end(surfaceTop + 1);

        int top = surfaceTop;
        if (surfaceY < map.seaLevel()) {
            int waterTop = Math.min(map.seaLevel(), maxY);
            if (waterTop > surfaceY) {
                sink.span(surfaceY + 1, waterTop + 1, fill.water());
                top = waterTop;
            }
        }
        if (top < maxY)
            sink.span(top + 1, maxY + 1, air);
    }

    private static BlockState columnState(int y, int minY, int surfaceY, BlockState surfaceState,
                                          boolean waterColumn, BlockState riverbed, BlockState fluidState,
                                          int fluidDepth, int dirtDepth, boolean dirtLayer, FillStates fill) {
        if (fluidState != null && y > surfaceY - fluidDepth)
            return fluidState;
        if (y == surfaceY)
            return surfaceState;
        if (y == minY)
            return fill.bedrock();
        if (waterColumn && y >= surfaceY - fluidDepth - 1)
            return riverbed;
        if (y >= surfaceY - dirtDepth && dirtLayer)
            return fill.dirt();
        return fill.stone();
    }

    private static final class ColumnRun {
        private final ColumnSpanSink sink;
        private int bottom;
        private BlockState state;

        private ColumnRun(ColumnSpanSink sink) {
            this.sink = sink;
        }

        private void push(int y, BlockState next) {
            if (next == this.state)
                return;
            if (this.state != null)
                this.sink.span(this.bottom, y, this.state);
            this.bottom = y;
            this.state = next;
        }

        private void end(int y) {
            if (this.state != null)
                this.sink.span(this.bottom, y, this.state);
        }
    }

    @Override
    public int getMinY() {
        return this.map().minY();
    }

    @Override
    public int getGenDepth() {
        return this.map().worldHeight();
    }

    @Override
    public int getSeaLevel() {
        return this.map().seaLevel();
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        GensokyoMapData map = this.map();
        info.add("Gensokyo map: " + map.id());
        if (map.contains(pos.getX(), pos.getZ())) {
            info.add("map height=" + map.heightAt(pos.getX(), pos.getZ()));
            info.add("map biome=" + map.biomeIndexAt(pos.getX(), pos.getZ()));
            info.add("map surface=" + map.surfaceStateAt(pos.getX(), pos.getZ()));
        }
    }

    private record FillStates(BlockState bedrock, BlockState dirt, BlockState stone, BlockState water,
                              BlockState lava, BlockState gravel, BlockState sand, BlockState clay) {
        static FillStates create() {
            return new FillStates(Blocks.BEDROCK.defaultBlockState(), Blocks.DIRT.defaultBlockState(),
                    Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), Blocks.LAVA.defaultBlockState(),
                    Blocks.GRAVEL.defaultBlockState(), Blocks.SAND.defaultBlockState(), Blocks.CLAY.defaultBlockState());
        }
    }
}
