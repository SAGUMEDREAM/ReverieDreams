package cc.thonly.reverie_dreams.paper.world.forest;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

/** World-independent geometry, shared by world generation and the offline preview. */
public final class GensokyoTreeShape {
    public enum Species { OAK, DARK_OAK, BIRCH, SPRUCE, CHERRY, IROHA_MAPLE, ITAYA_MAPLE, HAUCHIWA_MAPLE }
    public enum Axis { X, Y, Z }
    public record Point(int x, int y, int z) {
        Point offset(int dx, int dy, int dz) { return new Point(x + dx, y + dy, z + dz); }
    }
    public record Voxel(Point point, boolean log, Axis axis, int leafDistance) {}
    public record Tree(int height, List<Voxel> voxels) {}
    public record Settings(Species species, int minHeight, int maxHeight, int trunkWidth, int crownRadius) {
        public Settings {
            if (species == null || minHeight < 8 || maxHeight > 40 || minHeight > maxHeight
                    || trunkWidth < 1 || trunkWidth > 2 || crownRadius < 2 || crownRadius > 6) {
                throw new IllegalArgumentException("Invalid Gensokyo tree dimensions");
            }
        }
    }

    private static final int[][] NEIGHBORS = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};

    public static Tree create(Settings settings, RandomGenerator random) {
        return new Builder(settings, random).build();
    }

    private static final class Builder {
        private final Settings settings;
        private final RandomGenerator random;
        private final int height;
        private final Map<Point, Axis> logs = new LinkedHashMap<>();
        private final Map<Point, Boolean> leaves = new LinkedHashMap<>();

        Builder(Settings settings, RandomGenerator random) {
            this.settings = settings;
            this.random = random;
            int span = settings.maxHeight() - settings.minHeight() + 1;
            // Most individuals sit near the middle of their age class, with fewer extremes.
            this.height = settings.minHeight() + (random.nextInt(span) + random.nextInt(span)) / 2;
        }

        Tree build() {
            if (settings.species() == Species.SPRUCE) spruce();
            else if (settings.species() == Species.BIRCH) birch();
            else broadleaf();
            // Actual six-neighbor leaf paths, rather than a Manhattan estimate through air.
            Map<Point, Integer> distances = new LinkedHashMap<>();
            ArrayDeque<Point> queue = new ArrayDeque<>(logs.keySet());
            while (!queue.isEmpty()) {
                Point current = queue.removeFirst();
                int nextDistance = distances.getOrDefault(current, 0) + 1;
                if (nextDistance > 6) continue;
                for (int[] offset : NEIGHBORS) {
                    Point next = current.offset(offset[0], offset[1], offset[2]);
                    if (!logs.containsKey(next) && leaves.containsKey(next) && !distances.containsKey(next)) {
                        distances.put(next, nextDistance);
                        queue.addLast(next);
                    }
                }
            }
            List<Voxel> result = new ArrayList<>(logs.size() + distances.size());
            logs.forEach((point, axis) -> result.add(new Voxel(point, true, axis, 0)));
            distances.forEach((point, distance) -> result.add(new Voxel(point, false, Axis.Y, distance)));
            return new Tree(height, List.copyOf(result));
        }

        private void broadleaf() {
            Species species = settings.species();
            boolean cherry = species == Species.CHERRY;
            boolean mapleSpreading = species == Species.IROHA_MAPLE || species == Species.HAUCHIWA_MAPLE;
            boolean spreading = cherry || mapleSpreading;
            boolean large = species == Species.DARK_OAK || species == Species.ITAYA_MAPLE;
            int topDepth = spreading ? 2 : 3 + random.nextInt(2);
            int crownY = height - 1 - topDepth;
            int leanX = random.nextInt(-2, 3);
            int leanZ = random.nextInt(-2, 3);
            List<Point> stem = stem(height - 3, leanX, leanZ, settings.trunkWidth());
            int radius = Math.clamp(settings.crownRadius() + random.nextInt(-1, 2), 2, 6);
            int offsetX = random.nextInt(-1, 2);
            int offsetZ = random.nextInt(-1, 2);
            ellipsoid(leanX + offsetX, crownY, leanZ + offsetZ,
                    radius, topDepth, Math.max(2, radius - random.nextInt(2)));

            int branches = large ? 4 + random.nextInt(3) : 3 + random.nextInt(spreading ? 2 : 3);
            double rotation = random.nextDouble() * Math.PI * 2;
            for (int branch = 0; branch < branches; branch++) {
                // Unequal angles, branch lengths and levels avoid a radial wheel of identical lobes.
                double angle = rotation + branch * 2.3999632297 + random.nextDouble(-0.4, 0.4);
                int reach = Math.max(2, radius + random.nextInt(-1, 2));
                int endX = leanX + (int) Math.round(Math.cos(angle) * reach);
                int endZ = leanZ + (int) Math.round(Math.sin(angle) * reach);
                int startY = Math.max(3, Math.min(height - 5, (int) (height * random.nextDouble(mapleSpreading ? 0.24 : 0.28, mapleSpreading ? 0.42 : 0.48))));
                int endY = Math.min(height - 4, Math.max(startY + 1, (int) (height * random.nextDouble(mapleSpreading ? 0.54 : 0.58, mapleSpreading ? 0.76 : 0.80))));
                Point start = stem.get(startY);
                Point elbow = new Point((start.x() + endX) / 2, (startY + endY) / 2 + 1, (start.z() + endZ) / 2);
                Point end = new Point(endX, endY, endZ);
                line(start, elbow);
                line(elbow, end);
                int lobeRadius = spreading ? 3 + random.nextInt(2) : 2 + random.nextInt(3);
                ellipsoid(endX, endY + 1, endZ, lobeRadius, spreading ? 2 : 2 + random.nextInt(2),
                        Math.max(2, lobeRadius - random.nextInt(2)));
                ellipsoid(elbow.x(), elbow.y() + 1, elbow.z(), 2, 2, 2);
                if (random.nextDouble() < (large ? 0.7 : 0.4)) {
                    Point fork = end.offset((int) Math.round(-Math.sin(angle) * 2), 1,
                            (int) Math.round(Math.cos(angle) * 2));
                    line(elbow, fork);
                    // Leaf cover around terminal twigs avoids exposed horizontal cut ends.
                    ellipsoid(fork.x(), fork.y() + 1, fork.z(), 2, 2, 2);
                }
            }
        }

        private void birch() {
            int form = random.nextInt(3); // slender spindle, offset oval, occasional split upper leader
            int leanX = random.nextInt(-1, 2);
            int leanZ = random.nextInt(-1, 2);
            List<Point> stem = stem(height - 3, leanX, leanZ, 1);
            int crownBase = Math.max(4, (int) (height * random.nextDouble(0.38, 0.48)));
            int widest = Math.min(settings.crownRadius(), form == 0 ? 2 : 3);
            int biasX = form == 1 ? random.nextInt(-1, 2) : 0;
            int biasZ = form == 1 ? random.nextInt(-1, 2) : 0;
            for (int y = crownBase + 2; y <= height - 3; y += 2) {
                double t = (double) (y - crownBase) / Math.max(1, height - 1 - crownBase);
                int radius = Math.max(2, (int) Math.round(widest * Math.sin(Math.PI * t)));
                Point center = stem.get(y);
                ellipsoid(center.x() + biasX, y, center.z() + biasZ, radius, 2,
                        Math.max(2, radius - (form == 0 ? 0 : random.nextInt(2))));
            }
            double rotation = random.nextDouble() * Math.PI * 2;
            int branches = 2 + random.nextInt(3);
            for (int branch = 0; branch < branches; branch++) {
                int startY = Math.min(height - 4, crownBase + random.nextInt(Math.max(1, height - 3 - crownBase)));
                double angle = rotation + branch * 2.3999632297 + random.nextDouble(-0.3, 0.3);
                int reach = 1 + random.nextInt(2);
                Point start = stem.get(startY);
                Point end = start.offset((int) Math.round(Math.cos(angle) * reach),
                        Math.min(2, height - 3 - startY), (int) Math.round(Math.sin(angle) * reach));
                line(start, end);
                ellipsoid(end.x(), end.y(), end.z(), 2, 2 + random.nextInt(2), 2);
            }
            if (form == 2 && height >= 14) {
                Point from = stem.get(height * 3 / 5);
                Point leader = new Point(leanX + (random.nextBoolean() ? 2 : -2), height - 4, leanZ + 1);
                line(from, leader);
                ellipsoid(leader.x(), height - 4, leader.z(), 2, 3, 2);
            }
            // The tip remains supported and the total height has its literal advertised meaning.
            ellipsoid(leanX, height - 3, leanZ, 1, 2, 1);
        }

        private List<Point> stem(int top, int leanX, int leanZ, int width) {
            List<Point> result = new ArrayList<>();
            Point previous = new Point(0, 0, 0);
            int straightBase = Math.max(2, height / 4);
            for (int y = 0; y <= top; y++) {
                double progress = Math.max(0, (double) (y - straightBase) / Math.max(1, top - straightBase));
                Point center = new Point((int) Math.round(leanX * progress), y, (int) Math.round(leanZ * progress));
                result.add(center);
                line(previous, center);
                for (int dx = 0; dx < width; dx++) {
                    for (int dz = 0; dz < width; dz++) log(center.offset(dx, 0, dz), Axis.Y);
                }
                previous = center;
            }
            return result;
        }

        private void spruce() {
            // Keep the final four blocks needle-only instead of exposing a pole above the crown.
            int trunkTop = height - 5;
            for (int y = 0; y <= trunkTop; y++) log(new Point(0, y, 0), Axis.Y);
            int firstLayer = Math.max(4, (int) (height * random.nextDouble(0.22, 0.34)));
            int widest = Math.clamp(settings.crownRadius() + random.nextInt(-1, 2), 2, 6);
            int tierPhase = random.nextInt(3);
            boolean alongX = random.nextBoolean();
            // A connected crown at every height avoids bare gaps between shrinking ellipsoids.
            for (int y = firstLayer; y < height - 1; y++) {
                double fraction = (double) (y - firstLayer) / Math.max(1, height - 1 - firstLayer);
                int radius = Math.max(1, (int) Math.ceil(widest * Math.pow(1 - fraction, 0.9)));
                if ((y - firstLayer + tierPhase) % 3 == 2 && radius > 2) radius--;
                int shortRadius = radius > 2 ? radius - random.nextInt(2) : radius;
                spruceNeedleLayer(y, radius, shortRadius);
                int reach = Math.min(radius, shortRadius) - 2;
                if (reach > 0 && y <= trunkTop - 2 && (y - firstLayer) % 3 == 0) {
                    // Only short internal supports in wide tiers, with leaf cover beyond both ends.
                    for (int sign : new int[]{-1, 1}) {
                        line(new Point(0, y, 0), new Point(alongX ? sign * reach : 0, y, alongX ? 0 : sign * reach));
                    }
                    alongX = !alongX;
                }
            }
            leaves.put(new Point(0, height - 1, 0), true);
        }

        private void spruceNeedleLayer(int y, int rx, int rz) {
            for (int dx = -rx; dx <= rx; dx++) {
                for (int dz = -rz; dz <= rz; dz++) {
                    double distance = (double) dx * dx / (rx * rx) + (double) dz * dz / (rz * rz);
                    if (distance > 1) continue;
                    // The tight collar around the central stem never receives random holes.
                    if (Math.abs(dx) + Math.abs(dz) <= 2 || distance < 0.78 || random.nextInt(9) != 0) {
                        leaves.put(new Point(dx, y, dz), true);
                    }
                }
            }
        }

        private void ellipsoid(int cx, int cy, int cz, int rx, int ry, int rz) {
            for (int dx = -rx; dx <= rx; dx++) {
                for (int dy = -ry; dy <= ry; dy++) {
                    for (int dz = -rz; dz <= rz; dz++) {
                        int y = cy + dy;
                        if (y < 4 || y >= height) continue;
                        double distance = (double) dx * dx / (rx * rx) + (double) dy * dy / (ry * ry)
                                + (double) dz * dz / (rz * rz);
                        if (distance <= 1.0 && (distance < 0.82 || random.nextInt(6) != 0)) {
                            leaves.put(new Point(cx + dx, y, cz + dz), true);
                        }
                    }
                }
            }
            // Do not let edge randomness remove the intended height marker.
            if (cy + ry == height - 1) leaves.put(new Point(cx, height - 1, cz), true);
        }

        private void line(Point from, Point to) {
            Point current = from;
            log(current, Axis.Y);
            int steps = Math.max(Math.max(Math.abs(to.x() - from.x()), Math.abs(to.y() - from.y())), Math.abs(to.z() - from.z()));
            for (int step = 1; step <= steps; step++) {
                int x = from.x() + (int) Math.round((double) (to.x() - from.x()) * step / steps);
                int y = from.y() + (int) Math.round((double) (to.y() - from.y()) * step / steps);
                int z = from.z() + (int) Math.round((double) (to.z() - from.z()) * step / steps);
                while (current.x() != x) { current = current.offset(Integer.signum(x - current.x()), 0, 0); log(current, Axis.X); }
                while (current.z() != z) { current = current.offset(0, 0, Integer.signum(z - current.z())); log(current, Axis.Z); }
                while (current.y() != y) { current = current.offset(0, Integer.signum(y - current.y()), 0); log(current, Axis.Y); }
            }
        }

        private void log(Point point, Axis axis) {
            logs.putIfAbsent(point, axis);
        }
    }

    private GensokyoTreeShape() {}
}
