package cc.thonly.reverie_dreams.paper.npc;

import cc.thonly.reverie_dreams.paper.util.Ids;

import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.VanillaGoal;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.LivingEntity;

/**
 * Main-thread service for an invisible ground-AI carrier and a persistent humanoid mannequin.
 * UUIDs exposed to callers always identify the carrier. No NMS, packet library or fake Player is used.
 */
public final class NPCService implements Listener {
    public static final int INVENTORY_SIZE = 24;
    private static final int SCHEMA = 1;
    private final JavaPlugin plugin;
    private final Map<String, NPCRole> roles = new LinkedHashMap<>();
    private final Map<String, String> aliases = new LinkedHashMap<>();
    private final Map<UUID, Wolf> loaded = new LinkedHashMap<>();
    private final NamespacedKey schemaKey, roleKey, ownerKey, modeKey, inventoryKey, avatarKey, carrierKey;
    private final NamespacedKey archiveKey, archiveIdKey, archiveHealthKey, archiveMaxHealthKey, archiveRespawnKey;
    private final GoalKey<Wolf> followKey;
    private Supplier<ItemStack> archiveFactory = () -> new ItemStack(Material.PAPER);
    private boolean enabled;
    private Consumer<PlayerInteractEntityEvent> interactionHandler;
    private Consumer<UUID> closeInventoryHandler;
    private Consumer<Wolf> goalInstaller;
    private BiPredicate<Wolf, LivingEntity> targetPermission = (wolf, target) -> false;
    private BiConsumer<PersistentDataContainer, PersistentDataContainer> archiveDataHandler;

    NPCService(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
        schemaKey = key("npc/schema"); roleKey = key("npc/role"); ownerKey = key("npc/owner");
        modeKey = key("npc/mode"); inventoryKey = key("npc/inventory"); avatarKey = key("npc/avatar");
        carrierKey = key("npc/carrier"); archiveKey = key("npc/archive"); archiveIdKey = key("npc/archive_id");
        archiveHealthKey = key("npc/archive_health"); archiveMaxHealthKey = key("npc/archive_max_health");
        archiveRespawnKey = key("npc/archive_can_respawn"); followKey = GoalKey.of(Wolf.class, key("npc/follow_owner"));
        loadDomain();
    }

    private NamespacedKey key(String name) { return new NamespacedKey(plugin, name); }
    private void thread() {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("NPC service requires the Paper server thread");
    }
    private void active() { thread(); if (!enabled) throw new IllegalStateException("NPC service is disabled"); }

    private void loadDomain() {
        try (var stream = plugin.getResource("migration/npc-domain.json")) {
            if (stream == null) throw new IllegalStateException("Missing migration/npc-domain.json");
            var root = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            if (root.get("schemaVersion").getAsInt() != 1) throw new IllegalStateException("Unsupported NPC domain schema");
            for (var entry : root.getAsJsonObject("roles").entrySet()) {
                var data = entry.getValue().getAsJsonObject();
                var skin = data.getAsJsonObject("skin");
                String texture = skin.get("textureKey").isJsonNull() ? null : skin.get("textureKey").getAsString();
                roles.put(entry.getKey(), new NPCRole(entry.getKey(), skin.get("id").getAsString(), texture,
                        skin.getAsJsonObject("sourceConfig").get("type").getAsString(),
                        data.get("translationKey").getAsString(), data.getAsJsonObject("names").get("zh_cn").getAsString()));
            }
            for (var entry : root.getAsJsonObject("roleAliases").entrySet()) aliases.put(entry.getKey(), entry.getValue().getAsString());
        } catch (IOException failure) {
            throw new IllegalStateException("Cannot load NPC domain", failure);
        }
    }

    void enable() {
        thread(); enabled = true;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        for (var world : Bukkit.getWorlds()) for (var entity : world.getEntities()) {
            if (entity instanceof Wolf wolf && hasSchema(wolf)) attachSafely(wolf, true);
        }
    }

    void disable() {
        thread(); enabled = false;
        for (var wolf : loaded.values()) if (wolf.isValid()) {
            wolf.getPathfinder().stopPathfinding(); Bukkit.getMobGoals().removeGoal(wolf, followKey);
        }
        loaded.clear(); HandlerList.unregisterAll(this);
    }

    void setInteractionHandler(Consumer<PlayerInteractEntityEvent> handler) { interactionHandler = handler; }
    void setCloseInventoryHandler(Consumer<UUID> handler) { closeInventoryHandler = handler; }

