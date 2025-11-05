package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class VaisravanasPagodaItem extends Item {

    public VaisravanasPagodaItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide && world instanceof ServerLevel serverWorld) {
            ItemStack itemStack = user.getItemInHand(hand);
            float pitch = user.getXRot();
            float yaw = user.getYRot();
            for (int i = 0; i < 3; i++) {
                DanmakuEntity entity = DanmakuTrajectory.spawnByItemStack(
                        serverWorld, user, user.getX(), user.getY(), user.getZ(),
                        DanmakuTypes.random(DanmakuTypes.LASER),
                        pitch, yaw,
                        0.0f, 1.5f
                );
                entity.setOnHitEffect((livingEntity, damage) -> {
                    livingEntity.setRemainingFireTicks(20 * 5);
                });
            }
            ItemCooldowns itemCooldownManager = user.getCooldowns();
            itemCooldownManager.addCooldown(itemStack, 20 * 10);
            itemStack.hurtWithoutBreaking(1, user);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEventInit.FIRE, SoundSource.NEUTRAL, 1f, 1.0f);

            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }
}
