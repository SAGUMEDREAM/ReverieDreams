package cc.thonly.reverie_dreams.world.dream;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Small, flattened and bevelled rocks with bounded, deterministic voxel geometry.
 */
public final class DreamFloatingRockShape {
    public record Shape(int radiusX, int radiusY, int radiusZ, List<BlockPos> blocks) {
        public Shape {
            blocks = List.copyOf(blocks);
        }
    }

    public static Shape create(long seed, int radius) {
        if (radius < 1 || radius > 5)
            throw new IllegalArgumentException("Dream rock radius must be 1..5");
        // Adjacent seed numbers otherwise share the first high bits of java.util.Random.
        seed = (seed ^ (seed >>> 30)) * 0xbf58476d1ce4e5b9L;
        seed = (seed ^ (seed >>> 27)) * 0x94d049bb133111ebL;
        Random random = new Random(seed ^ (seed >>> 31));
        int shortRadius = Math.max(1, radius - random.nextInt(2));
        int rx = random.nextBoolean() ? radius : shortRadius;
        int rz = rx == radius ? shortRadius : radius;
        int ry = Math.max(1, (radius + 1) / 2);
        double bevel = 1.68 + random.nextDouble() * .16;
        List<BlockPos> blocks = new ArrayList<>();
        for (int x = -rx; x <= rx; x++)
            for (int y = -ry; y <= ry; y++)
                for (int z = -rz; z <= rz; z++) {
                    double nx = x / (double) rx, ny = y / (double) ry, nz = z / (double) rz;
                    if (nx * nx + ny * ny + nz * nz <= 1.02
                            && Math.abs(nx) + Math.abs(ny) + Math.abs(nz) <= bevel) {
                        blocks.add(new BlockPos(x, y, z));
                    }
                }
        return new Shape(rx, ry, rz, blocks);
    }

    private DreamFloatingRockShape() {
    }
}
