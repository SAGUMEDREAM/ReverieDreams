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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockDelegate implements Holder<Block>, DeferredSupplier<Block>, ItemLike {
    @Delegate
    final RegistrySupplier<Block> supplier;

    public BlockDelegate(RegistrySupplier<Block> supplier) {
        this.supplier = supplier;
    }

    public static BlockDelegate of(RegistrySupplier<Block> supplier) {
        return new BlockDelegate(supplier);
    }

    @Override
    public Item asItem() {
        return this.supplier.get().asItem();
    }

    public Block asBlock() {
        return this.supplier.get();
    }

    public BlockState defaultBlockState() {
        return this.supplier.get().defaultBlockState();
    }

    public BlockState asBlockState() {
        return this.supplier.get().defaultBlockState();
    }

    public Holder<Block> asHolder() {
        return this;
    }

    public ItemStack createStack() {
        return this.supplier.get().asItem().getDefaultInstance();
    }

    public ItemStack toStack() {
        return this.supplier.get().asItem().getDefaultInstance();
    }

    public ItemStack toStack(int count) {
        ItemStack stack = this.toStack();
        stack.setCount(count);
        return stack;
    }

    public static ResourceKey<Block> createKey(Identifier key) {
        return ResourceKey.create(Registries.BLOCK, key);
    }

    public ItemStackTemplate createTemplate() {
        return new ItemStackTemplate(this.supplier.get().asItem());
    }
}
