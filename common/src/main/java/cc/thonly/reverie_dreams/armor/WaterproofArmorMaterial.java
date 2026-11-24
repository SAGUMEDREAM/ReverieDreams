package cc.thonly.reverie_dreams.armor;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.Map;

public interface WaterproofArmorMaterial {
    int BASE_DURABILITY = 12;
    ResourceKey<EquipmentAsset> REGISTRY_KEY = ResourceKey.create(EquipmentAssets.ROOT_ID, ReverieDreams.id("waterproof"));

    ArmorMaterial INSTANCE = new ArmorMaterial(
            BASE_DURABILITY,
            Map.of(
                    ArmorType.HELMET, 1,
                    ArmorType.CHESTPLATE, 4,
                    ArmorType.LEGGINGS, 3,
                    ArmorType.BOOTS, 1,
                    ArmorType.BODY, 4
            ),
            12,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            0.0F,
            0.0F,
            ItemTags.REPAIRS_LEATHER_ARMOR,
            REGISTRY_KEY
    );
}
