package cc.thonly.pak;

import javax.crypto.KDF;
import javax.crypto.SecretKey;
import javax.crypto.spec.HKDFParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

public final class KeyDerivation {
    private KeyDerivation() {}

    public static SecretKey derive(SecretKey master, byte[] salt, byte[] info) throws GeneralSecurityException {
        byte[] encoded = master.getEncoded();
        if (encoded == null) throw new GeneralSecurityException("PAK master key must have a RAW encoding");
        // HKDF providers may erase temporary encodings. Never lend them provider-owned data.
        SecretKey snapshot = new SecretKeySpec(encoded, "HmacSHA256");
        return KDF.getInstance("HKDF-SHA256").deriveKey("AES",
                HKDFParameterSpec.ofExtract().addIKM(snapshot).addSalt(salt).thenExpand(info, 32));
    }

    static byte[] context(PakAsset asset) throws IOException {
        var bytes = new ByteArrayOutputStream();
        var out = new DataOutputStream(bytes);
        out.write("PAK2/v2/AES-256-GCM".getBytes(StandardCharsets.US_ASCII));
        for (String field : new String[]{asset.owner(), asset.resource(), asset.keyId()}) {
            byte[] text = field.getBytes(StandardCharsets.US_ASCII);
            out.writeShort(text.length);
            out.write(text);
        }
        return bytes.toByteArray();
    }
}
