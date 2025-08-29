package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.item.danmaku.AbstractDanmakuItem;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;

@FunctionalInterface
public interface MobDanmakuShooter {
    MobDanmakuShooter DEFAULT = new MobDanmakuShooter() {
        @Override
        public void fire(LivingEntity self, Entity target, ServerWorld world) {
            ItemStack stack = DanmakuTypes.random(DanmakuTypes.FIREBALL_GLOWY);
            float[] pitchYaw = getPitchYaw(self, target);
            spawn(world, self, stack, pitchYaw[0], pitchYaw[1] - 15.0f, 1.0f, 0f, 0.35f);
            spawn(world, self, stack, pitchYaw[0], pitchYaw[1], 1.05f, 0f, 0.35f);
            spawn(world, self, stack, pitchYaw[0], pitchYaw[1] + 15.0f, 1.0f, 0f, 0.35f);
        }
    };

    void fire(LivingEntity self, Entity target, ServerWorld world);

    default void sound(LivingEntity self) {
        self.playSound(SoundEventInit.FIRE);
    }

    static float[] getPitchYaw(Entity self, Entity target) {
        double dx = target.getX() - self.getX();
        double dy = target.getY() - self.getEyeY();
        double dz = target.getZ() - self.getZ();

        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);


        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, horizontalDistance)));
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx))) - 90.0f;
        return new float[]{pitch, yaw};
    }

    static DanmakuEntity spawn(ServerWorld world, LivingEntity entity, ItemStack stack, float pitch, float yaw, float speed, float divergence, float offsetDist) {
        Item item = stack.getItem();
        if (item instanceof AbstractDanmakuItem danmakuItem) {
            DanmakuEntity danmakuEntity = new DanmakuEntity(
                    (LivingEntity) entity,
                    world,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    stack.copy(),
                    pitch,
                    yaw,
                    speed,
                    0f,
                    divergence,
                    offsetDist
            );
            world.spawnEntity(danmakuEntity);
            return danmakuEntity;
        }
        return null;
    }
}
