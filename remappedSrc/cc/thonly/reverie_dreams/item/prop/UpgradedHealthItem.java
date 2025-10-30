package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.interfaces.ILivingEntity;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class UpgradedHealthItem extends Item {
    public UpgradedHealthItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide && world instanceof ServerLevel serverWorld) {
            ItemStack itemStack = user.getItemInHand(hand);
            world.playSound(null, user.getX(), user.getEyeY(), user.getZ(), SoundEventInit.UP, user.getSoundSource(), 1.0f, 1.0f);
            ILivingEntity modifier = (ILivingEntity) user;
            float value = modifier.getMaxHealthModifier();
            modifier.setMaxHealthModifier(value + 2);
            AttributeInstance maxHealthAttributeInstance = user.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealthAttributeInstance != null) {
                maxHealthAttributeInstance.setBaseValue(user.getMaxHealth() + 2);
            }
            user.awardStat(Stats.ITEM_USED.get(this));
            itemStack.consume(1, user);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }
}
