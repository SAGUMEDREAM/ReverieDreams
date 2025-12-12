package cc.thonly.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;

public interface ValueOutput {
    <T> void store(String string, Codec<T> codec, T object);

    <T> void storeNullable(String string, Codec<T> codec, @Nullable T object);

    /** @deprecated */
    @Deprecated
    <T> void store(MapCodec<T> mapCodec, T object);

    void putBoolean(String string, boolean bl);

    void putByte(String string, byte b);

    void putShort(String string, short s);

    void putInt(String string, int i);

    void putLong(String string, long l);

    void putFloat(String string, float f);

    void putDouble(String string, double d);

    void putString(String string, String string2);

    void putIntArray(String string, int[] is);

    ValueOutput child(String string);

    ValueOutputList childrenList(String string);

    <T> TypedOutputList<T> list(String string, Codec<T> codec);

    void discard(String string);

    boolean isEmpty();

    public interface TypedOutputList<T> {
        void add(T object);

        boolean isEmpty();
    }

    public interface ValueOutputList {
        ValueOutput addChild();

        void discardLast();

        boolean isEmpty();
    }
}
