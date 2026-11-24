package cc.thonly.reverie_dreams.world.gensokyo.storage;

import cc.thonly.reverie_dreams.ReverieDreamsConfiguration;
import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import com.google.gson.Gson;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.zip.InflaterInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Exercises vanilla's real Anvil allocation and external-file writer, without starting a world. */
class GensokyoRegionFileTest {
    @TempDir Path directory;

    @BeforeAll
    static void initializeVanillaRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void targetsOnlyGensokyoTerrainAndDefaultsToDisabled() {
        assertTrue(GensokyoChunkCompression.isTerrainStorage(info("chunk")));
        for (String type : new String[]{"entities", "poi", "chunk_upgrade"}) {
            assertFalse(GensokyoChunkCompression.isTerrainStorage(info(type)));
        }
        assertFalse(GensokyoChunkCompression.isTerrainStorage(new RegionStorageInfo("test", RDBuiltinLevels.DREAM_WORLD, "chunk")));
        assertFalse(GensokyoChunkCompression.isTerrainStorage(new RegionStorageInfo("test", RDBuiltinLevels.THE_MOON, "chunk")));
        for (var dimension : List.of(Level.OVERWORLD, Level.NETHER, Level.END)) {
            assertFalse(GensokyoChunkCompression.isTerrainStorage(new RegionStorageInfo("test", dimension, "chunk")));
        }
        assertFalse(new ReverieDreamsConfiguration().enableGensokyoChunkZstd);
        assertFalse(new Gson().fromJson("{}", ReverieDreamsConfiguration.class).enableGensokyoChunkZstd);
        assertTrue(new Gson().fromJson("{\"enableGensokyoChunkZstd\":true}", ReverieDreamsConfiguration.class).enableGensokyoChunkZstd);
        assertEquals(8, new ReverieDreamsConfiguration().gensokyoChunkZstdLevel);
    }

    @Test
    void versionSupportsDirectWrapWithoutChangingGlobalCustomCodec() throws Exception {
        var version = GensokyoChunkCompression.zstdVersion(8);
        assertNotSame(RegionFileVersion.VERSION_CUSTOM, version);
        assertEquals(127, version.getId());
        assertSame(RegionFileVersion.VERSION_CUSTOM, RegionFileVersion.fromId(127));
        assertSame(version, GensokyoChunkCompression.zstdVersion(8));
        assertNotSame(version, GensokyoChunkCompression.zstdVersion(3));
        assertSame(GensokyoChunkCompression.zstdVersion(1), GensokyoChunkCompression.zstdVersion(0));
        assertSame(GensokyoChunkCompression.zstdVersion(19), GensokyoChunkCompression.zstdVersion(99));
        byte[] payload = new byte[65536];
        new Random(38).nextBytes(payload);
        var buffer = new ByteArrayOutputStream();
        // This is the direct version.wrap call used by C2ME's asynchronous compressor.
        try (OutputStream compressed = version.wrap(buffer)) {
            compressed.write(payload);
        }
        try (var input = version.wrap(new ByteArrayInputStream(buffer.toByteArray()))) {
            assertArrayEquals(payload, input.readAllBytes());
        }
        assertThrows(UnsupportedOperationException.class,
                () -> RegionFileVersion.VERSION_CUSTOM.wrap(new ByteArrayOutputStream()));
    }

    @Test
    void mixesOldDeflateAndNewZstdChunksAndCanResumeVanillaWrites() throws Exception {
        Path file = directory.resolve("r.0.0.mca");
        ChunkPos oldPos = new ChunkPos(0, 0);
        ChunkPos newPos = new ChunkPos(1, 0);
        byte[] oldData = "old complete chunk".getBytes(StandardCharsets.UTF_8);
        byte[] newData = new byte[65536];
        new Random(71).nextBytes(newData);
        try (RegionFile region = new RegionFile(info("chunk"), file, directory, RegionFileVersion.VERSION_DEFLATE, true)) {
            try (var output = region.getChunkDataOutputStream(oldPos)) {
                output.write(oldData);
            }
        }
        try (RegionFile region = new RegionFile(info("chunk"), file, directory, GensokyoChunkCompression.zstdVersion(8), true)) {
            writeCustom(region, newPos, newData);
            assertTrue(region.doesChunkExist(oldPos));
            assertTrue(region.doesChunkExist(newPos));
        }
        assertArrayEquals(oldData, readRawChunk(file, oldPos));
        assertArrayEquals(newData, readRawChunk(file, newPos));
        // Turning the writer off never changes how previously written custom chunks are decoded.
        try (RegionFile region = new RegionFile(info("chunk"), file, directory, RegionFileVersion.VERSION_DEFLATE, true)) {
            try (var output = region.getChunkDataOutputStream(oldPos)) {
                output.write(newData);
            }
        }
        assertArrayEquals(newData, readRawChunk(file, oldPos));
        assertArrayEquals(newData, readRawChunk(file, newPos));
    }

    @Test
    void preservesVanillaExternalMccSupportForLargeZstdChunks() throws Exception {
        Path file = directory.resolve("r.0.0.mca");
        ChunkPos position = new ChunkPos(2, 0);
        byte[] payload = new byte[3 * 1024 * 1024];
        new Random(1234).nextBytes(payload);
        try (RegionFile region = new RegionFile(info("chunk"), file, directory, GensokyoChunkCompression.zstdVersion(8), true)) {
            writeCustom(region, position, payload);
            assertTrue(region.doesChunkExist(position));
        }
        assertTrue(Files.exists(directory.resolve("c.2.0.mcc")));
        assertArrayEquals(payload, readRawChunk(file, position));
    }

    private static RegionStorageInfo info(String type) {
        return new RegionStorageInfo("zstd-test", RDBuiltinLevels.GENSOKYO, type);
    }

    private static void writeCustom(RegionFile region, ChunkPos position, byte[] payload) throws Exception {
        try (OutputStream output = region.getChunkDataOutputStream(position)) {
            output.write(payload);
        }
    }

    private byte[] readRawChunk(Path file, ChunkPos position) throws IOException {
        // Independently verifies the on-disk codec id, sector envelope, and external stream flag.
        try (RandomAccessFile input = new RandomAccessFile(file.toFile(), "r")) {
            input.seek(((position.x() & 31) + (position.z() & 31) * 32) * 4L);
            int offset = input.readInt();
            assertNotEquals(0, offset);
            input.seek((offset >>> 8) * 4096L);
            int length = input.readInt();
            int version = input.readUnsignedByte();
            byte[] compressed;
            if ((version & 128) != 0) {
                assertEquals(1, length);
                compressed = Files.readAllBytes(directory.resolve("c." + position.x() + "." + position.z() + ".mcc"));
            } else {
                compressed = new byte[length - 1];
                input.readFully(compressed);
            }
            if ((version & 127) == 2) {
                try (var stream = new InflaterInputStream(new ByteArrayInputStream(compressed))) {
                    return stream.readAllBytes();
                }
            }
            assertEquals(127, version & 127);
            try (DataInputStream stream = ZstdChunkCodec.decodeCustom(new ByteArrayInputStream(compressed), ignored -> {
                throw new IOException("Unexpected custom codec");
            })) {
                return stream.readAllBytes();
            }
        }
    }
}
