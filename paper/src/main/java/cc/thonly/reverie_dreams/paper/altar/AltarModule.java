package cc.thonly.reverie_dreams.paper.altar;

import cc.thonly.reverie_dreams.paper.util.Ids;

import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.block.behavior.BukkitBlockBehavior;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviors;
import net.momirealms.craftengine.core.block.behavior.EntityBlock;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.BlockEntityController;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviors;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.context.UseOnContext;
import net.momirealms.craftengine.libraries.antigrieflib.Flag;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.Locale;
import net.momirealms.craftengine.core.block.BlockDefinition;
import net.momirealms.craftengine.core.world.World;

/** Entry points keep NPC card construction in the already-migrated NPC module. */
public final class AltarModule implements Listener {
    private static final int[] GUI_SLOTS = {2, 4, 6, 20, 24, 38, 40, 42, 22};
    private static final Set<AltarController> LOADED = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Map<AltarController, List<View>> VIEWS = new IdentityHashMap<>();
    private static AltarModule instance;
    private static boolean registered;
    private final JavaPlugin plugin;
    private final FastRecipeBookService books;
    private AltarModule(JavaPlugin plugin) { this.plugin = plugin; books = new FastRecipeBookService(plugin); }

    public static void registerTypes() {
        if (registered) return;
        BlockBehaviors.register(Key.of(Ids.MOD_ID + ":altar"), (block, section) -> new Behavior(block));
        ItemBehaviors.register(Key.of(Ids.MOD_ID + ":fast_recipe_book"), (pack, path, id, section) -> new BookBehavior());
        registered = true;
    }
    public static void enable(JavaPlugin plugin, Function<String, ItemStack> npcRoleCardFactory) {
        if (instance != null) return;
        AltarRecipes.load(npcRoleCardFactory); instance = new AltarModule(plugin);
        Bukkit.getPluginManager().registerEvents(instance, plugin); instance.books.enable();
    }
    public static void disable() {
        if (instance == null) return;
        for (AltarController controller : List.copyOf(LOADED)) { closeViews(controller); controller.dirty(); }
        instance.books.disable(); HandlerList.unregisterAll(instance);
        instance = null; VIEWS.clear(); LOADED.clear();
    }
    public static FastRecipeBookService books() { if (instance == null) throw new IllegalStateException("Altar module not enabled"); return instance.books; }
    static boolean enabled() { return instance != null; }
    static void loaded(AltarController controller) { LOADED.add(controller); }
    static void unloaded(AltarController controller) { LOADED.remove(controller); }
    public static AltarController controllerAt(Block block) {
        AltarController.thread();
        if (!block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)) return null;
        BlockEntity entity = BukkitAdaptor.adapt(block.getWorld()).storageWorld().getBlockEntityAtIfLoaded(new BlockPos(block.getX(), block.getY(), block.getZ()));
        return entity == null ? null : entity.controller.get(AltarController.class, 0);
    }
    private static void use(Player player, AltarController controller) {
        if (!controller.available() || !BukkitCraftEngine.instance().antiGriefProvider().test(player, Flag.OPEN_CONTAINER, controller.location())) return;
        if (player.isSneaking()) {
            AltarController.CraftResult result = controller.craft();
            if (result.status() != AltarController.CraftStatus.SUCCESS) {
                player.sendActionBar(Component.translatable("message.reverie_dreams.altar." + result.status().name().toLowerCase(Locale.ROOT))); return;
            }
            if (result.evacuatedCore() != null) player.getInventory().addItem(result.evacuatedCore()).values().forEach(left -> player.getWorld().dropItemNaturally(player.getLocation(), left));
            var center = controller.location();
            center.getWorld().playSound(center, Sound.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1, 1);
            center.getWorld().spawnParticle(Particle.ENCHANT, center.clone().add(0, .5, 0), 80, 3, 3, 3, .5);
            center.getWorld().spawnParticle(Particle.WITCH, center.clone().add(0, .5, 0), 40, 0, 0, 0, .5);
            for (int[] offset : AltarController.OFFSETS) center.getWorld().spawnParticle(Particle.PORTAL, center.clone().add(offset[0], 0, offset[1]), 20, 1, 2, 1, .5);
        } else {
            View view = new View(controller); VIEWS.computeIfAbsent(controller, ignored -> new ArrayList<>()).add(view);
            view.refresh(); player.openInventory(view.inventory);
        }
    }
    static void refreshViews(AltarController controller) { for (View view : List.copyOf(VIEWS.getOrDefault(controller, List.of()))) view.refresh(); }
    static void closeViews(AltarController controller) {
        List<View> views = VIEWS.remove(controller); if (views == null) return;
        for (View view : views) for (var player : List.copyOf(view.inventory.getViewers())) player.closeInventory();
    }
    private void closeLater(Player player, View view) { Bukkit.getScheduler().runTask(plugin, () -> { if (player.getOpenInventory().getTopInventory() == view.inventory) player.closeInventory(); }); }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void click(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof View view)) return;
        int raw = event.getRawSlot(); boolean ordinary = event.getClick() == ClickType.LEFT || event.getClick() == ClickType.RIGHT;
        if (raw < 0 || raw >= 45) { if (!ordinary) event.setCancelled(true); return; }
        event.setCancelled(true); if (!ordinary || !(event.getWhoClicked() instanceof Player player)) return;
        if (!near(player, view.controller)) { closeLater(player, view); return; }
        if (!BukkitCraftEngine.instance().antiGriefProvider().test(player, Flag.OPEN_CONTAINER, view.controller.location())) return;
        int slot = -1; for (int i = 0; i < 9; i++) if (GUI_SLOTS[i] == raw) slot = i; if (slot < 0) return;
        ItemStack stored = view.controller.item(slot), cursor = event.getCursor(); boolean right = event.getClick() == ClickType.RIGHT;
        if (AltarController.empty(cursor)) {
            int count = AltarController.empty(stored) ? 0 : right ? (stored.getAmount() + 1) / 2 : stored.getAmount();
            ItemStack removed = view.controller.take(slot, count); if (removed != null) player.setItemOnCursor(removed);
        } else if (AltarController.empty(stored) || stored.isSimilar(cursor)) {
            ItemStack offered = cursor.clone(); if (right) offered.setAmount(1);
            ItemStack remainder = view.controller.offer(slot, offered);
            ItemStack next = cursor.clone(); next.subtract(offered.getAmount() - (AltarController.empty(remainder) ? 0 : remainder.getAmount()));
            player.setItemOnCursor(next.isEmpty() ? null : next);
        } else if (cursor.getAmount() <= cursor.getMaxStackSize()) {
            ItemStack removed = view.controller.take(slot, stored.getAmount()); view.controller.offer(slot, cursor); player.setItemOnCursor(removed);
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void drag(InventoryDragEvent event) { if (event.getView().getTopInventory().getHolder() instanceof View && event.getRawSlots().stream().anyMatch(slot -> slot < 45)) event.setCancelled(true); }
    @EventHandler public void close(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof View view)) return;
        var views = VIEWS.get(view.controller); if (views != null) { views.remove(view); if (views.isEmpty()) VIEWS.remove(view.controller); }
        view.controller.dirty();
    }
    @EventHandler(ignoreCancelled = true) public void move(PlayerMoveEvent event) {
        if (event.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof View view && !near(event.getPlayer(), view.controller)) event.getPlayer().closeInventory();
    }
    private static boolean near(Player player, AltarController controller) { return controller.available() && player.getWorld().equals(controller.location().getWorld()) && player.getLocation().distanceSquared(controller.location()) <= 64; }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) public void breaking(BlockBreakEvent event) {
        var controller = controllerAt(event.getBlock()); if (controller != null && controller.snapshot().quarantined()) event.setCancelled(true);
    }
    private static final class View implements InventoryHolder {
        private final AltarController controller;
        private final Inventory inventory;
        View(AltarController controller) { this.controller = controller; inventory = Bukkit.createInventory(this, 45, Component.translatable("block.reverie_dreams.gensokyo_altar")); }
        @Override public Inventory getInventory() { return inventory; }
        void refresh() { inventory.clear(); for (int slot = 0; slot < 9; slot++) inventory.setItem(GUI_SLOTS[slot], controller.item(slot)); }
    }
    private static final class Behavior extends BukkitBlockBehavior implements EntityBlock {
        private int controllerId;
        Behavior(BlockDefinition block) { super(block); }
        @Override public BlockEntityController createBlockEntityController(BlockEntity entity) { return new AltarController(entity); }
        @Override public void initControllerId(int id) { controllerId = id; }
        @Override public InteractionResult useOnBlock(UseOnContext context, ImmutableBlockState state) { return interact(context); }
        @Override public InteractionResult useWithoutItem(UseOnContext context, ImmutableBlockState state) { return interact(context); }
        private InteractionResult interact(UseOnContext context) {
            if (!enabled() || context.getPlayer() == null) return InteractionResult.PASS;
            BlockEntity entity = context.getLevel().storageWorld().getBlockEntityAtIfLoaded(context.getClickedPos()); if (entity == null) return InteractionResult.FAIL;
            AltarController controller = entity.controller.getAt(AltarController.class, controllerId); if (controller == null) return InteractionResult.FAIL;
            use((Player) context.getPlayer().platformPlayer(), controller); return InteractionResult.SUCCESS_AND_CANCEL;
        }
    }
    private static final class BookBehavior extends ItemBehavior {
        @Override public InteractionResult use(World world, net.momirealms.craftengine.core.entity.player.Player player, InteractionHand hand) {
            if (!enabled() || player == null) return InteractionResult.PASS;
            instance.books.open((Player) player.platformPlayer(), hand == InteractionHand.MAIN_HAND ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND); return InteractionResult.SUCCESS_AND_CANCEL;
        }
        @Override public InteractionResult useOnBlock(UseOnContext context) {
            if (!enabled() || context.getPlayer() == null) return InteractionResult.PASS;
            Player player = (Player) context.getPlayer().platformPlayer(); var pos = context.getClickedPos();
            Block block = ((org.bukkit.World) context.getLevel().platformWorld()).getBlockAt(pos.x(), pos.y(), pos.z());
            return instance.books.useOnBlock(player, context.getHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND, block)
                    ? InteractionResult.SUCCESS_AND_CANCEL : InteractionResult.PASS;
        }
    }
}
