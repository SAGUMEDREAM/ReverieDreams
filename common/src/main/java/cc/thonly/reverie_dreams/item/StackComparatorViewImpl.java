package cc.thonly.reverie_dreams.item;

import lombok.ToString;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("deprecation")
@ToString
public final class StackComparatorViewImpl implements ItemComparatorView {
    private final ItemStack itemStack;
    private final List<TagKey<Item>> itemTags;

    public StackComparatorViewImpl(IngredientStack stack) {
        this.itemStack = stack.build();
        this.itemTags = List.copyOf(stack.getTags());
    }

    public StackComparatorViewImpl(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.itemTags = List.of();
    }

    public StackComparatorViewImpl(ItemStack itemStack, List<TagKey<Item>> itemTags) {
        this.itemStack = itemStack;
        this.itemTags = List.copyOf(itemTags);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ItemComparatorView other)) return false;
        return ItemStack.matches(this.stack(), other.stack())
                && Objects.equals(this.tags(), other.tags());
    }

    @Override
    public boolean matches(ItemComparatorView other) {
        if (other == this) {
            return true;
        }
        if (ItemStack.matches(this.stack(), other.stack())) {
            return true;
        }
        for (TagKey<Item> tagA : other.tags()) {
            for (TagKey<Item> tagB : this.tags()) {
                if (Objects.equals(tagA, tagB)) {
                    if (Objects.equals(this.stack().components, other.stack().components)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean test(ItemComparatorView other) {
        if (!ItemStack.isSameItem(this.stack(), other.stack())) {
            for (TagKey<Item> tag : this.tags()) {
                Holder.Reference<Item> itemReference = other.stack().getItem().builtInRegistryHolder();
                Set<TagKey<Item>> tags = itemReference.tags;
                if (tags != null && tags.contains(tag)) {
                    return true;
                }
            }
            return false;
        }

        if (!this.stack().getComponentsPatch().isEmpty()) {
//            if (this.stack().getItem() instanceof DanmakuShapeCreatorItem && other.stack().getItem() instanceof DanmakuShapeCreatorItem) {
//                System.out.println(this.stack().getComponents());
//                System.out.println(other.stack().getComponents());
//            }
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

        if (this.stack().isEmpty() && this.tags().isEmpty()) {
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
                this.stack().getCount(),
                this.tags()
        );
    }

    @Override
    public ItemStack stack() {
        return this.itemStack;
    }

    @Override
    public List<TagKey<Item>> tags() {
        return this.itemTags;
    }
}
