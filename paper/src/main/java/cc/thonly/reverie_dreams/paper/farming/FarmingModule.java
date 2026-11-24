package cc.thonly.reverie_dreams.paper.farming;

import cc.thonly.reverie_dreams.paper.util.Ids;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.api.event.CraftEngineReloadEvent;
import net.momirealms.craftengine.bukkit.api.event.CustomBlockBreakEvent;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.UpdateFlags;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviors;
import net.momirealms.craftengine.core.block.property.Property;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.libraries.antigrieflib.Flag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.IOException;

/** Register types in onLoad, enable in onEnable. Runtime work occurs only in native CE block callbacks. */
public final class FarmingModule implements Listener {
    public static final Key PADDY_ID = Key.of(Ids.MOD_ID + ":paddy_field");
    public record PaddyBinding(BlockData nativeData, int moisture, int stateCount) {
        public PaddyBinding { nativeData = nativeData.clone(); }
        @Override public BlockData nativeData() { return nativeData.clone(); }
    }
    private static final Map<String, CropRule> CROPS = new LinkedHashMap<>();
    private static FarmingModule instance;
    private static boolean registered;
    private static volatile PaddyBinding paddy;
    private static FarmingLootBridge loot;
    private FarmingModule() { }
    public static void registerTypes() {
        if (registered) return;
        BlockBehaviors.register(PADDY_ID, PaddyFieldBehavior.FACTORY);
        BlockBehaviors.register(Key.of(Ids.MOD_ID + ":crop"), SourceCropBehavior.FACTORY);
        registered = true;
    }
    public static void enable(JavaPlugin plugin) {
        thread(); if (instance != null) return;
        try (var input = plugin.getResource("migration/farming-domain.json")) {
            if (input == null) throw new IllegalStateException("Missing farming domain");
            var root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
            if (root.get("schemaVersion").getAsInt() != 1) throw new IllegalStateException("Unsupported farming domain");
            for (var entry : root.getAsJsonObject("crops").entrySet()) {
                var row = entry.getValue().getAsJsonObject();
                CROPS.put(entry.getKey(), new CropRule(entry.getKey(), row.get("seed").getAsString(), row.get("product").getAsString(),
                        row.get("maxAge").getAsInt(), row.get("selfSeed").getAsBoolean()));
            }
        } catch (IOException failure) { throw new IllegalStateException("Cannot load farming rules", failure); }
        instance = new FarmingModule(); Bukkit.getPluginManager().registerEvents(instance, plugin);
        loot = new FarmingLootBridge(); Bukkit.getPluginManager().registerEvents(loot, plugin);
        if (CraftEngineBlocks.byId(PADDY_ID) != null) bindPaddy();
    }
    public static void disable() {
        thread(); if (instance != null) HandlerList.unregisterAll(instance);
        if (loot != null) HandlerList.unregisterAll(loot); loot = null;
        instance = null; paddy = null; CROPS.clear();
    }
    static boolean enabled() { return instance != null; }
    @EventHandler public void onReload(CraftEngineReloadEvent event) { bindPaddy(); }
    public static Map<String, CropRule> crops() { return Collections.unmodifiableMap(CROPS); }
    private static void thread() {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Farming world mutations require the Paper server thread");
    }
    public static PaddyBinding bindPaddy() {
        thread(); var block = CraftEngineBlocks.byId(PADDY_ID);
        if (block == null) throw new IllegalStateException("CE has not loaded the real paddy_field definition");
        var states = block.variantProvider().states();
        if (states.size() != 8) throw new IllegalStateException("Paddy requires all eight source moisture states");
        for (int moisture = 0; moisture <= 7; moisture++) assertPaddyRoundTrip(CraftEngineBlocks.getBukkitBlockData(paddyState(moisture)), moisture);
        var data = CraftEngineBlocks.getBukkitBlockData(block.defaultState()); assertPaddyRoundTrip(data);
        paddy = new PaddyBinding(data, 7, states.size()); return paddy;
    }
    /** Safe immutable snapshot for a chunk generator after a successful main-thread bind/reload. Never returns visual farmland. */
    public static BlockData nativePaddyData() {
        var binding = paddy;
        if (binding == null) throw new IllegalStateException("Paddy not bound after CE reload");
        return binding.nativeData();
    }
    public static ImmutableBlockState paddyState(int moisture) {
        if (moisture < 0 || moisture > 7) throw new IllegalArgumentException("Moisture outside source range");
        var block = CraftEngineBlocks.byId(PADDY_ID);
        if (block == null) throw new IllegalStateException("Paddy not loaded");
        Property<Integer> property = block.defaultState().getProperty("moisture");
        return block.defaultState().with(property, moisture);
    }
    public static void assertPaddyRoundTrip(BlockData data) { assertPaddyRoundTrip(data, 7); }
    public static void assertPaddyRoundTrip(BlockData data, int expectedMoisture) {
        var state = CraftEngineBlocks.getCustomBlockState(data);
        if (state == null || !PADDY_ID.equals(state.owner().value().id())) throw new IllegalArgumentException("BlockData is not the real custom paddy_field");
        Property<Integer> moisture = state.getProperty("moisture");
        if (moisture == null || state.get(moisture) != expectedMoisture) throw new IllegalArgumentException("Wrong source paddy moisture");
        if (!CraftEngineBlocks.getBukkitBlockData(state).matches(data)) throw new IllegalArgumentException("CE paddy native-data round trip failed");
    }
    static String id(ImmutableBlockState state) { return state == null ? "" : state.owner().value().id().asString(); }
    public static CropRule inspect(Block block) { return CROPS.get(id(CraftEngineBlocks.getCustomBlockState(block))); }
    public static int age(Block block) {
        var state = CraftEngineBlocks.getCustomBlockState(block);
        if (state == null || !CROPS.containsKey(id(state))) return -1;
        Property<Integer> age = state.getProperty("age"); return state.get(age);
    }
    public static boolean isSoil(Block block) {
        return block.getType() == Material.FARMLAND || PADDY_ID.asString().equals(id(CraftEngineBlocks.getCustomBlockState(block)));
    }
    static Block loadedRelative(Block block, int x, int y, int z) {
        int bx = block.getX() + x, bz = block.getZ() + z, by = block.getY() + y;
        var world = block.getWorld();
        if (by < world.getMinHeight() || by >= world.getMaxHeight() || !world.isChunkLoaded(bx >> 4, bz >> 4)) return null;
        return world.getBlockAt(bx, by, bz);
    }
    public static float sourceMoisture(Block crop) {
        float factor = 1.0f;
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            var soil = loadedRelative(crop, x, -1, z); float bonus = 0;
            // Source explicitly uses state.is(Blocks.FARMLAND), so a paddy moisture7 contributes ZERO here.
            if (soil != null && soil.getType() == Material.FARMLAND) bonus = ((Farmland) soil.getBlockData()).getMoisture() > 0 ? 3.0f : 1.0f;
            if (x != 0 || z != 0) bonus /= 4.0f;
            factor += bonus;
        }
        String cropId = id(CraftEngineBlocks.getCustomBlockState(crop));
        boolean x = same(crop, cropId, -1, 0) || same(crop, cropId, 1, 0);
        boolean z = same(crop, cropId, 0, -1) || same(crop, cropId, 0, 1);
        if (x && z || same(crop, cropId, -1, -1) || same(crop, cropId, 1, -1)
                || same(crop, cropId, 1, 1) || same(crop, cropId, -1, 1)) factor /= 2.0f;
        return factor;
    }
    private static boolean same(Block block, String id, int x, int z) {
        var other = loadedRelative(block, x, 0, z);
        return other != null && id.equals(id(CraftEngineBlocks.getCustomBlockState(other)));
    }
    public static int growthDenominator(Block block) { return (int) (15.0f / sourceMoisture(block)) + 1; }
    /** Deterministic callback seam: draw is a native-style uniform integer in [0, growthDenominator). */
    public static boolean tryRandomTick(Block block, int rawBrightness, int draw) {
        thread(); var rule = inspect(block);
        if (!enabled() || rule == null || age(block) >= rule.maxAge() || rawBrightness < 9) return false;
        if (draw < 0 || draw >= growthDenominator(block)) throw new IllegalArgumentException("Random draw outside source denominator");
        return draw == 0 && advance(block, 1);
    }
    public static boolean advance(Block block, int delta) {
        thread(); var rule = inspect(block); if (rule == null || delta <= 0) return false;
        var before = CraftEngineBlocks.getCustomBlockState(block); Property<Integer> property = before.getProperty("age");
        int age = before.get(property); if (age >= rule.maxAge()) return false;
        var soil = loadedRelative(block, 0, -1, 0); if (soil == null || !isSoil(soil)) return false;
        var next = before.with(property, Math.min(rule.maxAge(), age + delta));
        var snapshot = block.getState(); snapshot.setBlockData(CraftEngineBlocks.getBukkitBlockData(next));
        var event = new BlockGrowEvent(block, snapshot); Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() || !before.equals(CraftEngineBlocks.getCustomBlockState(block))) return false;
        return event.getNewState().update(true, false);
    }
    public static boolean boneMeal(Block block, int sourceBonus) {
        if (sourceBonus < 1 || sourceBonus > 3) throw new IllegalArgumentException("Source bone meal bonus must be 1..3");
        boolean success = advance(block, sourceBonus);
        if (success) block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().add(.5, .5, .5), 15, .25, .25, .25);
        return success;
    }
    public static boolean harvest(Block block, Player player) {
        thread(); var rule = inspect(block); if (rule == null || age(block) < rule.maxAge()) return false;
        var before = CraftEngineBlocks.getCustomBlockState(block);
        if (player != null) {
            if (!BukkitCraftEngine.instance().antiGriefProvider().test(player, Flag.BREAK, block.getLocation())) return false;
            var event = new CustomBlockBreakEvent(BukkitAdaptor.adapt(player), block.getLocation(), block, before, true);
            Bukkit.getPluginManager().callEvent(event); if (event.isCancelled()) return false;
            // Source creative right-click also drops the mature crop; event may explicitly suppress drops.
            if (!before.equals(CraftEngineBlocks.getCustomBlockState(block))) return false;
            if (!CraftEngineBlocks.remove(block, player, false, event.dropItems(), true)) return false;
        } else if (!CraftEngineBlocks.remove(block, null, false, true, true)) return false;
        return CraftEngineBlocks.place(block.getLocation(), before.owner().value().defaultState(), UpdateFlags.UPDATE_ALL, false);
    }
    public static boolean checkSupport(Block block) {
        thread(); if (inspect(block) == null) return false;
        var soil = loadedRelative(block, 0, -1, 0); if (soil == null || isSoil(soil)) return false;
        return CraftEngineBlocks.remove(block, null, false, true, true);
    }
}
