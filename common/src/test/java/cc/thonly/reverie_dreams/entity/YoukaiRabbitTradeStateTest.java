package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.entity.variant.YoukaiRabbitTradeState;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.UUID;

import static cc.thonly.reverie_dreams.entity.variant.YoukaiRabbitTradeState.Offer.*;
import static org.junit.jupiter.api.Assertions.*;

class YoukaiRabbitTradeStateTest {
    @Test void everyDayChangesAndAllEightSaleGoodsAppearWithinFifteenDays() {
        for (UUID id : new UUID[]{new UUID(0, 0), UUID.randomUUID(), new UUID(Long.MIN_VALUE, Long.MAX_VALUE)}) {
            var seen = new HashSet<YoukaiRabbitTradeState.Offer>();
            for (long day = -1; day < 15; day++) {
                var offers = YoukaiRabbitTradeState.dailyOffers(id, day);
                assertEquals(7, offers.size());
                assertEquals(7, new HashSet<>(offers).size());
                assertTrue(offers.contains(CARROT)); assertTrue(offers.contains(MELON_SLICE));
                assertEquals(1, offers.stream().filter(o -> o.currency == YoukaiRabbitTradeState.Currency.SILVER).count());
                assertNotEquals(new HashSet<>(offers), new HashSet<>(YoukaiRabbitTradeState.dailyOffers(id, day + 1)));
                assertEquals(offers, YoukaiRabbitTradeState.dailyOffers(id, day));
                seen.addAll(offers);
            }
            assertEquals(10, seen.size());
        }
    }
    @Test void reopeningAndReloadingShareStockAndOnlyANewDayRestocks() {
        var state = new YoukaiRabbitTradeState(); state.refresh(14);
        for (int i = 0; i < UDUMBARA_FLOWER.stock; i++) assertTrue(state.complete(UDUMBARA_FLOWER, 14));
        state.refresh(14);
        assertFalse(state.available(UDUMBARA_FLOWER, 14));
        var restored = new YoukaiRabbitTradeState(); restored.restore(state.day(), state.counts());
        assertFalse(restored.complete(UDUMBARA_FLOWER, 14));
        restored.refresh(15);
        assertTrue(restored.available(UDUMBARA_FLOWER, 15));
        assertFalse(restored.complete(UDUMBARA_FLOWER, 14), "Yesterday's open GUI cannot spend today's stock");
        assertEquals(0, restored.sold(UDUMBARA_FLOWER));
    }
    @Test void checkingPickupNeverSpendsStockAndLargeShiftTradesCannotExceedLimit() {
        var state = new YoukaiRabbitTradeState(); state.refresh(0);
        for (int i = 0; i < 100; i++) assertTrue(state.available(CARROT, 0));
        assertEquals(0, state.sold(CARROT));
        int completed = 0;
        for (int i = 0; i < 100; i++) if (state.complete(CARROT, 0)) completed++;
        assertEquals(CARROT.stock, completed);
        assertEquals(CARROT.stock, state.sold(CARROT));
    }
    @Test void corruptSavedStockIsBoundedAndSnapshotCannotModifyLiveStock() {
        var state = new YoukaiRabbitTradeState(); state.restore(8, new int[]{999, -2});
        assertEquals(BAMBOO.stock, state.sold(BAMBOO)); assertEquals(0, state.sold(BAMBOO_SHOOTS));
        int[] snapshot = state.counts(); snapshot[BAMBOO.ordinal()] = 0;
        assertFalse(state.available(BAMBOO, 8));
    }
    @Test void produceBuybackCannotBeReversedAtThisShop() {
        var offers = YoukaiRabbitTradeState.Offer.values();
        assertEquals(8, CARROT.count); assertEquals(12, MELON_SLICE.count);
        for (var offer : offers) {
            assertEquals(offer == CARROT || offer == MELON_SLICE, offer.buying);
            assertTrue(offer.price > 0); assertTrue(offer.stock > 0);
        }
    }
}
