package cc.thonly.reverie_dreams.paper.farming;

import cc.thonly.reverie_dreams.paper.util.Ids;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;

/** Original additive seed/grape loot only; keeps native loot and other modules' extra pools. */
public final class FarmingLootBridge implements Listener {
    public record SeedDrop(String id, int amount) { }
    public static final List<String> GRASS_SEEDS = List.of("tomato", "red_beans", "onion", "cucumber", "chill", "broccoli", "soy_beans");
    public static final List<String> CHEST_SEEDS = List.of("sweet_potato", "white_radish", "toon", "red_beans", "grape");
    public static final Set<String> CHESTS = Set.of("minecraft:chests/village/village_weaponsmith", "minecraft:chests/end_city_treasure",
            "minecraft:chests/abandoned_mineshaft", "minecraft:chests/nether_bridge", "minecraft:chests/village/village_plains_house",
            "minecraft:chests/village/village_savanna_house", "minecraft:chests/pillager_outpost", "minecraft:chests/simple_dungeon",
            "minecraft:chests/shipwreck_supply", "minecraft:chests/shipwreck_treasure", "minecraft:chests/desert_pyramid");
    public static Optional<String> grassDrop(RandomGenerator random) {
        return random.nextFloat() < .05f ? Optional.of(Ids.MOD_ID + ":" + GRASS_SEEDS.get(random.nextInt(GRASS_SEEDS.size())) + "_seeds") : Optional.empty();
    }
    public static List<SeedDrop> chestDrops(RandomGenerator random) {
        if (random.nextFloat() >= .24f) return List.of();
        int rolls = random.nextInt(1, 3); var drops = new ArrayList<SeedDrop>(rolls);
        for (int i = 0; i < rolls; i++) drops.add(new SeedDrop(Ids.MOD_ID + ":" + CHEST_SEEDS.get(random.nextInt(CHEST_SEEDS.size())) + "_seeds", 1));
        return List.copyOf(drops);
    }
    private static ItemStack item(String id, int count) {
        var type = CraftEngineItems.byId(Key.of(id));
        if (type == null) throw new IllegalStateException("Missing migrated farming loot " + id);
        var result = type.buildBukkitItem(); result.setAmount(count); return result;
    }
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockDrops(BlockDropItemEvent event) {
        if (!FarmingModule.enabled()) return;
        var original = event.getBlockState().getType(); var random = ThreadLocalRandom.current();
        if (original == Material.SHORT_GRASS) grassDrop(random).ifPresent(id ->
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation().add(.5, .5, .5), item(id, 1)));
        if (original == Material.VINE && event.getPlayer().getInventory().getItemInMainHand().getType().name().endsWith("_HOE")
                && random.nextFloat() < .1f) event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation().add(.5, .5, .5), item(Ids.MOD_ID + ":ingredient/grape", 1));
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChestLoot(LootGenerateEvent event) {
        if (!FarmingModule.enabled() || !CHESTS.contains(event.getLootTable().getKey().asString())) return;
        for (var drop : chestDrops(ThreadLocalRandom.current())) event.getLoot().add(item(drop.id(), drop.amount()));
    }
}
