package cc.thonly.reverie_dreams.openai;

import cc.thonly.reverie_dreams.ai.*;
import cc.thonly.reverie_dreams.api.entity.type.ChatAIEntity;
import cc.thonly.reverie_dreams.ai.provider.OpenAIProvider;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.Function;
import cc.thonly.reverie_dreams.ai.provider.ClaudeProvider;
import cc.thonly.reverie_dreams.ai.provider.GeminiProvider;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** Uses an actual HTTP conversation against a loopback fixture; no external provider/config/credentials. */
@Timeout(15)
class AIProviderLoopTest {
    private static final List<AIMessage> HISTORY = List.of(new AIMessage("system", "Fixture persona"), new AIMessage("user", "fixture request"));
    private static final String TOOL = """
            {"choices":[{"message":{"role":"assistant","content":null,"tool_calls":[
              {"id":"fixture-call","type":"function","function":{"name":"follow","arguments":"{}"}}
            ]}}],"usage":{"total_tokens":7}}
            """;
    private static final String REPLY = "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"fixture finished\"}}],\"usage\":{\"total_tokens\":9}}";

    private static class Fixture implements AutoCloseable {
        final HttpServer server;
        final ExecutorService handlers = Executors.newVirtualThreadPerTaskExecutor();
        final AtomicInteger calls = new AtomicInteger();
        final AIUsageBudget usage = new AIUsageBudget(() -> {});
        final AtomicBoolean valid = new AtomicBoolean(true);
        final Function<JsonObject, String> response;
        Fixture(Function<JsonObject, String> response) throws Exception {
            this.response = response;
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.setExecutor(handlers);
            server.createContext("/chat", exchange -> {
                var request = JsonParser.parseString(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
                calls.incrementAndGet();
                try {
                    byte[] reply = this.response.apply(request).getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, reply.length);
                    exchange.getResponseBody().write(reply);
                } finally { exchange.close(); }
            });
            server.start();
        }
        AIRequestContext request(AIUsageBudget.Limits limits) {
            return new AIRequestContext(Runnable::run, valid::get,
                    new AIRequestContext.Settings("http://127.0.0.1:" + server.getAddress().getPort() + "/chat", "fixture-only", "fixture-model", "", .7, 512),
                    "fixture request", bytes -> usage.reserve("2026-11-05", new UUID(0, 1), false, bytes + 512L, limits), TimeUnit.SECONDS.toNanos(10));
        }
        AIRequestContext request() { return request(new AIUsageBudget.Limits(10, 10, 10, 1000000, 1000000)); }
        @Override public void close() { server.stop(0); handlers.shutdownNow(); }
    }
    private static class Target implements ChatAIEntity<Entity> {
        final AtomicInteger accesses = new AtomicInteger();
        @Override public Entity getEntity() { accesses.incrementAndGet(); return null; }
        @Override public List<AIMessage> getChatHistory(ServerPlayer player) { return List.of(); }
        @Override public void clearChatHistory() {}
        @Override public void clearChatHistory(ServerPlayer player) {}
        @Override public void openChatAIGUI(ServerPlayer player) {}
        @Override public CompletableFuture<Void> send(ServerPlayer player, String message) { return CompletableFuture.completedFuture(null); }
        @Override public void handleCommand(String message) {}
        @Override public String submitData(ServerPlayer player, Entity entity) { return "{}"; }
    }

