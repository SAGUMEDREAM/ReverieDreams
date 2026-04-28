package cc.thonly.reverie_dreams.util;

import java.util.List;
import java.util.function.Supplier;

public class ListMaker {
    public static <T> List<T> of(Supplier<List<T>> supplier) {
        return supplier.get();
    }
}
