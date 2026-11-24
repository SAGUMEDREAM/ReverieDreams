package cc.thonly.reverie_dreams.entity.misc;

import cc.thonly.reverie_dreams.api.entity.type.BypassHitEntity;
import cc.thonly.reverie_dreams.api.entity.type.FriendlyFaction;
import cc.thonly.reverie_dreams.entity.misc.spark.MasterSparkBeam;
import cc.thonly.reverie_dreams.entity.misc.spark.MasterSparkCycle;
import cc.thonly.reverie_dreams.entity.misc.spark.MasterSparkAim;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.content.RDDamageTypes;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.tag.RDEntityTypeTags;
import cc.thonly.reverie_dreams.registry.content.sound.RDSoundEvents;
import cc.thonly.reverie_dreams.util.codec.UUIDCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.UUID;

/** A stationary, owner-attributed Master Spark turret; the beam is not a volley of projectiles. */
public class BaguaFurnaceEntity extends Entity implements BypassHitEntity {
    public static final EntityDataAccessor<Float> INIT_X_ROT = SynchedEntityData.defineId(BaguaFurnaceEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> INIT_Y_ROT = SynchedEntityData.defineId(BaguaFurnaceEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> CHARGE_PROGRESS = SynchedEntityData.defineId(BaguaFurnaceEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Long> BEAM_STARTED_AT = SynchedEntityData.defineId(BaguaFurnaceEntity.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Float> BEAM_LENGTH = SynchedEntityData.defineId(BaguaFurnaceEntity.class, EntityDataSerializers.FLOAT);
    private static final int MAX_TICK = MasterSparkCycle.LIFETIME_TICKS;
    private static final int TARGET_SEARCH_INTERVAL = 4;

    private UUID ownerId;
    @Nullable private LivingEntity owner;
    @Nullable private LivingEntity target;
    private final MasterSparkCycle cycle = new MasterSparkCycle();
    private final MasterSparkAim.Interpolation clientAim = new MasterSparkAim.Interpolation();
    private int livingTick;
    private int targetCooldown;

    public BaguaFurnaceEntity(EntityType<? extends Entity> type, Level level) { super(type, level); }

    public BaguaFurnaceEntity(ServerLevel level, LivingEntity owner) {
        this(RDEntityTypes.BAGUA_FURNACE.value(), level);
        this.owner = owner;
        this.ownerId = owner.getUUID();
        setAim(owner.getXRot(), owner.getYRot());
        Vec3 eye = owner.getEyePosition();
        Vec3 front = eye.add(owner.getLookAngle().scale(1.1));
        var hit = level.clip(new ClipContext(eye, front, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        if (hit.getType() != HitResult.Type.MISS) front = hit.getLocation().subtract(owner.getLookAngle().scale(MasterSparkBeam.MUZZLE_OFFSET + 0.2));
        setPos(front.x, front.y - MasterSparkBeam.MOUNT_HEIGHT, front.z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(INIT_X_ROT, 0f);
        builder.define(INIT_Y_ROT, 0f);
        builder.define(CHARGE_PROGRESS, 0f);
        builder.define(BEAM_STARTED_AT, -1L);
        builder.define(BEAM_LENGTH, 0f);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) {
            clientAim.tick(getInitXRot(), getInitYRot());
            return;
        }
        if (livingTick++ >= MAX_TICK) { discard(); return; }
        if (owner != null && (!owner.isAlive() || owner.isRemoved() || owner.level() != level)) owner = null;
        if (owner == null && ownerId != null && livingTick % 20 == 1) {
            Entity resolved = level.getEntity(ownerId);
            if (resolved instanceof LivingEntity living && living.isAlive()) owner = living;
        }
        tickTargeting(level);
        if (target != null) aimAtTarget();
        MasterSparkCycle.Phase previous = cycle.phase();
        cycle.tick(owner != null);
        if (cycle.phase() == MasterSparkCycle.Phase.FIRE) {
            if (previous != cycle.phase()) {
                entityData.set(BEAM_STARTED_AT, level.getGameTime());
                level.playSound(null, blockPosition(), RDSoundEvents.BAGUA.value(), SoundSource.PLAYERS, 1.6f, 0.85f);
                Vec3 muzzle = getMuzzlePosition();
                level.sendParticles(ColorParticleOption.create(ParticleTypes.FLASH, 0xFFFFFFFF), muzzle.x, muzzle.y, muzzle.z, 1, 0, 0, 0, 0);
            }
            // Steering changes the ray every tick, including while acquiring a new target.
            entityData.set(BEAM_LENGTH, (float) traceBeamLength(level));
            if (cycle.damagePulse()) damageBeam(level);
        } else {
            entityData.set(BEAM_STARTED_AT, -1L);
            entityData.set(BEAM_LENGTH, 0f);
        }
        entityData.set(CHARGE_PROGRESS, cycle.charge());
    }

    private void tickTargeting(ServerLevel level) {
        if (owner == null) {
            target = null;
            targetCooldown = 0;
            return;
        }
        if (target != null && (!isValidTarget(target) || target.level() != level
                || distanceToSqr(target) > MasterSparkBeam.RANGE * MasterSparkBeam.RANGE || !hasLineOfSight(target))) {
            target = null;
            targetCooldown = 0; // A dead/invalid target must not delay the next kill.
        }
        if (targetCooldown > 0) targetCooldown--;
        if (target == null && targetCooldown <= 0) {
            target = level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(MasterSparkBeam.RANGE), this::isValidTarget)
                    .stream().filter(entity -> distanceToSqr(entity) <= MasterSparkBeam.RANGE * MasterSparkBeam.RANGE)
                    .sorted(Comparator.comparingDouble(this::distanceToSqr))
                    .filter(this::hasLineOfSight).findFirst().orElse(null);
            targetCooldown = TARGET_SEARCH_INTERVAL;
        }
    }

    private void aimAtTarget() {
        if (target == null) return;
        Vec3 delta = target.getBoundingBox().getCenter().subtract(position().add(0, MasterSparkBeam.MOUNT_HEIGHT, 0));
        MasterSparkAim.Angles next = MasterSparkAim.towards(getInitXRot(), getInitYRot(), delta);
        setAim(next.pitch(), next.yaw());
    }

    private void setAim(float pitch, float yaw) {
        setXRot(pitch);
        setYRot(yaw);
        entityData.set(INIT_X_ROT, pitch);
        entityData.set(INIT_Y_ROT, yaw);
    }

    public Vec3 getBeamDirection() { return Vec3.directionFromRotation(getInitXRot(), getInitYRot()); }
    public Vec3 getMuzzlePosition() { return position().add(0, MasterSparkBeam.MOUNT_HEIGHT, 0).add(getBeamDirection().scale(MasterSparkBeam.MUZZLE_OFFSET)); }

    private double traceBeamLength(ServerLevel level) {
        Vec3 start = getMuzzlePosition(), direction = getBeamDirection();
        double limit = MasterSparkBeam.RANGE;
        for (double distance = 0; distance <= limit; distance += 2) {
            if (!level.hasChunkAt(BlockPos.containing(start.add(direction.scale(distance))))) {
                limit = Math.max(0, distance - 2);
                break;
            }
        }
        var hit = level.clip(new ClipContext(start, start.add(direction.scale(limit)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return hit.getType() == HitResult.Type.MISS ? limit : start.distanceTo(hit.getLocation());
    }

    private void damageBeam(ServerLevel level) {
        Vec3 start = getMuzzlePosition(), direction = getBeamDirection();
        double length = getBeamLength();
        if (length <= 0) return;
        DamageSource damage = RDDamageTypes.create(registryAccess(), RDDamageTypes.DANMAKU_GENERIC, this, owner);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, MasterSparkBeam.bounds(start, direction, length), this::isValidTarget)) {
            if (!MasterSparkBeam.intersects(start, direction, length, victim.getBoundingBox(), getBeamStrength(0))) continue;
            if (level.clip(new ClipContext(start, victim.getBoundingBox().getCenter(), ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, this)).getType() != HitResult.Type.MISS) continue;
            victim.hurtServer(level, damage, MasterSparkBeam.DAMAGE);
        }
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (!entity.isAlive() || entity.isSpectator() || entity == owner || entity.getUUID().equals(ownerId) || entity instanceof Player) return false;
        if (entity instanceof TamableAnimal tame && tame.isTame()) return false;
        if (owner != null && (entity.isAlliedTo(owner) || FriendlyFaction.isFriendLy(entity, owner)
                || entity instanceof BaseNPCLikeEntity npc && npc.isOwner(owner))) return false;
        if (!(entity instanceof Mob mob)) return false;
        return mob instanceof Enemy || mob.getType().builtInRegistryHolder().is(RDEntityTypeTags.YOKAI)
                || mob.getType().builtInRegistryHolder().is(RDEntityTypeTags.YOUSEI)
                || mob.getType().builtInRegistryHolder().is(EntityTypeTags.UNDEAD);
    }

    private boolean hasLineOfSight(LivingEntity target) {
        return level().clip(new ClipContext(getMuzzlePosition(), target.getBoundingBox().getCenter(), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    public void setOwnerId(UUID id) { ownerId = id; owner = null; }
    public float getInitXRot() { return entityData.get(INIT_X_ROT); }
    public float getInitYRot() { return entityData.get(INIT_Y_ROT); }
    public float getRenderPitch(float partialTick) { return clientAim.initialized() ? clientAim.pitch(partialTick) : getInitXRot(); }
    public float getRenderYaw(float partialTick) { return clientAim.initialized() ? clientAim.yaw(partialTick) : getInitYRot(); }
    public float getChargeProgress() { return entityData.get(CHARGE_PROGRESS); }
    public float getBeamLength() { return entityData.get(BEAM_LENGTH); }
    public boolean isFiring() { return entityData.get(BEAM_STARTED_AT) >= 0; }
    public float getBeamStrength(float partialTick) {
        if (!isFiring()) return 0;
        float age = level().getGameTime() - entityData.get(BEAM_STARTED_AT) + partialTick;
        return Mth.clamp((age + 1) / 4f, 0, 1);
    }

    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) { return false; }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        input.read("OwnerId", UUIDCodec.CODEC).ifPresent(this::setOwnerId);
        livingTick = Mth.clamp(input.getIntOr("LivingTick", MAX_TICK), 0, MAX_TICK);
        setAim(input.getFloatOr("LockedPitch", 0), input.getFloatOr("LockedYaw", 0));
        cycle.reset();
        entityData.set(CHARGE_PROGRESS, 0f);
        entityData.set(BEAM_STARTED_AT, -1L);
        entityData.set(BEAM_LENGTH, 0f);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        if (ownerId != null) output.store("OwnerId", UUIDCodec.CODEC, ownerId);
        output.putInt("LivingTick", livingTick);
        output.putFloat("LockedPitch", getInitXRot());
        output.putFloat("LockedYaw", getInitYRot());
    }
}
