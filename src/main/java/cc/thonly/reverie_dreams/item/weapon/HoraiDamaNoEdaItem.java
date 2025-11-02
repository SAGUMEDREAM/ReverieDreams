package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.server.DelayedTask;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;

public class HoraiDamaNoEdaItem extends SwordItem {
    public static final ToolMaterial HORAI_DAMA_NO_EDA = new ToolMaterial(ModTags.BlockTypeTag.EMPTY, 300, 4.5f, 3.5f, 5, ModTags.ItemTypeTag.ORB_BLOCK);

    public HoraiDamaNoEdaItem(float attackDamage, float attackSpeed, Properties settings) {
        super(HORAI_DAMA_NO_EDA, attackDamage + 1f, attackSpeed - 2.4f, settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (!world.isClientSide() && user instanceof ServerPlayer player) {
            ServerLevel serverWorld = (ServerLevel) world;

            float pitch = user.getXRot();
            float yaw = user.getYRot();
            DelayedTask.repeat(world.getServer(), 3, 5, () -> {
                DanmakuEntity center = DanmakuTrajectory.spawnByItemStack(serverWorld, user, user.getX(), user.getY(), user.getZ(), DanmakuTypes.random(DanmakuTypes.FIREBALL), pitch, yaw, 0.0f, 0.8f);
                DanmakuEntity left = DanmakuTrajectory.spawnByItemStack(serverWorld, user, user.getX(), user.getY(), user.getZ(), DanmakuTypes.random(DanmakuTypes.FIREBALL), pitch, yaw - 10, 0.0f, 0.8f);
                DanmakuEntity right = DanmakuTrajectory.spawnByItemStack(serverWorld, user, user.getX(), user.getY(), user.getZ(), DanmakuTypes.random(DanmakuTypes.FIREBALL), pitch, yaw+ 10, 0.0f, 0.8f);

                center.playSound(SoundEventInit.FIRE, 1.0f, 1.0f);
            });

            ItemCooldowns itemCooldownManager = player.getCooldowns();
            itemCooldownManager.addCooldown(stack, 20 * 6);
            if (!player.hasInfiniteMaterials()) {
                stack.hurtWithoutBreaking(1, player);
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return super.use(world, user, hand);
    }
}
