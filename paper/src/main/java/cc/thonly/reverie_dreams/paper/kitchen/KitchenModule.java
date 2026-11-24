package cc.thonly.reverie_dreams.paper.kitchen;

import cc.thonly.reverie_dreams.paper.util.Ids;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviors;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.libraries.antigrieflib.Flag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Source-shaped paged recipe selector backed by an independent native six-slot CE container. */
public final class KitchenModule implements Listener {
    private static final Set<KitchenController> LOADED = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Map<KitchenController, List<View>> VIEWS = new IdentityHashMap<>();
    private static final List<Integer> RECIPES = List.of(1, 2, 3, 4, 5, 6, 7, 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25);
    private static final int[] INPUTS = {37, 38, 39, 40, 41};
    private static KitchenModule instance;
    private static boolean registered;
    private static boolean handlingClick;
    private final JavaPlugin plugin;
    private KitchenModule(JavaPlugin plugin) { this.plugin = plugin; }

    public static void registerTypes() {
        if (registered) return;
        BlockBehaviors.register(Key.of(Ids.MOD_ID + ":kitchen"), KitchenBlockBehavior.FACTORY);
        registered = true;
    }
    public static void enable(JavaPlugin plugin) {
        if (instance != null) return;
        KitchenRecipes.load();
        instance = new KitchenModule(plugin);
        Bukkit.getPluginManager().registerEvents(instance, plugin);
    }
    public static void disable() {
        if (instance != null) HandlerList.unregisterAll(instance);
        instance = null;
        for (KitchenController controller : List.copyOf(LOADED)) { closeViews(controller); controller.dirty(); }
        VIEWS.clear();
        LOADED.clear();
    }
    static boolean enabled() { return instance != null; }
    static void loaded(KitchenController controller) { LOADED.add(controller); }
    static void unloaded(KitchenController controller) { LOADED.remove(controller); }

