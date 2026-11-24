package cc.thonly.pak;

import java.io.IOException;

/** Shared size and PNG preflight for authenticated plaintext; client-runtime safe. */
public final class PakPlaintext {
    private PakPlaintext() {}

    public static void check(byte[] plaintext, String resource) throws IOException {
        if (plaintext.length < 1 || plaintext.length > PakLimits.MAX_PLAINTEXT_BYTES) {
            throw new PakException("Invalid or oversized PAK plaintext");
        }
        if (resource.endsWith(".png")) PngHeader.check(plaintext);
    }
}
