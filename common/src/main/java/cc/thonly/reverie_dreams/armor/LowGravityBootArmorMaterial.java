package cc.thonly.reverie_dreams.armor;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.Map;

public interface LowGravityBootArmorMaterial {
    int BASE_DURABILITY = 250;
    ResourceKey<EquipmentAsset> REGISTRY_KEY = ResourceKey.create(EquipmentAssets.ROOT_ID, ReverieDreams.id("low_gravity_boot"));

    ArmorMaterial INSTANCE = new ArmorMaterial(
            BASE_DURABILITY,
            Map.of(
                    ArmorType.HELMET, 3,
                    ArmorType.CHESTPLATE, 8,
                    ArmorType.LEGGINGS, 6,
                    ArmorType.BOOTS, 3
            ),
            5,
            SoundEvents.ARMOR_EQUIP_IRON,
            0.0F,
            0.0F,
            RDItemTags.SILVER_TOOL_MATERIALS,
            REGISTRY_KEY
    );
}
