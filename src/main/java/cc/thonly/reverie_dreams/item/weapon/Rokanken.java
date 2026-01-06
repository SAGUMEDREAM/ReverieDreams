package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.polymer.item.IBasicPolymerItem;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;

public class Rokanken extends SwordItem implements YoumuSwordUsing, IBasicPolymerItem {
    public static final ToolMaterial ROKANKEN = new ToolMaterial(RDBlockTags.EMPTY, 1250, 8.0f, 5.5f, 10, RDItemTags.SILVER_BLOCK);

    public Rokanken(float attackDamage, float attackSpeed, Properties settings) {
        super(ROKANKEN, attackDamage, attackSpeed, settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        return this.useItem(world, user, hand);
    }
}
