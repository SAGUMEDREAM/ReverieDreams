package cc.thonly.reverie_dreams.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@Slf4j
public class FinalReference<Value> implements Supplier<Value> {
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private volatile Value value;
    private final Supplier<Value> lazySupplier;

    private FinalReference(Supplier<Value> lazySupplier) {
        this.lazySupplier = lazySupplier != null ? lazySupplier : () -> null;
    }

    private FinalReference(Value defaultValue) {
        this.lazySupplier = null;
        this.value = defaultValue;
    }

    public static <V> FinalReference<V> empty() {
        return new FinalReference<>((V) null);
    }

    public static <V> FinalReference<V> withDefault(V defaultValue) {
        return new FinalReference<>(defaultValue);
    }

    public static <V> FinalReference<V> ofLazy(Supplier<V> lazySupplier) {
        return new FinalReference<>(lazySupplier);
    }

    public void set(Value value) {
        this.set(value, false);
    }

    public boolean set(Value value, boolean thrown) {
        if (this.initialized.compareAndSet(false, true)) {
            this.value = value;
            return true;
        }

        String errorMsg = "FinalReference has already been initialized and cannot be modified again.";
        IllegalStateException exception = new IllegalStateException(errorMsg);
        if (thrown) {
            throw exception;
        } else {
            // 传入 exception 对象作为第二个参数，SLF4J 会自动打印完整的调用堆栈
            log.error(errorMsg, exception);
            return false;
        }
    }

    @Override
    public Value get() {
        if (!this.initialized.get() && this.lazySupplier != null) {
            synchronized (this) {
                if (this.initialized.compareAndSet(false, true)) {
                    this.value = this.lazySupplier.get();
                }
            }
        }
        return this.value;
    }

    /**
     * 安全获取 Optional 包装的值，避免空指针
     */
    public Optional<Value> getOptional() {
        return Optional.ofNullable(get());
    }

    /**
     * 判断是否已经完成最终初始化
     */
    public boolean isInitialized() {
        return this.initialized.get();
    }
}