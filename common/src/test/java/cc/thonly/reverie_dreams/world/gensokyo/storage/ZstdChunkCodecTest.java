package cc.thonly.reverie_dreams.world.gensokyo.storage;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.FilterInputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ZstdChunkCodecTest {
    @Test
    void preservesEmptyAndLargePayloadsAtAllSupportedLevelBoundaries() throws Exception {
        byte[] payload = new byte[2 * 1024 * 1024];
        new Random(42).nextBytes(payload);
        Arrays.fill(payload, 0, payload.length / 2, (byte) 0);
        for (int level : new int[]{-1, 1, 8, 19, 100}) {
            assertArrayEquals(payload, decode(encode(payload, level)));
            assertArrayEquals(new byte[0], decode(encode(new byte[0], level)));
        }
    }

    @Test
    void hasNamespacedHeaderAndStandardZstdFrame() throws Exception {
        try (DataInputStream input = new DataInputStream(new ByteArrayInputStream(encode(new byte[128], 8)))) {
            assertEquals(ZstdChunkCodec.ID, input.readUTF());
            assertEquals(0x28B52FFD, input.readInt());
        }
    }

    @Test
    void preservesUnknownCodecHeadersForOtherMods() throws Exception {
        String unknownId = "other_mod:" + "a".repeat(65525);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        new DataOutputStream(bytes).writeUTF(unknownId);
        bytes.write(new byte[]{1, 2, 3});
        try (DataInputStream input = ZstdChunkCodec.decodeCustom(new ByteArrayInputStream(bytes.toByteArray()), DataInputStream::new)) {
            assertEquals(unknownId, input.readUTF());
            assertArrayEquals(new byte[]{1, 2, 3}, input.readAllBytes());
        }
    }

    @Test
    void rejectsTruncatedFramesAndMalformedHeaders() throws Exception {
        byte[] payload = new byte[65536];
        new Random(19).nextBytes(payload);
        byte[] compressed = encode(payload, 8);
        assertThrows(IOException.class, () -> decode(Arrays.copyOf(compressed, compressed.length / 2)));
        assertThrows(IOException.class, () -> decode(new byte[]{0, 10, 1}));
    }

    @Test
    void rejectsChecksumCorruptionInsteadOfReturningMissing() throws Exception {
        byte[] compressed = encode(new byte[65536], 8);
        compressed[compressed.length - 1] ^= 0x55;
        assertThrows(IOException.class, () -> decode(compressed));
    }

    @Test
    void verifiesFrameTrailerEvenWhenNbtReaderStopsBeforeEof() throws Exception {
        byte[] payload = new byte[65536];
        byte[] compressed = encode(payload, 8);
        compressed[compressed.length - 1] ^= 0x55;
        assertThrows(IOException.class, () -> {
            // Limit reads so the checksum has not already been prefetched with the NBT bytes.
            var slowSource = new FilterInputStream(new ByteArrayInputStream(compressed)) {
                @Override
                public int read(byte[] buffer, int offset, int length) throws IOException {
                    return super.read(buffer, offset, Math.min(1, length));
                }
            };
            try (DataInputStream input = ZstdChunkCodec.decodeCustom(slowSource, DataInputStream::new)) {
                input.readNBytes(payload.length);
            }
        });
    }

    @Test
    void keepsConcurrentChunkStreamsIndependent() throws Exception {
        try (var pool = Executors.newFixedThreadPool(4)) {
            var tasks = IntStream.range(0, 24).mapToObj(index -> (Callable<Void>) () -> {
                byte[] payload = new byte[65536 + index];
                new Random(index).nextBytes(payload);
                assertArrayEquals(payload, decode(encode(payload, 8)));
                return null;
            }).toList();
            for (var result : pool.invokeAll(tasks)) {
                result.get();
            }
        }
    }

    private static byte[] encode(byte[] payload, int level) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (OutputStream output = ZstdChunkCodec.encode(bytes, level)) {
            output.write(payload);
        }
        return bytes.toByteArray();
    }

    private static byte[] decode(byte[] bytes) throws IOException {
        try (DataInputStream input = ZstdChunkCodec.decodeCustom(new ByteArrayInputStream(bytes), ignored -> {
            throw new IOException("Unexpected fallback");
        })) {
            return input.readAllBytes();
        }
    }
}
