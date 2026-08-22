package cc.thonly.reverie_dreams.inventory;

import cc.thonly.reverie_dreams.registry.SerializableProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InfiniteInventory implements Container, StackedContentsCompatible {
    private final List<Entry> entries = new ArrayList<>();

    @Getter
    private final int maxSize;

    public InfiniteInventory(int maxSize) {
        this.maxSize = maxSize;
    }

    public static void saveAllItems(ValueOutput view, InfiniteInventory inventory) {
        ValueOutput.TypedOutputList<Entry> list =
                view.list("Items", Entry.CODEC);

        for (Entry entry : inventory.entries) {
            if (entry.itemStack().isEmpty()) {
                continue;
            }

            if (entry.count() <= 0) {
                continue;
            }

            list.add(entry);
        }
    }

    public static void loadAllItems(ValueInput view, InfiniteInventory inventory) {
        inventory.clearContent();

        for (Entry entry : view.listOrEmpty("Items", Entry.CODEC)) {
            if (entry.index() < 0 || entry.index() >= inventory.maxSize) {
                continue;
            }

            if (entry.itemStack().isEmpty()) {
                continue;
            }

            if (entry.count() <= 0) {
                continue;
            }

            ItemStack itemStack = entry.itemStack().get();

            inventory.setEntry(
                    entry.index(),
                    itemStack,
                    entry.count()
            );
        }
    }

    @Override
    public int getContainerSize() {
        return this.maxSize;
    }

    @Override
    public boolean isEmpty() {
        for (Entry entry : this.entries) {
            if (entry.itemStack().isEmpty()) {
                continue;
            }

            if (entry.count() <= 0) {
                continue;
            }

            return false;
        }

        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        Entry entry = this.getEntry(slot);
        if (entry == null) {
            return ItemStack.EMPTY;
        }

        Optional<ItemStack> optional = entry.itemStack();
        if (optional.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (entry.count() <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack result = optional.get().copy();
        result.setCount(Math.min(entry.count(), result.getMaxStackSize()));

        return result;
    }

    public ItemStack getSingleItem(int slot) {
        Entry entry = this.getEntry(slot);

        if (entry == null) {
            return ItemStack.EMPTY;
        }

        Optional<ItemStack> optional = entry.itemStack();

        return optional.map(itemStack -> itemStack.copyWithCount(1)).orElse(ItemStack.EMPTY);

    }

    public int getItemCount(int slot) {
        Entry entry = this.getEntry(slot);

        if (entry == null) {
            return 0;
        }

        if (entry.itemStack().isEmpty()) {
            return 0;
        }

        return Math.max(entry.count(), 0);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        if (count <= 0) {
            return ItemStack.EMPTY;
        }

        Entry entry = this.getEntry(slot);

        if (entry == null) {
            return ItemStack.EMPTY;
        }

        Optional<ItemStack> optional = entry.itemStack();

        if (optional.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (entry.count() <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack source = optional.get();

        int removed = Math.min(
                count,
                entry.count()
        );

        ItemStack result = source.copy();

        result.setCount(
                Math.min(
                        removed,
                        result.getMaxStackSize()
                )
        );

        int remaining = entry.count() - removed;

        if (remaining <= 0) {
            this.clearSlot(slot);
        } else {
            this.setEntry(
                    slot,
                    source,
                    remaining
            );
        }

        this.setChanged();

        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        Entry entry = this.getEntry(slot);

        if (entry == null) {
            return ItemStack.EMPTY;
        }

        Optional<ItemStack> optional = entry.itemStack();

        if (optional.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (entry.count() <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack result = optional.get().copy();

        result.setCount(
                Math.min(
                        entry.count(),
                        result.getMaxStackSize()
                )
        );

        this.clearSlot(slot);

        return result;
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        if (slot < 0 || slot >= this.maxSize) {
            return;
        }

        if (itemStack == null || itemStack.isEmpty()) {
            this.clearSlot(slot);
            return;
        }

        this.setEntry(
                slot,
                itemStack.copyWithCount(1),
                itemStack.getCount()
        );

        this.setChanged();
    }

    public void setItem(
            int slot,
            ItemStack itemStack,
            int count
    ) {
        if (slot < 0 || slot >= this.maxSize) {
            return;
        }

        if (itemStack == null || itemStack.isEmpty() || count <= 0) {
            this.clearSlot(slot);
            return;
        }

        this.setEntry(
                slot,
                itemStack.copyWithCount(1),
                count
        );

        this.setChanged();
    }

    public int addItemAndGetInserted(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return 0;
        }

        int remaining = itemStack.getCount();

        for (int i = 0; i < this.maxSize && remaining > 0; i++) {
            Entry entry = this.getEntry(i);

            if (entry == null) {
                continue;
            }

            Optional<ItemStack> optional = entry.itemStack();

            if (optional.isEmpty()) {
                continue;
            }

            ItemStack stored = optional.get();

            if (!ItemStack.isSameItemSameComponents(stored, itemStack)) {
                continue;
            }

            this.setEntry(
                    i,
                    stored,
                    entry.count() + remaining
            );

            remaining = 0;
        }

        for (int i = 0; i < this.maxSize && remaining > 0; i++) {
            Entry entry = this.getEntry(i);

            if (entry != null && entry.itemStack().isPresent()) {
                continue;
            }

            this.setEntry(
                    i,
                    itemStack.copyWithCount(1),
                    remaining
            );

            remaining = 0;
        }

        int inserted = itemStack.getCount() - remaining;

        if (inserted > 0) {
            this.setChanged();
        }

        return inserted;
    }

    public void addItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return;
        }

        int remaining = itemStack.getCount();

        for (int i = 0; i < this.maxSize && remaining > 0; i++) {
            Entry entry = this.getEntry(i);

            if (entry == null) {
                continue;
            }

            Optional<ItemStack> optional = entry.itemStack();

            if (optional.isEmpty()) {
                continue;
            }

            ItemStack stored = optional.get();

            if (!ItemStack.isSameItemSameComponents(
                    stored,
                    itemStack
            )) {
                continue;
            }

            this.setEntry(
                    i,
                    stored.copyWithCount(1),
                    entry.count() + remaining
            );

            remaining = 0;
        }

        for (int i = 0; i < this.maxSize && remaining > 0; i++) {
            Entry entry = this.getEntry(i);

            if (entry != null && entry.itemStack().isPresent()) {
                continue;
            }

            this.setEntry(
                    i,
                    itemStack.copyWithCount(1),
                    remaining
            );

            remaining = 0;
        }

        this.setChanged();
    }

    public boolean contains(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }

        for (Entry entry : this.entries) {
            Optional<ItemStack> optional = entry.itemStack();

            if (optional.isEmpty()) {
                continue;
            }

            if (entry.count() <= 0) {
                continue;
            }

            ItemStack stored = optional.get();

            if (ItemStack.isSameItemSameComponents(
                    stored,
                    itemStack
            )) {
                return true;
            }
        }

        return false;
    }

    public int getCount(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return 0;
        }

        int count = 0;

        for (Entry entry : this.entries) {
            Optional<ItemStack> optional = entry.itemStack();

            if (optional.isEmpty()) {
                continue;
            }

            if (entry.count() <= 0) {
                continue;
            }

            ItemStack stored = optional.get();

            if (ItemStack.isSameItemSameComponents(
                    stored,
                    itemStack
            )) {
                count += entry.count();
            }
        }

        return count;
    }

    public Entry getEntry(int slot) {
        if (slot < 0 || slot >= this.maxSize) {
            return null;
        }

        if (slot >= this.entries.size()) {
            return null;
        }

        return this.entries.get(slot);
    }

    private void setEntry(
            int slot,
            ItemStack itemStack,
            int count
    ) {
        if (slot < 0 || slot >= this.maxSize) {
            return;
        }

        if (itemStack == null || itemStack.isEmpty() || count <= 0) {
            this.clearSlot(slot);
            return;
        }

        while (this.entries.size() <= slot) {
            this.entries.add(
                    new Entry(
                            this.entries.size(),
                            Optional.empty(),
                            0
                    )
            );
        }

        this.entries.set(
                slot,
                new Entry(
                        slot,
                        Optional.of(itemStack.copyWithCount(1)),
                        count
                )
        );
    }

    private void clearSlot(int slot) {
        if (slot < 0 || slot >= this.maxSize) {
            return;
        }

        while (this.entries.size() <= slot) {
            this.entries.add(
                    new Entry(
                            this.entries.size(),
                            Optional.empty(),
                            0
                    )
            );
        }

        this.entries.set(
                slot,
                new Entry(
                        slot,
                        Optional.empty(),
                        0
                )
        );
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        this.entries.clear();
        this.setChanged();
    }

    @Override
    public void fillStackedContents(
            StackedItemContents contents
    ) {
        for (Entry entry : this.entries) {
            Optional<ItemStack> optional = entry.itemStack();

            if (optional.isEmpty()) {
                continue;
            }

            if (entry.count() <= 0) {
                continue;
            }

            ItemStack stored = optional.get();

            int remaining = entry.count();

            while (remaining > 0) {
                int amount = Math.min(
                        remaining,
                        stored.getMaxStackSize()
                );

                ItemStack stack =
                        stored.copyWithCount(amount);

                contents.accountStack(stack);

                remaining -= amount;
            }
        }
    }

    public List<Entry> getEntries() {
        return List.copyOf(this.entries);
    }

    public record Entry(
            int index,
            Optional<ItemStack> itemStack,
            int count
    ) {
        public static final Codec<Entry> CODEC =
                RecordCodecBuilder.create(instance ->
                        instance.group(Codec.INT.fieldOf("index").forGetter(Entry::index),
                                SerializableProvider.ITEM_STACK_CODEC.optionalFieldOf("item").forGetter(Entry::itemStack),
                                Codec.INT.fieldOf("count").forGetter(Entry::count)
                        ).apply(instance, Entry::new)
                );
    }
}