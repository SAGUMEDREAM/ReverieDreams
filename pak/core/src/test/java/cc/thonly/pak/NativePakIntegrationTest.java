package cc.thonly.pak;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Base64;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

/** Actual JNI calls. Everything encrypted by this test is synthetic and stays in memory. */
@EnabledIfSystemProperty(named = "pak.test.native.resources", matches = ".+")
class NativePakIntegrationTest {
    private static final PakAsset ASSET = new PakAsset("pak_test", "pak_test:textures/test.png", "synthetic-v1");
    private static byte[] master() { byte[] bytes = new byte[32]; for (int i = 0; i < bytes.length; i++) bytes[i] = (byte)i; return bytes; }
    private static KeyRegistry keys(PakAsset asset, byte[] master) {
        var keys = new KeyRegistry(); keys.register(asset.owner(), asset.keyId(), master); return keys;
    }
    private static byte[] png() throws Exception {
        var image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, 0x12345678); image.setRGB(63, 63, 0xffaabbcc);
        var bytes = new ByteArrayOutputStream(); assertTrue(ImageIO.write(image, "png", bytes)); return bytes.toByteArray();
    }
    private static ByteArrayInputStream input(byte[] bytes) { return new ByteArrayInputStream(bytes); }
    private static NativePak backend() throws Exception {
        return new NativePak(ClassResourceSource.fromLocation(Path.of(System.getProperty("pak.test.native.resources")).toUri().toURL()), true);
    }
    private static byte[] encrypted() throws Exception { return PakCodec.encrypt(png(), ASSET, keys(ASSET, master())); }

    @Test void javaEncryptedPngIsRecoveredExactlyByNativeCode() throws Exception {
        byte[] original = png();
        byte[] encrypted = PakCodec.encrypt(original, ASSET, keys(ASSET, master()));
        byte[] actual = backend().decrypt(input(encrypted), ASSET);
        assertArrayEquals(original, actual);
        assertEquals(0x12345678, ImageIO.read(input(actual)).getRGB(0, 0));
        assertEquals(0xffaabbcc, ImageIO.read(input(actual)).getRGB(63, 63));
    }

    @Test void wrongKeyFailsAuthentication() throws Exception {
        byte[] wrong = master(); wrong[0] ^= 1;
        byte[] encrypted = PakCodec.encrypt(png(), ASSET, keys(ASSET, wrong));
        IOException error = assertThrows(IOException.class, () -> backend().decrypt(input(encrypted), ASSET));
        assertTrue(error.getMessage().contains("authenticate"));
    }

    @Test void authenticatesHeaderSaltNonceCiphertextAndTag() throws Exception {
        byte[] encoded = encrypted();
        PakHeader header = PakCodec.inspect(input(encoded));
        var nativeCode = backend();
        for (int offset : new int[]{16, 32, header.authenticatedBytes().length, encoded.length - 1}) {
            byte[] changed = encoded.clone(); changed[offset] ^= 1;
            IOException error = assertThrows(IOException.class, () -> nativeCode.decrypt(input(changed), ASSET));
            assertTrue(error.getMessage().contains("authenticate"));
        }
    }

    @Test void bindsCallerAssetAndNeverFallsBackToAnotherKey() throws Exception {
        var nativeCode = backend(); byte[] encoded = encrypted();
        assertThrows(IOException.class, () -> nativeCode.decrypt(input(encoded),
                new PakAsset(ASSET.owner(), "pak_test:textures/other.png", ASSET.keyId())));
        PakAsset missing = new PakAsset(ASSET.owner(), ASSET.resource(), "unprovisioned");
        byte[] other = PakCodec.encrypt(png(), missing, keys(missing, master()));
        IOException error = assertThrows(IOException.class, () -> nativeCode.decrypt(input(other), missing));
        assertTrue(error.getMessage().contains("No native PAK key slot"));
    }

    @Test void rejectsEveryTruncationAndTrailingBytes() throws Exception {
        byte[] encoded = encrypted(); var nativeCode = backend();
        for (int length = 0; length < encoded.length; length++) {
            byte[] truncated = Arrays.copyOf(encoded, length);
            assertThrows(IOException.class, () -> nativeCode.decrypt(input(truncated), ASSET));
        }
        assertThrows(IOException.class, () -> nativeCode.decrypt(input(Arrays.copyOf(encoded, encoded.length + 1)), ASSET));
    }

    @Test void rejectsUnsupportedAndOversizedHeaders() throws Exception {
        byte[] encoded = encrypted(); var nativeCode = backend();
        for (int offset : new int[]{0, 4, 5, 6, 16, 32}) {
            byte[] changed = encoded.clone(); changed[offset] = (byte)0xff;
            assertThrows(IOException.class, () -> nativeCode.decrypt(input(changed), ASSET));
        }
        for (int offset : new int[]{8, 12}) {
            byte[] changed = encoded.clone(); ByteBuffer.wrap(changed).putInt(offset, -1);
            assertThrows(IOException.class, () -> nativeCode.decrypt(input(changed), ASSET));
        }
        assertThrows(IOException.class, () -> nativeCode.decrypt(input(new byte[PakCodec.MAX_FILE_BYTES + 1]), ASSET));
    }

    @Test void rejectsAuthenticatedInvalidPngBeforeReturningAnything() throws Exception {
        byte[] encoded = encrypted(); PakHeader header = PakCodec.inspect(input(encoded));
        byte[] badPng = png(); ByteBuffer.wrap(badPng).putInt(16, 0);
        var key = KeyDerivation.derive(keys(ASSET, master()).findKey(ASSET.owner(), ASSET.keyId()), header.salt(), KeyDerivation.context(ASSET));
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, header.nonce()));
        cipher.updateAAD(header.authenticatedBytes());
        var output = new ByteArrayOutputStream(); output.write(header.authenticatedBytes()); output.write(cipher.doFinal(badPng));
        IOException error = assertThrows(IOException.class, () -> backend().decrypt(input(output.toByteArray()), ASSET));
        assertTrue(error.getMessage().contains("PNG"));
    }

    @Test void concurrentNativeCallsReturnIndependentImages() throws Exception {
        byte[] encoded = encrypted(), expected = png(); var nativeCode = backend();
        try (var pool = Executors.newFixedThreadPool(8)) {
            var work = new ArrayList<Callable<byte[]>>();
            for (int i = 0; i < 32; i++) work.add(() -> nativeCode.decrypt(input(encoded), ASSET));
            var images = pool.invokeAll(work);
            for (var image : images) assertArrayEquals(expected, image.get());
            images.getFirst().get()[0] = 0;
            assertArrayEquals(expected, images.get(1).get());
        }
    }

    @Test void nativeClientRunsWithoutTheJavaAuthorCodecOrRawKeyApis() throws Exception {
        String javaExecutable = Path.of(System.getProperty("java.home"), "bin", System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java").toString();
        String tests = Path.of(NativeClientProbe.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        String classpath = System.getProperty("pak.test.client.runtime.classes") + java.io.File.pathSeparator + tests;
        Process process = new ProcessBuilder(javaExecutable, "--enable-native-access=ALL-UNNAMED", "-Xcheck:jni",
                "-Djava.io.tmpdir=" + System.getProperty("java.io.tmpdir"), "-cp", classpath,
                NativeClientProbe.class.getName(), System.getProperty("pak.test.native.resources"))
                .redirectError(ProcessBuilder.Redirect.INHERIT).start();
        try {
            try (var input = process.getOutputStream()) { input.write(Base64.getEncoder().encode(encrypted())); }
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Native client probe did not exit");
            String actual = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.US_ASCII);
            assertEquals(0, process.exitValue());
            assertEquals(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(png())), actual);
        } finally {
            if (process.isAlive()) { process.destroyForcibly(); assertTrue(process.waitFor(10, TimeUnit.SECONDS)); }
        }
    }

    @Test void machineCacheRoundTripsAndRejectsTamperingAndOtherBindings() throws Exception {
        var nativeCode = backend();
        var asset = new PakAsset("reverie_dreams", "reverie_dreams:skin-cache/cache.json", "machine-v1");
        byte[] original = "{\"namespace\":\"reverie_dreams:skin-cache/v1\",\"entries\":{}}"
                .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] salt = new byte[16], nonce = new byte[12];
        Arrays.fill(salt, (byte)7); Arrays.fill(nonce, (byte)11);
        byte[] encoded = nativeCode.sealCache(original, asset, salt, nonce);
        assertArrayEquals(new byte[]{'P', 'A', 'K', '2'}, Arrays.copyOf(encoded, 4));
        assertArrayEquals(original, nativeCode.openCache(input(encoded), asset));
        assertThrows(IOException.class, () -> nativeCode.openCache(input(encoded),
                new PakAsset("reverie_dreams", "reverie_dreams:skin-cache/other.json", "machine-v1")));
        encoded[encoded.length - 1] ^= 1;
        assertThrows(IOException.class, () -> nativeCode.openCache(input(encoded), asset));
        assertThrows(IllegalArgumentException.class, () -> nativeCode.sealCache(original, ASSET, salt, nonce));
    }
}
