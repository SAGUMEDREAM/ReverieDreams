package cc.thonly.reverie_dreams.paper.equipment;

import org.bukkit.inventory.EquipmentSlot;

public record EquipmentRule(String id, String material, EquipmentSlot slot, int durability,
                            double armor, double toughness, int enchantability, String repairTag) { }
