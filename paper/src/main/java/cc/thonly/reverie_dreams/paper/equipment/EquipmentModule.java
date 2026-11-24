package cc.thonly.reverie_dreams.paper.equipment;

import cc.thonly.reverie_dreams.paper.util.Ids;

import com.google.gson.JsonParser;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import java.io.IOException;
import java.util.Objects;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Pose;

/** Exact current-source stats live in YAML. This module handles only behavior public item components cannot express. */
public final class EquipmentModule implements Listener {
    public record RepairPreview(ItemStack result, int materialCount, int repairedDurability) {
        public RepairPreview { result = result.clone(); }
        @Override public ItemStack result() { return result.clone(); }
    }
    private final Map<String, EquipmentRule> rules = new LinkedHashMap<>();
    private final Map<String, Set<String>> customRepair = new LinkedHashMap<>();
    private final Set<String> silverMarker = new HashSet<>();
    private final Map<UUID, LivingEntity> tracked = new HashMap<>();
    private JavaPlugin plugin;
    private NamespacedKey avatarCarrierKey;
    private BukkitTask task;
    private CrownInstantBridge crown;
    private volatile EquipmentSoundBridge sound;
    private EquipmentRepairBridge repair;
    private SilverDamageBridge silver;
    private Predicate<LivingEntity> friendly = entity -> entity.getScoreboardTags().contains(Ids.MOD_ID + ":friendly_faction");
    private Predicate<LivingEntity> customBoss = entity -> false;

    public void enable(JavaPlugin plugin) {
        if (this.plugin != null) throw new IllegalStateException("Equipment module already enabled");
        thread(); this.plugin = plugin; avatarCarrierKey = new NamespacedKey(plugin, "npc/carrier"); load();
        crown = new CrownInstantBridge(this); sound = new EquipmentSoundBridge(this);
        repair = new EquipmentRepairBridge(this); silver = new SilverDamageBridge(this);
        for (var listener : new Listener[]{this, crown, sound, repair, silver}) Bukkit.getPluginManager().registerEvents(listener, plugin);
        for (var world : Bukkit.getWorlds()) for (var entity : world.getLivingEntities()) track(entity);
        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }
    public void disable() {
        thread(); if (task != null) task.cancel();
        HandlerList.unregisterAll(this);
        if (crown != null) { HandlerList.unregisterAll(crown); crown.clear(); }
        var previousSound = sound; sound = null;
        if (previousSound != null) { HandlerList.unregisterAll(previousSound); previousSound.clear(); }
        if (repair != null) HandlerList.unregisterAll(repair);
        if (silver != null) HandlerList.unregisterAll(silver);
        tracked.clear(); rules.clear(); customRepair.clear(); silverMarker.clear(); plugin = null;
    }
    private static void thread() {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Equipment entity operations require Paper server thread");
    }
    private void load() {
        try (var input = plugin.getResource("migration/equipment-domain.json")) {
            if (input == null) throw new IllegalStateException("Missing equipment domain");
            var root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
            if (root.get("schemaVersion").getAsInt() != 1) throw new IllegalStateException("Unsupported equipment domain");
            for (var entry : root.getAsJsonObject("items").entrySet()) {
                var row = entry.getValue().getAsJsonObject();
                var slot = switch (row.get("slot").getAsString()) {
                    case "head" -> EquipmentSlot.HEAD; case "chest" -> EquipmentSlot.CHEST;
                    case "legs" -> EquipmentSlot.LEGS; case "feet" -> EquipmentSlot.FEET;
                    default -> throw new IllegalArgumentException("Unsupported armor slot");
                };
                rules.put(entry.getKey(), new EquipmentRule(entry.getKey(), row.get("material").getAsString(), slot,
                        row.get("durability").getAsInt(), row.get("armor").getAsDouble(), row.get("toughness").getAsDouble(),
                        row.get("enchantability").getAsInt(), row.get("repairTag").getAsString()));
            }
            for (var entry : root.getAsJsonObject("customRepair").entrySet()) {
                var targets = new HashSet<String>(); for (var value : entry.getValue().getAsJsonArray()) targets.add(value.getAsString());
                customRepair.put(entry.getKey(), Set.copyOf(targets));
            }
            for (var value : root.getAsJsonArray("silverMarkerItems")) silverMarker.add(value.getAsString());
        } catch (IOException failure) { throw new IllegalStateException("Cannot load equipment rules", failure); }
    }
    public Map<String, EquipmentRule> items() { return Collections.unmodifiableMap(rules); }
    public Optional<EquipmentRule> inspect(ItemStack item) { return Optional.ofNullable(rules.get(id(item))); }
    static String id(ItemStack item) {
        if (item == null || item.isEmpty()) return "minecraft:air";
        var id = CraftEngineItems.getCustomItemId(item);
        return id == null ? item.getType().getKey().asString() : id.asString();
    }
    public boolean hasSilverMarker(ItemStack item) { return silverMarker.contains(id(item)); }
    public void setFriendlyFactionPredicate(Predicate<LivingEntity> predicate) { thread(); friendly = Objects.requireNonNull(predicate); }
    public void setBossPredicate(Predicate<LivingEntity> predicate) { thread(); customBoss = Objects.requireNonNull(predicate); }
    boolean isVisualCopy(LivingEntity entity) {
        return entity instanceof Mannequin && entity.getPersistentDataContainer().has(avatarCarrierKey, PersistentDataType.STRING);
    }
    public boolean wears(LivingEntity entity, EquipmentSlot slot, String itemId) {
        var equipment = entity.getEquipment(); return equipment != null && itemId.equals(id(equipment.getItem(slot)));
    }
    public boolean wearsCrown(LivingEntity entity) { return wears(entity, EquipmentSlot.HEAD, Ids.MOD_ID + ":crown_of_the_underworld"); }
    public boolean hasFullWaterproof(LivingEntity entity) {
        return wears(entity, EquipmentSlot.HEAD, Ids.MOD_ID + ":waterproof_hat")
                && wears(entity, EquipmentSlot.CHEST, Ids.MOD_ID + ":waterproof_clothing")
                && wears(entity, EquipmentSlot.LEGS, Ids.MOD_ID + ":waterproof_leggings")
                && wears(entity, EquipmentSlot.FEET, Ids.MOD_ID + ":waterproof_boots");
    }
    /** Source rain-only exclusion for plugin consumers. Public Paper APIs cannot override the engine-wide query. */
    public boolean isInWaterOrRain(LivingEntity entity) {
        thread();
        if (hasFullWaterproof(entity) && !entity.isInWater() && entity.isInRain()) return false;
        return entity.isInWaterOrRain();
    }
    /** Hook for the original Oni entity's passive target selector; retaliation remains allowed. */
    public boolean mayOniTarget(LivingEntity target, boolean retaliation) {
        return retaliation || !wears(target, EquipmentSlot.HEAD, Ids.MOD_ID + ":oni_mask");
    }

