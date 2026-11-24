package cc.thonly.reverie_dreams.world.dream;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DreamGridLayoutTest {
    @Test void walkablePlanesLeaveTheEntireIslandActivityBandAndHeadroomClear() {
        assertEquals(List.of(32, 64, 192, 224), DreamGridLayout.layers().stream().map(DreamGridLayout.Layer::y).toList());
        assertEquals(List.of(true, false, true, false), DreamGridLayout.layers().stream().map(DreamGridLayout.Layer::red).toList());
        for (int y = DreamGridLayout.ACTIVITY_MIN_Y; y <= DreamGridLayout.ACTIVITY_MAX_Y; y++)
            assertFalse(DreamGridLayout.isGridPosition(new BlockPos(0, y, 0)), "Activity Y=" + y);
        for (var layer : DreamGridLayout.layers()) {
            assertFalse(DreamGridLayout.isGridPosition(new BlockPos(0, layer.y() + 1, 0)));
            assertFalse(DreamGridLayout.isGridPosition(new BlockPos(0, layer.y() + 2, 0)));
            assertFalse(DreamGridLayout.isGridPosition(new BlockPos(1, layer.y(), 1)), "Grid cells stay open");
        }
        assertThrows(UnsupportedOperationException.class, () -> DreamGridLayout.layers().clear());
    }

    @Test void unmaskedGeometricBaselineIsContinuousAcrossPositiveAndNegativeChunkSeamsRegardlessOfOrder() {
        List<List<BlockPos>> chunks = new ArrayList<>();
        for (int cx = -3; cx <= 3; cx++) for (int cz = -3; cz <= 3; cz++) {
            var blocks = DreamGridLayout.blocksInChunk(cx, cz, 0, 256);
            assertEquals(4 * 31, blocks.size());
            for (BlockPos pos : blocks) {
                assertEquals(cx, Math.floorDiv(pos.getX(), 16));
                assertEquals(cz, Math.floorDiv(pos.getZ(), 16));
            }
            chunks.add(blocks);
        }
        var forward = new HashSet<BlockPos>();
        for (var blocks : chunks) for (BlockPos pos : blocks) assertTrue(forward.add(pos), "No neighbor may write the same cell");
        Collections.reverse(chunks);
        var reverse = new HashSet<BlockPos>();
        chunks.forEach(reverse::addAll);
        assertEquals(forward, reverse);
        for (var layer : DreamGridLayout.layers()) for (int x = -48; x < 64; x++) for (int z = -48; z < 64; z++) {
            BlockPos pos = new BlockPos(x, layer.y(), z);
            boolean expected = x % 16 == 0 || z % 16 == 0;
            assertEquals(expected, forward.contains(pos), "Missing/extra world grid block " + pos);
            assertEquals(expected, DreamGridLayout.isGridPosition(pos));
        }
    }

    @Test void seededProductionRedPlanesStayCompleteAcrossCellAndChunkBoundaries() {
        for (long seed : new long[]{1, 72913, -420}) {
            for (int cx : new int[]{-33, -17, -16, -1, 0, 15, 16, 31}) {
                for (int cz : new int[]{-17, -1, 0, 15, 16}) {
                    for (int y : new int[]{32, 192}) {
                        var geometric = DreamGridLayout.blocksInChunk(cx, cz, y, y + 1);
                        assertEquals(31, geometric.size());
                        assertEquals(geometric, DreamGridLayout.blocksInChunk(seed, cx, cz, y, y + 1));
                        for (BlockPos pos : geometric) assertTrue(DreamGridLayout.isGridPosition(seed, pos));
                    }
                }
            }
        }
    }

    @Test void absoluteWorldHeightClippingNeverShiftsPlanesOrWritesBeyondTheBuildLimit() {
        assertTrue(DreamGridLayout.blocksInChunk(0, 0, 80, 192).isEmpty());
        var clipped = DreamGridLayout.blocksInChunk(-2, 1, 64, 224);
        assertEquals(62, clipped.size());
        assertEquals(Set.of(64, 192), clipped.stream().map(BlockPos::getY).collect(Collectors.toSet()));
        assertEquals(DreamGridLayout.blocksInChunk(0, 0, 0, 256), DreamGridLayout.blocksInChunk(0, 0, -64, 320));
        assertThrows(UnsupportedOperationException.class, () -> clipped.clear());
    }
}
