package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.entity.Scarecrow;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public class ScarecrowItem extends Item {
    public ScarecrowItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack itemStack = context.getItemInHand();
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockState blockState = level.getBlockState(blockPos);
        if (player == null) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide() && level instanceof ServerLevel world) {
            BlockPos blockPos2 = blockState.getCollisionShape(level, blockPos).isEmpty() ? blockPos : blockPos.relative(direction);

            itemStack.consume(1, player);
            boolean b = Objects.equals(blockPos, blockPos2) && direction == Direction.UP;
            Scarecrow entity = RDEntityTypes.SCARECROW.value().spawn(
                    world,
                    null,
                    player,
                    blockPos2,
                    EntitySpawnReason.SPAWN_ITEM_USE,
                    true,
                    !b);
            if (entity != null) {
                double dx = player.getX() - entity.getX();
                double dz = player.getZ() - entity.getZ();
                float yaw = (float) (Math.atan2(dz, dx) * (180 / Math.PI)) - 90f;

                entity.setYRot(yaw);
                entity.setYBodyRot(yaw);
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }
}
