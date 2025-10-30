package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;

public class Hakuroken extends SwordItem implements YoumuSwordUsing{
    public static final ToolMaterial HAKUROKEN = new ToolMaterial(ModTags.BlockTypeTag.EMPTY, 1250, 8.0f, 5.5f, 10, ModTags.ItemTypeTag.SILVER_BLOCK);

    public Hakuroken(float attackDamage, float attackSpeed, Properties settings) {
        super(HAKUROKEN, attackDamage, attackSpeed, settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        return this.useItem(world, user, hand);
    }
}
