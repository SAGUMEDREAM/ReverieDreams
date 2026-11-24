package cc.thonly.reverie_dreams.smoke;

import cc.thonly.reverie_dreams.paper.storage.CashBoxController;
import cc.thonly.reverie_dreams.paper.storage.CupboardController;
import cc.thonly.reverie_dreams.paper.storage.StorageSpecialModule;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.UUID;
import java.util.function.BiConsumer;

/** Real native hopper, integer storage, cancelled spawn and pending-drop restart conservation checks. */
final class StorageSpecialSmokeChecks {
    private static final NamespacedKey KIND = new NamespacedKey("smoke", "storage_kind");
    private static final NamespacedKey PENDING = new NamespacedKey("smoke", "pending_drop");
    private static final File FIXTURE = new File("probe-storage-fixture.yml");
    private static boolean release;
    static void install(JavaPlugin plugin) {
        release = false;
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler public void spawn(ItemSpawnEvent event) {
                if (!release && event.getEntity().getItemStack().getPersistentDataContainer().has(PENDING, PersistentDataType.STRING)) event.setCancelled(true);
            }
        }, plugin);
    }
    static void verify(BiConsumer<Boolean, String> check, World world, boolean restart) throws Exception {
        Block cashBlock = world.getBlockAt(14, 176, 8), cupboardBlock = world.getBlockAt(12, 176, 8);
        if (!restart) { place(cashBlock, "cash_box"); place(cupboardBlock, "cupboard"); }
        var cash = (CashBoxController) StorageSpecialModule.controllerAt(cashBlock);
        var cupboard = (CupboardController) StorageSpecialModule.controllerAt(cupboardBlock);
        check.accept(cash != null && cash.available() && cupboard != null && cupboard.available(), "cash box and integer cupboard have separate live position controllers");
        var saved = YamlConfiguration.loadConfiguration(FIXTURE);
        if (!restart) {
            var coins = CraftEngineItems.byId("reverie_dreams:copper_coin").buildBukkitItem(); coins.setAmount(96);
            check.accept(cash.offerGui(0, coins) == null && cash.automationInventory().getMaxStackSize() == 99, "cash GUI retains native source 96 coin stack capacity");
            check.accept(cash.offerGui(26, new ItemStack(Material.STONE, 7)).getAmount() == 7 && cash.item(26) == null, "cash GUI refuses noncoin insertion");
            cashBlock.getRelative(0, 1, 0).setType(Material.HOPPER, false);
            ((Hopper) cashBlock.getRelative(0, 1, 0).getState()).getInventory().setItem(0, new ItemStack(Material.STONE, 7));
            for (int amount : new int[]{64,64,2}) check.accept(cupboard.offer(new ItemStack(Material.BEEF, amount)) == null, "cupboard accepts original beef quantity " + amount);
            check.accept(cupboard.snapshot().entries().size() == 1 && cupboard.entry(0).count() == 130, "cupboard stores 130 units as one full identity and real integer count");
            var tagged = tagged("independent", 7); cupboard.offer(tagged);
            ItemStack unstackable = new ItemStack(Material.BEEF); unstackable.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
            for (int i = 0; i < 3; i++) cupboard.offer(unstackable);
            check.accept(cupboard.snapshot().entries().size() == 3 && cupboard.take(2, 64).getAmount() == 1 && cupboard.entry(2).count() == 2,
                    "cupboard distinguishes full metadata and debits only one unstackable food");
            check.accept(cupboard.offer(new ItemStack(Material.APPLE)).getAmount() == 1 && cupboard.offer(new ItemStack(Material.STONE)).getAmount() == 1,
                    "cupboard preserves source food filter rather than accepting every edible item");
            for (int i = 0; i < 33; i++) if (cupboard.offer(tagged("kind-" + i, 1)) != null) throw new AssertionError("Identity capacity filled too early");
            long total = cupboard.snapshot().totalUnits();
            check.accept(cupboard.snapshot().entries().size() == 36 && cupboard.offer(tagged("overflow", 1)).getAmount() == 1 && cupboard.snapshot().totalUnits() == total,
                    "full 36-kind cupboard returns the entire unaccepted stack without debit");
            for (int i = 3; i < 36; i++) cupboard.take(i, 64);
            var destination = Bukkit.createInventory(null, 9); for (int i = 0; i < 9; i++) destination.setItem(i, new ItemStack(Material.STONE, 64));
            check.accept(cupboard.transferTo(0, destination, 64) == 0 && cupboard.entry(0).count() == 130, "full destination cannot consume cupboard inventory");
            destination.setItem(0, new ItemStack(Material.BEEF, 63));
            check.accept(cupboard.transferTo(0, destination, 64) == 1 && cupboard.entry(0).count() == 129, "partial destination debits exactly its one accepted food");
            destination.clear(); cupboard.offer(new ItemStack(Material.BEEF));
            Block broken = world.getBlockAt(14, 176, 10); place(broken, "cupboard"); var toBreak = (CupboardController) StorageSpecialModule.controllerAt(broken);
            for (int amount : new int[]{64,64,2}) { var stack = new ItemStack(Material.BEEF, amount); stack.editMeta(meta -> meta.getPersistentDataContainer().set(PENDING, PersistentDataType.STRING, "persist")); toBreak.offer(stack); }
            CraftEngineBlocks.remove(broken, null, false, false, true);
            var queue = StorageSpecialModule.drops().snapshot().stream().filter(p -> p.location().getBlockX() == 14 && p.location().getBlockY() == 176 && p.location().getBlockZ() == 10).findFirst().orElseThrow();
            check.accept(queue.units() == 130, "cancelled item spawns retain all 130 broken cupboard units in persistent queue");
            saved.set("queue", queue.marker().toString()); saved.save(FIXTURE);
        } else {
            int stones = cash.snapshot().items().stream().filter(s -> s != null && s.getType() == Material.STONE).mapToInt(ItemStack::getAmount).sum();
            check.accept(stones == 7 && cash.item(0).getAmount() == 96 && cash.takeGui(1, 7) == null,
                    "real hopper inserts noncoins; cash native inventory persists them while GUI cannot withdraw them");
            check.accept(cupboard.entry(0).count() == 130 && cupboard.entry(1).count() == 7 && cupboard.entry(2).count() == 2 && cupboard.snapshot().totalUnits() == 139
                            && "independent".equals(cupboard.entry(1).prototype().getPersistentDataContainer().get(KIND, PersistentDataType.STRING))
                            && cupboard.entry(2).prototype().getMaxStackSize() == 1,
                    "cupboard preserves all integer counts and complete prototypes through real restart");
            UUID marker = UUID.fromString(saved.getString("queue"));
            check.accept(StorageSpecialModule.drops().snapshot().stream().anyMatch(p -> p.marker().equals(marker) && p.units() == 130)
                            && StorageSpecialModule.drops().entries(marker).getFirst().prototype().getPersistentDataContainer().has(PENDING, PersistentDataType.STRING),
                    "broken cupboard pending quantity and full item metadata survive actual restart");
            release = true;
            cash.automationInventory().clear(); ((Hopper) cashBlock.getRelative(0, 1, 0).getState()).getInventory().clear(); cashBlock.getRelative(0, 1, 0).setType(Material.AIR, false);
            CraftEngineBlocks.remove(cashBlock, null, false, false, true);
            for (int i = 0; i < 36; i++) while (cupboard.entry(i) != null) cupboard.take(i, 64);
            CraftEngineBlocks.remove(cupboardBlock, null, false, false, true);
        }
    }
    static void complete(BiConsumer<Boolean, String> check, World world, boolean restart) {
        var saved = YamlConfiguration.loadConfiguration(FIXTURE); UUID id = UUID.fromString(saved.getString("queue"));
        if (!restart) check.accept(StorageSpecialModule.drops().snapshot().stream().anyMatch(p -> p.marker().equals(id) && p.units() == 130), "cancelled drops remain conserved throughout the initial run");
        else {
            int total = 0;
            for (Item item : world.getEntitiesByClass(Item.class)) if (item.getItemStack().getPersistentDataContainer().has(PENDING, PersistentDataType.STRING)) {
                check.accept(item.getItemStack().getAmount() <= item.getItemStack().getMaxStackSize(), "restored drop uses a legal native stack size");
                total += item.getItemStack().getAmount(); item.remove();
            }
            check.accept(total == 130 && StorageSpecialModule.drops().snapshot().stream().noneMatch(p -> p.marker().equals(id)), "resumed drop queue emits exactly 130 units once and removes its empty carrier");
        }
    }
    private static ItemStack tagged(String value, int count) { var item = new ItemStack(Material.BEEF, count); item.editMeta(meta -> meta.getPersistentDataContainer().set(KIND, PersistentDataType.STRING, value)); return item; }
    private static void place(Block block, String path) { if (!CraftEngineBlocks.place(block.getLocation(), Key.of("reverie_dreams:" + path), false)) throw new AssertionError("Cannot place " + path); }
}
