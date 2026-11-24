package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;

public class YukaFlowerUmbrella extends SwordItem {
    public static final ToolMaterial INSTANCE = new ToolMaterial(RDBlockTags.EMPTY, 800, 7.0f, 5.5f, 10, RDItemTags.SILVER_TOOL_MATERIALS);
    private static final double SWEEP_RADIUS = 2.5;

    public YukaFlowerUmbrella(float attackDamage, float attackSpeed, Properties settings) {
        super(INSTANCE, attackDamage, attackSpeed, settings.spear(INSTANCE, 1.15F, 1.2F, 0.4F, 2.5F, 7.0F, 5.5F, 5.1F, 8.75F, 4.6F).fireResistant());
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        player.startUsingItem(hand);
        if (!level.isClientSide()) {
            player.getCooldowns().addCooldown(stack, 100);
            level.playSound(null, player.blockPosition(), SoundEvents.SHIELD_BLOCK.value(),
                    SoundSource.PLAYERS, 0.7f, 1.2f);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 40;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.SPEAR;
    }

}
