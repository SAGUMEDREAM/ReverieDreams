package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;

public class PapilioPatternFan extends SwordItem {
    public static final ToolMaterial PAPILIO_PATTERN_FAN = new ToolMaterial(RDBlockTags.EMPTY, 370, 8.0f, 5f, 10, ItemTags.WOOL);

    public PapilioPatternFan(float attackDamage, float attackSpeed, Properties settings) {
        super(PAPILIO_PATTERN_FAN, attackDamage, attackSpeed, settings);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ItemStack itemStack = serverPlayer.getItemInHand(hand);
            serverPlayer.getCooldowns().addCooldown(itemStack, 35);
            var look = serverPlayer.getLookAngle();

            double forwardStrength = 1.4;
            double yBoost = 0.35;

            serverPlayer.push(
                    look.x * forwardStrength,
                    yBoost,
                    look.z * forwardStrength
            );

            serverPlayer.hurtMarked = true;
            serverPlayer.fallDistance = 0;
            SoundEventPlayUtils.playSound(player, SoundEvents.WIND_CHARGE_BURST.value(), SoundSource.PLAYERS);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }
}
