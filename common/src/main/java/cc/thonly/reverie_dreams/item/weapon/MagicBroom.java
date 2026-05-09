package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class MagicBroom extends SwordItem {
    public static final ToolMaterial MAGIC_BROOM = new ToolMaterial(RDBlockTags.EMPTY, 750, 7.5f, 4.0f, 10, RDItemTags.POWER_BLOCK);

    public MagicBroom(float attackDamage, float attackSpeed, Properties settings) {
        super(MAGIC_BROOM, attackDamage, attackSpeed, settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        ItemStack itemStack = context.getItemInHand();
        Player player = context.getPlayer();
        BlockPos blockPos = context.getClickedPos();
        InteractionHand hand = context.getHand();
        if (!world.isClientSide() && player != null) {
            cc.thonly.reverie_dreams.entity.misc.MagicBroom entity = new cc.thonly.reverie_dreams.entity.misc.MagicBroom(RDEntityTypes.MAGIC_BROOM.asHolder().value(), world, blockPos.getX() + 0.5f, blockPos.getY() + 1, blockPos.getZ() + 0.5f, IngredientStack.of(itemStack.copy()), player.getUUID());
            world.addFreshEntity(entity);
            itemStack.consume(1, player);
            player.swing(hand);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

}
