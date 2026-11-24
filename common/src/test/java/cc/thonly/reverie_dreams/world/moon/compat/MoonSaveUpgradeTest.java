package cc.thonly.reverie_dreams.world.moon.compat;

import net.minecraft.SharedConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;

class MoonSaveUpgradeTest {
    @TempDir Path root;
    @BeforeAll static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }

    static CompoundTag legacySettings() {
        CompoundTag source = new CompoundTag(); source.putString("type", "minecraft:fixed"); source.putString("biome", "reverie_dreams:the_moon");
        CompoundTag generator = new CompoundTag(); generator.putString("type", "minecraft:noise"); generator.putString("settings", "reverie_dreams:moon"); generator.put("biome_source", source);
        CompoundTag moon = new CompoundTag(); moon.putString("type", "reverie_dreams:the_moon"); moon.put("generator", generator);
        CompoundTag dimensions = new CompoundTag(); dimensions.put("reverie_dreams:the_moon", moon);
        CompoundTag other = new CompoundTag(); other.putString("sentinel", "do-not-modify"); dimensions.put("minecraft:overworld", other);
        CompoundTag data = new CompoundTag(); data.put("dimensions", dimensions); data.putLong("seed", 76123);
        CompoundTag result = new CompoundTag(); result.put("data", data); result.putInt("DataVersion", 4786); return result;
    }
    static CompoundTag generator(CompoundTag settings) { return settings.getCompoundOrEmpty("data").getCompoundOrEmpty("dimensions").getCompoundOrEmpty("reverie_dreams:the_moon").getCompoundOrEmpty("generator"); }
    static CompoundTag chunk() {
        CompoundTag chunk = new CompoundTag(); chunk.putInt("xPos", -1); chunk.putInt("zPos", 2); chunk.putString("Status", "minecraft:full");
        chunk.putString("custom-owner", "preserve"); chunk.putLong("InhabitedTime", 7654); chunk.putInt("yPos", -2);
        ListTag sections = new ListTag(); CompoundTag old = new CompoundTag(); old.putByte("Y", (byte)-2);
        CompoundTag blocks = new CompoundTag(); ListTag palette = new ListTag(); CompoundTag bedrock = new CompoundTag(); bedrock.putString("Name", "minecraft:bedrock"); palette.add(bedrock); blocks.put("palette", palette); old.put("block_states", blocks); sections.add(old);
        CompoundTag light = new CompoundTag(); light.putByte("Y", (byte)-3); light.putByteArray("SkyLight", new byte[2048]); sections.add(light);
        chunk.put("sections", sections); CompoundTag heights = new CompoundTag(); heights.putLongArray("WORLD_SURFACE", new long[]{125}); chunk.put("Heightmaps", heights);
        ListTag post = new ListTag(); ListTag first = new ListTag(); first.add(ShortTag.valueOf((short)42)); post.add(first); chunk.put("PostProcessing", post);
        ListTag blockEntities = new ListTag(); CompoundTag chest = new CompoundTag(); chest.putString("id", "minecraft:chest"); chest.putString("Items-test", "precious inventory"); blockEntities.add(chest); chunk.put("block_entities", blockEntities);
        return chunk;
    }
    static RegionStorageInfo info() { return new RegionStorageInfo("fixture", ResourceKey.create(Registries.DIMENSION, Identifier.parse("reverie_dreams:the_moon")), "chunk"); }

    @Test void convertsOnlyRecognizedMoonGenerator() {
        CompoundTag file = legacySettings(), seedAndOthers = file.copy();
        assertTrue(MoonSaveUpgrade.isLegacyGenerator(generator(file)));
        generator(file).putString("settings", "other:custom"); assertFalse(MoonSaveUpgrade.isLegacyGenerator(generator(file)));
        file = seedAndOthers.copy(); MoonSaveUpgrade.replaceGenerator(generator(file));
        assertEquals(76123, file.getCompoundOrEmpty("data").getLongOr("seed", 0));
        assertEquals(seedAndOthers.getCompoundOrEmpty("data").getCompoundOrEmpty("dimensions").get("minecraft:overworld"), file.getCompoundOrEmpty("data").getCompoundOrEmpty("dimensions").get("minecraft:overworld"));
        assertEquals("reverie_dreams:moon", generator(file).getStringOr("type", ""));
        assertFalse(MoonSaveUpgrade.isLegacyGenerator(generator(file)));
    }
    @Test void preservesOldBlocksInventoriesAndOffsetsWhileExtendingDownward() {
        CompoundTag original = chunk(), upgraded = original.copy();
        assertTrue(MoonChunkUpgrade.upgrade(upgraded));
        assertEquals(original.getListOrEmpty("sections").get(0), upgraded.getListOrEmpty("sections").get(0));
        assertEquals(original.get("block_entities"), upgraded.get("block_entities"));
        assertEquals(7654, upgraded.getLongOr("InhabitedTime", 0));
        assertFalse(upgraded.contains("Heightmaps")); assertFalse(upgraded.getBooleanOr("isLightOn", true));
        assertEquals(7, upgraded.getListOrEmpty("sections").size());
        assertEquals(original.getListOrEmpty("PostProcessing").get(0), upgraded.getListOrEmpty("PostProcessing").get(6));
        CompoundTag once = upgraded.copy(); assertFalse(MoonChunkUpgrade.upgrade(upgraded)); assertEquals(once, upgraded);
    }
    @Test void maskHandlesNegativeRegionsAndSmoothsBoundary() throws Exception {
        Path folder = root.resolve("region"); Files.createDirectories(folder);
        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(folder.resolve("r.-1.0.mca")))) {
            for (int i=0;i<1024;i++) out.writeInt(i==31+2*32 ? 0x201 : 0);
            out.write(new byte[8192]); // timestamps plus one occupied sector
        }
        MoonLegacyMask mask = MoonLegacyMask.scan(folder); assertTrue(mask.contains(-1,2)); assertFalse(mask.contains(0,2));
        assertEquals(0, mask.depressionFactor(-1,32));
        assertTrue(mask.depressionFactor(0,32)>0 && mask.depressionFactor(0,32)<0.01);
        assertTrue(mask.depressionFactor(32,32)>mask.depressionFactor(0,32)); assertEquals(1, mask.depressionFactor(64,32));
        mask.write(root.resolve(MoonLegacyMask.FILE)); assertTrue(MoonLegacyMask.load(root).contains(-1,2));
    }
    @Test void migrationBacksUpAllMoonDataAndIsByteIdempotent() throws Exception {
        Path settings = root.resolve(MoonSaveUpgrade.SETTINGS); Files.createDirectories(settings.getParent()); NbtIo.writeCompressed(legacySettings(), settings);
        Path moon = root.resolve(MoonSaveUpgrade.DIMENSION), regionDir = moon.resolve("region"); Files.createDirectories(regionDir);
        Path regionFile = regionDir.resolve("r.-1.0.mca"); ChunkPos pos = new ChunkPos(-1,2);
        CompoundTag original = chunk();
        try (RegionFile region = new RegionFile(info(), regionFile, regionDir, RegionFileVersion.VERSION_DEFLATE, true)) { try(var out=region.getChunkDataOutputStream(pos)){NbtIo.write(original,out);} }
        Files.createDirectories(moon.resolve("entities")); Files.writeString(moon.resolve("entities/sentinel.dat"),"entity-uuid-kept");
        byte[] originalRegion = Files.readAllBytes(regionFile), originalSettings = Files.readAllBytes(settings);
        MoonSaveUpgrade.upgrade(root);
        assertArrayEquals(originalRegion, Files.readAllBytes(root.resolve(MoonSaveUpgrade.BACKUP).resolve("dimension/region/r.-1.0.mca")));
        assertArrayEquals(originalSettings, Files.readAllBytes(root.resolve(MoonSaveUpgrade.BACKUP).resolve("world_gen_settings.dat")));
        assertEquals("entity-uuid-kept",Files.readString(moon.resolve("entities/sentinel.dat")));
        assertTrue(MoonLegacyMask.load(root).contains(-1,2));
        try (RegionFile region = new RegionFile(info(),regionFile,regionDir,RegionFileVersion.VERSION_DEFLATE,true);var in=region.getChunkDataInputStream(pos)) {
            CompoundTag updated=NbtIo.read(in); assertEquals(original.get("block_entities"),updated.get("block_entities")); assertEquals(2,updated.getIntOr(MoonChunkUpgrade.MARKER,0));
        }
        byte[] once=Files.readAllBytes(regionFile),settingsOnce=Files.readAllBytes(settings);
        MoonSaveUpgrade.upgrade(root); assertArrayEquals(once,Files.readAllBytes(regionFile));assertArrayEquals(settingsOnce,Files.readAllBytes(settings));
    }
    @Test void unreadableRegionNeverMovesOrOverwritesOriginal() throws Exception {
        Path settings=root.resolve(MoonSaveUpgrade.SETTINGS);Files.createDirectories(settings.getParent());NbtIo.writeCompressed(legacySettings(),settings);
        Path region=root.resolve(MoonSaveUpgrade.DIMENSION).resolve("region/r.0.0.mca");Files.createDirectories(region.getParent());Files.write(region,new byte[]{1,2,3});
        assertThrows(IOException.class,()->MoonSaveUpgrade.upgrade(root));
        assertArrayEquals(new byte[]{1,2,3},Files.readAllBytes(region));assertTrue(MoonSaveUpgrade.isLegacyGenerator(generator(NbtIo.readCompressed(settings,NbtAccounter.defaultQuota()))));
    }
    @Test void shiftsPartialChunkCarvingAndUpgradeIndices() {
        CompoundTag chunk=chunk(), upgrade=new CompoundTag(), indices=new CompoundTag();indices.putIntArray("0",new int[]{42});upgrade.put("Indices",indices);chunk.put("UpgradeData",upgrade);chunk.putLongArray("carving_mask",new long[]{17});
        MoonChunkUpgrade.upgrade(chunk);
        assertArrayEquals(new int[]{42},chunk.getCompoundOrEmpty("UpgradeData").getCompoundOrEmpty("Indices").getIntArray("6").orElseThrow());
        long[] carving=chunk.getLongArray("carving_mask").orElseThrow();assertEquals(385,carving.length);assertEquals(17,carving[384]);assertEquals(0,carving[0]);
    }
    @Test void resumesInterruptedDirectorySwapBeforeCommittingMetadata() throws Exception {
        Path settings=root.resolve(MoonSaveUpgrade.SETTINGS);Files.createDirectories(settings.getParent());NbtIo.writeCompressed(legacySettings(),settings);
        Path backup=root.resolve(MoonSaveUpgrade.BACKUP);Files.createDirectories(backup.resolve("dimension"));Files.copy(settings,backup.resolve("world_gen_settings.dat"));
        Files.writeString(backup.resolve("dimension/original"),"retained");Files.writeString(backup.resolve("staging-ready"),"complete");MoonLegacyMask.EMPTY.write(backup.resolve("legacy-mask.bin"));
        Path moon=root.resolve(MoonSaveUpgrade.DIMENSION),stage=moon.resolveSibling("the_moon.moon-v2-staging");Files.createDirectories(stage);Files.writeString(stage.resolve("converted"),"ready");
        MoonSaveUpgrade.upgrade(root);
        assertEquals("ready",Files.readString(moon.resolve("converted")));assertEquals("retained",Files.readString(backup.resolve("dimension/original")));
        assertEquals("reverie_dreams:moon",generator(NbtIo.readCompressed(settings,NbtAccounter.defaultQuota())).getStringOr("type",""));
    }
    @Test void importsOldLevelDatContainerWithoutChangingItAndUpdatesInlineHeight() throws Exception {
        CompoundTag settings=legacySettings(),moon=settings.getCompoundOrEmpty("data").getCompoundOrEmpty("dimensions").getCompoundOrEmpty("reverie_dreams:the_moon");
        CompoundTag inline=new CompoundTag();inline.putInt("min_y",-32);inline.putInt("height",256);inline.putFloat("ambient_light",.25f);moon.put("type",inline);
        CompoundTag level=new CompoundTag(),data=new CompoundTag();data.put("WorldGenSettings",settings.get("data").copy());level.put("Data",data);NbtIo.writeCompressed(level,root.resolve("level.dat"));byte[] original=Files.readAllBytes(root.resolve("level.dat"));
        MoonSaveUpgrade.upgrade(root);
        assertArrayEquals(original,Files.readAllBytes(root.resolve("level.dat")));
        CompoundTag imported=NbtIo.readCompressed(root.resolve(MoonSaveUpgrade.SETTINGS),NbtAccounter.defaultQuota());
        CompoundTag type=imported.getCompoundOrEmpty("data").getCompoundOrEmpty("dimensions").getCompoundOrEmpty("reverie_dreams:the_moon").getCompoundOrEmpty("type");
        assertEquals(-128,type.getIntOr("min_y",0));assertEquals(352,type.getIntOr("height",0));assertEquals(.25f,type.getFloatOr("ambient_light",0));
        assertArrayEquals(original,Files.readAllBytes(root.resolve(MoonSaveUpgrade.BACKUP).resolve("level.dat")));
    }
    @Test void invalidRegionAllocationStopsBeforeVanillaCanDiscardIt() throws Exception {
        Path folder=root.resolve("region");Files.createDirectories(folder);Path path=folder.resolve("r.0.0.mca");
        try(var out=new DataOutputStream(Files.newOutputStream(path))){out.writeInt(0x101);out.write(new byte[8188]);}
        assertThrows(IOException.class,()->MoonLegacyMask.scan(folder));
    }
    @Test void unsupportedChunkCompressionFailsWithoutReplacingWorld() throws Exception {
        Path settings=root.resolve(MoonSaveUpgrade.SETTINGS);Files.createDirectories(settings.getParent());NbtIo.writeCompressed(legacySettings(),settings);
        Path region=root.resolve(MoonSaveUpgrade.DIMENSION).resolve("region/r.0.0.mca");Files.createDirectories(region.getParent());
        try(var out=new DataOutputStream(Files.newOutputStream(region))){out.writeInt(0x201);out.write(new byte[8188]);out.writeInt(5);out.writeByte(126);out.writeInt(0);out.write(new byte[4087]);}
        byte[] original=Files.readAllBytes(region);assertThrows(IOException.class,()->MoonSaveUpgrade.upgrade(root));assertArrayEquals(original,Files.readAllBytes(region));
        assertTrue(MoonSaveUpgrade.isLegacyGenerator(generator(NbtIo.readCompressed(settings,NbtAccounter.defaultQuota()))));
    }
    @Test void refusesCustomGeneratorUnderExpandedBuiltinTypeBeforeAnyWrites() throws Exception {
        CompoundTag file=legacySettings();generator(file).putString("type","custom:moon");
        Path settings=root.resolve(MoonSaveUpgrade.SETTINGS);Files.createDirectories(settings.getParent());NbtIo.writeCompressed(file,settings);
        Path region=root.resolve(MoonSaveUpgrade.DIMENSION).resolve("region/r.0.0.mca");Files.createDirectories(region.getParent());Files.write(region,new byte[]{1});
        byte[] original=Files.readAllBytes(settings);
        assertThrows(IOException.class,()->MoonSaveUpgrade.upgrade(root));
        assertArrayEquals(original,Files.readAllBytes(settings));assertFalse(Files.exists(root.resolve(MoonSaveUpgrade.BACKUP)));
        // Explicit custom inline type does not use the globally expanded built-in type.
        CompoundTag moon=file.getCompoundOrEmpty("data").getCompoundOrEmpty("dimensions").getCompoundOrEmpty("reverie_dreams:the_moon");
        CompoundTag inline=new CompoundTag();inline.putInt("min_y",-32);inline.putInt("height",256);moon.put("type",inline);NbtIo.writeCompressed(file,settings);
        byte[] customInline=Files.readAllBytes(settings);MoonSaveUpgrade.upgrade(root);assertArrayEquals(customInline,Files.readAllBytes(settings));
    }
    @Test void refusesLegacyGeneratorWithUnknownDimensionTypeEvenWithoutOldChunks() throws Exception {
        CompoundTag file=legacySettings();file.getCompoundOrEmpty("data").getCompoundOrEmpty("dimensions").getCompoundOrEmpty("reverie_dreams:the_moon").putString("type","custom:moon_type");
        Path settings=root.resolve(MoonSaveUpgrade.SETTINGS);Files.createDirectories(settings.getParent());NbtIo.writeCompressed(file,settings);
        byte[] original=Files.readAllBytes(settings);assertThrows(IOException.class,()->MoonSaveUpgrade.upgrade(root));assertArrayEquals(original,Files.readAllBytes(settings));
        assertFalse(Files.exists(root.resolve(MoonSaveUpgrade.BACKUP)));
    }
    @Test void realSavedWorldDimensionsCanOmitMoonAndMixedChunksConvertOnlyOldOffsets() throws Exception {
        CompoundTag file=legacySettings();file.getCompoundOrEmpty("data").getCompoundOrEmpty("dimensions").remove("reverie_dreams:the_moon");
        Path settings=root.resolve(MoonSaveUpgrade.SETTINGS);Files.createDirectories(settings.getParent());NbtIo.writeCompressed(file,settings);
        Path folder=root.resolve(MoonSaveUpgrade.DIMENSION).resolve("region");Files.createDirectories(folder);Path path=folder.resolve("r.0.0.mca");
        CompoundTag old=chunk(),expanded=chunk();MoonChunkUpgrade.upgrade(expanded);expanded.remove(MoonChunkUpgrade.MARKER);
        try(var region=new RegionFile(info(),path,folder,RegionFileVersion.VERSION_DEFLATE,true)) {
            try(var out=region.getChunkDataOutputStream(new ChunkPos(0,0))){NbtIo.write(old,out);}
            try(var out=region.getChunkDataOutputStream(new ChunkPos(1,0))){NbtIo.write(expanded,out);}
        }
        // Real saved settings contain vanilla dimensions only; the Moon comes from datapack baking.
        assertThrows(IOException.class,()->MoonSaveUpgrade.upgrade(root,false));assertFalse(Files.exists(root.resolve(MoonSaveUpgrade.BACKUP)));
        byte[] originalSettings=Files.readAllBytes(settings);MoonSaveUpgrade.upgrade(root);
        assertArrayEquals(originalSettings,Files.readAllBytes(settings));assertEquals(file,NbtIo.readCompressed(settings,NbtAccounter.defaultQuota()));
        assertEquals("2",Files.readString(root.resolve(MoonSaveUpgrade.VERSION)).trim());
        try(var region=new RegionFile(info(),path,folder,RegionFileVersion.VERSION_DEFLATE,true)) {
            try(var in=region.getChunkDataInputStream(new ChunkPos(0,0))){CompoundTag updated=NbtIo.read(in);assertEquals(-8,updated.getIntOr("yPos",0));assertEquals(old.getListOrEmpty("PostProcessing").get(0),updated.getListOrEmpty("PostProcessing").get(6));}
            try(var in=region.getChunkDataInputStream(new ChunkPos(1,0))){assertEquals(expanded,NbtIo.read(in));}
        }
    }
    @Test void unmarkedExpandedWorldGetsVersionWithoutRewritingChunks() throws Exception {
        CompoundTag file=legacySettings();file.getCompoundOrEmpty("data").getCompoundOrEmpty("dimensions").remove("reverie_dreams:the_moon");
        Path settings=root.resolve(MoonSaveUpgrade.SETTINGS);Files.createDirectories(settings.getParent());NbtIo.writeCompressed(file,settings);
        Path folder=root.resolve(MoonSaveUpgrade.DIMENSION).resolve("region");Files.createDirectories(folder);Path path=folder.resolve("r.0.0.mca");CompoundTag expanded=chunk();MoonChunkUpgrade.upgrade(expanded);expanded.remove(MoonChunkUpgrade.MARKER);
        try(var region=new RegionFile(info(),path,folder,RegionFileVersion.VERSION_DEFLATE,true);var out=region.getChunkDataOutputStream(new ChunkPos(0,0))){NbtIo.write(expanded,out);}
        byte[] before=Files.readAllBytes(path);MoonSaveUpgrade.upgrade(root);assertArrayEquals(before,Files.readAllBytes(path));assertFalse(Files.exists(root.resolve(MoonSaveUpgrade.BACKUP)));assertTrue(Files.exists(root.resolve(MoonSaveUpgrade.VERSION)));
    }
    @Test void omittedMoonMetadataResumesSwapEvenWhenDimensionIsTemporarilyMissing() throws Exception {
        CompoundTag file=legacySettings();file.getCompoundOrEmpty("data").getCompoundOrEmpty("dimensions").remove("reverie_dreams:the_moon");
        Path settings=root.resolve(MoonSaveUpgrade.SETTINGS);Files.createDirectories(settings.getParent());NbtIo.writeCompressed(file,settings);byte[] original=Files.readAllBytes(settings);
        Path backup=root.resolve(MoonSaveUpgrade.BACKUP);Files.createDirectories(backup.resolve("dimension"));Files.copy(settings,backup.resolve("world_gen_settings.dat"));
        Files.writeString(backup.resolve("staging-ready"),"complete");MoonLegacyMask.EMPTY.write(backup.resolve("legacy-mask.bin"));
        Path moon=root.resolve(MoonSaveUpgrade.DIMENSION),stage=moon.resolveSibling("the_moon.moon-v2-staging");Files.createDirectories(stage);Files.writeString(stage.resolve("converted"),"ready");
        MoonSaveUpgrade.upgrade(root);
        assertEquals("ready",Files.readString(moon.resolve("converted")));assertArrayEquals(original,Files.readAllBytes(settings));
        assertTrue(Files.exists(root.resolve(MoonLegacyMask.FILE)));assertTrue(Files.exists(root.resolve(MoonSaveUpgrade.VERSION)));assertTrue(Files.exists(backup.resolve("completed")));
    }
    @Test void restoredOldWorldCreatesAnotherBackupInsteadOfReusingCompletedTransaction() throws Exception {
        CompoundTag file=legacySettings();file.getCompoundOrEmpty("data").getCompoundOrEmpty("dimensions").remove("reverie_dreams:the_moon");
        Path settings=root.resolve(MoonSaveUpgrade.SETTINGS);Files.createDirectories(settings.getParent());NbtIo.writeCompressed(file,settings);
        Path folder=root.resolve(MoonSaveUpgrade.DIMENSION).resolve("region");Files.createDirectories(folder);Path path=folder.resolve("r.0.0.mca");
        try(var region=new RegionFile(info(),path,folder,RegionFileVersion.VERSION_DEFLATE,true);var out=region.getChunkDataOutputStream(new ChunkPos(0,0))){NbtIo.write(chunk(),out);}
        byte[] original=Files.readAllBytes(path);MoonSaveUpgrade.upgrade(root);
        Files.write(path,original);Files.delete(root.resolve(MoonSaveUpgrade.VERSION));MoonSaveUpgrade.upgrade(root);
        assertTrue(Files.exists(root.resolve(MoonSaveUpgrade.BACKUP).resolve("completed")));
        try(var backups=Files.list(root.resolve("backups"))){assertEquals(1,backups.filter(p->p.getFileName().toString().startsWith("moon-terrain-v2-history-")).count());}
        try(var region=new RegionFile(info(),path,folder,RegionFileVersion.VERSION_DEFLATE,true);var in=region.getChunkDataInputStream(new ChunkPos(0,0))){assertEquals(-8,NbtIo.read(in).getIntOr("yPos",0));}
    }
}
