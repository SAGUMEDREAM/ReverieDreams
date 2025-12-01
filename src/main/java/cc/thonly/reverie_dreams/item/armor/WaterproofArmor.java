package cc.thonly.reverie_dreams.item.armor;

import cc.thonly.reverie_dreams.armor.WaterproofArmorMaterial;
import cc.thonly.reverie_dreams.item.base.ArmorItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import org.jetbrains.annotations.Nullable;

public class WaterproofArmor extends ArmorItem {
    public WaterproofArmor(ArmorType type, Properties settings) {
        super(WaterproofArmorMaterial.INSTANCE, type, settings);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel serverLevel, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(itemStack, serverLevel, entity, slot);
        if (slot == EquipmentSlot.HEAD && entity instanceof LivingEntity livingEntity) {
            if (!livingEntity.hasEffect(MobEffects.WATER_BREATHING)) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 1, 0, false, false, false));
            }
        }
    }

    public static boolean hasEquipment(LivingEntity livingEntity) {
        return livingEntity.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof WaterproofArmor ||
                livingEntity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof WaterproofArmor ||
                livingEntity.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof WaterproofArmor ||
                livingEntity.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof WaterproofArmor ||
                livingEntity.getItemBySlot(EquipmentSlot.BODY).getItem() instanceof WaterproofArmor;
    }
}
