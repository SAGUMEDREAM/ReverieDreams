package cc.thonly.reverie_dreams.paper;

import cc.thonly.reverie_dreams.paper.storage.FaithRules;
import java.util.Random;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FaithRulesTest {
    @Test void sameGameDayRejectsWithoutDrawingRandomness() {
        var state = new FaithRules.State(15, 2);
        var result = FaithRules.pray(state, 8, 2 * 24000L + 23999, new Random() { @Override public double nextDouble() { throw new AssertionError("Same day consumed randomness"); } });
        assertEquals(FaithRules.Status.ALREADY_PRAYED, result.status()); assertEquals(state, result.state()); assertEquals(0, result.requestedCoins());
    }
    @Test void sourceOverCapRandomBonusStillConsumesAndUpdatesDayButSetterRefusesValue() {
        var result = FaithRules.pray(new FaithRules.State(199, -1), 1, 0, new Random() {
            @Override public double nextDouble() { return .999; }
            @Override public int nextInt(int origin, int bound) { return 2; }
        });
        assertEquals(FaithRules.Status.SUCCESS, result.status()); assertEquals(new FaithRules.State(199, 0), result.state()); assertEquals(210, result.displayedValue()); assertEquals(2, result.requestedCoins());
    }
    @Test void sourceFullBranchDrawsBonusBeforeClampingWithoutChargingOrUpdatingDay() {
        int[] draws = {0}; var result = FaithRules.pray(new FaithRules.State(199, -1), 8, 0, new Random() { @Override public double nextDouble() { draws[0]++; return 0; } });
        assertEquals(1, draws[0]); assertEquals(FaithRules.Status.FULL, result.status()); assertEquals(new FaithRules.State(200, -1), result.state()); assertEquals(0, result.requestedCoins());
        assertEquals(new FaithRules.State(-3, 7), FaithRules.setValue(new FaithRules.State(0, 7), -3));
    }
}
