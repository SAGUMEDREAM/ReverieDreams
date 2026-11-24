package cc.thonly.reverie_dreams.data.danmaku.trajectory;

import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectoryConfig;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.DanmakuMotion;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.runtime.SpellCastOrigin;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The authored volley and motion for one player trajectory template.
 */
public final class PlayerTrajectory extends DanmakuTrajectory {
    private final PlayerTrajectoryType type;

    public PlayerTrajectory(PlayerTrajectoryType type) {
        this.type = type;
    }

    public PlayerTrajectoryType type() {
        return this.type;
    }

    public void fire(ServerLevel world, LivingEntity owner, InteractionHand hand, ItemStack stack) {
        SpellCastOrigin origin = SpellCastOrigin.fromBody(world, owner);
        this.fire(world, owner, hand, stack, origin.position(), new Frame(origin.pitch(), origin.yaw()));
    }

    @Override
    public void run(ServerLevel world, @Nullable LivingEntity owner, ItemStack stack, double x, double y, double z,
                    float pitch, float yaw, float divergence, float offsetDist, IDanmakuItem item,
                    DanmakuTrajectoryConfig config) {
        this.fire(world, owner, null, stack, new Vec3(x, y, z), new Frame(pitch, yaw));
    }

    private void fire(ServerLevel world, @Nullable LivingEntity owner, @Nullable InteractionHand hand, ItemStack stack,
                      Vec3 source, Frame aim) {
        var properties = stack.get(RDDataComponentTypes.DANMAKU_PROPERTIES.value());
        if (properties == null || properties.count() <= 0 || properties.actualSpeed() <= 0) {
            return;
        }
        float speed = properties.actualSpeed();
        float damage = properties.damage() * Math.min(2, properties.count());
        switch (this.type) {
            case BEAT_BURST, LANE_SWEEP -> {
                boolean sweep = this.type == PlayerTrajectoryType.LANE_SWEEP;
                PlayerTrajectoryCast.start(world, owner, hand, stack, source,
                        List.of(aim.direction(sweep ? -12 : 0), aim.forward(), aim.direction(sweep ? 12 : 0)),
                        speed, damage / 3);
            }
            case RANGE_CONVERGENCE -> {
                Vec3 intersection = source.add(aim.forward().scale(10));
                for (int side : new int[]{-1, 1}) {
                    Vec3 position = source.add(aim.right().scale(side * 0.7));
                    Vec3 velocity = intersection.subtract(position).normalize().scale(speed);
                    PlayerTrajectoryFlight.launch(world, owner, stack, source, position,
                            DanmakuMotion.straight(velocity), damage * 0.5f, null);
                }
            }
            default -> {
                float adjustedDamage = switch (this.type) {
                    case NEEDLE_PIERCE -> damage * 0.55f;
                    case SHORT_GUIDANCE, PROXIMITY_SEED -> damage * 0.75f;
                    case INTERCEPT_SHOT -> damage * 0.40f;
                    default -> damage;
                };
                var flight = PlayerTrajectoryFlight.create(this.type, world, owner, source, aim);
                if (this.type == PlayerTrajectoryType.STAR_FORK) {
                    adjustedDamage *= 0.5f;
                }
                PlayerTrajectoryFlight.launch(world, owner, stack, source, source,
                        this.motion(aim, speed), adjustedDamage, flight);
            }
        }
    }

    DanmakuMotion motion(Frame aim, float speed) {
        Vec3 forward = aim.forward().scale(speed);
        return switch (this.type) {
            case RAMP_SHOT -> DanmakuMotion.of(forward.scale(0.45), 0, 0, List.of(
                    MotionSegment.accelerate(0, (Math.min(4.0f, speed * 1.2f) - speed * 0.45f) / 8),
                    MotionSegment.linear(8),
                    MotionSegment.setSpeed(8, Math.min(4.0f, speed * 1.2f))));
            case LOB_SHOT -> DanmakuMotion.of(aim.horizontal().scale(speed * 0.76)
                    .add(0, Math.max(0.25, 0.53 + aim.forward().y * 0.38), 0), 0, 0, List.of(
                    MotionSegment.accelerateVector(0, new Vec3(0, -0.065, 0), -1)));
            case WEAVE_SHOT -> DanmakuMotion.of(forward, 0, 0, List.of(
                    MotionSegment.lateralWave(aim.right(), Vec3.ZERO, 0.75f)));
            case RETURN_ARC -> DanmakuMotion.of(forward.scale(0.8), 0, 0, List.of(
                    MotionSegment.rotate(7, 20f, aim.up()), MotionSegment.linear(16)));
            case CORKSCREW_SHOT -> DanmakuMotion.of(forward, 0, 0, List.of(
                    MotionSegment.lateralWave(aim.right(), aim.up(), 0.45f)));
            case PLUNGE_SHOT -> {
                double lift = 0.72 + aim.forward().y * 0.32;
                float turn = 63f + (float) Math.toDegrees(Math.atan2(lift, speed * 0.7));
                yield DanmakuMotion.of(aim.horizontal().scale(speed * 0.7).add(0, lift, 0), 0, 0, List.of(
                        MotionSegment.turn(8, turn, aim.right()), MotionSegment.setSpeed(8, speed)));
            }
            case GROUND_SKIMMER -> DanmakuMotion.straight(aim.horizontal().scale(speed * 0.7).add(0, -0.16, 0));
            case PROXIMITY_SEED -> DanmakuMotion.straight(aim.horizontal().scale(speed * 0.58).add(0, -0.30, 0));
            default -> DanmakuMotion.straight(forward);
        };
    }

    public record Frame(Vec3 forward, Vec3 right, Vec3 up, Vec3 horizontal) {
        public Frame(float pitch, float yaw) {
            this(DanmakuTrajectory.direction(pitch, yaw), horizontalRight(yaw), DanmakuTrajectory.direction(pitch, yaw).cross(horizontalRight(yaw)),
                    DanmakuTrajectory.direction(0, yaw));
        }

        private static Vec3 horizontalRight(float yaw) {
            double angle = Math.toRadians(yaw);
            return new Vec3(Math.cos(angle), 0, Math.sin(angle));
        }

        Vec3 direction(double degrees) {
            double angle = Math.toRadians(degrees);
            return this.forward.scale(Math.cos(angle)).add(this.right.scale(Math.sin(angle)));
        }
    }
}
