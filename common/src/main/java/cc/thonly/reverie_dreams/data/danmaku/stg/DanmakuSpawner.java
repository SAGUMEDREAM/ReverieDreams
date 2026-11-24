package cc.thonly.reverie_dreams.data.danmaku.stg;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.DanmakuMotion;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.entity.misc.DanmakuLaserEntity;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * STG 弹幕生成器：把 {@link DanmakuBulletSpec} 转成带运动模型的 {@link DanmakuEntity}。
 */
public final class DanmakuSpawner {
    private DanmakuSpawner() {
    }

    @Nullable
    public static DanmakuEntity spawn(ServerLevel world, @Nullable Entity owner, Vec3 position,
                                      float pitch, float yaw, DanmakuBulletSpec spec, float speedScale) {
        if (!canSpawnBullet(world, position)) {
            return null;
        }
        DanmakuType type = BuiltInRegistryProviders.DANMAKU_TYPE.getValue(spec.type());
        if (type == null) {
            type = DanmakuTypes.AMULET;
        }

        ItemStack stack = type.getItemHolder().asItem().getDefaultInstance();
        int color = spec.color();
        if (spec.randomColor() && color < 0) {
            color = randomColor(ThreadLocalRandom.current());
        }
        if (color >= 0) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
        }

        float speed = (spec.speed() > 0f ? spec.speed() : type.actualSpeed()) * speedScale;
        float scaleMultiplier = spec.scale() > 0f ? spec.scale() : 1f;
        float scale = type.getScale() * scaleMultiplier;
        DanmakuProperties properties = type.createDanmakuProperties()
                .withSpeed(speed)
                .withScale(scale)
                .withHitboxScale(type.effectiveHitboxScale() * scaleMultiplier);
        if (spec.damage() > 0f) {
            properties = properties.withDamage(spec.damage());
        }

