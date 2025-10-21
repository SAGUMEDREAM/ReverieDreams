package cc.thonly.reverie_dreams.mixin.block;

import cc.thonly.reverie_dreams.interfaces.IBedBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
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
    protected void readData(ReadView view) {
        super.readData(view);
        this.hasDreamPillow = view.getBoolean("HasDreamPillow", false);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putBoolean("HasDreamPillow", this.hasDreamPillow);
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
