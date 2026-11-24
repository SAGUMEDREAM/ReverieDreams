package cc.thonly.reverie_dreams.paper.specialweapons;

import cc.thonly.reverie_dreams.paper.util.Ids;

import cc.thonly.reverie_dreams.paper.ReverieDreamsPlugin;
import cc.thonly.reverie_dreams.paper.danmaku.DanmakuModule;
import cc.thonly.reverie_dreams.paper.danmaku.DanmakuRules;
import cc.thonly.reverie_dreams.paper.danmaku.DanmakuService;
import com.google.gson.JsonObject;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.UseCooldown;
import io.papermc.paper.event.player.PlayerStopUsingItemEvent;
import net.kyori.adventure.key.Key;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;
import java.util.stream.StreamSupport;
import org.bukkit.event.HandlerList;

/** Real source magazine guns and deferred ordinary-bullet weapons; source initial delta is NOT post-spawn properties.speed. */
public final class GunsService implements Listener {
    public enum Status { FIRED, RELOADED, DRY, REFUSED, CAST, PASS, COOLDOWN, AIMING }
    public record UseResult(Status status,int emitted,boolean sourceReturnsFail){}
    public record Shot(UUID caster,String weapon,UUID bullet,int serverTick,Location origin,DanmakuService.LaunchSpec spec){}
    public record Burst(UUID id,UUID caster,String weapon,int executedVolleys,int remainingVolleys,int nextTick,UUID capturedWorld){}
    private record Frame(Location eye,Location feet){}
    private record InteractionMark(UUID target,EquipmentSlot hand,int tick){}
    private record SoundLayer(Sound sound,float volume,float pitch){}
    private static final class Pending {
        final UUID id=UUID.randomUUID();final LivingEntity owner;final LivingEntity target;final World world;final String weapon;final float pitch,yaw;final RandomGenerator random;
        int next,executed,remaining;Frame deadFrame;
        Pending(LivingEntity owner,LivingEntity target,String weapon,int delay,int remaining,RandomGenerator random){this.owner=owner;this.target=target;world=owner.getWorld();this.weapon=weapon;pitch=owner.getPitch();yaw=owner.getYaw();next=Bukkit.getCurrentTick()+delay;this.remaining=remaining;this.random=random;}
    }
    private static final int AIM_HOLD_TICKS = 6;
    private static final int AIM_EFFECT_TICKS = 4;
    private static final int MAX_RECOIL_REDUCTION_LEVEL = 32;
    private static final float MIN_RECOIL_MULTIPLIER = .02f;
    private static final float NAUSEA_RECOIL_MULTIPLIER = 1.3f;
    private static final String GROUP_PREFIX = "lunar_weapon/";
    private static final org.bukkit.NamespacedKey TRIGGER = new org.bukkit.NamespacedKey(Ids.MOD_ID,"lunar_trigger_held");
    private static final java.util.List<SoundLayer> RIFLE_SOUNDS=List.of(new SoundLayer(Sound.ENTITY_FIREWORK_ROCKET_BLAST,.72f,1.65f),new SoundLayer(Sound.ITEM_CROSSBOW_SHOOT,.38f,1.45f));
    private static final java.util.List<SoundLayer> PISTOL_SOUNDS=List.of(new SoundLayer(Sound.ENTITY_FIREWORK_ROCKET_BLAST,.50f,1.95f),new SoundLayer(Sound.ITEM_CROSSBOW_SHOOT,.25f,1.7f));
    private static final java.util.List<SoundLayer> SHOTGUN_SOUNDS=List.of(new SoundLayer(Sound.ENTITY_GENERIC_EXPLODE,.75f,1.6f),new SoundLayer(Sound.ITEM_CROSSBOW_SHOOT,.5f,.75f));
    private static final java.util.List<SoundLayer> SMG_SOUNDS=List.of(new SoundLayer(Sound.BLOCK_NOTE_BLOCK_HAT,.32f,1.15f),new SoundLayer(Sound.ENTITY_FIREWORK_ROCKET_BLAST,.18f,1.9f));
    private static final java.util.List<SoundLayer> SNIPER_SOUNDS=List.of(new SoundLayer(Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST,1.2f,.85f),new SoundLayer(Sound.ITEM_TRIDENT_THROW,.3f,.65f),new SoundLayer(Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR,.4f,.8f));
    private final JavaPlugin plugin;
    private final MagazineService magazines;
    private final String[] colours;
    private final Map<UUID,Pending> pending=new LinkedHashMap<>();
    private final ArrayDeque<Shot> recent=new ArrayDeque<>();
    private final Map<UUID,InteractionMark> npcInteractions=new LinkedHashMap<>();
    private final Map<UUID,Integer> experienceLevels=new LinkedHashMap<>();
    private long totalLaunched;
    private BukkitTask ticker;
    GunsService(JavaPlugin plugin,MagazineService magazines,JsonObject domain){this.plugin=plugin;this.magazines=magazines;colours=StreamSupport.stream(domain.getAsJsonArray("colours").spliterator(),false).map(x->x.getAsString()).toArray(String[]::new);}
    void enable(){Bukkit.getPluginManager().registerEvents(this,plugin);ticker=Bukkit.getScheduler().runTaskTimer(plugin,this::tick,1,1);}
    void disable(){if(ticker!=null)ticker.cancel();pending.clear();recent.clear();npcInteractions.clear();experienceLevels.clear();HandlerList.unregisterAll(this);}
    public List<Burst> bursts(){SpecialWeaponsModule.thread();return pending.values().stream().map(x->new Burst(x.id,x.owner.getUniqueId(),x.weapon,x.executed,x.remaining,x.next,x.world.getUID())).toList();}
    public List<Shot> recentShots(){SpecialWeaponsModule.thread();return List.copyOf(recent);}
    public long launchedCount(){SpecialWeaponsModule.thread();return totalLaunched;}
    private void record(LivingEntity owner,String weapon,UUID bullet,Location origin,DanmakuService.LaunchSpec spec){if(bullet!=null)totalLaunched++;recent.addLast(new Shot(owner.getUniqueId(),weapon,bullet,Bukkit.getCurrentTick(),origin.clone(),spec));while(recent.size()>128)recent.removeFirst();}
    private ItemStack colour(String type,RandomGenerator random){return DanmakuModule.service().rules().create(type,colours[random.nextInt(colours.length)]);}
    private Frame frame(Pending job){return job.deadFrame!=null?job.deadFrame:new Frame(job.owner.getEyeLocation(),job.owner.getLocation());}
    private Location inWorld(Location original,World world){return new Location(world,original.getX(),original.getY(),original.getZ(),original.getYaw(),original.getPitch());}
    private static void empty(LivingEntity entity){entity.getWorld().playSound(entity.getLocation(),Sound.UI_BUTTON_CLICK,SoundCategory.PLAYERS,.5f,1.6f);}
    private static void turn(LivingEntity entity,float yawInput,float pitchInput){var value=WeaponMath.turn(entity.getYaw(),entity.getPitch(),yawInput,pitchInput);entity.setRotation(value.yaw(),value.pitch());}