    @Test void eventAnalysisRejectsRogueWriteToolAndReturnsItsActualReceiptToModel() throws Exception {
        var seen = new AtomicInteger();
        try (var fixture = new Fixture(body -> {
            assertEquals("fixture-model", body.get("model").getAsString());
            if (seen.getAndIncrement() == 0) return TOOL;
            var messages = body.getAsJsonArray("messages");
            var receipt = messages.get(messages.size()-1).getAsJsonObject();
            assertEquals("tool", receipt.get("role").getAsString());
            assertTrue(receipt.get("content").getAsString().startsWith("Denied:"));
            return REPLY;
        }); var request = fixture.request()) {
            var target = new Target();
            var reply = request.run(() -> new OpenAIProvider().chatEvent(HISTORY, target, null, "{}"));
            assertNotNull(reply); assertEquals("fixture finished", reply.getContent());
            assertEquals(2, fixture.calls.get());
            assertEquals(16, fixture.usage.save().get("reported_tokens").getAsLong());
        }
    }
    @Test void changedCommandDuringModelWaitPreventsWorldAccessAndFurtherHttpRounds() throws Exception {
        try (var fixture = new Fixture(body -> { throw new AssertionError("replaced below"); })) {
            fixture.server.removeContext("/chat");
            fixture.server.createContext("/chat", exchange -> {
                exchange.getRequestBody().readAllBytes(); fixture.calls.incrementAndGet();
                fixture.valid.set(false);
                byte[] reply = TOOL.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, reply.length); exchange.getResponseBody().write(reply); exchange.close();
            });
            try (var request = fixture.request()) {
                var target = new Target();
                assertNull(request.run(() -> new OpenAIProvider().chat(HISTORY, target, null, "{}")));
                assertEquals(0, target.accesses.get());
                assertEquals(1, fixture.calls.get());
            }
        }
    }
    @Test void exhaustedBudgetPreventsEvenTheFirstHttpRequest() throws Exception {
        try (var fixture = new Fixture(body -> REPLY); var request = fixture.request(new AIUsageBudget.Limits(0, 0, 0, 0, 0))) {
            assertNull(request.run(() -> new OpenAIProvider().chat(HISTORY, new Target(), null, "{}")));
            assertTrue(request.budgetDenied()); assertEquals(0, fixture.calls.get());
        }
    }
    @Test void eachToolRoundRequiresANewBudgetReservation() throws Exception {
        try (var fixture = new Fixture(body -> TOOL); var request = fixture.request(new AIUsageBudget.Limits(1, 1, 0, 1000000, 1000000))) {
            assertNull(request.run(() -> new OpenAIProvider().chatEvent(HISTORY, new Target(), null, "{}")));
            assertEquals(1, fixture.calls.get()); assertTrue(request.budgetDenied());
        }
    }
    @Test void cancelledQueuedConversationMakesNoHttpRequests() throws Exception {
        try (var fixture = new Fixture(body -> REPLY); var request = fixture.request()) {
            request.cancel();
            assertThrows(CancellationException.class, () -> request.run(() -> new OpenAIProvider().chat(HISTORY, new Target(), null, "{}")));
            assertEquals(0, fixture.calls.get());
        }
    }
    @Test void allProvidersReceiptsUseTheSameUsageLedger() {
        var budget = new AIUsageBudget(() -> {});
        for (var provider : List.of("openai", "claude", "gemini")) {
            var reservation = budget.reserve("2026-11-05", new UUID(0, 1), false, 100,
                    new AIUsageBudget.Limits(10, 10, 10, 1000, 1000));
            String receipt = switch (provider) {
                case "claude" -> "{\"usage\":{\"input_tokens\":3,\"output_tokens\":4}}";
                case "gemini" -> "{\"usageMetadata\":{\"totalTokenCount\":7}}";
                default -> "{\"usage\":{\"total_tokens\":7}}";
            };
            AIHttpTransport.reportUsage(reservation, JsonParser.parseString(receipt).getAsJsonObject(), provider);
        }
        assertEquals(21, budget.save().get("reported_tokens").getAsLong());
    }

    @Test void nativeProvidersUseTheSameGatedTransport() throws Exception {
        for (String type : List.of("claude", "gemini")) {
            String reply = type.equals("claude")
                    ? "{\"content\":[{\"type\":\"text\",\"text\":\"native fixture\"}],\"usage\":{\"input_tokens\":2,\"output_tokens\":3}}"
                    : "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"native fixture\"}]}}],\"usageMetadata\":{\"totalTokenCount\":5}}";
            ChatProvider provider = type.equals("claude") ? new ClaudeProvider()
                    : new GeminiProvider();
            try (var fixture = new Fixture(body -> reply); var request = fixture.request()) {
                assertEquals("native fixture", request.run(() -> provider.chat(HISTORY, new Target(), null, "{}")).getContent());
                assertEquals(5, fixture.usage.save().get("reported_tokens").getAsLong());
                fixture.valid.set(false);
                assertNull(request.run(() -> provider.chat(HISTORY, new Target(), null, "{}")));
                assertEquals(1, fixture.calls.get());
            }
        }
    }

    @Test void cancellingAnActualInFlightHttpRequestReleasesTheWorker() throws Exception {
        var arrived = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        try (var fixture = new Fixture(body -> {
            arrived.countDown();
            try { release.await(5,TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return REPLY;
        }); var request = fixture.request(); var worker = Executors.newSingleThreadExecutor()) {
            try {
                var result = worker.submit(() -> request.run(() -> new OpenAIProvider().chat(HISTORY,new Target(),null,"{}")));
                assertTrue(arrived.await(3,TimeUnit.SECONDS));
                request.cancel();
                assertNull(result.get(2,TimeUnit.SECONDS));
                assertEquals(1,fixture.calls.get());
                assertTrue(fixture.usage.save().get("charged_tokens").getAsLong()>0);
            } finally { release.countDown(); }
        }
    }
}
