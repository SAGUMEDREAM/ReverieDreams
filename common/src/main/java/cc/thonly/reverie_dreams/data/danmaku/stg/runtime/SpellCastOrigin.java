package cc.thonly.reverie_dreams.data.danmaku.stg.runtime;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * A cast follows its emitter while it is present, then keeps its last pose in the casting world.
 */
public final class SpellCastOrigin {
    private static final double BODY_MUZZLE_HEIGHT = 0.55D;

    private final ServerLevel world;
    @Nullable
    private final Entity entity;
    private final Vec3 offset;
    private final boolean followBody;
    private Vec3 position;
    private Vec3 feet;
    private Vec3 sourcePosition;
    private float pitch;
    private float yaw;
    private double height;
    @Nullable
    private SpellAimFrame aimFrame;
    private boolean detached;

    public SpellCastOrigin(ServerLevel world, @Nullable Entity entity, Vec3 initialPosition) {
        this(world, entity, initialPosition, entity == null ? initialPosition : entity.getEyePosition(), false);
    }

    public static SpellCastOrigin fromPlayer(ServerLevel world, Player player) {
        return fromBody(world, player);
    }

    public static SpellCastOrigin fromBody(ServerLevel world, Entity entity) {
        Vec3 muzzle = bodyMuzzle(entity);
        return new SpellCastOrigin(world, entity, muzzle, muzzle, true);
    }

    private SpellCastOrigin(ServerLevel world, @Nullable Entity entity, Vec3 initialPosition, Vec3 sourcePosition,
                            boolean followBody) {
        this.world = world;
        this.entity = entity;
        this.position = initialPosition;
        this.feet = entity == null ? initialPosition : entity.position();
        this.sourcePosition = sourcePosition;
        this.offset = initialPosition.subtract(sourcePosition);
        this.followBody = followBody;
        this.detached = entity == null;
        this.height = entity == null || entity.getBbHeight() <= 0 ? 1.8D : entity.getBbHeight();
        if (entity != null) {
            this.pitch = entity.getXRot();
            this.yaw = entity.getYRot();
        }
    }

    private static Vec3 bodyMuzzle(Entity entity) {
        // The current dimensions already include pose and, where supported, SCALE.
        return new Vec3(entity.getX(), entity.getY(BODY_MUZZLE_HEIGHT), entity.getZ());
    }

    /**
     * Returns false on defeat. Leaving the world or unloading only detaches the position.
     */
    public boolean update() {
        if (wasDefeated(this.entity)) {
            return false;
        }
        if (!this.detached && this.entity != null) {
            var removal = this.entity.getRemovalReason();
            if (removal == null && this.entity.level() == this.world) {
                this.sourcePosition = this.followBody ? bodyMuzzle(this.entity) : this.entity.getEyePosition();
                this.position = this.offset.lengthSqr() == 0 ? this.sourcePosition : this.sourcePosition.add(this.offset);
                this.feet = this.entity.position();
                this.height = this.entity.getBbHeight() <= 0 ? 1.8D : this.entity.getBbHeight();
                float newPitch = this.entity.getXRot();
                float newYaw = this.entity.getYRot();
                if (this.pitch != newPitch || this.yaw != newYaw) {
                    this.pitch = newPitch;
                    this.yaw = newYaw;
                    this.aimFrame = null;
                }
            }
            this.detached = removal != null || this.entity.level() != this.world;
        }
        return true;
    }

    public static boolean wasDefeated(@Nullable Entity entity) {
        return entity != null && (entity.getRemovalReason() == Entity.RemovalReason.KILLED
                || entity instanceof LivingEntity living && living.isDeadOrDying());
    }

    @Nullable
    public Entity entity() {
        return this.detached ? null : this.entity;
    }

    public Vec3 position() {
        return this.position;
    }

    Vec3 sourcePosition() {
        return this.sourcePosition;
    }

    public boolean hasEntityPose() {
        return this.entity != null;
    }

    public Vec3 feet() {
        return this.feet;
    }

    Vec3 bodyPosition() {
        return this.feet.add(0, this.height * BODY_MUZZLE_HEIGHT, 0);
    }

    double bodyScale() {
        return this.height / 1.8D;
    }

    public float pitch() {
        return this.pitch;
    }

    public float yaw() {
        return this.yaw;
    }

    SpellAimFrame aimFrame() {
        if (this.aimFrame == null) {
            this.aimFrame = new SpellAimFrame(this.pitch, this.yaw);
        }
        return this.aimFrame;
    }
}
