package cc.thonly.reverie_dreams.entity.misc;

import cc.thonly.reverie_dreams.api.entity.type.FriendlyFaction;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuMath;
import cc.thonly.reverie_dreams.data.danmaku.stg.LaserVolume;
import cc.thonly.reverie_dreams.data.danmaku.stg.MovingLaserSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.runtime.DanmakuPatternRuntime;
import net.minecraft.core.BlockPos;
import cc.thonly.reverie_dreams.registry.content.RDDamageTypes;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

/**
 * 有限长度的直线激光，穿透方块。固定激光随施法实体移动，飞行激光独立于施法原点推进。
 *
 * <p>状态机：预警（默认 20 tick，细激光视觉）→ 激活（粗激光，逐 tick 对线段附近的
 * LivingEntity 造成 LASER 类型默认伤害）→ discard。owner 死亡即消失。
 * 同一目标依靠原版 {@code invulnerableTime} 无敌帧防连伤，服务端另有
 * “每目标每 10 tick 最多一次判定”的节流，避免每 tick 刷伤害调用。
 */
public class DanmakuLaserEntity extends Entity {
    public static final int STATE_WARN = 0;
    public static final int STATE_ACTIVE = 1;

    public static final EntityDataAccessor<Float> DATA_PITCH =
            SynchedEntityData.defineId(DanmakuLaserEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> DATA_YAW =
            SynchedEntityData.defineId(DanmakuLaserEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> DATA_LENGTH =
            SynchedEntityData.defineId(DanmakuLaserEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> DATA_WIDTH =
            SynchedEntityData.defineId(DanmakuLaserEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(DanmakuLaserEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<String> DATA_OWNER_UUID =
            SynchedEntityData.defineId(DanmakuLaserEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<ItemStack> DATA_DISPLAY_ITEM =
            SynchedEntityData.defineId(DanmakuLaserEntity.class, EntityDataSerializers.ITEM_STACK);

    private static final long HIT_ATTEMPT_INTERVAL = 10L;

    private int warnTicks = 20;
    private int activeTicks = 40;
    private float sweepPerTick;
    private float sweepHalfAngle;
    private float sweepStartYaw;
    private Vec3 castOffset = Vec3.ZERO;
    @Nullable
    private DanmakuPatternRuntime spellRuntime;
    @Nullable
    private MovingLaserSpec movingBeam;
    @Nullable
    private InterpolationHandler movingInterpolation;
    /** 目标 UUID -> 下次允许尝试伤害的 gameTime。 */
    private final Object2LongOpenHashMap<UUID> nextHitAttempt = new Object2LongOpenHashMap<>();

    public DanmakuLaserEntity(EntityType<? extends DanmakuLaserEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            if (this.movingInterpolation != null) {
                this.movingInterpolation.interpolate();
            }
            return;
        }
        Entity owner;
        if (this.spellRuntime != null) {
            if (!this.spellRuntime.refreshOrigin() || this.spellRuntime.isDone()) {
                this.discard();
                return;
            }
            if (this.movingBeam == null) {
                Vec3 origin = this.spellRuntime.getOrigin().add(this.castOffset);
                if (!serverLevel.isPositionEntityTicking(BlockPos.containing(origin))) {
                    this.discard();
                    return;
                }
                this.setPos(origin);
            }
            owner = this.spellRuntime.getCaster();
        } else {
            owner = this.resolveOwner();
            if ((this.movingBeam == null && owner == null) || (owner != null && !owner.isAlive())) {
                this.discard();
                return;
            }
        }
        if (this.tickCount < this.warnTicks) {
            this.warnParticles(serverLevel);
            return;
        }
        if (this.tickCount >= this.warnTicks + this.activeTicks) {
            this.discard();
            return;
        }
        if (this.getState() != STATE_ACTIVE) {
            this.setState(STATE_ACTIVE);
        } else if (this.sweepPerTick != 0f) {
            float yaw = this.sweepHalfAngle > 0f
                    ? this.sweepStartYaw + DanmakuLaser.sweepOffset(this.sweepPerTick, this.sweepHalfAngle,
                            this.tickCount - this.warnTicks)
                    : this.getYaw() + this.sweepPerTick;
            this.setRotation(this.getPitch(), yaw);
        }
        var moving = this.movingBeam;
        if (moving == null) {
            this.damageAlongBeam(serverLevel, owner, this.beamVolume());
            return;
        }
        Vec3 start = this.position();
        float nextLength = moving.nextLength(this.getBeamLength());
        float advance = moving.tailAdvance(this.getBeamLength(), nextLength);
        Vec3 next = start.add(this.beamDirection().scale(advance));
        if (!serverLevel.isPositionEntityTicking(BlockPos.containing(next))) {
            this.discard();
            return;
        }
        this.setPos(next);
        this.setBeam(nextLength, moving.width());
        // Cover the path swept from last tick's tail to this tick's tip.
        this.damageAlongBeam(serverLevel, owner, new LaserVolume(start, this.getPitch(), this.getYaw(),
                nextLength + advance, this.getBeamWidth()));
    }

    public void setMovingBeam(MovingLaserSpec spec) {
        this.movingBeam = spec;
        this.warnTicks = 0;
        this.activeTicks = spec.duration();
        this.setBeam(spec.initialLength(), spec.width());
        this.setState(STATE_ACTIVE);
    }

    public boolean hasMovingBeamArt() {
        return this.getDisplayItem().is(DanmakuTypes.SHORT_LASER.getItemHolder().asItem());
    }

    @Override
    public @Nullable InterpolationHandler getInterpolation() {
        if (!this.level().isClientSide() || !this.hasMovingBeamArt()) {
            return null;
        }
        if (this.movingInterpolation == null) {
            this.movingInterpolation = new InterpolationHandler(this);
        }
        return this.movingInterpolation;
    }

    /**
     * 仅激活态：检测实体与可见的有限光束体积相交。
     * 不打击退与粒子，命中反馈交给目标自带的受击红闪与无敌帧。
     */
    private void damageAlongBeam(ServerLevel world, @Nullable Entity owner, LaserVolume beam) {
        long gameTime = world.getGameTime();
        List<LivingEntity> candidates = world.getEntitiesOfClass(LivingEntity.class, beam.bounds(),
                entity -> entity.isAlive() && !entity.isSpectator() && entity != owner
                        && !(FriendlyFaction.isFriendLy(entity, owner)
                        && !FriendlyFaction.isDirectTarget(owner, entity)));
        DamageSource source = null;
        for (LivingEntity target : candidates) {
            UUID targetId = target.getUUID();
            if (this.nextHitAttempt.getLong(targetId) > gameTime) {
                continue;
            }
            if (!beam.intersects(target.getBoundingBox())) {
                continue;
            }
            this.nextHitAttempt.put(targetId, gameTime + HIT_ATTEMPT_INTERVAL);
            if (source == null) {
                source = RDDamageTypes.create(this.registryAccess(), this.getBeamDamageType(), this, owner);
            }
            target.hurt(source, this.getBeamDamage());
        }
        // 偶发驱逐已经离开的条目，防止长时间存活激光的 map 增长
        if (this.tickCount % 100 == 0 && !this.nextHitAttempt.isEmpty()) {
            Iterator<Object2LongOpenHashMap.Entry<UUID>> iterator = this.nextHitAttempt.object2LongEntrySet().iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getLongValue() <= gameTime) {
                    iterator.remove();
                }
            }
        }
    }

    public Vec3 beamDirection() {
        return DanmakuMath.direction(this.getPitch(), this.getYaw());
    }

    public Vec3 beamEnd() {
        return this.position().add(this.beamDirection().scale(this.getBeamLength()));
    }

    public LaserVolume beamVolume() {
        return new LaserVolume(this.position(), this.getPitch(), this.getYaw(), this.getBeamLength(), this.getBeamWidth());
    }

    /**
     * 激光伤害/伤害类型：取外观物品对应的弹幕类型默认值（生成时写入 LASER 类型栈）；
     * 外观缺失时回退通用弹幕伤害。
     */
    public float getBeamDamage() {
        cc.thonly.reverie_dreams.data.danmaku.DanmakuType type = this.beamType();
        return type == null ? 4.0f : type.getDamage();
    }

    public net.minecraft.resources.ResourceKey<net.minecraft.world.damagesource.DamageType> getBeamDamageType() {
        cc.thonly.reverie_dreams.data.danmaku.DanmakuType type = this.beamType();
        return type == null ? RDDamageTypes.DANMAKU_GENERIC : type.getDamageType();
    }

    @Nullable
    private cc.thonly.reverie_dreams.data.danmaku.DanmakuType beamType() {
        return cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes.fromItem(this.getDisplayItem());
    }

    @Nullable
    public Entity resolveOwner() {
        String uuid = this.getOwnerUuid();
        if (uuid == null || uuid.isEmpty() || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        try {
            return serverLevel.getEntity(UUID.fromString(uuid));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    @Override
    public void onRemoval(RemovalReason reason) {
        super.onRemoval(reason);
        if (this.spellRuntime != null) {
            this.spellRuntime.untrack(this);
            this.spellRuntime = null;
        }
    }

    public void setSpellRuntime(DanmakuPatternRuntime runtime) {
        this.spellRuntime = runtime;
    }

    /** World offset fixed at creation, added to the live cast origin each tick. */
    public void setCastOffset(Vec3 worldOffset) {
        this.castOffset = worldOffset;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith(Entity entity) {
        return false;
    }

    /** 预警态提示粒子（服务端，客户端视觉由渲染器负责）。 */
    private void warnParticles(ServerLevel world) {
        if (this.tickCount % 4 != 0) {
            return;
        }
        Vec3 end = this.beamEnd();
        world.sendParticles(ParticleTypes.END_ROD,
                end.x, end.y, end.z,
                1, 0.1D, 0.1D, 0.1D, 0.01D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_PITCH, 0f);
        builder.define(DATA_YAW, 0f);
        builder.define(DATA_LENGTH, 16f);
        builder.define(DATA_WIDTH, 0.5f);
        builder.define(DATA_STATE, STATE_WARN);
        builder.define(DATA_OWNER_UUID, "");
        builder.define(DATA_DISPLAY_ITEM, ItemStack.EMPTY);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput view) {
        view.read("WarnTicks", Codec.INT).ifPresent(value -> this.warnTicks = value);
        view.read("ActiveTicks", Codec.INT).ifPresent(value -> this.activeTicks = value);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {
        view.putInt("WarnTicks", this.warnTicks);
        view.putInt("ActiveTicks", this.activeTicks);
    }

    public float getPitch() {
        return this.entityData.get(DATA_PITCH);
    }

    public float getYaw() {
        return this.entityData.get(DATA_YAW);
    }

    public void setRotation(float pitch, float yaw) {
        this.entityData.set(DATA_PITCH, pitch);
        this.entityData.set(DATA_YAW, yaw);
    }

    public float getBeamLength() {
        return this.entityData.get(DATA_LENGTH);
    }

    public float getBeamWidth() {
        return this.entityData.get(DATA_WIDTH);
    }

    public void setBeam(float length, float width) {
        this.entityData.set(DATA_LENGTH, Math.max(0.1f, length));
        this.entityData.set(DATA_WIDTH, Math.max(0.05f, width));
    }

    public int getState() {
        return this.entityData.get(DATA_STATE);
    }

    public void setState(int state) {
        this.entityData.set(DATA_STATE, state);
    }

    @Nullable
    public String getOwnerUuid() {
        String value = this.entityData.get(DATA_OWNER_UUID);
        return value == null || value.isEmpty() ? null : value;
    }

    public void setOwnerUuid(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNER_UUID, uuid == null ? "" : uuid.toString());
    }

    public ItemStack getDisplayItem() {
        return this.entityData.get(DATA_DISPLAY_ITEM);
    }

    public void setDisplayItem(ItemStack stack) {
        this.entityData.set(DATA_DISPLAY_ITEM, stack);
    }

    public int getWarnTicks() {
        return this.warnTicks;
    }

    public int getActiveTicks() {
        return this.activeTicks;
    }

    public void setSweep(float sweepPerTick, float halfAngle) {
        this.sweepPerTick = sweepPerTick;
        this.sweepHalfAngle = halfAngle;
        this.sweepStartYaw = this.getYaw();
    }

    public void setSweepPerTick(float sweepPerTick) {
        this.setSweep(sweepPerTick, 0f);
    }

    public void setTimings(int warnTicks, int activeTicks) {
        this.warnTicks = Math.max(1, warnTicks);
        this.activeTicks = Math.max(1, activeTicks);
    }
}