    /** Source interactLivingEntity path: vanilla clients never enter a using state here, so perform one immediate action. */
    public UseResult useLunar(Player player,EquipmentSlot hand,LunarWeaponType type,boolean entityInteraction){
        SpecialWeaponsModule.thread();
        ItemStack weapon=player.getInventory().getItem(hand);
        if(LunarWeaponType.weapon(weapon)!=type)return new UseResult(Status.PASS,0,false);
        weapon=ensureCooldownGroup(player,hand,weapon,type);
        if(player.hasCooldown(weapon))return new UseResult(Status.COOLDOWN,0,true);
        if(player.isSneaking()){
            boolean loaded=reload(player,hand,type);
            if(!loaded&&magazines.rounds(player.getInventory().getItem(hand))<=0)empty(player);
            return new UseResult(loaded?Status.RELOADED:Status.REFUSED,0,!loaded);
        }
        if(magazines.rounds(weapon)<=0){
            if(reload(player,hand,type))return new UseResult(Status.RELOADED,0,false);
            if(entityInteraction)return new UseResult(Status.DRY,0,true);
            empty(player);
            return new UseResult(Status.DRY,0,true);
        }
        if(entityInteraction){
            boolean fired=fireShot(type,player,hand,false,ThreadLocalRandom.current());
            return new UseResult(fired?Status.FIRED:Status.REFUSED,fired?1:0,!fired);
        }
        if(!fireShot(type,player,hand,false,ThreadLocalRandom.current()))return new UseResult(Status.REFUSED,0,true);
        setTrigger(player,hand,true);
        startUsing(player,hand);
        return new UseResult(Status.FIRED,1,false);
    }

