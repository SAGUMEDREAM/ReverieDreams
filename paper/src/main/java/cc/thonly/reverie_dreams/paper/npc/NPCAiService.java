package cc.thonly.reverie_dreams.paper.npc;

import cc.thonly.reverie_dreams.paper.util.Ids;

import com.destroystokyo.paper.entity.ai.GoalKey;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptor;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItem;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.libraries.antigrieflib.Flag;
import cc.thonly.reverie_dreams.paper.farming.FarmingModule;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import com.google.gson.JsonArray;
import java.util.Objects;
import org.bukkit.FluidCollisionMode;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Sheep;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EquipmentSlot;

/** First source-derived AI cohort: bounded FARM, COMBAT guard, owner defense and retaliation. */
public final class NPCAiService implements Listener {
    static final class State {
        final UUID id; NPCWorkMode mode = NPCWorkMode.COMBAT; NPCWorkSite site; String status = "ready"; long actions;
        boolean attacking, hit; long nextGuard; UUID ownerThreat;
        boolean requireSmoke=true;String honeyProduct="auto";
        NPCStorageSettings storageSettings=NPCStorageSettings.defaults();
        State(UUID id, NPCWorkSite site) { this.id = id; this.site = site; }
    }
    record Resource(UUID world, int x, int y, int z) { }
    record Lease(UUID owner, long until) { }
    record Crop(String seed, BlockData young) { }
    private final JavaPlugin plugin;
    final NPCService service;
    private final Consumer<UUID> refresh;
    private final Map<UUID, State> states = new HashMap<>();
    private final Map<Object, Lease> claims = new HashMap<>();
    private final NamespacedKey settingsKey, actionsKey;
    final NamespacedKey toolSlotKey, toolDataKey;
    private final Map<UUID,NPCWorkToolLease> tools=new HashMap<>();
    private NPCArmorWearBridge armorWear;
    final GoalKey<Wolf> farmKey, combatKey, guardKey,husbandryKey,collectKey,storageKey;
    final NPCStorageWork storage;
    final NPCHusbandry husbandry=new NPCHusbandry(this);
    private boolean enabled;
    public NPCAiService(JavaPlugin plugin, NPCService service, Consumer<UUID> refresh) {
        this.plugin = plugin; this.service = service; this.refresh = refresh;storage=new NPCStorageWork(plugin,this);
        settingsKey = new NamespacedKey(plugin, "npc/work_settings"); actionsKey = new NamespacedKey(plugin, "npc/work_actions");
        toolSlotKey=new NamespacedKey(plugin,"npc/tool_source");toolDataKey=new NamespacedKey(plugin,"npc/tool_stacks");
        farmKey = GoalKey.of(Wolf.class, new NamespacedKey(plugin, "npc/farm"));
        combatKey = GoalKey.of(Wolf.class, new NamespacedKey(plugin, "npc/melee"));
        guardKey = GoalKey.of(Wolf.class, new NamespacedKey(plugin, "npc/guard"));
        husbandryKey=GoalKey.of(Wolf.class,new NamespacedKey(plugin,"npc/husbandry"));collectKey=GoalKey.of(Wolf.class,new NamespacedKey(plugin,"npc/collect"));storageKey=GoalKey.of(Wolf.class,new NamespacedKey(plugin,"npc/storage_work"));
    }
    public void enable() {
        thread(); if (enabled) throw new IllegalStateException("NPC AI already enabled"); enabled = true;
        service.setGoalInstaller(this::install); service.setTargetPermission(this::mayAttack); service.setArchiveDataHandler(this::copyArchiveData);
        Bukkit.getPluginManager().registerEvents(this, plugin);
        armorWear=new NPCArmorWearBridge(this);Bukkit.getPluginManager().registerEvents(armorWear,plugin);
        for (var id : service.loadedIds()) install(service.carrier(id));
    }
    public void disable() {
        thread(); enabled = false; service.setGoalInstaller(null); service.setTargetPermission((wolf, target) -> false); service.setArchiveDataHandler(null);
        for (var id : List.copyOf(states.keySet())) if (Bukkit.getEntity(id) instanceof Wolf wolf) {
            var goals = Bukkit.getMobGoals(); goals.removeGoal(wolf, farmKey); goals.removeGoal(wolf, combatKey); goals.removeGoal(wolf, guardKey);goals.removeGoal(wolf,husbandryKey);goals.removeGoal(wolf,collectKey);goals.removeGoal(wolf,storageKey);
            restoreTools(wolf);storage.forget(wolf);wolf.setTarget(null); wolf.getPathfinder().stopPathfinding();
        }
        if(armorWear!=null)HandlerList.unregisterAll(armorWear);
        HandlerList.unregisterAll(this); states.clear(); claims.clear();tools.clear();
    }
    static void thread() { if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("NPC AI requires Paper server thread"); }
    private void install(Wolf wolf) {
        tools(wolf).restore(); state(wolf); var goals = Bukkit.getMobGoals(); goals.removeGoal(wolf, farmKey); goals.removeGoal(wolf, combatKey); goals.removeGoal(wolf, guardKey);goals.removeGoal(wolf,husbandryKey);goals.removeGoal(wolf,collectKey);goals.removeGoal(wolf,storageKey);
        goals.addGoal(wolf, 4, new NPCCombatGoal(this, wolf)); goals.addGoal(wolf, 6, new NPCFarmGoal(this, wolf));
        goals.addGoal(wolf, 6, new NPCGuardGoal(this, wolf));
        goals.addGoal(wolf,5,new NPCWorkCollectGoal(this,wolf));goals.addGoal(wolf,6,new NPCHusbandryGoal(this,wolf));goals.addGoal(wolf,6,new NPCStorageGoal(this,wolf));
    }
    State state(Wolf wolf) {
        return states.computeIfAbsent(wolf.getUniqueId(), id -> {
            var loc = wolf.getLocation(); var state = new State(id, new NPCWorkSite(wolf.getWorld().getUID(), loc.getBlockX(), (int) Math.round(loc.getY()), loc.getBlockZ(), 8, 8));
            var data = wolf.getPersistentDataContainer(); String json = data.get(settingsKey, PersistentDataType.STRING);
            if (json != null) {
                var row = JsonParser.parseString(json).getAsJsonObject(); state.mode = NPCWorkMode.fromId(row.get("mode").getAsString());
                state.site = new NPCWorkSite(UUID.fromString(row.get("world").getAsString()), row.get("x").getAsInt(), row.get("y").getAsInt(), row.get("z").getAsInt(), row.get("radius").getAsInt(), row.get("height").getAsInt());
                if(row.has("requireSmoke"))state.requireSmoke=row.get("requireSmoke").getAsBoolean();
                if(row.has("honeyProduct"))state.honeyProduct=checkedHoneyProduct(row.get("honeyProduct").getAsString());
                if(row.has("storage")){var sorting=row.getAsJsonObject("storage");var tags=new ArrayList<String>();for(var tag:sorting.getAsJsonArray("categoryTags"))tags.add(tag.getAsString());state.storageSettings=new NPCStorageSettings(sorting.get("claimEmpty").getAsBoolean(),sorting.get("reserveFood").getAsInt(),sorting.get("reserveTools").getAsInt(),tags);}
                if (!state.mode.implemented()) state.status = "unsupported_mode";
            }
            state.actions = data.getOrDefault(actionsKey, PersistentDataType.LONG, 0L); return state;
        });
    }
    private void persist(Wolf wolf) {
        var state = state(wolf); var row = new JsonObject(); row.addProperty("mode", state.mode.sourceId()); row.addProperty("world", state.site.worldId().toString());
        row.addProperty("x", state.site.x()); row.addProperty("y", state.site.y()); row.addProperty("z", state.site.z()); row.addProperty("radius", state.site.radius()); row.addProperty("height", state.site.height());
        row.addProperty("requireSmoke",state.requireSmoke);row.addProperty("honeyProduct",state.honeyProduct);
        var sorting=new JsonObject();sorting.addProperty("claimEmpty",state.storageSettings.claimEmpty());sorting.addProperty("reserveFood",state.storageSettings.reserveFood());sorting.addProperty("reserveTools",state.storageSettings.reserveTools());var tags=new JsonArray();for(var tag:state.storageSettings.categoryTags())tags.add(tag);sorting.add("categoryTags",tags);row.add("storage",sorting);
        wolf.getPersistentDataContainer().set(settingsKey, PersistentDataType.STRING, row.toString());
        wolf.getPersistentDataContainer().set(actionsKey, PersistentDataType.LONG, state.actions);
    }
    private void copyArchiveData(PersistentDataContainer source, PersistentDataContainer target) {
        var settings = source.get(settingsKey, PersistentDataType.STRING); if (settings != null) target.set(settingsKey, PersistentDataType.STRING, settings);
        var actions = source.get(actionsKey, PersistentDataType.LONG); if (actions != null) target.set(actionsKey, PersistentDataType.LONG, actions);
        var slot=source.get(toolSlotKey,PersistentDataType.INTEGER);var stacks=source.get(toolDataKey,PersistentDataType.BYTE_ARRAY);if(slot!=null&&stacks!=null){target.set(toolSlotKey,PersistentDataType.INTEGER,slot);target.set(toolDataKey,PersistentDataType.BYTE_ARRAY,stacks);}
    }
    public void work(UUID id, UUID owner, NPCWorkMode mode, NPCWorkSite site) {
        thread(); if (!enabled) throw new IllegalStateException("NPC AI disabled"); service.getInventory(id, owner);
        if (!mode.implemented()) throw new UnsupportedOperationException("NPC work mode requires its actual runtime migration: " + mode.sourceId());
        var wolf = service.carrier(id); if (!site.worldId().equals(wolf.getWorld().getUID())) throw new IllegalArgumentException("Work site must be in NPC's world");
        var state = state(wolf); restoreTools(wolf);storage.forget(wolf);release(wolf); wolf.setTarget(null); wolf.getPathfinder().stopPathfinding();
        state.mode = mode; state.site = site; state.status = "ready"; persist(wolf); service.beginWork(id, owner);
    }
    public void workHere(UUID id, UUID owner, NPCWorkMode mode) {
        var wolf = service.carrier(id); var pos = wolf.getLocation();
        work(id, owner, mode, new NPCWorkSite(wolf.getWorld().getUID(), pos.getBlockX(), (int) Math.round(pos.getY()), pos.getBlockZ(), 8, 8));
    }
    private static String checkedHoneyProduct(String value){if(!List.of("auto","honey","honeycomb").contains(value))throw new IllegalArgumentException("Unknown honey product "+value);return value;}
    public void configureHoneyWork(UUID id,UUID owner,boolean requireSmoke,String product){thread();service.getInventory(id,owner);var wolf=service.carrier(id);var state=state(wolf);state.honeyProduct=checkedHoneyProduct(product);state.requireSmoke=requireSmoke;persist(wolf);}
    public Map<String,Object> inspectHusbandry(UUID id){thread();var wolf=service.carrier(id);var state=state(wolf);return Map.of("maxAnimals",NPCHusbandry.MAX_ANIMALS,"foodReserve",NPCHusbandry.FOOD_RESERVE,"requireSmoke",state.requireSmoke,"honeyProduct",state.honeyProduct,"toolBorrowed",wolf.getPersistentDataContainer().has(toolSlotKey));}
    /** Real bounded transaction fixture; returns how many actual food units were consumed (0..2). */
    public int breedOnce(UUID id,UUID owner,Animals first,Animals second){
        thread();service.getInventory(id,owner);var wolf=service.carrier(id);
        if(first==second||first.getType()!=second.getType()||!working(wolf,NPCWorkMode.BREED)||!husbandry.ready(wolf,first)||!husbandry.valid(wolf,second)||second.customName()!=null||husbandry.countFood(wolf,first)<(second.isLoveMode()?1:2)+NPCHusbandry.FOOD_RESERVE||!claim(wolf,husbandry.breedKeys(wolf,first,second)))return 0;
        var same=husbandry.animals(wolf).stream().filter(animal->animal.getType()==first.getType()).toList();
        if(!NPCHusbandry.withinLimit(same.size(),(int)same.stream().filter(Animals::isLoveMode).count())){release(wolf);return 0;}
        try{int fed=husbandry.feed(wolf,first,first,second,0)?1:0;if(fed==1&&!second.isLoveMode()&&husbandry.feed(wolf,second,first,second,1))fed++;return fed;}finally{restoreTools(wolf);storage.forget(wolf);release(wolf);}
    }
    public boolean shearOnce(UUID id,UUID owner,Sheep sheep){thread();service.getInventory(id,owner);var wolf=service.carrier(id);if(!claim(wolf,List.of(sheep.getUniqueId())))return false;try{return husbandry.shear(wolf,sheep);}finally{restoreTools(wolf);storage.forget(wolf);release(wolf);}}
    public boolean honeyOnce(UUID id,UUID owner,Block block){thread();service.getInventory(id,owner);var wolf=service.carrier(id);if(!claim(wolf,block))return false;try{return husbandry.honey(wolf,block);}finally{restoreTools(wolf);storage.forget(wolf);release(wolf);}}
    public void configureStorageWork(UUID id,UUID owner,NPCStorageSettings settings){thread();service.getInventory(id,owner);var wolf=service.carrier(id);state(wolf).storageSettings=Objects.requireNonNull(settings);persist(wolf);storage.forget(wolf);}
    public NPCStorageSettings inspectStorageSettings(UUID id){thread();return state(service.carrier(id)).storageSettings;}
    public int smeltOnce(UUID id,UUID owner,Block block){thread();service.getInventory(id,owner);var wolf=service.carrier(id);if(!working(wolf,NPCWorkMode.SMELT)||!state(wolf).site.contains(block.getLocation()))return 0;var access=NPCContainerWorld.furnace(block);if(access==null||!storage.reached(wolf,access,2.5))return 0;var job=storage.furnacePlan(wolf,List.of(access));if(job==null||!claim(wolf,storage.keys(access)))return 0;try{return storage.furnace(wolf,job);}finally{release(wolf);}}
    public int sortOnce(UUID id,UUID owner){thread();service.getInventory(id,owner);var wolf=service.carrier(id);if(!working(wolf,NPCWorkMode.CHEST_CLASSIFICATION))return 0;var plan=storage.sortPlan(wolf);state(wolf).status=plan.status();var job=plan.job();if(job==null||!storage.reached(wolf,job.visiting().access(),3)||!claim(wolf,storage.keys(job)))return 0;try{var result=storage.sort(wolf,job);release(wolf);if(result.next()!=null)claim(wolf,storage.keys(result.next()));return result.moved();}catch(RuntimeException failure){release(wolf);storage.forget(wolf);throw failure;}}
    public Map<String,Object> inspectSorting(UUID id){thread();var wolf=service.carrier(id);var plan=storage.sortPlan(wolf);var job=plan.job();return Map.of("status",plan.status(),"pickup",job!=null&&job.pickup(),"routed",job!=null&&job.routed(),"source",job==null||job.source()==null?"":job.source().access().block().getLocation().toString(),"destination",job==null?"":job.destination().access().block().getLocation().toString(),"amount",job==null?0:job.amount());}
    public Map<String,Object> inspectStorage(Block block){thread();var access=NPCContainerWorld.storage(block);if(access==null)return Map.of("supported",false);return Map.of("supported",true,"slots",access.inventory().getSize(),"rule",storage.rules.store(access).rule().mode().name(),"inventory",NPCSnapshot.copy(access.inventory().getContents()),"parts",access.parts().stream().map(b->b.getLocation().toString()).toList());}
    public NPCWorkSnapshot inspectWork(UUID id) {
        thread(); var wolf = service.carrier(id); var state = state(wolf);
        return new NPCWorkSnapshot(id, state.mode, state.site, state.status, state.actions,
                wolf.getTarget() == null ? null : wolf.getTarget().getUniqueId(), service.mode(wolf) == NPCMode.WORKING);
    }
    boolean working(Wolf wolf, NPCWorkMode mode) {
        return enabled && wolf.isValid() && !wolf.isDead() && service.ownerId(wolf) != null && service.mode(wolf) == NPCMode.WORKING
                && state(wolf).mode == mode && state(wolf).site.worldId().equals(wolf.getWorld().getUID());
    }
    void action(Wolf wolf) { var state = state(wolf); state.actions++; state.status = "ready"; persist(wolf); refresh.accept(wolf.getUniqueId()); }
    ItemStack[] inventory(Wolf wolf) { return service.runtimeInventory(wolf); }
    void inventory(Wolf wolf, ItemStack[] items) { service.storeRuntimeInventory(wolf, items); refresh.accept(wolf.getUniqueId()); }
    NPCWorkToolLease tools(Wolf wolf) { return tools.computeIfAbsent(wolf.getUniqueId(),id->new NPCWorkToolLease(this,wolf)); }
    void syncNativeEquipment(Wolf wolf) {
        var items=inventory(wolf);var equipment=wolf.getEquipment();
        items[18]=equipment.getHelmet();items[19]=equipment.getChestplate();items[20]=equipment.getLeggings();items[21]=equipment.getBoots();items[22]=equipment.getItemInMainHand();items[23]=equipment.getItemInOffHand();inventory(wolf,items);
    }
    static String itemId(ItemStack item) {
        if (item == null || item.isEmpty()) return "minecraft:air";
        var id = CraftEngineItems.getCustomItemId(item); return id == null ? item.getType().getKey().asString() : id.asString();
    }
    Block loaded(Wolf wolf, NPCWorkSite.Pos pos) {
        var world = wolf.getWorld(); if (pos.y() < world.getMinHeight() || pos.y() >= world.getMaxHeight() || !world.isChunkLoaded(pos.x() >> 4, pos.z() >> 4)) return null;
        return world.getBlockAt(pos.x(), pos.y(), pos.z());
    }
    Resource resource(Block block) { return new Resource(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ()); }
    boolean available(Wolf wolf, Block block) {
        var lease = claims.get(resource(block)); return lease == null || lease.until() <= Bukkit.getCurrentTick() || lease.owner().equals(wolf.getUniqueId());
    }
    boolean claim(Wolf wolf, Block block) {
        claims.values().removeIf(lease -> lease.until() <= Bukkit.getCurrentTick()); if (!available(wolf, block)) return false;
        claims.put(resource(block), new Lease(wolf.getUniqueId(), (long) Bukkit.getCurrentTick() + 200)); return true;
    }
    boolean available(Wolf wolf,Object key) {var lease=claims.get(key);return lease==null||lease.until()<=Bukkit.getCurrentTick()||lease.owner().equals(wolf.getUniqueId());}
    boolean claim(Wolf wolf,List<?> keys) {
        claims.values().removeIf(lease->lease.until()<=Bukkit.getCurrentTick());
        if(keys.stream().anyMatch(key->!available(wolf,key)))return false;
        for(var key:keys)claims.put(key,new Lease(wolf.getUniqueId(),(long)Bukkit.getCurrentTick()+200));return true;
    }
    void release(Wolf wolf) { claims.values().removeIf(lease -> lease.owner().equals(wolf.getUniqueId())); }
    void restoreTools(Wolf wolf){var lease=tools.get(wolf.getUniqueId());if(lease!=null)lease.restore();}
    static Crop crop(Block block, boolean onlyMature) {
        var rule = FarmingModule.inspect(block);
        if (rule != null) {
            if (onlyMature && FarmingModule.age(block) < rule.maxAge()) return null;
            var def = CraftEngineBlocks.byId(Key.of(rule.id())); return new Crop(rule.seed(), CraftEngineBlocks.getBukkitBlockData(def.defaultState()));
        }
        String seed = switch (block.getType()) {
            case WHEAT -> "wheat_seeds"; case CARROTS -> "carrot"; case POTATOES -> "potato";
            case BEETROOTS -> "beetroot_seeds"; case TORCHFLOWER_CROP -> "torchflower_seeds"; default -> null;
        };
        if (seed == null || !(block.getBlockData() instanceof Ageable age)) return null;
        if (onlyMature && age.getAge() < age.getMaximumAge()) return null;
        age.setAge(0); return new Crop("minecraft:" + seed, age);
    }
    static BlockData seedBlock(ItemStack item) {
        String id = itemId(item);
        for (var crop : FarmingModule.crops().values()) if (crop.seed().equals(id)) return CraftEngineBlocks.getBukkitBlockData(CraftEngineBlocks.byId(Key.of(crop.id())).defaultState());
        Material material = switch (item.getType()) {
            case WHEAT_SEEDS -> Material.WHEAT; case CARROT -> Material.CARROTS; case POTATO -> Material.POTATOES;
            case BEETROOT_SEEDS -> Material.BEETROOTS; case TORCHFLOWER_SEEDS -> Material.TORCHFLOWER_CROP; default -> null;
        };
        return material == null || !id.startsWith("minecraft:") ? null : material.createBlockData();
    }
    int selectedSeed(ItemStack[] inventory) { for (int slot : new int[]{22, 23}) if (!inventory[slot].isEmpty() && seedBlock(inventory[slot]) != null) return slot; return -1; }
    boolean emptyPlot(Block block) {
        return block.getType().isAir() && block.getY() > block.getWorld().getMinHeight() && FarmingModule.isSoil(block.getRelative(0, -1, 0));
    }
    boolean commit(Wolf wolf, Block block, BlockData expected, BlockData next, ItemStack[] expectedInventory) {
        if (!block.getBlockData().matches(expected) || !block.canPlace(next)) return false;
        var owner = Bukkit.getPlayer(service.ownerId(wolf));
        if (owner != null && (!BukkitCraftEngine.instance().antiGriefProvider().test(owner, Flag.BREAK, block.getLocation())
                || !BukkitCraftEngine.instance().antiGriefProvider().test(owner, Flag.PLACE, block.getLocation()))) return false;
        var event = new EntityChangeBlockEvent(wolf, block, next); Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled() || !block.getBlockData().matches(expected) || !Arrays.equals(expectedInventory, inventory(wolf))) return false;
        block.setBlockData(next, true);
        if (!block.getBlockData().matches(next) || !Arrays.equals(expectedInventory, inventory(wolf))) {
            if (block.getBlockData().matches(next)) block.setBlockData(expected, false); return false;
        }
        return true;
    }
    public boolean farmOnce(UUID id, UUID owner, Block block) {
        thread(); service.getInventory(id, owner); var wolf = service.carrier(id); var state = state(wolf);
        if (!working(wolf, NPCWorkMode.FARM) || !state.site.contains(block.getLocation()) || !block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)) return false;
        var before = block.getBlockData(); var items = inventory(wolf); var expectedItems = NPCSnapshot.copy(items); var crop = crop(block, true);
        if (crop != null) {
            List<ItemStack> drops = drops(wolf, block, items[22]); int lootSeed = -1, slot = -1;
            for (int i = 0; i < drops.size(); i++) if (!drops.get(i).isEmpty() && itemId(drops.get(i)).equals(crop.seed())) { lootSeed = i; break; }
            if (lootSeed < 0) {
                for (int i = 0; i < 18; i++) if (!items[i].isEmpty() && itemId(items[i]).equals(crop.seed())) { slot = i; break; }
                if (slot < 0) for (int i : new int[]{22, 23}) if (!items[i].isEmpty() && itemId(items[i]).equals(crop.seed())) { slot = i; break; }
                if (slot < 0) { state.status = "need_matching_seeds"; return false; }
            }
            if (!commit(wolf, block, before, crop.young(), expectedItems)) { state.status = "work_write_refused"; return false; }
            if (lootSeed >= 0) drops.get(lootSeed).subtract(1); else items[slot].subtract(1);
            var overflow = new ArrayList<ItemStack>(); for (var drop : drops) { insertCargo(items, drop); if (!drop.isEmpty()) overflow.add(drop); }
            inventory(wolf, items); for (var drop : overflow) block.getWorld().dropItemNaturally(block.getLocation().add(.5, .5, .5), drop);
        } else {
            if (!emptyPlot(block)) return false; int hand = selectedSeed(items);
            if (hand < 0) { state.status = "need_seeds"; return false; }
            var next = seedBlock(items[hand]); if (!commit(wolf, block, before, next, expectedItems)) { state.status = "work_write_refused"; return false; }
            items[hand].subtract(1); inventory(wolf, items);
        }
        wolf.swingMainHand(); block.getWorld().playSound(block.getLocation(), Sound.ITEM_CROP_PLANT, 1, 1); action(wolf); return true;
    }
    private List<ItemStack> drops(Wolf wolf, Block block, ItemStack tool) {
        var custom = CraftEngineBlocks.getCustomBlockState(block); var result = new ArrayList<ItemStack>();
        if (custom == null) { for (var stack : block.getDrops(tool, wolf)) result.add(stack.clone()); return result; }
        var world = BukkitAdaptor.adapt(block.getWorld());
        var context = ContextHolder.builder(DirectContextParameters.CUSTOM_BLOCK_STATE, custom,
                DirectContextParameters.POSITION, new WorldPosition(world, block.getX()+.5, block.getY()+.5, block.getZ()+.5),
                DirectContextParameters.BLOCK, BukkitAdaptor.adapt(block), DirectContextParameters.ITEM_IN_HAND, BukkitAdaptor.adapt(tool))
                .withParameter(DirectContextParameters.ENTITY, BukkitAdaptor.adapt(wolf)).build();
        for (var item : custom.getDrops(context, world, null)) result.add(((BukkitItem) item).getBukkitItem().clone()); return result;
    }
    static void insertCargo(ItemStack[] inventory, ItemStack item) {
        if (item == null || item.isEmpty()) return;
        for (int i = 0; i < 18 && !item.isEmpty(); i++) if (!inventory[i].isEmpty() && inventory[i].isSimilar(item)) {
            int moved = Math.min(item.getAmount(), inventory[i].getMaxStackSize() - inventory[i].getAmount()); if (moved > 0) { inventory[i].add(moved); item.subtract(moved); }
        }
        for (int i = 0; i < 18 && !item.isEmpty(); i++) if (inventory[i].isEmpty()) { int moved = Math.min(item.getAmount(), item.getMaxStackSize()); inventory[i] = item.asQuantity(moved); item.subtract(moved); }
    }
    LivingEntity canonical(LivingEntity entity) {
        var id = service.canonicalId(entity).orElse(null); return id != null && Bukkit.getEntity(id) instanceof LivingEntity body ? body : entity;
    }
    boolean mayAttack(Wolf wolf, LivingEntity candidate) {
        if (!enabled || candidate == null) return false; var target = canonical(candidate); var owner = service.ownerId(wolf);
        if (target == wolf || !target.isValid() || target.isDead() || target.getWorld() != wolf.getWorld() || target.getLocation().distanceSquared(wolf.getLocation()) > 1024 || target instanceof EnderDragon) return false;
        if (target instanceof Player player && (player.getUniqueId().equals(owner) || player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR)) return false;
        if (owner != null && target instanceof Tameable pet && pet.getOwner() != null && owner.equals(pet.getOwner().getUniqueId())) return false;
        if (target instanceof Wolf other && service.isNPC(other) && Objects.equals(owner, service.ownerId(other))) return false;
        var board = Bukkit.getScoreboardManager().getMainScoreboard();
        var mine = board.getEntryTeam(wolf.getUniqueId().toString()); var theirs = board.getEntryTeam(target instanceof Player p ? p.getName() : target.getUniqueId().toString());
        if (mine != null && mine.equals(theirs)) return false;
        return !working(wolf, NPCWorkMode.COMBAT) || state(wolf).site.contains(target.getLocation());
    }
    void acquireGuard(Wolf wolf) {
        var state = state(wolf); int now = Bukkit.getCurrentTick();
        if (!working(wolf, NPCWorkMode.COMBAT) || now < state.nextGuard) return; state.nextGuard = now + 20;
        if (wolf.getTarget() != null) { if (!mayAttack(wolf, wolf.getTarget())) wolf.setTarget(null); else return; }
        var site = state.site; var center = new Location(wolf.getWorld(), site.x()+.5, site.y()+.5, site.z()+.5);
        var targets = wolf.getWorld().getNearbyEntities(center, site.radius()+.5, site.height()+.5, site.radius()+.5).stream()
                .filter(e -> e instanceof Monster).map(e -> (LivingEntity)e).filter(e -> e.customName() == null && mayAttack(wolf,e) && wolf.hasLineOfSight(e))
                .sorted(Comparator.<LivingEntity>comparingInt(e -> e.getUniqueId().equals(state.ownerThreat) || e instanceof Mob mob && mob.getTarget() != null && mob.getTarget().getUniqueId().equals(service.ownerId(wolf)) ? 0 : 1)
                        .thenComparingDouble(e -> e.getLocation().distanceSquared(wolf.getLocation()))).toList();
        for (var target : targets) { var path = wolf.getPathfinder().findPath(target); if (path != null && path.canReachFinalPoint()) { wolf.setTarget(target); state.status = "guarding"; return; } }
    }
    boolean rangedPending(Wolf wolf) {
        var hand = wolf.getEquipment().getItemInMainHand();
        if (hand.getData(DataComponentTypes.KINETIC_WEAPON) != null || hand.getType() == Material.TRIDENT) return true;
        var customId = CraftEngineItems.getCustomItemId(hand); var def = customId == null ? null : CraftEngineItems.byId(customId);
        if (def != null && def.settings().tags().contains(Key.of(Ids.MOD_ID + ":danmaku_item"))) return true;
        if (hand.getType() == Material.CROSSBOW) return true;
        if (hand.getType() == Material.BOW) for (var item : inventory(wolf)) if (item.getType() == Material.ARROW || item.getType() == Material.TIPPED_ARROW || item.getType() == Material.SPECTRAL_ARROW) return true;
        return false;
    }
    boolean meleeReach(Wolf wolf, LivingEntity target) {
        double reach = Math.sqrt(2.0399999618530273) - .6000000238418579;
        return wolf.getBoundingBox().clone().expand(reach, 0, reach).overlaps(target.getBoundingBox());
    }
    boolean lineOfSight(Wolf wolf, LivingEntity target) {
        var box = target.getBoundingBox(); double cx=(box.getMinX()+box.getMaxX())/2, cy=(box.getMinY()+box.getMaxY())/2, cz=(box.getMinZ()+box.getMaxZ())/2;
        var points = List.of(new Vector(cx,box.getMinY()+.1,cz),new Vector(cx,cy,cz),new Vector(cx,box.getMaxY()-.1,cz),new Vector(box.getMinX()+.1,cy,cz),new Vector(box.getMaxX()-.1,cy,cz),new Vector(cx,cy,box.getMinZ()+.1),new Vector(cx,cy,box.getMaxZ()-.1));
        var eye=wolf.getEyeLocation(); for(var point:points) { var delta=point.clone().subtract(eye.toVector()); if(delta.lengthSquared()<1.0e-9 || wolf.getWorld().rayTraceBlocks(eye,delta,delta.length(),FluidCollisionMode.NEVER,true)==null) return true; } return false;
    }
    public boolean attackOnce(UUID id, LivingEntity victim) {
        thread(); var wolf=service.carrier(id); var target=canonical(victim); var state=state(wolf);
        if(!mayAttack(wolf,target)||rangedPending(wolf)||!meleeReach(wolf,target)||!lineOfSight(wolf,target)) return false;
        state.attacking=true; state.hit=false;
        try { wolf.swingMainHand(); var snapshot=service.inspect(id); if(snapshot.avatarId()!=null && Bukkit.getEntity(snapshot.avatarId()) instanceof LivingEntity avatar) avatar.swingMainHand(); wolf.attack(target); }
        finally { state.attacking=false; }
        if(state.hit) {var held=wolf.getEquipment().getItemInMainHand();if(!held.isEmpty()&&held.getDataOrDefault(DataComponentTypes.MAX_DAMAGE,0)>0){wolf.damageItemStack(EquipmentSlot.HAND,1);syncNativeEquipment(wolf);} action(wolf); } return state.hit;
    }
    private LivingEntity source(Entity damager) { return damager instanceof LivingEntity living ? living : damager instanceof Projectile p && p.getShooter() instanceof LivingEntity living ? living : null; }
    @EventHandler(priority=EventPriority.LOWEST,ignoreCancelled=true) public void ownerControl(EntityDamageByEntityEvent event) {
        if(!(event.getDamager() instanceof Player player)||!player.isSneaking()||!player.getInventory().getItemInMainHand().isEmpty()||!player.getInventory().getItemInOffHand().isEmpty()) return;
        var id=service.canonicalId(event.getEntity()).orElse(null); if(id==null)return; var wolf=service.carrier(id);
        if(player.getUniqueId().equals(service.ownerId(wolf))) {event.setCancelled(true);wolf.setTarget(null);wolf.getPathfinder().stopPathfinding();}
    }
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true) public void damage(EntityDamageByEntityEvent event) {
        if(event.getFinalDamage()<=0)return; var attacker=source(event.getDamager()); if(attacker==null)return;
        if(event.getDamager() instanceof Wolf wolf && states.containsKey(wolf.getUniqueId()) && state(wolf).attacking) state(wolf).hit=true;
        if(event.getEntity() instanceof Wolf wolf && states.containsKey(wolf.getUniqueId()) && mayAttack(wolf,attacker)) wolf.setTarget(canonical(attacker));
        if(event.getEntity() instanceof Player owner) for(var id:List.copyOf(states.keySet())) if(Bukkit.getEntity(id) instanceof Wolf wolf&&owner.getUniqueId().equals(service.ownerId(wolf))) {
            state(wolf).ownerThreat=attacker.getUniqueId(); if(mayAttack(wolf,attacker))wolf.setTarget(canonical(attacker));
        }
        if(attacker instanceof Player owner && event.getEntity() instanceof LivingEntity target) for(var id:List.copyOf(states.keySet())) if(Bukkit.getEntity(id) instanceof Wolf wolf&&owner.getUniqueId().equals(service.ownerId(wolf))&&mayAttack(wolf,target))wolf.setTarget(canonical(target));
    }
    @EventHandler(priority=EventPriority.LOWEST) public void unload(EntitiesUnloadEvent event) { for(var entity:event.getEntities()) if(entity instanceof Wolf wolf&&states.containsKey(wolf.getUniqueId())) {restoreTools(wolf);states.remove(wolf.getUniqueId());tools.remove(wolf.getUniqueId());storage.forget(wolf);release(wolf);} }
    @EventHandler(priority=EventPriority.MONITOR)public void died(EntityDeathEvent event){if(event.getEntity() instanceof Wolf wolf&&states.remove(wolf.getUniqueId())!=null){tools.remove(wolf.getUniqueId());storage.forget(wolf);release(wolf);}}
}
