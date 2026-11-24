package cc.thonly.reverie_dreams.paper.tools;

import com.google.gson.JsonParser;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.IOException;

/** Native item components implement all tool gameplay; Java only fixes CE's material-count boundary. */
public final class ToolsModule {
    public record RepairPreview(ItemStack result, int materialCount, int repairedDurability) {
        public RepairPreview { result = result.clone(); }
        @Override public ItemStack result() { return result.clone(); }
    }
    private final Map<String, ToolRule> rules = new LinkedHashMap<>();
    private final Map<String, Set<String>> customRepair = new LinkedHashMap<>();
    private ToolRepairBridge repair;
    private JavaPlugin plugin;

    public void enable(JavaPlugin plugin) {
        thread();
        if (this.plugin != null) throw new IllegalStateException("Tools module already enabled");
        this.plugin = plugin;
        load(); repair = new ToolRepairBridge(this);
        Bukkit.getPluginManager().registerEvents(repair, plugin);
    }
    public void disable() {
        thread(); if (repair != null) HandlerList.unregisterAll(repair);
        repair = null; rules.clear(); customRepair.clear(); plugin = null;
    }
    private static void thread() {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Tool inventory operations require Paper server thread");
    }
    private void load() {
        try (var input = plugin.getResource("migration/tools-domain.json")) {
            if (input == null) throw new IllegalStateException("Missing tools domain");
            var root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
            if (root.get("schemaVersion").getAsInt() != 1) throw new IllegalStateException("Unsupported tools domain");
            for (var entry : root.getAsJsonObject("items").entrySet()) {
                var row = entry.getValue().getAsJsonObject();
                rules.put(entry.getKey(), new ToolRule(entry.getKey(), row.get("material").getAsString(), row.get("kind").getAsString(),
                        row.get("durability").getAsInt(), row.get("attackDamageModifier").getAsDouble(), row.get("attackSpeedModifier").getAsDouble(),
                        row.get("enchantability").getAsInt(), row.get("repairTag").getAsString(), row.get("silverMarker").getAsBoolean()));
            }
            for (var entry : root.getAsJsonObject("customRepair").entrySet()) {
                var targets = new LinkedHashSet<String>(); for (var value : entry.getValue().getAsJsonArray()) targets.add(value.getAsString());
                customRepair.put(entry.getKey(), Set.copyOf(targets));
            }
        } catch (IOException failure) { throw new IllegalStateException("Cannot load tool rules", failure); }
    }
    public Map<String, ToolRule> items() { return Collections.unmodifiableMap(rules); }
    public Optional<ToolRule> inspect(ItemStack item) { return Optional.ofNullable(rules.get(id(item))); }
    static String id(ItemStack item) {
        if (item == null || item.isEmpty()) return "minecraft:air";
        var id = CraftEngineItems.getCustomItemId(item);
        return id == null ? item.getType().getKey().asString() : id.asString();
    }
    /** Original vanilla repair loop consumes ceil(damage / floor(maxDamage / 4)), capped by material amount. */
    public Optional<RepairPreview> previewRepair(ItemStack item, ItemStack ingredient) {
        thread(); if (item == null || ingredient == null || ingredient.isEmpty()) return Optional.empty();
        var targets = customRepair.get(id(ingredient)); if (targets == null || !targets.contains(id(item))) return Optional.empty();
        int max = item.getDataOrDefault(DataComponentTypes.MAX_DAMAGE, 0), damage = item.getDataOrDefault(DataComponentTypes.DAMAGE, 0);
        int each = max / 4; if (damage <= 0 || each <= 0) return Optional.empty();
        int count = Math.min(ingredient.getAmount(), (damage + each - 1) / each);
        int repaired = Math.min(damage, count * each); var result = item.clone(); result.setData(DataComponentTypes.DAMAGE, damage - repaired);
        return Optional.of(new RepairPreview(result, count, repaired));
    }
}
