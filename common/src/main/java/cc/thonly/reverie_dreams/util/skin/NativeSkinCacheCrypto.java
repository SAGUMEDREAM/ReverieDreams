package cc.thonly.reverie_dreams.util.skin;

import cc.thonly.pak.ClassResourceSource;
import cc.thonly.pak.NativePak;
import cc.thonly.pak.PakAsset;
import cc.thonly.reverie_dreams.client.pak.PakSkinPacks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.SecureRandom;

/**
 * Native cache crypto. The machine-derived key stays inside the native process.
 */
final class NativeSkinCacheCrypto implements SkinCacheStore.Crypto {
    static final PakAsset ASSET = new PakAsset("reverie_dreams", "reverie_dreams:skin-cache/cache.json", "machine-v1");
    private static final SecureRandom RANDOM = new SecureRandom();
    private volatile NativePak backend;

    @Override
    public byte[] seal(byte[] plaintext) throws IOException {
        byte[] salt = new byte[16], nonce = new byte[12];
        RANDOM.nextBytes(salt);
        RANDOM.nextBytes(nonce);
        return backend().sealCache(plaintext, ASSET, salt, nonce);
    }

    @Override
    public byte[] open(byte[] encoded) throws IOException {
        return backend().openCache(new ByteArrayInputStream(encoded), ASSET);
    }

    private NativePak backend() throws IOException {
        NativePak current = backend;
        if (current != null)
            return current;
        synchronized (this) {
            if (backend == null)
                backend = new NativePak(ClassResourceSource.of(PakSkinPacks.class));
            return backend;
        }
    }
}
