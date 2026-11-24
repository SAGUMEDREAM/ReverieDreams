package cc.thonly.reverie_dreams.entity.ai.work;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class WorkAreaSearchTest {
    @Test void enumeratesTheWholeBoundedAreaOnceStartingAtTheCenter() {
        for (int radius : new int[]{1, 3, 8, 16}) {
            var area = new WorkArea(new BlockPos(31, 70, -15), radius, 3);
            var seen = new HashSet<BlockPos>();
            assertEquals(area.center(), area.position(0));
            for (int i = 0; i < area.size(); i++) { var pos = area.position(i); assertTrue(area.contains(pos)); assertTrue(seen.add(pos), pos.toString()); }
            assertEquals(area.size(), seen.size());
        }
    }
    @Test void searchBudgetIsBoundedAndUnloadedPositionsAreNeverRead() {
        var search = new WorkSearch(); var area = new WorkArea(BlockPos.ZERO, 8, 8);
        var reads = new AtomicInteger(); var checks = new AtomicInteger();
        assertNull(search.find(area, 0, p -> { checks.incrementAndGet(); return false; }, p -> { reads.incrementAndGet(); return true; }));
        assertTrue(checks.get() <= 128); assertEquals(0, reads.get());
    }
    @Test void cachesLiveTargetsSkipsFailuresAndInvalidatesOnSiteChange() {
        var search = new WorkSearch(); var area = new WorkArea(BlockPos.ZERO, 1, 1);
        assertEquals(BlockPos.ZERO, search.find(area, 0, p -> true, p -> true));
        search.reject(BlockPos.ZERO, 0);
        assertNull(search.find(area, 1, p -> true, p -> true));
        assertNotEquals(BlockPos.ZERO, search.find(area, 10, p -> true, p -> true));
        var moved = new WorkArea(new BlockPos(50, 0, 0), 1, 1);
        assertEquals(moved.center(), search.find(moved, 11, p -> true, p -> true));
    }
}
