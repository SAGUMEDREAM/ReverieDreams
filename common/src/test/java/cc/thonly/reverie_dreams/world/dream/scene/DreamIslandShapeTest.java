package cc.thonly.reverie_dreams.world.dream.scene;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class DreamIslandShapeTest {
    private static final String[] IDS = {"huaian_station", "bamboo_memory", "tea_garden_memory",
            "mirror_pond_memory", "crystal_reef_memory", "cherry_isle_memory", "archive_ruin_memory"};

    @Test void authoredCoastsRemainBoundedAndSolidUnderTheirVaryingOutlines() {
        for (String id : IDS) for (int seed = 0; seed < 8; seed++) {
            var shape = DreamIslandShape.forScene(id, seed);
            int area = 0, shallow = 0, deep = 0;
            for (int x = -28; x <= 28; x++) for (int z = -28; z <= 28; z++) {
                if (!shape.contains(x, z)) continue;
                area++;
                int depth = shape.depthAt(x, z, 16);
                assertTrue(depth >= 1 && depth <= 16);
                if (depth <= 3) shallow++;
                if (depth >= 7) deep++;
            }
            assertTrue(area > 250 && area < 3000, id + " must provide an island-sized coherent footprint");
            assertTrue(shallow > 0 && deep > 0, id + " must taper instead of becoming a flat cylinder");
            assertTrue(shape.minX() >= -28 && shape.maxX() <= 28 && shape.minZ() >= -28 && shape.maxZ() <= 28);
        }
    }

    @Test void seedChangesWholeCoastlinesDeterministicallyAndThemesHaveDifferentShapes() {
        var themes = new HashSet<String>();
        for (String id : IDS) {
            var variants = new HashSet<String>();
            for (int seed = 0; seed < 8; seed++) {
                var shape = DreamIslandShape.forScene(id, seed);
                var repeated = DreamIslandShape.forScene(id, seed);
                StringBuilder mask = new StringBuilder();
                for (int x = -28; x <= 28; x++) for (int z = -28; z <= 28; z++) {
                    assertEquals(shape.contains(x, z), repeated.contains(x, z));
                    mask.append(shape.contains(x, z) ? '1' : '0');
                }
                variants.add(mask.toString());
                if (seed == 0) themes.add(mask.toString());
            }
            assertTrue(variants.size() >= 4, id + " should vary by seed without losing its outline");
        }
        assertEquals(IDS.length, themes.size());
    }
}