    /** Server half of the source use/onUseTick/releaseUsing flow; the CE consumable supplies the client use state. */
    public boolean fireShot(LunarWeaponType type,LivingEntity owner,EquipmentSlot hand,boolean aimed,RandomGenerator random){
        SpecialWeaponsModule.thread();
        if(owner.isDead()||!owner.isValid())return false;
        if(owner instanceof Player player&&hand!=null){
            if(player.getGameMode()==GameMode.SPECTATOR)return false;
            ItemStack weapon=player.getInventory().getItem(hand);
            if(LunarWeaponType.weapon(weapon)!=type)return false;
            weapon=ensureCooldownGroup(player,hand,weapon,type);
            if(player.hasCooldown(weapon))return false;
            int rounds=magazines.rounds(weapon);
            if(rounds<=0){empty(player);return false;}
            var updated=weapon.clone();
            magazines.setRounds(updated,rounds-1);
            magazines.refreshTooltip(updated,player.getLevel());
            player.getInventory().setItem(hand,updated);
            player.setCooldown(updated,type.shotInterval());
        }
        ItemStack visual=bulletVisual(type);
        if(visual==null){empty(owner);return false;}
        float damage=type.damageAtLevel(owner instanceof Player player?player.getLevel():0);
        var properties=new DanmakuRules.Properties(Ids.MOD_ID + ":single",1,damage,Ids.MOD_ID + ":danmaku_generic",1f,type.speed(),0,false,false,1,type.bulletHitboxScale());
        var spec=new DanmakuService.LaunchSpec(properties,type.speed(),type.spread(aimed),0,true,type.lifetime(),true);
        Location origin=owner.getEyeLocation();int fired=0;
        for(int pellet=0;pellet<type.pellets();pellet++){
            UUID id=DanmakuModule.service().launchOne(owner,visual,origin,owner.getPitch(),owner.getYaw(),spec);
            if(id!=null){fired++;record(owner,type.itemPath(),id,origin,spec);}
        }
        if(fired==0)return false;
        playLunarSound(owner,type,random);
        float recoilMultiplier=aimed?.5f:1f;
        if(owner instanceof Player player){
            float progress=Math.clamp(player.getLevel(),0,MAX_RECOIL_REDUCTION_LEVEL)/(float)MAX_RECOIL_REDUCTION_LEVEL;
            float experienceMultiplier=progress*(MIN_RECOIL_MULTIPLIER-1f)+1f;
            recoilMultiplier=Math.max(MIN_RECOIL_MULTIPLIER,recoilMultiplier*experienceMultiplier);
            if(player.hasPotionEffect(PotionEffectType.NAUSEA))recoilMultiplier*=NAUSEA_RECOIL_MULTIPLIER;
        }
        float recoilPitch=type.recoil()*recoilMultiplier;
        float recoilYaw=(random.nextFloat()-.5f)*recoilPitch*.5f;
        turn(owner,recoilYaw,-recoilPitch);
        return true;
    }

    private ItemStack bulletVisual(LunarWeaponType type){
        var definition=CraftEngineItems.byId(type.bulletId());
        return definition==null?null:definition.buildBukkitItem();
    }

    private ItemStack ensureCooldownGroup(Player player,EquipmentSlot hand,ItemStack weapon,LunarWeaponType type){
        ItemStack prepared=prepare(weapon);
        if(prepared!=weapon)player.getInventory().setItem(hand,prepared);
        return prepared;
    }

