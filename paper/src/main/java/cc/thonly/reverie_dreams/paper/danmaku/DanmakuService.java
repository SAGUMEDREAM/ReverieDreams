package cc.thonly.reverie_dreams.paper.danmaku;

import cc.thonly.reverie_dreams.paper.util.Ids;

import cc.thonly.reverie_dreams.paper.ReverieDreamsPlugin;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiPredicate;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.block.data.Waterlogged;

/** Real ordinary bullet simulation: one physical hit per logical projectile, no vanilla projectile approximation. */
@SuppressWarnings("UnstableApiUsage")
public final class DanmakuService implements Listener {
    public enum FireStatus { FIRED, COOLDOWN, NOT_DANMAKU, UNSUPPORTED_TRAJECTORY, INVALID_PROPERTIES }
    public record FireResult(FireStatus status,List<UUID> bullets,int cooldownTicks) {}
    public record BulletSnapshot(UUID id,UUID owner,int age,int duration,int bounces,Location position,Vector velocity,DanmakuRules.Properties properties) {}
    public record Impact(UUID bullet,UUID target,boolean block,Location position,double damage) {}
    /** Straight source entity factory: initial velocity is separate from post-spawn component properties. */
    public record LaunchSpec(DanmakuRules.Properties properties,float initialSpeed,float divergence,float forwardOffset,boolean addOwnerMovement,int lifetimeTicks,boolean velocityFacing) {
        public LaunchSpec(DanmakuRules.Properties properties,float initialSpeed,float divergence,float forwardOffset,boolean addOwnerMovement) {
            this(properties, initialSpeed, divergence, forwardOffset, addOwnerMovement, 200, false);
        }
    }
    private static final NamespacedKey STATE=new NamespacedKey("reverie_dreams","danmaku_projectile");
    private static final NamespacedKey ITEM=new NamespacedKey("reverie_dreams","danmaku_projectile_item");
    private final JavaPlugin plugin;
    private final DanmakuRules rules=new DanmakuRules();
    private final Map<UUID,Bullet> bullets=new LinkedHashMap<>();
    private final Map<UUID,Integer> lastGraze=new LinkedHashMap<>();
    private final ArrayDeque<Impact> impacts=new ArrayDeque<>();
    private final DamageType generic,real;
    private BiPredicate<Entity,Entity> friendly=(owner,target)->false;
    private BukkitTask ticker;

    DanmakuService(JavaPlugin plugin) {
        this.plugin=plugin;
        var registry=RegistryAccess.registryAccess().getRegistry(RegistryKey.DAMAGE_TYPE);
        generic=registry.get(NamespacedKey.fromString(Ids.MOD_ID + ":danmaku_generic"));
        real=registry.get(NamespacedKey.fromString(Ids.MOD_ID + ":danmaku_real"));
        if(generic==null||real==null) throw new IllegalStateException("Call DanmakuBootstrap.configure from the plugin bootstrap before enabling danmaku");
    }
    void enable() {
        Bukkit.getPluginManager().registerEvents(this,plugin);
        for(World world:Bukkit.getWorlds()) for(Entity entity:world.getEntities()) if(entity instanceof ItemDisplay display) restore(display);
        ticker=Bukkit.getScheduler().runTaskTimer(plugin,this::tick,1,1);
    }
    void disable() {
        if(ticker!=null) ticker.cancel();
        HandlerList.unregisterAll(this);
        for(Bullet bullet:bullets.values()) persist(bullet);
        bullets.clear();lastGraze.clear();impacts.clear();
    }
    public DanmakuRules rules() { return rules; }
    /** Hook for the future source NPC faction/direct-target implementation; owner exclusion always applies. */
    public void setFriendlyPredicate(BiPredicate<Entity,Entity> predicate) { thread();friendly=Objects.requireNonNull(predicate); }
    public int activeCount() { return bullets.size(); }
    public List<Impact> recentImpacts() { return List.copyOf(impacts); }
    public List<BulletSnapshot> snapshots() { thread();return bullets.values().stream().map(b->new BulletSnapshot(b.display.getUniqueId(),b.owner,b.age,b.lifetime(),b.bounces,b.display.getLocation(),b.base.clone(),b.properties)).toList(); }