        DanmakuEntity entity = DanmakuEntity.create(world, stack, position.x, position.y, position.z, pitch, yaw, 0f);
        if (entity == null) {
            return null;
        }
        if (owner != null) {
            entity.setOwner(owner);
        }
        entity.setDanmakuProperties(properties);
        entity.setDanmakuMotion(buildMotion(spec.motion(), pitch, yaw, speed, position));
        entity.setTrajectoryInterceptable(spec.interceptable() && !type.isDeleteFromList());
        if (spec.lifetime() > 0) {
            entity.setDuration(spec.lifetime());
        }
        return world.addFreshEntity(entity) ? entity : null;
    }

    /**
     * 按裸参数直接生成一发直线子弹（SPLIT 等运行时派生弹用，避免伪造 ItemStack）。
     *
     * @param typeId          弹幕类型；{@code null} 时回退 AMULET
     * @param color           染色，{@code -1} 表示类型默认
     * @param scaleMultiplier 缩放倍率，{@code <= 0} 表示 1
     * @param speed           出膛速度（格/tick），{@code <= 0} 表示类型默认速度
     * @param lifetime        寿命（tick），{@code <= 0} 表示默认
     */
    @Nullable
    public static DanmakuEntity spawn(ServerLevel world, @Nullable Entity owner, Vec3 position,
                                      float pitch, float yaw, @Nullable Identifier typeId, int color,
                                      float scaleMultiplier, float speed, int lifetime) {
        if (!canSpawnBullet(world, position)) {
            return null;
        }
        DanmakuType type = typeId == null ? null : BuiltInRegistryProviders.DANMAKU_TYPE.getValue(typeId);
        if (type == null) {
            type = DanmakuTypes.AMULET;
        }
        ItemStack stack = type.getItemHolder().asItem().getDefaultInstance();
        if (color >= 0) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
        }
        float actualSpeed = speed > 0f ? speed : type.actualSpeed();
        float multiplier = scaleMultiplier > 0f ? scaleMultiplier : 1f;
        float scale = type.getScale() * multiplier;
        DanmakuProperties properties = type.createDanmakuProperties()
                .withSpeed(actualSpeed)
                .withScale(scale)
                .withHitboxScale(type.effectiveHitboxScale() * multiplier);
        DanmakuEntity entity = DanmakuEntity.create(world, stack, position.x, position.y, position.z, pitch, yaw, 0f);
        if (entity == null) {
            return null;
        }
        if (owner != null) {
            entity.setOwner(owner);
        }
        entity.setDanmakuProperties(properties);
        entity.setDanmakuMotion(buildMotion(DanmakuMotion.STRAIGHT, pitch, yaw, actualSpeed, position));
        if (lifetime > 0) {
            entity.setDuration(lifetime);
        }
        return world.addFreshEntity(entity) ? entity : null;
    }

    /**
     * 生成一条可碰撞激光。
     *
     * @return false 表示起点不在可运行实体的区块，或世界拒绝生成
     */
    public static boolean spawnLaser(ServerLevel world, Entity owner, Vec3 start, float pitch, float yaw,
                                     float length, float width, int warnTicks, int activeTicks, DanmakuType type) {
        return spawnBeam(world, owner, start,
                new DanmakuLaser(0, pitch, yaw, length, width, warnTicks, activeTicks, 0), type) != null;
    }

    @Nullable
    public static DanmakuLaserEntity spawnBeam(ServerLevel world, @Nullable Entity owner, Vec3 start,
                                               DanmakuLaser cue, DanmakuType type) {
        if (!world.isPositionEntityTicking(BlockPos.containing(start))) {
            return null;
        }
        DanmakuLaserEntity laser = new DanmakuLaserEntity(RDEntityTypes.DANMAKU_LASER.value(), world);
        laser.setPos(start.x, start.y, start.z);
        laser.setOwnerUuid(owner == null ? null : owner.getUUID());
        laser.setRotation(cue.pitch(), cue.yaw());
        laser.setBeam(cue.length(), cue.width());
        laser.setTimings(cue.warnTicks(), cue.activeTicks());
        laser.setSweep(cue.sweepPerTick(), cue.sweepHalfAngle());
        ItemStack display = type.getItemHolder().asItem().getDefaultInstance();
        if (cue.color() >= 0) {
            display.set(DataComponents.DYED_COLOR, new DyedItemColor(cue.color()));
        }
        laser.setDisplayItem(display);
        return world.addFreshEntity(laser) ? laser : null;
    }

    @Nullable
    public static DanmakuLaserEntity spawnMovingBeam(ServerLevel world, @Nullable Entity owner, Vec3 start,
                                                     Vec3 direction, MovingLaserSpec spec) {
        if (!canSpawnBullet(world, start)) {
            return null;
        }
        float[] angles = DanmakuMath.pitchYaw(Vec3.ZERO, direction);
        var beam = new DanmakuLaserEntity(RDEntityTypes.DANMAKU_LASER.value(), world);
        beam.setPos(start);
        beam.setOwnerUuid(owner == null ? null : owner.getUUID());
        beam.setRotation(angles[0], angles[1]);
        beam.setMovingBeam(spec);
        ItemStack display = DanmakuTypes.SHORT_LASER.getItemHolder().asItem().getDefaultInstance();
        if (spec.color() >= 0) {
            display.set(DataComponents.DYED_COLOR, new DyedItemColor(spec.color()));
        }
        beam.setDisplayItem(display);
        return world.addFreshEntity(beam) ? beam : null;
    }

    private static boolean canSpawnBullet(ServerLevel world, Vec3 position) {
        return world.isPositionEntityTicking(BlockPos.containing(position));
    }

    public static DanmakuMotion buildMotion(DanmakuMotion motion, float pitch, float yaw, float speed, Vec3 origin) {
        return motion.withVelocity(DanmakuMath.direction(pitch, yaw).scale(speed)).withOrigin(origin);
    }

    /**
     * 由弹幕物品堆直接生成一发子弹（供旧物品路径复用新运动模型时使用）。
     */
    @Nullable
    public static DanmakuEntity spawnFromStack(ServerLevel world, @Nullable Entity owner, Vec3 position,
                                               float pitch, float yaw, ItemStack stack) {
        if (!canSpawnBullet(world, position)) {
            return null;
        }
        DanmakuProperties properties = stack.getOrDefault(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault());
        // 旧组件没有 speed_version，actualSpeed() 会自动换算为旧版实际速度
        float speed = properties.actualSpeed();
        ItemStack copy = stack.copy();
        DanmakuEntity entity = DanmakuEntity.create(world, copy, position.x, position.y, position.z, pitch, yaw, 0f);
        if (entity == null) {
            return null;
        }
        if (owner != null) {
            entity.setOwner(owner);
        }
        entity.setDanmakuProperties(properties.copy());
        entity.setDanmakuMotion(DanmakuMotion.straight(DanmakuMath.direction(pitch, yaw).scale(speed)).withOrigin(position));
        return world.addFreshEntity(entity) ? entity : null;
    }

    public static int randomColor(Random random) {
        int red = 128 + random.nextInt(128);
        int green = 128 + random.nextInt(128);
        int blue = 128 + random.nextInt(128);
        return (red << 16) | (green << 8) | blue;
    }
}
