package cc.thonly.reverie_dreams.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import java.util.List;
import java.util.function.Supplier;

public class CodecMerger {
    @SafeVarargs
    public static <T> Codec<T> merge(Codec<T>... codecs) {
        return Codec.lazyInitialized(() -> {
            if (codecs.length == 0) {
                throw new IllegalArgumentException("At least one codec required");
            }

            Codec<T> result = codecs[0];

            for (int i = 1; i < codecs.length; i++) {
                Codec<T> next = codecs[i];

                result = Codec.either(result, next).xmap(
                        either -> either.map(
                                v -> v,
                                v -> v
                        ),
                        Either::left
                );
            }

            return result;
        });
    }

    public static <T> Codec<T> merge(List<Codec<T>> codecs) {
        return Codec.lazyInitialized(() -> {
            if (codecs.isEmpty()) {
                throw new IllegalArgumentException("At least one codec required");
            }

            Codec<T> result = codecs.getFirst();

            for (int i = 1; i < codecs.size(); i++) {
                Codec<T> next = codecs.get(i);

                result = Codec.either(result, next).xmap(
                        either -> either.map(
                                v -> v,
                                v -> v
                        ),
                        Either::left
                );
            }

            return result;
        });
    }

    public static <T> Codec<T> mergeLazyInitializedVarargs(Supplier<Codec<T>[]> codecs) {
        return Codec.lazyInitialized(() -> {
            Codec<T>[] array = codecs.get();

            if (array == null || array.length == 0) {
                throw new IllegalArgumentException("At least one codec required");
            }

            return merge(array);
        });
    }

    public static <T> Codec<T> mergeLazyInitialized(Supplier<List<Codec<T>>> codecs) {
        return Codec.lazyInitialized(() -> {
            List<Codec<T>> array = codecs.get();

            if (array == null || array.isEmpty()) {
                throw new IllegalArgumentException("At least one codec required");
            }

            return merge(array);
        });
    }
}
