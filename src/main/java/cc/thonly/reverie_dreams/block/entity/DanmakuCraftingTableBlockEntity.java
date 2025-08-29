package cc.thonly.reverie_dreams.block.entity;

import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;

@Getter
public class DanmakuCraftingTableBlockEntity extends BlockEntity {
    private SimpleInventory inventory = new SimpleInventory(5);

    public DanmakuCraftingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DANMAKU_CRAFTING_TABLE_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, inventory.heldStacks);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        SimpleInventory inventory = new SimpleInventory(6);
        Inventories.readData(view, inventory.heldStacks);
        this.inventory = inventory;
    }

}