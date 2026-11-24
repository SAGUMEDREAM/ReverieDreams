package cc.thonly.reverie_dreams.smoke;

import cc.thonly.reverie_dreams.paper.danmaku.DanmakuMath;
import cc.thonly.reverie_dreams.paper.danmaku.DanmakuModule;
import cc.thonly.reverie_dreams.paper.specialitems2.ChequeService;
import cc.thonly.reverie_dreams.paper.specialitems2.MoneyRules;
import cc.thonly.reverie_dreams.paper.specialitems2.SpecialItemsExpansion;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

/** Native server tests of shared production services; player-only use is a separate client acceptance test. */
final class ExpansionSmokeChecks {
    private static Set<UUID> expiringMarkers = Set.of();
    private ExpansionSmokeChecks() { }

    static void verify(BiConsumer<Boolean, String> check, World world) {
        var service = SpecialItemsExpansion.service();
        for (String id : new String[]{"upgraded_health", "bomb", "cheque", "treasure_hunting_rod"})
            check.accept(service.create(id) != null, "special expansion item builds: " + id);
        var cheque = service.create("cheque");
        cheque.editPersistentDataContainer(data -> {
            data.set(ChequeService.AMOUNT, PersistentDataType.INTEGER, Integer.MAX_VALUE);
            data.set(ChequeService.OWNER, PersistentDataType.STRING, "source_player_name");
            data.set(ChequeService.NAME, PersistentDataType.STRING, "{\"text\":\"Signed name\"}");
        });
        service.cheques().refreshTooltip(cheque);
        var restored = ItemStack.deserializeBytes(cheque.serializeAsBytes());
        check.accept(service.cheques().inspect(restored).equals(service.cheques().inspect(cheque))
                        && service.cheques().inspect(restored).amount() == Integer.MAX_VALUE,
                "cheque signed name and full integer value survive native item serialization");
        check.accept(MoneyRules.calculate(Integer.MAX_VALUE).value() == Integer.MAX_VALUE
                        && MoneyRules.add(Integer.MAX_VALUE, 1).isEmpty()
                        && MoneyRules.denomination("reverie_dreams:silver_coin") == 10,
                "cheque accounting conserves max integer and rejects overflow independently of Faith");

        // All scan chunks are preloaded flat-world fixtures; no source-map generation occurs here.
        for (int x = -1; x <= 0; x++) for (int z = -1; z <= 0; z++) world.getChunkAt(x, z);
        var origin = new Location(world, 6, 235, 6);
        world.getBlockAt(5, 235, 5).setType(Material.DIAMOND_ORE, false);
        world.getBlockAt(6, 235, 5).setType(Material.IRON_ORE, false);
        for (var fixture : new Object[][]{{9, "silver_ore"}, {10, "orb_ore"}}) {
            String id = "reverie_dreams:" + fixture[1];
            check.accept(CraftEngineBlocks.place(new Location(world, (int) fixture[0], 235, 9),
                    CraftEngineBlocks.byId(Key.of(id)).defaultState(), false), "place actual custom scan ore: " + id);
        }
        var rod = service.rod();
        var scan = rod.inspect(origin);
        check.accept(scan.ores().size() == 4 && scan.veins().size() == 2
                        && scan.veins().getFirst().size() == 2 && scan.targets().size() == 3,
                "rod scans vanilla and CE ores and groups mixed ore ids by six-neighbour adjacency");
        check.accept(rod.spawn(origin, scan) == 3 && rod.snapshots().size() == 3,
                "rod creates native glowing source-selected markers");
        Set<UUID> original = new HashSet<>();
        rod.snapshots().forEach(marker -> original.add(marker.entity()));
        rod.spawn(origin, scan);
        check.accept(rod.snapshots().size() == 3 && rod.snapshots().stream().allMatch(marker -> original.contains(marker.entity())),
                "repeated scan refreshes markers without duplicate entities");

        var actor = world.spawn(origin, Sheep.class, entity -> { entity.setAI(false); entity.setGravity(false); });
        var bullets = DanmakuModule.service();
        var base = bullets.rules().create("amulet", "red");
        var projectile = bullets.rules().configure(base, bullets.rules().properties(base).withDamage(0), DanmakuMath.Config.EMPTY);
        UUID own = bullets.launch(actor, projectile, origin.clone().add(0, 1, 0), 0, 0).getFirst();
        UUID hostile = bullets.launch(null, projectile, origin.clone().add(0, 2, 0), 0, 0).getFirst();
        var targets = service.bombs().targets(actor);
        check.accept(targets.size() == 2 && targets.stream().noneMatch(target -> original.contains(target.id()))
                        && targets.stream().anyMatch(target -> target.id().equals(hostile) && target.hostileBullet())
                        && targets.stream().anyMatch(target -> target.id().equals(own) && !target.hostileBullet()),
                "Bomb excludes real ore markers and classifies friendly and hostile ordinary projectiles");
        double health = actor.getHealth();
        var outcome = service.bombs().apply(actor, new Random(21));
        check.accept(outcome.cancelledBullets() == 1 && outcome.otherEntities() == 1
                        && outcome.pointItems() >= 2 && outcome.pointItems() <= 5 && actor.getHealth() == health
                        && bullets.snapshots().stream().noneMatch(bullet -> bullet.id().equals(hostile))
                        && bullets.cancel(own),
                "Bomb cancels hostile bullet once, keeps own bullet, emits points and causes no damage");
        int drops = 0;
        for (var entity : world.getNearbyEntities(origin, 3, 4, 3)) if (entity instanceof Item item) {
            if (SpecialItemsExpansion.id(item.getItemStack()).equals("reverie_dreams:point")) {
                drops += item.getItemStack().getAmount(); item.remove();
            }
        }
        check.accept(drops == outcome.pointItems(), "Bomb reports exactly the real emitted point items");
        actor.remove();
        expiringMarkers = Set.copyOf(original);
    }

    static void complete(BiConsumer<Boolean, String> check, World world) {
        var rod = SpecialItemsExpansion.service().rod();
        check.accept(rod.snapshots().stream().noneMatch(marker -> expiringMarkers.contains(marker.entity()))
                        && expiringMarkers.stream().allMatch(id -> Bukkit.getEntity(id) == null),
                "rod markers expire after 100 world ticks without leaking displays");
        for (int[] pos : new int[][]{{5, 5}, {6, 5}, {9, 9}, {10, 9}}) {
            var block = world.getBlockAt(pos[0], 235, pos[1]);
            if (CraftEngineBlocks.getCustomBlockState(block) != null) CraftEngineBlocks.remove(block, null, false, false, true);
            block.setType(Material.AIR, false);
        }
    }
}
