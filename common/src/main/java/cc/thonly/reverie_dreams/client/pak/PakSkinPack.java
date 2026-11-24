package cc.thonly.reverie_dreams.client.pak;

import cc.thonly.pak.PakDecoder;
import cc.thonly.pak.PakException;
import cc.thonly.pak.PakLimits;
import cc.thonly.pak.PngHeader;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.InclusiveRange;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * Only the client repository mounts this pack. It never proxies other resource packs.
 */
public final class PakSkinPack implements PackResources {
    @FunctionalInterface
    public interface Source {
        InputStream open(String path) throws IOException;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("reverie_dreams/pak");
    private static final String SKIN_DIRECTORY = "textures/entity/player/skin/";
    private final PackLocationInfo location;
    private final Source source;
    private final PakDecoder decoder;
    // Cache ciphertext only. Decoded payloads belong to individual streams.
    private final PakResourceCache cache = new PakResourceCache(2 * 1024 * 1024);
    private final Semaphore decoding = new Semaphore(4);
    private final Set<WipingPngInputStream> images = ConcurrentHashMap.newKeySet();
    private volatile SkinPackManifest manifest;
    private volatile IOException catalogFailure;
    private boolean closed;

    public PakSkinPack(PackLocationInfo location, Source source, PakDecoder decoder) {
        this.location = location;
        this.source = source;
        this.decoder = decoder;
    }

    private SkinPackManifest manifest() throws IOException {
        SkinPackManifest current = manifest;
        if (current != null)
            return current;
        synchronized (this) {
            if (manifest != null)
                return manifest;
            if (catalogFailure != null)
                throw new IOException("PAK catalog is unavailable", catalogFailure);
            try {
                manifest = SkinCatalog.load(source, decoder);
                return manifest;
            } catch (IOException e) {
                catalogFailure = e;
                LOGGER.error("Cannot read the PAK skin catalog: {}", e.getMessage());
                throw e;
            }
        }
    }

    private boolean skinResource(Identifier id) {
        return id.getNamespace().equals(SkinPackManifest.OWNER) && id.getPath().startsWith(SKIN_DIRECTORY);
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... path) {
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType type, Identifier id) {
        if (type != PackType.CLIENT_RESOURCES)
            return null;
        SkinPackManifest entries;
        try {
            entries = manifest();
        } catch (IOException e) {
            return null;
        }
        SkinPackManifest.Entry entry = entries.entries().get(id);
        return entry == null ? null : () -> openResource(entry, skinResource(id));
    }

    private InputStream openResource(SkinPackManifest.Entry entry, boolean skin) throws IOException {
        byte[] encoded = cache.get(entry.packedPath(), () -> {
            try (InputStream in = required(entry.packedPath())) {
                byte[] value = in.readNBytes(PakLimits.MAX_FILE_BYTES + 1);
                if (value.length > PakLimits.MAX_FILE_BYTES)
                    throw new PakException("PAK input exceeds size limit");
                return value;
            }
        });
        try {
            decoding.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted waiting to decode PAK resource", e);
        }
        byte[] plain = null;
        try {
            synchronized (this) {
                if (closed)
                    throw new IOException("PAK resource pack is closed");
            }
            plain = decoder.decrypt(new ByteArrayInputStream(encoded), entry.asset());
            if (skin) {
                PngHeader dimensions = PngHeader.check(plain);
                if (dimensions.width() != 64 || dimensions.height() != 64) {
                    throw new PakException("Gensokyo PAK skins must be 64x64");
                }
            }
            synchronized (this) {
                if (closed)
                    throw new IOException("PAK resource pack closed during decoding");
                var stream = new WipingPngInputStream(plain, images::remove);
                images.add(stream);
                plain = null; // Transfer exclusive ownership to the stream.
                return stream;
            }
        } finally {
            if (plain != null)
                Arrays.fill(plain, (byte) 0);
            decoding.release();
        }
    }

    private InputStream required(String path) throws IOException {
        InputStream input = source.open(path);
        if (input == null)
            throw new PakException("Missing PAK packed resource: " + path);
        return input;
    }

    @Override
    public void listResources(PackType type, String namespace, String prefix, ResourceOutput output) {
        if (type != PackType.CLIENT_RESOURCES || !namespace.equals(SkinPackManifest.OWNER))
            return;
        SkinPackManifest entries;
        try {
            entries = manifest();
        } catch (IOException e) {
            return;
        }
        entries.entries().forEach((id, entry) -> {
            if (prefix.isEmpty() || id.getPath().startsWith(prefix + "/")) {
                output.accept(id, () -> openResource(entry, skinResource(id)));
            }
        });
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.CLIENT_RESOURCES ? Set.of(SkinPackManifest.OWNER) : Set.of();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getMetadataSection(MetadataSectionType<T> type) {
        if (type == PackMetadataSection.CLIENT_TYPE || type == PackMetadataSection.FALLBACK_TYPE) {
            return (T) new PackMetadataSection(Component.literal("Gensokyo PAK skins"),
                    new InclusiveRange<>(SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES)));
        }
        return null;
    }

    @Override
    public PackLocationInfo location() {
        return location;
    }

    @Override
    public synchronized void close() {
        closed = true;
        cache.close();
        for (WipingPngInputStream stream : images)
            stream.close();
        images.clear();
    }
}
