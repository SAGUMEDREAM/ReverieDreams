package cc.thonly.reverie_dreams.paper.equipment;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Tag;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/** Only the registered LIVING_HURT hook is ported; the source's unused post-hit method is not added twice. */
final class SilverDamageBridge implements Listener {
    private final EquipmentModule module;
    private final Set<UUID> applying = new HashSet<>();
    SilverDamageBridge(EquipmentModule module) { this.module = module; }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof LivingEntity attacker) || !(event.getEntity() instanceof LivingEntity target)
                || !Tag.ENTITY_TYPES_UNDEAD.isTagged(target.getType()) || applying.contains(target.getUniqueId())
                || attacker.getEquipment() == null || !module.hasSilverMarker(attacker.getEquipment().getItemInMainHand())) return;
        int invulnerability = target.getNoDamageTicks(); double previousDamage = target.getLastDamage();
        applying.add(target.getUniqueId());
        try {
            // Native damage applies the additional 2 with its own armor/magic/absorption reduction, unlike adding 2 to the base hit.
            target.setNoDamageTicks(0); target.damage(2.0, event.getDamageSource());
        } finally {
            target.setNoDamageTicks(invulnerability); target.setLastDamage(previousDamage); applying.remove(target.getUniqueId());
        }
    }
}
