package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.block.decor.workshop.*;
import cc.thonly.reverie_dreams.workshop.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.*;

import java.util.*;

/**
 * Keeps the existing living_storage ID, slots and item format for old worlds.
 */
public class WorkshopStorageBlockEntity extends LivingStorageBlockEntity implements WorkshopDisplayHost {
    private Runnable displayUpdate;
    private boolean batching;
    private BlockPos restaurantPos;
    private UUID restaurantId, manager;

    public WorkshopStorageBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    protected WorkshopStorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public WorkshopKind workshopKind() {
        return ((WorkshopStorageBlock) getBlockState().getBlock()).kind();
    }

    @Override
    public List<ItemStack> displayItems() {
        var result = new ArrayList<ItemStack>();
        for (int slot = 0; slot < getContainerSize() && result.size() < 6; slot++) {
            var stack = getItem(slot);
            if (!stack.isEmpty() && WorkshopItems.displayable(workshopKind(), stack))
                result.add(stack.copyWithCount(1));
        }
        return List.copyOf(result);
    }

    public boolean insertOne(ItemStack held, Player player) {
        if (!WorkshopItems.displayable(workshopKind(), held))
            return false;
        for (int slot = 0; slot < getContainerSize(); slot++) {
            var current = getItem(slot);
            if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, held) && current.getCount() < current.getMaxStackSize()) {
                current.grow(1);
                held.consume(1, player);
                setChanged();
                FurnitureSounds.transfer(this, workshopKind(), false);
                return true;
            }
        }
        for (int slot = 0; slot < getContainerSize(); slot++)
            if (getItem(slot).isEmpty()) {
                setItem(slot, held.copyWithCount(1));
                held.consume(1, player);
                FurnitureSounds.transfer(this, workshopKind(), false);
                return true;
            }
        return false;
    }

    public boolean takeDisplay(Player player) {
        for (int slot = 0; slot < getContainerSize(); slot++)
            if (!getItem(slot).isEmpty() && WorkshopItems.displayable(workshopKind(), getItem(slot))) {
                WorkshopItems.give(player, removeItem(slot, 1));
                FurnitureSounds.transfer(this, workshopKind(), true);
                return true;
            }
        return false;
    }

    public void sortContents() {
        var stacks = new ArrayList<ItemStack>();
        for (int i = 0; i < getContainerSize(); i++)
            stacks.add(getItem(i).copy());
        stacks.sort(Comparator.comparing(ItemStack::isEmpty).thenComparing(s -> !WorkshopItems.displayable(workshopKind(), s)).thenComparing(s -> BuiltInRegistries.ITEM.getKey(s.getItem()).toString()));
        boolean changed = false;
        for (int i = 0; i < stacks.size(); i++)
            if (!ItemStack.matches(stacks.get(i), getItem(i))) {
                changed = true;
                break;
            }
        if (!changed)
            return;
        batching = true;
        try {
            for (int i = 0; i < stacks.size(); i++)
                setItem(i, stacks.get(i));
        } finally {
            batching = false;
            setChanged();
        }
        FurnitureSounds.sort(this);
    }

    public boolean canManage(Player player) {
        return !player.isSpectator() && player.mayBuild() && (manager == null || manager.equals(player.getUUID()) || player.isCreative());
    }

    public void bindRestaurant(RestaurantBlockEntity bell, Player player) {
        if (!canManage(player) || bell == null || !bell.isBell() || !bell.canEdit(player) || !bell.ready() || !bell.area().contains(worldPosition))
            throw new IllegalArgumentException("Cannot bind restaurant");
        bell.claim(player);
        restaurantPos = bell.getBlockPos().immutable();
        restaurantId = bell.id();
        manager = player.getUUID();
        setChanged();
    }

    public void unbindRestaurant(Player player) {
        if (canManage(player)) {
            restaurantPos = null;
            restaurantId = null;
            setChanged();
        }
    }

    public RestaurantBlockEntity restaurant() {
        return level != null && restaurantPos != null && level.hasChunkAt(restaurantPos) && level.getBlockEntity(restaurantPos) instanceof RestaurantBlockEntity bell
                && bell.isBell() && bell.id().equals(restaurantId) && bell.area().contains(worldPosition) ? bell : null;
    }

    @Override
    public void setDisplayUpdate(Runnable callback) {
        displayUpdate = callback;
        if (callback != null)
            callback.run();
    }

    @Override
    public void clearDisplayUpdate(Runnable callback) {
        if (displayUpdate == callback)
            displayUpdate = null;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!batching && level instanceof ServerLevel server) {
            server.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
            if (displayUpdate != null)
                displayUpdate.run();
        }
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        if (restaurantPos != null && restaurantId != null) {
            out.putLong("Workshop.RestaurantPos", restaurantPos.asLong());
            out.putString("Workshop.RestaurantId", restaurantId.toString());
        }
        if (manager != null)
            out.putString("Workshop.Manager", manager.toString());
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        restaurantPos = in.getLong("Workshop.RestaurantPos").map(BlockPos::of).orElse(null);
        restaurantId = RestaurantBlockEntity.uuid(in.getStringOr("Workshop.RestaurantId", ""), null);
        manager = RestaurantBlockEntity.uuid(in.getStringOr("Workshop.Manager", ""), null);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setRemoved() {
        displayUpdate = null;
        super.setRemoved();
    }
}
