package cc.thonly.reverie_dreams.item.base;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;


public class SwordItem extends Item {
    public SwordItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties settings) {
        this(settings.sword(material, attackDamage, attackSpeed));
    }

    protected SwordItem(Properties swordProperties) {
        super(swordProperties);
    }

}
