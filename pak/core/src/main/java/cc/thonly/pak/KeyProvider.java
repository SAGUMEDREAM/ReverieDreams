package cc.thonly.pak;

import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;

@FunctionalInterface
public interface KeyProvider {
    /** Returns the owner's 256-bit master key; never obtains keys from asset data. */
    SecretKey findKey(String owner, String keyId) throws GeneralSecurityException;
}
