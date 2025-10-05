package cc.thonly.reverie_dreams.entity.misc;

import cc.thonly.reverie_dreams.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class BaguaFurnaceEntity extends Entity implements BypassHitEntity{
    private static final int MAX_TICK = 300;
    private static final int PER_INTERVAL_TICK1 = 1;
    private static final int PER_INTERVAL_TICK2 = 5 * 20;
    private final LivingEntity owner;
    private int livingTick = 0;
    private int intervalTick1 = 0;
    private int intervalTick2 = 0;

    private final float fixedPitch;
    private final float fixedYaw;


    public BaguaFurnaceEntity(EntityType<? extends Entity> entityType, World world) {
        super(entityType, world);
        this.owner = null;
        this.fixedPitch = 0;
        this.fixedYaw = 0;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {

    }

    public BaguaFurnaceEntity(ServerWorld world, @NotNull LivingEntity owner) {
        super(ModEntities.BAGUA_FURNACE_ENTITY, world);
        this.owner = owner;

        this.setPitch(owner.getPitch());
        this.setYaw(owner.getYaw());
        Vec3d v3d = owner.getPos();
        this.setPosition(v3d);
        this.setPosition(v3d.x, owner.getEyeY(), v3d.z);

        double offsetX = -Math.sin(Math.toRadians(this.getYaw()));
        double offsetZ = Math.cos(Math.toRadians(this.getYaw()));
        this.setPos(this.getX() + offsetX, this.getY(), this.getZ() + offsetZ);

        this.fixedPitch = owner.getPitch();
        this.fixedYaw = owner.getYaw();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.livingTick > MAX_TICK) {
            this.discard();
            return;
        }
        if (this.owner == null) return;

        if (this.intervalTick1 <= 0) {
            DanmakuEntity entity = DanmakuTrajectory.spawnByItemStack(
                    (ServerWorld) this.getWorld(),
                    null,
                    this.getX(), this.getY(), this.getZ(),
                    DanmakuTypes.random(DanmakuTypes.BIG_LASER),
                    fixedPitch, fixedYaw,
                    1.6f, 0f, 0.0f, 0.5f
            );
            entity.setOwner(this.owner);
            this.intervalTick1 = PER_INTERVAL_TICK1;
        }

        if (this.intervalTick2 <= 0) {
            this.getWorld().playSound(
                    null, this.owner.getBlockPos(),
                    SoundEventInit.BAGUA, SoundCategory.PLAYERS
            );
            this.intervalTick2 = PER_INTERVAL_TICK2;
        }

        this.livingTick++;
        this.intervalTick1--;
        this.intervalTick2--;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readCustomData(ReadView view) {
        this.livingTick = view.getInt("LivingTick", MAX_TICK);
        this.intervalTick1 = view.getInt("IntervalTick1", PER_INTERVAL_TICK1);
        this.intervalTick1 = view.getInt("IntervalTick2", PER_INTERVAL_TICK2);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.putInt("LivingTick", this.livingTick);
        view.putInt("IntervalTick1", this.intervalTick1);
        view.putInt("IntervalTick2", this.intervalTick2);
    }

}
