package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.entity.misc.MagicBroomEntity;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MagicBroom extends SwordItem {
    public static final ToolMaterial MAGIC_BROOM = new ToolMaterial(ModTags.BlockTypeTag.EMPTY, 750, 7.5f, 4.0f, 10, ModTags.ItemTypeTag.POWER_BLOCK);

    public MagicBroom(float attackDamage, float attackSpeed, Item.Settings settings) {
        super(MAGIC_BROOM, attackDamage, attackSpeed, settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        ItemStack itemStack = context.getStack();
        PlayerEntity player = context.getPlayer();
        BlockPos blockPos = context.getBlockPos();
        Hand hand = context.getHand();
        if (!world.isClient() && player != null) {
            MagicBroomEntity entity = new MagicBroomEntity(ModEntities.BROOM_ENTITY_TYPE, world, blockPos.getX(), blockPos.getY() + 1, blockPos.getZ(), ItemStackWrapper.of(itemStack.copy()), player.getUuid().toString().intern());
            world.spawnEntity(entity);
            itemStack.decrementUnlessCreative(1, player);
            player.swingHand(hand);
            return ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.SUCCESS;
    }

}
