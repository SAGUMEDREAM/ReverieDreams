package cc.thonly.reverie_dreams.entity.ai.work;

import cc.thonly.reverie_dreams.inventory.NPCWorkInventory;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.BooleanSupplier;

import net.minecraft.world.item.BlockItem;

/**
 * Same-crop replacement is the commit point. Loot is only materialized after a successful write.
 */
public final class FarmTransaction {
    public enum Result {SUCCESS, NO_SEED, REFUSED, WRONG_CROP}

    private FarmTransaction() {
    }

    public static Result harvest(BlockState mature, BlockState replanted, ItemStack seed, Container inventory,
                                 List<ItemStack> generatedLoot, BiPredicate<BlockState, BlockState> commit, Consumer<ItemStack> deliver) {
        if (mature.getBlock() != replanted.getBlock() || seed.isEmpty()
                || !(seed.getItem() instanceof BlockItem blockItem)
                || blockItem.getBlock() != mature.getBlock())
            return Result.WRONG_CROP;
        var loot = new ArrayList<ItemStack>();
        for (var stack : generatedLoot)
            loot.add(stack.copy());
        int lootSeed = -1;
        for (int i = 0; i < loot.size(); i++)
            if (loot.get(i).is(seed.getItem()) && !loot.get(i).isEmpty()) {
                lootSeed = i;
                break;
            }
        int slot = lootSeed < 0 ? NPCWorkInventory.find(inventory, s -> s.is(seed.getItem())) : -1;
        if (lootSeed < 0 && slot < 0 && inventory.getContainerSize() >= 24) {
            if (inventory.getItem(22).is(seed.getItem()))
                slot = 22;
            else if (inventory.getItem(23).is(seed.getItem()))
                slot = 23;
        }
        if (lootSeed < 0 && slot < 0)
            return Result.NO_SEED;
        // Reserve only for this synchronous commit; restore the exact source on a rejected world write.
        ItemStack reserved = slot < 0 ? ItemStack.EMPTY : inventory.removeItem(slot, 1);
        if (!commit.test(mature, replanted)) {
            if (!reserved.isEmpty()) {
                var old = inventory.getItem(slot);
                if (old.isEmpty())
                    inventory.setItem(slot, reserved);
                else if (ItemStack.isSameItemSameComponents(old, reserved) && old.getCount() < old.getMaxStackSize()) {
                    inventory.setItem(slot, old.copyWithCount(old.getCount() + 1));
                } else {
                    NPCWorkInventory.insert(inventory, reserved);
                    if (!reserved.isEmpty())
                        deliver.accept(reserved);
                }
            }
            return Result.REFUSED;
        }
        if (lootSeed >= 0)
            loot.get(lootSeed).shrink(1);
        for (var stack : loot)
            if (!stack.isEmpty())
                deliver.accept(stack);
        inventory.setChanged();
        return Result.SUCCESS;
    }

    public static boolean plant(ItemStack expectedSeed, Container inventory, int slot, BooleanSupplier commit) {
        if (slot < 0 || slot >= inventory.getContainerSize()
                || slot >= NPCWorkInventory.CARGO_SIZE && slot != 22 && slot != 23 || expectedSeed.isEmpty())
            return false;
        var current = inventory.getItem(slot);
        if (current.isEmpty() || !ItemStack.isSameItemSameComponents(current, expectedSeed))
            return false;
        if (!commit.getAsBoolean())
            return false;
        inventory.setItem(slot, current.copyWithCount(current.getCount() - 1));
        inventory.setChanged();
        return true;
    }
}
