package cc.thonly.pak;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.concurrent.ConcurrentHashMap;

public final class KeyRegistry implements KeyProvider {
    private final ConcurrentHashMap<String, SecretKey> keys = new ConcurrentHashMap<>();

    public void register(String owner, String keyId, byte[] masterKey) {
        new PakAsset(owner, owner + ":pak/key-check.bin", keyId);
        if (masterKey.length != 32) throw new IllegalArgumentException("PAK requires a 256-bit master key");
        SecretKey key = new SecretKeySpec(masterKey, "AES");
        if (keys.putIfAbsent(owner + ":" + keyId, key) != null) {
            throw new IllegalArgumentException("PAK key ID is already registered");
        }
    }

    @Override
    public SecretKey findKey(String owner, String keyId) throws GeneralSecurityException {
        SecretKey key = keys.get(owner + ":" + keyId);
        if (key == null) throw new GeneralSecurityException("No key registered for PAK owner/key ID");
        return key;
    }
}
