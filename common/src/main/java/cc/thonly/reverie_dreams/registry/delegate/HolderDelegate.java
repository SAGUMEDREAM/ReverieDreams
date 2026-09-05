package cc.thonly.reverie_dreams.registry.delegate;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class HolderDelegate<I, O>
        extends RegistryDelegate<O> {

    private Identifier key;

    private final RegistryDelegate<I> input;
    private final HolderDelegateMapper<I, O> mapper;

    private HolderDelegate(
            RegistryDelegate<I> input,
            HolderDelegateMapper<I, O> mapper
    ) {
        super(null);

        this.input = Objects.requireNonNull(input, "input");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    public static <I, O> HolderDelegate<I, O> create(
            RegistryDelegate<I> input,
            HolderDelegateMapper<I, O> mapper
    ) {
        return new HolderDelegate<>(input, mapper);
    }

    private RegistryDelegate<O> mapped() {
        return this.mapper.map(this.input);
    }

    @Override
    public O get() {
        return this.mapped().get();
    }

    @Override
    public @Nullable Holder<O> getHolder() {
        return this.mapped().getHolder();
    }

    @Override
    public void bindKey(Identifier key) {
        this.key = Objects.requireNonNull(key, "key");
    }

    @Override
    public Identifier getRegistryId() {
        return this.key;
    }

    @Override
    public void bind(Holder<O> holder) {
        this.holder = Objects.requireNonNull(holder, "holder");
    }

    @FunctionalInterface
    public interface HolderDelegateMapper<I, O> {

        RegistryDelegate<O> map(
                RegistryDelegate<I> input
        );
    }
}