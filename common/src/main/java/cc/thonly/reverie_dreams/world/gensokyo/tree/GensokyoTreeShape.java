package cc.thonly.reverie_dreams.world.gensokyo.tree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

/**
 * World-independent geometry, shared by world generation and the offline preview.
 */
public final class GensokyoTreeShape {
    public enum Species {
        OAK, DARK_OAK, BIRCH, SPRUCE, CHERRY, IROHA_MAPLE, ITAYA_MAPLE, HAUCHIWA_MAPLE, JUNGLE;

        public boolean isMaple() {
            return this == IROHA_MAPLE || this == ITAYA_MAPLE || this == HAUCHIWA_MAPLE;
        }
    }

    public enum Form {
        FORKED, SPREADING, TIERED, WINDSWEPT, UPRIGHT, CONIFER,
        MAPLE_TAPERED, MAPLE_LAYERED, MAPLE_FORKED, ROOTED_CANOPY, JUNGLE_YOUNG
    }

    public enum CrownStyle {
        BRANCHING, TALL_MAPLE,
        JUNGLE_CANOPY, JUNGLE_FORKED, JUNGLE_TIERED, JUNGLE_LEANING, JUNGLE_SPREADING, JUNGLE_YOUNG;

        public boolean isJungle() {
            return name().startsWith("JUNGLE_");
        }
    }

    public enum Axis {X, Y, Z}

    public record Point(int x, int y, int z) {
        Point offset(int dx, int dy, int dz) {
            return new Point(x + dx, y + dy, z + dz);
        }
    }

    public record Voxel(Point point, boolean log, Axis axis, int leafDistance) {
    }

    public record Tree(int height, List<Voxel> voxels, Form form) {
    }

