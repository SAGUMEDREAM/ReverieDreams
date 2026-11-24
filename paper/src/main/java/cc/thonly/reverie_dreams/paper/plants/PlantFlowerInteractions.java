package cc.thonly.reverie_dreams.paper.plants;

import cc.thonly.reverie_dreams.paper.util.Ids;

import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.libraries.antigrieflib.Flag;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.SoundCategory;
import org.bukkit.entity.MushroomCow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.potion.PotionEffectType;
import io.papermc.paper.potion.SuspiciousEffectEntry;
import java.util.List;

/** Source FlowerBlock is a SuspiciousEffectHolder; a CE paper block item needs this public Paper bridge. */
final class PlantFlowerInteractions implements Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFeed(PlayerInteractEntityEvent event) {
        if (!PlantsModule.enabled() || event instanceof PlayerInteractAtEntityEvent
                || !(event.getRightClicked() instanceof MushroomCow cow)
                || cow.getVariant() != MushroomCow.Variant.BROWN || !cow.isAdult()) return;
        var player = event.getPlayer(); var item = player.getInventory().getItem(event.getHand());
        var id = CraftEngineItems.getCustomItemId(item);
        if (id == null || !id.asString().equals(Ids.MOD_ID + ":udumbara_flower") && !id.asString().equals(Ids.MOD_ID + ":tremella")) return;
        event.setCancelled(true);
        if (!BukkitCraftEngine.instance().antiGriefProvider().test(player, Flag.INTERACT_ENTITY, cow)) return;
        if (cow.hasEffectsForNextStew()) {
            cow.getWorld().spawnParticle(Particle.SMOKE, cow.getLocation().add(0, cow.getHeight() * .5, 0), 2, .25, 0, .25, .05);
            return;
        }
        cow.setStewEffects(List.of(SuspiciousEffectEntry.create(PotionEffectType.REGENERATION, 60)));
        if (player.getGameMode() != GameMode.CREATIVE) { item.subtract(1); player.getInventory().setItem(event.getHand(), item); }
        cow.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, cow.getLocation().add(0, cow.getHeight() * .5, 0), 4, .25, 0, .25, .05);
        cow.getWorld().playSound(cow.getLocation(), "minecraft:entity.mooshroom.eat", SoundCategory.NEUTRAL, 2.0f, 1.0f);
        player.swingHand(event.getHand());
    }
}
