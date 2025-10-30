package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;

public class DeathScytheItem extends SwordItem {
    public static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(ModTags.BlockTypeTag.EMPTY, 1561, 8.0f, 5.5f, 10, ItemTags.NETHERITE_TOOL_MATERIALS);

    public DeathScytheItem(float attackDamage, float attackSpeed, Properties settings) {
        super(TOOL_MATERIAL, attackDamage, attackSpeed, settings);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level world = target.level();
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
            if (serverWorld.random.nextFloat() < 0.2f) {
                attacker.setHealth(attacker.getHealth() + 3);
            }
        }
        super.hurtEnemy(stack, target, attacker);
    }
}
