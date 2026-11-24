package cc.thonly.reverie_dreams.client.audio.radio;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Some radio servers insert ICY metadata even when Icy-MetaData: 0 was requested.
 */
final class IcyInputStream extends FilterInputStream {
    private final int interval;
    private int remaining;

    IcyInputStream(InputStream input, int interval) {
        super(input);
        this.interval = interval;
        remaining = interval;
    }

    @Override
    public int read() throws IOException {
        byte[] one = new byte[1];
        return read(one, 0, 1) < 0 ? -1 : one[0] & 255;
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        if (length == 0)
            return 0;
        if (interval <= 0)
            return in.read(bytes, offset, length);
        if (remaining == 0) {
            int size = in.read();
            if (size < 0)
                return -1;
            for (int n = size * 16; n > 0; ) {
                long skipped = in.skip(n);
                if (skipped == 0) {
                    if (in.read() < 0)
                        throw new IOException("Truncated ICY metadata");
                    skipped = 1;
                }
                n -= (int) skipped;
            }
            remaining = interval;
        }
        int count = in.read(bytes, offset, Math.min(length, remaining));
        if (count > 0)
            remaining -= count;
        return count;
    }
}
