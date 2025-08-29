package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class Rokanken extends SwordItem implements YoumuSwordUsing {
    public static final ToolMaterial ROKANKEN = new ToolMaterial(ModTags.BlockTypeTag.EMPTY, 1250, 8.0f, 5.5f, 10, ModTags.ItemTypeTag.SILVER_BLOCK);

    public Rokanken(float attackDamage, float attackSpeed, Settings settings) {
        super(ROKANKEN, attackDamage, attackSpeed, settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        return this.useItem(world, user, hand);
    }
}
