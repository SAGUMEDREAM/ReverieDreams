package cc.thonly.reverie_dreams.paper.farming;

/** Crop IDs and age/loot boundaries are derived from current source/generated resources. */
public record CropRule(String id, String seed, String product, int maxAge, boolean selfSeed) { }
