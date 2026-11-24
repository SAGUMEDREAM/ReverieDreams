package cc.thonly.reverie_dreams.smoke;

import cc.thonly.reverie_dreams.paper.ReverieDreamsPlugin;
import cc.thonly.reverie_dreams.paper.farming.FarmingModule;
import cc.thonly.reverie_dreams.paper.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.paper.npc.NPCWorkSite;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.bukkit.DyeColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Item;
import org.bukkit.entity.Wolf;

/** Source AI transaction and restart probes with an offline owner; no fake Player or NMS. */
final class NPCAiSmokeChecks {
    static void verify(JavaPlugin probe, BiConsumer<Boolean, String> check, World world, boolean restart) throws Exception {
        var module = ReverieDreamsPlugin.getMod().getNPC(); var service = module.service(); var ai = module.ai();
        UUID owner = UUID.fromString("67fae7e5-e1fa-4d94-afc2-0e7bec89ea80");
        var file = Path.of("probe-ai-fixture.yml").toFile(); var saved = YamlConfiguration.loadConfiguration(file);
        if (restart) {
            UUID id = UUID.fromString(saved.getString("worker")); var work = ai.inspectWork(id);
            check.accept(work.working() && work.workMode() == NPCWorkMode.FARM && work.completedActions() >= 2
                            && work.site().worldId().equals(world.getUID()) && work.site().x() == 3 && work.site().y() == 175 && work.site().z() == 9,
                    "NPC work mode site and action count survive actual server restart");
            check.accept(service.getInventory(id, owner)[0].getAmount() == 7 && service.getInventory(id, owner)[22].getType() == Material.DIAMOND_SWORD,
                    "NPC work persistence retains independent inventory and equipment");
            check.accept(ai.inspectHusbandry(id).get("honeyProduct").equals("honeycomb") && Boolean.TRUE.equals(ai.inspectHusbandry(id).get("requireSmoke")),
                    "NPC harvesting preferences survive actual restart");
            service.stay(id, owner); Bukkit.getEntity(id).remove();
            for (int x = 1; x <= 5; x++) for (int z = 7; z <= 11; z++) world.getBlockAt(x, 174, z).setType(Material.AIR, false);
            return;
        }
        for (int x = 1; x <= 5; x++) for (int z = 7; z <= 11; z++) world.getBlockAt(x, 174, z).setType(Material.STONE, false);
        UUID id = service.spawnRole(service.roles().stream().findFirst().orElseThrow().id(), new Location(world, 3.5, 175, 9.5));
        check.accept(service.recruit(id, owner), "NPC AI fixture recruits with offline owner");
        ItemStack[] inventory = new ItemStack[24]; inventory[0] = item("reverie_dreams:silver_ingot"); inventory[0].setAmount(7);
        inventory[22] = new ItemStack(Material.DIAMOND_SWORD); inventory[23] = new ItemStack(Material.SHIELD);
        service.storeInventory(id, owner, inventory); var original = service.getInventory(id, owner);
        ai.work(id, owner, NPCWorkMode.FARM, new NPCWorkSite(world.getUID(), 3, 175, 9, 2, 1));
        Block soil = world.getBlockAt(2, 174, 9), crop = soil.getRelative(0, 1, 0);
        CraftEngineBlocks.place(soil.getLocation(), Key.of("reverie_dreams:paddy_field"), false);
        CraftEngineBlocks.place(crop.getLocation(), Key.of("reverie_dreams:chill"), false);
        FarmingModule.advance(crop, 100);
        check.accept(ai.farmOnce(id, owner, crop) && FarmingModule.age(crop) == 0,
                "NPC harvests original mature CE crop and replants its same seed");
        var harvested = service.getInventory(id, owner);
        check.accept(count(harvested, "reverie_dreams:ingredient/chili") >= 1
                        && count(harvested, "reverie_dreams:ingredient/chili") <= 3
                        && count(harvested, "reverie_dreams:chill_seeds") >= 1 && count(harvested, "reverie_dreams:chill_seeds") <= 2
                        && harvested[0].getAmount() == 7 && harvested[22].getType() == Material.DIAMOND_SWORD && harvested[23].getType() == Material.SHIELD,
                "NPC collects source produce and spends one dropped seed without touching stored gear");
        FarmingModule.advance(crop, 100); var before = service.getInventory(id, owner); int age = FarmingModule.age(crop);
        Listener cancellation = new Listener() {
            @EventHandler public void change(EntityChangeBlockEvent event) { if (event.getEntity().getUniqueId().equals(id)) event.setCancelled(true); }
        };
        Bukkit.getPluginManager().registerEvents(cancellation, probe);
        try {
            check.accept(!ai.farmOnce(id, owner, crop) && age == FarmingModule.age(crop) && Arrays.equals(before, service.getInventory(id, owner)),
                    "cancelled NPC harvest leaves crop and all inventory quantities unchanged");
        } finally { HandlerList.unregisterAll(cancellation); }
        CraftEngineBlocks.remove(crop, null, false, false, true);
        var planting = Arrays.stream(original).map(ItemStack::clone).toArray(ItemStack[]::new);
        planting[23] = item("reverie_dreams:chill_seeds"); planting[23].setAmount(2); service.storeInventory(id, owner, planting);
        check.accept(ai.farmOnce(id, owner, crop) && FarmingModule.age(crop) == 0 && service.getInventory(id, owner)[23].getAmount() == 1,
                "NPC planting spends exactly one held custom seed");
        CraftEngineBlocks.remove(crop, null, false, false, true); CraftEngineBlocks.remove(soil, null, false, false, true);
        soil.setType(Material.STONE, false); service.storeInventory(id, owner, original); service.stay(id, owner);
        var carrier = Bukkit.getEntity(id);
        Sheep target = world.spawn(carrier.getLocation().add(.9, 0, 0), Sheep.class, sheep -> {
            sheep.setAI(false); sheep.setGravity(false); sheep.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20); sheep.setHealth(20);
        });
        try {
            check.accept(ai.attackOnce(id, target) && target.getHealth() < 20
                            && service.getInventory(id, owner)[22].getDataOrDefault(DataComponentTypes.DAMAGE, 0) == 1,
                    "NPC real native melee deals damage and charges exactly source one durability");
        } finally { target.remove(); }
        service.storeInventory(id, owner, original);
        verifyHusbandry(check, world, id, owner, original);
        ai.work(id, owner, NPCWorkMode.FARM, new NPCWorkSite(world.getUID(), 3, 175, 9, 2, 1));
        saved.set("worker", id.toString()); saved.save(file);
    }
    private static void verifyHusbandry(BiConsumer<Boolean, String> check, World world, UUID id, UUID owner, ItemStack[] original) {
        var module = ReverieDreamsPlugin.getMod().getNPC(); var service = module.service(); var ai = module.ai();
        var carrier = (Wolf) Bukkit.getEntity(id);
        var inventory = Arrays.stream(original).map(ItemStack::clone).toArray(ItemStack[]::new);
        inventory[5] = new ItemStack(Material.WHEAT, 4); inventory[6] = new ItemStack(Material.SHEARS); inventory[7] = new ItemStack(Material.GLASS_BOTTLE, 2);
        service.storeInventory(id, owner, inventory);
        var site = new NPCWorkSite(world.getUID(), 3, 175, 9, 2, 2);
        var first = world.spawn(new Location(world, 3, 175, 9), Cow.class, cow -> { cow.setAI(false); cow.setAge(0); });
        var second = world.spawn(new Location(world, 4, 175, 9), Cow.class, cow -> { cow.setAI(false); cow.setAge(0); });
        try {
            ai.work(id, owner, NPCWorkMode.BREED, site);
            check.accept(ai.breedOnce(id, owner, first, second) == 2 && first.isLoveMode() && second.isLoveMode()
                            && Arrays.stream(service.getInventory(id, owner)).filter(i -> i.getType() == Material.WHEAT).mapToInt(ItemStack::getAmount).sum() == 2,
                    "NPC feeds both real animals while retaining original two-food reserve");
            check.accept(service.getInventory(id, owner)[22].getType() == Material.DIAMOND_SWORD && service.getInventory(id, owner)[23].getType() == Material.SHIELD
                            && !Boolean.TRUE.equals(ai.inspectHusbandry(id).get("toolBorrowed")), "breeding restores borrowed hand slots and their equipment");
            first.setLoveModeTicks(0); second.setLoveModeTicks(0);
            check.accept(ai.breedOnce(id, owner, first, second) == 0, "NPC refuses breeding below original food reserve");
        } finally { first.remove(); second.remove(); }
        var sheep = world.spawn(new Location(world, 4, 175, 9), Sheep.class, animal -> { animal.setAI(false); animal.setAdult(); animal.setSheared(false); animal.setColor(DyeColor.WHITE); });
        try {
            ai.work(id, owner, NPCWorkMode.SHEEP_SHEARING, site);
            check.accept(ai.shearOnce(id, owner, sheep) && sheep.isSheared() && service.getInventory(id, owner)[6].getDataOrDefault(DataComponentTypes.DAMAGE, 0) == 1,
                    "NPC shears a real sheep and charges its borrowed source shears one durability");
            check.accept(!ai.shearOnce(id, owner, sheep) && service.getInventory(id, owner)[6].getDataOrDefault(DataComponentTypes.DAMAGE, 0) == 1,
                    "already shorn sheep cannot consume tool wear twice");
            int wool = 0; for (var drop : world.getEntitiesByClass(Item.class)) if (drop.getItemStack().getType() == Material.WHITE_WOOL && drop.getLocation().distanceSquared(sheep.getLocation()) < 9) {
                wool += drop.getItemStack().getAmount(); drop.remove();
            }
            check.accept(wool >= 1 && wool <= 3, "native sheep shear produces real original-range wool drops");
        } finally { sheep.remove(); }
        Block campfire = world.getBlockAt(2, 174, 8), hive = world.getBlockAt(2, 176, 8);
        campfire.setType(Material.CAMPFIRE, false); hive.getRelative(0, -1, 0).setType(Material.AIR, false); hive.setType(Material.BEEHIVE, false);
        var honey = (Beehive) hive.getBlockData(); honey.setHoneyLevel(5); hive.setBlockData(honey, false);
        ai.work(id, owner, NPCWorkMode.HONEYCOMB_HARVESTING, site); ai.configureHoneyWork(id, owner, true, "honey");
        check.accept(((org.bukkit.block.Beehive) hive.getState()).isSedated() && ai.honeyOnce(id, owner, hive)
                        && ((Beehive) hive.getBlockData()).getHoneyLevel() == 0
                        && service.getInventory(id, owner)[7].getAmount() == 1
                        && Arrays.stream(service.getInventory(id, owner)).filter(i -> i.getType() == Material.HONEY_BOTTLE).mapToInt(ItemStack::getAmount).sum() == 1,
                "NPC smoky hive harvest commits honey level and spends exactly one real bottle");
        honey.setHoneyLevel(5); hive.setBlockData(honey, false); ai.configureHoneyWork(id, owner, true, "honeycomb");
        check.accept(ai.honeyOnce(id, owner, hive) && service.getInventory(id, owner)[6].getDataOrDefault(DataComponentTypes.DAMAGE, 0) == 2,
                "NPC comb harvest charges actual shears one additional durability");
        int comb = 0; for (var drop : world.getEntitiesByClass(Item.class)) if (drop.getItemStack().getType() == Material.HONEYCOMB && drop.getLocation().distanceSquared(hive.getLocation()) < 9) { comb += drop.getItemStack().getAmount(); drop.remove(); }
        check.accept(comb == 3, "NPC comb harvest creates three real native honeycomb items");
        hive.setType(Material.AIR, false); campfire.setType(Material.STONE, false);
        var armored = Arrays.stream(original).map(ItemStack::clone).toArray(ItemStack[]::new);
        armored[18] = new ItemStack(Material.DIAMOND_HELMET); armored[19] = new ItemStack(Material.DIAMOND_CHESTPLATE);
        armored[20] = new ItemStack(Material.DIAMOND_LEGGINGS); armored[21] = new ItemStack(Material.DIAMOND_BOOTS);
        service.storeInventory(id, owner, armored); service.stay(id, owner);
        double maximum = carrier.getAttribute(Attribute.MAX_HEALTH).getBaseValue(); carrier.getAttribute(Attribute.MAX_HEALTH).setBaseValue(40); carrier.setHealth(40);
        carrier.setNoDamageTicks(0);
        carrier.damage(8, DamageSource.builder(DamageType.MOB_ATTACK).build());
        var after = service.getInventory(id, owner);
        for (int slot = 18; slot <= 21; slot++) check.accept(after[slot].getDataOrDefault(DataComponentTypes.DAMAGE, 0) == 2,
                "NPC humanoid armor uses original pre-armor eight damage wear of two: slot " + slot);
        carrier.setNoDamageTicks(0); carrier.damage(2);
        for (int slot = 18; slot <= 21; slot++) check.accept(service.getInventory(id, owner)[slot].getDataOrDefault(DataComponentTypes.DAMAGE, 0) == 2,
                "native generic armor-bypassing damage does not wear NPC armor: slot " + slot);
        carrier.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maximum); carrier.setHealth(Math.min(carrier.getHealth(), carrier.getAttribute(Attribute.MAX_HEALTH).getValue()));
        service.storeInventory(id, owner, original);
    }
    private static ItemStack item(String id) { return CraftEngineItems.byId(id).buildBukkitItem(); }
    private static int count(ItemStack[] items, String id) {
        int count = 0; for (var item : items) if (Key.of(id).equals(CraftEngineItems.getCustomItemId(item))) count += item.getAmount(); return count;
    }
}
