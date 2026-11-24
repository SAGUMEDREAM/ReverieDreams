package cc.thonly.reverie_dreams.entity.ai.goal.work;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class CustomerSeatSearchTest {
    @Test
    void keepsAnAvailableSeatWithoutRescanning() {
        CustomerSeatSearch search = new CustomerSeatSearch();
        BlockPos seat = new BlockPos(2, 0, 0);
        AtomicInteger checks = new AtomicInteger();
        Predicate<BlockPos> available = pos -> {
            checks.incrementAndGet();
            return pos.equals(seat);
        };
        search.reset(0, 0);
        assertEquals(seat, search.find(0, BlockPos.ZERO, available));
        int firstSearch = checks.get();
        for (int tick = 1; tick <= 100; tick++) {
            assertEquals(seat, search.find(tick, BlockPos.ZERO, available));
        }
        assertEquals(firstSearch + 100, checks.get(), "A cached seat needs only one availability check per tick");
    }

    @Test
    void throttlesEmptySearchesAndStaggersTheFirstAttempt() {
        CustomerSeatSearch search = new CustomerSeatSearch();
        AtomicInteger checks = new AtomicInteger();
        Predicate<BlockPos> unavailable = pos -> { checks.incrementAndGet(); return false; };
        search.reset(100, 7);
        for (int tick = 100; tick < 107; tick++) assertNull(search.find(tick, BlockPos.ZERO, unavailable));
        assertEquals(0, checks.get());
        assertNull(search.find(107, BlockPos.ZERO, unavailable));
        assertEquals(4913, checks.get());
        for (int tick = 108; tick < 127; tick++) assertNull(search.find(tick, BlockPos.ZERO, unavailable));
        assertEquals(4913, checks.get());
        assertNull(search.find(127, BlockPos.ZERO, unavailable));
        assertEquals(9826, checks.get());
    }

    @Test
    void selectsNearestEligibleSeatAndInvalidatesAReservedOrRemovedTarget() {
        CustomerSeatSearch search = new CustomerSeatSearch();
        BlockPos near = new BlockPos(1, 0, 0);
        BlockPos far = new BlockPos(3, 0, 0);
        Set<BlockPos> available = new HashSet<>(Set.of(near, far));
        search.reset(0, 0);
        assertEquals(near, search.find(0, BlockPos.ZERO, available::contains));
        available.remove(near);
        assertNull(search.find(1, BlockPos.ZERO, available::contains));
        assertEquals(far, search.find(20, BlockPos.ZERO, available::contains));
        assertNull(search.find(21, new BlockPos(30, 0, 0), available::contains));
        search.reset(30, 0);
        assertNull(search.target());
        assertEquals(far, search.find(30, BlockPos.ZERO, available::contains));
    }
}
