package cc.thonly.reverie_dreams.world.dream;

import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.world.dream.scene.DreamIslandScenes;
import cc.thonly.reverie_dreams.world.dream.scene.DreamScene;
import cc.thonly.reverie_dreams.world.dream.scene.IslandType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Random;
import java.util.EnumMap;
import java.util.Locale;

/**
 * Deterministic, chunk-clipped islands. No delayed writes or changes to existing chunks.
 */
public final class DreamArchipelagoLayout {
    public static final int CLUSTER_SPACING = 512;
    public static final int FLOOR_Y = 128;

    public record Module(String id, BlockPos origin, DreamScene scene) {
        public BlockPos landing() {
            return origin.offset(scene.landing());
        }
    }

    public record Placement(BlockPos pos, BlockState state) {
    }

    public record ChestPlacement(BlockPos pos, ResourceKey<LootTable> table, long seed) {
    }

    public record Bounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        public boolean contains(BlockPos pos) {
            return pos.getX() >= minX && pos.getX() <= maxX
                    && pos.getY() >= minY && pos.getY() <= maxY
                    && pos.getZ() >= minZ && pos.getZ() <= maxZ;
        }
    }

    // Registry-backed block states must not be resolved while the feature registry is initializing.
    private static final class Plans {
        private static final List<Module> MODULES = List.of(
                new Module("station", new BlockPos(0, FLOOR_Y, 0), DreamIslandScenes.station()),
                new Module("bamboo", new BlockPos(-112, FLOOR_Y, 0), DreamIslandScenes.bamboo()),
                new Module("tea_garden", new BlockPos(0, FLOOR_Y, 112), DreamIslandScenes.teaGarden()),
                new Module("mirror_pond", new BlockPos(112, FLOOR_Y, 0), DreamIslandScenes.mirrorPond()));
        private static final Map<BlockPos, BlockState> BLOCKS = build();
        private static final Map<Long, List<Placement>> CHUNKS = indexChunks();
        private static final List<Bounds> RESERVED = bounds();

        private static Map<BlockPos, BlockState> build() {
            Map<BlockPos, BlockState> result = new LinkedHashMap<>();
            for (Module module : MODULES) {
                module.scene.blocks().forEach((pos, state) -> result.put(module.origin.offset(pos), state));
            }
            connect(result, Direction.WEST, MODULES.get(1), Direction.EAST);
            connect(result, Direction.SOUTH, MODULES.get(2), Direction.NORTH);
            connect(result, Direction.EAST, MODULES.get(3), Direction.WEST);
            return Map.copyOf(result);
        }

        private static void connect(Map<BlockPos, BlockState> result, Direction out, Module destination, Direction in) {
            Module station = MODULES.getFirst();
            BlockPos start = station.origin.offset(station.scene.exits().get(out));
            BlockPos end = destination.origin.offset(destination.scene.exits().get(in));
            if (start.getY() != FLOOR_Y || end.getY() != FLOOR_Y
                    || (start.getX() != end.getX() && start.getZ() != end.getZ())) {
                throw new IllegalStateException("Dream scene ports must share a straight, level approach: " + destination.id);
            }
            boolean alongX = start.getZ() == end.getZ();
            int from = alongX ? Math.min(start.getX(), end.getX()) : Math.min(start.getZ(), end.getZ());
            int to = alongX ? Math.max(start.getX(), end.getX()) : Math.max(start.getZ(), end.getZ());
            BlockState rail = Blocks.BAMBOO_FENCE.defaultBlockState()
                    .setValue(alongX ? FenceBlock.EAST : FenceBlock.NORTH, true)
                    .setValue(alongX ? FenceBlock.WEST : FenceBlock.SOUTH, true);
            for (int step = from; step <= to; step++) {
                for (int side = -2; side <= 2; side++) {
                    BlockPos floor = alongX ? new BlockPos(step, FLOOR_Y, start.getZ() + side)
                            : new BlockPos(start.getX() + side, FLOOR_Y, step);
                    result.put(floor, RDBlocks.DREAM_STONE_BRICK.block().defaultBlockState());
                    for (int y = 1; y <= 4; y++)
                        result.put(floor.above(y), Blocks.AIR.defaultBlockState());
                    if (Math.abs(side) == 2) {
                        boolean light = Math.floorMod(step, 16) == 8;
                        result.put(floor.above(), light ? Blocks.SEA_LANTERN.defaultBlockState() : rail);
                    }
                }
            }
        }

        private static Map<Long, List<Placement>> indexChunks() {
            Map<Long, List<Placement>> result = new LinkedHashMap<>();
            BLOCKS.forEach((pos, state) -> result.computeIfAbsent(chunkKey(Math.floorDiv(pos.getX(), 16),
                    Math.floorDiv(pos.getZ(), 16)), ignored -> new ArrayList<>()).add(new Placement(pos, state)));
            result.replaceAll((key, placements) -> List.copyOf(placements));
            return Map.copyOf(result);
        }

        private static List<Bounds> bounds() {
            List<Bounds> result = new ArrayList<>();
            for (Module module : MODULES) {
                BlockPos min = module.origin.offset(module.scene.min());
                BlockPos max = module.origin.offset(module.scene.max());
                // Keep a local viewing volume open as well as the actual rooms. The old solid
                // grid otherwise crosses directly in front of every island at eye/aerial height.
                result.add(new Bounds(min.getX() - 32, FLOOR_Y - 16, min.getZ() - 32,
                        max.getX() + 32, FLOOR_Y + 64, max.getZ() + 32));
            }
            result.add(new Bounds(-112, FLOOR_Y - 2, -3, 112, FLOOR_Y + 5, 3));
            result.add(new Bounds(-3, FLOOR_Y - 2, 0, 3, FLOOR_Y + 5, 112));
            return List.copyOf(result);
        }
    }

    public static List<Module> modules() {
        return Plans.MODULES;
    }

    public static Map<BlockPos, BlockState> clusterBlocks() {
        return Plans.BLOCKS;
    }

    private record Cell(long seed, int x, int z) {
    }

    private record Cluster(List<Module> modules, Map<BlockPos, BlockState> blocks,
                           Map<Long, List<Placement>> chunks, List<Bounds> reserved) {
    }

    private static final Map<Cell, Cluster> CLUSTERS = new LinkedHashMap<>(8, .75f, true);

    /**
     * Origins and block coordinates in these seeded overloads are absolute world coordinates.
     */
    public static List<Module> modules(long seed, int cellX, int cellZ) {
        return cluster(seed, cellX, cellZ).modules;
    }

    public static Map<BlockPos, BlockState> clusterBlocks(long seed, int cellX, int cellZ) {
        return cluster(seed, cellX, cellZ).blocks;
    }

    public static List<Placement> blocksInChunk(long seed, int chunkX, int chunkZ) {
        int cellX = Math.floorDiv(chunkX * 16 + CLUSTER_SPACING / 2, CLUSTER_SPACING);
        int cellZ = Math.floorDiv(chunkZ * 16 + CLUSTER_SPACING / 2, CLUSTER_SPACING);
        return cluster(seed, cellX, cellZ).chunks.getOrDefault(chunkKey(chunkX, chunkZ), List.of());
    }

    /**
     * Metadata is clipped to the same owner as its chest block, including negative chunks.
     */
    public static List<ChestPlacement> chestsInChunk(long seed, int chunkX, int chunkZ) {
        int cellX = Math.floorDiv(chunkX * 16 + CLUSTER_SPACING / 2, CLUSTER_SPACING);
        int cellZ = Math.floorDiv(chunkZ * 16 + CLUSTER_SPACING / 2, CLUSTER_SPACING);
        List<ChestPlacement> result = new ArrayList<>();
        for (Module module : modules(seed, cellX, cellZ)) {
            module.scene.lootChests().forEach((relative, table) -> {
                BlockPos pos = module.origin.offset(relative);
                if (Math.floorDiv(pos.getX(), 16) == chunkX && Math.floorDiv(pos.getZ(), 16) == chunkZ)
                    result.add(new ChestPlacement(pos, table, DreamChestLoot.seed(seed, pos, table)));
            });
        }
        return List.copyOf(result);
    }

    public static boolean isReserved(long seed, BlockPos pos) {
        int cellX = Math.floorDiv(pos.getX() + CLUSTER_SPACING / 2, CLUSTER_SPACING);
        int cellZ = Math.floorDiv(pos.getZ() + CLUSTER_SPACING / 2, CLUSTER_SPACING);
        return cluster(seed, cellX, cellZ).reserved.stream().anyMatch(bounds -> bounds.contains(pos))
                || DreamTerrainLayout.isReserved(seed, pos);
    }

    private static synchronized Cluster cluster(long seed, int cellX, int cellZ) {
        Cell key = new Cell(seed, cellX, cellZ);
        Cluster cached = CLUSTERS.get(key);
        if (cached != null)
            return cached;
        Cluster built = buildCluster(seed, cellX, cellZ);
        // Large scene maps must not accumulate as players travel or load different saves.
        if (CLUSTERS.size() >= 8)
            CLUSTERS.remove(CLUSTERS.keySet().iterator().next());
        CLUSTERS.put(key, built);
        return built;
    }

    private static Cluster buildCluster(long seed, int cellX, int cellZ) {
        int shiftX = cellX * CLUSTER_SPACING, shiftZ = cellZ * CLUSTER_SPACING;
        List<Module> modules = new ArrayList<>();
        Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        List<Bounds> reserved = new ArrayList<>();
        // Huaian Station and its three bridges are a landmark at the world origin.
        if (cellX == 0 && cellZ == 0) {
            modules.addAll(Plans.MODULES);
            blocks.putAll(Plans.BLOCKS);
            reserved.addAll(Plans.RESERVED);
        }

        Random random = new Random(mix(seed ^ cellX * 341873128712L ^ cellZ * 132897987541L));
        List<IslandType> kinds = new ArrayList<>(List.of(IslandType.CRYSTAL_REEF, IslandType.CHERRY_ISLE, IslandType.ARCHIVE_RUIN));
        Collections.shuffle(kinds, random);
        kinds.add(IslandType.values()[random.nextInt(IslandType.values().length)]);
        List<Integer> heights = new ArrayList<>(List.of(104, 116, 140, 152));
        Collections.shuffle(heights, random);
        int[][] sites = {{-152, -152}, {152, -152}, {152, 152}, {-152, 152}};
        for (int i = 0; i < sites.length; i++) {
            IslandType kind = kinds.get(i);
            DreamScene scene = rotate(DreamIslandScenes.create(kind, random.nextLong()), Rotation.values()[random.nextInt(4)]);
            BlockPos origin = new BlockPos(shiftX + sites[i][0] + random.nextInt(21) - 10, heights.get(i),
                    shiftZ + sites[i][1] + random.nextInt(21) - 10);
            Module module = new Module("wild_" + i + "_" + kind.name().toLowerCase(Locale.ROOT), origin, scene);
            modules.add(module);
            scene.blocks().forEach((pos, state) -> blocks.put(origin.offset(pos), state));
            BlockPos min = origin.offset(scene.min()), max = origin.offset(scene.max());
            reserved.add(new Bounds(min.getX() - 32, origin.getY() - 18, min.getZ() - 32,
                    max.getX() + 32, origin.getY() + 64, max.getZ() + 32));
        }
        Map<Long, List<Placement>> chunks = new LinkedHashMap<>();
        blocks.forEach((pos, state) -> chunks.computeIfAbsent(chunkKey(Math.floorDiv(pos.getX(), 16),
                Math.floorDiv(pos.getZ(), 16)), ignored -> new ArrayList<>()).add(new Placement(pos, state)));
        chunks.replaceAll((key, value) -> List.copyOf(value));
        return new Cluster(List.copyOf(modules), Map.copyOf(blocks), Map.copyOf(chunks), List.copyOf(reserved));
    }

    static DreamScene rotate(DreamScene scene, Rotation rotation) {
        if (rotation == Rotation.NONE)
            return scene;
        Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        scene.blocks().forEach((pos, state) -> blocks.put(rotate(pos, rotation), state.rotate(rotation)));
        Map<Direction, BlockPos> exits = new EnumMap<>(Direction.class);
        scene.exits().forEach((direction, pos) -> exits.put(rotation.rotate(direction), rotate(pos, rotation)));
        Map<BlockPos, ResourceKey<LootTable>> lootChests = new LinkedHashMap<>();
        scene.lootChests().forEach((pos, table) -> lootChests.put(rotate(pos, rotation), table));
        return new DreamScene(scene.id(), blocks, rotate(scene.landing(), rotation), exits, lootChests);
    }

    private static BlockPos rotate(BlockPos pos, Rotation rotation) {
        return switch (rotation) {
            case NONE -> pos;
            case CLOCKWISE_90 -> new BlockPos(-pos.getZ(), pos.getY(), pos.getX());
            case CLOCKWISE_180 -> new BlockPos(-pos.getX(), pos.getY(), -pos.getZ());
            case COUNTERCLOCKWISE_90 -> new BlockPos(pos.getZ(), pos.getY(), -pos.getX());
        };
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
        value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
        return value ^ (value >>> 31);
    }

    public static List<Bounds> reservedBounds() {
        return Plans.RESERVED;
    }

    /**
     * The origin landing, or the coordinate of a possible legacy station in an older save.
     */
    public static BlockPos stationLanding(int cellX, int cellZ) {
        return Plans.MODULES.getFirst().landing().offset(cellX * CLUSTER_SPACING, 0, cellZ * CLUSTER_SPACING);
    }

    public static boolean isReserved(BlockPos pos) {
        return Plans.RESERVED.stream().anyMatch(bounds -> bounds.contains(pos));
    }

    public static List<Placement> blocksInChunk(int chunkX, int chunkZ) {
        return Plans.CHUNKS.getOrDefault(chunkKey(chunkX, chunkZ), List.of());
    }

    private static long chunkKey(int x, int z) {
        return ((long) x << 32) ^ (z & 0xffffffffL);
    }

    private DreamArchipelagoLayout() {
    }
}
