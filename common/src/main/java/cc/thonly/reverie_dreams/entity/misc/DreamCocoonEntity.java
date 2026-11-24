package cc.thonly.reverie_dreams.entity.misc;

import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class DreamCocoonEntity extends Entity {
    private static final int DURATION = 60 * 20;
    private static final float SHELL_HEALTH = 8.0F;
    private static final EntityDataAccessor<Float> WIDTH =
            SynchedEntityData.defineId(DreamCocoonEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> HEIGHT =
            SynchedEntityData.defineId(DreamCocoonEntity.class, EntityDataSerializers.FLOAT);
    private long expiresAt;
    private float shellHealth = SHELL_HEALTH;

    public DreamCocoonEntity(EntityType<? extends DreamCocoonEntity> type, Level level) {
        super(type, level);
    }

    public static boolean wrap(ServerLevel level, Mob target) {
        DreamCocoonEntity shell = new DreamCocoonEntity(RDEntityTypes.DREAM_COCOON.value(), level);
        shell.entityData.set(WIDTH, target.getBbWidth() + 0.26F);
        shell.entityData.set(HEIGHT, target.getBbHeight() + 0.24F);
        shell.setPos(target.position());
        shell.expiresAt = level.getGameTime() + DURATION;
        if (!level.noBlockCollision(shell, shell.getBoundingBox()) || !level.addFreshEntity(shell)) return false;
        if (!target.startRiding(shell, true, false)) {
            shell.discard();
            return false;
        }
        target.setDeltaMovement(Vec3.ZERO);
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(WIDTH, 0.9F).define(HEIGHT, 2.0F);
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.fixed(entityData.get(WIDTH), entityData.get(HEIGHT));
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (WIDTH.equals(key) || HEIGHT.equals(key)) refreshDimensions();
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) return;
        if (getFirstPassenger() == null || !getFirstPassenger().isAlive() || level.getGameTime() >= expiresAt) {
            release();
            return;
        }
        applyGravity();
        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(getDeltaMovement().multiply(0.75, 0.98, 0.75));
    }

    @Override
    protected double getDefaultGravity() {
        return 0.08;
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction move) {
        move.accept(passenger, getX(), getY(), getZ());
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().isEmpty() && passenger instanceof Mob;
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (isRemoved() || amount <= 0) return false;
        shellHealth -= amount;
        if (shellHealth <= 0) release();
        return true;
    }

    private void release() {
        ejectPassengers();
        discard();
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        expiresAt = input.getLongOr("ExpiresAt", 0);
        shellHealth = input.getFloatOr("ShellHealth", SHELL_HEALTH);
        entityData.set(WIDTH, input.getFloatOr("ShellWidth", 0.9F));
        entityData.set(HEIGHT, input.getFloatOr("ShellHeight", 2.0F));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putLong("ExpiresAt", expiresAt);
        output.putFloat("ShellHealth", shellHealth);
        output.putFloat("ShellWidth", getBbWidth());
        output.putFloat("ShellHeight", getBbHeight());
    }
}
