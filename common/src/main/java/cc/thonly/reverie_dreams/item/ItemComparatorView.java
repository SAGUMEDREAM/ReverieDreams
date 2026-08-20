package cc.thonly.reverie_dreams.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ItemComparatorView {
    boolean equals(Object obj);

    boolean matches(ItemComparatorView obj);

    default boolean matches(IngredientStack obj) {
        return this.matches(ItemComparatorView.of(obj));
    }

    boolean greaterThan(ItemComparatorView other);

    default boolean greaterThan(IngredientStack other) {
        return this.greaterThan(ItemComparatorView.of(other));
    }

    default boolean greaterThan(ItemStack other) {
        return this.greaterThan(ItemComparatorView.of(other));
    }

    boolean test(ItemComparatorView other);

    default boolean test(IngredientStack other) {
        return this.test(ItemComparatorView.of(other));
    }

    default boolean test(ItemStack other) {
        return this.test(ItemComparatorView.of(other));
    }

    default ItemComparatorView map(Consumer<ItemStack> function) {
        function.accept(this.stack());
        return this;
    }

    default ItemComparatorView map(Function<ItemStack, ItemStack> function) {
        this.stack(function.apply(this.stack()));
        return this;
    }

    ItemStack stack();

    ItemStack stack(ItemStack stack);

    static ItemComparatorView of(IngredientStack stack) {
        if (!stack.areComponentsBound()) {
            throw new RuntimeException("Component not bound");
        }
        return new StackComparatorViewImpl(stack);
    }

    static ItemComparatorView of(IngredientStackSet set) {
        return new StackSetComparatorViewImpl(set);
    }

    static ItemComparatorView of(ItemStack stack) {
        return new StackComparatorViewImpl(stack);
    }

    static ItemComparatorView of(ItemStack stack, List<TagKey<Item>> itemTags) {
        return new StackComparatorViewImpl(stack, itemTags);
    }
}
