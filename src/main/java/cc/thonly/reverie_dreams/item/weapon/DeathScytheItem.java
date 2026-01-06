package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

import java.util.concurrent.ThreadLocalRandom;

public class DeathScytheItem extends SwordItem {
    public static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(RDBlockTags.EMPTY, 1561, 8.5f, 5.5f, 10, ItemTags.NETHERITE_TOOL_MATERIALS);
    private static final double SWEEP_RADIUS = 2.5;

    public DeathScytheItem(float attackDamage, float attackSpeed, Properties settings) {
        super(TOOL_MATERIAL, attackDamage, attackSpeed, settings.component(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.builder().add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(ResourceLocation.withDefaultNamespace("base_attack_range"), 5.0f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HAND).build()));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level world = target.level();
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
            if (ThreadLocalRandom.current().nextFloat() < 0.28f) {
                attacker.setHealth(attacker.getHealth() + 3);
            }
        }
        return  super.hurtEnemy(stack, target, attacker);
    }
}
