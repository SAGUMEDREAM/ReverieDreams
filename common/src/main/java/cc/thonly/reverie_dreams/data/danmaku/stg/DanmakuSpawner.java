package cc.thonly.reverie_dreams.data.danmaku.stg;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.DanmakuMotion;
import cc.thonly.reverie_dreams.data.danmaku.stg.runtime.DanmakuDirector;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import net.minecraft.core.component.DataComponents;
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
        if (!DanmakuDirector.consumeSpawnBudget()) {
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
        float scale = type.getScale() * (spec.scale() > 0f ? spec.scale() : 1f);
        DanmakuProperties properties = type.createDanmakuProperties()
                .withSpeed(speed)
                .withScale(scale);
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
        if (spec.lifetime() > 0) {
            entity.setDuration(spec.lifetime());
        }
        world.addFreshEntity(entity);
        return entity;
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
        if (!DanmakuDirector.consumeSpawnBudget()) {
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
        world.addFreshEntity(entity);
        return entity;
    }

    public static int randomColor(Random random) {
        int red = 128 + random.nextInt(128);
        int green = 128 + random.nextInt(128);
        int blue = 128 + random.nextInt(128);
        return (red << 16) | (green << 8) | blue;
    }
}
