package cc.thonly.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

public class ValueInputContextHelper {
    final HolderLookup.Provider lookup;
    private final DynamicOps<Tag> ops;
    final ValueInput.ValueInputList emptyChildList = new ValueInput.ValueInputList() {
        public boolean isEmpty() {
            return true;
        }

        public Stream<ValueInput> stream() {
            return Stream.empty();
        }

        public Iterator<ValueInput> iterator() {
            return Collections.emptyIterator();
        }
    };
    private final ValueInput.TypedInputList<Object> emptyTypedList = new ValueInput.TypedInputList<>() {
        public boolean isEmpty() {
            return true;
        }

        public Stream<Object> stream() {
            return Stream.empty();
        }

        public Iterator<Object> iterator() {
            return Collections.emptyIterator();
        }
    };
    private final ValueInput empty = new ValueInput() {
        public <T> Optional<T> read(String string, Codec<T> codec) {
            return Optional.empty();
        }

        public <T> Optional<T> read(MapCodec<T> mapCodec) {
            return Optional.empty();
        }

        public Optional<ValueInput> child(String string) {
            return Optional.empty();
        }

        public ValueInput childOrEmpty(String string) {
            return this;
        }

        public Optional<ValueInput.ValueInputList> childrenList(String string) {
            return Optional.empty();
        }

        public ValueInput.ValueInputList childrenListOrEmpty(String string) {
            return ValueInputContextHelper.this.emptyChildList;
        }

        public <T> Optional<ValueInput.TypedInputList<T>> list(String string, Codec<T> codec) {
            return Optional.empty();
        }

        public <T> ValueInput.TypedInputList<T> listOrEmpty(String string, Codec<T> codec) {
            return ValueInputContextHelper.this.emptyTypedList();
        }

        public boolean getBooleanOr(String string, boolean bl) {
            return bl;
        }

        public byte getByteOr(String string, byte b) {
            return b;
        }

        public int getShortOr(String string, short s) {
            return s;
        }

        public Optional<Integer> getInt(String string) {
            return Optional.empty();
        }

        public int getIntOr(String string, int i) {
            return i;
        }

        public long getLongOr(String string, long l) {
            return l;
        }

        public Optional<Long> getLong(String string) {
            return Optional.empty();
        }

        public float getFloatOr(String string, float f) {
            return f;
        }

        public double getDoubleOr(String string, double d) {
            return d;
        }

        public Optional<String> getString(String string) {
            return Optional.empty();
        }

        public String getStringOr(String string, String string2) {
            return string2;
        }

        public HolderLookup.Provider lookup() {
            return ValueInputContextHelper.this.lookup;
        }

        public Optional<int[]> getIntArray(String string) {
            return Optional.empty();
        }
    };

    public ValueInputContextHelper(HolderLookup.Provider provider, DynamicOps<Tag> dynamicOps) {
        this.lookup = provider;
        this.ops = provider.createSerializationContext(dynamicOps);
    }

    public DynamicOps<Tag> ops() {
        return this.ops;
    }

    public HolderLookup.Provider lookup() {
        return this.lookup;
    }

    public ValueInput empty() {
        return this.empty;
    }

    public ValueInput.ValueInputList emptyList() {
        return this.emptyChildList;
    }

    @SuppressWarnings("unchecked")
    public <T> ValueInput.TypedInputList<T> emptyTypedList() {
        return (ValueInput.TypedInputList<T>) this.emptyTypedList;
    }
}
