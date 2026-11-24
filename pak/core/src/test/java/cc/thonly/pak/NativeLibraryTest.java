package cc.thonly.pak;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class NativeLibraryTest {
    @TempDir Path directory;
    private static ByteArrayInputStream text(String value) { return new ByteArrayInputStream(value.getBytes(StandardCharsets.US_ASCII)); }
    private static String descriptor(NativePlatform platform, boolean fixture) {
        return "abi=3\nfile=" + platform.library() + "\nsha256=" + "0".repeat(64) + "\nfixture=" + fixture + "\n";
    }

    @Test void selectsEightTargetsUsingJvmArchitectureIncludingDarwinAndAndroid() throws Exception {
        assertEquals(NativePlatform.WINDOWS_X86_64, NativePlatform.detect("Windows 11", "amd64"));
        assertEquals(NativePlatform.WINDOWS_ARM64, NativePlatform.detect("Windows 11", "aarch64"));
        assertEquals(NativePlatform.LINUX_X86_64, NativePlatform.detect("Linux", "x86_64"));
        assertEquals(NativePlatform.LINUX_ARM64, NativePlatform.detect("Linux", "arm64"));
        assertEquals(NativePlatform.MACOS_X86_64, NativePlatform.detect("Mac OS X", "x86_64"));
        assertEquals(NativePlatform.MACOS_ARM64, NativePlatform.detect("Darwin", "aarch64"));
        for (String arch : new String[]{"x86", "i386", "arm", "", "riscv64"}) assertThrows(IOException.class, () -> NativePlatform.detect("Linux", arch));
        assertEquals(NativePlatform.ANDROID_ARM64, NativePlatform.detect("Android", "aarch64"));
        assertEquals(NativePlatform.ANDROID_X86_64, NativePlatform.detect("Android", "amd64"));
    }

    @Test void descriptorRejectsUnknownDuplicateTraversalAndOversizedFields() throws Exception {
        NativePlatform platform = NativePlatform.WINDOWS_X86_64;
        String valid = descriptor(platform, false);
        assertFalse(NativeLibrary.Descriptor.read(text(valid), platform).fixture());
        for (String bad : new String[]{valid + "abi=3\n", valid + "extra=1\n", valid.replace("abi=3", "abi=1"),
                valid.replace("pak_native.dll", "../pak_native.dll"), valid.replace("fixture=false", "fixture=0"),
                valid.replace("0".repeat(64), "A".repeat(64)), "x".repeat(4097), ""}) {
            assertThrows(IOException.class, () -> NativeLibrary.Descriptor.read(text(bad), platform), bad);
        }
        assertThrows(IOException.class, () -> NativeLibrary.Descriptor.read(null, platform));
        assertThrows(IOException.class, () -> NativeLibrary.Descriptor.read(new ByteArrayInputStream(new byte[]{(byte)0xff}), platform));
    }

    @Test void unprovisionedBackendIsDormantUntilAnAssetIsRequested() throws Exception {
        var source = ClassResourceSource.fromLocation(directory.toUri().toURL());
        var backend = new NativePak(source);
        try (var files = Files.list(directory)) { assertEquals(0, files.count()); }
        IOException error = assertThrows(IOException.class, () -> backend.decrypt(text(""),
                new PakAsset("pak_test", "pak_test:textures/test.png", "synthetic-v1")));
        assertTrue(error.getMessage().contains("provisioned"));
    }

    @Test void productionBackendRejectsSyntheticDescriptorsBeforeLoadingCode() throws Exception {
        NativePlatform platform = NativePlatform.current();
        Path resource = directory.resolve(platform.resourceRoot());
        Files.createDirectories(resource);
        Files.writeString(resource.resolve("library.properties"), descriptor(platform, true));
        var backend = new NativePak(ClassResourceSource.fromLocation(directory.toUri().toURL()));
        IOException error = assertThrows(IOException.class, () -> backend.decrypt(text(""),
                new PakAsset("pak_test", "pak_test:textures/test.png", "synthetic-v1")));
        assertTrue(error.getMessage().contains("Synthetic"));
    }
}
