package cc.thonly.reverie_dreams.util.math;

import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ModMth {
    public static <T> T getRandomElement(RandomSource randomSource, List<T> list) {
        return list.get(randomSource.nextIntBetweenInclusive(0, list.size() - 1));
    }

    public static <T> List<T> toList(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
    }

    public static <T> List<T> toList(Stream<T> stream) {
        return StreamSupport.stream(stream.spliterator(), false).collect(Collectors.toList());
    }
}
