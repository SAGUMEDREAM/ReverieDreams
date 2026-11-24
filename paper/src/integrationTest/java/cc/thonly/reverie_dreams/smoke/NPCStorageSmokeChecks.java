package cc.thonly.reverie_dreams.smoke;

import cc.thonly.reverie_dreams.paper.ReverieDreamsPlugin;
import cc.thonly.reverie_dreams.paper.npc.NPCInventoryTransferEvent;
import cc.thonly.reverie_dreams.paper.npc.NPCStorageSettings;
import cc.thonly.reverie_dreams.paper.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.paper.npc.NPCWorkSite;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Furnace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import cc.thonly.reverie_dreams.paper.specialitems2.SpecialItemsExpansion;

/** Actual NPC/container transactions and native furnace progression, isolated from production worlds. */
final class NPCStorageSmokeChecks {
    private static final UUID OWNER = UUID.fromString("0f705fcb-7bdd-4821-87b6-0c0103f710ad");
    private static final Path FILE = Path.of("probe-npc-storage.yml");
    private static final NPCStorageSettings SETTINGS = new NPCStorageSettings(false, 17, 2, List.of("c:ingots", "minecraft:logs"));
    private static UUID worker;
    private NPCStorageSmokeChecks() { }

    static void verify(JavaPlugin probe, BiConsumer<Boolean, String> check, World world, boolean restart) throws Exception {
        var module = ReverieDreamsPlugin.getMod().getNPC(); var service = module.service(); var ai = module.ai();
        var saved = YamlConfiguration.loadConfiguration(FILE.toFile());
        if (restart) {
            worker = UUID.fromString(saved.getString("worker"));
            check.accept(ai.inspectStorageSettings(worker).equals(SETTINGS), "NPC sorting category order and nondefault reserves survive restart");
            var furnace = ((Furnace) world.getBlockAt(6, 260, 8).getState()).getInventory();
            int total = count(service.getInventory(worker, OWNER), "minecraft:iron_ingot")
                    + count(furnace.getContents(), "minecraft:raw_iron") + count(furnace.getContents(), "minecraft:iron_ingot");
            check.accept(total == 3 && count(service.getInventory(worker, OWNER), "reverie_dreams:silver_ingot") == 1,
                    "NPC cargo and native in-flight furnace conserve all iron and custom silver across restart");
            check.accept(count(((Chest) world.getBlockAt(7, 260, 6).getState()).getInventory().getContents(), "minecraft:gold_ingot") == 2,
                    "NPC classification destination inventory persists actual restart");
            return;
        }
        for (int x = 4; x <= 8; x++) for (int z = 4; z <= 9; z++) world.getBlockAt(x, 259, z).setType(Material.STONE, false);
        worker = service.spawnRole(service.roles().iterator().next().id(), new Location(world, 6.5, 260, 6.5));
        check.accept(service.recruit(worker, OWNER), "storage worker recruits with offline owner");
        Bukkit.getEntity(worker).setGravity(false);
        var site = new NPCWorkSite(world.getUID(), 6, 260, 6, 3, 2);
        world.getBlockAt(6, 260, 8).setType(Material.FURNACE, false);
        world.getBlockAt(6, 260, 4).setType(Material.FURNACE, false);
        var iron = (Furnace) world.getBlockAt(6, 260, 8).getState();
        var silver = (Furnace) world.getBlockAt(6, 260, 4).getState();
        ItemStack[] cargo = new ItemStack[24];
        cargo[0] = new ItemStack(Material.RAW_IRON); cargo[1] = new ItemStack(Material.COAL, 3);
        cargo[2] = CraftEngineItems.byId("reverie_dreams:raw_silver").buildBukkitItem();
        cargo[3] = CraftEngineItems.byId("reverie_dreams:iroha_maple_planks").buildBukkitItem().asQuantity(3);
        service.storeInventory(worker, OWNER, cargo); ai.work(worker, OWNER, NPCWorkMode.SMELT, site);
        class Cancel implements Listener { @EventHandler public void transfer(NPCInventoryTransferEvent event) { if (event.getNPCId().equals(worker)) event.setCancelled(true); } }
        var cancellation = new Cancel(); Bukkit.getPluginManager().registerEvents(cancellation, probe);
        try { check.accept(ai.smeltOnce(worker, OWNER, iron.getBlock()) == 0 && iron.getInventory().isEmpty()
                        && service.getInventory(worker, OWNER)[0].getType() == Material.RAW_IRON,
                "cancelled NPC furnace transfer preserves both real inventories"); }
        finally { HandlerList.unregisterAll(cancellation); }
        check.accept(ai.smeltOnce(worker, OWNER, iron.getBlock()) == 1 && ai.smeltOnce(worker, OWNER, iron.getBlock()) == 3,
                "NPC supplies native smelting input and real coal separately");
        check.accept(ai.smeltOnce(worker, OWNER, silver.getBlock()) == 1 && ai.smeltOnce(worker, OWNER, silver.getBlock()) == 3,
                "NPC recognizes real CE silver recipe and configured CE wood fuel");
        service.stay(worker, OWNER);

        world.getBlockAt(5, 260, 6).setType(Material.CHEST, false);
        world.getBlockAt(7, 260, 6).setType(Material.CHEST, false);
        var input = ((Chest) world.getBlockAt(5, 260, 6).getState()).getInventory();
        var output = ((Chest) world.getBlockAt(7, 260, 6).getState()).getInventory();
        ItemFrame sourceFrame = frame(world, 5, Rotation.FLIPPED), destinationFrame = frame(world, 7, Rotation.NONE);
        input.setItem(0, new ItemStack(Material.IRON_INGOT, 3));
        ai.work(worker, OWNER, NPCWorkMode.CHEST_CLASSIFICATION, site);
        check.accept(ai.inspectStorage(sourceFrame.getLocation().getBlock().getRelative(sourceFrame.getAttachedFace())).get("rule").equals("INPUT"),
                "actual source item-frame rotation identifies INPUT storage");
        check.accept(ai.sortOnce(worker, OWNER) == 3 && input.isEmpty() && output.isEmpty()
                        && count(service.getInventory(worker, OWNER), "minecraft:iron_ingot") == 3,
                "classification first leg moves input into actual NPC cargo");
        check.accept(ai.sortOnce(worker, OWNER) == 3 && count(output.getContents(), "minecraft:iron_ingot") == 3
                        && count(service.getInventory(worker, OWNER), "minecraft:iron_ingot") == 0,
                "classification second leg deposits cargo without direct chest teleportation");
        output.clear(); destinationFrame.setRotation(Rotation.CLOCKWISE);
        cargo = service.getInventory(worker, OWNER); cargo[0] = new ItemStack(Material.GOLD_INGOT, 2); cargo[22] = new ItemStack(Material.DIAMOND_SWORD);
        service.storeInventory(worker, OWNER, cargo);
        check.accept(ai.sortOnce(worker, OWNER) == 2 && count(output.getContents(), "minecraft:gold_ingot") == 2
                        && service.getInventory(worker, OWNER)[22].getType() == Material.DIAMOND_SWORD,
                "source c:ingots category accepts gold from iron sample and excludes equipped weapon");
        cargo = service.getInventory(worker, OWNER); cargo[0] = new ItemStack(Material.IRON_INGOT, 2); service.storeInventory(worker, OWNER, cargo);
        class Interference implements Listener { @EventHandler public void transfer(NPCInventoryTransferEvent event) {
            if (event.getNPCId().equals(worker)) output.setItem(26, new ItemStack(Material.DIRT, 64));
        } }
        var interference = new Interference(); Bukkit.getPluginManager().registerEvents(interference, probe);
        try { check.accept(ai.sortOnce(worker, OWNER) == 0 && service.getInventory(worker, OWNER)[0].getAmount() == 2
                        && output.getItem(26).getAmount() == 64 && count(output.getContents(), "minecraft:iron_ingot") == 0,
                "container mutation during transfer rejects stale plan and preserves external changes"); }
        finally { HandlerList.unregisterAll(interference); }
        output.setItem(26, null); cargo = service.getInventory(worker, OWNER); cargo[0] = null; service.storeInventory(worker, OWNER, cargo);
        ai.configureStorageWork(worker, OWNER, SETTINGS); service.stay(worker, OWNER);
        saved.set("worker", worker.toString()); saved.save(FILE.toFile());
    }

