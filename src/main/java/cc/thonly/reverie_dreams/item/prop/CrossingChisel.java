package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class CrossingChisel extends Item {
    public static final Integer DEFAULT_VALUE = 16;

    public CrossingChisel(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ItemStack stack = context.getStack();
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        BlockPos origin = context.getBlockPos();
        Direction direction = context.getSide();
        Integer maxDistance = stack.get(ModDataComponentTypes.MAX_DISTANCE);
        maxDistance = maxDistance != null ? maxDistance : DEFAULT_VALUE;

        if (player != null) {
            BlockPos targetPos = getTravelPos(world, origin, direction, maxDistance);
            if (targetPos != null && isSafePos(world, targetPos.up())) {
                if(!world.isClient()) {
                    player.requestTeleport(
                            targetPos.getX() + 0.5,
                            targetPos.getY(),
                            targetPos.getZ() + 0.5
                    );

                    world.syncWorldEvent(2001, origin, 0);
                    world.playSound(null, targetPos, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, player.getSoundCategory(), 1.0f, 1.0f);
                    player.swingHand(context.getHand());
                    if (stack.isDamageable() && !player.isInCreativeMode()) {
                        stack.damage(1, player);
                    }
                    return ActionResult.SUCCESS_SERVER;
                } else {
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }

    private static BlockPos getTravelPos(World world, BlockPos origin, Direction direction, Integer maxDistance) {
        Direction travelDir = direction.getOpposite();

        for (int i = 0; i < maxDistance+1; i++) {
            BlockPos current = origin.offset(travelDir, i);
            BlockState state = world.getBlockState(current);
            Block block = state.getBlock();

            if (block == Blocks.BEDROCK || block == Blocks.OBSIDIAN || state.getHardness(world, current) < 0) {
                continue;
            }

            if (isSafePos(world, current)) {
                if (travelDir == Direction.DOWN && !isSafePos(world, current.down())) {
                    continue;
                }
                return travelDir == Direction.UP ? current : current.down();
            }
        }

        return null;
    }

    private static boolean isSafePos(World world, BlockPos pos) {
        return world.getBlockState(pos).isAir();
    }

}