    /** Source inventoryTick lazy group: install a unique cooldown key on first contact with the stack. */
    public ItemStack prepare(ItemStack weapon){
        var current=weapon.getData(DataComponentTypes.USE_COOLDOWN);
        Key group=current==null?null:current.cooldownGroup();
        if(group!=null&&group.value().startsWith(GROUP_PREFIX))return weapon;
        LunarWeaponType type=LunarWeaponType.weapon(weapon);
        if(type==null)return weapon;
        var updated=weapon.clone();
        updated.setData(DataComponentTypes.USE_COOLDOWN,UseCooldown.useCooldown(type.shotInterval()/20f)
                .cooldownGroup(Key.key(Ids.MOD_ID,GROUP_PREFIX+UUID.randomUUID())).build());
        return updated;
    }

    private static void startUsing(Player player,EquipmentSlot hand){if(!player.hasActiveItem())player.startUsingItem(hand);}
    private static boolean hasTrigger(ItemStack item){return item!=null&&!item.isEmpty()&&item.getPersistentDataContainer().has(TRIGGER,PersistentDataType.BYTE);}
    private void setTrigger(Player player,EquipmentSlot hand,boolean enabled){
        ItemStack item=player.getInventory().getItem(hand);
        if(item==null||item.isEmpty())return;
        item.editPersistentDataContainer(data->{if(enabled)data.set(TRIGGER,PersistentDataType.BYTE,(byte)1);else data.remove(TRIGGER);});
        player.getInventory().setItem(hand,item);
    }
    private void playLunarSound(LivingEntity owner,LunarWeaponType type,RandomGenerator random){
        float variation=.98f+random.nextFloat()*.04f;
        for(SoundLayer layer:sounds(type))owner.getWorld().playSound(owner.getLocation(),layer.sound(),SoundCategory.PLAYERS,layer.volume(),Math.min(2,layer.pitch()*variation));
    }
    private static List<SoundLayer> sounds(LunarWeaponType type){return switch(type){case RIFLE->RIFLE_SOUNDS;case PISTOL->PISTOL_SOUNDS;case SHOTGUN->SHOTGUN_SOUNDS;case SMG->SMG_SOUNDS;case SNIPER->SNIPER_SOUNDS;};}

    private boolean reload(Player player,EquipmentSlot hand,LunarWeaponType type){
        ItemStack weapon=player.getInventory().getItem(hand);
        if(LunarWeaponType.weapon(weapon)!=type)return false;
        ensureCooldownGroup(player,hand,weapon,type);
        if(player.hasCooldown(player.getInventory().getItem(hand)))return false;
        int slot=hand==EquipmentSlot.OFF_HAND?40:player.getInventory().getHeldItemSlot();
        var result=magazines.reload(player.getInventory(),slot,40,player.getGameMode()==GameMode.CREATIVE,player.getLevel(),
                stack->player.getWorld().dropItemNaturally(player.getLocation(),stack));
        if(result.status()!=MagazineService.Status.RELOADED)return false;
        player.setCooldown(player.getInventory().getItem(hand),type.reloadTicks());
        player.getWorld().playSound(player.getLocation(),Sound.ITEM_LODESTONE_COMPASS_LOCK,SoundCategory.PLAYERS,1,1.4f);
        return true;
    }

