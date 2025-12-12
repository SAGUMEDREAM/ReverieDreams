package cc.thonly.reverie_dreams.block.entity;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Getter
public class StrengthenTableBlockEntity extends BlockEntity {
    private SimpleContainer inventory = new SimpleContainer(2);

    public StrengthenTableBlockEntity(BlockPos pos, BlockState state) {
        super(RDBlockEntityTypes.STRENGTH_TABLE_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        ContainerHelper.saveAllItems(compoundTag, inventory.items, provider);
    }

    @Override
    public void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        SimpleContainer inventory = new SimpleContainer(2);
        ContainerHelper.loadAllItems(compoundTag, inventory.items, provider);
        this.inventory = inventory;
    }
}