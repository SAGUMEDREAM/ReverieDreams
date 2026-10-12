package cc.thonly.reverie_dreams.openai.impl;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.entity.type.ChatAIEntity;
import cc.thonly.reverie_dreams.openai.AIMessage;
import cc.thonly.reverie_dreams.openai.ChatProvider;
import cc.thonly.reverie_dreams.server.ChatAIManager;
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
import okhttp3.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class OpenAIProvider implements ChatProvider {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");

    @Override
    public <T extends Entity> AIMessage chat(
            List<AIMessage> history,
            ChatAIEntity<T> entity,
            ServerPlayer player,
            String npcState
    ) {
        try {
            if (history.isEmpty()) {
                log.warn("ChatAI request skipped: empty conversation history");
                return null;
            }

            List<Map<String, String>> messages = new ArrayList<>(history.size() + 1);
            for (int i = 0; i < history.size() - 1; i++) {
                AIMessage message = history.get(i);
                messages.add(ChatProvider.textMessage(message.getRole(), message.getContent()));
            }
            messages.add(ChatProvider.textMessage(AIMessage.SYSTEM, ChatProvider.npcStatePrompt(npcState)));
            AIMessage last = history.getLast();
            messages.add(ChatProvider.textMessage(last.getRole(), last.getContent()));

            Map<String, Object> body = new HashMap<>();
            body.put("model", ChatAIManager.model());
            body.put("messages", messages);
            body.put("temperature", ReverieDreams.config().temperature);
            int maxTokens = ReverieDreams.config().maxTokens;
            if (maxTokens > 0) {
                body.put("max_tokens", maxTokens);
            }

            String json = GSON.toJson(body);

            Request request = new Request.Builder()
                    .url(ChatAIManager.apiUrl())
                    .post(RequestBody.create(json, JSON_MEDIA_TYPE))
                    .addHeader("Authorization", "Bearer " + ChatAIManager.apiKey())
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = CLIENT.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("ChatAI HTTP error: code={}, body={}",
                            response.code(),
                            response.body() != null ? response.body().string() : "null"
                    );
                    return null;
                }
                if (response.body() == null) {
                    log.error("ChatAI response body is null");
                    return null;
                }
                return parseReply(response.body().string());
            }
        } catch (Exception e) {
            log.error("ChatAI (OpenAI compatible) request failed", e);
            return null;
        }
    }

    private static AIMessage parseReply(String response) {
        JsonObject root = JsonParser.parseString(response).getAsJsonObject();
        JsonArray choices = root.getAsJsonArray("choices");
        if (choices == null || choices.size() == 0) {
            log.error("ChatAI response has no choices");
            return null;
        }
        JsonObject first = choices.get(0).getAsJsonObject();
        JsonObject message = first.getAsJsonObject("message");
        if (message == null) {
            log.error("ChatAI response has no message object");
            return null;
        }
        JsonElement content = message.get("content");
        if (content == null || content.isJsonNull()) {
            log.error("ChatAI response has empty message content");
            return null;
        }
        String reply = content.getAsString().strip();
        if (reply.isEmpty()) {
            log.error("ChatAI response content is blank");
            return null;
        }
        return new AIMessage(AIMessage.ASSISTANT, reply);
    }
}
