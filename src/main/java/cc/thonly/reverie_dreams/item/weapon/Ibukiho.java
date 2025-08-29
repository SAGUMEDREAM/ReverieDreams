package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class Ibukiho extends SwordItem {
    public static final ToolMaterial IBUKIHO = new ToolMaterial(ModTags.BlockTypeTag.EMPTY, 1561, 8.0f, 5.5f, 10, ItemTags.WART_BLOCKS);

    public Ibukiho(float attackDamage, float attackSpeed, Settings settings) {
        super(IBUKIHO,
                attackDamage,
                attackSpeed,
                settings
        );
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack copy = stack.copy();
        Hand activeHand = user.getActiveHand();
        if (copy.isDamageable() && user instanceof ServerPlayerEntity player && !player.isInCreativeMode()) {
            copy.damage(10, user, EquipmentSlot.MAINHAND);
        }
        user.setStackInHand(activeHand, copy);
        StatusEffectInstance strength = new StatusEffectInstance(StatusEffects.STRENGTH, 60 * 20);
        StatusEffectInstance speed = new StatusEffectInstance(StatusEffects.SPEED, 60 * 20);
        StatusEffectInstance jumpBoost = new StatusEffectInstance(StatusEffects.JUMP_BOOST, 60 * 20);
        StatusEffectInstance nausea = new StatusEffectInstance(StatusEffects.NAUSEA, 60 * 20);
        user.addStatusEffect(strength);
        user.addStatusEffect(speed);
        user.addStatusEffect(jumpBoost);
        user.addStatusEffect(nausea);
        return super.finishUsing(stack, world, user);
    }
}
