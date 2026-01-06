package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.polymer.item.IBasicPolymerItem;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ToolMaterial;

public class PapilioPatternFan extends SwordItem implements IBasicPolymerItem {
    public static final ToolMaterial PAPILIO_PATTERN_FAN = new ToolMaterial(RDBlockTags.EMPTY, 370, 8.0f, 5f, 10, ItemTags.WOOL);

    public PapilioPatternFan(float attackDamage, float attackSpeed, Properties settings) {
        super(PAPILIO_PATTERN_FAN, attackDamage, attackSpeed, settings);
    }
}
