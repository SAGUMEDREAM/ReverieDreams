package cc.thonly.reverie_dreams.entity.misc;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import cc.thonly.reverie_dreams.util.codec.UUIDCodec;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class BaguaFurnaceEntity extends Entity implements BypassHitEntity {
    private static final int MAX_TICK = 300;
    private static final int PER_INTERVAL_TICK1 = 1;
    private static final int PER_INTERVAL_TICK2 = 5 * 20;
    private UUID ownerId = UUID.randomUUID();
    @Nullable
    private LivingEntity owner;
    private int livingTick = 0;
    private int intervalTick1 = 0;
    private int intervalTick2 = 0;

    private final float fixedPitch;
    private final float fixedYaw;


    public BaguaFurnaceEntity(EntityType<? extends Entity> entityType, Level world) {
        super(entityType, world);
        this.fixedPitch = 0;
        this.fixedYaw = 0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    public BaguaFurnaceEntity(ServerLevel world, @NotNull LivingEntity owner) {
        super(RDEntityTypes.BAGUA_FURNACE, world);
        this.ownerId = owner.getUUID();

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
            if (this.owner != null) {
                entity.setOwner(this.owner);
            }
            this.intervalTick1 = PER_INTERVAL_TICK1;
        }

        if (this.intervalTick2 <= 0) {
            this.level().playSound(
                    null, this.blockPosition(),
                    SoundEventInit.BAGUA, SoundSource.PLAYERS
            );
            this.intervalTick2 = PER_INTERVAL_TICK2;
        }

        this.livingTick++;
        this.intervalTick1--;
        this.intervalTick2--;
    }

    public void setOwnerId(@NotNull UUID ownerId) {
        this.ownerId = ownerId;
        Level level = this.level();
        if (level.isClientSide()) {
            return;
        }
        Entity entity = level.getEntity(this.ownerId);
        if (entity instanceof LivingEntity livingEntity) {
            this.owner = livingEntity;
        }
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput view) {
        Optional<UUID> ownerId = view.read("OwnerId", UUIDCodec.CODEC);
        ownerId.ifPresent(this::setOwnerId);
        this.livingTick = view.getIntOr("LivingTick", MAX_TICK);
        this.intervalTick1 = view.getIntOr("IntervalTick1", PER_INTERVAL_TICK1);
        this.intervalTick1 = view.getIntOr("IntervalTick2", PER_INTERVAL_TICK2);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput view) {
        view.store("OwnerId", UUIDCodec.CODEC, this.ownerId);
        view.putInt("LivingTick", this.livingTick);
        view.putInt("IntervalTick1", this.intervalTick1);
        view.putInt("IntervalTick2", this.intervalTick2);
    }

}
