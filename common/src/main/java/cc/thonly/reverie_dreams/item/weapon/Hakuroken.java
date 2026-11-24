package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;

public class Hakuroken extends SwordItem implements YoumuSwordUsing {
    public static final ToolMaterial HAKUROKEN = new ToolMaterial(RDBlockTags.EMPTY, 900, 7.0f, 4.5f, 12, RDItemTags.SILVER_BLOCK);

    public Hakuroken(float attackDamage, float attackSpeed, Properties settings) {
        super(HAKUROKEN, attackDamage, attackSpeed, settings);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BLOCK;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return player.isShiftKeyDown() ? YoumuSwordCombat.purify(level, player, hand)
                : YoumuSwordCombat.startGuard(level, player, hand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (user instanceof Player player)
            YoumuSwordCombat.onUseTick(level, player, stack);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity user, int remaining) {
        if (user instanceof Player player)
            YoumuSwordCombat.endGuard(player);
        return false;
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        YoumuSwordCombat.afterMeleeHit(stack, target, attacker);
    }

    @Override
    public float getAttackDamageBonus(Entity target, float damage, DamageSource source) {
        if (!(target instanceof LivingEntity living))
            return 0;
        return YoumuSwordCombat.ordinaryGhost(living) ? 40 : YoumuSwordCombat.spirit(living) ? 2 : 0;
    }
}
