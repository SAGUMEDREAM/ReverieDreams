package cc.thonly.reverie_dreams.item.armor;

import cc.thonly.reverie_dreams.armor.LowGravityBootArmorMaterial;
import cc.thonly.reverie_dreams.item.base.ArmorItem;
import cc.thonly.reverie_dreams.util.entity.EquipmentSlotHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import org.jspecify.annotations.Nullable;

public class LowGravityBootItem extends ArmorItem {
    public LowGravityBootItem(Properties settings) {
        super(LowGravityBootArmorMaterial.INSTANCE, ArmorType.BOOTS, settings);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        super.inventoryTick(itemStack, level, owner, slot);
        if (EquipmentSlotHelper.isArmorSlot(slot) && owner instanceof LivingEntity livingEntity) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 10, 1, false, false, false));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 10, 1, false, false, false));
        }
    }
}
