package cc.thonly.pak;

import java.nio.ByteBuffer;
import java.util.Arrays;

/** Small bounded preflight; this deliberately does not implement PNG decoding. */
public record PngHeader(int width, int height) {
    private static final byte[] SIGNATURE = {(byte)137, 80, 78, 71, 13, 10, 26, 10};

    public static PngHeader check(byte[] png) throws PakException {
        if (png.length < 33 || png.length > PakLimits.MAX_PNG_BYTES
                || !Arrays.equals(SIGNATURE, Arrays.copyOf(png, 8))) {
            throw new PakException("Invalid or oversized PNG input");
        }
        ByteBuffer data = ByteBuffer.wrap(png);
        if (data.getInt(8) != 13 || data.getInt(12) != 0x49484452) {
            throw new PakException("PNG must start with an IHDR chunk");
        }
        int width = data.getInt(16), height = data.getInt(20);
        if (width < 1 || height < 1 || width > 8192 || height > 8192 || (long)width * height > 16777216L) {
            throw new PakException("PNG dimensions exceed PAK limits");
        }
        return new PngHeader(width, height);
    }
}