    public static KitchenController controllerAt(Block block) {
        KitchenController.requireThread();
        if (!block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)) return null;
        BlockEntity entity = BukkitAdaptor.adapt(block.getWorld()).storageWorld().getBlockEntityAtIfLoaded(
                new BlockPos(block.getX(), block.getY(), block.getZ()));
        return entity == null ? null : entity.controller.get(KitchenController.class, 0);
    }

    static void open(Player player, KitchenController controller) {
        if (!controller.available() || !BukkitCraftEngine.instance().antiGriefProvider().test(player, Flag.OPEN_CONTAINER, controller.location())) return;
        if (controller.working()) {
            player.sendActionBar(Component.translatable("message.reverie_dreams.kitchen.working",
                    Component.text((int) Math.ceil(controller.snapshot().ticksRemaining() / controller.snapshot().speed() / 20))));
            return;
        }
        View view = new View(controller);
        VIEWS.computeIfAbsent(controller, ignored -> new ArrayList<>()).add(view);
        view.refresh();
        player.openInventory(view.inventory);
    }

    static void closeViews(KitchenController controller) {
        var views = VIEWS.remove(controller);
        if (views == null) return;
        for (View view : views) closeView(view);
    }

    private static void closeView(View view) {
        Runnable close = () -> {
            for (var viewer : List.copyOf(view.inventory.getViewers())) {
                if (viewer.getOpenInventory().getTopInventory() == view.inventory) viewer.closeInventory();
            }
        };
        // Paper forbids open/closeInventory while InventoryClickEvent is being processed.
        if (handlingClick && instance != null) Bukkit.getScheduler().runTask(instance.plugin, close);
        else close.run();
    }

    static void refreshViews(KitchenController controller) {
        for (View view : List.copyOf(VIEWS.getOrDefault(controller, List.of()))) view.refresh();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void click(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof View view)) return;
        handlingClick = true;
        try { clickInView(event, view); }
        finally { handlingClick = false; }
    }

    private void clickInView(InventoryClickEvent event, View view) {
        int raw = event.getRawSlot();
        boolean ordinary = event.getClick() == ClickType.LEFT || event.getClick() == ClickType.RIGHT;
        if (raw < 0 || raw >= 54) {
            if (!ordinary) event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        if (!ordinary || !(event.getWhoClicked() instanceof Player player)) return;
        KitchenController controller = view.controller;
        if (!near(player, controller) || controller.working()) { closeView(view); return; }
        if (!BukkitCraftEngine.instance().antiGriefProvider().test(player, Flag.OPEN_CONTAINER, controller.location())) return;
        if (raw == 28 || raw == 34) {
            view.page += raw == 28 ? -1 : 1;
            view.refresh();
            return;
        }
        String recipe = view.recipeIds.get(raw);
        if (recipe != null) { controller.start(recipe); return; }
        int slot = raw == 43 ? 5 : -1;
        for (int i = 0; i < 5; i++) if (INPUTS[i] == raw) slot = i;
        if (slot < 0) return;
        ItemStack cursor = event.getCursor();
        ItemStack stored = controller.item(slot);
        boolean right = event.getClick() == ClickType.RIGHT;
        if (KitchenController.empty(cursor)) {
            int count = KitchenController.empty(stored) ? 0 : right ? (stored.getAmount() + 1) / 2 : stored.getAmount();
            ItemStack result = slot == 5 ? controller.extractOutput(count) : controller.takeInput(slot, count);
            if (result != null) player.setItemOnCursor(result);
        } else if (KitchenController.empty(stored) || stored.isSimilar(cursor)) {
            ItemStack offered = cursor.clone();
            if (right) offered.setAmount(1);
            ItemStack remainder = slot == 5 ? controller.offerOutput(offered) : controller.offerInput(slot, offered);
            int accepted = offered.getAmount() - (KitchenController.empty(remainder) ? 0 : remainder.getAmount());
            ItemStack result = cursor.clone();
            result.subtract(accepted);
            player.setItemOnCursor(result.isEmpty() ? null : result);
        } else if (cursor.getAmount() <= cursor.getMaxStackSize()) {
            // Both operations run without yielding; the now-empty slot can accept this complete cursor stack.
            ItemStack removed = slot == 5 ? controller.extractOutput(stored.getAmount()) : controller.takeInput(slot, stored.getAmount());
            if (slot == 5) controller.offerOutput(cursor); else controller.offerInput(slot, cursor);
            player.setItemOnCursor(removed);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void drag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof View && event.getRawSlots().stream().anyMatch(slot -> slot < 54)) event.setCancelled(true);
    }
    @EventHandler public void close(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof View view)) return;
        List<View> views = VIEWS.get(view.controller);
        if (views != null) { views.remove(view); if (views.isEmpty()) VIEWS.remove(view.controller); }
        view.controller.dirty();
    }
    @EventHandler(ignoreCancelled = true) public void move(PlayerMoveEvent event) {
        if (event.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof View view && !near(event.getPlayer(), view.controller)) event.getPlayer().closeInventory();
    }
    private static boolean near(Player player, KitchenController controller) {
        return controller.available() && player.getWorld().equals(controller.location().getWorld())
                && player.getLocation().distanceSquared(controller.location()) <= 64;
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) public void breaking(BlockBreakEvent event) {
        KitchenController controller = controllerAt(event.getBlock());
        if (controller != null && controller.protectedFromBreak()) {
            event.setCancelled(true);
            event.getPlayer().sendActionBar(Component.translatable("message.reverie_dreams.kitchen.wait_before_break"));
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) public void explosion(BlockExplodeEvent event) { protect(event.blockList()); }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) public void explosion(EntityExplodeEvent event) { protect(event.blockList()); }
    private static void protect(List<Block> blocks) { blocks.removeIf(block -> { var controller = controllerAt(block); return controller != null && controller.protectedFromBreak(); }); }

    private static final class View implements InventoryHolder {
        private final KitchenController controller;
        private final Inventory inventory;
        private final Map<Integer, String> recipeIds = new HashMap<>();
        private int page;
        View(KitchenController controller) {
            this.controller = controller;
            inventory = Bukkit.createInventory(this, 54, Component.translatable("block." + controller.snapshot().blockId().replace(':', '.')));
        }
        @Override public Inventory getInventory() { return inventory; }
        void refresh() {
            inventory.clear();
            recipeIds.clear();
            for (int slot = 0; slot < 5; slot++) inventory.setItem(INPUTS[slot], controller.item(slot));
            inventory.setItem(43, controller.item(5));
            List<KitchenController.Choice> choices = controller.choices();
            int maximum = Math.max(0, (choices.size() - 1) / RECIPES.size());
            page = Math.clamp(page, 0, maximum);
            for (int i = 0; i < RECIPES.size() && page * RECIPES.size() + i < choices.size(); i++) {
                var choice = choices.get(page * RECIPES.size() + i);
                inventory.setItem(RECIPES.get(i), choice.preview().clone());
                recipeIds.put(RECIPES.get(i), choice.recipeId());
            }
            ItemStack previous = new ItemStack(Material.ARROW), next = new ItemStack(Material.ARROW);
            previous.editMeta(meta -> meta.itemName(Component.translatable("message.reverie_dreams.kitchen.previous")));
            next.editMeta(meta -> meta.itemName(Component.translatable("message.reverie_dreams.kitchen.next")));
            inventory.setItem(28, previous);
            inventory.setItem(34, next);
        }
    }
}