    public FireResult fire(Player player,EquipmentSlot hand) {
        thread();ItemStack held=player.getInventory().getItem(hand);DanmakuRules.Type type=rules.type(held);
        if(type==null) return new FireResult(FireStatus.NOT_DANMAKU,List.of(),0);
        if(player.hasCooldown(held)) return new FireResult(FireStatus.COOLDOWN,List.of(),type.cooldownTicks());
        DanmakuRules.Properties p;
        List<UUID> spawned;
        try { p=rules.properties(held);spawned=launch(player,held,player.getEyeLocation(),player.getEyeLocation().getPitch(),player.getEyeLocation().getYaw()); }
        catch(IllegalArgumentException malformed) { return new FireResult(malformed.getMessage()!=null&&malformed.getMessage().startsWith("Trajectory")?FireStatus.UNSUPPORTED_TRAJECTORY:FireStatus.INVALID_PROPERTIES,List.of(),0); }
        player.setCooldown(held,Math.max(1,type.cooldownTicks()));
        if(!p.infinite()) player.getInventory().setItem(hand,wearWithoutBreaking(player,held));
        player.getWorld().playSound(player.getLocation(),Ids.MOD_ID + ":fire",SoundCategory.NEUTRAL,1,1);
        if(hand==EquipmentSlot.HAND) player.swingMainHand();else player.swingOffHand();
        return new FireResult(FireStatus.FIRED,spawned,Math.max(1,type.cooldownTicks()));
    }

    /** Trusted launcher for NPC/source integrations and server QA. It does not debit a player's inventory/cooldown. */
    public List<UUID> launch(LivingEntity owner,ItemStack item,Location origin,float pitch,float yaw) {
        thread();DanmakuRules.Type type=rules.type(item);if(type==null) throw new IllegalArgumentException("Not an ordinary danmaku");
        DanmakuRules.Properties p=rules.properties(item);
        if(!Float.isFinite(p.actualSpeed())||!Float.isFinite(p.damage())||!Float.isFinite(p.scale())||p.damage()<0
                ||damageType(p.damageType())==null) throw new IllegalArgumentException("Invalid danmaku properties");
        List<DanmakuMath.Shot> shots=DanmakuMath.volley(p.templateId(),pitch,yaw,rules.trajectory(item),p.count());
        List<UUID> ids=new ArrayList<>();List<Bullet> created=new ArrayList<>();
        try {
            for(var shot:shots) {
                Vector offset=new Vector(shot.offset().x(),shot.offset().y(),shot.offset().z());
                Location position=origin.clone().add(offset);World world=position.getWorld();
                if(!world.isChunkLoaded(position.getBlockX()>>4,position.getBlockZ()>>4)) continue;
                var v=DanmakuMath.velocity(shot,p.actualSpeed(),ThreadLocalRandom.current());
                // Preserve default DanmakuMotionState's zero/near-zero velocity fallback to the projectile look vector.
                if(v.length()<.0001) {double n=v.length();v=n<1e-8?new DanmakuMath.V(0,0,1):new DanmakuMath.V(-v.x()/n,-v.y()/n,v.z()/n);}
                ItemDisplay display=world.spawn(position,ItemDisplay.class,entity->{entity.setPersistent(true);entity.setGravity(false);entity.setInvulnerable(true);entity.setItemStack(item.asOne());entity.setTeleportDuration(1);entity.setInterpolationDuration(1);entity.setViewRange(2);entity.setBrightness(new Display.Brightness(15,15));});
                Bullet bullet=new Bullet(display,owner==null?null:canonical(owner).getUniqueId(),item.asOne(),p,new Vector(v.x(),v.y(),v.z()),type.id(),shot.pitch(),shot.yaw());
                created.add(bullet);render(bullet);persist(bullet);ids.add(display.getUniqueId());
            }
        } catch(RuntimeException failure) { for(Bullet bullet:created) bullet.display.remove();throw failure; }
        for(Bullet bullet:created) bullets.put(bullet.display.getUniqueId(),bullet);
        return List.copyOf(ids);
    }

