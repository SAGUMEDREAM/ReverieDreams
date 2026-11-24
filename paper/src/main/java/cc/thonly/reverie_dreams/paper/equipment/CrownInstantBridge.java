package cc.thonly.reverie_dreams.paper.equipment;

import io.papermc.paper.event.entity.EntityEffectTickEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.DoubleSupplier;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Tag;

/** Target-version Paper emits EntityEffectTickEvent for instantaneous HealOrHarm as well as timed effects. */
final class CrownInstantBridge implements Listener {
    private record EffectKey(PotionEffectType type, int amplifier) { }
    private static final class Pending {
        final int tick = Bukkit.getCurrentTick();
        final Entity source, owner;
        final DoubleSupplier scale;
        final ArrayList<EffectKey> effects = new ArrayList<>();
        Pending(Entity source, Entity owner, DoubleSupplier scale, Collection<PotionEffect> effects) {
            this.source = source; this.owner = owner; this.scale = scale;
            for (var effect : effects) if (instant(effect.getType())) this.effects.add(new EffectKey(effect.getType(), effect.getAmplifier()));
        }
    }
    private final EquipmentModule module;
    private final Map<UUID, Pending> pending = new HashMap<>();
    CrownInstantBridge(EquipmentModule module) { this.module = module; }
    private static boolean instant(PotionEffectType type) {
        return type.equals(PotionEffectType.INSTANT_HEALTH) || type.equals(PotionEffectType.INSTANT_DAMAGE);
    }
    private void capture(LivingEntity entity, Entity source, Entity owner, DoubleSupplier scale, Collection<PotionEffect> effects) {
        if (!module.wearsCrown(entity) || module.isVisualCopy(entity)) return;
        var context = new Pending(source, owner, scale, effects);
        if (!context.effects.isEmpty()) pending.put(entity.getUniqueId(), context);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSplash(PotionSplashEvent event) {
        var potion = event.getPotion(); Entity owner = potion.getShooter() instanceof Entity entity ? entity : null;
        for (var target : event.getAffectedEntities()) capture(target, potion, owner,
                () -> event.isCancelled() ? -1 : event.getIntensity(target), potion.getEffects());
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCloud(AreaEffectCloudApplyEvent event) {
        var cloud = event.getEntity(); Entity owner = cloud.getSource() instanceof Entity entity ? entity : null;
        var effects = new ArrayList<PotionEffect>();
        if (cloud.getBasePotionType() != null) effects.addAll(cloud.getBasePotionType().getPotionEffects());
        effects.addAll(cloud.getCustomEffects());
        for (var target : event.getAffectedEntities()) capture(target, cloud, owner,
                () -> event.isCancelled() || !event.getAffectedEntities().contains(target) ? -1 : 0.5, effects);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInstant(EntityEffectTickEvent event) {
        if (!instant(event.getType()) || !module.wearsCrown(event.getEntity()) || module.isVisualCopy(event.getEntity())) return;
        double scale = 1.0; Entity source = null, owner = null;
        var context = pending.get(event.getEntity().getUniqueId());
        if (context != null && context.tick == Bukkit.getCurrentTick()
                && context.effects.remove(new EffectKey(event.getType(), event.getAmplifier()))) {
            double captured = context.scale.getAsDouble();
            if (captured >= 0) { scale = captured; source = context.source; owner = context.owner; }
            if (context.effects.isEmpty()) pending.remove(event.getEntity().getUniqueId());
        }
        event.setCancelled(true);
        module.applyCrownInstant(event.getEntity(), event.getType(), event.getAmplifier(), scale, source, owner);
    }
    void expire() { int tick = Bukkit.getCurrentTick(); pending.values().removeIf(value -> value.tick != tick); }
    void clear() { pending.clear(); }

    static void apply(LivingEntity entity, boolean isHarm, int amplifier, double scale, Entity source, Entity owner) {
        if (entity.isDead() || !entity.isValid()) return;
        boolean inverted = Tag.ENTITY_TYPES_INVERTED_HEALING_AND_HARM.isTagged(entity.getType());
        if (isHarm == inverted) {
            int amount = (int) (scale * (6 << amplifier) + 0.5);
            if (amount <= 0) return;
            var builder = DamageSource.builder(source == null ? DamageType.MAGIC : DamageType.INDIRECT_MAGIC);
            if (source != null) builder.withDirectEntity(source);
            if (owner != null) builder.withCausingEntity(owner);
            entity.damage(amount, builder.build());
        } else {
            int amount = (int) (scale * (4 << amplifier) + 0.5);
            if (amount <= 0) return;
            var heal = new EntityRegainHealthEvent(entity, amount, EntityRegainHealthEvent.RegainReason.MAGIC);
            Bukkit.getPluginManager().callEvent(heal);
            if (!heal.isCancelled() && heal.getAmount() > 0 && !entity.isDead()) {
                var max = entity.getAttribute(Attribute.MAX_HEALTH);
                if (max != null) entity.setHealth(Math.min(max.getValue(), entity.getHealth() + heal.getAmount()));
            }
        }
    }
}
