package cc.thonly.reverie_dreams.restaurant;

import net.minecraft.resources.Identifier;
import java.util.*;
import java.util.function.Predicate;

/** Menu entries are configuration, never inventories or customer definitions. */
public final class RestaurantMenu {
    public static final int LIMIT = 128;
    private final LinkedHashSet<Identifier> foods = new LinkedHashSet<>(), drinks = new LinkedHashSet<>();
    public List<Identifier> entries(boolean beverages) { return List.copyOf(beverages ? drinks : foods); }
    public boolean contains(boolean beverages, Identifier id) { return (beverages ? drinks : foods).contains(id); }
    public boolean toggle(boolean beverages, Identifier id) {
        var set = beverages ? drinks : foods;
        if (set.remove(id)) return true;
        return set.size() < LIMIT && set.add(id);
    }
    public void load(boolean beverages, List<Identifier> entries) {
        var set = beverages ? drinks : foods; set.clear();
        entries.stream().filter(Objects::nonNull).limit(LIMIT).forEach(set::add);
    }
    public List<Identifier> available(boolean beverages, Predicate<Identifier> valid) { return entries(beverages).stream().filter(valid).toList(); }
    public boolean hasAvailable(boolean beverages, Predicate<Identifier> valid) {
        return (beverages ? drinks : foods).stream().anyMatch(valid);
    }

    public boolean ready(Predicate<Identifier> food, Predicate<Identifier> drink) {
        return hasAvailable(false, food) && hasAvailable(true, drink);
    }
}
