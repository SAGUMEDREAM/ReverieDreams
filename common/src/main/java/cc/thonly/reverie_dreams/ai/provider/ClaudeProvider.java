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
public class ClaudeProvider implements ChatProvider {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");
    private static final int DEFAULT_MAX_TOKENS = 512;

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
                log.warn("Claude request skipped: empty conversation history");
                return null;
            }

            // Static persona + dynamic state belong into the top level system prompt.
            StringBuilder systemPrompt = new StringBuilder();
            int firstConversationIndex = 0;
            while (firstConversationIndex < history.size()
                    && AIMessage.SYSTEM.equals(history.get(firstConversationIndex).getRole())) {
                if (!systemPrompt.isEmpty()) {
                    systemPrompt.append("\n\n");
                }
                systemPrompt.append(history.get(firstConversationIndex).getContent());
                firstConversationIndex++;
            }
            if (!systemPrompt.isEmpty()) {
                systemPrompt.append("\n\n");
            }
            systemPrompt.append(ChatProvider.npcStatePrompt(npcState));

            List<Map<String, Object>> messages = new ArrayList<>(history.size());
            for (int i = firstConversationIndex; i < history.size(); i++) {
                AIMessage message = history.get(i);
                messages.add(Map.of(
                        "role", AIMessage.ASSISTANT.equals(message.getRole()) ? "assistant" : "user",
                        "content", List.of(Map.of(
                                "type", "text",
                                "text", message.getContent() == null ? "" : message.getContent()
                        ))
                ));
            }

            int maxTokens = settings.maxTokens();
            if (maxTokens <= 0) {
                maxTokens = DEFAULT_MAX_TOKENS;
            }
            double temperature = settings.temperature();
            // Anthropic only accepts temperatures between 0 and 1.
            temperature = Math.max(0.0, Math.min(1.0, temperature));

            Map<String, Object> body = new HashMap<>();
            body.put("model", settings.model());
            body.put("system", systemPrompt.toString());
            // max_tokens is REQUIRED by the Anthropic Messages API.
            body.put("max_tokens", maxTokens);
            body.put("temperature", temperature);
            body.put("messages", messages);

            String json = GSON.toJson(body);

            Request request = new Request.Builder()
                    .url(settings.url())
                    .post(RequestBody.create(json, JSON_MEDIA_TYPE))
                    .addHeader("x-api-key", settings.key())
                    .addHeader("anthropic-version", "2023-06-01")
                    .build();

            return parseReply(AIHttpTransport.execute(request, json, "claude"));
        } catch (CancellationException e) {
            return null;
        } catch (Exception e) {
            log.error("Claude request failed", e);
            return null;
        }
    }

    private static AIMessage parseReply(String response) {
        JsonObject root = JsonParser.parseString(response).getAsJsonObject();
        JsonArray content = root.getAsJsonArray("content");
        if (content == null || content.size() == 0) {
            log.error("Claude response has no content");
            return null;
        }
        for (JsonElement element : content) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject block = element.getAsJsonObject();
            JsonElement text = block.get("text");
            if (text == null || text.isJsonNull()) {
                continue;
            }
            String reply = text.getAsString().strip();
            if (!reply.isEmpty()) {
                return new AIMessage(AIMessage.ASSISTANT, reply);
            }
        }
        log.error("Claude response has no usable text block");
        return null;
    }
}
