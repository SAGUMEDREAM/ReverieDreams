package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.advancement.UseItemTrigger;
import cc.thonly.reverie_dreams.registry.content.advancements.RDCriteriaTriggers;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SatoriEye extends Item {
    public SatoriEye(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand hand) {
        Level level = player.level();
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            MutableComponent message = Component.empty();
            message.append(target.getName());
            message.append("§c: %.1f/%.1f".formatted(target.getHealth(), target.getMaxHealth()));
            serverPlayer.sendSystemMessage(message, true);
            target.swing(hand);
            RDCriteriaTriggers.USE_ITEM.trigger(serverPlayer, RDItems.SATORI_EYE.getDefaultInstance());
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }
}
