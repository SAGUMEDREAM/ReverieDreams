package cc.thonly.reverie_dreams.api.entity.type;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.item.danmaku.AbstractDanmakuItem;
import cc.thonly.reverie_dreams.item.prop.Knife;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDEntityHolderItems;
import cc.thonly.reverie_dreams.registry.content.sound.RDSoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

@FunctionalInterface
public interface DanmakuShooter {
    DanmakuShooter DEFAULT = (self, target, world) -> {
        ItemStack stack = DanmakuTypes.random(DanmakuTypes.FIREBALL_GLOWY).create();
        float[] pitchYaw = getPitchYaw(self, target);
        spawn(world, self, stack, pitchYaw[0], pitchYaw[1] - 15.0f, 1.0f, 0f, 0.35f);
        spawn(world, self, stack, pitchYaw[0], pitchYaw[1], 1.05f, 0f, 0.35f);
        spawn(world, self, stack, pitchYaw[0], pitchYaw[1] + 15.0f, 1.0f, 0f, 0.35f);
    };

    void fire(LivingEntity self, Entity target, ServerLevel world);

    static void soundDefault(LivingEntity self) {
        self.makeSound(RDSoundEvents.FIRE.value());
    }

    default void sound(LivingEntity self) {
        self.makeSound(RDSoundEvents.FIRE.value());
    }

    static float[] getPitchYaw(Entity self, Entity target) {
        double dx = target.getX() - self.getX();
        double dy = target.getY() - self.getEyeY() + 0.45;
        double dz = target.getZ() - self.getZ();

        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);


        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, horizontalDistance)));
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx))) - 90.0f;
        return new float[]{pitch, yaw};
    }

    /**
     * Aims at the target's center with a simple velocity lead. Used by shooters
     * that fire after a delay, where the old snapshot angle would miss moving
     * targets.
     *
     * @param projectileSpeed actual blocks-per-tick speed of the projectile
     */
    static float[] getPitchYaw(Entity self, Entity target, float projectileSpeed) {
        Vec3 selfPos = new Vec3(self.getX(), self.getEyeY(), self.getZ());
        Vec3 targetPos = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        double distance = selfPos.distanceTo(targetPos);
        double travelTicks = Math.min(20.0D, distance / Math.max(0.1D, projectileSpeed));
        Vec3 predicted = targetPos.add(target.getDeltaMovement().scale(travelTicks));

        double dx = predicted.x - selfPos.x;
        double dy = predicted.y - selfPos.y;
        double dz = predicted.z - selfPos.z;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontalDistance));
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        return new float[]{pitch, yaw};
    }

    static DanmakuEntity spawn(ServerLevel world, LivingEntity entity, ItemStack stack, float pitch, float yaw, float speed, float divergence, float offsetDist, StackModifier modifier) {
        ItemStack itemStack = modifier.get(stack.copy());
        return spawn(world, entity, itemStack, pitch, yaw, speed, divergence, offsetDist);
    }

    static DanmakuEntity spawn(ServerLevel world, LivingEntity entity, ItemStack stack, float pitch, float yaw, float speed, float divergence, float offsetDist) {
        Item item = stack.getItem();
        DanmakuProperties properties = stack.getOrDefault(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault());
        properties = properties.withSpeed(speed);
        if (item instanceof AbstractDanmakuItem danmakuItem) {
            DanmakuEntity danmakuEntity = DanmakuEntity.create(
                    world, entity, stack.copy(),
                    entity.getX(), entity.getEyeY(), entity.getZ(),
                    pitch,
                    yaw,
                    divergence
            );
            danmakuEntity.setDanmakuProperties(properties.copy());
            world.addFreshEntity(danmakuEntity);
            return danmakuEntity;
        }
        if (item instanceof Knife || item == RDEntityHolderItems.KNIFE_DISPLAY.asItem()) {
            DanmakuEntity danmakuEntity = DanmakuEntity.create(
                    world, entity, stack.copy(),
                    entity.getX(), entity.getEyeY(), entity.getZ(),
                    pitch,
                    yaw,
                    divergence
            );
            danmakuEntity.setDanmakuProperties(properties.copy());
            world.addFreshEntity(danmakuEntity);
            return danmakuEntity;
        }
        return null;
    }

    @FunctionalInterface
    public interface StackModifier {
        ItemStack get(ItemStack origin);
    }
}
