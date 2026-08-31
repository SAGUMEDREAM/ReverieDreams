package cc.thonly.reverie_dreams.util;

import java.util.*;
import java.util.function.Supplier;

/**
 * A lazy initialized List implementation.
 * <p>
 * The backing list will only be created when it is accessed.
 */
public class LazyList<T> extends AbstractList<T> {

    private final Supplier<List<T>> getter;

    private List<T> values;

    public LazyList() {
        this(ArrayList::new);
    }

    public LazyList(Supplier<List<T>> getter) {
        this.getter = Objects.requireNonNull(getter, "getter");
    }

    private List<T> getDelegate() {
        if (this.values == null) {
            this.values = Objects.requireNonNull(
                    this.getter.get(),
                    "LazyList supplier returned null"
            );
        }

        return this.values;
    }

    @Override
    public boolean isEmpty() {
        List<T> delegate = this.getDelegate();
        return delegate.isEmpty();
    }

    @Override
    public T get(int index) {
        return this.getDelegate().get(index);
    }

    @Override
    public int size() {
        return this.values == null ? 0 : this.values.size();
    }

    @Override
    public T set(int index, T element) {
        return this.getDelegate().set(index, element);
    }

    @Override
    public void add(int index, T element) {
        this.getDelegate().add(index, element);
    }

    @Override
    public T remove(int index) {
        return this.getDelegate().remove(index);
    }

    @Override
    public int indexOf(Object o) {
        this.getDelegate();
        return super.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        this.getDelegate();
        return super.lastIndexOf(o);
    }

    @Override
    public void clear() {
        if (this.values != null) {
            this.values.clear();
        }
    }

    /**
     * Check whether this list has been initialized.
     */
    public boolean isInitialized() {
        return this.values != null;
    }

    /**
     * Force initialize and get backing list.
     */
    public List<T> unwrap() {
        return this.getDelegate();
    }

    /**
     * Release backing storage.
     */
    public void unload() {
        this.values = null;
    }
}