package cc.thonly.reverie_dreams.inventory;

import lombok.Getter;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

@Getter
public class NPCInventoryImpl extends SimpleContainer {
    public static final int MAX_SIZE = 24;
    public static final int HEAD = 18;
    public static final int CHEST = 19;
    public static final int LEGS = 20;
    public static final int FEET = 21;
    public static final int MAIN_HAND = 22;
    public static final int OFF_HAND = 23;

    public NPCInventoryImpl(int size) {
        super(size);
        init();
    }

    public NPCInventoryImpl(ItemStack... items) {
        super(items);
        init();
    }

    protected void init() {

    }

    public ItemStack getHand(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            return getMainHand();
        } else if (hand == InteractionHand.OFF_HAND) {
            return getOffHand();
        }
        return ItemStack.EMPTY;
    }

    public ItemStack getMainHand() {
        return this.getItem(MAIN_HAND);
    }

    public ItemStack getOffHand() {
        return this.getItem(OFF_HAND);
    }

    public ItemStack getHead() {
        return this.getItem(HEAD);
    }

    public ItemStack getChest() {
        return this.getItem(CHEST);
    }

    public ItemStack getLegs() {
        return this.getItem(LEGS);
    }

    public ItemStack getFeet() {
        return this.getItem(FEET);
    }

    public void setHand(InteractionHand hand, ItemStack stack) {
        if (hand == InteractionHand.MAIN_HAND) {
            setMainHand(stack);
        } else if (hand == InteractionHand.OFF_HAND) {
            setOffHand(stack);
        }
    }

    public void setMainHand(ItemStack stack) {
        this.setItem(MAIN_HAND, stack.copy());
    }

    public void setOffHand(ItemStack stack) {
        this.setItem(OFF_HAND, stack.copy());
    }

    public void setHead(ItemStack stack) {
        this.setItem(HEAD, stack.copy());
    }

    public void setChest(ItemStack stack) {
        this.setItem(CHEST, stack.copy());
    }

    public void setLegs(ItemStack stack) {
        this.setItem(LEGS, stack.copy());
    }

    public void setFeet(ItemStack stack) {
        this.setItem(FEET, stack.copy());
    }

    public boolean isArmorSlot(int slot) {
        return HEAD == slot || CHEST == slot || LEGS == slot || FEET == slot;
    }

    @Override
    public boolean canAddItem(ItemStack stack) {
        return super.canAddItem(stack);
    }

    public int insertStack(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        int remaining = stack.getCount();
        int inserted = 0;
        NonNullList<ItemStack> items = this.getItems();
        for (int i = 0; i < items.size(); i++) {
            if (this.isArmorSlot(i)) {
                continue;
            }
            ItemStack slot = items.get(i);
            if (slot.isEmpty()) {
                int toInsert = Math.min(remaining, stack.getMaxStackSize());
                items.set(i, stack.copy());
                items.get(i).setCount(toInsert);
                inserted += toInsert;
                remaining -= toInsert;
            } else if (ItemStack.isSameItemSameComponents(slot, stack) && slot.getCount() < slot.getMaxStackSize()) {
                int space = slot.getMaxStackSize() - slot.getCount();
                int toInsert = Math.min(remaining, space);
                slot.grow(toInsert);
                inserted += toInsert;
                remaining -= toInsert;
            }
            if (remaining <= 0) break;
        }

        stack.shrink(inserted);
        return inserted;
    }

    @NotNull
    public List<Integer> findItemSlots(int maxLength, Predicate<ItemStack> isGood, Predicate<Integer> isExcludeIndex) {
        List<Integer> itemSlots = new LinkedList<>();
        for (int i = MAX_SIZE - 1; i >= 0; i--) {
            if (itemSlots.size() >= maxLength) break;
            ItemStack stack = this.getItem(i);
            if ((!stack.isEmpty()) && (!isExcludeIndex.test(i)) && isGood.test(stack))
                itemSlots.add(i);
        }
        return itemSlots;
    }

    public Integer findSlot(Predicate<ItemStack> isGood) {
        List<Integer> slots = findItemSlots(1, isGood, (i) -> i >= 18 && i <= 21);
        return slots.isEmpty() ? null : slots.getFirst();
    }

    public Integer findHand(Predicate<ItemStack> isGood) {
        return isGood.test(getItem(23))
                ? Integer.valueOf(23) : isGood.test(getItem(22)) ? 22 : null;
    }
}
