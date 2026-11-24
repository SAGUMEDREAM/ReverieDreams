package cc.thonly.reverie_dreams.world.dream;

import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;

/**
 * Seeded column plans for natural hills, ridges and peaks; no world reads or neighbor writes.
 */
public final class DreamTerrainLayout {
    public static final int CLUSTER_SPACING = 512;
    private static final int ABSENT = Integer.MIN_VALUE;
    private static final int SCENE_GAP = 12;

    private record Cell(long seed, int x, int z) {
    }

    private static final Map<Cell, List<Island>> CACHE = new LinkedHashMap<>(8, .75f, true);

    public enum Kind {LOW_HILL, RIDGE, LONE_PEAK}

    private record Decoration(int x, int z, int floorY, int height, int type) {
    }

    /**
     * Compact immutable height/underside arrays, not a cached block volume. All public positions
     * are absolute world positions. surfaceY includes a path slab, but never foliage above it.
     */
    public static final class Island {
        private final String id;
        private final Kind kind;
        private final int centerX, centerZ, radius, side, baseY;
        private final short[] floorHalf;
        private final short[] bottoms;
        private final boolean[] path;
        private final boolean[] planted;
        private final List<BlockPos> route;
        private final List<Decoration> decorations;
        private final int minX, maxX, minZ, maxZ, minY, maxY;
        private final double phase;

