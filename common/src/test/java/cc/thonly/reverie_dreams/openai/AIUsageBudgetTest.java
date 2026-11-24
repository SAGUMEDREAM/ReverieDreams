package cc.thonly.reverie_dreams.openai;

import cc.thonly.reverie_dreams.ai.AIUsageBudget;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class AIUsageBudgetTest {
    @Test void snapshotsAreReadOnlyAndDoNotRevealOtherPlayers() {
        var budget = new AIUsageBudget(() -> {});
        var other = UUID.randomUUID();
        budget.reserve(day, player, false, 100, limits).report(70);
        budget.reserve(day, other, true, 300, limits);
        var before = budget.save().deepCopy();
        var view = budget.snapshot(day, player, limits);
        assertEquals(1, view.get("player_requests").getAsInt());
        assertEquals(70, view.get("player_charged_tokens").getAsLong());
        assertEquals(70, view.get("player_reported_tokens").getAsLong());
        assertEquals(630, view.get("server_tokens_remaining").getAsLong());
        assertFalse(view.toString().contains(other.toString()));
        assertFalse(view.has("players"));
        assertEquals(before, budget.save());
    }

    @Test void viewingAfterMidnightShowsFreshLimitsWithoutDebitingOrMutatingTheLedger() {
        var budget = new AIUsageBudget(() -> {});
        budget.reserve(day, player, false, 500, limits);
        var saved = budget.save().deepCopy();
        var view = budget.snapshot("2026-11-06", player, limits);
        assertEquals(0, view.get("player_requests").getAsInt());
        assertEquals(800, view.get("player_tokens_remaining").getAsLong());
        assertEquals(saved, budget.save());
    }
    private final String day = "2026-11-05";
    private final UUID player = UUID.randomUUID();
    private final AIUsageBudget.Limits limits = new AIUsageBudget.Limits(3, 2, 1, 1000, 800);
    @Test void differentNPCsShareTheSamePlayerAndServerRequestCaps() {
        var budget = new AIUsageBudget(() -> {});
        assertNotNull(budget.reserve(day, player, false, 10, limits));
        assertNotNull(budget.reserve(day, player, false, 10, limits));
        assertNull(budget.reserve(day, player, false, 10, limits));
        assertNotNull(budget.reserve(day, UUID.randomUUID(), false, 10, limits));
        assertNull(budget.reserve(day, UUID.randomUUID(), false, 10, limits));
    }
    @Test void unknownOrFailedRequestsKeepReservedTokensAndKnownReceiptsSettleOnce() {
        var budget = new AIUsageBudget(() -> {});
        var first = budget.reserve(day, player, false, 600, limits);
        assertNotNull(first);
        assertNull(budget.reserve(day, player, false, 201, limits));
        first.report(100); first.report(900);
        assertNotNull(budget.reserve(day, player, false, 700, limits));
        assertEquals(800, budget.save().get("charged_tokens").getAsLong());
        assertEquals(100, budget.save().get("reported_tokens").getAsLong());
    }
    @Test void backgroundQuotaNeverConsumesAllForegroundCapacity() {
        var budget = new AIUsageBudget(() -> {});
        assertNotNull(budget.reserve(day, player, true, 10, limits));
        assertNull(budget.reserve(day, UUID.randomUUID(), true, 10, limits));
        assertNotNull(budget.reserve(day, player, false, 10, limits));
    }
    @Test void savingAndLoadingDoesNotResetTheDailyAllowance() {
        var budget = new AIUsageBudget(() -> {});
        budget.reserve(day, player, false, 500, limits);
        var loaded = new AIUsageBudget(() -> {});
        loaded.load(JsonParser.parseString(budget.save().toString()).getAsJsonObject());
        assertNull(loaded.reserve(day, player, false, 301, limits));
        assertNotNull(loaded.reserve(day, player, false, 300, limits));
    }
    @Test void midnightResetsAllowanceAndLateReceiptsCannotDebitANewDay() {
        var budget = new AIUsageBudget(() -> {});
        var yesterday = budget.reserve(day, player, false, 500, limits);
        assertNotNull(budget.reserve("2026-11-06", player, false, 200, limits));
        yesterday.report(1);
        assertEquals(200, budget.save().get("charged_tokens").getAsLong());
    }
    @Test void concurrentReservationsCannotOverspend() throws Exception {
        var budget = new AIUsageBudget(() -> {});
        var accepted = new AtomicInteger();
        try (var workers = Executors.newFixedThreadPool(8)) {
            var futures = new ArrayList<Future<?>>();
            for (int i=0; i<50; i++) futures.add(workers.submit(() -> {
                if (budget.reserve(day, UUID.randomUUID(), false, 500, limits) != null) accepted.incrementAndGet();
            }));
            for (var future : futures) future.get(2, TimeUnit.SECONDS);
        }
        assertEquals(2, accepted.get());
    }
    @Test void disablingBudgetsMeansDenyingRequestsRatherThanUnlimitedSpending() {
        assertNull(new AIUsageBudget(() -> {}).reserve(day, player, false, 1, new AIUsageBudget.Limits(0, 0, 0, 0, 0)));
    }
}
