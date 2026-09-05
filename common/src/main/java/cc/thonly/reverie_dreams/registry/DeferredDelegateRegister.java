package cc.thonly.reverie_dreams.registry;

import cc.thonly.reverie_dreams.registry.delegate.DelegateKeyType;
import cc.thonly.reverie_dreams.registry.delegate.RegistryDelegate;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.core.BalmRegistrar;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("rawtypes")
public class DeferredDelegateRegister<T> implements Iterable<Holder<T>> {

    private final String modId;
    @Getter
    private final ResourceKey<Registry<T>> key;
    private final Map<Identifier, Entry<T>> entries = new Object2ObjectLinkedOpenHashMap<>(64);
    private boolean registered = false;

    public DeferredDelegateRegister(
            String modId,
            ResourceKey<Registry<T>> key
    ) {
        this.modId = Objects.requireNonNull(modId, "modId");
        this.key = Objects.requireNonNull(key, "key");
    }

    public static <T> DeferredDelegateRegister<T> create(
            ResourceKey<Registry<T>> key
    ) {
        return new DeferredDelegateRegister<>("minecraft", key);
    }

    public static <T> DeferredDelegateRegister<T> create(
            String modId,
            ResourceKey<Registry<T>> key
    ) {
        return new DeferredDelegateRegister<>(modId, key);
    }

    /**
     * 注册一个 Entry。
     * <p>
     * 如果当前 Registry 尚未执行 register()，
     * Entry 会进入 Deferred 队列。
     * <p>
     * 如果 Registry 已经执行过 register()，
     * 则立即向 Balm 注册。
     */
    public <R extends T> RegistryDelegate<R> register(
            String id,
            Supplier<? extends R> supplier
    ) {
        Objects.requireNonNull(id, "id");

        return register(
                Identifier.fromNamespaceAndPath(this.modId, id),
                supplier
        );
    }

    /**
     * 注册一个 Entry。
     */
    @SuppressWarnings("unchecked")
    public <R extends T> RegistryDelegate<R> register(
            Identifier id,
            Supplier<? extends R> supplier
    ) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(supplier, "supplier");

        /*
         * 防止重复注册。
         */
        if (this.entries.containsKey(id)) {
            throw new IllegalStateException(
                    "Duplicate registry entry: " + id
            );
        }

        Entry<R> entry = new Entry<>(id, supplier);

        /*
         * 保存 Entry。
         */
        this.entries.put(id, (Entry<T>) entry);

        /*
         * 如果 Registry 已经完成初始化，
         * 则直接注册当前 Entry。
         */
        if (this.registered) {
            this.registerEntry((Entry) entry);
        }

        return entry;
    }

    /**
     * 判断是否已经声明了指定 Entry。
     */
    public boolean contains(Identifier id) {
        return this.entries.containsKey(id);
    }

    /**
     * 获取指定 Entry。
     */
    public RegistryDelegate<T> get(Identifier id) {
        return this.entries.get(id);
    }

    /**
     * 获取指定 Entry。
     */
    public Entry<T> getEntry(Identifier id) {
        return this.entries.get(id);
    }

    /**
     * 返回所有 Entry。
     */
    public Iterable<Entry<T>> entries() {
        return this.entries.values();
    }

    /**
     * 返回所有 Holder。
     */
    @Override
    public @NotNull Iterator<Holder<T>> iterator() {
        return this.entries.values()
                .stream()
                .map(entry -> (Holder<T>) entry)
                .iterator();
    }

    /**
     * 执行 Registry 批量注册。
     * <p>
     * 这个方法通常只需要调用一次。
     * <p>
     * 后续如果再次调用 register(...)
     * 会自动走 registerEntry(...) 立即注册。
     */
    public void register() {
        if (this.registered) {
            return;
        }

        /*
         * 这里直接遍历现有 Entry。
         *
         * registerEntry() 内部会按照 Entry 自己的
         * namespace 获取对应的 Scoped。
         */
        for (Entry<T> entry : this.entries.values()) {
            this.registerEntry(entry);
        }

        this.registered = true;
    }

    /**
     * 注册单个 Entry。
     * <p>
     * 无论 Entry 是在 register() 之前还是之后创建，
     * 最终都统一走这里。
     */
    private void registerEntry(Entry<T> entry) {
        Identifier id = entry.getRegistryId();

    }

    /**
     * Registry Entry。
     * <p>
     * 一个 Entry 同时承担：
     * <p>
     * Identifier
     * Supplier
     * RegistryDelegate
     * Holder
     */
    public static class Entry<T>
            extends RegistryDelegate<T> {

        private Identifier key;
        private final Supplier<T> supplier;

        public Entry(
                Identifier key,
                Supplier<? extends T> supplier
        ) {
            super(null);

            this.key = Objects.requireNonNull(
                    key,
                    "key"
            );

            this.supplier = Objects.requireNonNull(
                    supplier,
                    "supplier"
            )::get;
        }

        @Override
        public void bindKey(Identifier key) {
            this.key = key;
        }

        @Override
        public Identifier getRegistryId() {
            return this.key;
        }

        /**
         * 将实际 Registry Holder 绑定到当前 Entry。
         * <p>
         * 一个 Entry 只能绑定一次。
         */
        @Override
        public void bind(Holder<T> holder) {
            Objects.requireNonNull(
                    holder,
                    "holder"
            );

            if (this.holder != null) {
                throw new IllegalStateException(
                        "Registry delegate is already bound: "
                                + this.key
                );
            }

            this.holder = holder;
        }

        /**
         * 获取当前 Entry 的 Supplier。
         */
        public Supplier<T> supplier() {
            return this.supplier;
        }

        @Override
        public String toString() {
            return "DeferredRegistryEntry[" + this.key + "]";
        }
    }
}