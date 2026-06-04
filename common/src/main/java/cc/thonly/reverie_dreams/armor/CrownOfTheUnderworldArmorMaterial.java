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

public interface CrownOfTheUnderworldArmorMaterial {
    int BASE_DURABILITY = 250;
    ResourceKey<EquipmentAsset> REGISTRY_KEY = ResourceKey.create(EquipmentAssets.ROOT_ID, ReverieDreams.id("crown_of_the_underworld"));

    ArmorMaterial INSTANCE = new ArmorMaterial(
            BASE_DURABILITY,
            Map.of(
                    ArmorType.HELMET, 5,
                    ArmorType.CHESTPLATE, 9,
                    ArmorType.LEGGINGS, 7,
                    ArmorType.BOOTS, 4
            ),
            5,
            SoundEvents.ARMOR_EQUIP_LEATHER,
            0.0F,
            0.0F,
            ItemTags.REPAIRS_GOLD_ARMOR,
            REGISTRY_KEY
    );
}
