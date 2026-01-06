package cc.thonly.reverie_dreams.item.base;

import cc.thonly.polymer.item.IBasicPolymerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;


public class SwordItem extends net.minecraft.world.item.SwordItem implements IBasicPolymerItem {
    public SwordItem(ToolMaterial material, float attackDamage, float attackSpeed, Item.Properties settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

}
