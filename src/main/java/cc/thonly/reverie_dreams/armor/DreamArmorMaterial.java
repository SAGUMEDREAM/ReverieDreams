package cc.thonly.reverie_dreams.armor;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.data.ModTags;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvents;

import java.util.Map;

public interface DreamArmorMaterial {
    int BASE_DURABILITY = 18;
    RegistryKey<EquipmentAsset> REGISTRY_KEY = RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, Touhou.id("dream"));

    ArmorMaterial INSTANCE = new ArmorMaterial(
            BASE_DURABILITY,
            Map.of(
                    EquipmentType.HELMET, 3,
                    EquipmentType.CHESTPLATE, 6,
                    EquipmentType.LEGGINGS, 5,
                    EquipmentType.BOOTS, 2
            ),
            5,
            SoundEvents.ITEM_ARMOR_EQUIP_IRON,
            0.0F,
            0.0F,
            ModTags.ItemTypeTag.SILVER_TOOL_MATERIALS,
            REGISTRY_KEY
    );
}
