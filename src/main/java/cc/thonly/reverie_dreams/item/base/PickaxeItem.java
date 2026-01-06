package cc.thonly.reverie_dreams.item.base;

import cc.thonly.polymer.item.IBasicPolymerItem;
import net.minecraft.world.item.ToolMaterial;

public class PickaxeItem extends net.minecraft.world.item.PickaxeItem implements IBasicPolymerItem {
    public PickaxeItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties settings) {
        super(material, attackDamage, attackSpeed, settings);
    }

}
