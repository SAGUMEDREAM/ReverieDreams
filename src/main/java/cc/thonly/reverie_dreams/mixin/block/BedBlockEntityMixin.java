package cc.thonly.reverie_dreams.mixin.block;

import cc.thonly.reverie_dreams.inf.IBedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        this.hasDreamPillow = view.getBooleanOr("HasDreamPillow", false);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
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
