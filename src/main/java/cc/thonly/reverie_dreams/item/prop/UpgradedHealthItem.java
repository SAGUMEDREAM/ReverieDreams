package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.config.ReverieDreamsConfiguration;
import cc.thonly.reverie_dreams.inf.ILivingEntity;
import cc.thonly.reverie_dreams.registry.content.advancements.RDCriteriaTriggers;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
        if (!world.isClientSide && world instanceof ServerLevel serverWorld && user instanceof ServerPlayer player) {
            ItemStack itemStack = user.getItemInHand(hand);
            AttributeInstance maxHealthAttributeInstance = user.getAttribute(Attributes.MAX_HEALTH);
            if (maxHealthAttributeInstance != null && maxHealthAttributeInstance.getValue() + 2 > ReverieDreamsConfiguration.MAX_UPGRADED_HEALTH_VALUE) {
                player.sendSystemMessage(Component.translatable("item.action.click.upgraded_health.max_full", ReverieDreamsConfiguration.MAX_UPGRADED_HEALTH_VALUE), true);
                return InteractionResult.SUCCESS_SERVER;
            }

            world.playSound(null, user.getX(), user.getEyeY(), user.getZ(), SoundEventInit.UP, user.getSoundSource(), 1.0f, 1.0f);
            ILivingEntity modifier = (ILivingEntity) user;
            float value = modifier.getMaxHealthModifier();
            modifier.setMaxHealthModifier(value + 2);
            if (maxHealthAttributeInstance != null) {
                maxHealthAttributeInstance.setBaseValue(user.getMaxHealth() + 2);
            }
            user.awardStat(Stats.ITEM_USED.get(this));
            RDCriteriaTriggers.USE_ITEM.trigger(player, itemStack);
            itemStack.consume(1, user);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }
}
