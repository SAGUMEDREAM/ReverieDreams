package cc.thonly.reverie_dreams.world.gensokyo.storage;

import cc.thonly.reverie_dreams.ReverieDreams;

import com.github.luben.zstd.RecyclingBufferPool;
import com.github.luben.zstd.ZstdInputStreamNoFinalizer;
import com.github.luben.zstd.ZstdOutputStreamNoFinalizer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A namespaced custom Anvil codec. It stores the complete, unchanged chunk NBT.
 */
public final class ZstdChunkCodec {
    public static final String ID = ReverieDreams.MOD_ID + ":zstd_v1";
    // DataInput.readUTF permits up to 65535 encoded bytes, plus its two-byte length.
    private static final int MAX_CUSTOM_HEADER = 65537;

    private ZstdChunkCodec() {
    }

    public static OutputStream encode(OutputStream destination, int level) throws IOException {
        // Construct the native codec before writing anything to the destination.
        ZstdOutputStreamNoFinalizer compressed = new ZstdOutputStreamNoFinalizer(
                destination, RecyclingBufferPool.INSTANCE, Math.clamp(level, 1, 19));
        try {
            compressed.setChecksum(true);
            new DataOutputStream(destination).writeUTF(ID);
            return compressed;
        } catch (IOException | RuntimeException | Error failure) {
            // Do not close destination here: closing a RegionFile.ChunkBuffer commits it.
            try {
                compressed.closeWithoutClosingParentStream();
            } catch (IOException closeFailure) {
                failure.addSuppressed(closeFailure);
            }
            throw failure;
        }
    }

    /**
     * Unknown custom codecs are delegated with their original header intact.
     */
    public static DataInputStream decodeCustom(InputStream source, FallbackReader fallback) throws IOException {
        BufferedInputStream buffered = new BufferedInputStream(source);
        buffered.mark(MAX_CUSTOM_HEADER);
        try {
            String id = new DataInputStream(buffered).readUTF();
            if (!ID.equals(id)) {
                buffered.reset();
                DataInputStream delegated = fallback.read(buffered);
                if (delegated == null) {
                    buffered.close();
                }
                return delegated;
            }
            // IOException propagates; a corrupt chunk must never be returned as missing (null).
            return new DataInputStream(new CheckedFrameInputStream(
                    new ZstdInputStreamNoFinalizer(buffered, RecyclingBufferPool.INSTANCE)));
        } catch (IOException | RuntimeException | Error failure) {
            try {
                buffered.close();
            } catch (IOException closeFailure) {
                failure.addSuppressed(closeFailure);
            }
            throw failure;
        }
    }

    @FunctionalInterface
    public interface FallbackReader {
        DataInputStream read(InputStream source) throws IOException;
    }

    private static final class CheckedFrameInputStream extends FilterInputStream {
        private boolean closed;

        private CheckedFrameInputStream(InputStream stream) {
            super(stream);
        }

        @Override
        public void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                // NBT readers and partial scanners can stop before reading the frame trailer.
                // Consume it before closing so a bad checksum/truncated frame is not accepted.
                try (InputStream remaining = this.in) {
                    remaining.transferTo(OutputStream.nullOutputStream());
                }
            }
        }
    }
}
