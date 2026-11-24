package cc.thonly.reverie_dreams.restaurant;

import java.util.List;
import java.util.function.Predicate;

/** Dedicated profiles remain dedicated even when their optional preferences are empty. */
public final class CustomerOrderChoice {
    private CustomerOrderChoice() {}
    public static <T> List<T> candidates(List<T> menu, boolean generic, boolean hasPreferences, Predicate<T> preferred) {
        return generic || !hasPreferences ? List.copyOf(menu) : menu.stream().filter(preferred).toList();
    }
}
