package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.interfaces.IBedBlockEntity;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class DreamPillowItem extends Item {

    public DreamPillowItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        if (!world.isClient && world instanceof ServerWorld serverWorld && player instanceof ServerPlayerEntity) {
            boolean sneaking = player.isSneaking();
            ItemStack itemStack = player.getStackInHand(context.getHand());
            BlockPos blockPos = context.getBlockPos();
            Pair<Boolean, BlockPos> bedHead = getBedHead(serverWorld, blockPos);
            if (sneaking && bedHead.getLeft() && serverWorld.getBlockEntity(bedHead.getRight()) instanceof BedBlockEntity blockEntity) {
                IBedBlockEntity iBedBlockEntity = (IBedBlockEntity) blockEntity;
                if (iBedBlockEntity.hasDreamPillow()) {
                    return ActionResult.PASS;
                } else {
                    iBedBlockEntity.setHasDreamPillow(true);
                    itemStack.decrementUnlessCreative(1, player);
                    return ActionResult.SUCCESS_SERVER;
                }

            }
            return ActionResult.FAIL;
        }
        return ActionResult.SUCCESS;
    }

    public static Pair<Boolean, BlockPos> getBedHead(ServerWorld serverWorld, BlockPos blockPos) {
        BlockState blockState = serverWorld.getBlockState(blockPos);
        if (blockState.getBlock() instanceof BedBlock) {
            BedPart bedPart = blockState.get(BedBlock.PART);
            Direction direction = blockState.get(HorizontalFacingBlock.FACING);
            BlockPos headPos;

            if (bedPart == BedPart.HEAD) {
                headPos = blockPos;
            } else {
                headPos = blockPos.offset(direction);
            }
            return new Pair<>(true, headPos);
        }
        return new Pair<>(false, null);
    }

}
