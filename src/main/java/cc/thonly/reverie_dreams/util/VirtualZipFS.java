package cc.thonly.reverie_dreams.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 一个虚拟文件系统包装类，用于在内存中访问 ZIP 内容。
 * 支持 Zip64、一次性加载、流式访问，不会写入磁盘。
 */
@Slf4j
public class VirtualZipFS implements Closeable {
    private final ZipFile zip;
    private final Map<String, FileEntry> files = new LinkedHashMap<>();

    public VirtualZipFS(ZipFile zip) {
        this.zip = zip;
        this.load();
    }

    /** 从 Path 打开 ZIP */
    public static VirtualZipFS open(Path path) throws IOException {
        return new VirtualZipFS(new ZipFile(path.toFile()));
    }

    /** 获取某个文件的字节内容 */
    public byte[] getBytes(String path) {
        path = normalize(path);
        FileEntry entry = files.get(path);
        if (entry == null) {
            log.warn("File not found in virtual zip: {}", path);
            return new byte[0];
        }
        return entry.data();
    }

    /** 获取某个文件的 UTF-8 字符串内容 */
    public String getString(String path) {
        return new String(getBytes(path), StandardCharsets.UTF_8);
    }

    /** 获取完整的 FileEntry */
    public FileEntry get(String path) {
        return files.get(normalize(path));
    }

    /** 列出指定目录下的所有文件路径（递归） */
    public List<String> list(String path) {
        path = normalize(path);
        if (!path.endsWith("/")) path += "/";
        List<String> result = new ArrayList<>();
        for (String name : this.files.keySet()) {
            if (name.startsWith(path)) {
                result.add(name);
            }
        }
        return result;
    }

    /** 检查文件是否存在 */
    public boolean exists(String path) {
        return files.containsKey(normalize(path));
    }

    /** 加载 ZIP 文件中所有内容 */
    private void load() {
        Enumeration<? extends ZipEntry> entries = this.zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory()) continue;

            String fullName = normalize(entry.getName());
            String filename = extractFileName(fullName);
            String extension = extractExtension(fullName);

            try (InputStream in = zip.getInputStream(entry)) {
                byte[] data = in.readAllBytes();
                this.files.put(fullName, new FileEntry(filename, extension, data));
            } catch (Exception err) {
                log.error("Failed to load entry {} from zip: {}", fullName, err.toString());
            }
        }
        log.info("Loaded virtual zip with {} files", this.files.size());
    }

    /** 提取文件名（不含路径） */
    private static String extractFileName(String fullPath) {
        int slash = fullPath.lastIndexOf('/');
        return (slash >= 0 ? fullPath.substring(slash + 1) : fullPath);
    }

    /** 提取扩展名（不含点） */
    private static String extractExtension(String fullPath) {
        String name = extractFileName(fullPath);
        int dot = name.lastIndexOf('.');
        return (dot >= 0 ? name.substring(dot + 1).toLowerCase(Locale.ROOT) : "");
    }

    /** 统一路径分隔符为 '/' */
    private static String normalize(String path) {
        return path.replace('\\', '/');
    }

    @Override
    public void close() throws IOException {
        this.zip.close();
        this.files.clear();
    }

    /** 内部文件结构记录 */
    public record FileEntry(String filename, String extension, byte[] data) {}
}
