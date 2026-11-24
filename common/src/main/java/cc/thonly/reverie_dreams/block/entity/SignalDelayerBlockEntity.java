package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.block.redstone.SignalDelayerBlock;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import cc.thonly.reverie_dreams.block.redstone.RedstoneSettings;

@Setter
@Getter
public class SignalDelayerBlockEntity extends BlockEntity {
    private int nowTick = -1;
    private int maxDelayTick = 20;
    private long activationTick = -1;
    private boolean initialized;

    public SignalDelayerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(RDBlockEntityTypes.SIGNAL_DELAYER_BLOCK_ENTITY.value(), blockPos, blockState);
    }

    public static void onBlockEntityTick(Level level, BlockPos pos, BlockState state, SignalDelayerBlockEntity be) {
        if (level.isClientSide())
            return;

        if (!be.initialized) {
            be.initialized = true;
            ((SignalDelayerBlock) state.getBlock()).refreshInput(level, pos, state);
            state = level.getBlockState(pos);
            if (state.getValue(SignalDelayerBlock.OCCUPIED) && !state.getValue(SignalDelayerBlock.POWERED)) {
                if (be.activationTick < 0)
                    be.activationTick = level.getGameTime() + Math.max(0, be.maxDelayTick - be.nowTick);
                level.scheduleTick(pos, state.getBlock(), (int) Math.clamp(be.activationTick - level.getGameTime(), 1, be.maxDelayTick + 1));
            }
        }
    }

    public void setMaxDelayTick(int ticks) {
        maxDelayTick = RedstoneSettings.delay(ticks);
        nowTick = 0;
        activationTick = -1;
        setChanged();
        if (level != null && !level.isClientSide() && getBlockState().getBlock() instanceof SignalDelayerBlock block)
            block.restart(level, worldPosition, getBlockState());
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        initialized = false;
    }


    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        this.nowTick = view.getIntOr("NowTick", 0);
        this.maxDelayTick = RedstoneSettings.delay(view.getIntOr("MaxDelayTick", 20));
        this.activationTick = view.getLongOr("ActivationTick", -1);
        this.initialized = false;
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        view.putInt("NowTick", this.nowTick);
        view.putInt("MaxDelayTick", this.maxDelayTick);
        view.putLong("ActivationTick", this.activationTick);
    }

}
