package cc.thonly.reverie_dreams.openai.impl;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.interfaces.ChatAIEntity;
import cc.thonly.reverie_dreams.openai.AIMessage;
import cc.thonly.reverie_dreams.openai.ChatProvider;
import cc.thonly.reverie_dreams.server.ChatAIManager;
import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import okhttp3.*;

import java.util.*;

@SuppressWarnings("unchecked")
@Slf4j
public class ClaudeProvider implements ChatProvider {

    @Override
    public <T extends Entity> AIMessage chat(
            List<AIMessage> realHistory,
            ChatAIEntity<T> entity,
            ServerPlayer player,
            String msgStr
    ) {
        try {
            MinecraftServer server = ReverieDreams.getServer();
            if (server == null) return null;

            String npcState = entity.submitData(player, (T) entity.getEntity());

            List<AIMessage> history = AIMessage.copy(realHistory);

            List<Map<String, Object>> messages = new ArrayList<>();

            for (int i = 0; i < history.size() - 1; i++) {
                AIMessage m = history.get(i);
                messages.add(Map.of(
                        "role", m.getRole().equals("assistant") ? "assistant" : "user",
                        "content", List.of(Map.of("type", "text", "text", m.getContent()))
                ));
            }

            messages.add(Map.of(
                    "role", "user",
                    "content", List.of(Map.of("type", "text", "text", msgStr))
            ));

            String systemPrompt = """
                    Current NPC internal state (you feel this naturally, not as numbers).
                    This information is NOT necessarily known by the player.

                    Rules:
                    - Do NOT mention numbers or raw data
                    - Do NOT question how the player knows anything
                    - Do NOT say things like "how do you know that"
                    - Only express it naturally (tired, injured, fine, etc.)

                    """ + npcState;

            Map<String, Object> body = new HashMap<>();
            body.put("model", ChatAIManager.model()); // 例如 claude-3-sonnet
            body.put("system", systemPrompt);
            body.put("messages", messages);

            String json = new Gson().toJson(body);

            Request request = new Request.Builder()
                    .url(ChatAIManager.apiUrl()) // https://api.anthropic.com/v1/messages
                    .post(RequestBody.create(json, MediaType.parse("application/json")))
                    .addHeader("x-api-key", ChatAIManager.apiKey())
                    .addHeader("anthropic-version", "2023-06-01")
                    .build();

            try (Response response = OpenAIProvider.CLIENT.newCall(request).execute()) {

                if (!response.isSuccessful()) {
                    log.error("Claude HTTP Error: code={}, body={}",
                            response.code(),
                            response.body() != null ? response.body().string() : "null");
                    return null;
                }

                if (response.body() == null) return null;

                String resp = response.body().string();
                JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();

                String reply = obj.getAsJsonArray("content")
                                  .get(0).getAsJsonObject()
                                  .get("text").getAsString();

                return new AIMessage("assistant", reply);
            }

        } catch (Exception e) {
            log.error("Claude Error: {}", e.toString());
            return null;
        }
    }
}