        private Island(String id, Kind kind, long seed, int centerX, int centerZ) {
            this.id = id;
            this.kind = kind;
            this.centerX = centerX;
            this.centerZ = centerZ;
            Random random = new Random(mix(seed));
            int radiusU = switch (kind) {
                case LOW_HILL -> 15 + random.nextInt(7);
                case RIDGE -> 27 + random.nextInt(7);
                case LONE_PEAK -> 39 + random.nextInt(9);
            };
            int radiusV = switch (kind) {
                case LOW_HILL -> 14 + random.nextInt(5);
                case RIDGE -> 22 + random.nextInt(6);
                case LONE_PEAK -> 34 + random.nextInt(8);
            };
            radius = Math.max(radiusU, radiusV);
            side = radius * 2 + 1;
            floorHalf = new short[side * side];
            bottoms = new short[side * side];
            path = new boolean[side * side];
            planted = new boolean[side * side];
            baseY = 108 + random.nextInt(25);
            phase = random.nextDouble() * Math.PI * 2;
            double phase2 = random.nextDouble() * Math.PI * 2;
            int rotation = random.nextInt(4);
            int rise = switch (kind) {
                case LOW_HILL -> 6 + random.nextInt(5);
                case RIDGE -> 13 + random.nextInt(8);
                case LONE_PEAK -> 22 + random.nextInt(7);
            };
            double[] heights = new double[side * side];
            double maximum = 0;
            int peakU = 0, peakV = 0;
            for (int u = -radiusU; u <= radiusU; u++)
                for (int v = -radiusV; v <= radiusV; v++) {
                    double nx = u / (double) radiusU, nz = v / (double) radiusV;
                    double angle = Math.atan2(nz, nx);
                    double coast = 1 + .065 * Math.sin(angle * 3 + phase) + .04 * Math.sin(angle * 5 + phase2);
                    double r = Math.hypot(nx, nz) / coast;
                    if (r > .98)
                        continue;
                    int index = indexLocal(u, v, rotation);
                    double edge = Math.min(1, (1 - r) * 3.8);
                    double a = Math.exp(-((nx - .13) * (nx - .13) * 3.3 + (nz + .24) * (nz + .24) * 3.0));
                    double b = Math.exp(-((nx + .36) * (nx + .36) * 6 + (nz - .05) * (nz - .05) * 5));
                    double height = switch (kind) {
                        case LOW_HILL -> edge * (.75 * a + .27 * b);
                        case RIDGE -> edge * Math.max(a, .83 * b);
                        case LONE_PEAK -> edge * (Math.pow(a, 1.35) + .15 * b);
                    };
                    heights[index] = height;
                    if (height > maximum) {
                        maximum = height;
                        peakU = u;
                        peakV = v;
                    }
                    // One offset keel plus a lesser side root; a thin, connected edge, no satellites.
                    double root = Math.max(0, 1 - Math.hypot(nx - .19, nz + .14) / .9);
                    double sideRoot = Math.max(0, 1 - Math.hypot(nx + .38, nz - .22) / .7);
                    int depth = 1 + (int) Math.floor(27 * Math.min(edge, .65 * edge + .25 * root + .1 * sideRoot));
                    bottoms[index] = (short) (baseY - depth);
                }
            for (int i = 0; i < heights.length; i++)
                if (bottoms[i] != 0)
                    floorHalf[i] = (short) (2 * (baseY + (int) Math.floor(rise * heights[i] / maximum) + 1));

            List<int[]> localRoute = new ArrayList<>();
            int entryU = (int) Math.round(-radiusU * .45), entryV = (int) Math.round(radiusV * .58);
            addLine(localRoute, entryU, entryV, (int) Math.round(radiusU * .45), entryV);
            addLine(localRoute, localRoute.getLast()[0], entryV, localRoute.getLast()[0], (int) Math.round(radiusV * .1));
            addLine(localRoute, localRoute.getLast()[0], localRoute.getLast()[1], (int) Math.round(-radiusU * .25), localRoute.getLast()[1]);
            addLine(localRoute, localRoute.getLast()[0], localRoute.getLast()[1], localRoute.getLast()[0], peakV);
            addLine(localRoute, localRoute.getLast()[0], peakV, peakU, peakV);
            int startHalf = floorHalf[indexLocal(entryU, entryV, rotation)];
            int endHalf = 2 * (baseY + rise + 1);
            if (startHalf == 0 || localRoute.size() - 1 < endHalf - startHalf)
                throw new IllegalStateException("Natural-island path exceeds half-step grade budget");
            int[] nearestStep = new int[side * side];
            int[] nearestDistance = new int[side * side];
            Arrays.fill(nearestStep, -1);
            Arrays.fill(nearestDistance, Integer.MAX_VALUE);
            for (int step = 0; step < localRoute.size(); step++) {
                int[] p = localRoute.get(step);
                for (int du = -5; du <= 5; du++)
                    for (int dv = -5; dv <= 5; dv++) {
                        int u = p[0] + du, v = p[1] + dv;
                        if (Math.abs(u) > radius || Math.abs(v) > radius)
                            continue;
                        int i = indexLocal(u, v, rotation), distance = du * du + dv * dv;
                        if (bottoms[i] != 0 && distance < nearestDistance[i]) {
                            nearestStep[i] = step;
                            nearestDistance[i] = distance;
                        }
                    }
            }
            for (int i = 0; i < floorHalf.length; i++)
                if (nearestStep[i] >= 0) {
                    int graded = startHalf + (endHalf - startHalf) * nearestStep[i] / (localRoute.size() - 1);
                    if (nearestDistance[i] <= 2) {
                        path[i] = true;
                        floorHalf[i] = (short) graded;
                    } else if (nearestDistance[i] < 25) {
                        double weight = Math.max(0, (5 - Math.sqrt(nearestDistance[i])) / 3.5);
                        floorHalf[i] = (short) (2 * Math.round((floorHalf[i] * (1 - weight) + graded * weight) / 2));
                    }
                }
            List<BlockPos> routePoints = new ArrayList<>();
            for (int[] p : localRoute) {
                int[] rotated = rotate(p[0], p[1], rotation);
                int x = centerX + rotated[0], z = centerZ + rotated[1];
                routePoints.add(new BlockPos(x, surfaceY(x, z), z));
            }
            route = List.copyOf(routePoints);

            List<Decoration> decor = new ArrayList<>();
            int wanted = switch (kind) {
                case LOW_HILL -> 2;
                case RIDGE -> 4;
                case LONE_PEAK -> 3;
            };
            for (int attempt = 0; attempt < 100 && decor.size() < wanted; attempt++) {
                int x = centerX + random.nextInt(side), z = centerZ + random.nextInt(side);
                x -= radius;
                z -= radius;
                int i = index(x, z);
                if (i < 0 || floorHalf[i] == 0 || floorHalf[i] % 2 != 0 || nearestDistance[i] <= 25)
                    continue;
                boolean safe = true;
                for (int dx = -3; dx <= 3 && safe; dx++)
                    for (int dz = -3; dz <= 3; dz++)
                        if (!containsColumn(x + dx, z + dz)) {
                            safe = false;
                            break;
                        }
                for (Decoration prior : decor)
                    if (Math.hypot(prior.x - x, prior.z - z) < 9)
                        safe = false;
                if (!safe)
                    continue;
                int type = switch (kind) {
                    case LOW_HILL -> 0;
                    case RIDGE -> 1;
                    case LONE_PEAK -> 2;
                };
                int height = type == 0 ? 4 + random.nextInt(2) : type == 1 ? 4 + random.nextInt(3) : 1 + random.nextInt(3);
                decor.add(new Decoration(x, z, surfaceY(x, z), height, type));
                planted[i] = type != 2;
            }
            decorations = List.copyOf(decor);
            int lowX = Integer.MAX_VALUE, highX = Integer.MIN_VALUE, lowZ = lowX, highZ = highX;
            int lowY = Integer.MAX_VALUE, highY = Integer.MIN_VALUE;
            for (int x = centerX - radius; x <= centerX + radius; x++)
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    int i = index(x, z);
                    if (floorHalf[i] == 0)
                        continue;
                    lowX = Math.min(lowX, x);
                    highX = Math.max(highX, x);
                    lowZ = Math.min(lowZ, z);
                    highZ = Math.max(highZ, z);
                    lowY = Math.min(lowY, bottoms[i]);
                    highY = Math.max(highY, surfaceY(x, z));
                }
            for (Decoration d : decorations)
                highY = Math.max(highY, d.floorY + d.height + (d.type == 0 ? 1 : 0));
            minX = lowX;
            maxX = highX;
            minZ = lowZ;
            maxZ = highZ;
            minY = lowY;
            maxY = highY;
        }

