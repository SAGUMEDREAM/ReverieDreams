package cc.thonly.pak;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;
import java.nio.file.FileSystemNotFoundException;

/** Reads the defining class's container, never a global class-loader resource search. */
public final class ClassResourceSource {
    private final List<Path> roots;

    private ClassResourceSource(List<Path> roots) { this.roots = List.copyOf(roots); }

    public static ClassResourceSource of(Class<?> anchor) throws IOException {
        var domain = anchor.getProtectionDomain();
        var code = domain == null ? null : domain.getCodeSource();
        if (code == null || code.getLocation() == null) {
            throw new IOException("Class has no identifiable resource container: " + anchor.getName());
        }
        return fromLocation(code.getLocation());
    }

    static ClassResourceSource fromLocation(URL location) throws IOException {
        try {
            if (location.getProtocol().equals("jar")) {
                var connection = (JarURLConnection)location.openConnection();
                connection.setUseCaches(false);
                if (connection.getEntryName() != null && !connection.getEntryName().isEmpty()) {
                    throw new IOException("Nested class-container URLs are not supported");
                }
                return fromLocation(connection.getJarFileURL());
            }
            // Includes already mounted loader file-system providers, e.g. NeoForge union roots.
            Path root = Path.of(location.toURI()).toAbsolutePath().normalize();
            if (!Files.isDirectory(root) && !Files.isRegularFile(root)) {
                throw new IOException("Class resource container does not exist");
            }
            var roots = new ArrayList<Path>();
            roots.add(root);
            if (Files.isDirectory(root) && root.getFileSystem().equals(FileSystems.getDefault())) {
                // Gradle separates classes/java/<sourceSet> from resources/<sourceSet>.
                Path language = root.getParent();
                Path classes = language == null ? null : language.getParent();
                if (classes != null && classes.getFileName() != null
                        && classes.getFileName().toString().equals("classes")
                        && language.getFileName() != null
                        && language.getFileName().toString().equals("java")) {
                    Path companion = classes.getParent().resolve("resources").resolve(root.getFileName());
                    if (Files.isDirectory(companion)) roots.add(companion);
                }
            }
            return new ClassResourceSource(roots);
        } catch (URISyntaxException | IllegalArgumentException | FileSystemNotFoundException e) {
            throw new IOException("Unsupported class resource container protocol: " + location.getProtocol(), e);
        }
    }

    /** Missing resources return null; returned streams own and close any opened archive. */
    public InputStream open(String resource) throws IOException {
        validate(resource);
        for (Path root : roots) {
            if (Files.isDirectory(root)) {
                Path target = root.resolve(resource).normalize();
                if (!target.startsWith(root)) throw new IOException("Resource escaped its class container");
                if (Files.isRegularFile(target)) {
                    if (!target.toRealPath().startsWith(root.toRealPath())) {
                        throw new IOException("Resource link escaped its class container");
                    }
                    return Files.newInputStream(target);
                }
            } else {
                ZipFile archive = new ZipFile(root.toFile());
                var entry = archive.getEntry(resource);
                if (entry == null || entry.isDirectory()) { archive.close(); continue; }
                try {
                    return new FilterInputStream(archive.getInputStream(entry)) {
                        @Override public void close() throws IOException {
                            try { super.close(); } finally { archive.close(); }
                        }
                    };
                } catch (IOException | RuntimeException e) {
                    archive.close();
                    throw e;
                }
            }
        }
        return null;
    }

    private static void validate(String resource) throws IOException {
        if (resource == null || resource.isEmpty() || resource.length() > 2048
                || resource.startsWith("/") || resource.indexOf('\\') >= 0 || resource.indexOf(':') >= 0 || resource.indexOf(0) >= 0) {
            throw new IOException("Invalid container-relative resource path");
        }
        for (String segment : resource.split("/", -1)) {
            if (segment.isEmpty() || segment.equals(".") || segment.equals("..")) {
                throw new IOException("Invalid container-relative resource path segment");
            }
        }
    }
}
