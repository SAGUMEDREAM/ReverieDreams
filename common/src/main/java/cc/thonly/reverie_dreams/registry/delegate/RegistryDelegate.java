package cc.thonly.reverie_dreams.registry.delegate;

import com.mojang.datafixers.util.Either;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Slf4j
@SuppressWarnings("DataFlowIssue")
public abstract class RegistryDelegate<T> implements Holder<T>, Supplier<T>, DelegateKeyType {

    @Nullable
    protected Holder<T> holder;

    protected RegistryDelegate(@Nullable Holder<T> holder) {
        this.holder = holder;
    }

    @Override
    public T value() {
        return this.getHolder().value();
    }

    @Override
    public boolean isBound() {
        return this.getHolder().isBound();
    }

    @Override
    public boolean is(Identifier location) {
        return Objects.equals(location, this.getRegistryId());
    }

    @Override
    public boolean is(ResourceKey<T> resourceKey) {
        return this.getHolder().is(resourceKey);
    }

    @Override
    public boolean is(Predicate<ResourceKey<T>> predicate) {
        return this.getHolder().is(predicate);
    }

    @Override
    public boolean is(TagKey<T> tagKey) {
        return this.getHolder().is(tagKey);
    }

    @Override
    public boolean is(Holder<T> holder) {
        return this.getHolder().is(holder);
    }

    @Override
    public Stream<TagKey<T>> tags() {
        return Stream.empty();
    }

    @Override
    public Either<ResourceKey<T>, T> unwrap() {
        return this.getHolder().unwrap();
    }

    @Override
    public Optional<ResourceKey<T>> unwrapKey() {
        return this.getHolder().unwrapKey();
    }

    @Override
    public Kind kind() {
        return this.getHolder().kind();
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner) {
        return this.getHolder().canSerializeIn(owner);
    }

    @Override
    public String getRegisteredName() {
        return this.getHolder().getRegisteredName();
    }

    public @Nullable Holder<T> getHolder() {
        return this.holder;
    }

    /**
     * 获取实际注册对象。
     *
     * @throws IllegalStateException 如果当前 Delegate 尚未绑定到 Holder
     */
    @Override
    public T get() {
        Holder<T> holder = this.getHolder();

        if (holder == null) {
            this.tryBind(false);
        }

        if (holder == null) {
            throw new IllegalStateException(
                    "Registry delegate has not been bound: " + this.getRegistryId()
            );
        }

        return holder.value();
    }

    public void tryBind(boolean thrown) {

    }

    /**
     * 获取当前 Delegate 对应的 Registry Key。
     */
    public abstract Identifier getRegistryId();

    /**
     * 将 Delegate 绑定到实际的 Holder。
     */
    public abstract void bind(Holder<T> holder);

    /**
     * 创建一个直接代理现有 Holder 的 Delegate。
     */
    public static <T> RegistryDelegate<T> direct(Holder<T> holder) {
        Objects.requireNonNull(holder, "holder");

        return new RegistryDelegate<>(holder) {

            @Override
            public void bindKey(Identifier key) {

            }

            @Override
            public Identifier getRegistryId() {
                return Identifier.withDefaultNamespace("default");
            }

            @Override
            public void bind(Holder<T> holder) {
                // Direct delegate 不允许重新绑定。
            }
        };
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof RegistryDelegate<?> that)) {
            return false;
        }

        return Objects.equals(this.getRegistryId(), that.getRegistryId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.holder);
    }

    @Override
    public String toString() {
        return "RegistryDelegate[" + this.getRegistryId() + "]";
    }
}