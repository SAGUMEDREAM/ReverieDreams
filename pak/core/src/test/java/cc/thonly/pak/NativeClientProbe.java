package cc.thonly.pak;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HexFormat;

/** Test-only child JVM entry point; its classpath intentionally has no author codec or key API. */
public final class NativeClientProbe {
    public static void main(String[] arguments) throws Exception {
        for (String name : new String[]{"PakCodec", "KeyRegistry", "KeyProvider", "KeyDerivation"}) {
            try {
                Class.forName("cc.thonly.pak." + name);
                throw new IllegalStateException("Author key/codec class leaked into the client runtime");
            } catch (ClassNotFoundException expected) { }
        }
        byte[] encoded = Base64.getDecoder().decode(System.in.readAllBytes());
        var nativeCode = new NativePak(ClassResourceSource.fromLocation(Path.of(arguments[0]).toUri().toURL()), true);
        byte[] png = nativeCode.decrypt(new ByteArrayInputStream(encoded),
                new PakAsset("pak_test", "pak_test:textures/test.png", "synthetic-v1"));
        System.out.print(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(png)));
    }
}
