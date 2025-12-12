package cc.thonly.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;

import java.util.Optional;
import java.util.stream.Stream;

public interface ValueInput {
    <T> Optional<T> read(String string, Codec<T> codec);

    /** @deprecated */
    @Deprecated
    <T> Optional<T> read(MapCodec<T> mapCodec);

    Optional<ValueInput> child(String string);

    ValueInput childOrEmpty(String string);

    Optional<ValueInputList> childrenList(String string);

    ValueInputList childrenListOrEmpty(String string);

    <T> Optional<TypedInputList<T>> list(String string, Codec<T> codec);

    <T> TypedInputList<T> listOrEmpty(String string, Codec<T> codec);

    boolean getBooleanOr(String string, boolean bl);

    byte getByteOr(String string, byte b);

    int getShortOr(String string, short s);

    Optional<Integer> getInt(String string);

    int getIntOr(String string, int i);

    long getLongOr(String string, long l);

    Optional<Long> getLong(String string);

    float getFloatOr(String string, float f);

    double getDoubleOr(String string, double d);

    Optional<String> getString(String string);

    String getStringOr(String string, String string2);

    Optional<int[]> getIntArray(String string);

    /** @deprecated */
    @Deprecated
    HolderLookup.Provider lookup();

    public interface TypedInputList<T> extends Iterable<T> {
        boolean isEmpty();

        Stream<T> stream();
    }

    public interface ValueInputList extends Iterable<ValueInput> {
        boolean isEmpty();

        Stream<ValueInput> stream();
    }
}