        private int indexLocal(int u, int v, int rotation) {
            int[] p = rotate(u, v, rotation);
            return (p[1] + radius) * side + p[0] + radius;
        }

        private int index(int x, int z) {
            int dx = x - centerX, dz = z - centerZ;
            return Math.abs(dx) > radius || Math.abs(dz) > radius ? -1 : (dz + radius) * side + dx + radius;
        }

        public String id() {
            return id;
        }

        public Kind kind() {
            return kind;
        }

        public int minX() {
            return minX;
        }

        public int maxX() {
            return maxX;
        }

        public int minZ() {
            return minZ;
        }

        public int maxZ() {
            return maxZ;
        }

        public int minY() {
            return minY;
        }

        public int maxY() {
            return maxY;
        }

        public List<BlockPos> route() {
            return route;
        }

        public boolean containsColumn(int x, int z) {
            int i = index(x, z);
            return i >= 0 && floorHalf[i] != 0;
        }

        public int surfaceY(int x, int z) {
            int i = index(x, z);
            return i < 0 || floorHalf[i] == 0 ? ABSENT : (floorHalf[i] - 1) / 2;
        }

        public int bottomY(int x, int z) {
            int i = index(x, z);
            return i < 0 || floorHalf[i] == 0 ? ABSENT : bottoms[i];
        }

        public double surfaceHeight(int x, int z) {
            int i = index(x, z);
            return i < 0 || floorHalf[i] == 0 ? Double.NaN : floorHalf[i] / 2.0;
        }

        private boolean moss(int x, int z, int index) {
            if (planted[index])
                return true;
            if (path[index])
                return false;
            double patches = Math.sin((x - centerX) / 7.0 + phase) + .7 * Math.cos((z - centerZ) / 9.0 - phase);
            return surfaceY(x, z) < baseY + (kind == Kind.LONE_PEAK ? 12 : 18) && patches > -.8;
        }

