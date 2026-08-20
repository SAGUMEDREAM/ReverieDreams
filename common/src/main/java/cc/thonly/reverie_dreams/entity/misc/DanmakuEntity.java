package cc.thonly.reverie_dreams.entity.misc;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("resource")
public class DanmakuEntity extends BaseDanmakuEntity {

    public DanmakuEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public DanmakuEntity(Level level) {
        super(RDEntityTypes.DANMAKU.value(), level);
    }

    public DanmakuEntity(@NotNull LivingEntity owner, Level level, ItemStack item) {
        super(RDEntityTypes.DANMAKU.value(), owner, level, item);
    }

    public DanmakuEntity(double x, double y, double z, Level level, ItemStack item) {
        super(RDEntityTypes.DANMAKU.value(), x, y, z, level, item);
    }

    public DanmakuEntity(@NotNull LivingEntity owner, double x, double y, double z, Level level, ItemStack item) {
        super(RDEntityTypes.DANMAKU.value(), x, y, z, level, item);
    }

    public static DanmakuEntity create(LivingEntity owner, ItemStack itemStack, float inaccuracy) {
        Level level = owner.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }
        return create(serverLevel, owner, itemStack, inaccuracy);
    }

    public static DanmakuEntity create(ServerLevel level, LivingEntity owner, ItemStack itemStack, float inaccuracy) {
        DanmakuProperties properties = itemStack.getOrDefault(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault()).copy();
        DanmakuEntity entity = new DanmakuEntity(owner, level, itemStack);
        float xRot = owner.getXRot();
        float yRot = owner.getYRot();
        double x = owner.getX();
        double y = owner.getEyeY();
        double z = owner.getZ();
        entity.setPos(x, y, z);
        entity.setItem(itemStack);
        entity.setDanmakuProperties(properties);
        entity.shootFromRotation(owner, xRot, yRot, 0.0f, properties.speed() / 2, inaccuracy);
        return entity;
    }

    public static DanmakuEntity create(ServerLevel level, ItemStack itemStack, BlockPos pos, float xRot, float yRot, float inaccuracy) {
        return create(
                level,
                itemStack,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                xRot,
                yRot,
                inaccuracy
        );
    }

    public static DanmakuEntity create(ServerLevel level, ItemStack itemStack, double x, double y, double z, float xRot, float yRot, float inaccuracy) {
        DanmakuProperties properties = itemStack
                .getOrDefault(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault())
                .copy();
        DanmakuEntity entity = new DanmakuEntity(x, y, z, level, itemStack);
        entity.setItem(itemStack);
        entity.setDanmakuProperties(properties);
        entity.shootFromRotation(xRot, yRot, 0f, properties.speed() / 2, inaccuracy);
        return entity;
    }

    public static DanmakuEntity create(ServerLevel level, LivingEntity owner, ItemStack itemStack, double x, double y, double z, float xRot, float yRot, float inaccuracy) {
        DanmakuEntity entity = create(level, itemStack, x, y, z, xRot, yRot, inaccuracy);
        if (owner != null) {
            entity.setOwner(owner);
        }
        return entity;
    }

    public static DanmakuEntity create(ServerLevel level, LivingEntity owner, ItemStack itemStack, float xRot, float yRot, float inaccuracy) {
        DanmakuProperties properties = itemStack
                .getOrDefault(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault())
                .copy();
        DanmakuEntity entity = new DanmakuEntity(owner.getX(), owner.getEyeY(), owner.getZ(), level, itemStack);
        entity.setOwner(owner);
        entity.setDanmakuProperties(properties);
        entity.shootFromRotation(owner, xRot, yRot, 0, properties.speed() / 2, inaccuracy);
        return entity;
    }
}
