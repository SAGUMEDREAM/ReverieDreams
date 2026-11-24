package cc.thonly.reverie_dreams.paper.storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.event.HandlerList;

/** Native persistent empty-display carriers keep broken infinite-store remainders safe without a giant single-tick spawn. */
public final class StorageDropService implements Listener {
    public static final NamespacedKey KEY=new NamespacedKey("reverie_dreams","storage_pending_drops");
    public record Pending(UUID marker,Location location,long units,int identities){}
    private final JavaPlugin plugin;
    private final Map<UUID,Queue> pending=new LinkedHashMap<>();
    private BukkitTask task;
    StorageDropService(JavaPlugin plugin){this.plugin=plugin;}
    void enable(){Bukkit.getPluginManager().registerEvents(this,plugin);for(var world:Bukkit.getWorlds())for(var entity:world.getEntities())if(entity instanceof ItemDisplay display)restore(display);task=Bukkit.getScheduler().runTaskTimer(plugin,this::tick,1,1);}
    void disable(){if(task!=null)task.cancel();for(var queue:pending.values())persist(queue);pending.clear();HandlerList.unregisterAll(this);}
    public List<Pending> snapshot(){PlayerFaithService.thread();return pending.values().stream().map(q->new Pending(q.marker.getUniqueId(),q.marker.getLocation(),q.entries.stream().mapToLong(e->e.count).sum(),q.entries.size())).toList();}
    public List<CupboardController.Entry> entries(UUID marker){PlayerFaithService.thread();var queue=pending.get(marker);if(queue==null)return List.of();var result=new ArrayList<CupboardController.Entry>();for(int i=0;i<queue.entries.size();i++){var entry=queue.entries.get(i);result.add(new CupboardController.Entry(i,entry.prototype.asOne(),entry.count));}return List.copyOf(result);}
    public void enqueue(Location location,List<CupboardController.Entry> items){
        PlayerFaithService.thread();var entries=new ArrayList<Entry>();for(var item:items)if(item.count()>0&&!CupboardController.empty(item.prototype()))entries.add(new Entry(item.prototype().asOne(),item.count()));if(entries.isEmpty())return;
        ItemDisplay marker=location.getWorld().spawn(location,ItemDisplay.class,entity->{entity.setItemStack(new ItemStack(Material.AIR));entity.setGravity(false);entity.setInvulnerable(true);entity.setPersistent(true);});
        if(!marker.isValid())throw new IllegalStateException("Persistent storage drop carrier spawn was rejected");
        var queue=new Queue(marker,entries);persist(queue);pending.put(marker.getUniqueId(),queue);drain(queue,64);
    }
    private void tick(){int budget=64;for(var queue:List.copyOf(pending.values())){
        if(!queue.marker.isValid()){pending.remove(queue.marker.getUniqueId());continue;}
        if(budget<=0)break;budget-=drain(queue,budget);
    }}
    private int drain(Queue queue,int budget){int emitted=0;
        while(emitted<budget&&!queue.entries.isEmpty()){
            var entry=queue.entries.getFirst();int amount=Math.min(entry.count,entry.prototype.getMaxStackSize());var stack=entry.prototype.clone();stack.setAmount(amount);
            var drop=queue.marker.getWorld().dropItemNaturally(queue.marker.getLocation(),stack);if(!drop.isValid())break;
            entry.count-=amount;emitted++;if(entry.count==0)queue.entries.removeFirst();
        }
        if(queue.entries.isEmpty()){pending.remove(queue.marker.getUniqueId());queue.marker.remove();}else persist(queue);return emitted;
    }
    private void persist(Queue queue){var json=new JsonObject();json.addProperty("schema",1);var entries=new JsonArray();for(var entry:queue.entries){var value=new JsonObject();value.addProperty("item",Base64.getEncoder().encodeToString(entry.prototype.serializeAsBytes()));value.addProperty("count",entry.count);entries.add(value);}json.add("entries",entries);queue.marker.getPersistentDataContainer().set(KEY,PersistentDataType.STRING,json.toString());}
    private void restore(ItemDisplay marker){String raw=marker.getPersistentDataContainer().get(KEY,PersistentDataType.STRING);if(raw==null||pending.containsKey(marker.getUniqueId()))return;try{
        var json=JsonParser.parseString(raw).getAsJsonObject();if(json.get("schema").getAsInt()!=1)throw new IllegalArgumentException("Unknown storage drop schema");var entries=new ArrayList<Entry>();for(var value:json.getAsJsonArray("entries")){var entry=value.getAsJsonObject();var item=ItemStack.deserializeBytes(Base64.getDecoder().decode(entry.get("item").getAsString()));int count=entry.get("count").getAsInt();if(count<=0||item.isEmpty())throw new IllegalArgumentException("Invalid storage drop entry");entries.add(new Entry(item.asOne(),count));}if(entries.isEmpty()){marker.remove();return;}pending.put(marker.getUniqueId(),new Queue(marker,entries));
        }catch(RuntimeException failure){plugin.getSLF4JLogger().error("Storage drop carrier {} preserved without activation",marker.getUniqueId(),failure);}}
    @EventHandler public void load(EntitiesLoadEvent event){for(Entity entity:event.getEntities())if(entity instanceof ItemDisplay display)restore(display);}
    @EventHandler public void unload(EntitiesUnloadEvent event){for(Entity entity:event.getEntities()){var queue=pending.remove(entity.getUniqueId());if(queue!=null)persist(queue);}}
    private static final class Entry{final ItemStack prototype;int count;Entry(ItemStack prototype,int count){this.prototype=prototype;this.count=count;}}
    private record Queue(ItemDisplay marker,ArrayList<Entry> entries){}
}
