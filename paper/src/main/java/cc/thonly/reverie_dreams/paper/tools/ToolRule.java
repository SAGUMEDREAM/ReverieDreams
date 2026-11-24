package cc.thonly.reverie_dreams.paper.tools;

/** Current Mod registration values, distinct from the player's base attack attributes. */
public record ToolRule(String id, String material, String kind, int durability, double attackDamageModifier,
                       double attackSpeedModifier, int enchantability, String repairTag, boolean silverMarker) { }
