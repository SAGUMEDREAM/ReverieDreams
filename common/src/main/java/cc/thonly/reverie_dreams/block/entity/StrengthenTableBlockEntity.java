package cc.thonly.reverie_dreams.block.entity;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

@Getter
public class StrengthenTableBlockEntity extends BlockEntity {
    private SimpleContainer inventory = new SimpleContainer(2);

    public StrengthenTableBlockEntity(BlockPos pos, BlockState state) {
        super(RDBlockEntityTypes.STRENGTH_TABLE.value(), pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        ContainerHelper.saveAllItems(view, this.inventory.getItems());
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        SimpleContainer inventory = new SimpleContainer(2);
        ContainerHelper.loadAllItems(view, inventory.getItems());
        this.inventory = inventory;
    }
}