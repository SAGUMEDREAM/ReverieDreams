package cc.thonly.reverie_dreams.paper.specialitems;

import cc.thonly.reverie_dreams.paper.util.Ids;

import cc.thonly.reverie_dreams.paper.ReverieDreamsPlugin;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviors;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.context.InteractEntityContext;
import net.momirealms.craftengine.core.world.context.UseOnContext;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.persistence.PersistentDataType;

/** Twelve real source abilities. Other audited identities remain explicitly unregistered until their behavior is ported. */
public final class SpecialItemsModule implements Listener {
    public enum Status { SUCCESS, PASS, COOLDOWN, NO_TARGET, NO_PERMISSION, ALREADY_FROZEN, TELEPORT_DENIED }
    public record Result(Status status,int affected,UUID spawnedEntity) {}
    public record ClockState(UUID owner,int remainingTicks,boolean serverFrozen) {}
    private static SpecialItemsModule instance;
    private static boolean registered;
    private final JavaPlugin plugin;
    private final JsonObject domain;
    private final SpecialItemRepairBridge repairs;
    private final NamespacedKey maxDistance=new NamespacedKey("reverie_dreams","max_distance");
    private boolean allowMultiplayerFreeze=true;
    private UUID clockOwner;
    private int clockDeadline;
    private BukkitTask clockRelease;
    private SpecialItemsModule(JavaPlugin plugin){this.plugin=plugin;
        try(var reader=new InputStreamReader(Objects.requireNonNull(plugin.getResource("migration/specialitems-domain.json")),StandardCharsets.UTF_8)){
            domain=JsonParser.parseReader(reader).getAsJsonObject();if(domain.get("schemaVersion").getAsInt()!=1)throw new IllegalArgumentException("Unsupported special items domain");
        }catch(IOException failure){throw new IllegalStateException("Cannot read special item rules",failure);}
        repairs=new SpecialItemRepairBridge();
        if(!new File(plugin.getDataFolder(),"specialitems.yml").isFile())plugin.saveResource("specialitems.yml",false);
        var configuration=YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(),"specialitems.yml"));
        allowMultiplayerFreeze=configuration.getBoolean("enable-time-stopping-multiplayer",true);
    }
    public static void registerTypes(){if(registered)return;ItemBehaviors.register(Key.of(Ids.MOD_ID + ":special_item"),(pack,path,id,section)->new Behavior());registered=true;}
    public static void enable(JavaPlugin plugin){if(instance!=null)return;instance=new SpecialItemsModule(plugin);Bukkit.getPluginManager().registerEvents(instance,plugin);Bukkit.getPluginManager().registerEvents(instance.repairs,plugin);}
    public static void disable(){if(instance==null)return;instance.releaseClock();HandlerList.unregisterAll(instance);HandlerList.unregisterAll(instance.repairs);instance=null;}
    public static SpecialItemsModule service(){if(instance==null)throw new IllegalStateException("Special items not enabled");return instance;}
    public JsonObject domain(){return domain.deepCopy();}
    public ItemStack create(String id){thread();if(!id.contains(":"))id=Ids.MOD_ID + ":"+id;if(!domain.getAsJsonObject("implemented").has(id))return null;var item=CraftEngineItems.byId(id);return item==null?null:item.buildBukkitItem();}
    public static String id(ItemStack stack){if(stack==null||stack.isEmpty())return "minecraft:air";var key=CraftEngineItems.getCustomItemId(stack);return key==null?stack.getType().getKey().asString():key.asString();}
    private boolean supported(ItemStack item){return domain.getAsJsonObject("implemented").has(id(item));}
    public ClockState clockState(){thread();return new ClockState(clockOwner,clockOwner==null?0:Math.max(0,clockDeadline-Bukkit.getCurrentTick()),Bukkit.getServer().getServerTickManager().isFrozen());}
    public SpecialItemRepairBridge.Preview previewRepair(ItemStack item,ItemStack ingredient){thread();return SpecialItemRepairBridge.preview(item,ingredient);}
    public void releaseClock(){thread();if(clockRelease!=null){clockRelease.cancel();clockRelease=null;}if(clockOwner!=null){clockOwner=null;Bukkit.getServer().getServerTickManager().setFrozen(false);}}
    private Result clock(Player player,EquipmentSlot hand,ItemStack stack){
        var ticks=Bukkit.getServer().getServerTickManager();
        if(ticks.isFrozen()){
            if(player.getUniqueId().equals(clockOwner)){releaseClock();return result(Status.SUCCESS,0);}
            player.sendActionBar(Component.translatable("message.reverie_dreams.time_stop.already_frozen"));return result(Status.ALREADY_FROZEN,0);
        }
        if(!allowMultiplayerFreeze&&!player.isOp()&&!player.hasPermission("minecraft.command.gamemode")){
            player.sendActionBar(Component.translatable("message.reverie_dreams.time_stop.no_permission"));return result(Status.NO_PERMISSION,0);
        }
        if(player.hasCooldown(stack))return result(Status.COOLDOWN,0);
        player.setCooldown(stack,600);clockOwner=player.getUniqueId();clockDeadline=Bukkit.getCurrentTick()+100;ticks.setFrozen(true);
        player.getWorld().playSound(player.getLocation(),Sound.BLOCK_ENCHANTMENT_TABLE_USE,SoundCategory.BLOCKS,.5f,1);
        SpecialItemDurability.withoutBreaking(player,hand,1);if(clockRelease!=null)clockRelease.cancel();clockRelease=Bukkit.getScheduler().runTaskLater(plugin,this::releaseClock,100);
        return result(Status.SUCCESS,0);
    }
    public Result use(Player player,EquipmentSlot hand){
        thread();ItemStack stack=player.getInventory().getItem(hand);String id=id(stack);if(!supported(stack))return result(Status.PASS,0);
        if(id.equals(Ids.MOD_ID + ":time_stop_clock"))return clock(player,hand,stack);
        if(player.hasCooldown(stack))return result(Status.COOLDOWN,0);
        return switch(id){
            case Ids.MOD_ID + ":yin-yang_orb"->{player.addPotionEffect(effect(PotionEffectType.RESISTANCE,200,3));player.setCooldown(stack,800);player.getWorld().playSound(player.getEyeLocation(),Ids.MOD_ID + ":spell_card",SoundCategory.PLAYERS,1,1);yield result(Status.SUCCESS,1);}
            case Ids.MOD_ID + ":papilio_pattern_fan"->{Vector look=player.getEyeLocation().getDirection();player.setVelocity(player.getVelocity().add(new Vector(look.getX()*1.4,.35,look.getZ()*1.4)));player.setFallDistance(0);player.setCooldown(stack,35);player.getWorld().playSound(player.getLocation(),Sound.ENTITY_WIND_CHARGE_WIND_BURST,SoundCategory.PLAYERS,1,1);yield result(Status.SUCCESS,1);}
            case Ids.MOD_ID + ":maple_leaf_fan"->{WindCharge charge=wind(player);player.setCooldown(stack,10);player.damageItemStack(hand,1);yield new Result(Status.SUCCESS,1,charge.getUniqueId());}
            case Ids.MOD_ID + ":hakuroken",Ids.MOD_ID + ":rokanken"->{int affected=illuminateUndead(player);if(affected>0)SpecialItemDurability.withoutBreaking(player,hand,1);
                if(id.endsWith(":rokanken")){WindCharge charge=wind(player);player.setCooldown(stack,100);yield new Result(Status.SUCCESS,affected,charge.getUniqueId());}
                yield result(affected>0?Status.SUCCESS:Status.NO_TARGET,affected);}
            default->result(Status.PASS,0);
        };
    }
    private WindCharge wind(Player player){
        // Paper74's actual implementation invokes native shootFromRotation(owner,pitch,yaw,0,1.5,1).
        WindCharge projectile=player.launchProjectile(WindCharge.class);
        player.getWorld().playSound(player.getEyeLocation(),Sound.ENTITY_WIND_CHARGE_THROW,SoundCategory.NEUTRAL,.5f,.4f/(ThreadLocalRandom.current().nextFloat()*.4f+.8f));return projectile;
    }
    private int illuminateUndead(Player player){int x=player.getLocation().getBlockX(),y=player.getLocation().getBlockY(),z=player.getLocation().getBlockZ(),count=0;
        for(var target:player.getWorld().getNearbyEntities(new BoundingBox(x-16,y-16,z-16,x+16,y+16,z+16)))if(target instanceof LivingEntity living&&target!=player&&Tag.ENTITY_TYPES_UNDEAD.isTagged(target.getType())){living.addPotionEffect(effect(PotionEffectType.GLOWING,300,0));count++;}
        if(count>0)player.getWorld().playSound(player.getEyeLocation(),Sound.ENTITY_PLAYER_ATTACK_SWEEP,SoundCategory.PLAYERS,1,1);return count;
    }
    public Result useOnBlock(Player player,EquipmentSlot hand,Block block,BlockFace face){
        thread();ItemStack item=player.getInventory().getItem(hand);
        if(!id(item).equals(Ids.MOD_ID + ":crossing_chisel"))return result(Status.PASS,0);
        if(player.hasCooldown(item))return result(Status.COOLDOWN,0);
        int distance=item.getPersistentDataContainer().getOrDefault(maxDistance,PersistentDataType.INTEGER,16);
        BlockFace direction=face.getOppositeFace();if(Math.abs(direction.getModX())+Math.abs(direction.getModY())+Math.abs(direction.getModZ())!=1)return result(Status.PASS,0);
        var world=block.getWorld();Location target=null;
        for(int i=0;i<distance+1;i++){
            int x=block.getX()+direction.getModX()*i,y=block.getY()+direction.getModY()*i,z=block.getZ()+direction.getModZ()*i;
            if(!world.isChunkLoaded(x>>4,z>>4))break;
            if(y>=world.getMinHeight()&&y<world.getMaxHeight()){
                var current=world.getBlockAt(x,y,z);if(current.getType()==Material.BEDROCK||current.getType()==Material.OBSIDIAN||current.getType().getHardness()<0)continue;
            }
            if(!air(world,x,y,z))continue;if(direction==BlockFace.DOWN&&!air(world,x,y-1,z))continue;
            int feet=direction==BlockFace.UP?y:y-1;target=new Location(world,x+.5,feet,z+.5,player.getYaw(),player.getPitch());break;
        }
        // Source checks the head space only after accepting the FIRST candidate, rather than searching a later gap.
        if(target==null||!air(world,target.getBlockX(),target.getBlockY()+1,target.getBlockZ()))return result(Status.NO_TARGET,0);
        SpecialItemDurability.withoutBreaking(player,hand,1);player.setCooldown(item,60);player.swingHand(hand);
        world.playSound(target,Sound.ITEM_CHORUS_FRUIT_TELEPORT,SoundCategory.PLAYERS,1,1);
        if(target.getY()<world.getMinHeight())return result(Status.SUCCESS,0);
        return result(player.teleport(target,PlayerTeleportEvent.TeleportCause.PLUGIN)?Status.SUCCESS:Status.TELEPORT_DENIED,1);
    }
    private static boolean air(World world,int x,int y,int z){return y<world.getMinHeight()||y>=world.getMaxHeight()||world.getBlockAt(x,y,z).getType().isAir();}
    public Result interact(Player player,EquipmentSlot hand,LivingEntity original){
        thread();ItemStack item=player.getInventory().getItem(hand);if(!id(item).equals(Ids.MOD_ID + ":satori_eye"))return result(Status.PASS,0);
        LivingEntity target=canonical(original);if(target==player)return result(Status.PASS,0);
        double max=target.getAttribute(Attribute.MAX_HEALTH)==null?target.getHealth():target.getAttribute(Attribute.MAX_HEALTH).getValue();
        var message=Component.empty().append(target.name().color(NamedTextColor.WHITE)).append(Component.text(String.format(Locale.ROOT," HP: %.1f/%.1f",target.getHealth(),max),NamedTextColor.RED));
        int armor=(int)(target.getAttribute(Attribute.ARMOR)==null?0:target.getAttribute(Attribute.ARMOR).getValue());message=message.append(Component.text(" | Armor: "+armor,NamedTextColor.BLUE));
        if(target instanceof Mob mob&&mob.getTarget()!=null)message=message.append(Component.text(" | Target → ",NamedTextColor.GOLD)).append(mob.getTarget().name().color(NamedTextColor.YELLOW));
        var effects=new ArrayList<>(target.getActivePotionEffects());if(!effects.isEmpty()){message=message.append(Component.text(" | Effects: ",NamedTextColor.LIGHT_PURPLE));for(int i=0;i<Math.min(3,effects.size());i++){if(i>0)message=message.append(Component.text(", ",NamedTextColor.GRAY));message=message.append(Component.translatable(effects.get(i).getType().translationKey()).color(NamedTextColor.LIGHT_PURPLE));}if(effects.size()>3)message=message.append(Component.text("...",NamedTextColor.DARK_GRAY));}
        player.sendActionBar(message);target.swingHand(hand);return result(Status.SUCCESS,1);
    }
    static LivingEntity canonical(LivingEntity entity){try{var id=ReverieDreamsPlugin.getMod().getNPC().service().canonicalId(entity);if(id.isPresent()&&Bukkit.getEntity(id.get()) instanceof LivingEntity carrier)return carrier;}catch(IllegalStateException unavailable){/* Shutdown or independent API use. */}return entity;}
    static boolean npc(LivingEntity entity){try{return ReverieDreamsPlugin.getMod().getNPC().service().canonicalId(entity).isPresent();}catch(IllegalStateException unavailable){return false;}}
    /** Source post-hit implementation; RNG injection is the same production algorithm, useful to other combat consumers. */
    public boolean applyMelee(LivingEntity attacker,LivingEntity target,ItemStack weapon,RandomGenerator random){
        thread();String id=id(weapon);
        switch(id){
            case Ids.MOD_ID + ":levatin"->{if(target instanceof Player||npc(target)||random.nextFloat()>=.15f)return false;var slots=EquipmentSlot.values();var slot=slots[random.nextInt(slots.length)];var equipment=target.getEquipment();if(equipment==null)return false;var item=equipment.getItem(slot);int maximum=item.getDataOrDefault(DataComponentTypes.MAX_DAMAGE,0);if(maximum<=0)return false;target.damageItemStack(slot,Math.max(1,maximum/4));target.getWorld().playSound(target.getLocation(),Sound.ENTITY_ITEM_BREAK,SoundCategory.PLAYERS,1,1);return true;}
            case Ids.MOD_ID + ":death_scythe"->{if(random.nextFloat()>=.19f)return false;var attribute=attacker.getAttribute(Attribute.MAX_HEALTH);if(attribute==null)return false;attacker.setHealth(Math.min(attribute.getValue(),attacker.getHealth()+1.5));target.getWorld().playSound(target.getLocation(),Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN,SoundCategory.PLAYERS,1,1);return true;}
            case Ids.MOD_ID + ":rokanken"->{attacker.addPotionEffect(effect(PotionEffectType.SATURATION,1,0));var look=attacker.getEyeLocation().getDirection();attacker.setVelocity(attacker.getVelocity().add(new Vector(look.getX()*.34,.05,look.getZ()*.34)));attacker.setFallDistance(0);target.getWorld().playSound(target.getLocation(),Sound.ENTITY_PLAYER_ATTACK_SWEEP,SoundCategory.NEUTRAL,1,1);return true;}
            case Ids.MOD_ID + ":iron_bar",Ids.MOD_ID + ":oni_iron_bar"->{target.knockback(id.endsWith(":oni_iron_bar")?1.4:.8,attacker.getX()-target.getX(),attacker.getZ()-target.getZ());Sound sound=random.nextInt(2)==0?Sound.ITEM_MACE_SMASH_GROUND_HEAVY:Sound.ITEM_MACE_SMASH_GROUND;target.getWorld().playSound(target.getLocation(),sound,SoundCategory.NEUTRAL,1,1);return true;}
            default->{return false;}
        }
    }
    @EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)public void melee(EntityDamageByEntityEvent event){
        if(event.getCause()!=EntityDamageEvent.DamageCause.ENTITY_ATTACK||!(event.getDamager() instanceof LivingEntity attacker)||!(event.getEntity() instanceof LivingEntity target)||canonical(target)!=target)return;
        if(event.getFinalDamage()<=0||attacker.getEquipment()==null)return;applyMelee(attacker,target,attacker.getEquipment().getItemInMainHand(),ThreadLocalRandom.current());
    }
    @EventHandler public void quit(PlayerQuitEvent event){if(event.getPlayer().getUniqueId().equals(clockOwner))releaseClock();}
    private static PotionEffect effect(PotionEffectType type,int ticks,int amplifier){return new PotionEffect(type,ticks,amplifier,false,true,true);}
    private static Result result(Status status,int affected){return new Result(status,affected,null);}
    private static void thread(){if(!Bukkit.isPrimaryThread())throw new IllegalStateException("Special item effects require the Paper main thread");}
    private static EquipmentSlot slot(InteractionHand hand){return hand==InteractionHand.MAIN_HAND?EquipmentSlot.HAND:EquipmentSlot.OFF_HAND;}
    private static InteractionResult adapt(Result result){return result.status==Status.PASS||result.status==Status.NO_TARGET?InteractionResult.PASS:result.status==Status.SUCCESS?InteractionResult.SUCCESS_AND_CANCEL:InteractionResult.FAIL;}
    private static final class Behavior extends ItemBehavior{
        public InteractionResult use(net.momirealms.craftengine.core.world.World world,net.momirealms.craftengine.core.entity.player.Player player,InteractionHand hand){if(instance==null||player==null)return InteractionResult.PASS;return adapt(instance.use((Player)player.platformPlayer(),slot(hand)));}
        public InteractionResult useOnBlock(UseOnContext context){if(instance==null||context.getPlayer()==null)return InteractionResult.PASS;var p=context.getClickedPos();var block=((World)context.getLevel().platformWorld()).getBlockAt(p.x(),p.y(),p.z());var face=context.getClickedFace();return adapt(instance.useOnBlock((Player)context.getPlayer().platformPlayer(),slot(context.getHand()),block,BlockFace.valueOf(face.name())));}
        public InteractionResult useOnEntity(InteractEntityContext context){if(instance==null||context.getPlayer()==null||context.getTarget()==null)return InteractionResult.PASS;var target=context.getTarget().platformEntity();if(!(target instanceof LivingEntity living))return InteractionResult.PASS;return adapt(instance.interact((Player)context.getPlayer().platformPlayer(),slot(context.getHand()),living));}
    }
}
