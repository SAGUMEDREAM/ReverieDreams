package cc.thonly.reverie_dreams.world.moon.compat;

import cc.thonly.reverie_dreams.ReverieDreams;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.regex.Pattern;
import java.util.UUID;

import net.minecraft.nbt.NbtUtils;

/**
 * Transactional, one-time Moon conversion performed while the world's storage lock is held.
 */
public final class MoonSaveUpgrade {
    public static final String SETTINGS = "data/minecraft/world_gen_settings.dat";
    public static final String DIMENSION = "dimensions/reverie_dreams/the_moon";
    public static final String BACKUP = "backups/moon-terrain-v2";
    public static final String VERSION = "data/reverie_dreams/moon_terrain_version";
    private static final Logger LOGGER = LoggerFactory.getLogger("MoonSaveUpgrade");

    private MoonSaveUpgrade() {
    }

    public static boolean isLegacyGenerator(CompoundTag generator) {
        CompoundTag source = generator.getCompoundOrEmpty("biome_source");
        return generator.getStringOr("type", "").equals("minecraft:noise")
                && generator.getStringOr("settings", "").equals(ReverieDreams.MOD_ID + ":moon")
                && source.getStringOr("type", "").equals("minecraft:fixed")
                && source.getStringOr("biome", "").equals(ReverieDreams.MOD_ID + ":the_moon");
    }

    public static void replaceGenerator(CompoundTag generator) {
        generator.putString("type", ReverieDreams.MOD_ID + ":moon");
        CompoundTag source = new CompoundTag();
        source.putString("highlands", ReverieDreams.MOD_ID + ":the_moon");
        source.putString("basalt_mare", ReverieDreams.MOD_ID + ":basalt_mare");
        source.putString("ice_crater", ReverieDreams.MOD_ID + ":shadow_ice_crater");
        source.putString("sea_boundary", ReverieDreams.MOD_ID + ":moon_sea");
        generator.put("biome_source", source);
    }

    public static synchronized void upgrade(Path root) throws IOException {
        upgrade(root, true);
    }

