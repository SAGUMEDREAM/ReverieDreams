package cc.thonly.reverie_dreams.paper.plants;

import cc.thonly.reverie_dreams.paper.util.Ids;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviors;
import net.momirealms.craftengine.core.block.property.Property;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.libraries.antigrieflib.Flag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import net.momirealms.craftengine.bukkit.world.BukkitWorldManager;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;

/** Register factories during onLoad; activate callbacks during onEnable after CE's dependency. */
public final class PlantsModule {
    private static final Map<String, String> FRUIT = Map.of(
            Ids.MOD_ID + ":lemon_fruit_leaves", Ids.MOD_ID + ":ingredient/lemon",
            Ids.MOD_ID + ":ginkgo_fruit_leaves", Ids.MOD_ID + ":ingredient/ginkgo",
            Ids.MOD_ID + ":peach_fruit_leaves", Ids.MOD_ID + ":ingredient/peach");
    private static boolean registered;
    private static volatile boolean enabled;
    private static PlantFlowerInteractions flowerInteractions;
    private PlantsModule() { }
    public static void registerTypes() {
        if (registered) return;
        BlockBehaviors.register(Key.of(Ids.MOD_ID + ":fruit_leaves"), FruitLeavesBehavior.FACTORY);
        BlockBehaviors.register(Key.of(Ids.MOD_ID + ":fertilizable_flower"), FertilizableFlowerBehavior.FACTORY);
        registered = true;
    }
    public static void enable(JavaPlugin plugin) {
        thread(); if (enabled) return;
        flowerInteractions = new PlantFlowerInteractions();
        Bukkit.getPluginManager().registerEvents(flowerInteractions, plugin); enabled = true;
    }
    public static void disable() {
        thread(); enabled = false;
        if (flowerInteractions != null) HandlerList.unregisterAll(flowerInteractions);
        flowerInteractions = null;
    }
    static boolean enabled() { return enabled; }
    private static void thread() {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Plants mutations require the Paper server thread");
    }
    static String id(ImmutableBlockState state) { return state == null ? "" : state.owner().value().id().asString(); }
    public static Map<String, String> fruitProducts() { return FRUIT; }
    /** Explicit post-CE-load readiness check for integration QA; never substitutes a vanilla tree/state. */
    public static Map<String, Integer> validateLoadedDefinitions() {
        thread(); var counts = new LinkedHashMap<String, Integer>();
        for (String family : new String[] {"spiritual", "lemon", "ginkgo", "peach", "iroha_maple", "itaya_maple", "hauchiwa_maple"}) {
            validateBlock(family + "_leaves", 28, counts); validateBlock(family + "_sapling", 2, counts);
            if (BukkitWorldManager.instance().configuredFeatureHolderById(Key.of(Ids.MOD_ID + ":" + family + "_tree")) == null)
                throw new IllegalStateException("Missing real source configured tree: " + family);
        }
        for (String family : new String[] {"lemon", "ginkgo", "peach"}) validateBlock(family + "_fruit_leaves", 112, counts);
        for (String family : new String[] {"iroha_maple", "itaya_maple", "hauchiwa_maple"}) validateBlock(family + "_leaf_litter", 16, counts);
        validateBlock("udumbara_flower", 1, counts); validateBlock("tremella", 1, counts);
        return Collections.unmodifiableMap(counts);
    }
    private static void validateBlock(String name, int expected, Map<String, Integer> counts) {
        var id = Key.of(Ids.MOD_ID + ":" + name); var block = CraftEngineBlocks.byId(id);
        if (block == null || block.variantProvider().states().size() != expected || CraftEngineItems.byId(id) == null)
            throw new IllegalStateException("Missing/incomplete real plant definition: " + id);
        for (var state : block.variantProvider().states()) {
            var roundTrip = CraftEngineBlocks.getCustomBlockState(CraftEngineBlocks.getBukkitBlockData(state));
            if (!state.equals(roundTrip)) throw new IllegalStateException("Plant native state lost identity: " + id);
        }
        counts.put(id.asString(), expected);
    }
    public static int fruitAge(Block block) {
        var state = CraftEngineBlocks.getCustomBlockState(block);
        if (!FRUIT.containsKey(id(state))) return -1;
        Property<Integer> age = state.getProperty("fruit_age"); return state.get(age);
    }
    private static Block relative(Block block, int x, int y, int z) {
        int bx = block.getX() + x, by = block.getY() + y, bz = block.getZ() + z;
        var world = block.getWorld();
        if (by < world.getMinHeight() || by >= world.getMaxHeight() || !world.isChunkLoaded(bx >> 4, bz >> 4)) return null;
        return world.getBlockAt(bx, by, bz);
    }
    private static boolean same(Block block, String id, int x, int z) {
        var other = relative(block, x, 0, z);
        return other != null && id.equals(id(CraftEngineBlocks.getCustomBlockState(other)));
    }
    public static float sourceMoisture(Block block) {
        int[] below = new int[9]; Arrays.fill(below, -1);
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
            var soil = relative(block, x, -1, z);
            // Original checks Blocks.FARMLAND identity. Custom paddy contributes zero even at moisture7.
            if (soil != null && CraftEngineBlocks.getCustomBlockState(soil) == null && soil.getType() == Material.FARMLAND)
                below[(x + 1) * 3 + z + 1] = ((Farmland) soil.getBlockData()).getMoisture();
        }
        String id = id(CraftEngineBlocks.getCustomBlockState(block));
        return PlantRules.moisture(below,
                same(block, id, -1, 0) || same(block, id, 1, 0),
                same(block, id, 0, -1) || same(block, id, 0, 1),
                same(block, id, -1, -1) || same(block, id, 1, -1) || same(block, id, 1, 1) || same(block, id, -1, 1));
    }
    public static int growthDenominator(Block block) { return PlantRules.growthDenominator(sourceMoisture(block)); }
    /** Deterministic acceptance seam. Natural growth preserves the original withAge default-reset quirk. */
    public static boolean tryRandomTick(Block block, int rawBrightness, int draw) {
        thread(); int age = fruitAge(block); if (!enabled || age < 0) return false;
        return PlantRules.grows(age, rawBrightness, draw, growthDenominator(block)) && grow(block, true);
    }
    public static boolean boneMealFruit(Block block) { thread(); return enabled && grow(block, false); }
    private static boolean grow(Block block, boolean natural) {
        int age = fruitAge(block); if (age < 0 || age >= PlantRules.MATURE_AGE) return false;
        var before = CraftEngineBlocks.getCustomBlockState(block);
        var next = before;
        if (natural) {
            // Source Fruit constructor uses StateDefinition.any(): generated source default is persistent=true.
            next = before.owner().value().defaultState();
            Property<Integer> distance = next.getProperty("distance");
            Property<Boolean> persistent = next.getProperty("persistent"), waterlogged = next.getProperty("waterlogged");
            next = next.with(distance, 7).with(persistent, true).with(waterlogged, false);
        }
        Property<Integer> property = next.getProperty("fruit_age"); next = next.with(property, PlantRules.boneMealAge(age));
        var snapshot = block.getState(); snapshot.setBlockData(CraftEngineBlocks.getBukkitBlockData(next));
        var event = new BlockGrowEvent(block, snapshot); Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() || !before.equals(CraftEngineBlocks.getCustomBlockState(block))) return false;
        return event.getNewState().update(true, !natural);
    }
    /** One real source harvest, age3 -> age1; player=null is reserved for bounded server QA. */
    public static boolean harvestFruit(Block block, Player player, int count) {
        return harvestFruit(block, player, count, EquipmentSlot.HAND);
    }
    static boolean harvestFruit(Block block, Player player, int count, EquipmentSlot hand) {
        thread(); if (!enabled || fruitAge(block) != PlantRules.MATURE_AGE) return false;
        if (count < 1 || count > 3) throw new IllegalArgumentException("Source fruit count must be1..3");
        if (player != null && (holdsBoneMeal(player) || !BukkitCraftEngine.instance().antiGriefProvider().test(player, Flag.INTERACT, block.getLocation()))) return false;
        var before = CraftEngineBlocks.getCustomBlockState(block);
        var definition = CraftEngineItems.byId(FRUIT.get(id(before)));
        if (definition == null) throw new IllegalStateException("Missing real fruit product for " + id(before));
        ItemStack product = definition.buildBukkitItem(); product.setAmount(count);
        List<ItemStack> drops = new ArrayList<>(); drops.add(product);
        if (player != null) {
            var event = new PlayerHarvestBlockEvent(player, block, hand, drops);
            Bukkit.getPluginManager().callEvent(event); if (event.isCancelled()) return false;
            drops = event.getItemsHarvested();
        }
        if (!before.equals(CraftEngineBlocks.getCustomBlockState(block))) return false;
        Property<Integer> age = before.getProperty("fruit_age");
        block.setBlockData(CraftEngineBlocks.getBukkitBlockData(before.with(age, PlantRules.HARVEST_AGE)), true);
        int dropY = block.getY();
        for (int i = 0; i < 16; i++) {
            var at = relative(block, 0, -i, 0);
            if (at != null && at.isReplaceable()) { dropY = at.getY(); break; }
        }
        var location = block.getLocation().add(.5, .3 + dropY - block.getY(), .5);
        for (var drop : drops) if (drop != null && !drop.isEmpty())
            block.getWorld().dropItem(location, drop, entity -> entity.setPickupDelay(10));
        block.getWorld().playSound(block.getLocation(), "minecraft:block.sweet_berry_bush.pick_berries", SoundCategory.BLOCKS,
                1.0f, 0.8f + ThreadLocalRandom.current().nextFloat() * .4f);
        return true;
    }
    public static boolean holdsBoneMeal(Player player) {
        return player.getInventory().getItemInMainHand().getType() == Material.BONE_MEAL
                || player.getInventory().getItemInOffHand().getType() == Material.BONE_MEAL;
    }
    public static boolean duplicateUdumbara(Block block) {
        thread(); if (!enabled || !id(CraftEngineBlocks.getCustomBlockState(block)).equals(Ids.MOD_ID + ":udumbara_flower")) return false;
        var definition = CraftEngineItems.byId(Ids.MOD_ID + ":udumbara_flower");
        if (definition == null) throw new IllegalStateException("Udumbara block item is not loaded");
        block.getWorld().dropItemNaturally(block.getLocation().add(.5, .5, .5), definition.buildBukkitItem()); return true;
    }
}