    private static void applyAimSlowness(Player player,LunarWeaponType type){
        PotionEffect current=player.getPotionEffect(PotionEffectType.SLOWNESS);
        int amplifier=type.aimSlowness();
        if(current!=null&&current.getAmplifier()>=amplifier&&(current.isInfinite()||current.getDuration()>=AIM_EFFECT_TICKS))return;
        // Let the effect expire naturally; removing SLOWNESS would also erase potion effects.
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,AIM_EFFECT_TICKS,amplifier,false,false,false));
    }

    @EventHandler(priority=EventPriority.NORMAL)public void stopUsing(PlayerStopUsingItemEvent event){
        Player player=event.getPlayer();
        EquipmentSlot hand=player.hasActiveItem()?player.getActiveItemHand():(LunarWeaponType.weapon(player.getInventory().getItemInMainHand())!=null?EquipmentSlot.HAND:EquipmentSlot.OFF_HAND);
        ItemStack weapon=player.getInventory().getItem(hand);
        LunarWeaponType type=LunarWeaponType.weapon(weapon);
        if(type==null)return;
        if(hasTrigger(weapon)){setTrigger(player,hand,false);return;}
        if(!player.isSneaking())return;
        int held=event.getTicksHeldFor();
        if(held<0)return;
        if(held>=AIM_HOLD_TICKS){fireShot(type,player,hand,true,ThreadLocalRandom.current());return;}
        if(reload(player,hand,type))return;
        if(magazines.rounds(player.getInventory().getItem(hand))<=0)empty(player);
    }

    @EventHandler public void consume(PlayerItemConsumeEvent event){if(LunarWeaponType.weapon(event.getItem())!=null)event.setCancelled(true);}
    @EventHandler public void quit(PlayerQuitEvent event){experienceLevels.remove(event.getPlayer().getUniqueId());npcInteractions.remove(event.getPlayer().getUniqueId());}

    /** Initial trumpet volley is delayed2ticks, followed by2 and2; all three retain cast pitch/yaw. */
    public UUID trumpet(LivingEntity owner,RandomGenerator random){SpecialWeaponsModule.thread();var job=new Pending(owner,null,"trumpet_gun",2,3,random);pending.put(job.id,job);return job.id;}
    /** Sunflower fires now, then15 and30ticks; aim and target center are recomputed every volley. */
    public UUID sunflower(LivingEntity owner,LivingEntity target,RandomGenerator random){SpecialWeaponsModule.thread();target=target==null?null:canonical(target);var job=new Pending(owner,target,"sunflower",15,2,random);emitSunflower(job);job.executed=1;pending.put(job.id,job);return job.id;}
    public UUID trumpet(LivingEntity owner){return trumpet(owner,ThreadLocalRandom.current());}
    public UUID sunflower(LivingEntity owner,LivingEntity target){return sunflower(owner,target,ThreadLocalRandom.current());}
    private void tick(){int now=Bukkit.getCurrentTick();npcInteractions.values().removeIf(mark->now-mark.tick>2);for(var job:List.copyOf(pending.values()))if(now>=job.next){if(job.weapon.equals("trumpet_gun"))emitTrumpet(job);else emitSunflower(job);job.executed++;job.remaining--;if(job.remaining<=0)pending.remove(job.id);else job.next=now+(job.weapon.equals("trumpet_gun")?2:15);}tickLunar(now);}
    private void tickLunar(int now){
        for(Player player:Bukkit.getOnlinePlayers()){
            if(player.hasActiveItem()){
                EquipmentSlot hand=player.getActiveItemHand();ItemStack active=player.getInventory().getItem(hand);
                LunarWeaponType type=LunarWeaponType.weapon(active);
                if(type!=null){
                    if(hasTrigger(active)){
                        if(player.isSneaking()||magazines.rounds(active)<=0){setTrigger(player,hand,false);player.clearActiveItem();}
                        else if(type.automatic())fireShot(type,player,hand,false,ThreadLocalRandom.current());
                    }else if(!player.isSneaking())player.clearActiveItem();
                    else if(player.getActiveItemUsedTime()>=AIM_HOLD_TICKS&&active!=null&&!active.isEmpty())applyAimSlowness(player,type);
                }
            }
            Integer last=experienceLevels.get(player.getUniqueId());
            if(last==null||last!=player.getLevel()||now%20==0){experienceLevels.put(player.getUniqueId(),player.getLevel());refreshHeld(player,EquipmentSlot.HAND);refreshHeld(player,EquipmentSlot.OFF_HAND);}
        }
    }
    private void refreshHeld(Player player,EquipmentSlot hand){ItemStack item=player.getInventory().getItem(hand);if(LunarWeaponType.weapon(item)!=null&&magazines.refreshTooltip(item,player.getLevel()))player.getInventory().setItem(hand,item);}
    private void emitTrumpet(Pending job){var frame=frame(job);var item=colour("bullet",job.random);var rules=DanmakuModule.service().rules();var base=rules.properties(item);var properties=base.withSpeed(2.3f,base.speedVersion());var spec=new DanmakuService.LaunchSpec(properties,base.actualSpeed(),0,1.5f,false);var origin=inWorld(frame.eye,job.world);var id=DanmakuModule.service().launchOne(job.owner,item,origin,job.pitch,job.yaw,spec);record(job.owner,job.weapon,id,origin,spec);job.world.playSound(inWorld(frame.feet,job.world), Ids.MOD_ID + ":fire",SoundCategory.HOSTILE,1,1);}
    private void emitSunflower(Pending job){var frame=frame(job);var target=job.target;boolean aimed=target!=null;
        if(aimed){if(target.isDead()||!target.isValid()||!target.getWorld().equals(job.world))return;var t=target.getLocation();double dx=t.getX()-frame.feet.getX(),dy=t.getY()-frame.feet.getY(),dz=t.getZ()-frame.feet.getZ();if(dx*dx+dy*dy+dz*dz>1024)return;}
        float pitch=frame.eye.getPitch(),yaw=frame.eye.getYaw();if(aimed){var targetPos=target.getLocation();double dx=targetPos.getX()-frame.eye.getX(),dy=targetPos.getY()+target.getHeight()*.5-frame.eye.getY(),dz=targetPos.getZ()-frame.eye.getZ();pitch=(float)-Math.toDegrees(Math.atan2(dy,Math.sqrt(dx*dx+dz*dz)));yaw=(float)Math.toDegrees(Math.atan2(dz,dx))-90;}
        for(float offset:WeaponMath.fan(aimed)){var item=colour("bubble",job.random);var base=DanmakuModule.service().rules().properties(item);var properties=new DanmakuRules.Properties(base.templateId(),base.count(),3.5f,base.damageType(),2.5f,1.8f,base.acceleration(),base.tile(),base.infinite(),base.speedVersion(),2.5f);var spec=new DanmakuService.LaunchSpec(properties,base.actualSpeed(),1,0,false);var origin=inWorld(frame.eye,job.world);var id=DanmakuModule.service().launchOne(job.owner,item,origin,pitch,yaw+offset,spec);record(job.owner,job.weapon,id,origin,spec);}
        // DanmakuShooter.soundDefault uses the shooter's CURRENT world, independent of captured spawn world.
        job.owner.getWorld().playSound(job.owner.getLocation(),Ids.MOD_ID + ":fire",SoundCategory.NEUTRAL,1,1);
    }
    @EventHandler public void death(PlayerDeathEvent event){for(var job:pending.values())if(job.owner.getUniqueId().equals(event.getEntity().getUniqueId())&&job.deadFrame==null)job.deadFrame=new Frame(event.getEntity().getEyeLocation(),event.getEntity().getLocation());}
    /** Owned-NPC entity events are normally cancelled at HIGHEST. Resolve the two source weapon overrides first. */
    @EventHandler(priority=EventPriority.NORMAL,ignoreCancelled=true)public void npcInteract(PlayerInteractEntityEvent event){
        String held=SpecialWeaponsModule.id(event.getPlayer().getInventory().getItem(event.getHand()));if(LunarWeaponType.weapon(held)==null&&!held.equals(Ids.MOD_ID + ":sunflower"))return;
        var target=ReverieDreamsPlugin.getMod().getNPC().service().canonicalId(event.getRightClicked());if(target.isEmpty()||!(Bukkit.getEntity(target.get()) instanceof LivingEntity nativeTarget))return;
        event.setCancelled(true);var mark=new InteractionMark(target.get(),event.getHand(),Bukkit.getCurrentTick());if(mark.equals(npcInteractions.put(event.getPlayer().getUniqueId(),mark)))return;
        SpecialWeaponsModule.service().use(event.getPlayer(),event.getHand(),nativeTarget);
    }
    @EventHandler(priority=EventPriority.NORMAL,ignoreCancelled=true)public void npcInteractAt(PlayerInteractAtEntityEvent event){npcInteract(event);}
    private static LivingEntity canonical(LivingEntity entity){var id=ReverieDreamsPlugin.getMod().getNPC().service().canonicalId(entity);return id.isPresent()&&Bukkit.getEntity(id.get()) instanceof LivingEntity nativeEntity?nativeEntity:entity;}
}
