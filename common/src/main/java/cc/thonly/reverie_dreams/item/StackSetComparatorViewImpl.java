package cc.thonly.reverie_dreams.item;

import lombok.ToString;
import net.minecraft.world.item.ItemStack;
import java.util.List;

@SuppressWarnings("ALL")
@ToString
public class StackSetComparatorViewImpl implements ItemComparatorView {
    final List<ItemComparatorView> views;

    public StackSetComparatorViewImpl(IngredientStackSet set) {
        this.views = set.values().stream()
                        .map(ItemComparatorView::of)
                        .toList();
    }

    @Override
    public boolean matches(ItemComparatorView obj) {
        return this.views.stream().anyMatch(v -> v.matches(obj));
    }

    @Override
    public boolean greaterThan(ItemComparatorView other) {
        return this.views.stream().anyMatch(v -> v.greaterThan(other));
    }

    @Override
    public boolean test(ItemComparatorView other) {
        return this.views.stream().anyMatch(v -> v.test(other));
    }

    @Override
    public ItemStack stack() {
        return this.views.isEmpty()
                ? IngredientStack.empty().getLazyStack()
                : this.views.getFirst().stack();
    }
}
