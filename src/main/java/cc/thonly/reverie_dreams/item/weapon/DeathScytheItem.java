package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class DeathScytheItem extends SwordItem {
    public static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(ModTags.BlockTypeTag.EMPTY, 1561, 8.0f, 5.5f, 10, ItemTags.NETHERITE_TOOL_MATERIALS);

    public DeathScytheItem(float attackDamage, float attackSpeed, Settings settings) {
        super(TOOL_MATERIAL, attackDamage, attackSpeed, settings);
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        World world = target.getWorld();
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            if (serverWorld.random.nextFloat() < 0.2f) {
                attacker.setHealth(attacker.getHealth() + 3);
            }
        }
        super.postHit(stack, target, attacker);
    }
}
