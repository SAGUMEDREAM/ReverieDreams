package cc.thonly.pak;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

/**
 * PAK v2, defined in pak/FORMAT.md. No file system or Minecraft dependency.
 * The serialized header carries only cryptographic parameters; the logical path and key slot
 * are supplied by the caller and authenticated through the derived key.
 */
public final class PakCodec {
    public static final int MAX_PNG_BYTES = PakLimits.MAX_PNG_BYTES;
    public static final int MAX_HEADER_BYTES = PakLimits.HEADER_BYTES;
    public static final int MAX_FILE_BYTES = PakLimits.MAX_FILE_BYTES;
    private static final int FIXED_HEADER = PakLimits.HEADER_BYTES;
    private PakCodec() {}

    public static byte[] encrypt(byte[] plaintext, PakAsset asset, KeyProvider keys) throws IOException {
        return encrypt(plaintext, asset, keys, new SecureRandom());
    }

    public static byte[] encrypt(byte[] plaintext, PakAsset asset, KeyProvider keys, SecureRandom random) throws IOException {
        PakPlaintext.check(plaintext, asset.resource());
        byte[] salt = new byte[16], nonce = new byte[12];
        random.nextBytes(salt);
        random.nextBytes(nonce);
        var bytes = new ByteArrayOutputStream();
        var out = new DataOutputStream(bytes);
        out.writeInt(0x50414b32);
        out.writeByte(2);
        out.writeByte(1);
        out.writeShort(0);
        out.writeInt(FIXED_HEADER);
        out.writeInt(plaintext.length + PakLimits.AUTH_TAG_BYTES);
        out.write(salt);
        out.write(nonce);
        byte[] header = bytes.toByteArray();
        try {
            Cipher cipher = cipher(Cipher.ENCRYPT_MODE, asset, salt, nonce, keys);
            cipher.updateAAD(header);
            out.write(cipher.doFinal(plaintext));
            return bytes.toByteArray();
        } catch (GeneralSecurityException e) {
            throw new PakException("Unable to encrypt PAK asset " + asset.resource(), e);
        }
    }

    /** Header data is unverified until decrypt succeeds. Input remains open. */
    public static PakHeader inspect(InputStream input) throws IOException {
        byte[] fixed = exact(input, FIXED_HEADER);
        var in = new DataInputStream(new ByteArrayInputStream(fixed));
        if (in.readInt() != 0x50414b32) throw new PakException("Invalid PAK magic");
        if (in.readUnsignedByte() != 2 || in.readUnsignedByte() != 1) throw new PakException("Unsupported PAK version/suite");
        if (in.readUnsignedShort() != 0) throw new PakException("Unsupported PAK flags");
        long headerSize = Integer.toUnsignedLong(in.readInt());
        long payloadSize = Integer.toUnsignedLong(in.readInt());
        if (headerSize != FIXED_HEADER
                || payloadSize < PakLimits.AUTH_TAG_BYTES + 1L
                || payloadSize > PakLimits.MAX_PLAINTEXT_BYTES + (long)PakLimits.AUTH_TAG_BYTES) {
            throw new PakException("Invalid PAK lengths");
        }
        byte[] salt = in.readNBytes(16), nonce = in.readNBytes(12);
        return new PakHeader((int)payloadSize - PakLimits.AUTH_TAG_BYTES, salt, nonce, fixed);
    }

    public static byte[] decrypt(InputStream input, PakAsset expected, KeyProvider keys) throws IOException {
        PakHeader header = inspect(input);
        byte[] payload = exact(input, header.plainLength() + PakLimits.AUTH_TAG_BYTES);
        if (input.read() != -1) throw new PakException("Trailing data after PAK payload");
        try {
            Cipher cipher = cipher(Cipher.DECRYPT_MODE, expected, header.salt(), header.nonce(), keys);
            cipher.updateAAD(header.authenticatedBytes());
            byte[] plaintext = cipher.doFinal(payload);
            PakPlaintext.check(plaintext, expected.resource());
            return plaintext;
        } catch (GeneralSecurityException e) {
            throw new PakException("Unable to authenticate PAK asset " + expected.resource(), e);
        }
    }

    private static Cipher cipher(int mode, PakAsset asset, byte[] salt, byte[] nonce, KeyProvider keys)
            throws GeneralSecurityException, IOException {
        SecretKey master = keys.findKey(asset.owner(), asset.keyId());
        byte[] material = master.getEncoded();
        if (material == null || material.length != 32) throw new GeneralSecurityException("PAK requires a 256-bit key slot");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(mode, KeyDerivation.derive(master, salt, KeyDerivation.context(asset)), new GCMParameterSpec(128, nonce));
        return cipher;
    }

    private static byte[] exact(InputStream input, int count) throws IOException {
        byte[] bytes = input.readNBytes(count);
        if (bytes.length != count) throw new PakException("Truncated PAK file");
        return bytes;
    }
}
