package cc.thonly.reverie_dreams.world.gensokyo;

import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoChunkGenerator;
import com.google.gson.JsonParser;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A collision-only perimeter: it blocks movement without affecting terrain, water, light or heightmaps.
 */
public final class GensokyoBoundaryCollision {
    private static final double EDGE_EPSILON = 1.0E-3;

    public record Bounds(int x, int z, int width, int depth) {
        public boolean contains(double worldX, double worldZ) {
            return worldX >= this.x && worldX <= this.x + this.width
                    && worldZ >= this.z && worldZ <= this.z + this.depth;
        }

        public Bounds expanded(int margin) {
            return new Bounds(this.x - margin, this.z - margin,
                    this.width + margin * 2, this.depth + margin * 2);
        }

        public double clampX(double worldX) {
            return Mth.clamp(worldX, this.x + EDGE_EPSILON, this.x + this.width - EDGE_EPSILON);
        }

        public double clampZ(double worldZ) {
            return Mth.clamp(worldZ, this.z + EDGE_EPSILON, this.z + this.depth - EDGE_EPSILON);
        }

        public List<VoxelShape> touching(AABB test) {
            if (test.minX >= x && test.maxX <= x + width && test.minZ >= z && test.maxZ <= z + depth)
                return List.of();
            var shapes = new ArrayList<VoxelShape>(4);
            // Infinite vertical extent also prevents flight/vehicles from crossing above the build limit.
            double low = -30_000_000, high = 30_000_000;
            for (var wall : List.of(new AABB(x - 1, low, z - 1, x, high, z + depth + 1),
                    new AABB(x + width, low, z - 1, x + width + 1, high, z + depth + 1),
                    new AABB(x - 1, low, z - 1, x + width + 1, high, z),
                    new AABB(x - 1, low, z + depth, x + width + 1, high, z + depth + 1))) {
                if (wall.intersects(test))
                    shapes.add(Shapes.create(wall));
            }
            return shapes;
        }
    }

    private static final Bounds CLIENT_BOUNDS = loadBounds();

    private GensokyoBoundaryCollision() {
    }

    private static Bounds loadBounds() {
        try (var input = GensokyoBoundaryCollision.class.getResourceAsStream("/data/reverie_dreams/gensokyo/map.json")) {
            var root = JsonParser.parseReader(new InputStreamReader(Objects.requireNonNull(input), StandardCharsets.UTF_8)).getAsJsonObject();
            var origin = root.getAsJsonArray("origin");
            return new Bounds(origin.get(0).getAsInt(), origin.get(1).getAsInt(), root.get("width").getAsInt(), root.get("depth").getAsInt());
        } catch (Exception error) {
            throw new IllegalStateException("Cannot read Gensokyo boundary bounds", error);
        }
    }

    public static Bounds bounds(Level level) {
        if (level instanceof ServerLevel server && server.getChunkSource().getGenerator() instanceof GensokyoChunkGenerator generator) {
            var map = generator.mapData();
            return new Bounds(map.originX(), map.originZ(), map.width(), map.depth());
        }
        return CLIENT_BOUNDS;
    }

    public static List<VoxelShape> append(@Nullable Entity source, Level level, AABB area, List<VoxelShape> existing) {
        if (!GensokyoBoundary.applies(level))
            return existing;
        Bounds bounds = boundsFor(source, level);
        if (bounds == null)
            return existing;
        var added = bounds.touching(area);
        if (added.isEmpty())
            return existing;
        var result = new ArrayList<VoxelShape>(existing.size() + added.size());
        result.addAll(existing);
        result.addAll(added);
        return result;
    }

    /**
     * Spectators have no perimeter at all; everything else (players, boats, minecarts,
     * projectiles, mobs) is confined to the exact map rectangle.
     */
    private static @Nullable Bounds boundsFor(@Nullable Entity source, Level level) {
        if (source == null)
            return bounds(level);
        if (source instanceof Player player && player.isSpectator())
            return null;
        return GensokyoBoundary.playerBounds(level);
    }
}
