package cc.thonly.pak;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

/** Client opt-in backend. Construction alone never loads native code. No Java key API. */
public final class NativePak implements PakDecoder {
    private final NativeLibrary library;

    public NativePak(ClassResourceSource container) { this(container, false); }

    // Package-private for synthetic JNI tests; production callers always reject fixture builds.
    NativePak(ClassResourceSource container, boolean allowTestFixtures) {
        library = new NativeLibrary(Objects.requireNonNull(container), allowTestFixtures);
    }

    @Override public byte[] decrypt(InputStream input, PakAsset expected) throws IOException {
        Objects.requireNonNull(expected);
        byte[] encoded = input.readNBytes(PakLimits.MAX_FILE_BYTES + 1);
        if (encoded.length > PakLimits.MAX_FILE_BYTES) throw new PakException("PAK input exceeds size limit");
        library.ensureLoaded();
        byte[] plaintext = null;
        try {
            plaintext = decryptNative(encoded, expected.owner(), expected.resource(), expected.keyId());
            if (plaintext == null) throw new PakException("Native PAK returned no authenticated data");
            PakPlaintext.check(plaintext, expected.resource());
            return plaintext;
        } catch (IOException | RuntimeException | LinkageError e) {
            if (plaintext != null) Arrays.fill(plaintext, (byte)0);
            if (e instanceof IOException io) throw io;
            throw new PakException("Native PAK decoding is unavailable", e);
        }
    }

    /** Seals one skin-cache payload with the native, machine-derived in-memory key. */
    public byte[] sealCache(byte[] plaintext, PakAsset asset, byte[] salt, byte[] nonce) throws IOException {
        Objects.requireNonNull(plaintext);
        Objects.requireNonNull(asset);
        requireCacheAsset(asset);
        if (salt == null || salt.length != 16 || nonce == null || nonce.length != 12) {
            throw new IllegalArgumentException("PAK cache requires a 16-byte salt and 12-byte nonce");
        }
        PakPlaintext.check(plaintext, asset.resource());
        library.ensureLoaded();
        byte[] encoded = null;
        try {
            encoded = sealCacheNative(plaintext, asset.resource(), salt, nonce);
            if (encoded == null || encoded.length > PakLimits.MAX_FILE_BYTES) {
                throw new PakException("Native PAK cache returned no sealed data");
            }
            return encoded;
        } catch (IOException | RuntimeException | LinkageError e) {
            if (encoded != null) Arrays.fill(encoded, (byte)0);
            if (e instanceof IOException io) throw io;
            throw new PakException("Native PAK cache encryption is unavailable", e);
        }
    }

    /** Opens one skin-cache payload. The key is derived and retained only in native memory. */
    public byte[] openCache(InputStream input, PakAsset expected) throws IOException {
        Objects.requireNonNull(input);
        Objects.requireNonNull(expected);
        requireCacheAsset(expected);
        byte[] encoded = input.readNBytes(PakLimits.MAX_FILE_BYTES + 1);
        if (encoded.length > PakLimits.MAX_FILE_BYTES) throw new PakException("PAK cache input exceeds size limit");
        library.ensureLoaded();
        byte[] plaintext = null;
        try {
            plaintext = openCacheNative(encoded, expected.resource());
            if (plaintext == null) throw new PakException("Native PAK cache returned no authenticated data");
            PakPlaintext.check(plaintext, expected.resource());
            return plaintext;
        } catch (IOException | RuntimeException | LinkageError e) {
            if (plaintext != null) Arrays.fill(plaintext, (byte)0);
            if (e instanceof IOException io) throw io;
            throw new PakException("Native PAK cache decoding is unavailable", e);
        }
    }

    private static void requireCacheAsset(PakAsset asset) {
        if (!asset.owner().equals("reverie_dreams") || !asset.keyId().equals("machine-v1")
                || !asset.resource().startsWith("reverie_dreams:skin-cache/")) {
            throw new IllegalArgumentException("Not a skin-cache PAK asset");
        }
    }

    static native int abiVersionNative();
    static native boolean fixtureModeNative();
    private static native byte[] decryptNative(byte[] encoded, String owner, String asset, String keyId) throws IOException;
    private static native byte[] sealCacheNative(byte[] plaintext, String asset, byte[] salt, byte[] nonce) throws IOException;
    private static native byte[] openCacheNative(byte[] encoded, String asset) throws IOException;
}
