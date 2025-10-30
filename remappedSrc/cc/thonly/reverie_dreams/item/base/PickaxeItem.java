package cc.thonly.reverie_dreams.item.base;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public class PickaxeItem extends Item {
    public PickaxeItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties settings) {
        super(settings.pickaxe(material, attackDamage, attackSpeed));
    }

}