    public static synchronized void upgrade(Path root, boolean supportedDatapackGenerator) throws IOException {
        root = root.toAbsolutePath().normalize();
        Path settings = root.resolve(SETTINGS);
        boolean legacyContainer = !Files.isRegularFile(settings);
        CompoundTag file;
        if (legacyContainer) {
            Path oldLevel = root.resolve("level.dat");
            if (!Files.isRegularFile(oldLevel))
                return;
            CompoundTag level = NbtIo.readCompressed(oldLevel, NbtAccounter.defaultQuota());
            CompoundTag oldSettings = level.getCompoundOrEmpty("Data").getCompoundOrEmpty("WorldGenSettings");
            if (oldSettings.isEmpty())
                return;
            file = new CompoundTag();
            file.put("data", oldSettings.copy());
            NbtUtils.addCurrentDataVersion(file);
        } else
            file = NbtIo.readCompressed(settings, NbtAccounter.defaultQuota());
        CompoundTag moon = file.getCompoundOrEmpty("data").getCompoundOrEmpty("dimensions")
                .getCompoundOrEmpty(ReverieDreams.MOD_ID + ":the_moon");
        boolean absentMoon = moon.isEmpty();
        boolean pendingSwap = Files.exists(root.resolve(BACKUP).resolve("staging-ready"))
                && !Files.exists(root.resolve(BACKUP).resolve("completed"));
        if (absentMoon) {
            // Datapack dimensions are baked into the runtime registry but often omitted from saved WorldDimensions.
            if (Files.isRegularFile(root.resolve(VERSION))) {
                if (!Files.readString(root.resolve(VERSION)).trim().equals("2"))
                    throw new IOException("Unsupported Moon terrain version marker");
                return;
            }
            Path regions = root.resolve(DIMENSION).resolve("region");
            if (!pendingSwap && (!hasRegionFiles(regions) || !hasLegacyChunks(regions))) {
                writeVersion(root);
                return;
            }
            if (!supportedDatapackGenerator)
                throw new IOException("Legacy Moon chunks exist, but the active datapack supplied an unsupported Moon generator; world has not been changed");
        }
        CompoundTag generator = moon.getCompoundOrEmpty("generator");
        if (!absentMoon && !isLegacyGenerator(generator)) {
            // The built-in dimension type expands globally, even when a datapack supplied its generator.
            // Loading untouched old relative section metadata under that type would shift heightmaps.
            if (!generator.getStringOr("type", "").equals(ReverieDreams.MOD_ID + ":moon")
                    && moon.getStringOr("type", "").equals(ReverieDreams.MOD_ID + ":the_moon")
                    && hasRegionFiles(root.resolve(DIMENSION).resolve("region")))
                throw new IOException("Existing Moon uses the expanded built-in dimension type with an unsupported custom generator. "
                        + "World has not been changed; migrate the custom generator/type on a backup first.");
            return;
        }
        if (!absentMoon && !(moon.get("type") instanceof CompoundTag)
                && !moon.getStringOr("type", "").equals(ReverieDreams.MOD_ID + ":the_moon"))
            throw new IOException("Legacy Moon generator uses an unsupported custom dimension type; world has not been changed");
        if (moon.get("type") instanceof CompoundTag inlineType
                && (inlineType.getIntOr("min_y", -32) != -32 || inlineType.getIntOr("height", 256) != 256))
            throw new IOException("Unsupported custom inline Moon height; original world has not been changed");
        Path dimension = root.resolve(DIMENSION);
        Path staging = dimension.resolveSibling("the_moon.moon-v2-staging");
        Path backup = root.resolve(BACKUP);
        if (Files.exists(backup.resolve("completed"))) {
            // A restored old world may legitimately need migration again. Keep the earlier backup untouched.
            move(backup, backup.resolveSibling("moon-terrain-v2-history-" + UUID.randomUUID()));
        }
        Path oldDimension = backup.resolve("dimension");
        Path ready = backup.resolve("staging-ready");
        Files.createDirectories(backup);
        if (!Files.exists(backup.resolve("world_gen_settings.dat"))) {
            if (legacyContainer)
                NbtIo.writeCompressed(file, backup.resolve("world_gen_settings.dat"));
            else
                Files.copy(settings, backup.resolve("world_gen_settings.dat"));
        }
        if (Files.isRegularFile(root.resolve("level.dat")) && !Files.exists(backup.resolve("level.dat")))
            Files.copy(root.resolve("level.dat"), backup.resolve("level.dat"));
        LOGGER.info("Preparing Moon terrain v2 upgrade; original files retained in {}", backup);

        if (!Files.exists(oldDimension)) {
            if (Files.isDirectory(dimension)) {
                if (!Files.exists(ready)) {
                    copyTree(dimension, staging);
                    MoonLegacyMask.scan(staging.resolve("region")).write(backup.resolve("legacy-mask.bin"));
                    convertRegions(staging.resolve("region"));
                    Files.writeString(ready, "complete\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                }
                move(dimension, oldDimension);
            } else if (!Files.exists(ready)) {
                MoonLegacyMask.EMPTY.write(backup.resolve("legacy-mask.bin"));
                Files.writeString(ready, "no-existing-moon\n");
            }
        }
        // Recovery after interruption between the two directory moves.
        if (Files.exists(staging) && !Files.exists(dimension))
            move(staging, dimension);
        if (Files.exists(oldDimension) && !Files.exists(dimension))
            throw new IOException("Moon conversion staging missing; restore backup before starting: " + backup);
        Path mask = root.resolve(MoonLegacyMask.FILE);
        Files.createDirectories(mask.getParent());
        Files.copy(backup.resolve("legacy-mask.bin"), mask, StandardCopyOption.REPLACE_EXISTING);
        if (moon.get("type") instanceof CompoundTag inlineType) {
            inlineType.putInt("min_y", -128);
            inlineType.putInt("height", 352);
            inlineType.putInt("logical_height", 352);
        }
        if (!absentMoon)
            replaceGenerator(generator);
        if (!absentMoon || legacyContainer) {
            Files.createDirectories(settings.getParent());
            Path temp = settings.resolveSibling("world_gen_settings.dat.moon-v2.tmp");
            NbtIo.writeCompressed(file, temp);
            moveReplace(temp, settings);
        }
        writeVersion(root);
        Files.writeString(backup.resolve("completed"), "version=2\n", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        LOGGER.info("Moon upgrade complete; original surface, buildings, entities, and inventory NBT preserved. Backup: {}", backup);
    }

    private static void convertRegions(Path folder) throws IOException {
        if (!Files.isDirectory(folder))
            return;
        var pattern = Pattern.compile("r\\.(-?\\d+)\\.(-?\\d+)\\.mca");
        var info = new RegionStorageInfo("moon-upgrade", ResourceKey.create(Registries.DIMENSION,
                Identifier.parse(ReverieDreams.MOD_ID + ":the_moon")), "chunk");
        try (var files = Files.list(folder)) {
            for (Path path : files.sorted().toList()) {
                var match = pattern.matcher(path.getFileName().toString());
                if (!match.matches())
                    continue;
                int rx = Integer.parseInt(match.group(1)), rz = Integer.parseInt(match.group(2));
                try (var region = new RegionFile(info, path, folder, RegionFileVersion.VERSION_DEFLATE, true)) {
                    for (int z = 0; z < 32; z++)
                        for (int x = 0; x < 32; x++) {
                            ChunkPos pos = new ChunkPos(rx * 32 + x, rz * 32 + z);
                            if (!region.hasChunk(pos))
                                continue;
                            CompoundTag chunk;
                            try (var in = region.getChunkDataInputStream(pos)) {
                                if (in == null)
                                    throw new IOException("Unreadable Moon chunk " + pos + " in " + path);
                                chunk = NbtIo.read(in);
                            }
                            if (MoonChunkUpgrade.upgrade(chunk))
                                try (var out = region.getChunkDataOutputStream(pos)) {
                                    NbtIo.write(chunk, out);
                                }
                        }
                    region.flush();
                }
                LOGGER.info("Converted Moon region {} in staging", path.getFileName());
            }
        }
    }

    private static boolean hasRegionFiles(Path folder) throws IOException {
        if (!Files.isDirectory(folder))
            return false;
        try (var files = Files.list(folder)) {
            return files.anyMatch(file -> Files.isRegularFile(file)
                    && file.getFileName().toString().matches("r\\.-?\\d+\\.-?\\d+\\.mca"));
        }
    }

    /**
     * Called before creation of a fresh world's first Moon chunk. Existing unversioned worlds must be scanned.
     */
    public static void markFreshWorld(Path root) throws IOException {
        if (!Files.exists(root.resolve(VERSION)) && !hasRegionFiles(root.resolve(DIMENSION).resolve("region")))
            writeVersion(root);
    }

    private static void writeVersion(Path root) throws IOException {
        Path version = root.resolve(VERSION);
        Files.createDirectories(version.getParent());
        Path temp = version.resolveSibling("moon_terrain_version.tmp");
        Files.writeString(temp, "2\n");
        moveReplace(temp, version);
    }

    private static boolean hasLegacyChunks(Path folder) throws IOException {
        MoonLegacyMask.scan(folder); // Validate allocations before RegionFile can sanitize a corrupt header.
        var pattern = Pattern.compile("r\\.(-?\\d+)\\.(-?\\d+)\\.mca");
        var info = new RegionStorageInfo("moon-upgrade-inspect", ResourceKey.create(Registries.DIMENSION,
                Identifier.parse(ReverieDreams.MOD_ID + ":the_moon")), "chunk");
        boolean legacy = false;
        try (var files = Files.list(folder)) {
            for (Path path : files.toList()) {
                var match = pattern.matcher(path.getFileName().toString());
                if (!match.matches())
                    continue;
                int rx = Integer.parseInt(match.group(1)), rz = Integer.parseInt(match.group(2));
                try (var region = new RegionFile(info, path, folder, RegionFileVersion.VERSION_DEFLATE, false)) {
                    for (int z = 0; z < 32; z++)
                        for (int x = 0; x < 32; x++) {
                            ChunkPos pos = new ChunkPos(rx * 32 + x, rz * 32 + z);
                            if (!region.hasChunk(pos))
                                continue;
                            try (var in = region.getChunkDataInputStream(pos)) {
                                if (in == null)
                                    throw new IOException("Unreadable Moon chunk " + pos);
                                CompoundTag chunk = NbtIo.read(in);
                                if (chunk.getIntOr(MoonChunkUpgrade.MARKER, 0) >= 2 || chunk.getIntOr("yPos", -2) == -8)
                                    continue;
                                if (chunk.getIntOr("yPos", -2) != -2)
                                    throw new IOException("Unsupported Moon chunk min section at " + pos);
                                legacy = true;
                            }
                        }
                }
            }
        }
        return legacy;
    }

    private static void copyTree(Path source, Path destination) throws IOException {
        Files.createDirectories(destination);
        try (var paths = Files.walk(source)) {
            for (Path path : paths.toList()) {
                if (Files.isSymbolicLink(path))
                    throw new IOException("Symbolic links unsupported in Moon upgrade: " + path);
                Path target = destination.resolve(source.relativize(path));
                if (Files.isDirectory(path))
                    Files.createDirectories(target);
                else
                    Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            }
        }
    }

    private static void move(Path source, Path destination) throws IOException {
        try {
            Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException unsupported) {
            Files.move(source, destination);
        }
    }

    private static void moveReplace(Path source, Path destination) throws IOException {
        try {
            Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException unsupported) {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
