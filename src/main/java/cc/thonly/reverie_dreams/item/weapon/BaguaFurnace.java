package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.server.DelayedTask;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class BaguaFurnace extends Item {
    public BaguaFurnace(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient() && user instanceof ServerPlayerEntity player) {
            ServerWorld serverWorld = (ServerWorld) world;
            DelayedTask.repeat(world.getServer(), 200, 1, () -> {
                if (user.getStackInHand(hand).getItem() != this) {
                    return;
                }
                float pitch = user.getPitch();
                float yaw = user.getYaw();
                DanmakuEntity entity = DanmakuTrajectory.spawnByItemStack(
                        serverWorld, user, user.getX(), user.getY(), user.getZ(),
                        DanmakuTypes.random(DanmakuTypes.BIG_LASER),
                        pitch, yaw, 1.6f, 0f, 0.0f, 1.5f
                );
            });
            serverWorld.playSound(null, user.getBlockPos(), SoundEventInit.BAGUA, SoundCategory.PLAYERS);
            DelayedTask.repeat(world.getServer(), 1, 5 * 20, () -> {
                if (user.getStackInHand(hand).getItem() != this) {
                    return;
                }
                serverWorld.playSound(null, user.getBlockPos(), SoundEventInit.BAGUA, SoundCategory.PLAYERS);
            });

            HungerManager hungerManager = player.getHungerManager();
            hungerManager.add(-6, -6);

            ItemCooldownManager itemCooldownManager = player.getItemCooldownManager();
            itemCooldownManager.set(stack, 20 * 40);
            if (!player.isInCreativeMode()) {
                stack.damage(1, player);
            }
            return ActionResult.SUCCESS_SERVER;
        }
        return super.use(world, user, hand);
    }
}
