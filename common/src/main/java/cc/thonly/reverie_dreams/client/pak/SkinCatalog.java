package cc.thonly.reverie_dreams.client.pak;

import cc.thonly.pak.PakDecoder;
import cc.thonly.pak.PakException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Decrypts the catalog once per consumer; the plaintext is never cached after parsing.
 */
public final class SkinCatalog {
    private SkinCatalog() {
    }

    public static SkinPackManifest load(PakSkinPack.Source source, PakDecoder decoder) throws IOException {
        byte[] encoded;
        try (InputStream in = source.open(SkinPackManifest.CATALOG)) {
            if (in == null)
                throw new PakException("Missing PAK catalog: " + SkinPackManifest.CATALOG);
            encoded = in.readNBytes(SkinPackManifest.MAX_CATALOG_BYTES + 1);
            if (encoded.length == 0 || encoded.length > SkinPackManifest.MAX_CATALOG_BYTES) {
                throw new PakException("PAK catalog exceeds size limit");
            }
        }
        byte[] plaintext = null;
        try {
            plaintext = decoder.decrypt(new ByteArrayInputStream(encoded), SkinPackManifest.CATALOG_ASSET);
            return SkinPackManifest.read(plaintext);
        } finally {
            Arrays.fill(encoded, (byte) 0);
            if (plaintext != null)
                Arrays.fill(plaintext, (byte) 0);
        }
    }
}
