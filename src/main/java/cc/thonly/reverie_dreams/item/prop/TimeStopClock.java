package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.server.DelayedTask;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickManager;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class TimeStopClock extends Item {
    public static DelayedTask TASK = null;

    public TimeStopClock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        MinecraftServer server = world.getServer();
        if (!world.isClient && server != null) {
            ItemStack itemStack = user.getStackInHand(hand);
            ItemCooldownManager itemCooldownManager = user.getItemCooldownManager();
            itemCooldownManager.set(itemStack, 20 * 10);
            ServerTickManager tickManager = server.getTickManager();
            boolean freeze = tickManager.isFrozen();
            if (!freeze) {
                world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 0.5f, 1);
            } else {
                world.playSound(null, user.getBlockPos(), SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.BLOCKS, 0.5f, 1);
            }
            tickManager.setFrozen(!freeze);
            if (!user.isInCreativeMode()) {
                itemStack.damage(1, user);
                if (itemStack.isDamageable() && itemStack.getDamage() >= itemStack.getMaxDamage()) {
                    itemStack.decrement(1);
                }
            }
            if (TASK != null) {
                TASK.stop();
            }
            TASK = DelayedTask.create(server, 20 * 20, () -> {
                if (tickManager.isFrozen()) {
                    tickManager.setFrozen(false);
                }
                TASK = null;
            });
            return ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.SUCCESS;
    }
}
