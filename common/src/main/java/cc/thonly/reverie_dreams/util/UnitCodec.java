package cc.thonly.reverie_dreams.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import java.util.function.Supplier;

public final class UnitCodec {
    public static <A> Codec<A> unit(final A defaultValue) {
        return MapCodec.unit(defaultValue).codec();
    }

    public static <A> Codec<A> unit(final Supplier<A> defaultValue) {
        return MapCodec.unit(defaultValue).codec();
    }
}
