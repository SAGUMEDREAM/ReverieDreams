package cc.thonly.reverie_dreams.entity.variant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** Daily stock is shared by all customers of one rabbit, including after reopening/reloading. */
public final class YoukaiRabbitTradeState {
    public enum Currency { COPPER, SILVER }
    public enum Offer {
        BAMBOO(false, 8, Currency.COPPER, 2, 8),
        BAMBOO_SHOOTS(false, 2, Currency.COPPER, 4, 6),
        RED_MUSHROOM(false, 2, Currency.COPPER, 3, 6),
        BROWN_MUSHROOM(false, 2, Currency.COPPER, 3, 6),
        PORK(false, 2, Currency.COPPER, 6, 4),
        BLACK_PORK(false, 1, Currency.SILVER, 1, 2),
        UDUMBARA_FLOWER(false, 1, Currency.SILVER, 4, 1),
        TRUFFLE(false, 1, Currency.SILVER, 2, 2),
        CARROT(true, 8, Currency.COPPER, 1, 4),
        MELON_SLICE(true, 12, Currency.COPPER, 1, 4);

        public final boolean buying;
        public final int count;
        public final Currency currency;
        public final int price;
        public final int stock;
        Offer(boolean buying, int count, Currency currency, int price, int stock) {
            this.buying = buying; this.count = count; this.currency = currency; this.price = price; this.stock = stock;
        }
    }

    private long day = Long.MIN_VALUE;
    private final int[] sold = new int[Offer.values().length];

    public long day() { return this.day; }
    public int sold(Offer offer) { return this.sold[offer.ordinal()]; }
    public void refresh(long currentDay) {
        if (this.day == currentDay) return;
        this.day = currentDay;
        Arrays.fill(this.sold, 0);
    }
    public boolean available(Offer offer, long expectedDay) {
        return this.day == expectedDay && this.sold(offer) < offer.stock;
    }
    public boolean complete(Offer offer, long expectedDay) {
        if (!this.available(offer, expectedDay)) return false;
        this.sold[offer.ordinal()]++;
        return true;
    }
    public void restore(long savedDay, int[] counts) {
        this.day = savedDay;
        Arrays.fill(this.sold, 0);
        for (int i = 0; i < Math.min(counts.length, this.sold.length); i++)
            this.sold[i] = Math.clamp(counts[i], 0, Offer.values()[i].stock);
    }
    public int[] counts() { return this.sold.clone(); }

    public static List<Offer> dailyOffers(UUID rabbit, long day) {
        long seed = rabbit.getMostSignificantBits() ^ rabbit.getLeastSignificantBits();
        int omitted = (int)Math.floorMod(Math.floorMod(seed, 5) + Math.floorMod(day, 5), 5);
        int rare = (int)Math.floorMod(Math.floorMod(seed, 3) + Math.floorMod(day, 3), 3);
        List<Offer> result = new ArrayList<>();
        for (int i = 0; i < 5; i++) if (i != omitted) result.add(Offer.values()[i]);
        result.add(Offer.values()[5 + rare]);
        result.add(Offer.CARROT);
        result.add(Offer.MELON_SLICE);
        return List.copyOf(result);
    }
}
