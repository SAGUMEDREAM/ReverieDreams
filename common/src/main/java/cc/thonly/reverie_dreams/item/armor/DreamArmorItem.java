package cc.thonly.reverie_dreams.item.armor;

import cc.thonly.reverie_dreams.armor.DreamArmorMaterial;
import cc.thonly.reverie_dreams.item.base.ArmorItem;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.equipment.ArmorType;

public class DreamArmorItem extends ArmorItem {
    public DreamArmorItem(ArmorType type, Properties settings) {
        super(DreamArmorMaterial.INSTANCE, type, settings);
    }

    public static boolean wearsFullSet(LivingEntity wearer) {
        return wearer.getItemBySlot(EquipmentSlot.CHEST).is(RDItems.DREAM_CHESTPLATE.asItem())
                && wearer.getItemBySlot(EquipmentSlot.HEAD).is(RDItems.DREAM_HELMET.asItem())
                && wearer.getItemBySlot(EquipmentSlot.LEGS).is(RDItems.DREAM_LEGGINGS.asItem())
                && wearer.getItemBySlot(EquipmentSlot.FEET).is(RDItems.DREAM_BOOTS.asItem());
    }
}
