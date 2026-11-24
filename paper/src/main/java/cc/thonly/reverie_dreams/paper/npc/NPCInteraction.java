package cc.thonly.reverie_dreams.paper.npc;

import cc.thonly.reverie_dreams.paper.util.Ids;

import com.google.gson.JsonParser;
import io.papermc.paper.datacomponent.DataComponentTypes;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.IOException;
import java.util.function.BiConsumer;
import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import org.bukkit.attribute.Attribute;

/** Original recruitment and owner interaction through vanilla inventories; no custom client screen is needed. */
public final class NPCInteraction implements Listener {
    public enum RecruitResult { SUCCESS, FAILED, NOT_APPLICABLE }
    private final JavaPlugin plugin;
    private final NPCService service;
    private BiConsumer<Player, UUID> workMenuHandler;
    private final Map<String, List<String>> cards = new LinkedHashMap<>();
    private final Map<String, String> itemToCard = new LinkedHashMap<>();
    private final List<String> tameFood = new ArrayList<>();
    private final Map<UUID, InteractionMark> recent = new HashMap<>();
    private final NamespacedKey cardGroupKey;

    NPCInteraction(JavaPlugin plugin, NPCService service) {
        this.plugin = plugin; this.service = service; cardGroupKey = new NamespacedKey(plugin, "npc/role_card_group");
        try (var input = plugin.getResource("migration/npc-domain.json")) {
            if (input == null) throw new IllegalStateException("Missing NPC interaction domain");
            var root = JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
            for (var entry : root.getAsJsonObject("roleCards").entrySet()) {
                var roles = new ArrayList<String>();
                for (var value : entry.getValue().getAsJsonObject().getAsJsonArray("roles")) roles.add(value.getAsString());
                cards.put(entry.getKey(), List.copyOf(roles));
            }
            var interaction = root.getAsJsonObject("interaction");
            for (var value : interaction.getAsJsonArray("recruitFoodItems")) tameFood.add(value.getAsString());
            for (var entry : interaction.getAsJsonObject("roleCardItems").entrySet()) itemToCard.put(entry.getKey(), entry.getValue().getAsString());
        } catch (IOException failure) { throw new IllegalStateException("Cannot read NPC interaction domain", failure); }
    }

