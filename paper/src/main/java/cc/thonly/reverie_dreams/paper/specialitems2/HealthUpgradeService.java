package cc.thonly.reverie_dreams.paper.specialitems2;

import cc.thonly.reverie_dreams.paper.util.Ids;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.text.Component;

/** Source auxiliary float is separate from the native persistent MAX_HEALTH base; never adds it twice on login. */
public final class HealthUpgradeService implements Listener {
    public static final NamespacedKey KEY=new NamespacedKey("reverie_dreams","max_health_modifier");
    public record Snapshot(float modifier,double base,double effective,double maximum){}
    private final JavaPlugin plugin;
    private final double maximum;
    HealthUpgradeService(JavaPlugin plugin,double maximum){this.plugin=plugin;this.maximum=maximum;}
    public Snapshot snapshot(Player player){SpecialItemsExpansion.thread();var attr=player.getAttribute(Attribute.MAX_HEALTH);return new Snapshot(player.getPersistentDataContainer().getOrDefault(KEY,PersistentDataType.FLOAT,0f),attr==null?0:attr.getBaseValue(),attr==null?0:attr.getValue(),maximum);}
    public boolean use(Player player,EquipmentSlot hand){SpecialItemsExpansion.thread();var stack=player.getInventory().getItem(hand);if(!SpecialItemsExpansion.id(stack).equals(Ids.MOD_ID + ":upgraded_health"))return false;
        var attribute=player.getAttribute(Attribute.MAX_HEALTH);if(attribute!=null&&attribute.getValue()+2>maximum){player.sendActionBar(Component.translatable("item.action.click.upgraded_health.max_full",Component.text((int)maximum)));return false;}
        float next=snapshot(player).modifier+2;if(!Float.isFinite(next))throw new IllegalStateException("Invalid stored health modifier; unchanged");
        // Source sets base to the CURRENT EFFECTIVE value plus2, including other attribute modifiers.
        if(attribute!=null)attribute.setBaseValue(attribute.getValue()+2);player.getPersistentDataContainer().set(KEY,PersistentDataType.FLOAT,next);
        player.getWorld().playSound(player.getEyeLocation(),Ids.MOD_ID + ":up",SoundCategory.PLAYERS,1,1);
        if(player.getGameMode()!=GameMode.CREATIVE){var remainder=stack.clone();remainder.subtract();player.getInventory().setItem(hand,remainder.isEmpty()?null:remainder);}return true;
    }
    /** Source die HEAD changes only the auxiliary modifier, not MAX_HEALTH itself. */
    public void recordDeath(Player player){SpecialItemsExpansion.thread();float value=snapshot(player).modifier;if(value>=1)value--;player.getPersistentDataContainer().set(KEY,PersistentDataType.FLOAT,Math.max(0,value));}
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)public void damage(EntityDamageEvent event){if(!(event.getEntity() instanceof Player player)||!player.getPersistentDataContainer().has(KEY,PersistentDataType.FLOAT))return;
        // Source LivingEntityMixin.damage HEAD tests raw incoming damage before armour/absorption; not final damage.
        var attribute=player.getAttribute(Attribute.MAX_HEALTH);if(attribute!=null&&player.getHealth()-event.getDamage()<=0&&attribute.getValue()>20)attribute.setBaseValue(Math.abs(attribute.getValue()-2));
    }
    @EventHandler(priority=EventPriority.MONITOR)public void death(PlayerDeathEvent event){recordDeath(event.getEntity());}
    @EventHandler public void join(PlayerJoinEvent event){var p=event.getPlayer();float value=snapshot(p).modifier;if(!Float.isFinite(value))plugin.getSLF4JLogger().error("Non-finite health modifier for {} preserved without activation",p.getUniqueId());}
    @EventHandler public void respawn(PlayerRespawnEvent event){var player=event.getPlayer();
        // The source has no restoreFrom copy for its unique field. A new ServerPlayer starts at0;
        // native restoreFrom assigns old attribute BASE values independently (Paper74 bytecode audited).
        player.getPersistentDataContainer().set(KEY,PersistentDataType.FLOAT,0f);
    }
    void tick(){for(var player:Bukkit.getOnlinePlayers()){if(Bukkit.getServer().getServerTickManager().isFrozen(player))continue;var state=snapshot(player);if(Float.isFinite(state.modifier)&&state.modifier>maximum){var attribute=player.getAttribute(Attribute.MAX_HEALTH);if(attribute!=null){player.getPersistentDataContainer().set(KEY,PersistentDataType.FLOAT,state.modifier-1);attribute.setBaseValue(attribute.getValue()-1);}}}}
}
