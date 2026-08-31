package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.data.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.item.base.IDanmakuItem;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.server.DelayedTask;
import cc.thonly.reverie_dreams.sound.RDSoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TrumpetGun extends Item implements IDanmakuItem {

    public TrumpetGun(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (!world.isClientSide() && user instanceof ServerPlayer player) {
            ServerLevel serverWorld = (ServerLevel) world;
            float pitch = user.getXRot();
            float yaw = user.getYRot();
            DelayedTask.repeat(world.getServer(), 3, 2, () -> {
                DanmakuEntity entity = DanmakuTrajectory.spawnByItemStack(serverWorld, user, user.getX(), user.getY(), user.getZ(), DanmakuTypes.random(DanmakuTypes.BULLET).create(), pitch, yaw, 0.0f, 1.5f);
                entity.setDanmakuProperties(entity.getDanmakuProperties().withSpeed(2.3f));
                world.playSound(null, user.getOnPos(), RDSoundEvents.FIRE.value(), SoundSource.HOSTILE, 1.0f, 1.0f);
            });
            ItemCooldowns itemCooldownManager = player.getCooldowns();
            itemCooldownManager.addCooldown(stack, 35);
            if (!player.hasInfiniteMaterials()) {
                stack.hurtWithoutBreaking(1, player);
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return super.use(world, user, hand);
    }

}
