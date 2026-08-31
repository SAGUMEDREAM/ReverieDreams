package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class GhostStatusEffectTargetGoal<T extends LivingEntity> extends StatusEffectTargetGoal<T> {
    private static final int DEFAULT_RECIPROCAL_CHANCE = 10;

    public GhostStatusEffectTargetGoal(Mob mob, Class<T> targetClass, boolean checkVisibility, @Nullable Holder<MobEffect> requiredEffect) {
        this(mob, targetClass, DEFAULT_RECIPROCAL_CHANCE, checkVisibility, false, null, requiredEffect);
    }

    public GhostStatusEffectTargetGoal(Mob mob, Class<T> targetClass, int reciprocalChance, boolean checkVisibility, boolean checkCanNavigate, @Nullable TargetingConditions.Selector targetPredicate, @Nullable Holder<MobEffect> requiredEffect) {
        super(mob, targetClass, reciprocalChance, checkVisibility, checkCanNavigate, targetPredicate, requiredEffect);
    }

    @Override
    public void start() {
        if (this.targetEntity != null) {
            var hasSilver = hasSilverArmor(this.targetEntity);
            if (!hasSilver) {
                super.start();
            }
        }
    }

    public static boolean hasSilverArmor(LivingEntity targetEntity) {
        if (!targetEntity.isAlive()) return false;
        final var tag = RDItemTags.SILVER_ARMOR;
        ItemStack head = targetEntity.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = targetEntity.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = targetEntity.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack feet = targetEntity.getItemBySlot(EquipmentSlot.FEET);

        return head.is(tag) || chest.is(tag) || legs.is(tag) || feet.is(tag);
    }
}
