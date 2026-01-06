package cc.thonly.reverie_dreams.entity.misc;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Set;

public class BaguaFurnaceEntity extends Entity implements BypassHitEntity, PolymerEntity {
    private static final int MAX_TICK = 300;
    private static final int PER_INTERVAL_TICK1 = 1;
    private static final int PER_INTERVAL_TICK2 = 5 * 20;
    private final LivingEntity owner;
    private int livingTick = 0;
    private int intervalTick1 = PER_INTERVAL_TICK1;
    private int intervalTick2 = PER_INTERVAL_TICK2;

    private final float fixedPitch;
    private final float fixedYaw;


    public BaguaFurnaceEntity(EntityType<? extends Entity> entityType, Level world) {
        super(entityType, world);
        this.owner = null;
        this.fixedPitch = 0;
        this.fixedYaw = 0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    public BaguaFurnaceEntity(ServerLevel world, @NotNull LivingEntity owner) {
        super(RDEntityTypes.BAGUA_FURNACE, world);
        this.owner = owner;

        this.setXRot(owner.getXRot());
        this.setYRot(owner.getYRot());
        Vec3 v3d = owner.position();
        this.setPos(v3d);
        this.setPos(v3d.x, owner.getEyeY(), v3d.z);

        double offsetX = -Math.sin(Math.toRadians(this.getYRot()));
        double offsetZ = Math.cos(Math.toRadians(this.getYRot()));
        this.setPosRaw(this.getX() + offsetX, this.getY(), this.getZ() + offsetZ);

        this.fixedPitch = owner.getXRot();
        this.fixedYaw = owner.getYRot();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.livingTick > MAX_TICK) {
            this.discard();
            return;
        }
        if (this.owner == null) {
            return;
        }

        if (this.intervalTick1 <= 0) {
            ItemStack stack = DanmakuTypes.random(DanmakuTypes.BIG_LASER);
            DanmakuProperties properties = stack.get(RDDataComponents.DANMAKU_PROPERTIES);
            if (properties != null) {
                stack.set(RDDataComponents.DANMAKU_PROPERTIES, properties.withSpeed(2f));
            }
            DanmakuEntity entity = DanmakuTrajectory.spawnByItemStack(
                    (ServerLevel) this.level(),
                    null,
                    this.getX(), this.getY(), this.getZ(),
                    stack,
                    fixedPitch, fixedYaw,
                    0.0f, 0.5f
            );
            entity.setOwner(this.owner);
            this.intervalTick1 = PER_INTERVAL_TICK1;
        }

        if (this.intervalTick2 <= 0) {
            this.level().playSound(
                    null, this.owner.blockPosition(),
                    SoundEventInit.BAGUA, SoundSource.PLAYERS
            );
            this.intervalTick2 = PER_INTERVAL_TICK2;
        }

        this.livingTick++;
        this.intervalTick1--;
        this.intervalTick2--;
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        this.livingTick = compoundTag.getInt("LivingTick");
        this.intervalTick1 = compoundTag.getInt("IntervalTick1");
        this.intervalTick1 = compoundTag.getInt("IntervalTick2");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("LivingTick", this.livingTick);
        compoundTag.putInt("IntervalTick1", this.intervalTick1);
        compoundTag.putInt("IntervalTick2", this.intervalTick2);
    }

    public void setTileProjectileData(List<SynchedEntityData.DataValue<?>> data, boolean initial) {
        if (initial && !this.level().isClientSide) {
            var sendBase = true;
            for (int i = 0; i < data.size(); i++) {
                var roll = data.get(i);
                if (roll.id() == DanmakuEntity.ROLL.id() && roll.serializer() == DanmakuEntity.ROLL.serializer()) {
                    float base = (float) roll.value();
                    Quaternionf from = new Quaternionf().rotateY(Mth.HALF_PI).rotateZ(base);
                    Quaternionf to = new Quaternionf().rotateY(Mth.HALF_PI).rotateZ(base + (float) (2 * Math.PI));
                    data.set(i, SynchedEntityData.DataValue.create(DisplayTrackedData.LEFT_ROTATION, from));
                    data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.RIGHT_ROTATION, to));
                    data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.INTERPOLATION_DURATION, 20));
                    sendBase = false;
                    break;
                }

            }

            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.TELEPORTATION_DURATION, 3));
            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.INTERPOLATION_DURATION, 0));
            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.SCALE, new Vector3f(2f)));
            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.TRANSLATION, new Vector3f(0, -0.1f, 0)));
            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.INTERPOLATION_DURATION, 2));
            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.TELEPORTATION_DURATION, 4));
            if (sendBase) {
                data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.LEFT_ROTATION, new Quaternionf().rotateX(Mth.HALF_PI)));
            }

            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.Item.ITEM, RDItems.BAGUA_FURNACE.getDefaultInstance()));
            data.add(SynchedEntityData.DataValue.create(DisplayTrackedData.Item.ITEM_DISPLAY, ItemDisplayContext.GROUND.getId()));
        }
    }

    @Override
    public void modifyRawTrackedData(List<SynchedEntityData.DataValue<?>> data, ServerPlayer player, boolean initial) {
        PolymerEntity.super.modifyRawTrackedData(data, player, initial);
        setTileProjectileData(data, initial);
    }

    @Override
    public void onEntityTrackerTick(Set<ServerPlayerConnection> listeners) {
        PolymerEntity.super.onEntityTrackerTick(listeners);
    }

    @Override
    public EntityType<?> getPolymerEntityType(PacketContext packetContext) {
        return EntityType.ITEM_DISPLAY;
    }
}
