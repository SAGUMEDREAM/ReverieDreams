package cc.thonly.pak;

import org.junit.jupiter.api.Test;

import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HexFormat;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import static org.junit.jupiter.api.Assertions.*;

class PakCodecTest {
    private final PakAsset asset = new PakAsset("example", "example:textures/skin/test.png", "slot-a");
    private final KeyProvider keys = (o, k) -> new SecretKeySpec(new byte[32], "AES");
    private static ByteArrayInputStream input(byte[] bytes) { return new ByteArrayInputStream(bytes); }
    private static byte[] png() throws Exception {
        var out = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB), "png", out));
        return out.toByteArray();
    }

    @Test void roundTripPreservesEveryOriginalByteWithoutSerializedIdentity() throws Exception {
        byte[] original = png();
        byte[] encrypted = PakCodec.encrypt(original, asset, keys);
        assertArrayEquals(original, PakCodec.decrypt(input(encrypted), asset, keys));
        PakHeader header = PakCodec.inspect(input(encrypted));
        assertEquals(original.length, header.plainLength());
        assertEquals(PakLimits.HEADER_BYTES, header.authenticatedBytes().length);
        byte[] headerBytes = Arrays.copyOf(encrypted, PakLimits.HEADER_BYTES);
        assertFalse(new String(headerBytes, java.nio.charset.StandardCharsets.US_ASCII).contains("test.png"));
        assertNotEquals(HexFormat.of().formatHex(original), HexFormat.of().formatHex(encrypted));
    }

    @Test void arbitraryOwnerScopedPathsRoundTrip() throws Exception {
        for (String path : new String[]{"example:models/item/test.json", "example:sounds/ambient/test.ogg",
                "example:lang/en_us.json", "example:pak/catalog.json"}) {
            PakAsset arbitrary = new PakAsset("example", path, "slot-a");
            byte[] original = path.endsWith(".png") ? png() : "{\"test\":true}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] encrypted = PakCodec.encrypt(original, arbitrary, keys);
            assertArrayEquals(original, PakCodec.decrypt(input(encrypted), arbitrary, keys));
        }
    }

    @Test void freshEncryptionChangesSaltNonceAndCiphertext() throws Exception {
        byte[] a = PakCodec.encrypt(png(), asset, keys), b = PakCodec.encrypt(png(), asset, keys);
        assertFalse(Arrays.equals(PakCodec.inspect(input(a)).salt(), PakCodec.inspect(input(b)).salt()));
        assertFalse(Arrays.equals(PakCodec.inspect(input(a)).nonce(), PakCodec.inspect(input(b)).nonce()));
        assertFalse(Arrays.equals(a, b));
    }

    @Test void wrongKeyAndMissingKeyAreRejected() throws Exception {
        byte[] data = PakCodec.encrypt(png(), asset, keys);
        byte[] wrong = new byte[32]; wrong[0] = 1;
        assertThrows(PakException.class, () -> PakCodec.decrypt(input(data), asset, (o, k) -> new SecretKeySpec(wrong, "AES")));
        assertThrows(PakException.class, () -> PakCodec.decrypt(input(data), asset, new KeyRegistry()));
    }

    @Test void ciphertextAndTagTamperingAreRejected() throws Exception {
        byte[] original = PakCodec.encrypt(png(), asset, keys);
        int start = PakCodec.inspect(input(original)).authenticatedBytes().length;
        for (int offset : new int[]{start, original.length - 1}) {
            byte[] changed = original.clone(); changed[offset] ^= 1;
            assertThrows(PakException.class, () -> PakCodec.decrypt(input(changed), asset, keys));
        }
    }

    @Test void headerSaltAndNonceAreAuthenticated() throws Exception {
        byte[] original = PakCodec.encrypt(png(), asset, keys);
        for (int offset : new int[]{0, 4, 5, 6, 16, 32}) {
            byte[] changed = original.clone(); changed[offset] ^= 1;
            assertThrows(PakException.class, () -> PakCodec.decrypt(input(changed), asset, keys));
        }
    }

    @Test void callerMustSupplyCorrectOwnerResourceAndKeyId() throws Exception {
        byte[] data = PakCodec.encrypt(png(), asset, keys);
        for (PakAsset wrong : new PakAsset[]{new PakAsset("other", "other:textures/skin/test.png", "slot-a"),
                new PakAsset("example", "example:textures/skin/copied.png", "slot-a"),
                new PakAsset("example", asset.resource(), "slot-b")}) {
            assertThrows(PakException.class, () -> PakCodec.decrypt(input(data), wrong, keys));
        }
    }

    @Test void everyTruncationAndTrailingBytesAreRejected() throws Exception {
        byte[] data = PakCodec.encrypt(png(), asset, keys);
        for (int length = 0; length < data.length; length++) {
            byte[] truncated = Arrays.copyOf(data, length);
            assertThrows(PakException.class, () -> PakCodec.decrypt(input(truncated), asset, keys));
        }
        assertThrows(PakException.class, () -> PakCodec.decrypt(input(Arrays.copyOf(data, data.length + 1)), asset, keys));
    }

    @Test void unknownFieldsAndUnsignedOversizedLengthsFailBeforePayloadAllocation() throws Exception {
        byte[] data = PakCodec.encrypt(png(), asset, keys);
        for (int offset : new int[]{0, 4, 5, 6}) {
            byte[] changed = data.clone(); changed[offset] ^= 8;
            assertThrows(PakException.class, () -> PakCodec.inspect(input(changed)));
        }
        for (int offset : new int[]{8, 12}) {
            byte[] changed = data.clone(); ByteBuffer.wrap(changed).putInt(offset, -1);
            assertThrows(PakException.class, () -> PakCodec.inspect(input(changed)));
        }
    }

    @Test void pngResourcesArePreflightedButOtherResourcesAreNotPng() throws Exception {
        assertThrows(PakException.class, () -> PakCodec.encrypt(new byte[128], asset, keys));
        byte[] giant = png(); ByteBuffer.wrap(giant).putInt(16, 8192).putInt(20, 8192);
        assertThrows(PakException.class, () -> PakCodec.encrypt(giant, asset, keys));
        PakAsset raw = new PakAsset("example", "example:config/test.bin", "slot-a");
        assertArrayEquals(new byte[128], PakCodec.decrypt(input(PakCodec.encrypt(new byte[128], raw, keys)), raw, keys));
    }

    @Test void registryRejectsShortAndDuplicateKeysAndCopiesInput() throws Exception {
        var registry = new KeyRegistry();
        assertThrows(IllegalArgumentException.class, () -> registry.register("example", "slot-a", new byte[16]));
        byte[] key = new byte[32]; registry.register("example", "slot-a", key); key[0] = 99;
        assertEquals(0, registry.findKey("example", "slot-a").getEncoded()[0]);
        assertThrows(IllegalArgumentException.class, () -> registry.register("example", "slot-a", key));
    }

    @Test void hkdfMatchesRfc5869TestCaseOneFirst32Bytes() throws Exception {
        byte[] ikm = new byte[22]; Arrays.fill(ikm, (byte)0x0b);
        var hex = HexFormat.of();
        byte[] derived = KeyDerivation.derive(new SecretKeySpec(ikm, "HmacSHA256"),
                hex.parseHex("000102030405060708090a0b0c"), hex.parseHex("f0f1f2f3f4f5f6f7f8f9")).getEncoded();
        assertEquals("3cb25f25faacd57a90434f64d0362f2a2d2d0a90cf1a5a4c5db02d56ecc4c5bf", hex.formatHex(derived));
    }

    @Test void identifiersRejectTraversalAndCrossNamespaceBinding() {
        for (String path : new String[]{"example:textures/../x.png", "example:textures//x.png", "other:textures/a.png",
                "example:textures/A.png", "example:", "example:./a.png", "example:a\\b.png"}) {
            assertThrows(IllegalArgumentException.class, () -> new PakAsset("example", path, "slot-a"));
        }
        for (String path : new String[]{"example:models/a.json", "example:sounds/a.ogg", "example:data/a/b/c.dat"}) {
            assertEquals(path, new PakAsset("example", path, "slot-a").resource());
        }
    }

    @Test void independentJceDecryptorRequiresTheExactSerializedHeaderAsAad() throws Exception {
        byte[] original = png(), encrypted = PakCodec.encrypt(original, asset, keys);
        PakHeader header = PakCodec.inspect(input(encrypted));
        byte[] aad = header.authenticatedBytes();
        byte[] payload = Arrays.copyOfRange(encrypted, aad.length, encrypted.length);
        var derived = KeyDerivation.derive(keys.findKey(asset.owner(), asset.keyId()), header.salt(), KeyDerivation.context(asset));
        var cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, derived, new GCMParameterSpec(128, header.nonce()));
        cipher.updateAAD(aad);
        assertArrayEquals(original, cipher.doFinal(payload));
        aad[6] ^= 1;
        cipher.init(Cipher.DECRYPT_MODE, derived, new GCMParameterSpec(128, header.nonce()));
        cipher.updateAAD(aad);
        assertThrows(AEADBadTagException.class, () -> cipher.doFinal(payload));
    }

    @Test void validationDoesNotMutateAProvidersOwnedKeyEncoding() throws Exception {
        byte[] owned = new byte[32]; Arrays.fill(owned, (byte)3);
        SecretKey key = new SecretKey() {
            public String getAlgorithm() { return "AES"; }
            public String getFormat() { return "RAW"; }
            public byte[] getEncoded() { return owned; }
        };
        byte[] encrypted = PakCodec.encrypt(png(), asset, (o, k) -> key);
        assertArrayEquals(png(), PakCodec.decrypt(input(encrypted), asset, (o, k) -> key));
        for (byte value : owned) assertEquals(3, value);
    }
}
