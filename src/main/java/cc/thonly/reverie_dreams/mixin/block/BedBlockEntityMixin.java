package cc.thonly.reverie_dreams.mixin.block;

import cc.thonly.reverie_dreams.inf.IBedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BedBlockEntity.class)
public class BedBlockEntityMixin extends BlockEntity implements IBedBlockEntity {
    @Unique
    boolean hasDreamPillow = false;

    public BedBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        this.hasDreamPillow = compoundTag.getBoolean("HasDreamPillow");
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        compoundTag.putBoolean("HasDreamPillow", this.hasDreamPillow);
    }

    @Override
    public void setHasDreamPillow(boolean value) {
        this.hasDreamPillow = value;
    }

    @Override
    public boolean hasDreamPillow() {
        return this.hasDreamPillow;
    }
}
