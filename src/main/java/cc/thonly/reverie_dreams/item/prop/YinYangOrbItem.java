package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.registry.content.advancements.RDCriteriaTriggers;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class YinYangOrbItem extends Item {

    public YinYangOrbItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            var itemCooldowns = player.getCooldowns();
            itemCooldowns.addCooldown(itemStack, 20 * 40);
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20 * 10, 3));
            level.playSound(null, player.getX(), player.getEyeY(), player.getZ(), SoundEventInit.SPELL_CARD, player.getSoundSource(), 1.0f, 1.0f);
            player.awardStat(Stats.ITEM_USED.get(this));
            RDCriteriaTriggers.USE_ITEM.trigger((ServerPlayer) player, itemStack);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

}
