package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.server.DelayedTask;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class HoraiDamaNoEdaItem extends SwordItem {
    public static final ToolMaterial HORAI_DAMA_NO_EDA = new ToolMaterial(ModTags.BlockTypeTag.EMPTY, 300, 4.5f, 3.5f, 5, ModTags.ItemTypeTag.ORB_BLOCK);

    public HoraiDamaNoEdaItem(float attackDamage, float attackSpeed, Settings settings) {
        super(HORAI_DAMA_NO_EDA, attackDamage + 1f, attackSpeed - 2.4f, settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient() && user instanceof ServerPlayerEntity player) {
            ServerWorld serverWorld = (ServerWorld) world;

            float pitch = user.getPitch();
            float yaw = user.getYaw();
            DelayedTask.repeat(world.getServer(), 3, 5, () -> {
                DanmakuEntity center = DanmakuTrajectory.spawnByItemStack(serverWorld, user, user.getX(), user.getY(), user.getZ(), DanmakuTypes.random(DanmakuTypes.FIREBALL), pitch, yaw, 1.4f, 0f, 0.0f, 0.8f);
                DanmakuEntity left = DanmakuTrajectory.spawnByItemStack(serverWorld, user, user.getX(), user.getY(), user.getZ(), DanmakuTypes.random(DanmakuTypes.FIREBALL), pitch, yaw - 10, 1.4f, 0f, 0.0f, 0.8f);
                DanmakuEntity right = DanmakuTrajectory.spawnByItemStack(serverWorld, user, user.getX(), user.getY(), user.getZ(), DanmakuTypes.random(DanmakuTypes.FIREBALL), pitch, yaw+ 10, 1.4f, 0f, 0.0f, 0.8f);

                center.playSound(SoundEventInit.FIRE, 1.0f, 1.0f);
            });

            ItemCooldownManager itemCooldownManager = player.getItemCooldownManager();
            itemCooldownManager.set(stack, 20 * 6);
            if (!player.isInCreativeMode()) {
                stack.damage(1, player);
            }
            return ActionResult.SUCCESS_SERVER;
        }
        return super.use(world, user, hand);
    }
}
