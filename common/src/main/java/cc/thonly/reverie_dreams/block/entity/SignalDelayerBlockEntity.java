package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.block.props.SignalDelayerBlock;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

@Setter
@Getter
public class SignalDelayerBlockEntity extends BlockEntity {
    private int nowTick = -1;
    private int maxDelayTick = 0;

    public SignalDelayerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(RDBlockEntityTypes.SIGNAL_DELAYER_BLOCK_ENTITY.value(), blockPos, blockState);
    }

    public static void onBlockEntityTick(Level level, BlockPos pos, BlockState state, SignalDelayerBlockEntity be) {
        if (level.isClientSide()) return;

        if (be.maxDelayTick <= 0) return;

        if (state.getValue(SignalDelayerBlock.OCCUPIED)) {
            be.nowTick++;

            if (be.nowTick >= be.maxDelayTick) {
                level.setBlock(pos, state
                        .setValue(SignalDelayerBlock.POWERED, true), 3);

                be.nowTick = 0;
            }

        }
    }


    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        this.nowTick = view.getIntOr("NowTick", 0);
        this.maxDelayTick = view.getIntOr("MaxDelayTick", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        view.putInt("NowTick", this.nowTick);
        view.putInt("MaxDelayTick", this.maxDelayTick);
    }

}
