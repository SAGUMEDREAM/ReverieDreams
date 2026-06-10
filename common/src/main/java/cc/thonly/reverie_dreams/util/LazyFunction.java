package cc.thonly.reverie_dreams.util;

import java.util.function.Function;

public interface LazyFunction<I, V> extends Function<I, V> {

    void unbound();

    static <I, V> LazyFunction<I, V> of(Function<I, V> function) {
        return new LazyFunctionImpl<>() {
            @Override
            public synchronized V apply(I i) {
                if (this.value == null) {
                    this.value = function.apply(i);
                }
                return this.value;
            }
        };
    }

    class LazyFunctionImpl<I, V> implements LazyFunction<I, V> {
        V value = null;

        @Override
        public void unbound() {
            this.value = null;
        }

        @Override
        public synchronized V apply(I i) {
            if (this.value == null) {
                this.value = this.apply(i);
            }
            return this.value;
        }
    }
}
