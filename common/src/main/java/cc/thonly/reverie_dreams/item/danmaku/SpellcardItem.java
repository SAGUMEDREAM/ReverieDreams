package cc.thonly.reverie_dreams.item.danmaku;

import cc.thonly.reverie_dreams.data.danmaku.SpellcardRenderer;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.sound.RDSoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class SpellcardItem extends Item {
    public SpellcardItem(Properties properties) {
        super(properties);
    }

    private InteractionResult spell(Level level, Player player, InteractionHand hand) {
        if (player == null) {
            return InteractionResult.FAIL;
        }
        ItemCooldowns cooldowns = player.getCooldowns();
        if (level instanceof ServerLevel world) {
            ItemStack itemStack = player.getItemInHand(hand);
            SpellcardRenderer renderer = itemStack.get(RDDataComponents.SPELL_CARD_COMPONENT.value());
            if (renderer == null) {
                return InteractionResult.PASS;
            }
            renderer = renderer.copy();
            renderer.setWorld(world);
            renderer.setSource(player);
            renderer.setPosition(player.getEyePosition());
            SpellcardRenderer.addRenderer(renderer);
            cooldowns.addCooldown(itemStack, 20);
            world.playSound(null, player.getX(), player.getEyeY(), player.getZ(), RDSoundEvents.SPELL_CARD, player.getSoundSource(), 1.0f, 1.0f);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        super.use(level, player, interactionHand);
        return this.spell(level, player, interactionHand);
    }

    @Override
    public InteractionResult useOn(UseOnContext useOnContext) {
        super.useOn(useOnContext);
        return this.spell(useOnContext.getLevel(), useOnContext.getPlayer(), useOnContext.getHand());
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand interactionHand) {
        super.interactLivingEntity(itemStack, player, livingEntity, interactionHand);
        return this.spell(player.level(), player, interactionHand);
    }
}
