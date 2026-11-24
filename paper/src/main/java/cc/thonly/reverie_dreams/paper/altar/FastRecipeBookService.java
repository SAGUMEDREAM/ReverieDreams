package cc.thonly.reverie_dreams.paper.altar;

import cc.thonly.reverie_dreams.paper.util.Ids;

import cc.thonly.reverie_dreams.paper.kitchen.KitchenController;
import cc.thonly.reverie_dreams.paper.kitchen.KitchenModule;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.libraries.antigrieflib.Flag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

/** Durable source recipe snapshots; a remembered book deliberately follows the source's distinct fast path. */
public final class FastRecipeBookService implements Listener {
    public static final String ITEM_ID = Ids.MOD_ID + ":fast_book_item";
    public static final NamespacedKey MEMORY = new NamespacedKey("reverie_dreams", "recipe_memory");
    private static final String[] TYPES = {"cooking_pot", "cutting_board", "frying_pan", "grill", "steamer"};
    private final JavaPlugin plugin;
    private final Map<String, JsonObject> recipes = new LinkedHashMap<>();

    FastRecipeBookService(JavaPlugin plugin) {
        this.plugin = plugin;
        for (var value : AltarRecipes.read("/migration/kitchen-recipes.json").getAsJsonArray("recipes")) {
            JsonObject source = value.getAsJsonObject().deepCopy(); recipes.put(source.get("id").getAsString(), source);
        }
    }
    void enable() { Bukkit.getPluginManager().registerEvents(this, plugin); }
    void disable() {
        HandlerList.unregisterAll(this);
        for (Player player : Bukkit.getOnlinePlayers()) if (player.getOpenInventory().getTopInventory().getHolder() instanceof Menu) player.closeInventory();
    }
    private static boolean isBook(ItemStack item) { var id = CraftEngineItems.getCustomItemId(item); return id != null && id.asString().equals(ITEM_ID); }

    /** Returns a new book stack; does not mutate a caller's inventory or re-resolve the recipe on later use. */
    public ItemStack remember(ItemStack book, String recipeId) {
        AltarController.thread();
        if (!isBook(book)) throw new IllegalArgumentException("Not a fast recipe book");
        JsonObject recipe = recipes.get(recipeId); if (recipe == null) throw new IllegalArgumentException("Unknown kitchen recipe: " + recipeId);
        JsonObject memory = new JsonObject(); memory.addProperty("schema", 1); memory.addProperty("key", recipeId); memory.add("recipe", recipe.deepCopy());
        ItemStack result = book.clone();
        result.editMeta(meta -> meta.getPersistentDataContainer().set(MEMORY, PersistentDataType.STRING, memory.toString()));
        return result;
    }

    public String rememberedRecipe(ItemStack book) {
        JsonObject memory = memory(book); return memory == null ? null : memory.get("key").getAsString();
    }

    private JsonObject memory(ItemStack book) {
        if (!isBook(book)) return null;
        String encoded = book.getPersistentDataContainer().get(MEMORY, PersistentDataType.STRING);
        if (encoded == null) return null;
        try {
            JsonObject memory = JsonParser.parseString(encoded).getAsJsonObject();
            if (memory.get("schema").getAsInt() != 1 || !memory.has("key") || !memory.has("recipe")) return null;
            return memory;
        } catch (RuntimeException invalid) { return null; } // Preserve opaque PDC; do not turn a corrupt memory into a different recipe.
    }

    /** Trusted low-level start; book and inputs are preserved on mismatch or missing definitions. */
    public boolean startRemembered(ItemStack book, KitchenController kitchen) {
        AltarController.thread();
        JsonObject memory = memory(book); if (memory == null) return false;
        JsonObject recipe = memory.getAsJsonObject("recipe");
        List<ItemStack> ingredients = new ArrayList<>();
        for (var value : recipe.getAsJsonArray("ingredients")) {
            ItemStack item = AltarRecipes.build(value.getAsJsonObject()); if (item == null) return false; ingredients.add(item);
        }
        ItemStack output = AltarRecipes.build(recipe.getAsJsonObject("output"));
        if (output == null) return false;
        return kitchen.startRememberedRecipe(memory.get("key").getAsString(), recipe.get("recipe_type").getAsString(),
                ingredients, output, recipe.has("cost_time") ? recipe.get("cost_time").getAsDouble() : 5.0);
    }

    void open(Player player, EquipmentSlot hand) {
        ItemStack book = player.getInventory().getItem(hand);
        if (!isBook(book)) return;
        Menu menu = new Menu(player, hand, book.clone()); menu.render(); player.openInventory(menu.inventory);
    }

