package cc.thonly.reverie_dreams.client.pak;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class WipingPngInputStreamTest {
    @Test void readingToEndErasesOnlyTheOwnedArrayAndReleasesOnce() throws Exception {
        byte[] owned = {1, 2, 3}; var released = new AtomicInteger();
        var input = new WipingPngInputStream(owned, ignored -> released.incrementAndGet());
        assertArrayEquals(new byte[]{1, 2, 3}, input.readAllBytes());
        assertArrayEquals(new byte[3], owned);
        assertEquals(-1, input.read()); assertEquals(0, input.available());
        input.close(); input.close(); assertEquals(1, released.get());
        assertThrows(IOException.class, input::read);
    }

    @Test void closingAnUnreadStreamErasesIt() throws Exception {
        byte[] owned = {4, 5, 6};
        var input = new WipingPngInputStream(owned, ignored -> {});
        assertEquals(4, input.read()); input.close();
        assertArrayEquals(new byte[3], owned);
        assertThrows(IOException.class, () -> input.readAllBytes());
    }

    @Test void skipAndEmptyReadsRespectInputStreamContract() throws Exception {
        byte[] owned = {7, 8}; var input = new WipingPngInputStream(owned, ignored -> {});
        assertEquals(0, input.read(new byte[0])); assertEquals(0, input.skip(-1));
        assertEquals(2, input.skip(Long.MAX_VALUE)); assertArrayEquals(new byte[2], owned);
        assertEquals(0, input.read(new byte[0])); assertEquals(-1, input.read());
        assertEquals(0, input.skip(1)); input.close();
    }
}
