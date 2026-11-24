package cc.thonly.reverie_dreams.paper.production;

import cc.thonly.reverie_dreams.paper.util.Ids;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviors;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.libraries.antigrieflib.Flag;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.core.util.Direction;
import org.bukkit.GameMode;
import org.bukkit.Tag;
import org.bukkit.event.inventory.ClickType;

/** Integration: registerTypes in onLoad, enable in onEnable, disable in onDisable. */
public final class ProductionModule implements Listener {
    private static final Set<ProductionController> LOADED = Collections.newSetFromMap(new IdentityHashMap<>());
    private static ProductionModule instance;
    private static boolean registered;

    private ProductionModule() {}

    public static void registerTypes() {
        if (registered) return;
        BlockBehaviors.register(Key.of(Ids.MOD_ID + ":production"), ProductionBlockBehavior.FACTORY);
        registered = true;
    }

    public static void enable(JavaPlugin plugin) {
        if (instance != null) return;
        ProductionRecipes.load();
        instance = new ProductionModule();
        plugin.getServer().getPluginManager().registerEvents(instance, plugin);
    }

    public static void disable() {
        if (instance != null) HandlerList.unregisterAll(instance);
        instance = null;
        for (ProductionController controller : List.copyOf(LOADED)) {
            controller.closeViewers();
            controller.dirty();
        }
        LOADED.clear();
    }

    static boolean enabled() { return instance != null; }
    static void loaded(ProductionController controller) { LOADED.add(controller); }
    static void unloaded(ProductionController controller) { LOADED.remove(controller); }

