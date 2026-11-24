package cc.thonly.reverie_dreams.world.gensokyo.tree;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class GensokyoTreeShapeTest {
    private static final int[][] NEIGHBORS = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};

    @ParameterizedTest
    @EnumSource(GensokyoTreeProfile.class)
    void allProfilesHaveConnectedWoodAndSupportedLeavesAtTheAdvertisedTotalHeight(GensokyoTreeProfile profile) {
        Set<Integer> sampledHeights = new HashSet<>();
        for (long seed = 0; seed < 64; seed++) {
            var tree = GensokyoTreeShape.create(profile.settings(), new Random(seed * 104729));
            sampledHeights.add(tree.height());
            assertTrue(tree.height() >= profile.settings().minHeight() && tree.height() <= profile.settings().maxHeight());
            assertEquals(tree.height(), tree.voxels().stream().mapToInt(v -> v.point().y()).max().orElseThrow() + 1);
            Map<GensokyoTreeShape.Point, GensokyoTreeShape.Voxel> voxels = new HashMap<>();
            for (var voxel : tree.voxels()) {
                assertNull(voxels.put(voxel.point(), voxel), "No overlapping output positions");
                assertTrue(voxel.point().y() >= 0 && voxel.point().y() < tree.height());
                assertTrue(Math.abs(voxel.point().x()) <= 14 && Math.abs(voxel.point().z()) <= 14,
                        () -> "Footprint exceeded: " + profile + " / " + tree.form() + " / " + voxel.point());
                assertTrue(voxel.log() || voxel.point().y() >= 4, "Keep the ground walkable");
            }
            Set<GensokyoTreeShape.Point> wood = new HashSet<>();
            ArrayDeque<GensokyoTreeShape.Point> queue = new ArrayDeque<>();
            queue.add(new GensokyoTreeShape.Point(0, 0, 0));
            while (!queue.isEmpty()) {
                var point = queue.removeFirst();
                if (!wood.add(point)) continue;
                for (int[] n : NEIGHBORS) {
                    var next = point.offset(n[0], n[1], n[2]);
                    if (voxels.containsKey(next) && voxels.get(next).log() && !wood.contains(next)) queue.add(next);
                }
            }
            assertEquals(tree.voxels().stream().filter(GensokyoTreeShape.Voxel::log).count(), wood.size(), "No floating branches");
            Map<GensokyoTreeShape.Point, Integer> distances = new HashMap<>();
            wood.forEach(point -> distances.put(point, 0));
            queue.addAll(wood);
            while (!queue.isEmpty()) {
                var point = queue.removeFirst();
                for (int[] n : NEIGHBORS) {
                    var next = point.offset(n[0], n[1], n[2]);
                    if (voxels.containsKey(next) && !distances.containsKey(next)) {
                        distances.put(next, distances.get(point) + 1);
                        queue.add(next);
                    }
                }
            }
            for (var voxel : tree.voxels()) {
                if (!voxel.log()) {
                    assertEquals(distances.get(voxel.point()), voxel.leafDistance());
                    assertTrue(voxel.leafDistance() >= 1 && voxel.leafDistance() <= 6);
                }
            }
        }
        assertTrue(sampledHeights.size() >= Math.min(4, profile.settings().maxHeight() - profile.settings().minHeight() + 1),
                "Individuals should visibly vary");
    }

    @Test
    void birchCrownsExtendDownTheStemAndHeightsFavorTheirMiddleRange() {
        for (var profile : new GensokyoTreeProfile[]{GensokyoTreeProfile.FOREST_BIRCH, GensokyoTreeProfile.TALL_BIRCH, GensokyoTreeProfile.YOUNG_BIRCH}) {
            double bareRatio = 0;
            int middle = 0;
            for (int seed = 0; seed < 128; seed++) {
                var tree = GensokyoTreeShape.create(profile.settings(), new Random(seed * 104729L));
                int firstLeaf = tree.voxels().stream().filter(voxel -> !voxel.log()).mapToInt(voxel -> voxel.point().y()).min().orElseThrow();
                bareRatio += (double) firstLeaf / tree.height();
                if (tree.height() > profile.settings().minHeight() && tree.height() < profile.settings().maxHeight()) middle++;
            }
            assertTrue(bareRatio / 128 < 0.5, "Birches should have foliage along the upper stem, rather than just at the tip");
            assertTrue(middle > 90, "Extreme-height trees should be a minority within their age class");
        }
    }

    @ParameterizedTest
    @EnumSource(value = GensokyoTreeProfile.class, names = {"SPRUCE", "YOUNG_SPRUCE"})
    void spruceCrownCoversEveryLogAndItsTopFourBlocksAreNeedles(GensokyoTreeProfile profile) {
        for (long seed = 0; seed < 256; seed++) {
            var tree = GensokyoTreeShape.create(profile.settings(), new Random(seed * 104729));
            Map<GensokyoTreeShape.Point, GensokyoTreeShape.Voxel> cells = new HashMap<>();
            tree.voxels().forEach(voxel -> cells.put(voxel.point(), voxel));
            int crownBase = tree.voxels().stream().filter(voxel -> !voxel.log()).mapToInt(voxel -> voxel.point().y()).min().orElseThrow();
            for (int y = tree.height() - 4; y < tree.height(); y++) {
                var tip = cells.get(new GensokyoTreeShape.Point(0, y, 0));
                assertNotNull(tip, "No gap in the needle-covered tip");
                assertFalse(tip.log(), "The trunk must end below the top four needle blocks");
            }
            for (var voxel : tree.voxels()) {
                if (!voxel.log() || voxel.point().y() < crownBase) continue;
                for (int[] direction : new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}}) {
                    var neighbor = cells.get(voxel.point().offset(direction[0], direction[1], direction[2]));
                    assertNotNull(neighbor, () -> "Crown wood protrudes into open air: " + voxel.point());
                }
            }
        }
    }

    @Test
    void deciduousProfilesMixBranchingSilhouettesAndKeepDeepCrowns() {
        for (var profile : GensokyoTreeProfile.values()) {
            if (profile.settings().species() == GensokyoTreeShape.Species.SPRUCE || profile.settings().crownStyle().isJungle()) continue;
            var forms = new HashSet<GensokyoTreeShape.Form>();
            int upright = 0;
            for (int seed = 0; seed < 128; seed++) {
                var tree = GensokyoTreeShape.create(profile.settings(), new Random(seed * 104729L));
                forms.add(tree.form());
                if (tree.form() == GensokyoTreeShape.Form.UPRIGHT) upright++;
                int firstLeaf = tree.voxels().stream().filter(v -> !v.log()).mapToInt(v -> v.point().y()).min().orElseThrow();
                assertTrue(firstLeaf <= Math.max(4, tree.height() * 0.5),
                        () -> profile + " has only a high crown: " + tree.form() + " / " + tree.height());
                if (tree.form() != GensokyoTreeShape.Form.UPRIGHT && !profile.isAutumnMaple()) {
                    // A high uninterrupted central pole would recreate the original silhouette.
                    long centralUpperLogs = tree.voxels().stream().filter(v -> v.log() && v.point().x() == 0
                            && v.point().z() == 0 && v.point().y() >= tree.height() * 0.75).count();
                    assertTrue(centralUpperLogs <= 1, () -> "Central pole above the fork: " + profile + " / " + tree.form());
                }
            }
            assertTrue(forms.size() >= 4, () -> "Only one crown silhouette for " + profile);
            assertTrue(upright < 32, "Narrow upright birches should be a minority");
        }
    }

    @Test
    void mapleCrownsAreMostlyTallTaperedAndCoverTheirUpperWood() {
        for (var profile : GensokyoTreeProfile.values()) {
            if (!profile.isAutumnMaple()) continue;
            int tall = 0;
            for (int seed = 0; seed < 128; seed++) {
                var tree = GensokyoTreeShape.create(profile.settings(), new Random(seed * 104729L));
                if (tree.form() == GensokyoTreeShape.Form.SPREADING) continue;
                tall++;
                Map<GensokyoTreeShape.Point, GensokyoTreeShape.Voxel> cells = new HashMap<>();
                tree.voxels().forEach(v -> cells.put(v.point(), v));
                var peak = tree.voxels().stream().filter(v -> !v.log()).max(Comparator.comparingInt(v -> v.point().y())).orElseThrow().point();
                for (int y = tree.height() - 3; y < tree.height(); y++) {
                    var needle = cells.get(new GensokyoTreeShape.Point(peak.x(), y, peak.z()));
                    assertNotNull(needle, "Continuous leafy maple leader");
                    assertFalse(needle.log(), "No naked trunk at the tip");
                }
                int firstLeaf = tree.voxels().stream().filter(v -> !v.log()).mapToInt(v -> v.point().y()).min().orElseThrow();
                assertTrue(firstLeaf <= Math.max(4, tree.height() * 0.32), "Maples need a deep vertical crown");
                if (tree.height() >= 16) {
                    var middle = tree.voxels().stream().filter(v -> !v.log() && v.point().y() >= tree.height() * 0.4
                            && v.point().y() <= tree.height() * 0.7).map(GensokyoTreeShape.Voxel::point).toList();
                    int spanX = middle.stream().mapToInt(GensokyoTreeShape.Point::x).max().orElseThrow()
                            - middle.stream().mapToInt(GensokyoTreeShape.Point::x).min().orElseThrow() + 1;
                    int spanZ = middle.stream().mapToInt(GensokyoTreeShape.Point::z).max().orElseThrow()
                            - middle.stream().mapToInt(GensokyoTreeShape.Point::z).min().orElseThrow() + 1;
                    assertTrue(Math.min(spanX, spanZ) >= 7, () -> "Maple crown is too thin: " + profile + " / " + tree.form());
                }
                for (var voxel : tree.voxels()) {
                    if (!voxel.log() || voxel.point().y() < tree.height() * 0.75) continue;
                    for (int[] direction : new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}}) {
                        assertNotNull(cells.get(voxel.point().offset(direction[0], 0, direction[2])),
                                () -> "Exposed upper maple wood: " + profile + " / " + voxel.point());
                    }
                }
            }
            assertTrue(tall >= 112, "Tall crowns should dominate, with only occasional wide trees");
        }
    }

    @Test
    void originalProfilesKeepTheirSeededGeometryAndHeightBounds() throws Exception {
        try (var input = getClass().getResourceAsStream("/data/reverie_dreams/tree/legacy-geometry-sha256.json")) {
            assertNotNull(input);
            var samples = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonArray();
            var digest = MessageDigest.getInstance("SHA-256");
            for (var sample : samples) {
                var expected = sample.getAsJsonObject();
                var profile = GensokyoTreeProfile.valueOf(expected.get("profile").getAsString());
                assertFalse(profile.isAutumnMaple());
                assertEquals(expected.get("min_height").getAsInt(), profile.settings().minHeight());
                assertEquals(expected.get("max_height").getAsInt(), profile.settings().maxHeight());
                var tree = GensokyoTreeShape.create(profile.settings(), new Random(expected.get("seed").getAsLong()));
                var encoded = new StringBuilder().append(tree.height()).append(':').append(tree.form()).append('\n');
                for (var v : tree.voxels()) {
                    encoded.append(v.point().x()).append(',').append(v.point().y()).append(',').append(v.point().z()).append(',')
                            .append(v.log() ? 1 : 0).append(',').append(v.axis()).append(',').append(v.leafDistance()).append('\n');
                }
                assertEquals(expected.get("sha256").getAsString(), HexFormat.of().formatHex(digest.digest(encoded.toString().getBytes(StandardCharsets.UTF_8))),
                        () -> "Original tree changed: " + profile + " / " + expected.get("seed"));
            }
        }
    }

    @Test
    void geometryIsRepeatableAndCatalogProbabilitiesAreAbsoluteWeights() {
        for (var profile : GensokyoTreeProfile.values()) {
            assertEquals(GensokyoTreeShape.create(profile.settings(), new Random(123)),
                    GensokyoTreeShape.create(profile.settings(), new Random(123)));
        }
        for (var type : GensokyoForestType.values()) {
            assertEquals(1.0, type.mix().stream().mapToDouble(GensokyoForestType.Mix::weight).sum(), 0.00001);
        }
    }

    @Test
    void invalidDimensionsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new GensokyoTreeShape.Settings(GensokyoTreeShape.Species.OAK, 20, 10, 1, 4));
        assertThrows(IllegalArgumentException.class, () -> new GensokyoTreeShape.Settings(GensokyoTreeShape.Species.OAK, 8, 100, 1, 4));
    }
}
