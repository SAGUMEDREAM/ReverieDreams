package cc.thonly.reverie_dreams.registry.delegate;

import dev.architectury.registry.registries.DeferredSupplier;
import dev.architectury.registry.registries.RegistrySupplier;
import lombok.experimental.Delegate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ItemDelegate implements Holder<Item>, DeferredSupplier<Item>, ItemLike {
    @Delegate
    final RegistrySupplier<Item> supplier;

    public ItemDelegate(RegistrySupplier<Item> supplier) {
        this.supplier = supplier;
    }

    public static ItemDelegate of(RegistrySupplier<Item> supplier) {
        return new ItemDelegate(supplier);
    }

    public static ItemDelegate of(Holder<Item> itemHolder) {
        return new ItemDelegate.Existed(itemHolder);
    }

    @Override
    public Item asItem() {
        return this.supplier.get();
    }

    public Holder<Item> asHolder() {
        return this;
    }

    public ItemStack createStack() {
        return this.supplier.get().getDefaultInstance();
    }

    public ItemStack toStack() {
        return this.supplier.get().getDefaultInstance();
    }

    public ItemStack toStack(int count) {
        ItemStack stack = this.toStack();
        stack.setCount(count);
        return stack;
    }

    public static ResourceKey<Item> createKey(Identifier key) {
        return ResourceKey.create(Registries.ITEM, key);
    }

    public ItemStackTemplate createTemplate() {
        return new ItemStackTemplate(this.supplier.get());
    }

    public static class Existed extends ItemDelegate {
        public Existed(Holder<Item> itemHolder) {
            super(new HolderDelegate<>(itemHolder));
        }
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || (obj instanceof Holder<?> holder && this.is((Holder) holder));
    }
}
