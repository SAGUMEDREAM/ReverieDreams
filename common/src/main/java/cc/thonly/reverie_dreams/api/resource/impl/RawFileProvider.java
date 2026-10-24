package cc.thonly.reverie_dreams.api.resource.impl;

import cc.thonly.reverie_dreams.api.resource.ResourceLocatorApi;
import net.minecraft.IdentifierException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class RawFileProvider {
    public static void addPacks(CompositeResourcePack pack) {
        Path modsDir = Path.of("./").toAbsolutePath().normalize().resolve("mods");
        if (!Files.isDirectory(modsDir)) {
            ResourceLocatorApi.LOGGER.error("Mods folder isn't a directory");
        }

        try (Stream<Path> stream = Files.walk(modsDir, FileVisitOption.FOLLOW_LINKS)) {
            stream.forEach(path -> {
                if (isValidFile(path)) {
                    try (
                            FileInputStream inputStream = new FileInputStream(path.toFile());
                            BufferedInputStream bufStream = new BufferedInputStream(inputStream);
                    ) {
                        handleFile(pack, bufStream, path.toString());
                    } catch (FileNotFoundException var10) {
                        ResourceLocatorApi.LOGGER.error("File seems to have disappeared {}", path, var10);
                    } catch (IOException var11) {
                        ResourceLocatorApi.LOGGER.error("IO exception reading {}", path, var11);
                    }
                }
            });
        } catch (IOException var7) {
            ResourceLocatorApi.LOGGER.error("IO exception whilst trying to walk files", var7);
        }
    }

    private static boolean isValidFile(Path path) {
        if (!Files.isRegularFile(path)) {
            return false;
        } else {
            try {
                if (Files.isHidden(path)) {
                    return false;
                }
            } catch (IOException var2) {
                ResourceLocatorApi.LOGGER.error("Error checking if file is hidden: " + path, var2);
                return false;
            }

            String fileName = path.getFileName().toString();
            return fileName.endsWith(".jar") && !fileName.startsWith(".");
        }
    }

    private static void handleFile(CompositeResourcePack pack, InputStream stream, String path) {
        BufferResourcePack newPack = new BufferResourcePack("Rawfile (" + path + ")");

        try {
            ZipInputStream zipStream = new ZipInputStream(stream);

            ZipEntry entry;
            while ((entry = zipStream.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    if (entry.getName().startsWith("META-INF/jars/") || entry.getName().startsWith("/META-INF/jars/")) {
                        handleFile(pack, zipStream, entry.getName() + " in " + path);
                    } else if (entry.getName().startsWith("assets/") || entry.getName().startsWith("/assets/")) {
                        ByteBuffer buffer = readMod(zipStream);
                        String[] split = entry.getName().substring(1).split("/", 3);
                        if (split.length == 3) {
                            Identifier id = Identifier.tryBuild(split[1], split[2]);
                            if (id != null) {
                                newPack.putAsset(id, buffer);
                            }
                        }
                    }
                }
            }
        } catch (IOException var9) {
            ResourceLocatorApi.LOGGER.error("Error whilst reading zip " + path, var9);
        }

        pack.append(newPack);
    }

    static ByteBuffer readMod(InputStream is) throws IOException {
        int available = is.available();
        boolean availableGood = available > 1;
        byte[] buffer = new byte[availableGood ? available : 30000];
        int offset = 0;

        int len;
        while ((len = is.read(buffer, offset, buffer.length - offset)) >= 0) {
            offset += len;
            if (offset == buffer.length) {
                if (availableGood) {
                    int val = is.read();
                    if (val < 0) {
                        break;
                    }

                    availableGood = false;
                    buffer = Arrays.copyOf(buffer, Math.max(buffer.length * 2, 30000));
                    buffer[offset++] = (byte) val;
                } else {
                    buffer = Arrays.copyOf(buffer, buffer.length * 2);
                }
            }
        }

        return ByteBuffer.wrap(buffer, 0, offset);
    }

    private static final class BufferResourcePack extends AbstractPackResources implements MoreContextPack {
        private final Map<Identifier, ByteBuffer> assets = new HashMap<>();
        private final Map<Identifier, ByteBuffer> data = new HashMap<>();

        private BufferResourcePack(String name) {
            super(new PackLocationInfo(name, Component.literal(name), PackSource.DEFAULT, Optional.empty()));
        }

        @Nullable
        public IoSupplier<InputStream> getRootResource(String... segments) {
            return segments.length >= 3
                    ? this.createSupplier(
                    segments[0].equals("assets") ? PackType.CLIENT_RESOURCES : PackType.SERVER_DATA,
                    Identifier.fromNamespaceAndPath(segments[0], String.join("/", List.of(segments).subList(2, segments.length)))
            )
                    : null;
        }

        @Nullable
        public IoSupplier<InputStream> getResource(PackType type, Identifier id) {
            return this.createSupplier(type, id);
        }

        public void listResources(PackType type, String namespace, String prefix, ResourceOutput consumer) {
            Map<Identifier, ByteBuffer> map = type == PackType.CLIENT_RESOURCES ? this.assets : this.data;
            map.forEach((path, buf) -> {
                if (path.getNamespace().equals(namespace) && path.getPath().startsWith(prefix)) {
                    try {
                        consumer.accept(path, () -> new ByteBufferInputStream(buf));
                    } catch (IdentifierException var6) {
                        ResourceLocatorApi.LOGGER.warn("Invalid path in pack, ignoring: " + path);
                    }
                }
            });
        }

        public Set<String> getNamespaces(PackType type) {
            return (type == PackType.CLIENT_RESOURCES ? this.assets : this.data)
                    .keySet()
                    .stream()
                    .<String>map(Identifier::getNamespace)
                    .collect(Collectors.toSet());
        }

        public void close() {
        }

        @Nullable
        private IoSupplier<InputStream> createSupplier(PackType type, Identifier identifier) {
            ByteBuffer buf = (type == PackType.CLIENT_RESOURCES ? this.assets : this.data).get(identifier);
            return buf == null ? null : () -> new ByteBufferInputStream(buf);
        }

        public void putAsset(Identifier id, ByteBuffer buf) {
            this.assets.put(id, buf);
        }

        public void putData(Identifier id, ByteBuffer buf) {
            this.data.put(id, buf);
        }

        @Override
        public String keine$getFullName() {
            return this.packId();
        }
    }

    private static final class ByteBufferInputStream extends InputStream {
        private final ByteBuffer buffer;
        private int pos;

        private ByteBufferInputStream(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public int read() throws IOException {
            return this.pos >= this.buffer.limit() ? -1 : this.buffer.get(this.pos++) & 0xFF;
        }

        @Override
        public int read(@NotNull byte[] b, int off, int len) throws IOException {
            int rem = this.buffer.limit() - this.pos;
            if (rem <= 0) {
                return -1;
            } else {
                len = Math.min(len, rem);
                System.arraycopy(this.buffer.array(), this.pos, b, off, len);
                this.pos += len;
                return len;
            }
        }
    }
}
