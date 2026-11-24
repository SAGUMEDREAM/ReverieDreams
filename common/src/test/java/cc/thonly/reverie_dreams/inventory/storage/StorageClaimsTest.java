package cc.thonly.reverie_dreams.inventory.storage;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class StorageClaimsTest {
    @Test void multiNPCAcquisitionIsAllOrNothingAndExpires() {
        var claims = new StorageClaims();
        var a = UUID.randomUUID(); var b = UUID.randomUUID();
        var input = BlockPos.ZERO; var output = new BlockPos(1, 0, 0);
        assertTrue(claims.acquire(List.of(input), a, 0));
        assertFalse(claims.acquire(List.of(output, input), b, 1));
        assertTrue(claims.available(output, a, 1));
        assertFalse(claims.available(input, b, 199));
        assertTrue(claims.acquire(List.of(input, output), b, 200));
        claims.release(b);
        assertTrue(claims.acquire(List.of(input, output), a, 201));
    }

    @Test void renewalAndWorldIsolationAreIndependent() {
        var a = UUID.randomUUID(); var b = UUID.randomUUID();
        var first = StorageClaims.forWorld(new Object());
        var second = StorageClaims.forWorld(new Object());
        assertTrue(first.acquire(List.of(BlockPos.ZERO), a, 0));
        assertTrue(first.acquire(List.of(BlockPos.ZERO), a, 190));
        assertFalse(first.available(BlockPos.ZERO, b, 201));
        assertTrue(second.acquire(List.of(BlockPos.ZERO), b, 201));
        assertTrue(first.available(BlockPos.ZERO, b, 390));
    }
}
