package cc.thonly.reverie_dreams.item.base;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;


public class SwordItem extends Item {
    public SwordItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties settings) {
        super(settings.sword(material, attackDamage, attackSpeed));
    }

}
