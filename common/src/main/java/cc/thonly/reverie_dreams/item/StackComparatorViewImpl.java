package cc.thonly.reverie_dreams.item;

import lombok.ToString;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("ALL")
@ToString
public final class StackComparatorViewImpl implements ItemComparatorView {
    private final ItemStack itemStack;

    public StackComparatorViewImpl(IngredientStack stack) {
        this.itemStack = stack.build();
    }

    public StackComparatorViewImpl(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public StackComparatorViewImpl(ItemStack itemStack, List<TagKey<Item>> itemTags) {
        this.itemStack = itemStack;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ItemComparatorView other)) return false;
        return ItemStack.matches(this.stack(), other.stack());
    }

    @Override
    public boolean matches(ItemComparatorView other) {
        if (other == this) {
            return true;
        }
        if (ItemStack.matches(this.stack(), other.stack())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean test(ItemComparatorView other) {
        if (!ItemStack.isSameItem(this.stack(), other.stack())) {
            return false;
        }

        if (!this.stack().getComponentsPatch().isEmpty()) {
            return this.isSameItemSameComponents(this.stack(), other.stack());
        }

        return true;
    }

    private boolean isSameItemSameComponents(ItemStack a, ItemStack b) {
        if (!a.is(b.getItem())) {
            return false;
        } else {
            return (a.isEmpty() && b.isEmpty()) || Objects.equals(a.components.asPatch(), b.components.asPatch());
        }
    }

    @Override
    public boolean greaterThan(ItemComparatorView other) {
        if (other == null || other.stack().isEmpty()) {
            return false;
        }

        if (this.stack().isEmpty()) {
            return true;
        }

        if (!this.test(other)) {
            return false;
        }

        return other.stack().getCount() >= this.stack().count();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                this.stack().getItem(),
                this.stack().getComponents(),
                this.stack().getCount()
        );
    }

    @Override
    public ItemStack stack() {
        return this.itemStack;
    }

}
