package cc.thonly.reverie_dreams.item.armor;

import cc.thonly.reverie_dreams.armor.DreamArmorMaterial;
import cc.thonly.reverie_dreams.item.base.ArmorItem;
import cc.thonly.reverie_dreams.server.ArmorAttributeManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.EquipmentType;

public class DreamArmorItem extends ArmorItem {
    public DreamArmorItem(EquipmentType type, Settings settings) {
        super(DreamArmorMaterial.INSTANCE, type, settings);
        ArmorAttributeManager.register(this::onAccept, this);
    }

    void onAccept(LivingEntity entity, ItemStack itemStack) {
        entity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 20, 0, true, false, true));
    }
}
