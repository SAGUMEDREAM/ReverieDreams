package cc.thonly.pak;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class NativeEmptyKeyTest {
    @Test void productionProfileLoadsButHasNoDefaultOrSyntheticKey() throws Exception {
        byte[] png = new byte[33];
        ByteBuffer.wrap(png).put(new byte[]{(byte)137, 80, 78, 71, 13, 10, 26, 10})
                .putInt(13).putInt(0x49484452).putInt(64).putInt(64);
        var asset = new PakAsset("pak_test", "pak_test:textures/test.png", "synthetic-v1");
        var keys = new KeyRegistry(); byte[] master = new byte[32];
        for (int i = 0; i < master.length; i++) master[i] = (byte)i;
        keys.register(asset.owner(), asset.keyId(), master);
        byte[] encoded = PakCodec.encrypt(png, asset, keys);
        var source = ClassResourceSource.fromLocation(Path.of(System.getProperty("pak.test.native.empty.resources")).toUri().toURL());
        var nativeCode = new NativePak(source);
        IOException error = assertThrows(IOException.class, () -> nativeCode.decrypt(new ByteArrayInputStream(encoded), asset));
        assertTrue(error.getMessage().contains("No native PAK key slot"), error.toString());
    }

    @Test void cacheUsesTheMachineKeyEvenWhenTheProductionBuildHasZeroKeySlots() throws Exception {
        byte[] plaintext = "{\"namespace\":\"reverie_dreams:skin-cache/v1\",\"version\":1,\"entries\":{}}"
                .getBytes(StandardCharsets.UTF_8);
        var asset = new PakAsset("reverie_dreams", "reverie_dreams:skin-cache/cache.json", "machine-v1");
        byte[] salt = new byte[16], nonce = new byte[12];
        for (int i = 0; i < salt.length; i++) salt[i] = (byte)i;
        for (int i = 0; i < nonce.length; i++) nonce[i] = (byte)(i + 16);
        var source = ClassResourceSource.fromLocation(Path.of(System.getProperty("pak.test.native.empty.resources")).toUri().toURL());
        var nativeCode = new NativePak(source);
        byte[] sealed = nativeCode.sealCache(plaintext, asset, salt, nonce);
        assertArrayEquals(plaintext, nativeCode.openCache(new ByteArrayInputStream(sealed), asset));
    }
}
