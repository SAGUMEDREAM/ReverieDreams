package cc.thonly.reverie_dreams.armor;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.ModTags;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public interface MagicIceArmorMaterial {
    int BASE_DURABILITY = 14;
    ResourceKey<EquipmentAsset> REGISTRY_KEY = ResourceKey.create(EquipmentAssets.ROOT_ID, ReverieDreams.id("magic_ice"));

    ArmorMaterial INSTANCE = new ArmorMaterial(
            BASE_DURABILITY,
            Map.of(
                    ArmorType.HELMET, 2,
                    ArmorType.CHESTPLATE, 6,
                    ArmorType.LEGGINGS, 5,
                    ArmorType.BOOTS, 2
            ),
            5,
            SoundEvents.ARMOR_EQUIP_IRON,
            0.0F,
            0.0F,
            ModTags.ItemTypeTag.MAGIC_ICE_TOOL_MATERIALS,
            REGISTRY_KEY
    );
}
