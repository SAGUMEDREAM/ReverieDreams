package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.block.base.BaseFumoBlock;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

@Getter
@Setter
@ToString
public class MarisaHatBlock extends BaseFumoBlock {
    public MarisaHatBlock(Vec3 offsets, Properties settings) {
        super(offsets, settings);
    }

    public MarisaHatBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide) {
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }


}
