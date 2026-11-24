package cc.thonly.reverie_dreams.paper.specialitems2;

import cc.thonly.reverie_dreams.paper.util.Ids;

import cc.thonly.reverie_dreams.paper.ReverieDreamsPlugin;
import cc.thonly.reverie_dreams.paper.danmaku.DanmakuModule;
import cc.thonly.reverie_dreams.paper.danmaku.DanmakuService;
import cc.thonly.reverie_dreams.paper.storage.StorageDropService;
import net.momirealms.craftengine.bukkit.api.CraftEngineFurniture;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.random.RandomGenerator;
import java.util.Objects;
import java.util.Set;

/** Source Bomb clears hostile ordinary BaseDanmaku equivalents and rewards ALL other logical source entities. No damage/explosion. */
public final class BombService {
    public record Target(UUID id,Location position,boolean hostileBullet){}
    public record Result(int cancelledBullets,int otherEntities,int pointItems){}
    private final Set<UUID> inFlight=new HashSet<>();
    public List<Target> targets(Entity player){SpecialItemsExpansion.thread();var range=player.getBoundingBox().expand(20);var result=new LinkedHashMap<UUID,Target>();var bulletIds=new HashSet<UUID>();
        for(var bullet:DanmakuModule.service().snapshots()){bulletIds.add(bullet.id());if(!bullet.position().getWorld().equals(player.getWorld()))continue;var p=bullet.position();double radius=.25*bullet.properties().collisionScale();var bounds=new BoundingBox(p.getX()-radius,p.getY(),p.getZ()-radius,p.getX()+radius,p.getY()+radius*2,p.getZ()+radius);if(!range.overlaps(bounds))continue;
            result.put(bullet.id(),new Target(bullet.id(),p.clone(),!player.getUniqueId().equals(bullet.owner())));
        }
        for(Entity candidate:player.getWorld().getNearbyEntities(range)){if(candidate.getUniqueId().equals(player.getUniqueId())||bulletIds.contains(candidate.getUniqueId())||helper(candidate))continue;
            var canonical=ReverieDreamsPlugin.getMod().getNPC().service().canonicalId(candidate);Entity logical=canonical.isPresent()?Bukkit.getEntity(canonical.get()):candidate;if(logical==null||!logical.isValid())continue;UUID id=logical.getUniqueId();result.putIfAbsent(id,new Target(id,logical.getLocation(),false));
        }return List.copyOf(result.values());
    }
    public static boolean helper(Entity entity){return entity.getPersistentDataContainer().has(StorageDropService.KEY)||entity.getPersistentDataContainer().has(TreasureRodService.MARKER)||CraftEngineFurniture.isFurniture(entity)||CraftEngineFurniture.isCollisionEntity(entity)||CraftEngineFurniture.isSeat(entity);}
    public Result use(Player player,EquipmentSlot hand,RandomGenerator random){SpecialItemsExpansion.thread();ItemStack stack=player.getInventory().getItem(hand);if(!SpecialItemsExpansion.id(stack).equals(Ids.MOD_ID + ":bomb")||player.hasCooldown(stack)||!inFlight.add(player.getUniqueId()))return new Result(0,0,0);
        try{
            player.getWorld().playSound(player.getEyeLocation(),Ids.MOD_ID + ":spell_card",SoundCategory.PLAYERS,1,1);
            var result=applyEffect(player,random);
            if(player.getGameMode()!=GameMode.CREATIVE){var remainder=stack.clone();remainder.subtract();player.getInventory().setItem(hand,remainder.isEmpty()?null:remainder);}return result;
        }finally{inFlight.remove(player.getUniqueId());}
    }
    /** Same production effect for native actors; callers own item debit and sound, as the player use wrapper does. */
    public Result apply(Entity actor,RandomGenerator random){SpecialItemsExpansion.thread();if(!inFlight.add(actor.getUniqueId()))return new Result(0,0,0);try{return applyEffect(actor,random);}finally{inFlight.remove(actor.getUniqueId());}}
    private Result applyEffect(Entity player,RandomGenerator random){var point=Objects.requireNonNull(CraftEngineItems.byId(Ids.MOD_ID + ":point"),"Missing real point item").buildBukkitItem().asOne();var targets=targets(player);int cancelled=0,others=0,points=0;
        for(var target:targets)if(target.hostileBullet&&DanmakuModule.service().cancel(target.id)){cancelled++;int count=random.nextInt(1,5);for(int i=0;i<count;i++)drop(target.position,point);points+=count;}
        for(var target:targets)if(!target.hostileBullet){drop(player.getLocation(),point);points++;others++;}return new Result(cancelled,others,points);
    }
    private static void drop(Location where,ItemStack point){where.getWorld().dropItem(where,point.clone());}
}
