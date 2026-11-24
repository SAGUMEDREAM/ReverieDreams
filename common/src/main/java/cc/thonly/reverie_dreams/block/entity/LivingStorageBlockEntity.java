package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.block.decor.living.LivingStorageBlock;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Ordinary 27-slot vanilla container: persistent inventory and vanilla removal drops.
 */
public class LivingStorageBlockEntity extends BaseContainerBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private final ContainerOpenersCounter openers = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            updateOpen(level, pos, state, true);
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            updateOpen(level, pos, state, false);
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int before, int after) {
        }

        @Override
        public boolean isOwnContainer(Player player) {
            return player.containerMenu instanceof ChestMenu menu && menu.getContainer() == LivingStorageBlockEntity.this;
        }
    };

    public LivingStorageBlockEntity(BlockPos pos, BlockState state) {
        this(RDBlockEntityTypes.LIVING_STORAGE.get(), pos, state);
    }

    protected LivingStorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private void updateOpen(Level level, BlockPos pos, BlockState state, boolean open) {
        level.setBlock(pos, state.setValue(LivingStorageBlock.OPEN, open), 3);
        level.playSound(null, pos, open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 0.45f, 0.9f);
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return ChestMenu.threeRows(id, inventory, this);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
    }

    @Override
    public void startOpen(ContainerUser user) {
        if (!remove && !user.getLivingEntity().isSpectator())
            openers.incrementOpeners(user.getLivingEntity(), level, worldPosition, getBlockState(), user.getContainerInteractionRange());
    }

    @Override
    public void stopOpen(ContainerUser user) {
        if (!remove && !user.getLivingEntity().isSpectator())
            openers.decrementOpeners(user.getLivingEntity(), level, worldPosition, getBlockState());
    }

    @Override
    public List<ContainerUser> getEntitiesWithContainerOpen() {
        return openers.getEntitiesWithContainerOpen(level, worldPosition);
    }

    public void recheckOpen() {
        if (!remove)
            openers.recheckOpeners(level, worldPosition, getBlockState());
    }
}
