package cc.thonly.reverie_dreams.item.base;

import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;

public class PickaxeItem extends Item {
    public PickaxeItem(ToolMaterial material, float attackDamage, float attackSpeed, Settings settings) {
        super(settings.pickaxe(material, attackDamage, attackSpeed));
    }

}
