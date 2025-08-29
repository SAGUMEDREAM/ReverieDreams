package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.ItemTags;

public class Gungnir extends SwordItem {
    public static final ToolMaterial GUNGNIR = new ToolMaterial(ModTags.BlockTypeTag.EMPTY, 1561, 8.0f, 5.5f, 10, ItemTags.NETHERITE_TOOL_MATERIALS);

    public Gungnir(float attackDamage, float attackSpeed, Settings settings) {
        super(GUNGNIR, attackDamage, attackSpeed, settings);
    }
}
