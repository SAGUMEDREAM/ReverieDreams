package cc.thonly.reverie_dreams.util;

import java.util.Objects;
import java.util.function.Supplier;

public interface LazySupplier<T> extends Supplier<T> {
    T get();

    static <T> LazySupplier<T> of(Supplier<T> supplier) {
        return new Impl<>(supplier);
    }

    static <T> LazySupplier<T> of(T value) {
        return new ImmediateImpl<>(value);
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
    }

    class Impl<T> implements LazySupplier<T> {
        private Supplier<T> supplier;
        private T value;
        private boolean initialized = false;

        public Impl(Supplier<T> supplier) {
            this.supplier = Objects.requireNonNull(supplier);
        }

        @Override
        public synchronized T get() {
            if (!this.initialized) {
                this.value = this.supplier.get();
                this.initialized = true;
                this.supplier = null;
            }
            return this.value;
        }
    }
}