    /** Loaded-only lookup; never loads or creates a chunk as a side effect. */
    public static ProductionController controllerAt(Block block) {
        ProductionController.requireThread();
        if (!block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)) return null;
        BlockEntity entity = BukkitAdaptor.adapt(block.getWorld()).storageWorld().getBlockEntityAtIfLoaded(
                new BlockPos(block.getX(), block.getY(), block.getZ()));
        return entity == null ? null : entity.controller.get(ProductionController.class, 0);
    }

    static void interact(Player player, InteractionHand hand, ProductionController controller) {
        if (!controller.available()) return;
        if (!BukkitCraftEngine.instance().antiGriefProvider().test(player, Flag.OPEN_CONTAINER, controller.location())) return;
        // Minecraft retries off-hand only for PASS results. Both callbacks return success/cancel.
        ItemStack held = hand == InteractionHand.MAIN_HAND ? player.getInventory().getItemInMainHand()
                : player.getInventory().getItemInOffHand();
        if (controller.kind() == ProductionController.Kind.ICE) {
            ItemStack output = controller.extractOutput();
            if (output != null) {
                giveOrDrop(player, output);
                sound(controller, Sound.BLOCK_GLASS_BREAK);
            } else if (held.getType() == Material.WATER_BUCKET && controller.startIce()) {
                // The original consumes in survival, keeps the water in creative, and returns a bucket.
                if (player.getGameMode() != GameMode.CREATIVE) consumeHand(player, hand, held);
                giveOrDrop(player, new ItemStack(Material.BUCKET));
                sound(controller, Sound.ITEM_BUCKET_FILL);
            } else progress(player, controller);
            return;
        }
        if (controller.snapshot().ticksRemaining() > 0) {
            progress(player, controller);
            return;
        }
        if (held.getType() == Material.GLASS_BOTTLE && controller.snapshot().servings() > 0) {
            ItemStack output = controller.extractOutput();
            if (output != null) {
                // Barrel source consumes bottles in every mode; preserve that behavior.
                consumeHand(player, hand, held);
                giveOrDrop(player, output);
                sound(controller, Sound.ITEM_BOTTLE_FILL);
            }
        } else if ((player.isSneaking() || held.getType() == Material.GLASS_BOTTLE) && controller.startBrewing()) {
            sound(controller, Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
        } else if (!player.isSneaking() && controller.canEditInputs() && held.getType() != Material.GLASS_BOTTLE) {
            player.openInventory(controller.getInventory());
            sound(controller, Sound.BLOCK_BARREL_OPEN);
        } else progress(player, controller);
    }

    private static void progress(Player player, ProductionController controller) {
        var snapshot = controller.snapshot();
        if (snapshot.ticksRemaining() > 0) {
            player.sendActionBar(Component.translatable("message.reverie_dreams.production.progress",
                    Component.text((snapshot.ticksRemaining() + 19) / 20)));
        } else if (snapshot.servings() > 0) {
            player.sendActionBar(Component.translatable("message.reverie_dreams.production.bottles",
                    Component.text(snapshot.servings())));
        } else if (controller.kind() == ProductionController.Kind.BARREL) {
            player.sendActionBar(Component.translatable("message.reverie_dreams.production.instructions"));
        }
    }

    private static void consumeHand(Player player, InteractionHand hand, ItemStack held) {
        ItemStack remainder = held.clone();
        remainder.subtract(1);
        if (hand == InteractionHand.MAIN_HAND) player.getInventory().setItemInMainHand(remainder);
        else player.getInventory().setItemInOffHand(remainder);
    }

    private static void giveOrDrop(Player player, ItemStack item) {
        player.getInventory().addItem(item).values().forEach(leftover -> player.getWorld()
                .dropItemNaturally(player.getLocation(), leftover));
    }

    private static void sound(ProductionController controller, Sound sound) {
        controller.location().getWorld().playSound(controller.location(), sound, SoundCategory.BLOCKS, .5f, 1f);
    }

    /**
     * Cancel vanilla moves (including shift, number keys, double click and creative cloning).
     * Only an ordinary cursor click on a top slot transfers exactly one input or removes it.
     * Bottom inventory ordinary clicks stay vanilla; cross-inventory actions are canceled.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void click(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof ProductionController controller)) return;
        if (!controller.available() || !controller.canEditInputs()) {
            event.setCancelled(true);
            return;
        }
        int raw = event.getRawSlot();
        boolean ordinary = event.getClick() == ClickType.LEFT
                || event.getClick() == ClickType.RIGHT;
        if (raw < 0 || raw >= 9) {
            if (!ordinary) event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        if (!ordinary || !(event.getWhoClicked() instanceof Player player)) return;
        if (!near(player, controller)) {
            player.closeInventory();
            return;
        }
        if (!BukkitCraftEngine.instance().antiGriefProvider().test(player, Flag.OPEN_CONTAINER, controller.location())) return;
        ItemStack cursor = event.getCursor();
        ItemStack stored = controller.getInventory().getItem(raw);
        if (ProductionController.empty(cursor)) {
            ItemStack removed = controller.takeInput(raw);
            if (removed != null) player.setItemOnCursor(removed);
        } else if (ProductionController.empty(stored)) {
            ItemStack remainder = controller.offerInput(raw, cursor);
            player.setItemOnCursor(remainder);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void drag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof ProductionController
                && event.getRawSlots().stream().anyMatch(i -> i < 9)) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void hopper(InventoryMoveItemEvent event) {
        if (event.getSource().getHolder() instanceof ProductionController
                || event.getDestination().getHolder() instanceof ProductionController) event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void move(PlayerMoveEvent event) {
        if (event.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof ProductionController controller
                && !near(event.getPlayer(), controller)) event.getPlayer().closeInventory();
    }

    private static boolean near(Player player, ProductionController controller) {
        return controller.available() && player.getWorld().equals(controller.location().getWorld())
                && player.getLocation().distanceSquared(controller.location()) <= 64;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void breaking(BlockBreakEvent event) {
        ProductionController controller = controllerAt(event.getBlock());
        if (controller != null && controller.requiresProtection()) {
            event.setCancelled(true);
            event.getPlayer().sendActionBar(Component.translatable("message.reverie_dreams.production.drain_first"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void explosion(BlockExplodeEvent event) { protect(event.blockList()); }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void explosion(EntityExplodeEvent event) { protect(event.blockList()); }

    private static void protect(List<Block> blocks) {
        blocks.removeIf(block -> {
            ProductionController controller = controllerAt(block);
            return controller != null && controller.requiresProtection();
        });
    }

    /** Native Mod dispenser automation: water starts ice, pickaxes remove one ready item. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void dispense(BlockDispenseEvent event) {
        if (!(event.getBlock().getBlockData() instanceof Directional directional)
                || !(event.getBlock().getState() instanceof Dispenser dispenser)) return;
        ProductionController controller = controllerAt(event.getBlock().getRelative(directional.getFacing()));
        if (controller == null || controller.kind() != ProductionController.Kind.ICE) return;
        ItemStack item = event.getItem();
        boolean water = item.getType() == Material.WATER_BUCKET;
        boolean pickaxe = Tag.ITEMS_PICKAXES.isTagged(item.getType());
        if (!water && !pickaxe) return;
        Inventory inventory = dispenser.getInventory();
        int slot = -1;
        for (int i = 0; i < inventory.getSize(); i++) {
            if (!ProductionController.empty(inventory.getItem(i)) && inventory.getItem(i).isSimilar(item)) {
                slot = i;
                break;
            }
        }
        if (slot < 0) return;
        if (water) {
            if (!controller.startIce()) return;
            event.setCancelled(true);
            ItemStack remainder = inventory.getItem(slot).clone();
            remainder.subtract(1);
            if (remainder.isEmpty()) inventory.setItem(slot, new ItemStack(Material.BUCKET));
            else {
                inventory.setItem(slot, remainder);
                inventory.addItem(new ItemStack(Material.BUCKET)).values().forEach(leftover ->
                        event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation().add(.5, .5, .5), leftover));
            }
        } else {
            event.setCancelled(true); // Keep the pickaxe even when no output is ready.
            ItemStack output = controller.extractOutput();
            if (output == null) return;
            Block machine = event.getBlock().getRelative(directional.getFacing());
            var state = CraftEngineBlocks.getCustomBlockState(machine);
            var facing = state.<Direction>getProperty("facing");
            var direction = state.get(facing).opposite();
            machine.getWorld().dropItemNaturally(controller.location().add(direction.stepX(), direction.stepY(), direction.stepZ()), output);
            ItemStack tool = inventory.getItem(slot).clone();
            if (tool.getItemMeta() instanceof Damageable damage && !damage.isUnbreakable()
                    && ThreadLocalRandom.current().nextInt(tool.getEnchantmentLevel(Enchantment.UNBREAKING) + 1) == 0) {
                int value = damage.getDamage() + 1;
                Integer maximum = tool.getData(DataComponentTypes.MAX_DAMAGE);
                if (maximum != null && value >= maximum) tool.subtract(1);
                else {
                    damage.setDamage(value);
                    tool.setItemMeta(damage);
                }
                inventory.setItem(slot, tool.isEmpty() ? null : tool);
            }
            sound(controller, Sound.BLOCK_GLASS_BREAK);
        }
    }
}
