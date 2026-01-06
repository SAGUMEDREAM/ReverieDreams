package cc.thonly.reverie_dreams.item.armor;

import cc.thonly.polymer.item.IBasicPolymerItem;
import cc.thonly.reverie_dreams.armor.DreamArmorMaterial;
import cc.thonly.reverie_dreams.item.base.ArmorItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.Level;

public class DreamArmorItem extends ArmorItem implements IBasicPolymerItem {
    public DreamArmorItem(ArmorType type, Properties settings) {
        super(DreamArmorMaterial.INSTANCE, type, settings);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean bl) {
        super.inventoryTick(itemStack, level, entity, i, bl);
        if (entity instanceof LivingEntity livingEntity) {
            ItemStack a = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
            ItemStack b = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
            ItemStack c = livingEntity.getItemBySlot(EquipmentSlot.LEGS);
            ItemStack d = livingEntity.getItemBySlot(EquipmentSlot.FEET);
            if (!livingEntity.hasEffect(MobEffects.REGENERATION) && (
                    itemStack.equals(a) ||
                    itemStack.equals(b) ||
                    itemStack.equals(c) ||
                    itemStack.equals(d)
                    )) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20, 0, true, false, true));
            }
        }
    }

}
