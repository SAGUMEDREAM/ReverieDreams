package cc.thonly.reverie_dreams.item.armor;

import cc.thonly.reverie_dreams.armor.DreamArmorMaterial;
import cc.thonly.reverie_dreams.item.base.ArmorItem;
import cc.thonly.reverie_dreams.util.EquipmentSlotUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import org.jetbrains.annotations.Nullable;

public class DreamArmorItem extends ArmorItem {
    public DreamArmorItem(ArmorType type, Properties settings) {
        super(DreamArmorMaterial.INSTANCE, type, settings);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, ServerLevel serverLevel, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(itemStack, serverLevel, entity, slot);
        if (EquipmentSlotUtil.isArmorSlot(slot) && entity instanceof LivingEntity livingEntity) {
            if (!livingEntity.hasEffect(MobEffects.REGENERATION)) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20, 0, true, false, true));
            }
        }
    }

}
