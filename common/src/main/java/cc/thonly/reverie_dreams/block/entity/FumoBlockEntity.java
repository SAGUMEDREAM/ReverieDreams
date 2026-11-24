package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.block.base.BaseFumoBlock;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.util.FumoGrowth;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Rotation stays in block state; only cake growth is saved and synchronized here.
 */
public class FumoBlockEntity extends BlockEntity {
    private int cakesFed;
    private Runnable growthUpdate;

    public FumoBlockEntity(BlockPos pos, BlockState state) {
        this(RDBlockEntityTypes.FUMO.value(), pos, state);
    }

    protected FumoBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public float getYaw() {
        return this.getBlockState().getValue(BaseFumoBlock.FACING_16).getYaw();
    }

    public int getCakesFed() {
        return cakesFed;
    }

    public float getGrowthScale() {
        return getBlockState().getBlock() instanceof BaseFumoBlock block && block.supportsGrowth()
                ? FumoGrowth.scale(cakesFed) : 1f;
    }

    public void setCakesFed(int value) {
        int next = FumoGrowth.clamp(value);
        if (cakesFed == next)
            return;
        cakesFed = next;
        setChanged();
        if (level instanceof ServerLevel server) {
            server.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            if (growthUpdate != null)
                growthUpdate.run();
        }
    }

    public boolean feedCake() {
        if (cakesFed >= FumoGrowth.MAX_CAKES)
            return false;
        setCakesFed(cakesFed + 1);
        return true;
    }

    public void setGrowthUpdate(Runnable callback) {
        growthUpdate = callback;
        if (callback != null)
            callback.run();
    }

    public void clearGrowthUpdate(Runnable callback) {
        if (growthUpdate == callback)
            growthUpdate = null;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("CakesFed", cakesFed);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        cakesFed = FumoGrowth.clamp(input.getIntOr("CakesFed", 0));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setRemoved() {
        growthUpdate = null;
        super.setRemoved();
    }
}
