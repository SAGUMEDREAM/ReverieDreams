package cc.thonly.reverie_dreams.entity.ai.work;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import cc.thonly.reverie_dreams.entity.ai.goal.util.EntityTargetUtil;
import static org.junit.jupiter.api.Assertions.*;

class WorkClaimsQueueTest {
    @Test void anOwnerWithNoAttackerDoesNotCrashTargetSelection() {
        assertFalse(EntityTargetUtil.canAttack(null, null));
    }
    @Test void multiResourceClaimsAreAtomicAndExpireWithoutRetainingAnEntity() {
        var claims = WorkClaims.of(new Object()); var first = UUID.randomUUID(); var second = UUID.randomUUID();
        assertTrue(claims.take(List.of("animal-a", "animal-b"), first, 0));
        assertFalse(claims.take(List.of("animal-c", "animal-b"), second, 1));
        assertTrue(claims.available("animal-c", first, 1));
        assertFalse(claims.available("animal-b", second, 199));
        assertTrue(claims.take(List.of("animal-a", "animal-b"), second, 201));
        claims.release(first);
        assertFalse(claims.available("animal-a", first, 202));
        claims.release(second);
        assertTrue(claims.available("animal-a", first, 202));
    }
    @Test void seatCompetitionIsFairAndIndependentRestaurantsDoNotBlockOneAnother() {
        var queue = CustomerQueue.of(new Object()); var first = UUID.randomUUID(); var second = UUID.randomUUID(); var distant = UUID.randomUUID();
        var site = new WorkArea(BlockPos.ZERO, 8, 3); var other = new WorkArea(new BlockPos(100, 0, 0), 8, 3);
        queue.join(first, site, 0); queue.join(distant, other, 0); queue.join(second, site, 0);
        queue.join(first, site, 20);
        assertTrue(queue.turn(first, BlockPos.ZERO, 20));
        assertFalse(queue.turn(second, BlockPos.ZERO, 20));
        assertTrue(queue.turn(distant, other.center(), 20));
        assertEquals(2, queue.position(second, 20));
        queue.leave(first);
        assertTrue(queue.turn(second, BlockPos.ZERO, 20)); assertEquals(1, queue.position(second, 20));
    }
    @Test void abandonedCustomersExpireAndMovingAWorkSiteRejoinsAtTheEnd() {
        var queue = CustomerQueue.of(new Object()); var first = UUID.randomUUID(); var second = UUID.randomUUID();
        var site = new WorkArea(BlockPos.ZERO, 8, 3);
        queue.join(first, site, 0); queue.join(second, site, 0);
        queue.join(first, new WorkArea(new BlockPos(1, 0, 0), 8, 3), 5);
        assertTrue(queue.turn(second, BlockPos.ZERO, 5));
        queue.join(first, site, 90);
        assertEquals(1, queue.position(first, 101));
        assertTrue(queue.turn(first, BlockPos.ZERO, 101));
        assertEquals(0, queue.position(first, 191));
    }
}
