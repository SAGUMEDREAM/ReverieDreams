package cc.thonly.reverie_dreams.util.item;

/**
 * Monetary values in copper. Faith offerings deliberately use a different scale.
 */
public final class CoinValues {
    public static final int COPPER = 1;
    public static final int SILVER = 10;
    public static final int GOLD = 100;
    public static final int EXCHANGE_RATIO = 10;

    private CoinValues() {
    }

    public static int denomination(String namespace, String path) {
        if (!namespace.equals("reverie_dreams"))
            return 0;
        return switch (path) {
            case "copper_coin" -> COPPER;
            case "silver_coin" -> SILVER;
            case "gold_coin" -> GOLD;
            default -> 0;
        };
    }

    public static boolean adjacent(int first, int second) {
        return first > 0 && second > 0
                && (first * EXCHANGE_RATIO == second || second * EXCHANGE_RATIO == first);
    }
}
