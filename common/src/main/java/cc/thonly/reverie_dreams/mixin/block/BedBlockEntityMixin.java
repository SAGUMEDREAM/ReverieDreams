package cc.thonly.reverie_dreams.mixin.block;

import cc.thonly.reverie_dreams.api.entity.BedBlockEntityDreamPillow;
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
public class BedBlockEntityMixin extends BlockEntity implements BedBlockEntityDreamPillow {
    @Unique
    boolean reverie_dream$hasDreamPillow = false;

    public BedBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        this.reverie_dream$hasDreamPillow = view.getBooleanOr("HasDreamPillow", false);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        view.putBoolean("HasDreamPillow", this.reverie_dream$hasDreamPillow);
    }

    @Override
    public void reverie_dreams$setHasDreamPillow(boolean value) {
        this.reverie_dream$hasDreamPillow = value;
    }

    @Override
    public boolean reverie_dreams$hasDreamPillow() {
        return this.reverie_dream$hasDreamPillow;
    }
}
