package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.entity.ai.YoukaiFeeding;
import cc.thonly.reverie_dreams.entity.ai.YoukaiRelations;
import cc.thonly.reverie_dreams.server.player.FactionReputationComponent;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ForestYoukaiReputationTest {
    private FactionReputationComponent component() {
        return new FactionReputationComponent(2, 3, 0, 5, 6, 7, 8, 9, 11);
    }

    private FactionReputationComponent reload(FactionReputationComponent component) {
        var encoded = FactionReputationComponent.CODEC.encodeStart(JsonOps.INSTANCE, component).getOrThrow();
        return FactionReputationComponent.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
    }

    @Test void fourActualMeatItemsPerPlayerAndDaySurviveReload() {
        var data = this.component();
        for (int i = 0; i < 4; i++) {
            assertTrue(data.acceptYoukaiFeeding(12));
            data = this.reload(data);
        }
        assertEquals(20, data.getYokai());
        assertEquals(4, data.getYoukaiFeeds());
        assertFalse(data.canFeedYoukai(12));
        assertFalse(data.acceptYoukaiFeeding(12));
        assertEquals(20, data.getYokai());
        assertEquals(2, data.getHuman());
        assertEquals(11, data.getHouseOfEternity());
    }

    @Test void newDayAndAnotherPlayerHaveIndependentQuotas() {
        var first = this.component();
        for (int i = 0; i < 4; i++) assertTrue(first.acceptYoukaiFeeding(5));
        assertTrue(this.component().acceptYoukaiFeeding(5));
        assertTrue(first.acceptYoukaiFeeding(6));
        assertEquals(1, first.getYoukaiFeeds());
        assertEquals(25, first.getYokai());
    }

    @Test void oldPlayerDataRetainsAllReputationAndStartsWithFreshQuota() {
        var old = FactionReputationComponent.CODEC.encodeStart(JsonOps.INSTANCE, this.component()).getOrThrow().getAsJsonObject();
        old.remove("youkai_feed_day");
        old.remove("youkai_feeds");
        var data = FactionReputationComponent.CODEC.parse(JsonOps.INSTANCE, old).getOrThrow();
        assertTrue(data.canFeedYoukai(0));
        assertEquals(0, data.getYokai());
        assertEquals(11, data.getHouseOfEternity());
        assertEquals(9, data.getLunarian());
        old.addProperty("youkai_feeds", 5);
        assertTrue(FactionReputationComponent.CODEC.parse(JsonOps.INSTANCE, old).error().isPresent());
    }

    @Test void feedingAndDamageClampWithoutChangingOtherFactions() {
        var data = this.component();
        data.setYokai(198);
        assertTrue(data.acceptYoukaiFeeding(0));
        assertEquals(200, data.getYokai());
        data.addYokai(-10);
        assertEquals(190, data.getYokai());
        data.addYokai(Integer.MIN_VALUE);
        assertEquals(-100, data.getYokai());
        data.addYokai(Integer.MAX_VALUE);
        assertEquals(200, data.getYokai());
        assertEquals(3, data.getFairy());
        assertEquals(11, data.getHouseOfEternity());
    }

    @Test void peaceSquareIncludesCornersAndBoundaryButNotOutsideAggressors() {
        assertTrue(YoukaiRelations.insideSquare(1000, -1000, 1128, -872));
        assertTrue(YoukaiRelations.insideSquare(1000, -1000, 872, -1128));
        assertFalse(YoukaiRelations.insideSquare(1000, -1000, 1128.01, -1000));
        assertFalse(YoukaiRelations.insideSquare(1000, -1000, 1000, -1128.01));
    }

    @Test void provenanceOnlyMergesSameDonorOrOrdinaryLootTogether() {
        UUID first = UUID.randomUUID(), second = UUID.randomUUID();
        assertTrue(YoukaiFeeding.mayMerge(null, null));
        assertTrue(YoukaiFeeding.mayMerge(first, first));
        assertFalse(YoukaiFeeding.mayMerge(first, second));
        assertFalse(YoukaiFeeding.mayMerge(first, null));
        assertFalse(YoukaiFeeding.mayMerge(null, second));
    }
}
