package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.api.entity.BedBlockEntityDataModifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

public class DreamPillowItem extends Item {

    public DreamPillowItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld && player instanceof ServerPlayer) {
            boolean sneaking = player.isShiftKeyDown();
            ItemStack itemStack = player.getItemInHand(context.getHand());
            BlockPos blockPos = context.getClickedPos();
            Tuple<Boolean, BlockPos> bedHead = getBedHead(serverWorld, blockPos);
            if (sneaking && bedHead.getA() && serverWorld.getBlockEntity(bedHead.getB()) instanceof BedBlockEntity blockEntity) {
                BedBlockEntityDataModifier bedBlockEntityDataModifier = (BedBlockEntityDataModifier) blockEntity;
                if (bedBlockEntityDataModifier.reverie_dreams$hasDreamPillow()) {
                    return InteractionResult.PASS;
                } else {
                    bedBlockEntityDataModifier.reverie_dreams$setHasDreamPillow(true);
                    itemStack.consume(1, player);
                    return InteractionResult.SUCCESS_SERVER;
                }

            }
            return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }

    public static Tuple<Boolean, BlockPos> getBedHead(ServerLevel serverWorld, BlockPos blockPos) {
        BlockState blockState = serverWorld.getBlockState(blockPos);
        if (blockState.getBlock() instanceof BedBlock) {
            BedPart bedPart = blockState.getValue(BedBlock.PART);
            Direction direction = blockState.getValue(HorizontalDirectionalBlock.FACING);
            BlockPos headPos;

            if (bedPart == BedPart.HEAD) {
                headPos = blockPos;
            } else {
                headPos = blockPos.relative(direction);
            }
            return new Tuple<>(true, headPos);
        }
        return new Tuple<>(false, null);
    }

}
