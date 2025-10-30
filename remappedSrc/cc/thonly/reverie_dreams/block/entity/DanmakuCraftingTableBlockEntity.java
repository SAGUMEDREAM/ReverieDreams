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
public class DanmakuCraftingTableBlockEntity extends BlockEntity {
    private SimpleContainer inventory = new SimpleContainer(5);

    public DanmakuCraftingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DANMAKU_CRAFTING_TABLE_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        ContainerHelper.saveAllItems(view, inventory.items);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        SimpleContainer inventory = new SimpleContainer(6);
        ContainerHelper.loadAllItems(view, inventory.items);
        this.inventory = inventory;
    }

}