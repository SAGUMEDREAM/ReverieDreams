package cc.thonly.reverie_dreams.item.armor;

import cc.thonly.reverie_dreams.armor.DreamArmorMaterial;
import cc.thonly.reverie_dreams.item.base.ArmorItem;
import net.minecraft.item.equipment.EquipmentType;

public class DreamArmorItem extends ArmorItem {
    public DreamArmorItem(EquipmentType type, Settings settings) {
        super(DreamArmorMaterial.INSTANCE, type, settings);
    }
}
