package cc.thonly.reverie_dreams.client.pak;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Owns one decoded PNG; reading to the end or closing erases its backing array.
 */
final class WipingPngInputStream extends InputStream {
    private byte[] data;
    private int position;
    private boolean closed;
    private final Consumer<WipingPngInputStream> onReleased;

    WipingPngInputStream(byte[] owned, Consumer<WipingPngInputStream> onReleased) {
        data = Objects.requireNonNull(owned);
        this.onReleased = Objects.requireNonNull(onReleased);
    }

    @Override
    public synchronized int read() throws IOException {
        checkOpen();
        if (data == null)
            return -1;
        if (position == data.length) {
            release();
            return -1;
        }
        int result = data[position++] & 255;
        if (position == data.length)
            release();
        return result;
    }

    @Override
    public synchronized int read(byte[] target, int offset, int length) throws IOException {
        Objects.checkFromIndexSize(offset, length, target.length);
        checkOpen();
        if (length == 0)
            return 0;
        if (data == null)
            return -1;
        int count = Math.min(length, data.length - position);
        if (count == 0) {
            release();
            return -1;
        }
        System.arraycopy(data, position, target, offset, count);
        position += count;
        if (position == data.length)
            release();
        return count;
    }

    @Override
    public synchronized long skip(long count) throws IOException {
        checkOpen();
        if (data == null)
            return 0;
        int skipped = (int) Math.min(Math.max(0, count), data.length - position);
        position += skipped;
        if (position == data.length)
            release();
        return skipped;
    }

    @Override
    public synchronized int available() throws IOException {
        checkOpen();
        return data == null ? 0 : data.length - position;
    }

    @Override
    public synchronized void close() {
        closed = true;
        release();
    }

    private void checkOpen() throws IOException {
        if (closed)
            throw new IOException("PAK image stream is closed");
    }

    private void release() {
        if (data != null) {
            Arrays.fill(data, (byte) 0);
            data = null;
            onReleased.accept(this);
        }
    }
}