    void setGoalInstaller(Consumer<Wolf> installer) { goalInstaller = installer; }
    void setTargetPermission(BiPredicate<Wolf, LivingEntity> permission) { targetPermission = permission; }
    void setArchiveDataHandler(BiConsumer<PersistentDataContainer, PersistentDataContainer> handler) { archiveDataHandler = handler; }
    Wolf carrier(UUID id) { active(); return require(id); }
    void beginWork(UUID id, UUID actor) { active(); setMode(requireOwned(id, actor), NPCMode.WORKING); }

    public Collection<NPCRole> roles() { return Collections.unmodifiableCollection(roles.values()); }
    public Collection<UUID> loadedIds() { active(); return List.copyOf(loaded.keySet()); }
    public void setArchiveFactory(Supplier<ItemStack> factory) { active(); archiveFactory = Objects.requireNonNull(factory); }

    private NPCRole role(String id) {
        String full = id.contains(":") ? id : Ids.MOD_ID + ":" + id;
        full = aliases.getOrDefault(full, full);
        var role = roles.get(full);
        if (role == null) throw new IllegalArgumentException("Unknown NPC role: " + id);
        return role;
    }

    public UUID spawnRole(String roleId, Location location) {
        active(); Objects.requireNonNull(location.getWorld(), "NPC spawn world");
        if (!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4))
            throw new IllegalArgumentException("NPC spawn chunk must already be loaded");
        var role = role(roleId);
        Wolf wolf = location.getWorld().spawn(location, Wolf.class, entity -> {
            var pdc = entity.getPersistentDataContainer();
            pdc.set(schemaKey, PersistentDataType.INTEGER, SCHEMA);
            pdc.set(roleKey, PersistentDataType.STRING, role.id());
            pdc.set(modeKey, PersistentDataType.STRING, NPCMode.NORMAL.sourceId());
            pdc.set(inventoryKey, PersistentDataType.BYTE_ARRAY, ItemStack.serializeItemsAsBytes(emptyInventory()));
            entity.setInvisible(true); entity.setSilent(true); entity.setPersistent(true);
            entity.setRemoveWhenFarAway(false); entity.setCanPickupItems(false); entity.setAdult();
            entity.setBreed(false); entity.setAngry(false); entity.setSitting(false);
            setBase(entity, Attribute.MAX_HEALTH, 20.0); setBase(entity, Attribute.MOVEMENT_SPEED, 0.25);
            setBase(entity, Attribute.FOLLOW_RANGE, 32.0); setBase(entity, Attribute.KNOCKBACK_RESISTANCE, 0.1);
            setBase(entity, Attribute.ATTACK_DAMAGE, 1.0); entity.setHealth(20.0);
        });
        if (!wolf.isValid()) throw new IllegalStateException("NPC carrier spawn was cancelled");
        try { attach(wolf); repairAvatar(wolf); }
        catch (RuntimeException failure) {
            loaded.remove(wolf.getUniqueId());
            for (var passenger : wolf.getPassengers()) if (passenger instanceof Mannequin) passenger.remove();
            wolf.remove(); throw failure;
        }
        return wolf.getUniqueId();
    }

    private static void setBase(Wolf wolf, Attribute attribute, double value) {
        var instance = wolf.getAttribute(attribute);
        if (instance != null) instance.setBaseValue(value);
    }

    /** Trusted deterministic recruitment. Food cost, 40% random chance and making_friend are not implied. */
    public boolean recruit(UUID npcId, UUID ownerId) {
        active(); Objects.requireNonNull(ownerId, "ownerId"); var wolf = require(npcId);
        UUID previous = ownerId(wolf);
        if (previous != null && !previous.equals(ownerId)) return false;
        double health = wolf.getHealth(), maxHealthBase = Objects.requireNonNull(wolf.getAttribute(Attribute.MAX_HEALTH)).getBaseValue();
        wolf.setOwner(Bukkit.getOfflinePlayer(ownerId));
        // Wolf taming has different vanilla attribute defaults; restore the NPC's own existing values.
        setBase(wolf, Attribute.MAX_HEALTH, maxHealthBase); wolf.setHealth(Math.min(health, maxHealth(wolf)));
        wolf.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, ownerId.toString());
        setMode(wolf, NPCMode.FOLLOW); return true;
    }

    public void follow(UUID npcId, UUID actor) { active(); var wolf = requireOwned(npcId, actor); setMode(wolf, NPCMode.FOLLOW); }
    public void stay(UUID npcId, UUID actor) { active(); var wolf = requireOwned(npcId, actor); setMode(wolf, NPCMode.STAY); }

    private void setMode(Wolf wolf, NPCMode mode) {
        wolf.getPersistentDataContainer().set(modeKey, PersistentDataType.STRING, mode.sourceId());
        wolf.setSitting(false); wolf.getPathfinder().stopPathfinding();
    }
    NPCMode mode(Wolf wolf) {
        return NPCMode.fromSourceId(wolf.getPersistentDataContainer().getOrDefault(modeKey, PersistentDataType.STRING, NPCMode.NORMAL.sourceId()));
    }
    UUID ownerId(Wolf wolf) {
        return readUuid(wolf.getPersistentDataContainer().get(ownerKey, PersistentDataType.STRING));
    }
    private static UUID readUuid(String value) { return value == null || value.isBlank() ? null : UUID.fromString(value); }

    private Wolf require(UUID id) {
        var wolf = loaded.get(id);
        if (wolf == null || !wolf.isValid() || wolf.isDead()) throw new IllegalArgumentException("NPC is not loaded: " + id);
        return wolf;
    }
    private Wolf requireOwned(UUID id, UUID actor) {
        var wolf = require(id);
        if (actor == null || !actor.equals(ownerId(wolf))) throw new SecurityException("NPC belongs to another player");
        return wolf;
    }

    public ItemStack[] getInventory(UUID npcId, UUID actor) { active(); return inventory(requireOwned(npcId, actor)); }
    public void storeInventory(UUID npcId, UUID actor, ItemStack[] content) {
        active(); storeInventory(requireOwned(npcId, actor), content);
    }
    ItemStack[] runtimeInventory(Wolf wolf) { active(); if(!hasSchema(wolf))throw new IllegalArgumentException("Not an NPC carrier");return inventory(wolf); }
    void storeRuntimeInventory(Wolf wolf, ItemStack[] content) { active();if(!hasSchema(wolf))throw new IllegalArgumentException("Not an NPC carrier");storeInventory(wolf,content); }
    private void storeInventory(Wolf wolf, ItemStack[] content) {
        if (content.length != INVENTORY_SIZE) throw new IllegalArgumentException("NPC inventory requires exactly 24 slots");
        var copy = NPCSnapshot.copy(content);
        for (var item : copy) if (!item.isEmpty() && (item.getAmount() < 1 || item.getAmount() > item.getMaxStackSize()))
            throw new IllegalArgumentException("Invalid NPC inventory stack amount");
        byte[] encoded = ItemStack.serializeItemsAsBytes(copy);
        syncEquipment(wolf, copy);
        wolf.getPersistentDataContainer().set(inventoryKey, PersistentDataType.BYTE_ARRAY, encoded);
    }
    private static ItemStack[] emptyInventory() {
        var result = new ItemStack[INVENTORY_SIZE]; Arrays.setAll(result, i -> ItemStack.empty()); return result;
    }
    private ItemStack[] inventory(Wolf wolf) {
        var bytes = wolf.getPersistentDataContainer().get(inventoryKey, PersistentDataType.BYTE_ARRAY);
        if (bytes == null) throw new IllegalStateException("Missing NPC inventory; refusing to erase data");
        var items = ItemStack.deserializeItemsFromBytes(bytes);
        if (items.length != INVENTORY_SIZE) throw new IllegalStateException("Unsupported NPC inventory slot count");
        return NPCSnapshot.copy(items);
    }

    public NPCSnapshot inspect(UUID npcId) {
        active(); var wolf = require(npcId); var role = role(wolf.getPersistentDataContainer().get(roleKey, PersistentDataType.STRING));
        return new NPCSnapshot(wolf.getUniqueId(), readUuid(wolf.getPersistentDataContainer().get(avatarKey, PersistentDataType.STRING)),
                role.id(), role.skinId(), ownerId(wolf), mode(wolf), wolf.getHealth(), maxHealth(wolf), inventory(wolf));
    }

    /** Exact plugin metadata and native item bytes for isolated persistence assertions; no mutable container escapes. */
    public Map<String, Object> inspectPersistentData(UUID npcId) {
        var data = inspect(npcId); var out = new LinkedHashMap<String, Object>();
        out.put("schemaVersion", SCHEMA); out.put("entityId", data.entityId().toString());
        out.put("avatarId", Objects.toString(data.avatarId(), "")); out.put("roleId", data.roleId());
        out.put("skinId", data.skinId()); out.put("ownerId", Objects.toString(data.ownerId(), ""));
        out.put("stateId", data.mode().sourceId()); out.put("health", data.health()); out.put("maxHealth", data.maxHealth());
        out.put("inventoryBytes", ItemStack.serializeItemsAsBytes(data.inventory()));
        return Collections.unmodifiableMap(out);
    }
    private static double maxHealth(Wolf wolf) { return Objects.requireNonNull(wolf.getAttribute(Attribute.MAX_HEALTH)).getValue(); }
    private boolean hasSchema(Entity entity) {
        return entity.getPersistentDataContainer().getOrDefault(schemaKey, PersistentDataType.INTEGER, 0) == SCHEMA;
    }
    public boolean isNPC(Entity entity) { return hasSchema(entity) || entity.getPersistentDataContainer().has(carrierKey, PersistentDataType.STRING); }
    public Optional<UUID> canonicalId(Entity entity) {
        if (entity instanceof Wolf && hasSchema(entity)) return Optional.of(entity.getUniqueId());
        String value = entity.getPersistentDataContainer().get(carrierKey, PersistentDataType.STRING);
        try { return Optional.ofNullable(readUuid(value)); } catch (IllegalArgumentException invalid) { return Optional.empty(); }
    }

    private void attachSafely(Wolf wolf, boolean repairLater) {
        try {
            attach(wolf);
            if (repairLater) Bukkit.getScheduler().runTask(plugin, () -> {
                if (enabled && wolf.isValid() && !wolf.isDead()) {
                    try { repairAvatar(wolf); } catch (RuntimeException failure) { plugin.getLogger().warning("Cannot attach NPC avatar " + wolf.getUniqueId() + ": " + failure.getMessage()); }
                }
            });
        } catch (RuntimeException failure) {
            plugin.getLogger().warning("NPC data preserved without activation for " + wolf.getUniqueId() + ": " + failure.getMessage());
        }
    }
    private void attach(Wolf wolf) {
        role(wolf.getPersistentDataContainer().get(roleKey, PersistentDataType.STRING));
        mode(wolf); var inventory = inventory(wolf); ownerId(wolf); // Validate before changing entity AI.
        var goals = Bukkit.getMobGoals(); var floating = goals.getGoal(wolf, VanillaGoal.FLOAT);
        goals.removeAllGoals(wolf);
        if (floating != null) goals.addGoal((Mob) wolf, 0, floating);
        goals.addGoal(wolf, 6, new NPCFollowGoal(this, wolf, followKey));
        wolf.setAI(true); wolf.setAware(true); wolf.setInvisible(true); wolf.setSilent(true);
        wolf.setPersistent(true); wolf.setRemoveWhenFarAway(false); wolf.setCanPickupItems(false);
        wolf.setTarget(null); wolf.setAngry(false); wolf.setSitting(false); wolf.setBreed(false);
        wolf.getPathfinder().setCanOpenDoors(true); wolf.getPathfinder().setCanPassDoors(true); wolf.getPathfinder().setCanFloat(true);
        loaded.put(wolf.getUniqueId(), wolf); syncEquipment(wolf, inventory);
        if (goalInstaller != null) goalInstaller.accept(wolf);
    }

    private void repairAvatar(Wolf wolf) {
        var role = role(wolf.getPersistentDataContainer().get(roleKey, PersistentDataType.STRING));
        UUID id = readUuid(wolf.getPersistentDataContainer().get(avatarKey, PersistentDataType.STRING));
        Entity current = id == null ? null : Bukkit.getEntity(id);
        Mannequin avatar;
        if (current instanceof Mannequin existing && canonicalId(existing).filter(wolf.getUniqueId()::equals).isPresent()) {
            avatar = existing;
        } else if (current != null) {
            throw new IllegalStateException("NPC avatar UUID references another entity; refusing replacement");
        } else {
            avatar = wolf.getWorld().spawn(wolf.getLocation(), Mannequin.class, mannequin -> {
                mannequin.getPersistentDataContainer().set(carrierKey, PersistentDataType.STRING, wolf.getUniqueId().toString());
                mannequin.setPersistent(true); mannequin.setCanPickupItems(false); mannequin.setSilent(true);
                mannequin.setCollidable(false); mannequin.setGravity(false); mannequin.setImmovable(true);
                mannequin.setProfile(role.profile()); mannequin.customName(role.displayName()); mannequin.setCustomNameVisible(true);
                mannequin.setDescription(Component.empty());
            });
            if (!avatar.isValid()) throw new IllegalStateException("NPC mannequin spawn was cancelled");
            wolf.getPersistentDataContainer().set(avatarKey, PersistentDataType.STRING, avatar.getUniqueId().toString());
        }
        if (avatar.getVehicle() != wolf && !wolf.addPassenger(avatar)) throw new IllegalStateException("NPC mannequin mount was cancelled");
        avatar.setProfile(role.profile()); avatar.customName(role.displayName()); syncEquipment(wolf, inventory(wolf));
    }

    private void syncEquipment(Wolf wolf, ItemStack[] items) {
        // Carrier equipment supplies native attributes. The mannequin only renders a matching copy.
        applyEquipment(wolf.getEquipment(), items, true);
        UUID id = readUuid(wolf.getPersistentDataContainer().get(avatarKey, PersistentDataType.STRING));
        if (id != null && Bukkit.getEntity(id) instanceof Mannequin avatar) applyEquipment(avatar.getEquipment(), items, false);
    }
    private static void applyEquipment(EntityEquipment equipment, ItemStack[] items, boolean mob) {
        if (equipment == null) return;
        equipment.setHelmet(items[18].clone()); equipment.setChestplate(items[19].clone());
        equipment.setLeggings(items[20].clone()); equipment.setBoots(items[21].clone());
        equipment.setItemInMainHand(items[22].clone()); equipment.setItemInOffHand(items[23].clone());
        if (mob) {
            equipment.setHelmetDropChance(0); equipment.setChestplateDropChance(0); equipment.setLeggingsDropChance(0);
            equipment.setBootsDropChance(0); equipment.setItemInMainHandDropChance(0); equipment.setItemInOffHandDropChance(0);
        }
    }

    public boolean isArchive(ItemStack item) {
        return item != null && !item.isEmpty() && item.getPersistentDataContainer().getOrDefault(archiveKey, PersistentDataType.INTEGER, 0) == SCHEMA;
    }
    public NPCSnapshot inspectArchive(ItemStack item) {
        thread();
        if (!isArchive(item)) throw new IllegalArgumentException("Not an NPC archive");
        var pdc = item.getPersistentDataContainer(); var role = role(pdc.get(roleKey, PersistentDataType.STRING));
        var bytes = Objects.requireNonNull(pdc.get(inventoryKey, PersistentDataType.BYTE_ARRAY));
        var items = ItemStack.deserializeItemsFromBytes(bytes);
        if (items.length != INVENTORY_SIZE) throw new IllegalStateException("Unsupported archived inventory size");
        return new NPCSnapshot(readUuid(pdc.get(archiveIdKey, PersistentDataType.STRING)), readUuid(pdc.get(avatarKey, PersistentDataType.STRING)),
                role.id(), role.skinId(), readUuid(pdc.get(ownerKey, PersistentDataType.STRING)),
                NPCMode.fromSourceId(pdc.get(modeKey, PersistentDataType.STRING)),
                pdc.getOrDefault(archiveHealthKey, PersistentDataType.DOUBLE, 0.0),
                pdc.getOrDefault(archiveMaxHealthKey, PersistentDataType.DOUBLE, 20.0), items);
    }
    private ItemStack archive(Wolf wolf) {
        ItemStack item;
        try { item = archiveFactory.get(); } catch (RuntimeException invalidFactory) { item = null; }
        if (item == null || item.isEmpty()) item = new ItemStack(Material.PAPER);
        item = item.asOne();
        var role = role(wolf.getPersistentDataContainer().get(roleKey, PersistentDataType.STRING));
        item.editMeta(meta -> {
            var source = wolf.getPersistentDataContainer(); var target = meta.getPersistentDataContainer();
            target.set(archiveKey, PersistentDataType.INTEGER, SCHEMA);
            target.set(archiveIdKey, PersistentDataType.STRING, wolf.getUniqueId().toString());
            target.set(roleKey, PersistentDataType.STRING, role.id());
            target.set(modeKey, PersistentDataType.STRING, mode(wolf).sourceId());
            target.set(inventoryKey, PersistentDataType.BYTE_ARRAY, Objects.requireNonNull(source.get(inventoryKey, PersistentDataType.BYTE_ARRAY)));
            String owner = source.get(ownerKey, PersistentDataType.STRING), avatar = source.get(avatarKey, PersistentDataType.STRING);
            if (owner != null) target.set(ownerKey, PersistentDataType.STRING, owner);
            if (avatar != null) target.set(avatarKey, PersistentDataType.STRING, avatar);
            target.set(archiveHealthKey, PersistentDataType.DOUBLE, 0.0);
            target.set(archiveMaxHealthKey, PersistentDataType.DOUBLE, maxHealth(wolf));
            target.set(archiveRespawnKey, PersistentDataType.BOOLEAN, false);
            if (archiveDataHandler != null) archiveDataHandler.accept(source, target);
            meta.displayName(Component.text("角色档案 (").append(role.displayName()).append(Component.text(")")));
        });
        return item;
    }

    @EventHandler public void onLoad(EntitiesLoadEvent event) {
        for (var entity : event.getEntities()) if (entity instanceof Wolf wolf && hasSchema(wolf)) attachSafely(wolf, true);
    }
    @EventHandler public void onUnload(EntitiesUnloadEvent event) {
        for (var entity : event.getEntities()) if (entity instanceof Wolf wolf && loaded.remove(wolf.getUniqueId()) != null) {
            wolf.getPathfinder().stopPathfinding(); Bukkit.getMobGoals().removeGoal(wolf, followKey);
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!isNPC(event.getRightClicked())) return;
        if (interactionHandler != null) interactionHandler.accept(event);
        // Block vanilla wolf tame/feed/sit and mannequin equipment edits after the owned interaction.
        event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractAt(PlayerInteractAtEntityEvent event) { onInteract(event); }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if (event.getEntity() instanceof Wolf wolf && hasSchema(wolf) && event.getTarget() != null
                && !targetPermission.test(wolf, event.getTarget())) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDismount(EntityDismountEvent event) {
        if (enabled && event.isCancellable() && event.getEntity() instanceof Mannequin mannequin && !mannequin.isDead() && event.getDismounted() instanceof Wolf wolf
                && loaded.containsKey(wolf.getUniqueId()) && !wolf.isDead()) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAvatarDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Mannequin)) return;
        var id = canonicalId(event.getEntity()).orElse(null); var carrier = id == null ? null : loaded.get(id);
        if (carrier == null || !carrier.isValid() || carrier.isDead()) return;
        event.setCancelled(true);
        // Area/environmental hits also reach the canonical carrier; forwarding
        // the appearance's copy would apply the same explosion/fire/potion twice.
        boolean directAttack = event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK;
        boolean projectile = event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE
                && event instanceof EntityDamageByEntityEvent hit && !(hit.getDamager() instanceof ThrownPotion);
        if (!directAttack && !projectile) return;
        // Re-run the raw damage once on the canonical health/equipment holder, preserving the public DamageSource.
        carrier.damage(event.getDamage(), event.getDamageSource());
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSplash(PotionSplashEvent event) {
        for (var entity : event.getAffectedEntities()) if (entity instanceof Mannequin && isNPC(entity)) event.setIntensity(entity, 0);
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCloud(AreaEffectCloudApplyEvent event) {
        event.getAffectedEntities().removeIf(entity -> entity instanceof Mannequin && isNPC(entity));
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Mannequin mannequin) {
            var id = canonicalId(mannequin).orElse(null);
            if (id == null) return;
            // The visible entity only carries equipment copies. Direct setHealth(0) bypasses damage forwarding.
            event.getDrops().clear(); event.setDroppedExp(0);
            var carrier = loaded.get(id);
            if (carrier != null && mannequin.getUniqueId().equals(readUuid(carrier.getPersistentDataContainer().get(avatarKey, PersistentDataType.STRING)))) {
                carrier.getPersistentDataContainer().remove(avatarKey);
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (enabled && carrier.isValid() && !carrier.isDead()) {
                        try { repairAvatar(carrier); }
                        catch (RuntimeException failure) { plugin.getLogger().warning("Cannot rebuild NPC avatar " + id + ": " + failure.getMessage()); }
                    }
                });
            }
            mannequin.remove();
            return;
        }
        if (!(event.getEntity() instanceof Wolf wolf) || !hasSchema(wolf)) return;
        if (closeInventoryHandler != null) closeInventoryHandler.accept(wolf.getUniqueId());
        ItemStack archive = archive(wolf); // Build before clearing drops, so a failed serialization cannot erase inventory.
        event.getDrops().clear(); event.getDrops().add(archive); event.setDroppedExp(0);
        loaded.remove(wolf.getUniqueId());
        UUID avatar = readUuid(wolf.getPersistentDataContainer().get(avatarKey, PersistentDataType.STRING));
        if (avatar != null && Bukkit.getEntity(avatar) instanceof Mannequin mannequin) mannequin.remove();
    }
}
