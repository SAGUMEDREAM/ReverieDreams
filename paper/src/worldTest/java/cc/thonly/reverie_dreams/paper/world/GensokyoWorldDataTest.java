package cc.thonly.reverie_dreams.paper.world;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Random;
import org.bukkit.block.data.BlockData;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;

import static org.junit.jupiter.api.Assertions.*;

class GensokyoWorldDataTest {
    @TempDir Path temporary;

    @Test void sourceDeltaNibbleOrderAndAuthoredWaterArePreserved() throws Exception {
        Path source = fixture("roundtrip", 3, 3, new short[]{1100,1024,1150,1099,1024,1151,1098,1024,1152},
                new int[]{0,1,2,0,1,2,0,1,2});
        Path cache = temporary.resolve("cache");
        var marker = GensokyoMapCache.prepare(source, cache);
        assertEquals(45, marker.get("bytes").getAsLong());
        try (var map = GensokyoMapCache.open(source, cache)) {
            assertEquals(1098, map.heightAt(0, 2));
            assertEquals(1152, map.heightAt(2, 2));
            assertEquals("minecraft:water", map.surfaceAt(1, 2));
            assertEquals("minecraft:lava", map.surfaceAt(2, 2));
            assertEquals(1, map.biomeAt(1, 2));
            var terrain = new GensokyoTerrain(map);
            assertEquals("minecraft:water", terrain.stateAt(1, 1022, 0));
            assertEquals("minecraft:clay", terrain.stateAt(1, 1021, 0));
            assertEquals("minecraft:clay", terrain.stateAt(1, 1020, 0));
            assertEquals("minecraft:stone", terrain.stateAt(1, 1019, 0));
            assertEquals("minecraft:stone", terrain.stateAt(2, 1054, 0));
            assertEquals("minecraft:lava", terrain.stateAt(2, 1055, 0));
            assertEquals("minecraft:air", terrain.stateAt(0, 1100, 0));
            assertEquals("minecraft:water[level=8]", terrain.stateAt(2, 1105, 0));
            assertTrue(terrain.column(2, 0).stream().anyMatch(r -> r.state().equals("minecraft:lava") && r.minY() <= 1105 && r.maxYExclusive() > 1105));
        }
    }

    @Test void gzipTruncationAndExtraPaletteBytesCannotPublishACache() throws Exception {
        Path source = fixture("truncated", 3, 3, filled(9, 1100), new int[9]);
        Path layer = source.resolve("heightmap.delta.gz");
        byte[] content = Files.readAllBytes(layer);
        Files.write(layer, Arrays.copyOf(content, content.length - 5));
        Path cache = temporary.resolve("broken-cache");
        assertThrows(IOException.class, () -> GensokyoMapCache.prepare(source, cache));
        assertFalse(Files.exists(cache.resolve("manifest.json")));
        Path extra = fixture("extra", 3, 3, filled(9, 1100), new int[9]);
        gzip(extra.resolve("biomes.pal4.gz"), new byte[]{0,0,0,0,0,0});
        assertThrows(IOException.class, () -> GensokyoMapCache.prepare(extra, temporary.resolve("extra-cache")));
    }

    @Test void rawByteBiomesPreserveHighUnsignedIndicesAndRejectWrongLength() throws Exception {
        Path source = fixture("raw-biomes", 3, 1, filled(3, 1100), new int[3]);
        Path metadata = source.resolve("map.json");
        var root = JsonParser.parseString(Files.readString(metadata)).getAsJsonObject();
        var palette = new JsonArray();
        for (int i = 0; i < 256; i++) palette.add("minecraft:plains");
        root.add("biome_palette", palette);
        root.getAsJsonObject("layers").addProperty("biomes", "biomes.bin.gz");
        Files.writeString(metadata, root.toString());
        gzip(source.resolve("biomes.bin.gz"), new byte[]{16, 75, (byte)255});
        Path cache = temporary.resolve("raw-biomes-cache");
        GensokyoMapCache.prepare(source, cache);
        try (var data = GensokyoMapCache.open(source, cache)) {
            assertEquals(16, data.biomeAt(0, 0));
            assertEquals(75, data.biomeAt(1, 0));
            assertEquals(255, data.biomeAt(2, 0));
            assertEquals(1, data.manifest().getAsJsonArray("biome_counts").get(255).getAsLong());
        }
        for (byte[] invalid : new byte[][]{new byte[]{16, 75}, new byte[]{16, 75, 0, 0}}) {
            gzip(source.resolve("biomes.bin.gz"), invalid);
            assertThrows(IOException.class, () -> GensokyoMapCache.prepare(source, temporary.resolve("bad-raw-" + invalid.length)));
        }
    }

