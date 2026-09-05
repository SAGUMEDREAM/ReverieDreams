package cc.thonly.reverie_dreams.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public interface LazySupplier<T> extends Supplier<T> {
    T get();

    default void unbound() {

    }

    static <T> LazySupplier<T> of(Supplier<T> getter) {
        return new Impl<>(getter);
    }

    static <T> LazySupplier<T> of(T value) {
        return new ImmediateImpl<>(value);
    }

    static <T> LazySupplier<T> defineByName(String name, Supplier<T> getter) {
        return (LazySupplier<T>) NamedImpl.IMPLS.computeIfAbsent(name, inst -> new NamedImpl<>(name, getter));
    }

    static <T> LazySupplier<T> byName(String name) {
        return (LazySupplier<T>) NamedImpl.IMPLS.get(name);
    }

    static <T> LazySupplier<T> byName(String name, Class<T> tClass) {
        return (LazySupplier<T>) NamedImpl.IMPLS.get(name);
    }

    class NamedImpl<T> implements LazySupplier<T> {
        protected static final Map<String, NamedImpl<?>> IMPLS = new Object2ObjectOpenHashMap<>(64);
        protected final String name;
        private final Supplier<T> getter;
        private T value;

        protected NamedImpl(String name, Supplier<T> getter) {
            this.name = name;
            this.getter = getter;
        }

        @Override
        public T get() {
            return this.value == null ? this.value = this.getter.get() : this.value;
        }

        @Override
        public String toString() {
            if (this.value != null) {
                return this.value.toString();
            }
            return super.toString();
        }
    }

    class ImmediateImpl<T> implements LazySupplier<T> {
        private final T value;

        public ImmediateImpl(T value) {
            this.value = Objects.requireNonNull(value);
        }

        @Override
        public T get() {
            return this.value;
        }

        @Override
        public String toString() {
            return this.value.toString();
        }
    }

    class Impl<T> implements LazySupplier<T> {
        private final Supplier<T> getter;
        private T value;
        private boolean initialized = false;

        public Impl(Supplier<T> getter) {
            this.getter = Objects.requireNonNull(getter);
        }

        @Override
        public synchronized T get() {
            if (!this.initialized) {
                this.value = this.getter.get();
                this.initialized = true;
            }
            return this.value;
        }

        @Override
        public void unbound() {
            this.initialized = false;
        }

        @Override
        public String toString() {
            if (this.initialized) {
                return this.value.toString();
            }
            return super.toString();
        }
    }
}
