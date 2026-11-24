package cc.thonly.reverie_dreams.entity.misc;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuFamiliar;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuMath;
import cc.thonly.reverie_dreams.data.danmaku.stg.runtime.SpellCastOrigin;
import net.minecraft.core.BlockPos;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import com.mojang.serialization.Codec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.UUID;

/**
 * STG 使魔/辅助发射源：漂浮在 owner 身旁、携带子图案的发射实体（不是弹幕，不可击破）。
 *
 * <p>服务端每 tick 把位置设在“owner 位置 + 偏移”（{@code orbitRadius > 0} 时绕 owner 水平公转），
 * 实体移动靠 setPos 同步，不做物理推进。生命周期：
 * <ul>
 *     <li>{@code duration > 0} —— 到期 discard；</li>
 *     <li>施法实体离开时围绕最后位置运行；父图案结束或施法实体被击败时随父图案清理。</li>
 * </ul>
 */
public class DanmakuFamiliarEntity extends Entity {
    public static final EntityDataAccessor<String> DATA_OWNER_UUID =
            SynchedEntityData.defineId(DanmakuFamiliarEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Vector3fc> DATA_OFFSET =
            SynchedEntityData.defineId(DanmakuFamiliarEntity.class, EntityDataSerializers.VECTOR3);
    public static final EntityDataAccessor<Float> DATA_ORBIT_RADIUS =
            SynchedEntityData.defineId(DanmakuFamiliarEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> DATA_ORBIT_SPEED =
            SynchedEntityData.defineId(DanmakuFamiliarEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> DATA_ORBIT_START =
            SynchedEntityData.defineId(DanmakuFamiliarEntity.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<ItemStack> DATA_DISPLAY_ITEM =
            SynchedEntityData.defineId(DanmakuFamiliarEntity.class, EntityDataSerializers.ITEM_STACK);

    private int duration = 0;
    private boolean viewRelative;
    @Nullable
    private SpellCastOrigin parentOrigin;

    public DanmakuFamiliarEntity(EntityType<? extends DanmakuFamiliarEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    /**
     * 由 {@code DanmakuPatternRuntime} 在 familiar.delay tick 时生成。
     */
    @Nullable
    public static DanmakuFamiliarEntity spawn(ServerLevel level, SpellCastOrigin parentOrigin, DanmakuFamiliar spec,
                                              boolean viewRelative) {
        DanmakuFamiliarEntity entity = new DanmakuFamiliarEntity(RDEntityTypes.DANMAKU_FAMILIAR.value(), level);
        entity.parentOrigin = parentOrigin;
        entity.viewRelative = viewRelative;
        Entity owner = parentOrigin.entity();
        entity.setOwnerUuid(owner == null ? null : owner.getUUID());
        entity.setFamiliarOffset(spec.offset());
        entity.setOrbit(spec.orbitRadius(), spec.orbitDegreesPerTick(), spec.orbitStartAngle());
        entity.setDuration(spec.duration());
        entity.followOrigin(parentOrigin.feet(), parentOrigin.pitch(), parentOrigin.yaw());
        return level.isPositionEntityTicking(BlockPos.containing(entity.position())) && level.addFreshEntity(entity) ? entity : null;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel level) {
            if (this.parentOrigin == null || !this.parentOrigin.update()
                    || this.duration > 0 && this.tickCount >= this.duration) {
                this.discard();
                return;
            }
            this.followOrigin(this.parentOrigin.feet(), this.parentOrigin.pitch(), this.parentOrigin.yaw());
            if (!level.isPositionEntityTicking(BlockPos.containing(this.position()))) {
                this.discard();
            }
        }
    }

    private void followOrigin(Vec3 feet, float pitch, float yaw) {
        float radius = this.getOrbitRadius();
        Vec3 offset = this.getFamiliarOffset();
        Vec3 displacement;
        if (radius > 0f) {
            float angle = this.tickCount * this.getOrbitDegreesPerTick() + this.getOrbitStartAngle();
            double radians = Math.toRadians(angle);
            displacement = new Vec3(Math.cos(radians) * radius, offset.y, Math.sin(radians) * radius);
        } else {
            displacement = offset;
        }
        Vec3 base = feet.add(this.viewRelative ? DanmakuMath.rotateY(displacement, -yaw) : displacement);
        this.setPos(base.x, base.y, base.z);
        this.setXRot(pitch);
        this.setYRot(yaw);
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
    public boolean shouldBeSaved() {
        return false;
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

    /** 渲染自转角（缓慢自转，客户端用）。 */
    public float getSpinDegrees(float partialTick) {
        return (this.tickCount + partialTick) * 2.5f;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_OWNER_UUID, "");
        builder.define(DATA_OFFSET, new org.joml.Vector3f(0f, 0f, 0f));
        builder.define(DATA_ORBIT_RADIUS, 0f);
        builder.define(DATA_ORBIT_SPEED, 0f);
        builder.define(DATA_ORBIT_START, 0f);
        builder.define(DATA_DISPLAY_ITEM, DanmakuTypes.FAMILIAR.getItemHolder().asItem().getDefaultInstance());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput view) {
        view.read("Duration", Codec.INT).ifPresent(value -> this.duration = value);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {
        view.putInt("Duration", this.duration);
    }

    @Nullable
    public String getOwnerUuid() {
        String value = this.entityData.get(DATA_OWNER_UUID);
        return value == null || value.isEmpty() ? null : value;
    }

    public void setOwnerUuid(@Nullable UUID uuid) {
        this.entityData.set(DATA_OWNER_UUID, uuid == null ? "" : uuid.toString());
    }

    public Vec3 getFamiliarOffset() {
        Vector3fc value = this.entityData.get(DATA_OFFSET);
        return new Vec3(value.x(), value.y(), value.z());
    }

    public void setFamiliarOffset(Vec3 offset) {
        this.entityData.set(DATA_OFFSET, new org.joml.Vector3f((float) offset.x, (float) offset.y, (float) offset.z));
    }

    public float getOrbitRadius() {
        return this.entityData.get(DATA_ORBIT_RADIUS);
    }

    public float getOrbitDegreesPerTick() {
        return this.entityData.get(DATA_ORBIT_SPEED);
    }

    public float getOrbitStartAngle() {
        return this.entityData.get(DATA_ORBIT_START);
    }

    public void setOrbit(float radius, float degreesPerTick, float startAngle) {
        this.entityData.set(DATA_ORBIT_RADIUS, radius);
        this.entityData.set(DATA_ORBIT_SPEED, degreesPerTick);
        this.entityData.set(DATA_ORBIT_START, startAngle);
    }

    public ItemStack getDisplayItem() {
        return this.entityData.get(DATA_DISPLAY_ITEM);
    }

    public void setDisplayItem(ItemStack stack) {
        this.entityData.set(DATA_DISPLAY_ITEM, stack);
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        this.duration = Math.max(0, duration);
    }
}