    void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        service.setInteractionHandler(this::interact);
        service.setCloseInventoryHandler(this::closeForNPC);
        service.setArchiveFactory(() -> customItem(Ids.MOD_ID + ":role_archive"));
    }
    void disable() {
        for (var player : Bukkit.getOnlinePlayers()) if (player.getOpenInventory().getTopInventory().getHolder() instanceof Menu)
            player.closeInventory();
        service.setInteractionHandler(null); service.setCloseInventoryHandler(null); HandlerList.unregisterAll(this); recent.clear();
    }

    private static Component text(String id, String fallback) {
        return Component.translatable("gui.reverie_dreams.paper.npc." + id).fallback(fallback);
    }
    private static ItemStack icon(Material material, Component name) {
        var item = new ItemStack(material); item.editMeta(meta -> meta.displayName(name)); return item;
    }
    private static String itemId(ItemStack item) {
        if (item == null || item.isEmpty()) return "minecraft:air";
        var custom = CraftEngineItems.getCustomItemId(item);
        return custom == null ? item.getType().getKey().asString() : custom.asString();
    }
    private static ItemStack customItem(String id) {
        var definition = CraftEngineItems.byId(id);
        if (definition == null) throw new IllegalStateException("CraftEngine item is not loaded: " + id);
        return definition.buildBukkitItem();
    }

    /** Source component variants are exposed as 19 stable CE adapters; also writes a durable group marker. */
    public ItemStack createRoleCard(String groupId) {
        String group = groupId.contains(":") ? groupId : Ids.MOD_ID + ":" + groupId;
        if (!cards.containsKey(group)) throw new IllegalArgumentException("Unknown role card group: " + groupId);
        var item = customItem(Ids.MOD_ID + ":" + group.split(":", 2)[1] + "_role_card");
        item.editMeta(meta -> meta.getPersistentDataContainer().set(cardGroupKey, PersistentDataType.STRING, group));
        return item;
    }

    /** Testable real transaction: accepts the random roll, validates food/unowned NPC, consumes and heals once. */
    public RecruitResult attemptRecruit(Player player, UUID npc, double roll) {
        if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("NPC recruitment requires server thread");
        if (!Double.isFinite(roll) || roll < 0.0 || roll >= 1.0) throw new IllegalArgumentException("Recruitment roll must be in [0,1)");
        var state = service.inspect(npc); var held = player.getInventory().getItemInMainHand();
        if (state.ownerId() != null || !tameFood.contains(itemId(held))) return RecruitResult.NOT_APPLICABLE;
        boolean recruited = roll <= 0.4 && service.recruit(npc, player.getUniqueId());
        if (Bukkit.getEntity(npc) instanceof Wolf carrier)
            carrier.setHealth(Math.min(carrier.getHealth() + 5.0, Objects.requireNonNull(carrier.getAttribute(Attribute.MAX_HEALTH)).getValue()));
        if (player.getGameMode() != GameMode.CREATIVE) held.subtract(1);
        player.swingMainHand();
        if (recruited) {
            var where = Bukkit.getEntity(npc).getLocation().add(0, 1, 0);
            where.getWorld().spawnParticle(Particle.HEART, where, 5, 0.5, 0.5, 0.5, 0.1);
            player.sendMessage(text("recruited", "You became friends."));
            return RecruitResult.SUCCESS;
        }
        player.sendMessage(text("recruit_failed", "Try offering another cake.")); return RecruitResult.FAILED;
    }

    private void interact(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        var player = event.getPlayer(); var id = service.canonicalId(event.getRightClicked()).orElse(null);
        if (id == null) return;
        var mark = new InteractionMark(id, Bukkit.getCurrentTick());
        if (mark.equals(recent.put(player.getUniqueId(), mark))) return;
        NPCSnapshot state;
        try { state = service.inspect(id); } catch (IllegalArgumentException missing) { return; }
        if (state.ownerId() == null) { attemptRecruit(player, id, ThreadLocalRandom.current().nextFloat()); return; }
        if (player.isSneaking() && player.getInventory().getItemInMainHand().isEmpty()) {
            if (!player.getUniqueId().equals(state.ownerId())) player.sendMessage(text("not_owner", "This companion belongs to another player."));
            else openInventory(player, id);
        }
    }

    void setWorkMenuHandler(BiConsumer<Player, UUID> handler) { workMenuHandler = handler; }
    void refreshNPC(UUID npc) {
        for (var player : Bukkit.getOnlinePlayers()) if (player.getOpenInventory().getTopInventory().getHolder() instanceof StorageMenu menu && menu.npc.equals(npc)) {
            try { refresh(menu, service.getInventory(npc, player.getUniqueId())); }
            catch (IllegalArgumentException | IllegalStateException | SecurityException missing) { player.closeInventory(); }
        }
    }

    public void openInventory(Player player, UUID npc) {
        var state = service.inspect(npc);
        if (!player.getUniqueId().equals(state.ownerId())) throw new SecurityException("Only the owner can open the NPC inventory");
        var menu = new StorageMenu(player.getUniqueId(), npc);
        menu.inventory = Bukkit.createInventory(menu, 27, text("inventory", "Companion inventory"));
        refresh(menu, state.inventory()); player.openInventory(menu.inventory);
    }

    private static void refresh(StorageMenu menu, ItemStack[] content) {
        for (int slot = 0; slot < 24; slot++) menu.inventory.setItem(slot, content[slot].isEmpty() ? null : content[slot].clone());
        var follow = icon(Material.IRON_BOOTS, text("follow", "Follow"));
        follow.editMeta(meta -> meta.lore(List.of(Component.translatable("reverie_dreams.npc_ai.work_hint").fallback("Right-click: work settings"))));
        menu.inventory.setItem(24, follow);
        menu.inventory.setItem(25, icon(Material.CHEST, text("stay", "Stay here")));
        menu.inventory.setItem(26, icon(Material.BARRIER, text("close", "Close")));
    }

    public void closeForNPC(UUID id) {
        for (var player : Bukkit.getOnlinePlayers()) if (player.getOpenInventory().getTopInventory().getHolder() instanceof StorageMenu menu && menu.npc.equals(id))
            player.closeInventory();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onUseRoleCard(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) return;
        var held = event.getPlayer().getInventory().getItemInMainHand(); var id = itemId(held);
        if (!id.equals(Ids.MOD_ID + ":role_card") && !itemToCard.containsKey(id)) return;
        var block = CraftEngineBlocks.getCustomBlockState(event.getClickedBlock());
        if (block != null && block.owner().value().id().asString().equals(Ids.MOD_ID + ":gensokyo_altar")) return;
        event.setCancelled(true);
        openRoleCard(event.getPlayer(), event.getClickedBlock().getLocation().add(0.5, 1.0, 0.5));
    }

    public void openRoleCard(Player player, Location spawnLocation) {
        var held = player.getInventory().getItemInMainHand();
        String id = itemId(held), group = itemToCard.get(id);
        if (!id.equals(Ids.MOD_ID + ":role_card") && !itemToCard.containsKey(id)) throw new IllegalArgumentException("No role card in main hand");
        if (group == null) group = held.getPersistentDataContainer().get(cardGroupKey, PersistentDataType.STRING);
        List<String> allowed = group == null ? null : cards.get(group);
        if (allowed == null && id.equals(Ids.MOD_ID + ":role_card") && plugin.getConfig().getBoolean("npc.free-choice-of-role", false))
            allowed = service.roles().stream().map(NPCRole::id).toList();
        if (allowed == null || allowed.isEmpty()) { player.sendMessage(text("unknown_card", "This role card has no group.")); return; }
        var menu = new CardMenu(player.getUniqueId(), spawnLocation.clone(), held.clone(), List.copyOf(allowed));
        menu.inventory = Bukkit.createInventory(menu, 54, text("card_title", "Choose a role"));
        renderCard(menu); player.openInventory(menu.inventory);
    }

    private void renderCard(CardMenu menu) {
        menu.inventory.clear();
        for (int i = 0; i < 45; i++) {
            int index = menu.page * 45 + i; if (index >= menu.roles.size()) break;
            String id = menu.roles.get(index);
            var role = service.roles().stream().filter(r -> r.id().equals(id)).findFirst().orElseThrow();
            menu.inventory.setItem(i, icon(Material.PAPER, role.displayName()));
        }
        menu.inventory.setItem(49, icon(Material.NETHER_STAR, text("random", "Random role")));
        menu.inventory.setItem(50, icon(Material.BARRIER, text("close", "Close")));
        if (menu.page > 0) menu.inventory.setItem(52, icon(Material.ARROW, text("previous", "Previous")));
        if ((menu.page + 1) * 45 < menu.roles.size()) menu.inventory.setItem(53, icon(Material.ARROW, text("next", "Next")));
    }

    private void summon(CardMenu menu, Player player, String role) {
        if (menu.completed) return;
        menu.completed = true;
        Bukkit.getScheduler().runTask(plugin, () -> {
            var held = player.getInventory().getItemInMainHand();
            if (!player.isOnline() || player.getWorld() != menu.spawn.getWorld() || player.getLocation().distanceSquared(menu.spawn) > 64
                    || held.isEmpty() || !held.isSimilar(menu.card) || !menu.roles.contains(role)) {
                if (player.isOnline()) { player.closeInventory(); player.sendMessage(text("invalid_card", "Keep the same role card and stay nearby.")); }
                return;
            }
            try {
                service.spawnRole(role, menu.spawn);
                if (player.getGameMode() != GameMode.CREATIVE) held.subtract(1);
                player.closeInventory(); player.swingMainHand();
                player.getWorld().playSound(player.getLocation(), "minecraft:item.bucket.fill", 2.0f, 1.0f);
            } catch (RuntimeException failure) {
                player.closeInventory(); plugin.getLogger().warning("Role card summon failed: " + failure.getMessage());
                player.sendMessage(text("invalid_card", "The role could not be summoned here."));
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof Menu raw)) return;
        if (!(event.getWhoClicked() instanceof Player player) || !raw.owner.equals(player.getUniqueId())) { event.setCancelled(true); return; }
        int slot = event.getRawSlot();
        if (raw instanceof CardMenu menu) {
            event.setCancelled(true);
            if (menu.completed || slot < 0 || slot >= 54 || !event.getClick().isLeftClick() && !event.getClick().isRightClick()) return;
            if (slot < 45) { int index = menu.page * 45 + slot; if (index < menu.roles.size()) summon(menu, player, menu.roles.get(index)); }
            else if (slot == 49) summon(menu, player, menu.roles.get(ThreadLocalRandom.current().nextInt(menu.roles.size())));
            else if (slot == 50) Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
            else if (slot == 52 && menu.page > 0) { menu.page--; renderCard(menu); }
            else if (slot == 53 && (menu.page + 1) * 45 < menu.roles.size()) { menu.page++; renderCard(menu); }
            return;
        }
        var menu = (StorageMenu) raw;
        if (slot >= 27 || slot < 0) {
            if (event.isShiftClick() || event.getClick().isKeyboardClick() || event.getClick() == ClickType.DOUBLE_CLICK) event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        try {
            if (slot == 24) {
                if (event.getClick().isRightClick() && workMenuHandler != null) Bukkit.getScheduler().runTask(plugin, () -> workMenuHandler.accept(player, menu.npc));
                else service.follow(menu.npc, player.getUniqueId());
                return;
            }
            if (slot == 25) { service.stay(menu.npc, player.getUniqueId()); return; }
            if (slot == 26) { Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory()); return; }
            if (event.getClick() != ClickType.LEFT && event.getClick() != ClickType.RIGHT) {
                player.sendMessage(text("basic_clicks", "Use left or right click to move items.")); return;
            }
            var content = service.getInventory(menu.npc, player.getUniqueId());
            var cursor = player.getItemOnCursor().clone(); var target = content[slot].clone();
            int maximum = slot >= 18 && slot <= 21 ? 1 : cursor.isEmpty() ? 64 : cursor.getMaxStackSize();
            if (!cursor.isEmpty() && slot >= 18 && slot <= 21) {
                var equippable = cursor.getData(DataComponentTypes.EQUIPPABLE);
                var expected = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}[slot - 18];
                if (equippable == null || equippable.slot() != expected) return;
            }
            ItemStack nextCursor;
            if (cursor.isEmpty()) {
                if (target.isEmpty()) return;
                int count = event.getClick() == ClickType.RIGHT ? (target.getAmount() + 1) / 2 : target.getAmount();
                nextCursor = target.asQuantity(count); content[slot] = target.getAmount() == count ? ItemStack.empty() : target.asQuantity(target.getAmount() - count);
            } else if (target.isEmpty() || target.isSimilar(cursor)) {
                int existing = target.isEmpty() ? 0 : target.getAmount();
                int moved = Math.min(cursor.getAmount(), Math.max(0, Math.min(maximum, cursor.getMaxStackSize()) - existing));
                if (event.getClick() == ClickType.RIGHT) moved = Math.min(1, moved);
                if (moved == 0) return;
                content[slot] = cursor.asQuantity(existing + moved);
                nextCursor = cursor.getAmount() == moved ? ItemStack.empty() : cursor.asQuantity(cursor.getAmount() - moved);
            } else {
                if (cursor.getAmount() > maximum) return;
                content[slot] = cursor; nextCursor = target;
            }
            service.storeInventory(menu.npc, player.getUniqueId(), content);
            player.setItemOnCursor(nextCursor); refresh(menu, content);
        } catch (IllegalArgumentException | IllegalStateException | SecurityException missing) {
            Bukkit.getScheduler().runTask(plugin, () -> player.closeInventory());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof Menu)) return;
        int size = event.getView().getTopInventory().getSize();
        if (event.getRawSlots().stream().anyMatch(slot -> slot < size)) event.setCancelled(true);
    }
    @EventHandler public void onQuit(PlayerQuitEvent event) { recent.remove(event.getPlayer().getUniqueId()); }

    private record InteractionMark(UUID npc, int tick) { }
    private abstract static class Menu implements InventoryHolder {
        final UUID owner;
        Inventory inventory;
        Menu(UUID owner) { this.owner = owner; }
        @Override public Inventory getInventory() { return inventory; }
    }
    private static final class StorageMenu extends Menu {
        final UUID npc;
        StorageMenu(UUID owner, UUID npc) { super(owner); this.npc = npc; }
    }
    private static final class CardMenu extends Menu {
        final Location spawn;
        final ItemStack card;
        final List<String> roles;
        int page;
        boolean completed;
        CardMenu(UUID owner, Location spawn, ItemStack card, List<String> roles) {
            super(owner); this.spawn = spawn; this.card = card; this.roles = roles;
        }
    }
}
