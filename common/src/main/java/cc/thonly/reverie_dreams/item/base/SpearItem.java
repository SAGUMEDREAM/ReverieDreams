package cc.thonly.reverie_dreams.item.base;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;


public class SpearItem extends Item {
    public SpearItem(ToolMaterial material, float swingDuration, float damageMultiplier, float delay, float dismountMaxDuration, float dismountMinSpeed, float knockbackMaxDuration, float knockbackMinSpeed, float damageMaxDuration, float damageMinSpeed, Properties settings) {
        super(settings.spear(material, swingDuration, damageMultiplier, delay, dismountMaxDuration, dismountMinSpeed, knockbackMaxDuration, knockbackMinSpeed, damageMaxDuration, damageMinSpeed));
    }

}
