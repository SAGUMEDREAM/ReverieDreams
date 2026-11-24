package cc.thonly.reverie_dreams.entity.ai.goal.work;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CustomerNavigationProgressTest {
    @Test void rejectsConsecutivePathFailuresAndRecoversToAnotherSeat() {
        var progress = new WorkNavigationProgress();
        progress.reset(0, 64);
        assertFalse(progress.pathFailed(false));
        assertFalse(progress.pathFailed(false));
        assertTrue(progress.pathFailed(false));
        var search = new CustomerSeatSearch();
        var near = new BlockPos(1, 0, 0);
        var far = new BlockPos(3, 0, 0);
        var available = Set.of(near, far);
        search.reset(0, 0);
        assertEquals(near, search.find(0, BlockPos.ZERO, available::contains));
        search.reject(20, near);
        assertEquals(far, search.find(21, BlockPos.ZERO, available::contains));
        assertNull(search.find(219, BlockPos.ZERO, near::equals));
        assertEquals(near, search.find(239, BlockPos.ZERO, near::equals));
    }

    @Test void partialPathsCannotKeepAnNPCStuckForever() {
        var progress = new WorkNavigationProgress();
        progress.reset(0, 64);
        assertFalse(progress.pathFailed(true));
        assertFalse(progress.stalled(99, 64));
        assertTrue(progress.stalled(100, 64));
        progress.reset(0, 64);
        assertFalse(progress.stalled(90, 49));
        assertFalse(progress.stalled(150, 49));
        assertTrue(progress.stalled(190, 49));
        assertFalse(progress.pathFailed(false));
        assertFalse(progress.pathFailed(true));
        assertFalse(progress.pathFailed(false));
    }
}
