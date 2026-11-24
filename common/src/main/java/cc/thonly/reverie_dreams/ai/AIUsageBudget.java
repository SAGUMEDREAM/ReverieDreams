package cc.thonly.reverie_dreams.ai;

import com.google.gson.JsonObject;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Shared by every NPC/provider in a save. Unknown/failed usage keeps its conservative reservation.
 */
public final class AIUsageBudget {
    public record Limits(int requests, int playerRequests, int backgroundRequests, long tokens, long playerTokens) {
    }

    private static final class Usage {
        long requests, tokens, reportedTokens;
    }

    private final Map<UUID, Usage> players = new HashMap<>();
    private final Runnable dirty;
    private String day = today();
    private long requests, backgroundRequests, tokens, reportedTokens;

    public AIUsageBudget(Runnable dirty) {
        this.dirty = dirty;
    }

    public static String today() {
        return LocalDate.now(ZoneOffset.UTC).toString();
    }

    public synchronized Reservation reserve(String today, UUID player, boolean background, long estimatedTokens, Limits limits) {
        if (!today.equals(day)) {
            day = today;
            requests = backgroundRequests = tokens = reportedTokens = 0;
            players.clear();
        }
        var usage = players.get(player);
        long playerRequests = usage == null ? 0 : usage.requests, playerTokens = usage == null ? 0 : usage.tokens;
        estimatedTokens = Math.max(1, estimatedTokens);
        if (requests >= limits.requests || playerRequests >= limits.playerRequests
                || background && backgroundRequests >= limits.backgroundRequests || players.size() >= 4096 && usage == null
                || estimatedTokens > limits.tokens - tokens || estimatedTokens > limits.playerTokens - playerTokens)
            return null;
        if (usage == null) {
            usage = new Usage();
            players.put(player, usage);
        }
        requests++;
        usage.requests++;
        tokens += estimatedTokens;
        usage.tokens += estimatedTokens;
        if (background)
            backgroundRequests++;
        dirty.run();
        return new Reservation(this, day, player, estimatedTokens);
    }

    public static final class Reservation {
        private final AIUsageBudget budget;
        private final String day;
        private final UUID player;
        private final long estimated;
        private boolean settled;

        private Reservation(AIUsageBudget budget, String day, UUID player, long estimated) {
            this.budget = budget;
            this.day = day;
            this.player = player;
            this.estimated = estimated;
        }

        public void report(long actual) {
            synchronized (budget) {
                if (settled || actual < 0)
                    return;
                settled = true;
                if (!day.equals(budget.day))
                    return;
                var usage = budget.players.get(player);
                // Bound malformed provider data while retaining the over-limit signal.
                actual = Math.min(actual, 1_000_000_000L);
                budget.tokens += actual - estimated;
                usage.tokens += actual - estimated;
                budget.reportedTokens += actual;
                usage.reportedTokens += actual;
                budget.dirty.run();
            }
        }
    }

    public synchronized JsonObject save() {
        JsonObject data = new JsonObject();
        data.addProperty("day_utc", day);
        data.addProperty("requests", requests);
        data.addProperty("background_requests", backgroundRequests);
        data.addProperty("charged_tokens", tokens);
        data.addProperty("reported_tokens", reportedTokens);
        JsonObject entries = new JsonObject();
        players.forEach((id, u) -> {
            JsonObject entry = new JsonObject();
            entry.addProperty("requests", u.requests);
            entry.addProperty("charged_tokens", u.tokens);
            entry.addProperty("reported_tokens", u.reportedTokens);
            entries.add(id.toString(), entry);
        });
        data.add("players", entries);
        return data;
    }

    /**
     * Read-only player view: another player's identity or usage is never exposed.
     */
    public synchronized JsonObject snapshot(String today, UUID player, Limits limits) {
        boolean current = day.equals(today);
        var own = current ? players.get(player) : null;
        JsonObject view = new JsonObject();
        view.addProperty("day_utc", today);
        view.addProperty("reset_time", "00:00 UTC / 08:00 Asia/Shanghai");
        view.addProperty("player_requests", own == null ? 0 : own.requests);
        view.addProperty("player_request_limit", limits.playerRequests);
        view.addProperty("player_requests_remaining", Math.max(0, limits.playerRequests - (own == null ? 0 : own.requests)));
        view.addProperty("player_charged_tokens", own == null ? 0 : own.tokens);
        view.addProperty("player_reported_tokens", own == null ? 0 : own.reportedTokens);
        view.addProperty("player_token_limit", limits.playerTokens);
        view.addProperty("player_tokens_remaining", Math.max(0, limits.playerTokens - (own == null ? 0 : own.tokens)));
        view.addProperty("server_requests_remaining", Math.max(0, limits.requests - (current ? requests : 0)));
        view.addProperty("server_requests", current ? requests : 0);
        view.addProperty("server_request_limit", limits.requests);
        view.addProperty("server_charged_tokens", current ? tokens : 0);
        view.addProperty("server_token_limit", limits.tokens);
        view.addProperty("background_requests", current ? backgroundRequests : 0);
        view.addProperty("background_request_limit", limits.backgroundRequests);
        view.addProperty("server_tokens_remaining", Math.max(0, limits.tokens - (current ? tokens : 0)));
        view.addProperty("background_requests_remaining", Math.max(0, limits.backgroundRequests - (current ? backgroundRequests : 0)));
        view.addProperty("accounting", "charged tokens include conservative reservations; this is not the provider's currency balance");
        return view;
    }

    public synchronized void load(JsonObject data) {
        day = data.get("day_utc").getAsString();
        LocalDate.parse(day);
        requests = nonnegative(data, "requests");
        backgroundRequests = nonnegative(data, "background_requests");
        tokens = nonnegative(data, "charged_tokens");
        reportedTokens = nonnegative(data, "reported_tokens");
        players.clear();
        for (var entry : data.getAsJsonObject("players").entrySet()) {
            if (players.size() >= 4096)
                throw new IllegalArgumentException("Too many AI budget entries");
            var u = new Usage();
            var object = entry.getValue().getAsJsonObject();
            u.requests = nonnegative(object, "requests");
            u.tokens = nonnegative(object, "charged_tokens");
            u.reportedTokens = nonnegative(object, "reported_tokens");
            players.put(UUID.fromString(entry.getKey()), u);
        }
    }

    private static long nonnegative(JsonObject data, String key) {
        long value = data.get(key).getAsLong();
        if (value < 0 || value > 1_000_000_000_000L)
            throw new IllegalArgumentException("Invalid AI usage");
        return value;
    }
}
