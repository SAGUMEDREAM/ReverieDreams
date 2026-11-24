package cc.thonly.reverie_dreams.world.moon.signal;

import cc.thonly.reverie_dreams.world.gen.RDBuiltinStructureSets;
import cc.thonly.reverie_dreams.world.gen.RDBuiltinStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;

import java.util.*;

/**
 * Bounded, incremental structure-start lookup. Never generates full chunks just to locate a site.
 */
public final class MoonSignalScan {
    public static final int RANGE = 4096;
    private final BlockPos origin;
    private final Iterator<ChunkPos> candidates;
    private final Map<Structure, String> kinds = new LinkedHashMap<>();
    private final Map<String, MoonSignalData.Target> nearest = new LinkedHashMap<>();

    public MoonSignalScan(ServerLevel level, BlockPos origin) {
        this.origin = origin.immutable();
        var structures = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        kinds.put(structures.getOrThrow(RDBuiltinStructures.MOON_INSTITUTE).value(), "institute");
        kinds.put(structures.getOrThrow(RDBuiltinStructures.MOON_EMPTY_BUILDING).value(), "supply");
        kinds.put(structures.getOrThrow(RDBuiltinStructures.QUARANTINE_POOL).value(), "anomaly");
        var set = level.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET).getOrThrow(RDBuiltinStructureSets.MOON_STRUCTURES).value();
        if (!(set.placement() instanceof RandomSpreadStructurePlacement placement)) {
            throw new IllegalStateException("Moon signal scanning requires a random-spread lunar structure set");
        }
        candidates = candidates(placement, level.getSeed(), origin).iterator();
    }

    public static List<ChunkPos> candidates(RandomSpreadStructurePlacement placement, long seed, BlockPos origin) {
        int spacing = placement.spacing();
        int regionX = Math.floorDiv(origin.getX() >> 4, spacing), regionZ = Math.floorDiv(origin.getZ() >> 4, spacing);
        int reach = (RANGE / 16 + spacing - 1) / spacing + 1;
        var positions = new ArrayList<ChunkPos>();
        for (int x = regionX - reach; x <= regionX + reach; x++)
            for (int z = regionZ - reach; z <= regionZ + reach; z++) {
                ChunkPos candidate = placement.getPotentialStructureChunk(seed, x * spacing, z * spacing);
                if (horizontalDistanceSquared(origin, candidate.getMiddleBlockPosition(0)) <= (long) (RANGE + 256) * (RANGE + 256))
                    positions.add(candidate);
            }
        positions.sort(Comparator.comparingLong(p -> horizontalDistanceSquared(origin, p.getMiddleBlockPosition(0))));
        return List.copyOf(positions);
    }

    /**
     * At most one structure-start request; caller enforces a global per-tick budget.
     */
    public boolean step(ServerLevel level) {
        if (!candidates.hasNext())
            return true;
        var chunkPos = candidates.next();
        var chunk = level.getChunkSource().getChunk(chunkPos.x(), chunkPos.z(), ChunkStatus.STRUCTURE_STARTS, true);
        if (chunk != null)
            for (var entry : kinds.entrySet()) {
                var start = chunk.getStartForStructure(entry.getKey());
                if (start == null || !start.isValid())
                    continue;
                BlockPos pos = start.getBoundingBox().getCenter();
                long distance = horizontalDistanceSquared(origin, pos);
                var old = nearest.get(entry.getValue());
                if (distance <= (long) RANGE * RANGE && (old == null || distance < horizontalDistanceSquared(origin, old.pos())))
                    nearest.put(entry.getValue(), new MoonSignalData.Target(entry.getValue(), pos));
            }
        return !candidates.hasNext();
    }

    public List<MoonSignalData.Target> targets() {
        return List.copyOf(nearest.values());
    }

    public static long horizontalDistanceSquared(BlockPos a, BlockPos b) {
        long x = (long) a.getX() - b.getX(), z = (long) a.getZ() - b.getZ();
        return x * x + z * z;
    }
}
