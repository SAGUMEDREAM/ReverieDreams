package cc.thonly.reverie_dreams.world.gensokyo.data;

import java.io.IOException;
import java.io.InputStream;

/**
 * Decodes into the retained array with only a small temporary input buffer.
 */
final class GensokyoLayerDecoder {
    private static final int BUFFER_SIZE = 8192;

    private GensokyoLayerDecoder() {
    }

    static short[] readShorts(InputStream input, int expected, int deltaWidth) throws IOException {
        short[] result = new short[expected];
        byte[] buffer = new byte[BUFFER_SIZE];
        int index = 0;
        while (index < expected) {
            int count = Math.min(buffer.length / Short.BYTES, expected - index);
            readExactly(input, buffer, count * Short.BYTES);
            for (int i = 0; i < count * Short.BYTES; i += Short.BYTES) {
                short value = (short) ((buffer[i] & 0xFF) | ((buffer[i + 1] & 0xFF) << 8));
                result[index] = deltaWidth > 0 && index >= deltaWidth
                        ? (short) (result[index - deltaWidth] + value) : value;
                index++;
            }
        }
        requireEnd(input);
        return result;
    }

    static byte[] readBytes(InputStream input, int expected, boolean palette4) throws IOException {
        byte[] result = new byte[expected];
        if (palette4) {
            readPalette4(input, expected, result, null);
        } else {
            readExactly(input, result, expected);
            requireEnd(input);
        }
        return result;
    }

    static short[] readSurfaces(InputStream input, int expected, boolean palette4) throws IOException {
        short[] result = new short[expected];
        if (palette4) {
            readPalette4(input, expected, null, result);
            return result;
        }
        // Legacy layers may use 8 or 16 bits, including inside gzip. Pack bytes in the final
        // array until EOF reveals their width, then expand 8-bit indices backwards in place.
        byte[] buffer = new byte[BUFFER_SIZE];
        long bytesRead = 0;
        long maximum = (long) expected * Short.BYTES;
        while (bytesRead < maximum) {
            int want = (int) Math.min(buffer.length, maximum - bytesRead);
            int count = input.readNBytes(buffer, 0, want);
            for (int i = 0; i < count; i++) {
                int index = (int) (bytesRead / Short.BYTES);
                int value = buffer[i] & 0xFF;
                if ((bytesRead & 1) == 0) {
                    result[index] = (short) value;
                } else {
                    result[index] = (short) (result[index] | (value << 8));
                }
                bytesRead++;
            }
            if (count < want)
                break;
        }
        requireEnd(input);
        if (bytesRead == maximum)
            return result;
        if (bytesRead == expected) {
            for (int index = expected - 1; index >= 0; index--) {
                result[index] = (short) ((result[index >> 1] >>> ((index & 1) * 8)) & 0xFF);
            }
            return result;
        }
        throw new IOException("Surface layer size mismatch: expected " + expected + " or " + maximum
                + " bytes, got " + bytesRead);
    }

    private static void readPalette4(InputStream input, int expected, byte[] bytes, short[] shorts) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int packedRemaining = expected / 2 + expected % 2;
        int index = 0;
        while (packedRemaining > 0) {
            int count = Math.min(buffer.length, packedRemaining);
            readExactly(input, buffer, count);
            for (int i = 0; i < count; i++) {
                int packed = buffer[i] & 0xFF;
                if (bytes != null) {
                    bytes[index++] = (byte) (packed >>> 4);
                    if (index < expected)
                        bytes[index++] = (byte) (packed & 0x0F);
                } else {
                    shorts[index++] = (short) (packed >>> 4);
                    if (index < expected)
                        shorts[index++] = (short) (packed & 0x0F);
                }
            }
            packedRemaining -= count;
        }
        requireEnd(input);
    }

    private static void readExactly(InputStream input, byte[] buffer, int count) throws IOException {
        int read = input.readNBytes(buffer, 0, count);
        if (read != count) {
            throw new IOException("Truncated map layer: expected " + count + " bytes, got " + read);
        }
    }

    private static void requireEnd(InputStream input) throws IOException {
        // Consume the gzip trailer too, checking its checksum and detecting truncation.
        if (input.read() != -1)
            throw new IOException("Map layer contains excess data");
    }
}
