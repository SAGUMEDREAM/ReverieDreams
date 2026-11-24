package cc.thonly.reverie_dreams.entity.ai.work;

import cc.thonly.reverie_dreams.inventory.NPCWorkInventory;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * All checks happen at execution, including a hand that the player may have changed in transit.
 */
public final class AnimalFeeding {
    private AnimalFeeding() {
    }

    public static int foodCount(Container inventory, Predicate<ItemStack> food) {
        int count = NPCWorkInventory.count(inventory, food);
        if (inventory.getContainerSize() >= 24)
            for (int i = 22; i < 24; i++)
                if (food.test(inventory.getItem(i)))
                    count += inventory.getItem(i).getCount();
        return count;
    }

    public static boolean feed(Container inventory, int slot, Predicate<ItemStack> food, BooleanSupplier eligible,
                               int reserve, Consumer<ItemStack> consume, Runnable love) {
        if (slot != 22 && slot != 23 || slot >= inventory.getContainerSize() || !eligible.getAsBoolean())
            return false;
        var stack = inventory.getItem(slot);
        if (stack.isEmpty() || !food.test(stack) || foodCount(inventory, food) <= Math.max(0, reserve))
            return false;
        int before = stack.getCount();
        consume.accept(stack);
        if (stack.getCount() != before - 1)
            return false;
        love.run();
        inventory.setChanged();
        return true;
    }

    public static boolean withinLimit(int living, int inLove, int maximum) {
        return living + (inLove + 1) / 2 < maximum;
    }
}
