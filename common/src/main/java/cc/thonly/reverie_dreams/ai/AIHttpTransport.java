package cc.thonly.reverie_dreams.ai;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.Request;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * All providers pass through the same cancellation, per-round budget and bounded response reader.
 */
public final class AIHttpTransport {
    private AIHttpTransport() {
    }

    public static String execute(Request request, String requestJson, String provider) throws IOException {
        var context = AIRequestContext.current();
        var reservation = context.reserve(requestJson.getBytes(StandardCharsets.UTF_8).length);
        var call = ChatProvider.client().newCall(request);
        context.attach(call);
        call.timeout().timeout(Math.max(1, Math.min(context.remainingNanos(), TimeUnit.SECONDS.toNanos(150))), TimeUnit.NANOSECONDS);
        try (var response = call.execute()) {
            if (!response.isSuccessful() || response.body() == null)
                throw new IOException("AI HTTP status " + response.code());
            byte[] bytes = response.body().byteStream().readNBytes(1_048_577);
            if (bytes.length > 1_048_576)
                throw new IOException("AI response exceeds 1 MiB");
            String text = new String(bytes, StandardCharsets.UTF_8);
            var root = JsonParser.parseString(text).getAsJsonObject();
            context.account(() -> reportUsage(reservation, root, provider));
            return text;
        } finally {
            context.detach(call);
        }
    }

    public static void reportUsage(AIUsageBudget.Reservation reservation, JsonObject root, String provider) {
        try {
            if (provider.equals("gemini"))
                reservation.report(root.getAsJsonObject("usageMetadata").get("totalTokenCount").getAsLong());
            else {
                var usage = root.getAsJsonObject("usage");
                reservation.report(provider.equals("claude") ? usage.get("input_tokens").getAsLong() + usage.get("output_tokens").getAsLong()
                        + (usage.has("cache_creation_input_tokens") ? usage.get("cache_creation_input_tokens").getAsLong() : 0)
                        + (usage.has("cache_read_input_tokens") ? usage.get("cache_read_input_tokens").getAsLong() : 0)
                        : usage.get("total_tokens").getAsLong());
            }
        } catch (RuntimeException ignored) { /* No usage receipt: keep the entire reservation. */ }
    }
}
