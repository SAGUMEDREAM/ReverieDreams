package cc.thonly.reverie_dreams.paper.food;

import com.google.gson.JsonParser;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Applies only effects/bonuses absent from the native CE consumable component. */
@SuppressWarnings("deprecation")
public final class FoodConsumptionModule implements Listener {
    public static final NamespacedKey FOOD_OVERRIDE = new NamespacedKey("reverie_dreams", "food_properties");
    public static final NamespacedKey DRINK_OVERRIDE = new NamespacedKey("reverie_dreams", "beverage_properties");
    private static final NamespacedKey ALCOHOL_PROTECTION = new NamespacedKey("reverie_dreams", "anti_alcohol_ticks");
    private final JavaPlugin plugin;
    private final FoodRules rules;
    private final Map<UUID, Integer> protection = new HashMap<>();
    private BukkitTask ticker;

    public FoodConsumptionModule(JavaPlugin plugin) {
        this.plugin = plugin;
        var resource = plugin.getResource("migration/food-domain.json");
        if (resource == null) throw new IllegalStateException("Missing food domain rules");
        try (var reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
            rules = new FoodRules(JsonParser.parseReader(reader).getAsJsonObject());
        } catch (Exception failure) {
            throw new IllegalStateException("Cannot load food domain rules", failure);
        }
    }

    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        for (var world : Bukkit.getWorlds()) for (LivingEntity entity : world.getLivingEntities()) restore(entity);
        ticker = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            var iterator = protection.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                var entity = Bukkit.getEntity(entry.getKey());
                if (!(entity instanceof LivingEntity living) || !living.isValid()) {
                    iterator.remove();
                } else if (Bukkit.getServer().getServerTickManager().isFrozen(living)) {
                    // Source anti-alcohol is an entity-ticked MobEffect. Paper includes player/passenger and tick-step exceptions.
                    continue;
                } else if (entry.getValue() <= 1) {
                    living.getPersistentDataContainer().remove(ALCOHOL_PROTECTION);
                    iterator.remove();
                } else {
                    entry.setValue(entry.getValue() - 1);
                    if (entry.getValue() % 20 == 0) persist(living, entry.getValue());
                }
            }
        }, 1L, 1L);
    }

    public void disable() {
        if (ticker != null) ticker.cancel();
        for (var entry : protection.entrySet()) {
            var entity = Bukkit.getEntity(entry.getKey());
            if (entity instanceof LivingEntity living) persist(living, entry.getValue());
        }
        protection.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack consumed = event.getItem().clone();
        UUID id = event.getPlayer().getUniqueId();
        // The consume event precedes vanilla food/effects and milk's clear-effects.
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(id);
            if (player != null && player.isOnline() && !player.isDead()) apply(player, consumed);
        });
    }

    public FoodRules.Plan apply(LivingEntity entity, ItemStack consumed) {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Food effects require the owning server thread");
        if (!protection.containsKey(entity.getUniqueId())) restore(entity);
        var custom = CraftEngineItems.getCustomItemId(consumed);
        String id = custom == null ? consumed.getType().getKey().toString() : custom.asString();
        FoodRules.Plan plan = rules.plan(id, override(consumed, FOOD_OVERRIDE), override(consumed, DRINK_OVERRIDE),
                consumed.hasItemMeta() && consumed.getItemMeta().hasFood(), protectionTicks(entity) > 0,
                ThreadLocalRandom.current());
        if (entity instanceof Player player && plan.nutrition() > 0) {
            player.setFoodLevel(Math.min(20, player.getFoodLevel() + plan.nutrition()));
            player.setSaturation(Math.min(player.getFoodLevel(), player.getSaturation() + plan.saturation()));
        }
        for (FoodRules.Effect effect : plan.effects()) {
            PotionEffectType type = PotionEffectType.getByKey(NamespacedKey.fromString(effect.id()));
            if (type == null) throw new IllegalStateException("Unknown source food effect: " + effect.id());
            entity.addPotionEffect(new PotionEffect(type, effect.duration(), effect.amplifier(), false, true, true));
        }
        if (plan.clearFire()) entity.setFireTicks(0);
        if (plan.fireTicks() > 0) entity.setFireTicks(Math.max(entity.getFireTicks(), plan.fireTicks()));
        if (plan.alcoholProtectionTicks() > 0) {
            protection.put(entity.getUniqueId(), Math.max(protectionTicks(entity), plan.alcoholProtectionTicks()));
            persist(entity, protection.get(entity.getUniqueId()));
        }
        return plan;
    }

    public int protectionTicks(LivingEntity entity) {
        return protection.getOrDefault(entity.getUniqueId(), entity.getPersistentDataContainer().getOrDefault(ALCOHOL_PROTECTION, PersistentDataType.INTEGER, 0));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) { restore(event.getPlayer()); }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Integer remaining = protection.remove(event.getPlayer().getUniqueId());
        if (remaining != null) persist(event.getPlayer(), remaining);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        protection.remove(event.getEntity().getUniqueId());
        event.getEntity().getPersistentDataContainer().remove(ALCOHOL_PROTECTION);
    }

    @EventHandler
    public void onLoad(EntitiesLoadEvent event) {
        for (var entity : event.getEntities()) if (entity instanceof LivingEntity living) restore(living);
    }

    @EventHandler
    public void onUnload(EntitiesUnloadEvent event) {
        for (var entity : event.getEntities()) if (entity instanceof LivingEntity living) {
            Integer remaining = protection.remove(living.getUniqueId());
            if (remaining != null) persist(living, remaining);
        }
    }

    private void restore(LivingEntity entity) {
        int remaining = entity.getPersistentDataContainer().getOrDefault(ALCOHOL_PROTECTION, PersistentDataType.INTEGER, 0);
        if (remaining > 0) protection.put(entity.getUniqueId(), remaining);
    }

    private static void persist(LivingEntity entity, int remaining) {
        if (remaining > 0) entity.getPersistentDataContainer().set(ALCOHOL_PROTECTION, PersistentDataType.INTEGER, remaining);
        else entity.getPersistentDataContainer().remove(ALCOHOL_PROTECTION);
    }

    private List<String> override(ItemStack item, NamespacedKey key) {
        if (!item.hasItemMeta()) return List.of();
        String json = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (json == null || json.isBlank()) return List.of();
        try {
            List<String> values = new ArrayList<>();
            for (var value : JsonParser.parseString(json).getAsJsonArray()) values.add(value.getAsString());
            return values;
        } catch (RuntimeException invalid) {
            plugin.getSLF4JLogger().warn("Ignoring invalid {} on consumed custom item", key);
            return List.of();
        }
    }
}
