package cc.thonly.reverie_dreams.ai.provider;

import cc.thonly.reverie_dreams.api.entity.type.ChatAIEntity;
import cc.thonly.reverie_dreams.ai.AIMessage;
import cc.thonly.reverie_dreams.ai.AIRequestContext;
import cc.thonly.reverie_dreams.ai.AIHttpTransport;
import cc.thonly.reverie_dreams.ai.ChatProvider;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;

@Slf4j
public class GeminiProvider implements ChatProvider {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");

    @Override
    public <T extends Entity> AIMessage chat(
            List<AIMessage> history,
            ChatAIEntity<T> entity,
            ServerPlayer player,
            String npcState
    ) {
        try {
            var settings = AIRequestContext.current().settings();
            if (history.isEmpty()) {
                log.warn("Gemini request skipped: empty conversation history");
                return null;
            }

            // Gemini has no cross-version reliable system role, so the static persona
            // and the dynamic NPC state are merged into the leading user turn.
            StringBuilder persona = new StringBuilder();
            int firstConversationIndex = 0;
            while (firstConversationIndex < history.size()
                    && AIMessage.SYSTEM.equals(history.get(firstConversationIndex).getRole())) {
                if (!persona.isEmpty()) {
                    persona.append("\n\n");
                }
                persona.append(history.get(firstConversationIndex).getContent());
                firstConversationIndex++;
            }

            String statePrompt = ChatProvider.npcStatePrompt(npcState);
            String leadText = persona.isEmpty() ? statePrompt : persona + "\n\n" + statePrompt;

            List<Map<String, Object>> contents = new ArrayList<>(history.size() + 1);
            contents.add(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", leadText))
            ));

            for (int i = firstConversationIndex; i < history.size(); i++) {
                AIMessage message = history.get(i);
                contents.add(Map.of(
                        "role", AIMessage.ASSISTANT.equals(message.getRole()) ? "model" : "user",
                        "parts", List.of(Map.of("text", message.getContent() == null ? "" : message.getContent()))
                ));
            }

            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", settings.temperature());
            int maxTokens = settings.maxTokens();
            if (maxTokens > 0) {
                generationConfig.put("maxOutputTokens", maxTokens);
            }

            Map<String, Object> body = new HashMap<>();
            body.put("contents", contents);
            body.put("generationConfig", generationConfig);

            String json = GSON.toJson(body);

            Request request = new Request.Builder()
                    .url(settings.url())
                    .post(RequestBody.create(json, JSON_MEDIA_TYPE))
                    // API keys must never travel in the query string (they end up in logs).
                    .addHeader("x-goog-api-key", settings.key())
                    .build();

            return parseReply(AIHttpTransport.execute(request, json, "gemini"));
        } catch (CancellationException e) {
            return null;
        } catch (Exception e) {
            log.error("Gemini request failed", e);
            return null;
        }
    }

    private static AIMessage parseReply(String response) {
        JsonObject root = JsonParser.parseString(response).getAsJsonObject();
        JsonArray candidates = root.getAsJsonArray("candidates");
        if (candidates == null || candidates.size() == 0) {
            log.error("Gemini response has no candidates");
            return null;
        }
        JsonObject candidate = candidates.get(0).getAsJsonObject();
        JsonObject content = candidate.getAsJsonObject("content");
        if (content == null) {
            JsonElement finishReason = candidate.get("finishReason");
            log.error("Gemini response has no content (finishReason={})",
                    finishReason == null ? "unknown" : finishReason.getAsString());
            return null;
        }
        JsonArray parts = content.getAsJsonArray("parts");
        if (parts == null || parts.size() == 0) {
            log.error("Gemini response has no parts");
            return null;
        }
        JsonElement text = parts.get(0).getAsJsonObject().get("text");
        if (text == null || text.isJsonNull()) {
            log.error("Gemini response part has no text");
            return null;
        }
        String reply = text.getAsString().strip();
        if (reply.isEmpty()) {
            log.error("Gemini response text is blank");
            return null;
        }
        return new AIMessage(AIMessage.ASSISTANT, reply);
    }
}
