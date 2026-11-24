package cc.thonly.reverie_dreams.paper.npc;

import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Locale;

/** Native inventory replacement for source work settings. Unsupported modes are shown explicitly, never activated. */
final class NPCWorkMenu implements Listener {
    private final JavaPlugin plugin;private final NPCService service;private final NPCAiService ai;
    NPCWorkMenu(JavaPlugin plugin,NPCService service,NPCAiService ai){this.plugin=plugin;this.service=service;this.ai=ai;}
    void enable(){Bukkit.getPluginManager().registerEvents(this,plugin);}
    void disable(){for(var player:Bukkit.getOnlinePlayers())if(player.getOpenInventory().getTopInventory().getHolder() instanceof Menu)player.closeInventory();HandlerList.unregisterAll(this);}
    private static Component text(String key,String fallback){return Component.translatable("reverie_dreams.npc_ai."+key).fallback(fallback);}
    private boolean allowed(Player player,UUID id){try{var state=service.inspect(id);var body=service.carrier(id);return player.getUniqueId().equals(state.ownerId())&&player.getWorld()==body.getWorld()&&player.getLocation().distanceSquared(body.getLocation())<=64;}catch(IllegalArgumentException missing){return false;}}
    void open(Player player,UUID id){if(!allowed(player,id))throw new SecurityException("Only the nearby owner may change NPC work");var menu=new Menu(player.getUniqueId(),id);menu.inventory=Bukkit.createInventory(menu,27,text("title","Companion work"));render(menu);player.openInventory(menu.inventory);}
    private static ItemStack icon(Material material,Component name,Component... lore){var item=new ItemStack(material);item.editMeta(meta->{meta.displayName(name);if(lore.length>0)meta.lore(List.of(lore));});return item;}
    private void render(Menu menu){menu.inventory.clear();var modes=NPCWorkMode.values();for(int i=0;i<modes.length;i++){var mode=modes[i];var material=switch(mode){case FARM->Material.WHEAT_SEEDS;case COMBAT->Material.IRON_SWORD;case BREED->Material.WHEAT;case SHEEP_SHEARING->Material.SHEARS;case HONEYCOMB_HARVESTING->Material.HONEYCOMB;case SMELT->Material.FURNACE;case CHEST_CLASSIFICATION->Material.CHEST;default->Material.GRAY_DYE;};menu.inventory.setItem(i,icon(material,text(mode.name().toLowerCase(Locale.ROOT),mode.sourceId()),text(mode.implemented()?"activate":"pending",mode.implemented()?"Start this work at the companion's position":"This work is not available yet")));}var work=ai.inspectWork(menu.npc);var options=ai.inspectHusbandry(menu.npc);menu.inventory.setItem(12,icon(Material.HONEY_BOTTLE,text("honey_product","Honey product"),text("product_"+options.get("honeyProduct"),options.get("honeyProduct").toString()),text("cycle","Click to cycle")));menu.inventory.setItem(13,icon(Material.CAMPFIRE,text("smoke","Require smoke"),text(Boolean.TRUE.equals(options.get("requireSmoke"))?"enabled":"disabled",options.get("requireSmoke").toString())));menu.inventory.setItem(18,icon(Material.COMPASS,text("anchor","Set work center here"),Component.text(work.site().x()+", "+work.site().y()+", "+work.site().z())));menu.inventory.setItem(20,icon(Material.IRON_BOOTS,text("follow","Stop work and follow")));menu.inventory.setItem(22,icon(Material.PAPER,text("status","Work status"),Component.text(work.status())));menu.inventory.setItem(26,icon(Material.BARRIER,text("close","Close")));}
    @EventHandler public void click(InventoryClickEvent event){if(!(event.getView().getTopInventory().getHolder() instanceof Menu menu))return;event.setCancelled(true);if(!(event.getWhoClicked() instanceof Player player)||!menu.owner.equals(player.getUniqueId())||!allowed(player,menu.npc))return;int slot=event.getRawSlot();if(slot<0||slot>=27||!event.getClick().isLeftClick()&&!event.getClick().isRightClick())return;try{if(slot<NPCWorkMode.values().length){var mode=NPCWorkMode.values()[slot];if(!mode.implemented()){player.sendMessage(text("pending","This work is not available yet"));return;}ai.workHere(menu.npc,player.getUniqueId(),mode);}else if(slot==12){var options=ai.inspectHusbandry(menu.npc);var product=options.get("honeyProduct").toString();var next=product.equals("auto")?"honey":product.equals("honey")?"honeycomb":"auto";ai.configureHoneyWork(menu.npc,player.getUniqueId(),Boolean.TRUE.equals(options.get("requireSmoke")),next);}else if(slot==13){var options=ai.inspectHusbandry(menu.npc);ai.configureHoneyWork(menu.npc,player.getUniqueId(),!Boolean.TRUE.equals(options.get("requireSmoke")),options.get("honeyProduct").toString());}else if(slot==18)ai.workHere(menu.npc,player.getUniqueId(),ai.inspectWork(menu.npc).workMode());else if(slot==20)service.follow(menu.npc,player.getUniqueId());else if(slot==26){Bukkit.getScheduler().runTask(plugin,()->player.closeInventory());return;}render(menu);}catch(IllegalArgumentException|IllegalStateException|SecurityException invalid){Bukkit.getScheduler().runTask(plugin,()->player.closeInventory());}}
    @EventHandler public void drag(InventoryDragEvent event){if(event.getView().getTopInventory().getHolder() instanceof Menu)event.setCancelled(true);}
    private static final class Menu implements InventoryHolder {final UUID owner,npc;Inventory inventory;Menu(UUID owner,UUID npc){this.owner=owner;this.npc=npc;}@Override public Inventory getInventory(){return inventory;}}
}