    static void complete(BiConsumer<Boolean, String> check, World world, boolean restart) {
        var module = ReverieDreamsPlugin.getMod().getNPC(); var service = module.service(); var ai = module.ai();
        if (restart) { service.stay(worker, OWNER); Bukkit.getEntity(worker).remove(); return; }
        var site = new NPCWorkSite(world.getUID(), 6, 260, 6, 3, 2);
        var iron = (Furnace) world.getBlockAt(6, 260, 8).getState(); var silver = (Furnace) world.getBlockAt(6, 260, 4).getState();
        check.accept(count(iron.getInventory().getContents(), "minecraft:iron_ingot") == 1
                        && count(silver.getInventory().getContents(), "reverie_dreams:silver_ingot") == 1,
                "native furnaces actually produce iron and CE silver using native and CE fuels");
        ai.work(worker, OWNER, NPCWorkMode.SMELT, site);
        int collectedIron = ai.smeltOnce(worker, OWNER, iron.getBlock()), collectedSilver = ai.smeltOnce(worker, OWNER, silver.getBlock());
        check.accept(collectedIron == 1 && collectedSilver == 1,
                "NPC collects native/custom furnace output: iron=" + collectedIron + ", silver=" + collectedSilver
                        + ", location=" + Bukkit.getEntity(worker).getLocation());
        service.stay(worker, OWNER);
        iron.getInventory().setSmelting(new ItemStack(Material.RAW_IRON, 2));
    }

    private static ItemFrame frame(World world, int x, Rotation rotation) {
        return world.spawn(new Location(world, x + .5, 261, 6.5), ItemFrame.class, entity -> {
            entity.setFacingDirection(BlockFace.UP, true); entity.setFixed(true);
            entity.setItem(new ItemStack(Material.IRON_INGOT)); entity.setRotation(rotation);
        });
    }
    private static int count(ItemStack[] items, String id) {
        return Arrays.stream(items).filter(item -> item != null && !item.isEmpty()
                        && SpecialItemsExpansion.id(item).equals(id))
                .mapToInt(ItemStack::getAmount).sum();
    }
}