    private void track(LivingEntity entity) {
        if (entity instanceof Player || isVisualCopy(entity)) return;
        var equipment = entity.getEquipment(); boolean relevant = false;
        if (equipment != null) for (var item : equipment.getArmorContents()) if (rules.containsKey(id(item))) relevant = true;
        if (relevant) tracked.put(entity.getUniqueId(), entity); else tracked.remove(entity.getUniqueId());
    }
    private void tick() {
        for (var player : Bukkit.getOnlinePlayers()) applyTick(player);
        for (var iterator = tracked.values().iterator(); iterator.hasNext();) {
            var entity = iterator.next(); if (!entity.isValid() || entity.isDead()) { iterator.remove(); continue; }
            applyTick(entity);
        }
        crown.expire(); sound.tick();
    }
    /** Public deterministic entry for isolated tests; production schedules it once per server tick. */
    public void applyTick(LivingEntity entity) {
        thread(); if (!entity.isValid() || entity.isDead() || isVisualCopy(entity)) return;
        var equipment = entity.getEquipment(); if (equipment == null) return;
        boolean dream = false;
        for (var item : equipment.getArmorContents()) { var rule = rules.get(id(item)); if (rule != null && rule.material().equals("dream")) dream = true; }
        if (dream && !entity.hasPotionEffect(PotionEffectType.REGENERATION)) entity.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20, 0, true, false, true));
        if (wears(entity, EquipmentSlot.FEET, Ids.MOD_ID + ":low_gravity_boot")) {
            entity.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 10, 1, false, false, false));
            entity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 10, 1, false, false, false));
        }
        boolean sneaking = entity instanceof Player player ? player.isSneaking() : entity.getPose() == Pose.SNEAKING;
        if (sneaking && entity.getVelocity().lengthSquared() <= 1.0 && wears(entity, EquipmentSlot.HEAD, Ids.MOD_ID + ":koishi_hat"))
            entity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 10, 0, false, false, false));
        if (hasFullWaterproof(entity) && !entity.hasPotionEffect(PotionEffectType.WATER_BREATHING))
            entity.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 1, 0, false, false, false));
        if (wears(entity, EquipmentSlot.HEAD, Ids.MOD_ID + ":oni_mask") && entity.getWorld().getGameTime() % 40 == 0) applyOniAura(entity);
    }
    private void applyOniAura(LivingEntity wearer) {
        var scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        var team = scoreboard.getEntryTeam(wearer.getScoreboardEntryName());
        for (var entity : wearer.getNearbyEntities(8, 8, 8)) {
            if (!(entity instanceof Monster monster) || monster.isDead() || friendly.test(monster)
                    || team != null && team.hasEntry(monster.getScoreboardEntryName())) continue;
            monster.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 0, false, false, false));
        }
    }

    public boolean applyCrownInstant(LivingEntity entity, PotionEffectType type, int amplifier, double scale, Entity source, Entity owner) {
        thread();
        if (!wearsCrown(entity) || isVisualCopy(entity) || !(type.equals(PotionEffectType.INSTANT_HEALTH) || type.equals(PotionEffectType.INSTANT_DAMAGE))) return false;
        if (!Double.isFinite(scale) || scale < 0) throw new IllegalArgumentException("Invalid instant effect intensity");
        CrownInstantBridge.apply(entity, type.equals(PotionEffectType.INSTANT_DAMAGE), amplifier, scale, source, owner); return true;
    }
    public Optional<RepairPreview> previewRepair(ItemStack item, ItemStack ingredient) {
        thread(); if (item == null || ingredient == null || ingredient.isEmpty()) return Optional.empty();
        var targets = customRepair.get(id(ingredient)); if (targets == null || !targets.contains(id(item))) return Optional.empty();
        int max = item.getDataOrDefault(DataComponentTypes.MAX_DAMAGE, 0), damage = item.getDataOrDefault(DataComponentTypes.DAMAGE, 0);
        int each = max / 4; if (damage <= 0 || each <= 0) return Optional.empty();
        int count = Math.min(ingredient.getAmount(), (damage + each - 1) / each);
        int repaired = Math.min(damage, count * each); var result = item.clone(); result.setData(DataComponentTypes.DAMAGE, damage - repaired);
        return Optional.of(new RepairPreview(result, count, repaired));
    }
    public void recordSoundSource(Entity source, Location location) {
        thread(); if (sound == null) return;
        recordSoundSource(source == null ? null : source.getUniqueId(), location.getWorld().getUID(), location.getX(), location.getY(), location.getZ());
    }
    /** Pure-data entry is safe for an optional packet listener; all Bukkit reads happen in the tick consumer. */
    public void recordSoundSource(UUID sourceId, UUID worldId, double x, double y, double z) {
        var bridge = sound; if (bridge != null) bridge.record(sourceId, worldId, x, y, z);
    }

    @EventHandler public void onEquipment(EntityEquipmentChangedEvent event) { track(event.getEntity()); }
    @EventHandler public void onJoin(PlayerJoinEvent event) { applyTick(event.getPlayer()); }
    @EventHandler public void onLoad(EntitiesLoadEvent event) { for (var entity : event.getEntities()) if (entity instanceof LivingEntity living) track(living); }
    @EventHandler public void onUnload(EntitiesUnloadEvent event) { for (var entity : event.getEntities()) tracked.remove(entity.getUniqueId()); }
    @EventHandler public void onDeath(EntityDeathEvent event) { tracked.remove(event.getEntity().getUniqueId()); }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity mob) || event.getTarget() == null || !wearsCrown(event.getTarget())
                || !Tag.ENTITY_TYPES_UNDEAD.isTagged(mob.getType()) || mob.getType() == EntityType.WITHER || customBoss.test(mob)) return;
        var bosses = Bukkit.getTag(Tag.REGISTRY_ENTITY_TYPES, new NamespacedKey("c", "bosses"), EntityType.class);
        if (bosses == null || !bosses.isTagged(mob.getType())) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRiptide(PlayerRiptideEvent event) {
        if (hasFullWaterproof(event.getPlayer()) && !event.getPlayer().isInWater() && event.getPlayer().isInRain()) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTridentUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getItem() == null
                || event.getItem().getType() != Material.TRIDENT || event.getItem().getEnchantmentLevel(Enchantment.RIPTIDE) == 0) return;
        if (hasFullWaterproof(event.getPlayer()) && !event.getPlayer().isInWater() && event.getPlayer().isInRain()) event.setUseItemInHand(Event.Result.DENY);
    }
}