        private void emit(int chunkX, int chunkZ, BiConsumer<BlockPos, BlockState> sink) {
            int startX = chunkX * 16, startZ = chunkZ * 16;
            BlockState stone = RDBlocks.DREAM_STONE.block().defaultBlockState();
            BlockState slab = RDBlocks.DREAM_STONE.slab().defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
            for (int x = Math.max(startX, minX); x <= Math.min(startX + 15, maxX); x++)
                for (int z = Math.max(startZ, minZ); z <= Math.min(startZ + 15, maxZ); z++) {
                    int i = index(x, z);
                    if (floorHalf[i] == 0)
                        continue;
                    int top = surfaceY(x, z);
                    boolean green = moss(x, z, i);
                    for (int y = bottoms[i]; y < top; y++)
                        sink.accept(new BlockPos(x, y, z), green && y == top - 1 ? Blocks.DIRT.defaultBlockState() : stone);
                    sink.accept(new BlockPos(x, top, z), floorHalf[i] % 2 != 0 ? slab : green ? Blocks.MOSS_BLOCK.defaultBlockState() : stone);
                }
            for (Decoration d : decorations) {
                if (d.type == 0) {
                    for (int y = 1; y <= d.height; y++)
                        emitOwned(sink, chunkX, chunkZ, d.x, d.floorY + y, d.z, Blocks.CHERRY_LOG.defaultBlockState());
                    BlockState leaves = Blocks.CHERRY_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
                    for (int dy = -1; dy <= 1; dy++)
                        for (int dx = -2; dx <= 2; dx++)
                            for (int dz = -2; dz <= 2; dz++) {
                                if (dx == 0 && dz == 0 && dy <= 0 || Math.abs(dx) + Math.abs(dz) > (dy == 0 ? 3 : 2))
                                    continue;
                                int x = d.x + dx, z = d.z + dz, y = d.floorY + d.height + dy;
                                if (y > surfaceY(x, z))
                                    emitOwned(sink, chunkX, chunkZ, x, y, z, leaves);
                            }
                } else if (d.type == 1) {
                    for (int y = 1; y <= d.height; y++)
                        emitOwned(sink, chunkX, chunkZ, d.x, d.floorY + y, d.z, Blocks.BAMBOO.defaultBlockState()
                                .setValue(BambooStalkBlock.LEAVES, y == d.height ? BambooLeaves.LARGE : y == d.height - 1 ? BambooLeaves.SMALL : BambooLeaves.NONE));
                } else {
                    for (int y = 1; y <= d.height; y++)
                        emitOwned(sink, chunkX, chunkZ, d.x, d.floorY + y, d.z, RDBlocks.DREAM_CRYSTAL_ORE.defaultBlockState());
                }
            }
        }
    }

    /**
     * Actual scene bounds are consulted only after the mod's blocks have been registered.
     */
    public static synchronized List<Island> islands(long seed, int cellX, int cellZ) {
        Cell cell = new Cell(seed, cellX, cellZ);
        List<Island> cached = CACHE.get(cell);
        if (cached != null)
            return cached;
        var modules = DreamArchipelagoLayout.modules(seed, cellX, cellZ);
        List<Island> accepted = new ArrayList<>();
        for (Island candidate : candidateIslands(seed, cellX, cellZ)) {
            boolean conflict = false;
            for (var module : modules) {
                BlockPos min = module.origin().offset(module.scene().min()), max = module.origin().offset(module.scene().max());
                if (candidate.minX - SCENE_GAP <= max.getX() && candidate.maxX + SCENE_GAP >= min.getX()
                        && candidate.minZ - SCENE_GAP <= max.getZ() && candidate.maxZ + SCENE_GAP >= min.getZ()) {
                    conflict = true;
                    break;
                }
            }
            if (!conflict)
                accepted.add(candidate);
        }
        List<Island> result = List.copyOf(accepted);
        if (CACHE.size() >= 8)
            CACHE.remove(CACHE.keySet().iterator().next());
        CACHE.put(cell, result);
        return result;
    }

    /**
     * Pure candidate planning: deliberately independent of registries and authored scene blocks.
     */
    static List<Island> candidateIslands(long seed, int cellX, int cellZ) {
        Random random = new Random(mix(seed ^ 0x4E41545552414C4CL ^ cellX * 341873128712L ^ cellZ * 132897987541L));
        List<Integer> sites = new ArrayList<>(List.of(0, 1, 2, 3));
        Collections.shuffle(sites, random);
        int count = 2 + random.nextInt(2);
        int[][] centers = {{196, 0}, {0, 196}, {-196, 0}, {0, -196}};
        List<Island> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int type = random.nextInt(100);
            Kind kind = type < 45 ? Kind.LOW_HILL : type < 90 ? Kind.RIDGE : Kind.LONE_PEAK;
            int[] p = centers[sites.get(i)];
            result.add(new Island("natural_" + cellX + "_" + cellZ + "_" + i, kind, random.nextLong(),
                    cellX * CLUSTER_SPACING + p[0] + random.nextInt(5) - 2,
                    cellZ * CLUSTER_SPACING + p[1] + random.nextInt(5) - 2));
        }
        return List.copyOf(result);
    }

    public static void forEachBlockInChunk(long seed, int chunkX, int chunkZ, BiConsumer<BlockPos, BlockState> sink) {
        int cellX = Math.floorDiv(chunkX * 16 + CLUSTER_SPACING / 2, CLUSTER_SPACING);
        int cellZ = Math.floorDiv(chunkZ * 16 + CLUSTER_SPACING / 2, CLUSTER_SPACING);
        for (Island island : islands(seed, cellX, cellZ)) {
            if (chunkX * 16 <= island.maxX && chunkX * 16 + 15 >= island.minX
                    && chunkZ * 16 <= island.maxZ && chunkZ * 16 + 15 >= island.minZ)
                island.emit(chunkX, chunkZ, sink);
        }
    }

    public static boolean isReserved(long seed, BlockPos pos) {
        int cellX = Math.floorDiv(pos.getX() + CLUSTER_SPACING / 2, CLUSTER_SPACING);
        int cellZ = Math.floorDiv(pos.getZ() + CLUSTER_SPACING / 2, CLUSTER_SPACING);
        for (Island island : islands(seed, cellX, cellZ)) {
            if (pos.getY() < island.minY - 2 || pos.getY() > island.maxY + 4)
                continue;
            for (int dx = -3; dx <= 3; dx++)
                for (int dz = -3; dz <= 3; dz++)
                    if (island.containsColumn(pos.getX() + dx, pos.getZ() + dz))
                        return true;
        }
        return false;
    }

    private static void emitOwned(BiConsumer<BlockPos, BlockState> sink, int cx, int cz, int x, int y, int z, BlockState state) {
        if (Math.floorDiv(x, 16) == cx && Math.floorDiv(z, 16) == cz)
            sink.accept(new BlockPos(x, y, z), state);
    }

    private static void addLine(List<int[]> points, int fromU, int fromV, int toU, int toV) {
        if (points.isEmpty())
            points.add(new int[]{fromU, fromV});
        while (fromU != toU || fromV != toV) {
            if (fromU != toU)
                fromU += Integer.signum(toU - fromU);
            else
                fromV += Integer.signum(toV - fromV);
            points.add(new int[]{fromU, fromV});
        }
    }

    private static int[] rotate(int u, int v, int rotation) {
        return switch (rotation) {
            case 1 -> new int[]{-v, u};
            case 2 -> new int[]{-u, -v};
            case 3 -> new int[]{v, -u};
            default -> new int[]{u, v};
        };
    }

    private static long mix(long value) {
        value = (value ^ value >>> 30) * 0xBF58476D1CE4E5B9L;
        value = (value ^ value >>> 27) * 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }

    private DreamTerrainLayout() {
    }
}
