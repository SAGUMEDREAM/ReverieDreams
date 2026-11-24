package cc.thonly.reverie_dreams.paper.storage;

import cc.thonly.reverie_dreams.paper.util.Ids;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.random.RandomGenerator;

/** Shared stable player-faith API for future abilities; source fields survive logout and player replacement. */
public final class PlayerFaithService implements Listener {
    public static final NamespacedKey KEY=new NamespacedKey("reverie_dreams","faith");
    public record PrayerResult(FaithRules.Prayer prayer,int consumedCoins){}
    private final JavaPlugin plugin;
    private final Map<UUID,FaithRules.State> lastKnown=new HashMap<>();
    PlayerFaithService(JavaPlugin plugin){this.plugin=plugin;}
    public FaithRules.State snapshot(Player player){thread();String raw=player.getPersistentDataContainer().get(KEY,PersistentDataType.STRING);FaithRules.State state=lastKnown.getOrDefault(player.getUniqueId(),FaithRules.State.EMPTY);
        if(raw!=null){var value=JsonParser.parseString(raw).getAsJsonObject();if(value.get("schema").getAsInt()!=1)throw new IllegalStateException("Unknown faith schema; stored data was preserved");state=new FaithRules.State(value.get("FaithValue").getAsInt(),value.get("DateOfLastPrayer").getAsLong());}
        lastKnown.put(player.getUniqueId(),state);return state;
    }
    public boolean setFaithValue(Player player,int value){thread();if(value>FaithRules.MAX_VALUE)return false;write(player,FaithRules.setValue(snapshot(player),value));return true;}
    public void setLastPrayerDay(Player player,long day){thread();var state=snapshot(player);write(player,new FaithRules.State(state.value(),day));}
    private void write(Player player,FaithRules.State state){var value=new JsonObject();value.addProperty("schema",1);value.addProperty("FaithValue",state.value());value.addProperty("DateOfLastPrayer",state.lastPrayerDay());player.getPersistentDataContainer().set(KEY,PersistentDataType.STRING,value.toString());lastKnown.put(player.getUniqueId(),state);}
    public PrayerResult prayMainHand(Player player,World world,RandomGenerator random){
        thread();var item=player.getInventory().getItemInMainHand();int value=coinValue(StorageSpecialModule.id(item));
        var plan=FaithRules.pray(snapshot(player),value,world.getGameTime(),random);int consumed=0;
        if(plan.status()==FaithRules.Status.SUCCESS){
            if(player.getGameMode()!=GameMode.CREATIVE){consumed=Math.min(item.getAmount(),plan.requestedCoins());var remaining=item.clone();remaining.subtract(consumed);player.getInventory().setItemInMainHand(remaining.isEmpty()?null:remaining);}
            write(player,plan.state());player.sendActionBar(Component.translatable("item.action.click.cashbox.success",Component.text(plan.displayedValue())));
        }else if(plan.status()==FaithRules.Status.FULL){write(player,plan.state());player.sendActionBar(Component.translatable("item.action.click.cashbox.fails.full",Component.text(FaithRules.MAX_VALUE)));}
        else if(plan.status()==FaithRules.Status.ALREADY_PRAYED)player.sendActionBar(Component.translatable("item.action.click.cashbox.fails.used"));
        if(plan.status()==FaithRules.Status.SUCCESS||plan.status()==FaithRules.Status.FULL)world.playSound(player.getLocation(),Sound.ENTITY_EXPERIENCE_ORB_PICKUP,SoundCategory.NEUTRAL,1,1);
        return new PrayerResult(plan,consumed);
    }
    public static int coinValue(String id){return switch(id){case Ids.MOD_ID + ":copper_coin"->1;case Ids.MOD_ID + ":silver_coin"->3;case Ids.MOD_ID + ":gold_coin"->8;default->0;};}
    void loadSafely(Player player){try{snapshot(player);}catch(RuntimeException malformed){lastKnown.remove(player.getUniqueId());plugin.getSLF4JLogger().error("Faith data for {} was preserved without activation",player.getUniqueId(),malformed);}}
    @EventHandler public void join(PlayerJoinEvent event){loadSafely(event.getPlayer());}
    @EventHandler public void quit(PlayerQuitEvent event){var player=event.getPlayer();var state=lastKnown.remove(player.getUniqueId());if(state!=null)write(player,state);lastKnown.remove(player.getUniqueId());}
    @EventHandler public void respawn(PlayerRespawnEvent event){var player=event.getPlayer();var state=lastKnown.get(player.getUniqueId());if(state!=null)Bukkit.getScheduler().runTask(plugin,()->{if(player.isOnline()&&lastKnown.get(player.getUniqueId())==state)write(player,state);});}
    static void thread(){if(!Bukkit.isPrimaryThread())throw new IllegalStateException("Faith requires the Paper main thread");}
}
