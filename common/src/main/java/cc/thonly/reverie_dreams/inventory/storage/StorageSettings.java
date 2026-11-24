package cc.thonly.reverie_dreams.inventory.storage;

import cc.thonly.reverie_dreams.ReverieDreamsConfiguration;
import java.util.List;

public record StorageSettings(boolean claimEmpty, int reserveFood, int reserveTools, List<String> categoryTags) {
    public StorageSettings {
        reserveFood = Math.max(0, reserveFood);
        reserveTools = Math.max(0, reserveTools);
        categoryTags = categoryTags == null ? List.of() : List.copyOf(categoryTags);
    }

    public static StorageSettings from(ReverieDreamsConfiguration config) {
        return new StorageSettings(config.chestSortClaimEmptyChests, config.chestSortReserveFood,
                config.chestSortReserveTools, config.chestSortCategoryTags);
    }
}
