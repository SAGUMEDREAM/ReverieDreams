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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public class BlockDelegate
        extends RegistryDelegate<Block>
        implements ItemLike {
    private Identifier key;

    private BlockDelegate(Holder<Block> holder) {
        super(holder);
        if (holder instanceof DeferredDelegateRegister.Entry<Block> entry) {
            this.key = entry.getRegistryId();
        }
    }

    public static BlockDelegate of(
            RegistryDelegate<Block> delegate
    ) {
        BlockDelegate result = new BlockDelegate(null);
        result.bindKey(delegate.getRegistryId());
        result.holder = delegate;
        return result;
    }

    public static BlockDelegate of(
            Holder<Block> holder
    ) {
        return new BlockDelegate(holder);
    }

    @Override
    public void bindKey(Identifier key) {
        this.key = key;
    }

    @Override
    public Item asItem() {
        return this.get().asItem();
    }

    public Block asBlock() {
        return this.get();
    }

    public BlockState defaultBlockState() {
        return this.get().defaultBlockState();
    }

    public BlockState asBlockState() {
        return this.get().defaultBlockState();
    }

    public Holder<Block> asHolder() {
        return this;
    }

    public ItemStack createStack() {
        return this.get().asItem().getDefaultInstance();
    }

    public ItemStack toStack() {
        return this.get().asItem().getDefaultInstance();
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
        return new ItemStackTemplate(this.get().asItem());
    }

    @Override
    public Identifier getRegistryId() {
        return this.key;
    }

    @Override
    public void bind(Holder<Block> holder) {
        Objects.requireNonNull(holder, "holder");

        if (this.holder != null) {
            throw new IllegalStateException(
                    "Block delegate is already bound"
            );
        }

        this.holder = holder;
    }
}