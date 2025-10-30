package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.entity.misc.MagicBroomEntity;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class MagicBroom extends SwordItem {
    public static final ToolMaterial MAGIC_BROOM = new ToolMaterial(ModTags.BlockTypeTag.EMPTY, 750, 7.5f, 4.0f, 10, ModTags.ItemTypeTag.POWER_BLOCK);

    public MagicBroom(float attackDamage, float attackSpeed, Item.Properties settings) {
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
            MagicBroomEntity entity = new MagicBroomEntity(ModEntities.BROOM_ENTITY_TYPE, world, blockPos.getX(), blockPos.getY() + 1, blockPos.getZ(), ItemStackWrapper.of(itemStack.copy()), player.getUUID().toString().intern());
            world.addFreshEntity(entity);
            itemStack.consume(1, player);
            player.swing(hand);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

}
