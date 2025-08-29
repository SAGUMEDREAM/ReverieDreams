package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.data.ModTags;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;

public class GhostStatusEffectTargetGoal<T extends LivingEntity> extends StatusEffectTargetGoal<T> {
    private static final int DEFAULT_RECIPROCAL_CHANCE = 10;

    public GhostStatusEffectTargetGoal(MobEntity mob, Class<T> targetClass, boolean checkVisibility, @Nullable RegistryEntry<StatusEffect> requiredEffect) {
        this(mob, targetClass, DEFAULT_RECIPROCAL_CHANCE, checkVisibility, false, null, requiredEffect);
    }

    public GhostStatusEffectTargetGoal(MobEntity mob, Class<T> targetClass, int reciprocalChance, boolean checkVisibility, boolean checkCanNavigate, @Nullable TargetPredicate.EntityPredicate targetPredicate, @Nullable RegistryEntry<StatusEffect> requiredEffect) {
        super(mob, targetClass, reciprocalChance, checkVisibility, checkCanNavigate, targetPredicate, requiredEffect);
    }

    @Override
    public void start() {
        if (this.targetEntity != null) {
            var hasSilver = hasSilverArmor(this.targetEntity);
            System.out.println(hasSilver);
            if (!hasSilver) {
                super.start();
            }
        }
    }

    public static boolean hasSilverArmor(LivingEntity targetEntity) {
        if (!targetEntity.isAlive()) return false;
        final var tag = ModTags.ItemTypeTag.SILVER_ARMOR;
        ItemStack head = targetEntity.getEquippedStack(EquipmentSlot.HEAD);
        ItemStack chest = targetEntity.getEquippedStack(EquipmentSlot.CHEST);
        ItemStack legs = targetEntity.getEquippedStack(EquipmentSlot.LEGS);
        ItemStack feet = targetEntity.getEquippedStack(EquipmentSlot.FEET);

        return head.isIn(tag) || chest.isIn(tag) || legs.isIn(tag) || feet.isIn(tag);
    }
}