    @Test void corruptedCacheAndChangedUndergroundAreDetected() throws Exception {
        Path source = fixture("integrity", 3, 3, filled(9, 1100), new int[9]);
        Path cache = temporary.resolve("integrity-cache");
        GensokyoMapCache.prepare(source, cache);
        byte[] raw = Files.readAllBytes(cache.resolve("columns.bin"));
        raw[0] ^= 1;
        Files.write(cache.resolve("columns.bin"), raw);
        assertThrows(IOException.class, () -> GensokyoMapCache.open(source, cache));
        GensokyoMapCache.prepare(source, cache);
        try (var data = GensokyoMapCache.open(source, cache)) { assertEquals(1100, data.heightAt(0, 0)); }
        Files.writeString(source.resolve("underground.json"), "{\"format_version\":1,\"blocks\":[]}");
        assertThrows(IOException.class, () -> GensokyoMapCache.open(source, cache));
    }

    @Test void sourceBorderGoldenVectorsAndFullHeightBarrierRemainUnscaled() {
        var terrain = new GensokyoTerrain(constantColumns("minecraft:grass_block", 1100));
        // Generated independently from the unchanged original private source
        // methods, with only its Minecraft data holders replaced by fixed stubs.
        int[][] expected = {{-1,5120,1100},{-16,5120,1100},{-100,5120,1115},{-320,5120,1220},
                {-640,5120,1324},{12800,5120,1100},{13439,5120,1323},{6400,-640,1358},{6400,10879,1363}};
        String[] tops = {"grass_block","grass_block","stone","stone","stone","grass_block","stone","stone","snow_block"};
        assertEquals(640, terrain.borderWidth());
        for (int i = 0; i < expected.length; i++) {
            int[] p = expected[i];
            assertEquals(p[2], terrain.borderHeight(p[0], p[1]), Arrays.toString(p));
            assertEquals("minecraft:" + tops[i], terrain.stateAt(p[0], p[2], p[1]));
        }
        assertEquals("minecraft:barrier", terrain.stateAt(-1, 0, 5120));
        assertEquals("minecraft:dirt", terrain.stateAt(-1, 1097, 5120));
        assertEquals("minecraft:barrier", terrain.stateAt(-1, 1101, 5120));
        assertEquals("minecraft:barrier", terrain.stateAt(-641, 0, 5120));
        assertEquals("minecraft:barrier", terrain.stateAt(-641, 1535, 5120));
        assertEquals(1535, terrain.baseHeight(-1, 5120, true, false));
    }

    @Test void sourceRiverbedGoldenVectorsAndCompleteShoreRingsArePreserved() {
        int[][] values = {{0,0,4,2},{1,0,4,2},{6400,5120,7,1},{12799,10239,6,0},{-64,-64,4,0}};
        for (int[] p : values) {
            assertEquals(p[2], GensokyoRiverbed.depthAt(p[0], p[1]));
            assertEquals(p[3], GensokyoRiverbed.materialAt(p[0], p[1]));
        }
        assertEquals(3, GensokyoRiverbed.depthAt(6400, 5120, (x,z) -> !(x == 6401 && z == 5121)));
        assertEquals(4, GensokyoRiverbed.depthAt(6400, 5120, (x,z) -> !(x == 6402 && z == 5122)));
    }

    @Test void sourceSentinelAndAirBranchesAreNotReplacedByAGenericFlatColumn() throws Exception {
        Path source = fixture("sentinel", 3, 1, new short[]{Short.MIN_VALUE, 0, 1535}, new int[]{0,0,3});
        Path cache = temporary.resolve("sentinel-cache");
        GensokyoMapCache.prepare(source, cache);
        try (var data = GensokyoMapCache.open(source, cache)) {
            var terrain = new GensokyoTerrain(data);
            assertEquals("minecraft:grass_block", terrain.stateAt(0, 1024, 0));
            assertEquals(0, terrain.baseHeight(0, 0, true, false));
            assertEquals("minecraft:grass_block", terrain.stateAt(1, 0, 0));
            assertEquals("minecraft:water", terrain.stateAt(1, 1, 0));
            assertTrue(terrain.column(2, 0).isEmpty());
        }
    }

    @Test void stripeCacheRetainsAtMostItsBoundAcrossRandomRows() throws Exception {
        int count = 3 * 16 * 20;
        Path source = fixture("stripes", 3, 16 * 20, filled(count, 1100), new int[count]);
        Path cache = temporary.resolve("stripe-cache");
        GensokyoMapCache.prepare(source, cache);
        try (var data = GensokyoMapCache.open(source, cache)) {
            for (int z = 0; z < 320; z += 16) assertEquals(1100, data.heightAt(0, z));
            assertEquals(16 * 16 * 3 * 5, data.retainedBytes());
        }
    }

