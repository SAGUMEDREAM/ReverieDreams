package cc.thonly.pak;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;

/** Loads only a bounded, digest-checked library from the defining mod's container. */
final class NativeLibrary {
    static final int MAX_LIBRARY_BYTES = 32 * 1024 * 1024;
    private static final Object LOAD_LOCK = new Object();
    private static String linkedDigest;
    private final ClassResourceSource container;
    private final boolean allowTestFixtures;
    private volatile boolean ready;

    NativeLibrary(ClassResourceSource container, boolean allowTestFixtures) {
        this.container = container;
        this.allowTestFixtures = allowTestFixtures;
    }

    void ensureLoaded() throws IOException {
        if (ready) return;
        NativePlatform platform = NativePlatform.current();
        Descriptor descriptor;
        try (InputStream input = container.open(platform.resourceRoot() + "library.properties")) {
            descriptor = Descriptor.read(input, platform);
        }
        if (descriptor.fixture() && !allowTestFixtures) throw new IOException("Synthetic PAK native builds cannot be used by the client");
        synchronized (LOAD_LOCK) {
            if (linkedDigest != null && !linkedDigest.equals(descriptor.sha256())) {
                throw new IOException("A different PAK native library is already linked; restart the JVM after changing builds");
            }
            try {
                if (linkedDigest == null) {
                    Path library = extract(platform, descriptor);
                    System.load(library.toString());
                    linkedDigest = descriptor.sha256();
                }
                if (NativePak.abiVersionNative() != 3 || NativePak.fixtureModeNative() != descriptor.fixture()) {
                    throw new IOException("PAK native ABI or build profile mismatch");
                }
            } catch (LinkageError | RuntimeException e) {
                throw new IOException("Cannot load PAK native code for " + platform.id()
                        + "; check the library, JVM architecture and native-access configuration", e);
            }
            ready = true;
        }
    }

    private Path extract(NativePlatform platform, Descriptor descriptor) throws IOException {
        // The OS requires a native binary on disk. PNGs and raw keys are never extracted here.
        Path temporary = Path.of(System.getProperty("java.io.tmpdir")).toRealPath();
        Path cache = childDirectory(temporary, "reverie-dreams-pak-native");
        Path directory = childDirectory(cache, platform.id() + "-" + descriptor.sha256());
        Path destination = directory.resolve(platform.library());
        try (FileChannel channel = FileChannel.open(directory.resolve("extract.lock"),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS);
             var ignored = channel.lock()) {
            if (Files.exists(destination, LinkOption.NOFOLLOW_LINKS)) {
                if (!Files.isRegularFile(destination, LinkOption.NOFOLLOW_LINKS)
                        || !hash(destination).equals(descriptor.sha256())) throw new IOException("PAK native cache verification failed");
                return destination;
            }
            Path partial = Files.createTempFile(directory, "library-", ".part");
            try {
                try (InputStream input = container.open(platform.resourceRoot() + platform.library())) {
                    if (input == null) throw new IOException("Missing bundled PAK library for " + platform.id());
                    byte[] binary = input.readNBytes(MAX_LIBRARY_BYTES + 1);
                    if (binary.length == 0 || binary.length > MAX_LIBRARY_BYTES
                            || !hash(binary).equals(descriptor.sha256())) throw new IOException("Bundled PAK native library failed verification");
                    Files.write(partial, binary, StandardOpenOption.TRUNCATE_EXISTING);
                }
                try { Files.move(partial, destination, StandardCopyOption.ATOMIC_MOVE); }
                catch (java.nio.file.AtomicMoveNotSupportedException e) { Files.move(partial, destination); }
                return destination;
            } finally { Files.deleteIfExists(partial); }
        }
    }

    private static Path childDirectory(Path parent, String name) throws IOException {
        Path path = parent.resolve(name);
        Files.createDirectories(path);
        if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS) || !path.toRealPath().equals(path)) {
            throw new IOException("PAK native cache directory escaped its owner");
        }
        return path;
    }

    private static MessageDigest sha256() {
        try { return MessageDigest.getInstance("SHA-256"); }
        catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }

    static String hash(byte[] bytes) { return HexFormat.of().formatHex(sha256().digest(bytes)); }
    private static String hash(Path path) throws IOException {
        if (Files.size(path) > MAX_LIBRARY_BYTES) throw new IOException("Oversized PAK native cache entry");
        MessageDigest digest = sha256();
        try (InputStream input = Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS)) {
            byte[] buffer = new byte[32768];
            for (int read; (read = input.read(buffer)) != -1;) digest.update(buffer, 0, read);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    record Descriptor(String sha256, boolean fixture) {
        static Descriptor read(InputStream input, NativePlatform platform) throws IOException {
            if (input == null) throw new IOException("No PAK native library is provisioned for " + platform.id());
            byte[] bytes = input.readNBytes(4097);
            if (bytes.length > 4096) throw new IOException("Oversized PAK native descriptor");
            for (byte value : bytes) if (value < 0 || value == 0) throw new IOException("Invalid PAK native descriptor encoding");
            Map<String, String> fields = new HashMap<>();
            for (String raw : new String(bytes, java.nio.charset.StandardCharsets.US_ASCII).split("\n")) {
                String line = raw.strip();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int equals = line.indexOf('=');
                if (equals < 1 || fields.putIfAbsent(line.substring(0, equals), line.substring(equals + 1)) != null) {
                    throw new IOException("Malformed or duplicate PAK native descriptor field");
                }
            }
            if (!fields.keySet().equals(Set.of("abi", "file", "sha256", "fixture")) || !"3".equals(fields.get("abi"))
                    || !platform.library().equals(fields.get("file")) || !fields.get("sha256").matches("[0-9a-f]{64}")
                    || !(fields.get("fixture").equals("true") || fields.get("fixture").equals("false"))) {
                throw new IOException("Unsupported PAK native descriptor");
            }
            return new Descriptor(fields.get("sha256"), Boolean.parseBoolean(fields.get("fixture")));
        }
    }
}
