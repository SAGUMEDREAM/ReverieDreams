package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.interfaces.ILivingEntity;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class UpgradedHealthItem extends Item {
    public UpgradedHealthItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            ItemStack itemStack = user.getStackInHand(hand);
            world.playSound(null, user.getX(), user.getEyeY(), user.getZ(), SoundEventInit.UP, user.getSoundCategory(), 1.0f, 1.0f);
            ILivingEntity modifier = (ILivingEntity) user;
            float value = modifier.getMaxHealthModifier();
            modifier.setMaxHealthModifier(value + 2);
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            itemStack.decrementUnlessCreative(1, user);
            return ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.SUCCESS;
    }
}
