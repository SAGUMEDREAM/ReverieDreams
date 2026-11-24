package cc.thonly.pak;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class ClassResourceSourceTest {
    private static final String RESOURCE = "META-INF/test-owner/scope.list";
    private static final String CLASS = "cc/thonly/pak/OriginFixture.class";
    @TempDir Path temp;

    private static byte[] fixtureClass() throws IOException {
        try (InputStream in = OriginFixture.class.getResourceAsStream("OriginFixture.class")) {
            assertNotNull(in); return in.readAllBytes();
        }
    }
    private Path jar(String name, boolean withClass, String text) throws IOException {
        Path target = temp.resolve(name);
        try (var out = new ZipOutputStream(Files.newOutputStream(target))) {
            if (withClass) { out.putNextEntry(new ZipEntry(CLASS)); out.write(fixtureClass()); out.closeEntry(); }
            if (text != null) { out.putNextEntry(new ZipEntry(RESOURCE)); out.write(text.getBytes(StandardCharsets.UTF_8)); out.closeEntry(); }
        }
        return target;
    }
    private static String read(InputStream stream) throws IOException {
        assertNotNull(stream);
        try (stream) { return new String(stream.readAllBytes(), StandardCharsets.UTF_8); }
    }

    @Test void owningJarWinsEvenWhenAnotherJarShadowsTheGlobalResourceName() throws Exception {
        Path foreign = jar("foreign.jar", false, "foreign"), owner = jar("owner skin #1.jar", true, "owner");
        try (var loader = new URLClassLoader(new URL[]{foreign.toUri().toURL(), owner.toUri().toURL()}, null)) {
            Class<?> anchor = loader.loadClass("cc.thonly.pak.OriginFixture");
            assertEquals("foreign", read(loader.getResourceAsStream(RESOURCE)));
            assertEquals("owner", read(ClassResourceSource.of(anchor).open(RESOURCE)));
        }
    }

    @Test void missingOwningJarEntryNeverFallsBackToAnotherJar() throws Exception {
        Path foreign = jar("foreign.jar", false, "foreign"), owner = jar("owner.jar", true, null);
        try (var loader = new URLClassLoader(new URL[]{foreign.toUri().toURL(), owner.toUri().toURL()}, null)) {
            assertNull(ClassResourceSource.of(loader.loadClass("cc.thonly.pak.OriginFixture")).open(RESOURCE));
        }
    }

    @Test void gradleDevelopmentClassesReadOnlyTheirOwnCompanionResources() throws Exception {
        Path build = temp.resolve("mod with spaces/build");
        Path classes = build.resolve("classes/java/main"), resources = build.resolve("resources/main");
        Files.createDirectories(classes.resolve(CLASS).getParent());
        Files.write(classes.resolve(CLASS), fixtureClass());
        Files.createDirectories(resources.resolve(RESOURCE).getParent());
        Files.writeString(resources.resolve(RESOURCE), "same-module");
        Path foreign = jar("foreign.jar", false, "foreign");
        try (var loader = new URLClassLoader(new URL[]{foreign.toUri().toURL(), classes.toUri().toURL()}, null)) {
            var source = ClassResourceSource.of(loader.loadClass("cc.thonly.pak.OriginFixture"));
            assertEquals("same-module", read(source.open(RESOURCE)));
            assertNull(source.open("META-INF/missing.list"));
        }
    }

    @Test void normalDirectoryAndMountedArchiveDirectoriesAreSupported() throws Exception {
        Files.createDirectories(temp.resolve(RESOURCE).getParent());
        Files.writeString(temp.resolve(RESOURCE), "directory");
        assertEquals("directory", read(ClassResourceSource.fromLocation(temp.toUri().toURL()).open(RESOURCE)));
        Path owner = jar("mounted.jar", false, "mounted");
        try (var fs = FileSystems.newFileSystem(owner)) {
            URL root = fs.getPath("/").toUri().toURL();
            assertEquals("mounted", read(ClassResourceSource.fromLocation(root).open(RESOURCE)));
        }
    }

    @Test void returnedJarStreamClosesItsArchiveAndSupportsRenamingAfterUse() throws Exception {
        Path owner = jar("closeable.jar", true, "owner");
        Class<?> anchor;
        try (var loader = new URLClassLoader(new URL[]{owner.toUri().toURL()}, null)) {
            anchor = loader.loadClass("cc.thonly.pak.OriginFixture");
        }
        var source = ClassResourceSource.of(anchor);
        InputStream in = source.open(RESOURCE);
        assertEquals("owner", read(in));
        assertThrows(IOException.class, in::read);
        assertTrue(Files.exists(Files.move(owner, temp.resolve("renamed.jar"))));
    }

    @Test void pathsCannotEscapeTheContainer() throws Exception {
        var source = ClassResourceSource.fromLocation(temp.toUri().toURL());
        for (String invalid : new String[]{"", "../secret", "/absolute", "a/../b", "a//b", "a\\b", "file:secret", "a\0b"}) {
            assertThrows(IOException.class, () -> source.open(invalid), invalid);
        }
    }

    @Test void unknownOriginsFailInsteadOfSearchingTheClasspath() throws Exception {
        assertThrows(IOException.class, () -> ClassResourceSource.of(String.class));
        assertThrows(IOException.class, () -> ClassResourceSource.fromLocation(new URL("https://example.invalid/mod.jar")));
    }
}