    boolean useOnBlock(Player player, EquipmentSlot hand, Block block) {
        KitchenController kitchen = KitchenModule.controllerAt(block); if (kitchen == null) return false;
        if (!BukkitCraftEngine.instance().antiGriefProvider().test(player, Flag.OPEN_CONTAINER, kitchen.location())) return true;
        if (startRemembered(player.getInventory().getItem(hand), kitchen)) {
            block.getWorld().playSound(block.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.BLOCKS, 1, 1);
        }
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void click(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof Menu menu)) return;
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= 54) { if (event.getClick() != ClickType.LEFT && event.getClick() != ClickType.RIGHT) event.setCancelled(true); return; }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || !player.getUniqueId().equals(menu.player.getUniqueId())) return;
        ItemStack current = player.getInventory().getItem(menu.hand);
        if (!current.equals(menu.expected)) { closeLater(menu); return; }
        ClickType click = event.getClick();
        if (click != ClickType.LEFT && click != ClickType.RIGHT && click != ClickType.SHIFT_LEFT && click != ClickType.SHIFT_RIGHT) return;
        if (slot == 45) menu.page--;
        else if (slot == 53) menu.page++;
        else if (slot >= 47 && slot <= 51) {
            menu.type = click.isShiftClick() ? null : Ids.MOD_ID + ":" + TYPES[slot - 47];
            menu.reversed = click == ClickType.RIGHT || click == ClickType.SHIFT_RIGHT;
        } else {
            String id = menu.displayed.get(slot);
            if (id != null) {
                ItemStack updated = remember(current, id);
                player.getInventory().setItem(menu.hand, updated);
                menu.expected = updated.clone();
            }
        }
        menu.render();
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void drag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof Menu && event.getRawSlots().stream().anyMatch(slot -> slot < 54)) event.setCancelled(true);
    }
    private void closeLater(Menu menu) { Bukkit.getScheduler().runTask(plugin, () -> { if (menu.player.getOpenInventory().getTopInventory() == menu.inventory) menu.player.closeInventory(); }); }

    private final class Menu implements InventoryHolder {
        private final Player player;
        private final EquipmentSlot hand;
        private final Inventory inventory;
        private final Map<Integer, String> displayed = new LinkedHashMap<>();
        private ItemStack expected;
        private String type;
        private boolean reversed;
        private int page;
        Menu(Player player, EquipmentSlot hand, ItemStack book) {
            this.player = player; this.hand = hand; expected = book;
            inventory = Bukkit.createInventory(this, 54, Component.translatable("item.reverie_dreams.fast_book_item"));
        }
        @Override public Inventory getInventory() { return inventory; }
        void render() {
            inventory.clear(); displayed.clear();
            Comparator<JsonObject> comparator = Comparator.comparing(recipe -> recipe.getAsJsonObject("output").get("id").getAsString());
            if (reversed && type != null) comparator = comparator.reversed();
            List<JsonObject> contents = recipes.values().stream().filter(recipe -> type == null || type.equals(recipe.get("recipe_type").getAsString())).sorted(comparator).toList();
            page = Math.clamp(page, 0, Math.max(0, (contents.size() - 1) / 45));
            String selected = rememberedRecipe(expected);
            for (int slot = 0; slot < 45 && page * 45 + slot < contents.size(); slot++) {
                JsonObject recipe = contents.get(page * 45 + slot); String id = recipe.get("id").getAsString();
                ItemStack icon = AltarRecipes.build(recipe.getAsJsonObject("output")); if (icon == null) continue;
                if (id.equals(selected)) icon.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
                List<Component> lore = new ArrayList<>();
                lore.add(Component.translatable("block." + recipe.get("recipe_type").getAsString().replace(':', '.')));
                for (var ingredient : recipe.getAsJsonArray("ingredients")) {
                    ItemStack material = AltarRecipes.build(ingredient.getAsJsonObject());
                    if (material != null) lore.add(material.effectiveName());
                }
                icon.editMeta(meta -> meta.lore(lore)); inventory.setItem(slot, icon); displayed.put(slot, id);
            }
            inventory.setItem(45, named(Material.ARROW, "message.reverie_dreams.kitchen.previous"));
            inventory.setItem(53, named(Material.ARROW, "message.reverie_dreams.kitchen.next"));
            for (int i = 0; i < TYPES.length; i++) {
                var definition = CraftEngineItems.byId(Ids.MOD_ID + ":" + TYPES[i]);
                if (definition != null) inventory.setItem(47 + i, definition.buildBukkitItem());
            }
        }
    }
    private static ItemStack named(Material material, String key) { ItemStack item = new ItemStack(material); item.editMeta(meta -> meta.itemName(Component.translatable(key))); return item; }
}
