package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.polymer.item.IBasicPolymerItem;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CrossingChisel extends Item implements IBasicPolymerItem {
    public static final Integer DEFAULT_VALUE = 16;

    public CrossingChisel(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Level world = context.getLevel();
        Player player = context.getPlayer();
        BlockPos origin = context.getClickedPos();
        Direction direction = context.getClickedFace();
        Integer maxDistance = stack.get(RDDataComponents.MAX_DISTANCE);
        maxDistance = maxDistance != null ? maxDistance : DEFAULT_VALUE;

        if (player != null) {
            BlockPos targetPos = getTravelPos(world, origin, direction, maxDistance);
            if (targetPos != null && isSafePos(world, targetPos.above())) {
                if(!world.isClientSide()) {
                    player.teleportTo(
                            targetPos.getX() + 0.5,
                            targetPos.getY(),
                            targetPos.getZ() + 0.5
                    );

                    world.levelEvent(2001, origin, 0);
                    world.playSound(null, targetPos, SoundEvents.CHORUS_FRUIT_TELEPORT, player.getSoundSource(), 1.0f, 1.0f);
                    player.swing(context.getHand());
                    if (stack.isDamageableItem() && !player.hasInfiniteMaterials()) {
                        stack.hurtWithoutBreaking(1, player);
                    }
                    return InteractionResult.SUCCESS_SERVER;
                } else {
                    return InteractionResult.SUCCESS;
                }
            }

            return InteractionResult.FAIL;
        }

        return InteractionResult.PASS;
    }

    private static BlockPos getTravelPos(Level world, BlockPos origin, Direction direction, Integer maxDistance) {
        Direction travelDir = direction.getOpposite();

        for (int i = 0; i < maxDistance+1; i++) {
            BlockPos current = origin.relative(travelDir, i);
            BlockState state = world.getBlockState(current);
            Block block = state.getBlock();

            if (block == Blocks.BEDROCK || block == Blocks.OBSIDIAN || state.getDestroySpeed(world, current) < 0) {
                continue;
            }

            if (isSafePos(world, current)) {
                if (travelDir == Direction.DOWN && !isSafePos(world, current.below())) {
                    continue;
                }
                return travelDir == Direction.UP ? current : current.below();
            }
        }

        return null;
    }

    private static boolean isSafePos(Level world, BlockPos pos) {
        return world.getBlockState(pos).isAir();
    }

}
