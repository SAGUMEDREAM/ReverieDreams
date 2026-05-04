package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.advancement.UseItemTrigger;
import cc.thonly.reverie_dreams.registry.content.advancements.RDCriteriaTriggers;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@SuppressWarnings("NullableProblems")
public class SatoriEye extends Item {
    public SatoriEye(Properties properties) {
        super(properties);
    }

    @SuppressWarnings("resource")
    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand hand) {
        Level level = player.level();
        if (player == target) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            MutableComponent message = Component.empty();
            message.append(target.getName().copy().withStyle(ChatFormatting.WHITE));
            message.append(Component.literal(
                    " HP: %.1f/%.1f".formatted(target.getHealth(), target.getMaxHealth())
            ).withStyle(ChatFormatting.RED));
            message.append(Component.literal(
                    " | Armor: " + target.getArmorValue()
            ).withStyle(ChatFormatting.BLUE));
            if (target instanceof Mob mob && mob.getTarget() != null) {
                message.append(Component.literal(" | Target → ").withStyle(ChatFormatting.GOLD));
                message.append(mob.getTarget().getName().copy().withStyle(ChatFormatting.YELLOW));
            }
            var effects = target.getActiveEffects();

            if (!effects.isEmpty()) {
                MutableComponent effectsComponent = Component.literal(" | Effects: ")
                        .withStyle(ChatFormatting.LIGHT_PURPLE);
                int count = 0;
                int max = 3;
                for (var effect : effects) {
                    if (count >= max) break;

                    MutableComponent name = effect.getEffect().value().getDisplayName().copy();

                    name.withStyle(ChatFormatting.LIGHT_PURPLE);

                    effectsComponent.append(name);

                    count++;

                    if (count < max && count < effects.size()) {
                        effectsComponent.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
                    }
                }

                if (effects.size() > max) {
                    effectsComponent.append(Component.literal("...").withStyle(ChatFormatting.DARK_GRAY));
                }

                message.append(effectsComponent);
            }
            serverPlayer.sendSystemMessage(message, true);
            target.swing(hand);
            RDCriteriaTriggers.USE_ITEM.value().trigger(serverPlayer, RDItems.SATORI_EYE.createStack());
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }
}
