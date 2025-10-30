package cc.thonly.reverie_dreams.inventory;

import lombok.Getter;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import java.util.function.Function;

@Getter
public class PredicateInventory extends SimpleContainer {
    public static final Factory ARMOR_SLOT_FACTORY = (size, predicate) -> new PredicateInventory(1, predicate);
    private final Function<ItemStack, Boolean> predicate;

    public PredicateInventory(Function<ItemStack, Boolean> predicate) {
        super(1);
        this.predicate = predicate;
    }

    public PredicateInventory(int size, Function<ItemStack, Boolean> predicate) {
        super(size);
        this.predicate = predicate;
    }

    @Override
    public boolean canAddItem(ItemStack stack) {
        return super.canAddItem(stack) && this.predicate.apply(stack);
    }

    public interface Factory {
        public PredicateInventory get(int size, Function<ItemStack, Boolean> predicate);
    }
}
