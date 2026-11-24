package cc.thonly.pak;

import java.io.IOException;
import java.io.InputStream;

/** Returns an exclusively owned, fully authenticated PNG. The caller owns its lifetime. */
@FunctionalInterface
public interface PakDecoder {
    byte[] decrypt(InputStream input, PakAsset expected) throws IOException;
}
