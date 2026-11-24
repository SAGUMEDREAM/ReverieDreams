package cc.thonly.pak;

import java.io.IOException;
import java.util.Locale;

/** Native targets selected using the running JVM architecture and Android runtime. */
public enum NativePlatform {
    WINDOWS_X86_64("windows-x86_64", "pak_native.dll"),
    WINDOWS_ARM64("windows-arm64", "pak_native.dll"),
    LINUX_X86_64("linux-x86_64", "libpak_native.so"),
    LINUX_ARM64("linux-arm64", "libpak_native.so"),
    MACOS_X86_64("macos-x86_64", "libpak_native.dylib"),
    MACOS_ARM64("macos-arm64", "libpak_native.dylib"),
    ANDROID_ARM64("android-arm64", "libpak_native.so"),
    ANDROID_X86_64("android-x86_64", "libpak_native.so");

    private final String id;
    private final String library;

    NativePlatform(String id, String library) { this.id = id; this.library = library; }
    public String id() { return id; }
    public String library() { return library; }
    public String resourceRoot() { return "META-INF/native/" + id + "/"; }

    public static NativePlatform current() throws IOException {
        String runtime = System.getProperty("java.runtime.name", "") + " "
                + System.getProperty("java.vm.name", "");
        String os = runtime.toLowerCase(Locale.ROOT).contains("android")
                || runtime.toLowerCase(Locale.ROOT).contains("dalvik") ? "android" : System.getProperty("os.name", "");
        return detect(os, System.getProperty("os.arch", ""));
    }

    static NativePlatform detect(String os, String arch) throws IOException {
        os = os.toLowerCase(Locale.ROOT);
        arch = arch.toLowerCase(Locale.ROOT);
        boolean arm = arch.equals("aarch64") || arch.equals("arm64");
        if (!arm && !arch.equals("amd64") && !arch.equals("x86_64") && !arch.equals("x64")) {
            throw new IOException("Unsupported PAK JVM architecture: " + arch);
        }
        if (os.startsWith("windows")) return arm ? WINDOWS_ARM64 : WINDOWS_X86_64;
        if (os.equals("android")) return arm ? ANDROID_ARM64 : ANDROID_X86_64;
        if (os.equals("linux")) return arm ? LINUX_ARM64 : LINUX_X86_64;
        if (os.equals("mac os x") || os.equals("macos") || os.equals("darwin")) return arm ? MACOS_ARM64 : MACOS_X86_64;
        throw new IOException("Unsupported PAK operating system: " + os);
    }
}
