package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class VaisravanasPagodaItem extends Item {

    public VaisravanasPagodaItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            ItemStack itemStack = user.getStackInHand(hand);
            float pitch = user.getPitch();
            float yaw = user.getYaw();
            for (int i = 0; i < 3; i++) {
                DanmakuEntity entity = DanmakuTrajectory.spawnByItemStack(
                        serverWorld, user, user.getX(), user.getY(), user.getZ(),
                        DanmakuTypes.random(DanmakuTypes.LASER),
                        pitch, yaw, 1.6f, 0f, 0.0f, 1.5f
                );
                entity.setOnHitEffect((livingEntity, damage) -> {
                    livingEntity.setFireTicks(20 * 5);
                });
            }
            ItemCooldownManager itemCooldownManager = user.getItemCooldownManager();
            itemCooldownManager.set(itemStack, 20 * 10);
            itemStack.damage(1, user);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEventInit.FIRE, SoundCategory.NEUTRAL, 1f, 1.0f);

            return ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.SUCCESS;
    }
}
