package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.entity.npc.NPCRoleEntity;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;

public class Levatin extends SwordItem {
    public static final ToolMaterial LEVATIN = new ToolMaterial(RDBlockTags.EMPTY, 1561, 8.0f, 5.5f, 10, ItemTags.NETHERITE_TOOL_MATERIALS);

    public Levatin(float attackDamage, float attackSpeed, Properties settings) {
        super(LEVATIN, attackDamage, attackSpeed, settings);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!target.level().isClientSide()) {
            tryBreakEquipments(target);
        }

        super.hurtEnemy(stack, target, attacker);
    }

    private void tryBreakEquipments(LivingEntity target) {
        if (target instanceof Player || target instanceof NPCRoleEntity)
            return;

        ServerLevel world = (ServerLevel) target.level();
        int i = world.random.nextIntBetweenInclusive(0, EquipmentSlot.values().length - 1);
        breakSlot(target, EquipmentSlot.values()[i]);
    }

    private void breakSlot(LivingEntity target, EquipmentSlot eSlot) {
        ServerLevel world = (ServerLevel) target.level();

        ItemStack stack = target.getItemBySlot(eSlot);
        if (stack != null && stack.isDamageableItem()) {
            stack.hurtAndBreak(stack.getMaxDamage(), target, eSlot);
            world.playSound(target, target.blockPosition(), SoundEvents.ITEM_BREAK.value(), SoundSource.PLAYERS, 1, 1);
        }
    }

}