    /**
     * Trusted single-entity source integration. Keeps the same default motion, zero gravity and source lifetime.
     * Vanilla visual items are allowed: WeaponOfTheMoon actually uses a copper nugget, not a registered bullet item.
     * Does not use an item template, debit inventory, set cooldown or invent acceleration from property metadata.
     */
    public UUID launchOne(LivingEntity owner,ItemStack item,Location origin,float pitch,float yaw,LaunchSpec spec) {
        thread();Objects.requireNonNull(spec);Objects.requireNonNull(item);var p=spec.properties();
        validateSpec(spec);if(!Float.isFinite(pitch)||!Float.isFinite(yaw))throw new IllegalArgumentException("Invalid source angles");
        var direction=DanmakuMath.direction(pitch,yaw);Location position=origin.clone().add(direction.x()*spec.forwardOffset(),direction.y()*spec.forwardOffset(),direction.z()*spec.forwardOffset());var world=position.getWorld();
        if(!world.isChunkLoaded(position.getBlockX()>>4,position.getBlockZ()>>4))return null;
        var random=ThreadLocalRandom.current();double spread=.0172275*spec.divergence();var v=direction.normal().add(new DanmakuMath.V((random.nextDouble()-random.nextDouble())*spread,(random.nextDouble()-random.nextDouble())*spread,(random.nextDouble()-random.nextDouble())*spread)).scale(spec.initialSpeed());
        Vector velocity=new Vector(v.x(),v.y(),v.z());if(spec.addOwnerMovement()&&owner!=null){var moving=owner.getVelocity();velocity.add(new Vector(moving.getX(),owner.isOnGround()?0:moving.getY(),moving.getZ()));}
        // Default MotionState clamps displacement to16 per tick; retain this without changing the old11-type path.
        if(velocity.lengthSquared()>256)velocity.normalize().multiply(16);
        // Projectile sets its initial look BEFORE shootFromRotation adds owner movement.
        // If those vectors cancel, MotionState falls back to that original look, not the owner's residual vector.
        if(velocity.length()<.0001){double n=v.length();velocity=n<1e-8?new Vector(0,0,1):new Vector(-v.x()/n,-v.y()/n,v.z()/n);}
        // Source bullet updateRotation derives the display look from the final delta, including owner movement.
        float facingPitch=pitch,facingYaw=yaw;
        if(spec.velocityFacing()){double horizontal=Math.hypot(velocity.getX(),velocity.getZ());if(horizontal>1.0E-8D)facingYaw=(float)Math.toDegrees(Math.atan2(-velocity.getX(),velocity.getZ()));facingPitch=(float)Math.toDegrees(Math.atan2(-velocity.getY(),horizontal));}
        var display=world.spawn(position,ItemDisplay.class,entity->{entity.setPersistent(true);entity.setGravity(false);entity.setInvulnerable(true);entity.setItemStack(item.asOne());entity.setTeleportDuration(1);entity.setInterpolationDuration(1);entity.setViewRange(2);entity.setBrightness(new Display.Brightness(15,15));});
        if(!display.isValid())return null;
        var type=rules.type(item);var custom=CraftEngineItems.getCustomItemId(item);String identity=type!=null?type.id():custom==null?item.getType().getKey().asString():custom.asString();
        var bullet=new Bullet(display,owner==null?null:canonical(owner).getUniqueId(),item.asOne(),p,velocity,identity,facingPitch,facingYaw,spec);
        try{render(bullet);persist(bullet);bullets.put(display.getUniqueId(),bullet);return display.getUniqueId();}catch(RuntimeException failure){display.remove();throw failure;}
    }
    private void validateSpec(LaunchSpec spec){var p=Objects.requireNonNull(spec.properties());if(!Float.isFinite(spec.initialSpeed())||spec.initialSpeed()<0||spec.initialSpeed()>16||!Float.isFinite(spec.divergence())||spec.divergence()<0||spec.divergence()>180||!Float.isFinite(spec.forwardOffset())||Math.abs(spec.forwardOffset())>16||spec.lifetimeTicks()<1||spec.lifetimeTicks()>72000
            ||!Float.isFinite(p.actualSpeed())||!Float.isFinite(p.damage())||p.damage()<0||!Float.isFinite(p.scale())||!Float.isFinite(p.acceleration())||!Float.isFinite(p.hitboxScale())||damageType(p.damageType())==null)throw new IllegalArgumentException("Invalid explicit source launch properties");}

