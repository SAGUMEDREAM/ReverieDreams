package cc.thonly.reverie_dreams.registry.delegate;

import cc.thonly.keine.item.ItemStackTemplate;
import cc.thonly.reverie_dreams.registry.DeferredDelegateRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.Objects;

@SuppressWarnings({"unchecked", "PatternVariableHidesField"})
public class ItemDelegate
        extends RegistryDelegate<Item>
        implements ItemLike {
    private Identifier key;

    private ItemDelegate(Holder<Item> delegate) {
        super(delegate);
        if (delegate instanceof DeferredDelegateRegister.Entry<Item> entry) {
            this.key = entry.getRegistryId();
        }
    }

    public static ItemDelegate of(
            RegistryDelegate<Item> delegate
    ) {
        ItemDelegate result = new ItemDelegate(null);
        result.bindKey(delegate.getRegistryId());
        result.holder = delegate;
        return result;
    }

    public static ItemDelegate of(
            Holder<Item> itemHolder
    ) {
        return new ItemDelegate(itemHolder);
    }

    @Override
    public void bindKey(Identifier key) {
        this.key = key;
    }

    @Override
    public Item asItem() {
        return this.get();
    }

    public Holder<Item> asHolder() {
        return this;
    }

    public ItemStack createStack() {
        return this.get().getDefaultInstance();
    }

    public ItemStack toStack() {
        return this.get().getDefaultInstance();
    }

    public ItemStack toStack(int count) {
        ItemStack stack = this.toStack();
        stack.setCount(count);
        return stack;
    }

    public ItemStackTemplate createTemplate() {
        return new ItemStackTemplate(this.get());
    }

    public static ResourceKey<Item> createKey(Identifier key) {
        return ResourceKey.create(Registries.ITEM, key);
    }

    @Override
    public Identifier getRegistryId() {
        return this.key;
    }

    @Override
    public void bind(Holder<Item> holder) {
        Objects.requireNonNull(holder, "holder");

        if (this.holder != null) {
            throw new IllegalStateException(
                    "Item delegate is already bound"
            );
        }

        this.holder = holder;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.holder);
    }
}