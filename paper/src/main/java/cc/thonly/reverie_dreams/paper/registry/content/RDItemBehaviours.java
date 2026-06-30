package cc.thonly.reverie_dreams.paper.registry.content;

import cc.thonly.reverie_dreams.paper.ReverieDreamsPlugin;
import cc.thonly.reverie_dreams.paper.item.armor.*;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviors;
import net.momirealms.craftengine.core.util.Key;

public class RDItemBehaviours {
    public static final Key CROWN_OF_THE_UNDER_WORLD_ARMOR = ReverieDreamsPlugin.key("crown_of_the_underworld_armor");
    public static final Key DREAM_ARMOR = ReverieDreamsPlugin.key("dream_armor");
    public static final Key EARPHONE_ARMOR = ReverieDreamsPlugin.key("earphone_armor");
    public static final Key KOISHI_HAT_ARMOR = ReverieDreamsPlugin.key("koishi_hat_armor");
    public static final Key LOW_GRAVITY_BOOT_ARMOR = ReverieDreamsPlugin.key("low_gravity_boot_armor");
    public static final Key WATERPROOF_ARMOR = ReverieDreamsPlugin.key("waterproof_armor");

    public static void initialize() {
        ItemBehaviors.register(CROWN_OF_THE_UNDER_WORLD_ARMOR, CrownOfTheUnderworldItem.FACTORY);
        ItemBehaviors.register(DREAM_ARMOR, DreamArmorItem.FACTORY);
        ItemBehaviors.register(EARPHONE_ARMOR, EarphoneItem.FACTORY);
        ItemBehaviors.register(KOISHI_HAT_ARMOR, KoishiHatItem.FACTORY);
        ItemBehaviors.register(LOW_GRAVITY_BOOT_ARMOR, LowGravityBootItem.FACTORY);
        ItemBehaviors.register(WATERPROOF_ARMOR, WaterproofArmor.FACTORY);
    }
}