    @Test void wrongHeightAndEscapingLayerPathsAreRejectedBeforeAllocation() throws Exception {
        Path source = fixture("header", 3, 3, filled(9,1100), new int[9]);
        JsonObject root = JsonParser.parseString(Files.readString(source.resolve("map.json"))).getAsJsonObject();
        root.addProperty("world_height",384);
        Files.writeString(source.resolve("map.json"),root.toString());
        assertThrows(IOException.class, () -> GensokyoMap.read(source));
        root.addProperty("world_height",1536);
        root.getAsJsonObject("layers").addProperty("heightmap","../other.delta.gz");
        Files.writeString(source.resolve("map.json"),root.toString());
        assertThrows(IOException.class, () -> GensokyoMap.read(source));
    }

    @Test void finalFeatureStageRestoresExactAuthoredAirAndFallingWater() throws Exception {
        Path source = fixture("feature-restoration", 3, 3, filled(9, 1100), new int[9]);
        Path prepared = temporary.resolve("feature-restoration-cache");
        GensokyoMapCache.prepare(source, prepared);
        try (var cache = GensokyoMapCache.open(source, prepared)) {
            var values = new HashMap<String, String>();
            values.put("0,1100,0", "minecraft:stone"); values.put("2,1105,0", "minecraft:stone");
            var region = (LimitedRegion) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{LimitedRegion.class}, (proxy, method, args) -> {
                if (method.getName().equals("isInRegion")) return true;
                if (!method.getName().equals("setBlockData")) throw new UnsupportedOperationException(method.getName());
                values.put(args[0] + "," + args[1] + "," + args[2], ((BlockData) args[3]).getAsString()); return null;
            });
            var info = (WorldInfo) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{WorldInfo.class}, (proxy, method, args) -> switch (method.getName()) {
                case "getMinHeight" -> 0; case "getMaxHeight" -> 1536; default -> throw new UnsupportedOperationException(method.getName());
            });
            var stateTable = new HashMap<String, BlockData>();
            for (String encoded : List.of("minecraft:air", "minecraft:water[level=8]")) stateTable.put(encoded,
                    (BlockData) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{BlockData.class}, (proxy, method, args) -> {
                        if (method.getName().equals("getAsString")) return encoded;
                        throw new UnsupportedOperationException(method.getName());
                    }));
            new GensokyoFeaturePopulator(cache, null, Map::of, () -> Map.copyOf(stateTable), false, false).populate(info, new Random(0), 0, 0, region);
            assertEquals("minecraft:air", values.get("0,1100,0"));
            assertEquals("minecraft:water[level=8]", values.get("2,1105,0"));
        }
    }

    @Test void neighboringFeatureStageRestoresAuthoredBorderWithinItsOwnWriteRegion() throws Exception {
        Path source = fixture("neighbor-priority", 48, 1, filled(48, 1100), new int[48]);
        Files.writeString(source.resolve("underground.json"), """
                {"format_version":1,"blocks":[
                 {"pos":[15,1100,0],"block":"minecraft:gravel"},
                 {"pos":[16,1100,0],"block":"minecraft:air"},
                 {"pos":[32,1100,0],"block":"minecraft:water[level=8]"}]}
                """);
        Path prepared = temporary.resolve("neighbor-priority-cache"); GensokyoMapCache.prepare(source, prepared);
        try (var cache = GensokyoMapCache.open(source, prepared)) {
            var values = new HashMap<Integer, String>();
            for (int x : new int[]{15,16,32}) values.put(x,"minecraft:stone");
            int[] bounds = {-16,32};
            var region = (LimitedRegion) Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{LimitedRegion.class},(proxy,method,args)-> {
                if (method.getName().equals("isInRegion")) return (int)args[0]>=bounds[0] && (int)args[0]<bounds[1];
                if (method.getName().equals("setBlockData")) { values.put((int)args[0],((BlockData)args[3]).getAsString());return null; }
                throw new UnsupportedOperationException(method.getName());
            });
            var states = new HashMap<String,BlockData>();
            for (String encoded : List.of("minecraft:gravel","minecraft:air","minecraft:water[level=8]"))
                states.put(encoded,(BlockData)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{BlockData.class},(proxy,method,args)->{
                    if(method.getName().equals("getAsString"))return encoded;throw new UnsupportedOperationException(method.getName());
                }));
            var info = (WorldInfo)Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{WorldInfo.class},(proxy,method,args)->switch(method.getName()){
                case "getMinHeight"->0;case "getMaxHeight"->1536;default->throw new UnsupportedOperationException(method.getName());
            });
            var stage = new GensokyoFeaturePopulator(cache,null,Map::of,()->Map.copyOf(states),false,false);
            stage.populate(info,new Random(0),0,0,region);
            assertEquals("minecraft:gravel",values.get(15));assertEquals("minecraft:air",values.get(16));
            assertEquals("minecraft:stone",values.get(32),"Never write outside the current LimitedRegion");
            values.put(15,"minecraft:stone");bounds[0]=0;bounds[1]=48;
            stage.populate(info,new Random(0),1,0,region);
            assertEquals("minecraft:gravel",values.get(15),"Later east-neighbor stage reasserts authored west boundary");
            assertEquals("minecraft:water[level=8]",values.get(32));
        }
    }

    private Path fixture(String name, int width, int depth, short[] heights, int[] surfaces) throws Exception {
        Path source = temporary.resolve(name); Files.createDirectories(source);
        JsonObject root = new JsonObject();
        root.addProperty("format_version",1);root.addProperty("map_version",2);root.addProperty("id","reverie_dreams:gensokyo");
        root.addProperty("width",width);root.addProperty("depth",depth);root.add("origin",array(0,0));
        root.addProperty("min_y",0);root.addProperty("world_height",1536);root.addProperty("sea_level",1024);
        root.addProperty("dirt_depth",3);root.addProperty("biome_transition",0);
        JsonObject spawn = new JsonObject();spawn.add("pos",array(0,1064,0));root.add("spawn",spawn);
        root.add("biome_palette",strings("minecraft:plains","minecraft:river"));
        root.add("surface_palette",strings("minecraft:grass_block","minecraft:water","minecraft:lava","minecraft:air"));
        JsonObject layers = new JsonObject();layers.addProperty("heightmap","heightmap.delta.gz");layers.addProperty("biomes","biomes.pal4.gz");layers.addProperty("surfaces","surfaces.pal4.gz");root.add("layers",layers);
        root.addProperty("underground","underground.json");root.add("anchors",new JsonArray());
        Files.writeString(source.resolve("map.json"),root.toString());
        JsonObject under = new JsonObject();under.addProperty("format_version",1);JsonArray blocks = new JsonArray();
        if (depth > 1) {
            JsonObject a = new JsonObject();a.add("pos",array(0,1100,0));a.addProperty("block","minecraft:air");blocks.add(a);
            JsonObject b = new JsonObject();b.add("pos",array(2,1105,0));b.addProperty("block","minecraft:water[level=8]");blocks.add(b);
        }
        under.add("blocks",blocks);Files.writeString(source.resolve("underground.json"),under.toString());
        ByteArrayOutputStream heightBytes = new ByteArrayOutputStream();
        for (int i=0;i<heights.length;i++) {
            short value = (short)(heights[i]-(i>=width?heights[i-width]:0));
            heightBytes.write(value & 255);heightBytes.write((value>>>8)&255);
        }
        int[] biomes = new int[heights.length];for(int i=0;i<biomes.length;i++)biomes[i]=i%width==1?1:0;
        gzip(source.resolve("heightmap.delta.gz"),heightBytes.toByteArray());
        gzip(source.resolve("biomes.pal4.gz"),packed(biomes));gzip(source.resolve("surfaces.pal4.gz"),packed(surfaces));
        return source;
    }
    private static byte[] packed(int[] values) { byte[] result=new byte[(values.length+1)/2];for(int i=0;i<values.length;i++)result[i/2]|=(byte)(values[i]<<((i%2==0)?4:0));if(values.length%2==1)result[result.length-1]|=15;return result; }
    private static void gzip(Path path, byte[] value) throws Exception { try(var out=new GZIPOutputStream(Files.newOutputStream(path))){out.write(value);} }
    private static JsonArray array(int...values){var result=new JsonArray();for(int v:values)result.add(v);return result;}
    private static JsonArray strings(String...values){var result=new JsonArray();for(String v:values)result.add(v);return result;}
    private static short[] filled(int size,int value){short[] result=new short[size];Arrays.fill(result,(short)value);return result;}
    private static GensokyoColumns constantColumns(String surface,int height){
        GensokyoMap map=new GensokyoMap(1,2,"reverie_dreams:gensokyo",12800,10240,0,0,0,1536,1024,3,0,
                new GensokyoMap.Spawn(6400,1064,5120,0,0),List.of("minecraft:plains"),List.of(surface),
                "heightmap.delta.gz","biomes.pal4.gz","surfaces.pal4.gz",null,0,Map.of());
        return new GensokyoColumns(){public GensokyoMap map(){return map;}public int heightAt(int x,int z){return map.contains(x,z)?height:GensokyoMap.NO_SURFACE;}public int biomeAt(int x,int z){return 0;}public String surfaceAt(int x,int z){return map.contains(x,z)?surface:"minecraft:air";}};
    }
}
