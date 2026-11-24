package cc.thonly.reverie_dreams.world.gensokyo.data;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class GensokyoLayerDecoderTest {
    @Test
    void streamsAbsoluteAndDeltaShortsAcrossBufferAndRowBoundaries() throws Exception {
        int width = 127;
        short[] heights = new short[width * 99];
        Random random = new Random(52);
        for (int i = 0; i < heights.length; i++) heights[i] = (short) random.nextInt();
        short[] delta = heights.clone();
        for (int i = width; i < delta.length; i++) delta[i] = (short) (heights[i] - heights[i - width]);
        assertArrayEquals(heights, GensokyoLayerDecoder.readShorts(fragmented(shortBytes(heights)), heights.length, 0));
        try (InputStream input = gzipInput(shortBytes(delta))) {
            assertArrayEquals(heights, GensokyoLayerDecoder.readShorts(input, heights.length, width));
        }
    }

    @Test
    void unpacksPalette4DirectlyForBothOutputWidthsIncludingOddCellCounts() throws Exception {
        for (int count : new int[]{1, 2, 3, 16385}) {
            byte[] packed = new byte[(count + 1) / 2];
            new Random(count).nextBytes(packed);
            byte[] expectedBytes = new byte[count];
            short[] expectedShorts = new short[count];
            for (int i = 0; i < count; i++) {
                int value = ((packed[i >> 1] & 0xFF) >>> ((i & 1) == 0 ? 4 : 0)) & 15;
                expectedBytes[i] = (byte) value;
                expectedShorts[i] = (short) value;
            }
            assertArrayEquals(expectedBytes, GensokyoLayerDecoder.readBytes(fragmented(packed), count, true));
            try (InputStream input = gzipInput(packed)) {
                assertArrayEquals(expectedShorts, GensokyoLayerDecoder.readSurfaces(input, count, true));
            }
        }
    }

    @Test
    void retainsLegacyEightAndSixteenBitSurfaceFormats() throws Exception {
        for (int count : new int[]{1, 2, 3, 8193}) {
            byte[] bytes = new byte[count];
            new Random(count).nextBytes(bytes);
            short[] expected = new short[count];
            for (int i = 0; i < count; i++) expected[i] = (short) (bytes[i] & 0xFF);
            assertArrayEquals(expected, GensokyoLayerDecoder.readSurfaces(fragmented(bytes), count, false));
            try (InputStream input = gzipInput(bytes)) {
                assertArrayEquals(expected, GensokyoLayerDecoder.readSurfaces(input, count, false));
            }
            short[] wide = expected.clone();
            wide[count - 1] = (short) 60000;
            assertArrayEquals(wide, GensokyoLayerDecoder.readSurfaces(fragmented(shortBytes(wide)), count, false));
            assertArrayEquals(bytes, GensokyoLayerDecoder.readBytes(fragmented(bytes), count, false));
        }
    }

    @Test
    void rejectsTruncationExcessBytesAndBadGzipTrailers() throws Exception {
        assertThrows(IOException.class, () -> GensokyoLayerDecoder.readShorts(fragmented(new byte[5]), 3, 0));
        assertThrows(IOException.class, () -> GensokyoLayerDecoder.readShorts(fragmented(new byte[7]), 3, 0));
        assertThrows(IOException.class, () -> GensokyoLayerDecoder.readBytes(fragmented(new byte[1]), 3, true));
        assertThrows(IOException.class, () -> GensokyoLayerDecoder.readBytes(fragmented(new byte[3]), 3, true));
        assertThrows(IOException.class, () -> GensokyoLayerDecoder.readBytes(fragmented(new byte[4]), 3, false));
        assertThrows(IOException.class, () -> GensokyoLayerDecoder.readSurfaces(fragmented(new byte[4]), 3, false));
        assertThrows(IOException.class, () -> GensokyoLayerDecoder.readSurfaces(fragmented(new byte[7]), 3, false));
        byte[] compressed = gzip(new byte[6]);
        byte[] truncated = Arrays.copyOf(compressed, compressed.length - 3);
        assertThrows(IOException.class, () -> {
            try (InputStream input = new GZIPInputStream(new ByteArrayInputStream(truncated))) {
                GensokyoLayerDecoder.readShorts(input, 3, 0);
            }
        });
        compressed[compressed.length - 8] ^= 1;
        assertThrows(IOException.class, () -> {
            try (InputStream input = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
                GensokyoLayerDecoder.readShorts(input, 3, 0);
            }
        });
    }

    private static InputStream fragmented(byte[] data) {
        return new FilterInputStream(new ByteArrayInputStream(data)) {
            @Override public int read(byte[] buffer, int off, int len) throws IOException {
                return super.read(buffer, off, Math.min(len, 3));
            }
            @Override public int available() { return 0; }
        };
    }

    private static byte[] shortBytes(short[] values) {
        ByteBuffer buffer = ByteBuffer.allocate(values.length * 2).order(ByteOrder.LITTLE_ENDIAN);
        buffer.asShortBuffer().put(values);
        return buffer.array();
    }

    private static byte[] gzip(byte[] data) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(bytes)) { gzip.write(data); }
        return bytes.toByteArray();
    }

    private static InputStream gzipInput(byte[] data) throws IOException {
        return new GZIPInputStream(new ByteArrayInputStream(gzip(data)));
    }
}
