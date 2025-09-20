package cc.thonly.reverie_dreams.block.entity;

import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

@Getter
public class CustomChestBlockEntity extends BlockEntity implements SidedInventory {
    private SimpleInventory inventory = new SimpleInventory(27);

    public CustomChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CUSTOM_CHEST_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.inventory = new SimpleInventory(27);
        Inventories.readData(view, this.inventory.heldStacks);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, this.inventory.heldStacks);
    }

    @Override
    public int size() {
        return 27;
    }

    @Override
    public boolean isEmpty() {
        return this.inventory.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.inventory.getStack(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return this.inventory.removeStack(slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return this.inventory.removeStack(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.inventory.setStack(slot, stack);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return IntStream.range(0, this.size()).toArray();
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return true;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    @Override
    public void clear() {
        this.inventory.clear();
    }
}
