package cc.thonly.reverie_dreams.data.danmaku.stg.runtime;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuOp;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuMath;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.DanmakuMotion;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.DyedItemColor;
import cc.thonly.reverie_dreams.entity.misc.BaseDanmakuEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Timeline operations on this cast's projectiles.
 */
final class DanmakuPatternOperations {
    private DanmakuPatternOperations() {
    }

    static void apply(DanmakuOp op, Iterable<Entity> ownedEntities, ServerLevel world, @Nullable Vec3 redirectTarget) {
        if (op.is(DanmakuOp.REDIRECT)) {
            if (redirectTarget == null) {
                return;
            }
            for (Entity entity : ownedEntities) {
                if (entity instanceof BaseDanmakuEntity bullet && bullet.isAlive()) {
                    Vec3 direction = redirectTarget.subtract(bullet.position());
                    if (direction.lengthSqr() < 1.0E-8D) {
                        continue;
                    }
                    float speed = op.value() > 0f ? op.value() : (float) bullet.currentSpeed();
                    bullet.redirect(direction.normalize().scale(speed));
                }
            }
        } else if (op.is(DanmakuOp.SPEED_SCALE)) {
            if (op.value() == 0f) {
                return;
            }
            for (Entity entity : ownedEntities) {
                if (entity instanceof BaseDanmakuEntity bullet && bullet.isAlive()) {
                    double speed = bullet.currentSpeed() * op.value();
                    bullet.redirect(bullet.currentDirection().scale(speed));
                }
            }
        } else if (op.is(DanmakuOp.STOP_AND_WHITEN) || op.is(DanmakuOp.SCATTER_ACCELERATION)) {
            for (Entity entity : ownedEntities) {
                if (!(entity instanceof BaseDanmakuEntity bullet) || !bullet.isAlive()) {
                    continue;
                }
                Vec3 origin = bullet.getDanmakuMotion().origin();
                if (op.is(DanmakuOp.STOP_AND_WHITEN)) {
                    var item = bullet.getItemStack().copy();
                    item.set(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFF));
                    bullet.setItem(item);
                    Vec3 direction = bullet.currentDirection();
                    bullet.setDanmakuMotion(DanmakuMotion.of(direction, 0, 0,
                            List.of(MotionSegment.setSpeed(0, 0))).withOrigin(origin));
                    bullet.resetMotionState();
                } else {
                    // In Perfect Freeze the old velocity is zero; a fixed random acceleration vector
                    // is equivalent to accelerating along its independently sampled direction.
                    float yaw = world.getRandom().nextFloat() * 360f;
                    bullet.setDanmakuMotion(DanmakuMotion.of(DanmakuMath.direction(0, yaw), 0, 0,
                            List.of(MotionSegment.setSpeed(0, 0), MotionSegment.accelerate(1, op.value()),
                                    MotionSegment.linear(op.duration() + 1))).withOrigin(origin));
                    bullet.resetMotionState();
                }
            }
        } else if (op.is(DanmakuOp.FREEZE_OWNED)) {
            for (Entity entity : ownedEntities) {
                if (entity instanceof BaseDanmakuEntity bullet && bullet.isAlive()) {
                    bullet.freezeMotion(op.duration());
                }
            }
        }
    }
}
