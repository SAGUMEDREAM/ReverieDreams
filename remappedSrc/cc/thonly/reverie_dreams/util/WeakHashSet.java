package cc.thonly.reverie_dreams.util;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

public class WeakHashSet<T> implements Set<T> {
    private final WeakHashMap<T, Boolean> backingMap = new WeakHashMap<>();

    @Override
    public boolean add(T t) {
        return this.backingMap.put(t, Boolean.TRUE) == null;
    }

    @Override
    public boolean remove(Object o) {
        return this.backingMap.remove(o) != null;
    }

    @Override
    public boolean contains(Object o) {
        return this.backingMap.containsKey(o);
    }

    @Override
    public void clear() {
        this.backingMap.clear();
    }

    @Override
    public boolean isEmpty() {
        return this.backingMap.isEmpty();
    }

    @Override
    public int size() {
        return this.backingMap.size();
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return this.backingMap.keySet().iterator();
    }

    @Override
    public Object @NotNull [] toArray() {
        return this.backingMap.keySet().toArray();
    }

    @Override
    public <E> E @NotNull [] toArray(E[] a) {
        return this.backingMap.keySet().toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.backingMap.keySet().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean modified = false;
        for (T element : c) {
            modified |= add(element);
        }
        return modified;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return this.backingMap.keySet().retainAll(c);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return this.backingMap.keySet().removeAll(c);
    }
}
