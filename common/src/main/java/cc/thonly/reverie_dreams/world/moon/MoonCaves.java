package cc.thonly.reverie_dreams.world.moon;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntBinaryOperator;

/**
 * World-coordinate passages, shared by adjacent chunks and carved before ores/structures.
 */
public final class MoonCaves {
    private static final int SPACING = 192;
    private static final int REACH = 192;

    private MoonCaves() {
    }

    /**
     * A dry buffer prevents side walls opening into the sea or an ice crater.
     */
    public static boolean canOpenSurface(long seed, int x, int z) {
        for (int dx = -12; dx <= 12; dx += 12)
            for (int dz = -12; dz <= 12; dz += 12) {
                var sample = MoonTerrain.sample(seed, x + dx, z + dz);
                if (sample.seaWeight() > 0 || sample.iceWeight() > 0)
                    return false;
            }
        return true;
    }

    public static List<Passage> passages(long seed, int chunkX, int chunkZ, IntBinaryOperator surface) {
        int minX = chunkX * 16, minZ = chunkZ * 16;
        var result = new ArrayList<Passage>();
        for (int gx = Math.floorDiv(minX - REACH, SPACING); gx <= Math.floorDiv(minX + 15 + REACH, SPACING); gx++) {
            for (int gz = Math.floorDiv(minZ - REACH, SPACING); gz <= Math.floorDiv(minZ + 15 + REACH, SPACING); gz++) {
                for (int kind = 0; kind < 2; kind++) {
                    long salt = seed + 72491 + kind * 9173L;
                    if (MoonTerrain.unit(salt, gx, gz) > (kind == 0 ? 0.72 : 0.30))
                        continue;
                    int x = gx * SPACING + 24 + (int) (MoonTerrain.unit(salt + 1, gx, gz) * 144);
                    int z = gz * SPACING + 24 + (int) (MoonTerrain.unit(salt + 2, gx, gz) * 144);
                    if (!canOpenSurface(seed, x, z))
                        continue;
                    double angle = MoonTerrain.unit(salt + 3, gx, gz) * Math.PI * 2;
                    double length = 96 + MoonTerrain.unit(salt + 4, gx, gz) * 64;
                    double depth = kind == 0 ? 74 + MoonTerrain.unit(salt + 5, gx, gz) * 20
                            : 30 + MoonTerrain.unit(salt + 5, gx, gz) * 28;
                    var passage = new Passage(kind == 1, x, z, Math.cos(angle), Math.sin(angle), length, depth, 0);
                    if (passage.intersects(minX, minZ, minX + 15, minZ + 15))
                        result.add(new Passage(passage.ravine, x, z, passage.dx, passage.dz, length, depth, surface.applyAsInt(x, z)));
                }
            }
        }
        return List.copyOf(result);
    }

    public record Cut(int bottom, int top) {
        public boolean contains(int y) {
            return y >= bottom && y <= top;
        }
    }

    public record Passage(boolean ravine, int x, int z, double dx, double dz,
                          double length, double depth, int surface) {
        private boolean intersects(int minX, int minZ, int maxX, int maxZ) {
            double endX = x + dx * length, endZ = z + dz * length;
            return maxX >= Math.min(x, endX) - 16 && minX <= Math.max(x, endX) + 16
                    && maxZ >= Math.min(z, endZ) - 16 && minZ <= Math.max(z, endZ) + 16;
        }

        public Cut cut(int blockX, int blockZ, int floor, boolean openSurface) {
            double rx = blockX - x, rz = blockZ - z;
            double along = rx * dx + rz * dz, t = along / length;
            if (ravine ? t < 0 || t > 1 : along < -3 || along > length + 3)
                return null;
            t = Math.clamp(t, 0, 1);
            double side = -rx * dz + rz * dx - Math.sin(t * Math.PI * 2) * 6;
            double shape = Math.sin(t * Math.PI);
            double radius = ravine ? 2 + 5 * shape : 3.2 + 2.2 * Math.pow(Math.sin(t * Math.PI * 3), 4);
            double end = ravine ? 0 : Math.max(0, Math.max(-along, along - length)) / 3.2;
            double cross = side * side / (radius * radius) + end * end;
            if (cross >= 1)
                return null;
            double crossSection = Math.sqrt(1 - cross);
            int bottom, top;
            if (ravine) {
                bottom = (int) Math.ceil(floor - depth * shape * crossSection);
                top = floor;
            } else {
                // A walkable descending tube joins surface rock to the new negative-Y cave layer.
                double descent = Math.min(depth, Math.max(0, surface - MoonTerrain.MIN_Y - 16));
                double center = surface + 1 - descent * t;
                double halfHeight = (2.8 + 1.4 * shape) * crossSection;
                bottom = (int) Math.ceil(center - halfHeight);
                top = (int) Math.floor(center + halfHeight);
            }
            bottom = Math.max(MoonTerrain.MIN_Y + 11, bottom);
            top = Math.min(openSurface ? floor : floor - 12, top);
            return bottom <= top ? new Cut(bottom, top) : null;
        }
    }
}
