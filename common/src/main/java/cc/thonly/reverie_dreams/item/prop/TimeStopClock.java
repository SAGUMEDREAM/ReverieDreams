package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.server.DelayedTask;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TimeStopClock extends Item {
    public static DelayedTask TASK = null;

    public TimeStopClock(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        MinecraftServer server = world.getServer();
        if (!world.isClientSide() && server != null) {
            ItemStack itemStack = user.getItemInHand(hand);
            ItemCooldowns itemCooldownManager = user.getCooldowns();
            itemCooldownManager.addCooldown(itemStack, 20 * 10);
            ServerTickRateManager tickManager = server.tickRateManager();
            boolean freeze = tickManager.isFrozen();
            if (!freeze) {
                world.playSound(null, user.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.5f, 1);
            } else {
                world.playSound(null, user.blockPosition(), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.5f, 1);
            }
            tickManager.setFrozen(!freeze);
            if (!user.hasInfiniteMaterials()) {
                itemStack.hurtWithoutBreaking(1, user);
                if (itemStack.isDamageableItem() && itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
                    itemStack.shrink(1);
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
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }
}