    private ItemStack wearWithoutBreaking(Player player,ItemStack item) {
        ItemStack result=item.clone();Integer maximum=result.getData(DataComponentTypes.MAX_DAMAGE);Integer damage=result.getData(DataComponentTypes.DAMAGE);
        if(maximum==null||damage==null) return result;
        // Preserve AbstractDanmakuItem's explicit already-depleted guard, then the source max-1 clamp.
        if(damage>=maximum) { result.subtract(1);return result.isEmpty()?null:result; }
        if(player.getGameMode()==GameMode.CREATIVE||result.getItemMeta().isUnbreakable()) return result;
        if(ThreadLocalRandom.current().nextInt(result.getEnchantmentLevel(Enchantment.UNBREAKING)+1)!=0) return result;
        int delta=Math.min(maximum-1,damage+1)-damage;if(delta<=0)return result;
        PlayerItemDamageEvent event=new PlayerItemDamageEvent(player,result,delta,1);Bukkit.getPluginManager().callEvent(event);
        if(!event.isCancelled()&&event.getDamage()!=0) result.setData(DataComponentTypes.DAMAGE,Math.max(0,Math.min(maximum-1,damage+event.getDamage())));
        return result;
    }

    private void tick() {
        for(Bullet bullet:List.copyOf(bullets.values())) {
            if(!bullet.display.isValid()||bullet.display.isDead()) { bullets.remove(bullet.display.getUniqueId());continue; }
            if(Bukkit.getServer().getServerTickManager().isFrozen(bullet.display)) continue;
            Location start=bullet.display.getLocation();
            boolean water=start.getBlock().getType()==Material.WATER||start.getBlock().getType()==Material.BUBBLE_COLUMN
                    ||start.getBlock().getBlockData() instanceof Waterlogged wet&&wet.isWaterlogged();
            Vector delta=bullet.base.clone().multiply(water?.8f:.99f);
            Location end=start.clone().add(delta);
            if(!start.getWorld().isChunkLoaded(end.getBlockX()>>4,end.getBlockZ()>>4)) { persist(bullet);continue; }
            Entity owner=bullet.owner==null?null:Bukkit.getEntity(bullet.owner);
            if(owner instanceof LivingEntity living&&living.isDead()) { if(++bullet.ownerDeadTicks>=20) {remove(bullet,true);continue;} }
            else bullet.ownerDeadTicks=0;
            double length=delta.length();RayTraceResult block=null,entity=null;
            if(length>1e-8) {
                Vector direction=delta.clone().normalize();
                block=start.getWorld().rayTraceBlocks(start,direction,length,FluidCollisionMode.NEVER,true);
                double bound=block==null?length:block.getHitPosition().distance(start.toVector());
                // Entity.baseTick increments tickCount before vanilla ProjectileUtil performs this scan.
                entity=start.getWorld().rayTraceEntities(start,direction,bound,DanmakuMath.hitMargin(bullet.age+1,bullet.properties.collisionScale()),target->canHit(bullet,owner,target));
            }
            if(entity!=null&&entity.getHitEntity()!=null) { hitEntity(bullet,owner,entity);continue; }
            if(block!=null&&block.getHitBlock()!=null) { hitBlock(bullet,block,delta);continue; }
            bullet.display.teleport(end);bullet.age++;
            bullet.roll-=Math.toRadians(15)*delta.lengthSquared();
            if(bullet.age>=bullet.lifetime()) {remove(bullet,true);continue;}
            if((bullet.age&1)==0) graze(bullet,owner);
            if(bullet.age%3==0) start.getWorld().spawnParticle(Particle.SNOWFLAKE,end.clone().subtract(delta.clone().multiply(2)),1,0,0,0,0);
            render(bullet);persist(bullet);
        }
    }
    private boolean canHit(Bullet bullet,Entity owner,Entity target) {
        if(!(target instanceof LivingEntity)||target.isDead()||!target.isValid()||target instanceof Player player&&player.getGameMode()==GameMode.SPECTATOR) return false;
        Entity normalized=canonical(target);
        return normalized!=null&&!normalized.getUniqueId().equals(bullet.owner)&&(owner==null||!friendly.test(owner,normalized));
    }
    private static Entity canonical(Entity entity) {
        var mod=ReverieDreamsPlugin.getMod();
        if(mod!=null) {
            try { var id=mod.getNPC().service().canonicalId(entity);if(id.isPresent()) {Entity carrier=Bukkit.getEntity(id.get());if(carrier!=null)return carrier;} }
            catch(IllegalStateException notEnabled) { /* Explicit NPC module shutdown; the original entity remains usable. */ }
        }
        return entity;
    }
    private void hitEntity(Bullet bullet,Entity owner,RayTraceResult result) {
        Entity target=canonical(result.getHitEntity());Location at=result.getHitPosition().toLocation(bullet.display.getWorld());
        // Remove before dispatching damage events, so re-entrant listeners cannot apply the same bullet twice.
        remove(bullet,false);
        if(target instanceof LivingEntity living) {
            DamageType type=damageType(bullet.properties.damageType());
            if(type==null)return; // Registry reload removed a custom override after launch; never substitute another damage type.
            DamageSource.Builder builder=DamageSource.builder(type);
            // Preserve the actual source overload: owner is both direct/causing; ownerless has neither nor a location.
            if(owner!=null) builder.withCausingEntity(owner).withDirectEntity(owner);
            living.damage(bullet.properties.damage(),builder.build());living.setNoDamageTicks(2);
            record(new Impact(bullet.display.getUniqueId(),target.getUniqueId(),false,at,bullet.properties.damage()));
        }
    }
    private void hitBlock(Bullet bullet,RayTraceResult result,Vector velocity) {
        Location at=result.getHitPosition().toLocation(bullet.display.getWorld());
        record(new Impact(bullet.display.getUniqueId(),null,true,at,0));
        boolean note=bullet.type.equals(Ids.MOD_ID + ":note");
        if(note)at.getWorld().playSound(at,Sound.BLOCK_NOTE_BLOCK_BASS,SoundCategory.BLOCKS,1,1);
        if(note&&++bullet.bounces<=5&&result.getHitBlockFace()!=null) {
            Vector normal=result.getHitBlockFace().getDirection();Vector reflection=velocity.clone().subtract(normal.clone().multiply(2*velocity.dot(normal))).multiply(.8);
            // The source constructs a fresh reflected entity: its per-entity graze/death-observation state resets too.
            bullet.base=reflection;bullet.age=0;bullet.roll=0;bullet.grazed=false;bullet.ownerDeadTicks=0;
            bullet.yaw=(float)Math.toDegrees(Math.atan2(reflection.getX(),reflection.getZ()));
            bullet.pitch=(float)Math.toDegrees(Math.atan2(reflection.getY(),Math.hypot(reflection.getX(),reflection.getZ())));
            bullet.display.teleport(at.clone().add(normal.multiply(.35)));
            render(bullet);persist(bullet);
        } else remove(bullet,false);
        int particles=Math.min(64,(int)(bullet.properties.damage()*velocity.length())*4);
        if(particles>0)at.getWorld().spawnParticle(Particle.BLOCK,at,particles,1,1,1,.25,result.getHitBlock().getBlockData());
    }
    private void record(Impact impact) { impacts.addLast(impact);while(impacts.size()>128)impacts.removeFirst(); }
    private DamageType damageType(String id) {
        if(id.equals(Ids.MOD_ID + ":danmaku_generic"))return generic;
        if(id.equals(Ids.MOD_ID + ":danmaku_real"))return real;
        NamespacedKey key=NamespacedKey.fromString(id);
        return key==null?null:RegistryAccess.registryAccess().getRegistry(RegistryKey.DAMAGE_TYPE).get(key);
    }
    public boolean cancel(UUID id) { thread();Bullet bullet=bullets.get(id);if(bullet==null)return false;remove(bullet,true);return true; }
    private void remove(Bullet bullet,boolean particle) {
        if(bullets.remove(bullet.display.getUniqueId())==null)return;
        if(particle)bullet.display.getWorld().spawnParticle(Particle.SNOWFLAKE,bullet.display.getLocation(),1,0,0,0,0);
        bullet.display.remove();
    }
    private void render(Bullet bullet) {
        float scale=bullet.properties.scale()*.85f;Quaternionf rotation=new Quaternionf();
        if(bullet.properties.tile()) {
            bullet.display.setBillboard(Display.Billboard.CENTER);bullet.display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);rotation.rotateY((float)Math.PI);
            if(bullet.type.equals(Ids.MOD_ID + ":star")||bullet.properties.templateId().equals(Ids.MOD_ID + ":star"))rotation.rotateZ((float)bullet.roll);
        } else {
            bullet.display.setBillboard(Display.Billboard.FIXED);bullet.display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.GUI);
            rotation.rotateY((float)Math.toRadians(bullet.yaw-90)).rotateZ((float)Math.toRadians(bullet.pitch)).rotateX((float)Math.PI/2).rotateZ(-(float)Math.PI/2);
            if(bullet.explicit!=null&&bullet.explicit.velocityFacing()){bullet.display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);rotation=new Quaternionf().rotateY((float)Math.toRadians(-bullet.yaw)).rotateX((float)Math.toRadians(bullet.pitch));}
        }
        bullet.display.setTransformation(new Transformation(new Vector3f(0,bullet.properties.tile()?.25f*scale:0,0),rotation,new Vector3f(scale),new Quaternionf()));
    }
    private void graze(Bullet bullet,Entity owner) {
        if(bullet.grazed||owner instanceof Player)return;
        Location pos=bullet.display.getLocation();Player nearest=null;double min=9;
        for(Player player:pos.getWorld().getNearbyPlayers(pos,3)) if(!player.isDead()&&player.getGameMode()!=GameMode.CREATIVE&&player.getGameMode()!=GameMode.SPECTATOR) {
            double distance=player.getLocation().distanceSquared(pos);if(distance<min){nearest=player;min=distance;}
        }
        if(nearest==null||owner!=null&&friendly.test(owner,nearest))return;
        double size=.5*bullet.properties.collisionScale();BoundingBox box=new BoundingBox(pos.getX()-size/2,pos.getY(),pos.getZ()-size/2,pos.getX()+size/2,pos.getY()+size,pos.getZ()+size/2);
        if(nearest.getBoundingBox().clone().expand(.05).overlaps(box))return;
        double dx=pos.getX()-nearest.getX(),dy=pos.getY()+size/2-(nearest.getY()+nearest.getHeight()/2),dz=pos.getZ()-nearest.getZ();
        if(dx*dx+dy*dy+dz*dz>2.25)return;
        int now=Bukkit.getCurrentTick();if(now-lastGraze.getOrDefault(nearest.getUniqueId(),Integer.MIN_VALUE/2)<3)return;
        bullet.grazed=true;lastGraze.put(nearest.getUniqueId(),now);nearest.getWorld().playSound(nearest.getLocation(),Ids.MOD_ID + ":graze",SoundCategory.PLAYERS,.45f,1.3f+ThreadLocalRandom.current().nextFloat()*.15f);
    }
    private void persist(Bullet bullet) {
        JsonObject data=new JsonObject();data.addProperty("schema",1);if(bullet.owner!=null)data.addProperty("owner",bullet.owner.toString());
        data.add("properties",DanmakuRules.encode(bullet.properties));data.addProperty("type",bullet.type);data.addProperty("x",bullet.base.getX());data.addProperty("y",bullet.base.getY());data.addProperty("z",bullet.base.getZ());
        data.addProperty("age",bullet.age);data.addProperty("bounces",bullet.bounces);data.addProperty("roll",bullet.roll);data.addProperty("pitch",bullet.pitch);data.addProperty("yaw",bullet.yaw);data.addProperty("grazed",bullet.grazed);data.addProperty("ownerDeadTicks",bullet.ownerDeadTicks);
        if(bullet.explicit!=null){var extra=new JsonObject();extra.addProperty("schema",1);extra.addProperty("initial_speed",bullet.explicit.initialSpeed());extra.addProperty("divergence",bullet.explicit.divergence());extra.addProperty("forward_offset",bullet.explicit.forwardOffset());extra.addProperty("owner_movement",bullet.explicit.addOwnerMovement());extra.addProperty("lifetime",bullet.explicit.lifetimeTicks());extra.addProperty("velocity_facing",bullet.explicit.velocityFacing());data.add("launch_spec",extra);}
        bullet.display.getPersistentDataContainer().set(STATE,PersistentDataType.STRING,data.toString());
        if(!bullet.display.getPersistentDataContainer().has(ITEM,PersistentDataType.BYTE_ARRAY))bullet.display.getPersistentDataContainer().set(ITEM,PersistentDataType.BYTE_ARRAY,bullet.item.serializeAsBytes());
    }
    private void restore(ItemDisplay display) {
        String encoded=display.getPersistentDataContainer().get(STATE,PersistentDataType.STRING);if(encoded==null||bullets.containsKey(display.getUniqueId()))return;
        try {
            JsonObject data=JsonParser.parseString(encoded).getAsJsonObject();if(data.get("schema").getAsInt()!=1)throw new IllegalArgumentException("Unknown projectile schema");
            ItemStack item=ItemStack.deserializeBytes(Objects.requireNonNull(display.getPersistentDataContainer().get(ITEM,PersistentDataType.BYTE_ARRAY)));
            LaunchSpec explicit=null;DanmakuRules.Properties properties;
            if(data.has("launch_spec")){var extra=data.getAsJsonObject("launch_spec");if(extra.get("schema").getAsInt()!=1)throw new IllegalArgumentException("Unknown explicit launch schema");var value=data.getAsJsonObject("properties");
                properties=new DanmakuRules.Properties(value.get("templateId").getAsString(),value.get("count").getAsInt(),value.get("damage").getAsFloat(),value.has("damage_type")?value.get("damage_type").getAsString():"reverie_dreams:danmaku_generic",value.get("scale").getAsFloat(),value.get("speed").getAsFloat(),value.get("acceleration").getAsFloat(),value.get("tile").getAsBoolean(),value.get("infinite").getAsBoolean(),value.has("speed_version")?value.get("speed_version").getAsInt():0,value.has("hitbox_scale")?value.get("hitbox_scale").getAsFloat():-1);
                explicit=new LaunchSpec(properties,extra.get("initial_speed").getAsFloat(),extra.get("divergence").getAsFloat(),extra.get("forward_offset").getAsFloat(),extra.get("owner_movement").getAsBoolean(),extra.has("lifetime")?extra.get("lifetime").getAsInt():200,extra.has("velocity_facing")&&extra.get("velocity_facing").getAsBoolean());validateSpec(explicit);
                var type=rules.type(item);var custom=CraftEngineItems.getCustomItemId(item);String identity=type!=null?type.id():custom==null?item.getType().getKey().asString():custom.asString();if(!identity.equals(data.get("type").getAsString()))throw new IllegalArgumentException("Saved visual identity mismatch");
            }else{item.editMeta(meta->meta.getPersistentDataContainer().set(DanmakuRules.PROPERTIES,PersistentDataType.STRING,data.getAsJsonObject("properties").toString()));properties=rules.properties(item);}
            Bullet bullet=new Bullet(display,data.has("owner")?UUID.fromString(data.get("owner").getAsString()):null,item,properties,new Vector(data.get("x").getAsDouble(),data.get("y").getAsDouble(),data.get("z").getAsDouble()),data.get("type").getAsString(),data.get("pitch").getAsFloat(),data.get("yaw").getAsFloat(),explicit);
            bullet.age=data.get("age").getAsInt();bullet.bounces=data.get("bounces").getAsInt();bullet.roll=data.get("roll").getAsDouble();bullet.grazed=data.get("grazed").getAsBoolean();
            bullet.ownerDeadTicks=data.has("ownerDeadTicks")?data.get("ownerDeadTicks").getAsInt():0;
            if(bullet.age<0||bullet.age>=bullet.lifetime()||bullet.bounces<0||bullet.bounces>5||!Double.isFinite(bullet.base.lengthSquared())
                    ||!Float.isFinite(bullet.properties.actualSpeed())||!Float.isFinite(bullet.properties.damage())||bullet.properties.damage()<0
                    ||!Float.isFinite(bullet.properties.scale())||!Double.isFinite(bullet.roll)||!Float.isFinite(bullet.pitch)||!Float.isFinite(bullet.yaw)
                    ||damageType(bullet.properties.damageType())==null)throw new IllegalArgumentException("Invalid saved projectile state");
            render(bullet);bullets.put(display.getUniqueId(),bullet);
        } catch(RuntimeException bad) {plugin.getSLF4JLogger().error("Danmaku projectile {} preserved without activation",display.getUniqueId(),bad);}
    }
    @EventHandler public void load(EntitiesLoadEvent event) {for(Entity entity:event.getEntities())if(entity instanceof ItemDisplay display)restore(display);}
    @EventHandler public void unload(EntitiesUnloadEvent event) {for(Entity entity:event.getEntities()){Bullet bullet=bullets.remove(entity.getUniqueId());if(bullet!=null)persist(bullet);}}
    @EventHandler public void quit(PlayerQuitEvent event) {lastGraze.remove(event.getPlayer().getUniqueId());}
    private static void thread() {if(!Bukkit.isPrimaryThread())throw new IllegalStateException("Danmaku requires the Paper main thread");}
    private static final class Bullet {
        final ItemDisplay display;final UUID owner;final ItemStack item;final DanmakuRules.Properties properties;final String type;
        final LaunchSpec explicit;
        Vector base;int age,bounces,ownerDeadTicks;double roll;float pitch,yaw;boolean grazed;
        int lifetime(){return explicit==null?200:explicit.lifetimeTicks();}
        Bullet(ItemDisplay display,UUID owner,ItemStack item,DanmakuRules.Properties properties,Vector base,String type,float pitch,float yaw){this(display,owner,item,properties,base,type,pitch,yaw,null);}
        Bullet(ItemDisplay display,UUID owner,ItemStack item,DanmakuRules.Properties properties,Vector base,String type,float pitch,float yaw,LaunchSpec explicit){this.display=display;this.owner=owner;this.item=item;this.properties=properties;this.base=base;this.type=type;this.pitch=pitch;this.yaw=yaw;this.explicit=explicit;}
    }
}
