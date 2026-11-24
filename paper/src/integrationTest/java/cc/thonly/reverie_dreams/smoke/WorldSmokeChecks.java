package cc.thonly.reverie_dreams.smoke;

import cc.thonly.reverie_dreams.paper.farming.FarmingModule;
import cc.thonly.reverie_dreams.paper.world.GensokyoGenerator;
import cc.thonly.reverie_dreams.paper.world.GensokyoWorldPlugin;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.core.util.Key;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.concurrent.CompletableFuture;
import org.bukkit.plugin.java.JavaPlugin;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.bukkit.Material;

/** Loaded only when the independently packaged world bootstrap is installed by the smoke runner. */
final class WorldSmokeChecks {
    private static JsonObject forestSample;
    static CompletableFuture<Void> prepare(JavaPlugin probe) throws Exception {
        World world = Objects.requireNonNull(Bukkit.getWorld(NamespacedKey.fromString("reverie_dreams:gensokyo")), "Gensokyo datapack world absent");
        var chunks = new LinkedHashSet<Long>();
        for (String file : List.of("gensokyo-native-points.json", "gensokyo-paddy-point.json", "gensokyo-active-biome-points.json")) {
            if (!Files.isRegularFile(Path.of(file))) continue;
            var fixture = JsonParser.parseString(Files.readString(Path.of(file))).getAsJsonObject();
            for (var value : fixture.getAsJsonArray("points")) {
                var point = value.getAsJsonObject(); int x = point.get("x").getAsInt() >> 4, z = point.get("z").getAsInt() >> 4;
                chunks.add(((long) x << 32) | (z & 0xffffffffL));
            }
        }
        var oreFile = Path.of("gensokyo-first-ore-veins-seed-123456789.json");
        if (Files.isRegularFile(oreFile)) for (var value : JsonParser.parseString(Files.readString(oreFile)).getAsJsonObject().getAsJsonArray("veins")) {
            for (var candidate : value.getAsJsonObject().getAsJsonArray("candidates")) {
                var point = candidate.getAsJsonObject(); int x = point.get("x").getAsInt() >> 4, z = point.get("z").getAsInt() >> 4;
                chunks.add(((long) x << 32) | (z & 0xffffffffL));
            }
        }
        var underground = Path.of("gensokyo-authored-underground.json");
        if (Files.isRegularFile(underground)) for (var value : JsonParser.parseString(Files.readString(underground)).getAsJsonObject().getAsJsonArray("blocks")) {
            var pos = value.getAsJsonObject().getAsJsonArray("pos"); int x = pos.get(0).getAsInt() >> 4, z = pos.get(2).getAsInt() >> 4;
            chunks.add(((long) x << 32) | (z & 0xffffffffL));
        }
        // Paper completes these futures on the main thread. Tickets retain earlier chunks while remote ones generate.
        var futures = chunks.stream().map(key -> world.getChunkAtAsync((int) (key >> 32), key.intValue(), true)
                .thenAccept(chunk -> chunk.addPluginChunkTicket(probe))).toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures).thenCompose(unused -> {
            var generator = (GensokyoGenerator) world.getGenerator();
            if (!generator.sourceForestTreesEnabled()) return CompletableFuture.completedFuture(null);
            try {
                Path observed = Path.of("gensokyo-forest-observed.json");
                if (Files.isRegularFile(observed)) forestSample = JsonParser.parseString(Files.readString(observed)).getAsJsonObject();
                else {
                    var sample = Objects.requireNonNull(generator.sourceForestSample(), "No source tree was committed");
                    forestSample = new JsonObject();forestSample.addProperty("profile", sample.profile());
                    forestSample.addProperty("x", sample.x());forestSample.addProperty("y", sample.y());forestSample.addProperty("z", sample.z());
                    forestSample.addProperty("state", sample.nativeState());
                }
                return world.getChunkAtAsync(forestSample.get("x").getAsInt() >> 4, forestSample.get("z").getAsInt() >> 4, true)
                        .thenAccept(chunk -> chunk.addPluginChunkTicket(probe));
            } catch (Exception failure) { return CompletableFuture.failedFuture(failure); }
        });
    }
    static void verify(BiConsumer<Boolean, String> check) throws Exception {
        World world = Bukkit.getWorld(NamespacedKey.fromString("reverie_dreams:gensokyo"));
        check.accept(world != null && world.getGenerator() instanceof GensokyoGenerator, "datapack Gensokyo dimension uses the independent map generator");
        check.accept(world.getMinHeight() == 0 && world.getMaxHeight() == 1536 && world.getSeaLevel() == 1024,
                "real Paper world retains original 1536 height and 1024 sea level");
        var generator = (GensokyoGenerator) world.getGenerator();
        check.accept(world.getSeed() == 123456789L, "world feature probes use the documented fixed seed");
        check.accept(generator.unresolved().isEmpty(), "complete actual source surface palette binds before world entry");
        var plugin = (GensokyoWorldPlugin) Bukkit.getPluginManager().getPlugin("ReverieDreamsWorld");
        boolean forestEnabled = plugin.getConfig().getBoolean("features.source-forest-trees", false);
        check.accept(generator.sourceForestTreesEnabled() == forestEnabled, "source forest stage follows its explicit opt-in switch");
        if (forestEnabled) {
            if (!Files.isRegularFile(Path.of("gensokyo-forest-observed.json"))) check.accept(generator.sourceForestTreesGenerated() > 0,
                    "source forest planner committed real complete trees through native LimitedRegion");
            var sample = Objects.requireNonNull(forestSample);
            check.accept(world.getBlockAt(sample.get("x").getAsInt(), sample.get("y").getAsInt(), sample.get("z").getAsInt())
                            .getBlockData().getAsString().equals(sample.get("state").getAsString()),
                    "real source-profile tree log remains exact through generation and restart: " + sample);
            Files.writeString(Path.of("gensokyo-forest-observed.json"), sample.toString());
        }
        else check.accept(generator.sourceForestTreesGenerated() == 0, "disabled source forest stage does not substitute native trees");
        check.accept(plugin.requestedCustomStates().equals(Set.of("reverie_dreams:paddy_field")), "generator requests exactly the custom paddy used by the actual source map");
        check.accept(world.getSpawnLocation().getBlockX() == 6400 && world.getSpawnLocation().getBlockY() == 1064 && world.getSpawnLocation().getBlockZ() == 5120,
                "source spawn retains original unscaled coordinates");
        var fixture = JsonParser.parseString(Files.readString(Path.of("gensokyo-native-points.json"))).getAsJsonObject();
        for (var point : fixture.getAsJsonArray("points")) verifyPoint(check, world, point.getAsJsonObject());
        var paddy = JsonParser.parseString(Files.readString(Path.of("gensokyo-paddy-point.json"))).getAsJsonObject().getAsJsonArray("points").get(0).getAsJsonObject();
        // Coordinate is independently derived from the unmodified, SHA-locked source column cache.
        int x = paddy.get("x").getAsInt(), z = paddy.get("z").getAsInt(), y = paddy.get("source_height").getAsInt();
        check.accept(world.isChunkLoaded(x >> 4, z >> 4), "custom surface chunk was asynchronously prepared");
        FarmingModule.assertPaddyRoundTrip(world.getBlockAt(x, y, z).getBlockData());
        check.accept(true, "first generated paddy uses the real CE block with original moisture seven");
        check.accept(world.getBlockAt(x, y - 1, z).getType() == Material.DIRT,
                "custom source surface retains original neighboring terrain");
        if (!mode().equals("terrain")) verifyFeatures(check, world, plugin);
    }
    private static void verifyPoint(BiConsumer<Boolean, String> check, World world, JsonObject point) {
        int x = point.get("x").getAsInt(), z = point.get("z").getAsInt();
        check.accept(world.isChunkLoaded(x >> 4, z >> 4), "native reference chunk was asynchronously prepared: " + x + "," + z);
        for (var value : point.getAsJsonArray("blocks")) {
            var block = value.getAsJsonObject(); int y = block.get("y").getAsInt();
            String expected = Bukkit.createBlockData(block.get("state").getAsString()).getAsString();
            if (mode().equals("all") && y != 0 && y != 1535) continue;
            if (mode().equals("ore") && expected.matches("minecraft:(stone|deepslate)(\\[.*)?")) continue;
            check.accept(expected.equals(world.getBlockAt(x, y, z).getBlockData().getAsString()), "original full terrain block state at " + x + "," + y + "," + z);
        }
        check.accept(world.getBiome(x, point.get("source_height").getAsInt(), z).getKey().asString().equals(point.get("generator_biome").getAsString()),
                "source quart biome preserved at " + x + "," + z);
    }
    private static String mode() {
        try { return Files.readString(Path.of("world-feature-mode.txt")).strip(); }
        catch (Exception missing) { return "terrain"; }
    }
    private static void verifyFeatures(BiConsumer<Boolean, String> check, World world, GensokyoWorldPlugin plugin) throws Exception {
        check.accept(plugin.requestedFeatureStates().equals(Set.of("reverie_dreams:silver_ore", "reverie_dreams:deepslate_silver_ore", "reverie_dreams:orb_ore", "reverie_dreams:deepslate_orb_ore")),
                "all custom worldgen ore states are explicitly verified before feature execution");
        var biomes = JsonParser.parseString(Files.readString(Path.of("gensokyo-active-biome-points.json"))).getAsJsonObject();
        var covered = new HashSet<String>();
        for (var value : biomes.getAsJsonArray("points")) {
            var point = value.getAsJsonObject(); int x = point.get("x").getAsInt(), z = point.get("z").getAsInt(), y = point.get("source_height").getAsInt();
            String expected = point.get("generator_biome").getAsString();
            check.accept(world.getBiome(x, y, z).getKey().asString().equals(expected), "active source biome supports real feature generation: " + expected);
            covered.add(expected);
        }
        check.accept(covered.size() == 11, "feature validation covers all eleven biomes actually used by source map");
        var underground = JsonParser.parseString(Files.readString(Path.of("gensokyo-authored-underground.json"))).getAsJsonObject().getAsJsonArray("blocks");
        for (var value : underground) {
            var row = value.getAsJsonObject(); var pos = row.getAsJsonArray("pos");
            int x = pos.get(0).getAsInt(), y = pos.get(1).getAsInt(), z = pos.get(2).getAsInt();
            String expected = Bukkit.createBlockData(row.get("block").getAsString()).getAsString();
            var block = world.getBlockAt(x, y, z);
            String actual = block.getBlockData().getAsString();
            if (!actual.equals(expected)) {
                var custom = CraftEngineBlocks.getCustomBlockState(block);
                check.accept(false, "authored state mismatch at " + x + "," + y + "," + z + ": expected=" + expected
                        + ", actual=" + actual + ", custom=" + (custom == null ? "none" : custom.owner().value().id()));
            }
            check.accept(true, "authored underground full state preserved after features: " + x + "," + y + "," + z);
        }
        var observedFile = Path.of("gensokyo-observed-ores.json");
        if (Files.isRegularFile(observedFile)) {
            for (var value : JsonParser.parseString(Files.readString(observedFile)).getAsJsonArray()) {
                var point = value.getAsJsonObject(); var state = CraftEngineBlocks.getCustomBlockState(world.getBlockAt(point.get("x").getAsInt(), point.get("y").getAsInt(), point.get("z").getAsInt()));
                check.accept(state != null && state.owner().value() == CraftEngineBlocks.byId(Key.of(point.get("source_ir").getAsString())), "generated real custom ore survives restart: " + point);
            }
            return;
        }
        var veins = JsonParser.parseString(Files.readString(Path.of("gensokyo-first-ore-veins-seed-123456789.json"))).getAsJsonObject().getAsJsonArray("veins");
        var observed = new JsonArray(); var types = new HashSet<String>();
        var positions = new HashSet<String>(); boolean adjacent = false;
        for (var value : veins) for (var candidate : value.getAsJsonObject().getAsJsonArray("candidates")) {
            var point = candidate.getAsJsonObject(); int x = point.get("x").getAsInt(), y = point.get("y").getAsInt(), z = point.get("z").getAsInt();
            var actual = CraftEngineBlocks.getCustomBlockState(world.getBlockAt(x, y, z)); String expected = point.get("source_ir").getAsString();
            if (actual == null || actual.owner().value() != CraftEngineBlocks.byId(Key.of(expected))) continue;
            String key = x + "," + y + "," + z;
            if (!positions.add(key)) continue;
            observed.add(point.deepCopy()); types.add(expected);
            for (var offset : List.of(new int[]{1,0,0},new int[]{-1,0,0},new int[]{0,1,0},new int[]{0,-1,0},new int[]{0,0,1},new int[]{0,0,-1})) {
                if (positions.contains((x + offset[0]) + "," + (y + offset[1]) + "," + (z + offset[2]))) adjacent = true;
            }
        }
        check.accept(types.containsAll(Set.of("reverie_dreams:silver_ore", "reverie_dreams:orb_ore")) && adjacent,
                "source band geometry produces real CE silver and orb veins with neighboring members");
        Files.writeString(observedFile, new GsonBuilder().setPrettyPrinting().create().toJson(observed) + "\n");
        check.accept(!observed.isEmpty(), "generated ore ownership snapshot saved for real restart comparison");
    }
}