    public record Settings(Species species, int minHeight, int maxHeight, int trunkWidth, int crownRadius,
                           CrownStyle crownStyle) {
        public Settings(Species species, int minHeight, int maxHeight, int trunkWidth, int crownRadius) {
            this(species, minHeight, maxHeight, trunkWidth, crownRadius, CrownStyle.BRANCHING);
        }

        public Settings {
            if (species == null || minHeight < 8 || maxHeight > 40 || minHeight > maxHeight
                    || trunkWidth < 1 || trunkWidth > 2 || crownRadius < 2 || crownRadius > 6 || crownStyle == null
                    || (crownStyle == CrownStyle.TALL_MAPLE && !species.isMaple())
                    || (crownStyle.isJungle() && species != Species.JUNGLE)) {
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
        private final Form form;
        private final Map<Point, Axis> logs = new LinkedHashMap<>();
        private final Map<Point, Boolean> leaves = new LinkedHashMap<>();

        Builder(Settings settings, RandomGenerator random) {
            this.settings = settings;
            this.random = random;
            int span = settings.maxHeight() - settings.minHeight() + 1;
            // Most individuals sit near the middle of their age class, with fewer extremes.
            this.height = settings.minHeight() + (random.nextInt(span) + random.nextInt(span)) / 2;
            // Do not consume another random value for spruce: its repaired needle crown is unchanged.
            this.form = settings.species() == Species.SPRUCE ? Form.CONIFER : chooseForm();
        }

        Tree build() {
            switch (form) {
                case CONIFER -> spruce();
                case FORKED -> forked();
                case SPREADING -> spreading();
                case TIERED -> tiered();
                case WINDSWEPT -> windswept();
                case UPRIGHT -> uprightBirch();
                case MAPLE_TAPERED, MAPLE_LAYERED, MAPLE_FORKED -> autumnMaple();
                case ROOTED_CANOPY -> rootedCanopy();
                case JUNGLE_YOUNG -> jungleYoung();
            }
            // Actual six-neighbor leaf paths, rather than a Manhattan estimate through air.
            Map<Point, Integer> distances = new LinkedHashMap<>();
            ArrayDeque<Point> queue = new ArrayDeque<>(logs.keySet());
            while (!queue.isEmpty()) {
                Point current = queue.removeFirst();
                int nextDistance = distances.getOrDefault(current, 0) + 1;
                if (nextDistance > 6)
                    continue;
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
            return new Tree(height, List.copyOf(result), form);
        }

        private Form chooseForm() {
            // Each jungle profile has its own silhouette, with individual branch/height variation.
            if (settings.crownStyle().isJungle())
                return switch (settings.crownStyle()) {
                    case JUNGLE_CANOPY -> Form.ROOTED_CANOPY;
                    case JUNGLE_FORKED -> Form.FORKED;
                    case JUNGLE_TIERED -> Form.TIERED;
                    case JUNGLE_LEANING -> Form.WINDSWEPT;
                    case JUNGLE_SPREADING -> Form.SPREADING;
                    case JUNGLE_YOUNG -> Form.JUNGLE_YOUNG;
                    default -> throw new IllegalStateException("Unmapped jungle crown style");
                };
            int roll = random.nextInt(100);
            if (settings.crownStyle() == CrownStyle.TALL_MAPLE) {
                int taperWeight = settings.species() == Species.ITAYA_MAPLE ? 40 : 50;
                return roll < taperWeight ? Form.MAPLE_TAPERED : roll < 80 ? Form.MAPLE_LAYERED
                        : roll < 95 ? Form.MAPLE_FORKED : Form.SPREADING;
            }
            return switch (settings.species()) {
                case BIRCH -> roll < 15 ? Form.UPRIGHT : roll < 60 ? Form.FORKED
                        : roll < 85 ? Form.TIERED : Form.WINDSWEPT;
                case CHERRY, IROHA_MAPLE, HAUCHIWA_MAPLE -> roll < 45 ? Form.SPREADING
                        : roll < 70 ? Form.FORKED : roll < 90 ? Form.WINDSWEPT : Form.TIERED;
                case DARK_OAK, ITAYA_MAPLE -> roll < 50 ? Form.FORKED : roll < 80 ? Form.TIERED
                        : roll < 90 ? Form.SPREADING : Form.WINDSWEPT;
                case OAK, JUNGLE -> roll < 40 ? Form.FORKED : roll < 65 ? Form.TIERED
                        : roll < 90 ? Form.SPREADING : Form.WINDSWEPT;
                case SPRUCE -> Form.CONIFER;
            };
        }

        private int crownRadius() {
            int limit = settings.species() == Species.BIRCH ? 3 : 6;
            return Math.clamp(settings.crownRadius() + random.nextInt(-1, 2), 2, limit);
        }

        private int crownDepth() {
            return Math.clamp(height / 5, 2, 5);
        }

        private Point polar(Point from, double angle, int reach, int y) {
            return new Point(from.x() + (int) Math.round(Math.cos(angle) * reach), y,
                    from.z() + (int) Math.round(Math.sin(angle) * reach));
        }

        private Point base(int y) {
            return stem(y, random.nextInt(-1, 2), random.nextInt(-1, 2), settings.trunkWidth()).getLast();
        }

        private Point halfway(Point from, Point to) {
            return new Point((from.x() + to.x()) / 2, (from.y() + to.y()) / 2, (from.z() + to.z()) / 2);
        }

        private void crown(Point center, int radius, int depth) {
            ellipsoid(center.x(), center.y(), center.z(), radius, depth, Math.max(2, radius - random.nextInt(2)));
        }

        private void leafyLimb(Point from, Point to, int radius, int depth) {
            Point elbow = halfway(from, to).offset(0, 1, 0);
            line(from, elbow);
            line(elbow, to);
            // A branch carries foliage along its length; it does not end in an isolated ball.
            crown(halfway(from, elbow), Math.max(2, radius - 2), Math.max(2, depth - 1));
            crown(elbow, Math.max(2, radius - 1), Math.max(2, depth - 1));
            crown(to, radius, depth);
        }

        private void forked() {
            Point split = base(Math.max(3, height / 3));
            int radius = crownRadius();
            int depth = crownDepth();
            int leaders = settings.species() == Species.BIRCH ? 2 : 3 + random.nextInt(2);
            double angle = random.nextDouble() * Math.PI * 2;
            for (int i = 0; i < leaders; i++) {
                int y = i == 0 ? height - 1 - depth : Math.max(split.y() + 1,
                        height - 1 - depth - random.nextInt(1, Math.max(2, height / 4)));
                Point tip = polar(split, angle + i * 2.3999632297 + random.nextDouble(-0.25, 0.25),
                        Math.max(2, radius - 1) + random.nextInt(2), y);
                leafyLimb(split, tip, radius, depth);
                if (height >= 14) {
                    Point twig = polar(tip, angle + i * 2.3999632297 + 1.1, 2, Math.max(split.y() + 2, y - 2));
                    leafyLimb(halfway(split, tip), twig, Math.max(2, radius - 1), Math.max(2, depth - 1));
                }
            }
        }

        private void rootedCanopy() {
            var trunk = stem(height - 5, random.nextInt(-1, 2), random.nextInt(-1, 2), 2);
            // Broad, low buttresses support the trunk; placement checks every foot before writing.
            for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                Point outer = new Point(d[0] * 3 + (d[0] > 0 ? 1 : 0), 0,
                        d[1] * 3 + (d[1] > 0 ? 1 : 0));
                line(new Point(d[0] > 0 ? 1 : 0, 3, d[1] > 0 ? 1 : 0), outer);
            }
            Point leader = trunk.getLast().offset(0, 1, 0);
            line(trunk.getLast(), leader);
            crown(leader, 3, 3);
            double rotation = random.nextDouble() * Math.PI * 2;
            for (int i = 0; i < 5; i++) {
                Point from = trunk.get(height / 2 + i * Math.max(1, height / 28));
                int depth = 3;
                int y = height - 6 - random.nextInt(3);
                Point tip = polar(from, rotation + i * 2.3999632297,
                        4 + random.nextInt(3), Math.max(from.y() + 1, y));
                leafyLimb(from, tip, Math.max(3, crownRadius() - 1), depth);
            }
        }

        private void jungleYoung() {
            int depth = 2;
            Point fork = base(3);
            double angle = random.nextDouble() * Math.PI * 2;
            Point leader = polar(fork, angle, 1 + random.nextInt(2), height - 1 - depth);
            leafyLimb(fork, leader, 2, depth);
            Point side = polar(fork, angle + 2.3, 2, Math.max(4, height - 4 - depth));
            leafyLimb(fork, side, 2, depth);
        }

        private void spreading() {
            Point split = base(Math.max(3, height * 3 / 10));
            int radius = crownRadius();
            int depth = Math.max(2, crownDepth() - 1);
            int branches = 3 + random.nextInt(3);
            double angle = random.nextDouble() * Math.PI * 2;
            for (int i = 0; i < branches; i++) {
                double direction = angle + i * 2.3999632297 + random.nextDouble(-0.3, 0.3);
                int y = i == 0 ? height - 1 - depth
                        : Math.max(split.y() + 1, (int) (height * random.nextDouble(0.55, 0.77)));
                // Several uneven, ascending boughs form a broad crown with open spaces between them.
                Point tip = polar(split, direction, Math.min(7, radius + random.nextInt(1, 3)), y);
                leafyLimb(split, tip, Math.max(2, radius - 1), depth);
                Point outer = polar(tip, direction + 0.65, 2, Math.max(split.y() + 1, y - 1));
                leafyLimb(halfway(split, tip), outer, Math.max(2, radius - 2), depth);
            }
        }

        private void tiered() {
            int firstFork = Math.max(3, height / 3);
            var trunk = stem(Math.max(firstFork + 1, height * 2 / 3), random.nextInt(-2, 3),
                    random.nextInt(-2, 3), settings.trunkWidth());
            int radius = crownRadius();
            int depth = crownDepth();
            double angle = random.nextDouble() * Math.PI * 2;
            for (int i = 0; i < 4; i++) {
                Point from = trunk.get(Math.min(trunk.size() - 1, firstFork + i * Math.max(1, height / 11)));
                int y = i == 3 ? height - 1 - depth
                        : Math.min(height - 2 - depth, Math.max(from.y() + 1, height * (5 + i) / 10));
                Point tip = polar(from, angle + i * 2.3999632297, Math.max(2, radius - i / 2), y);
                leafyLimb(from, tip, Math.max(2, radius - i / 2), depth);
            }
        }

        private void windswept() {
            int radius = crownRadius();
            int depth = crownDepth();
            double angle = random.nextDouble() * Math.PI * 2;
            Point split = base(Math.max(3, height / 3));
            Point bend = polar(split, angle, 2, Math.max(split.y() + 1, height * 3 / 5));
            line(split, bend);
            crown(bend, Math.max(2, radius - 1), depth);
            Point leader = polar(split, angle, 4 + random.nextInt(2), height - 1 - depth);
            leafyLimb(bend, leader, radius, depth);
            for (int i = 0; i < 3; i++) {
                Point from = i == 0 ? split : bend;
                double direction = angle + (i - 1) * 0.95;
                Point tip = polar(from, direction, Math.max(2, radius), Math.max(from.y(), height * (5 + i) / 10));
                leafyLimb(from, tip, Math.max(2, radius - 1), Math.max(2, depth - 1));
            }
        }

        private void uprightBirch() {
            int leanX = random.nextInt(-1, 2);
            int leanZ = random.nextInt(-1, 2);
            List<Point> stem = stem(height - 3, leanX, leanZ, 1);
            int crownBase = Math.max(4, (int) (height * random.nextDouble(0.30, 0.40)));
            int widest = Math.min(settings.crownRadius(), 3);
            int biasX = random.nextInt(-1, 2);
            int biasZ = random.nextInt(-1, 2);
            for (int y = crownBase + 2; y <= height - 3; y += 2) {
                double t = (double) (y - crownBase) / Math.max(1, height - 1 - crownBase);
                int radius = Math.max(2, (int) Math.round(widest * Math.sin(Math.PI * t)));
                Point center = stem.get(y);
                ellipsoid(center.x() + biasX, y, center.z() + biasZ, radius, 2,
                        Math.max(2, radius - random.nextInt(2)));
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
            // The tip remains supported and the total height has its literal advertised meaning.
            ellipsoid(leanX, height - 3, leanZ, 1, 2, 1);
        }

        private void autumnMaple() {
            int top = height - 4;
            int leanX = random.nextInt(-2, 3);
            int leanZ = random.nextInt(-2, 3);
            var spine = new ArrayList<Point>();
            Point previous = new Point(0, 0, 0);
            for (int y = 0; y <= top; y++) {
                double t = Math.max(0, (double) (y - height / 5) / Math.max(1, top - height / 5));
                Point center = new Point((int) Math.round(leanX * t), y, (int) Math.round(leanZ * t));
                line(previous, center);
                int width = y < height * 0.42 ? settings.trunkWidth() : 1;
                for (int dx = 0; dx < width; dx++) {
                    for (int dz = 0; dz < width; dz++)
                        log(center.offset(dx, 0, dz), Axis.Y);
                }
                spine.add(center);
                previous = center;
            }
            // Small attached buttresses widen the foot without a long horizontal root skeleton.
            if (height >= 16) {
                double rootAngle = random.nextDouble() * Math.PI * 2;
                for (int i = 0; i < 3; i++) {
                    Point foot = polar(spine.getFirst(), rootAngle + i * 2.1, 1, 0);
                    line(foot, spine.get(2));
                }
            }
            int crownBase = Math.max(4, (int) (height * random.nextDouble(0.23, 0.30)));
            int widest = Math.clamp(settings.crownRadius() + random.nextInt(2), 3, 6);
            double rotation = random.nextDouble() * Math.PI * 2;
            boolean layered = form == Form.MAPLE_LAYERED;
            int tier = 0;
            for (int y = crownBase; y <= top; y += 2, tier++) {
                double t = (double) (y - crownBase) / Math.max(1, top - crownBase);
                // Full lower/middle crown, steadily narrowing shoulders and a leafy leader.
                double envelope = Math.pow(Math.max(0, 1 - Math.pow(t, layered ? 2.5 : 3.0)), 0.65)
                        * (0.88 + 0.12 * Math.sin(Math.PI * t));
                int radius = Math.clamp((int) Math.round(widest * envelope), 1, 6);
                if (layered && tier % 3 == 1 && radius > 2)
                    radius--;
                Point center = spine.get(y);
                int depth = layered ? 2 : 3;
                ellipsoid(center.x(), y, center.z(), radius, depth, Math.max(1, radius - random.nextInt(2)));
                if (radius >= 3) {
                    double angle = rotation + tier * 2.3999632297 + random.nextDouble(-0.3, 0.3);
                    Point tip = polar(center, angle, Math.max(1, radius - 2), Math.min(top, y + 1));
                    line(center, tip);
                    crown(tip, Math.min(3, radius - 1), 2);
                    // Offset leafy shoulders give a broadleaf outline rather than identical conifer discs.
                    Point shoulder = polar(center, angle + 1.2, Math.max(1, radius - 1), y);
                    line(center, halfway(center, shoulder));
                    crown(shoulder, 2, 2);
                }
            }
            if (form == Form.MAPLE_FORKED && height >= 12) {
                Point split = spine.get(height * 2 / 5);
                Point second = polar(split, rotation + 1.1, 3, height - 6);
                leafyLimb(split, second, Math.min(4, widest), 3);
            }
            // Keep random edge holes away from the thin upper leader and its branch tips.
            for (Point wood : logs.keySet()) {
                if (wood.y() < height * 0.75)
                    continue;
                for (int[] offset : NEIGHBORS) {
                    if (offset[1] != 0)
                        continue;
                    Point next = wood.offset(offset[0], 0, offset[2]);
                    if (!logs.containsKey(next))
                        leaves.put(next, true);
                }
            }
            // The last three blocks are leaves, with wood below them and no bare pole at the tip.
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
                    for (int dz = 0; dz < width; dz++)
                        log(center.offset(dx, 0, dz), Axis.Y);
                }
                previous = center;
            }
            return result;
        }

        private void spruce() {
            // Keep the final four blocks needle-only instead of exposing a pole above the crown.
            int trunkTop = height - 5;
            for (int y = 0; y <= trunkTop; y++)
                log(new Point(0, y, 0), Axis.Y);
            int firstLayer = Math.max(4, (int) (height * random.nextDouble(0.22, 0.34)));
            int widest = Math.clamp(settings.crownRadius() + random.nextInt(-1, 2), 2, 6);
            int tierPhase = random.nextInt(3);
            boolean alongX = random.nextBoolean();
            // A connected crown at every height avoids bare gaps between shrinking ellipsoids.
            for (int y = firstLayer; y < height - 1; y++) {
                double fraction = (double) (y - firstLayer) / Math.max(1, height - 1 - firstLayer);
                int radius = Math.max(1, (int) Math.ceil(widest * Math.pow(1 - fraction, 0.9)));
                if ((y - firstLayer + tierPhase) % 3 == 2 && radius > 2)
                    radius--;
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
                    if (distance > 1)
                        continue;
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
                        if (y < 4 || y >= height)
                            continue;
                        double distance = (double) dx * dx / (rx * rx) + (double) dy * dy / (ry * ry)
                                + (double) dz * dz / (rz * rz);
                        if (distance <= 1.0 && (distance < 0.82 || random.nextInt(6) != 0)) {
                            leaves.put(new Point(cx + dx, y, cz + dz), true);
                        }
                    }
                }
            }
            // Do not let edge randomness remove the intended height marker.
            if (cy + ry == height - 1)
                leaves.put(new Point(cx, height - 1, cz), true);
        }

        private void line(Point from, Point to) {
            Point current = from;
            log(current, Axis.Y);
            int steps = Math.max(Math.max(Math.abs(to.x() - from.x()), Math.abs(to.y() - from.y())), Math.abs(to.z() - from.z()));
            for (int step = 1; step <= steps; step++) {
                int x = from.x() + (int) Math.round((double) (to.x() - from.x()) * step / steps);
                int y = from.y() + (int) Math.round((double) (to.y() - from.y()) * step / steps);
                int z = from.z() + (int) Math.round((double) (to.z() - from.z()) * step / steps);
                while (current.x() != x) {
                    current = current.offset(Integer.signum(x - current.x()), 0, 0);
                    log(current, Axis.X);
                }
                while (current.z() != z) {
                    current = current.offset(0, 0, Integer.signum(z - current.z()));
                    log(current, Axis.Z);
                }
                while (current.y() != y) {
                    current = current.offset(0, Integer.signum(y - current.y()), 0);
                    log(current, Axis.Y);
                }
            }
        }

        private void log(Point point, Axis axis) {
            logs.putIfAbsent(point, axis);
        }
    }

    private GensokyoTreeShape() {
    }
}
