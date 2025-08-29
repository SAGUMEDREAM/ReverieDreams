package cc.thonly.reverie_dreams.item.base;

import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;


public class SwordItem extends Item {
    public SwordItem(ToolMaterial material, float attackDamage, float attackSpeed, Item.Settings settings) {
        super(settings.sword(material, attackDamage, attackSpeed));
    }

}
