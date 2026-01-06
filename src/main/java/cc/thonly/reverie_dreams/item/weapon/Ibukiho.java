package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;

public class Ibukiho extends SwordItem {
    public static final ToolMaterial IBUKIHO = new ToolMaterial(RDBlockTags.EMPTY, 1561, 8.0f, 5.5f, 10, ItemTags.WART_BLOCKS);

    public Ibukiho(float attackDamage, float attackSpeed, Properties settings) {
        super(IBUKIHO,
                attackDamage,
                attackSpeed,
                settings
        );
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        ItemStack copy = stack.copy();
        InteractionHand activeHand = user.getUsedItemHand();
        if (copy.isDamageableItem() && user instanceof ServerPlayer player && !player.hasInfiniteMaterials()) {
            copy.hurtAndBreak(10, user, EquipmentSlot.MAINHAND);
        }
        user.setItemInHand(activeHand, copy);
        MobEffectInstance strength = new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60 * 20);
        MobEffectInstance speed = new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60 * 20);
        MobEffectInstance jumpBoost = new MobEffectInstance(MobEffects.JUMP, 60 * 20);
        MobEffectInstance nausea = new MobEffectInstance(MobEffects.CONFUSION, 60 * 20);
        user.addEffect(strength);
        user.addEffect(speed);
        user.addEffect(jumpBoost);
        user.addEffect(nausea);
        return super.finishUsingItem(stack, world, user);
    }
}
