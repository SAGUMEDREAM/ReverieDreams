package cc.thonly.reverie_dreams.armor;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.Map;

public interface OniMaskArmorMaterial {
    int BASE_DURABILITY = 20;
    ResourceKey<EquipmentAsset> REGISTRY_KEY = ResourceKey.create(EquipmentAssets.ROOT_ID, ReverieDreams.id("oni_mask"));

    ArmorMaterial INSTANCE = new ArmorMaterial(
            BASE_DURABILITY,
            Map.of(
                    ArmorType.HELMET, 3,
                    ArmorType.CHESTPLATE, 0,
                    ArmorType.LEGGINGS, 0,
                    ArmorType.BOOTS, 0
            ),
            15,
            SoundEvents.ARMOR_EQUIP_IRON,
            1.0F,
            0.0F,
            RDItemTags.ONI_MASK_MATERIALS,
            REGISTRY_KEY
    );
}