package cc.thonly.reverie_dreams.openai.impl;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.entity.type.ChatAIEntity;
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
public class GeminiProvider implements ChatProvider {

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

            List<Map<String, Object>> contents = new ArrayList<>();

            // system prompt（Gemini要塞进第一条 user）
            String systemPrompt = """
                    Current NPC internal state (you feel this naturally, not as numbers).
                    This information is NOT necessarily known by the player.

                    Rules:
                    - Do NOT mention numbers or raw data
                    - Do NOT question how the player knows anything
                    - Do NOT say things like "how do you know that"
                    - Only express it naturally (tired, injured, fine, etc.)

                    """ + npcState;

            contents.add(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", systemPrompt))
            ));

            for (int i = 0; i < history.size() - 1; i++) {
                AIMessage m = history.get(i);
                contents.add(Map.of(
                        "role", m.getRole().equals("assistant") ? "model" : "user",
                        "parts", List.of(Map.of("text", m.getContent()))
                ));
            }

            contents.add(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", msgStr))
            ));

            Map<String, Object> body = new HashMap<>();
            body.put("contents", contents);

            String json = new Gson().toJson(body);

            String url = ChatAIManager.apiUrl() +
                    "?key=" + ChatAIManager.apiKey();

            Request request = new Request.Builder()
                    .url(url) // https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent
                    .post(RequestBody.create(json, MediaType.parse("application/json")))
                    .build();

            try (Response response = OpenAIProvider.CLIENT.newCall(request).execute()) {

                if (!response.isSuccessful()) {
                    log.error("Gemini HTTP Error: code={}, body={}",
                            response.code(),
                            response.body() != null ? response.body().string() : "null");
                    return null;
                }

                if (response.body() == null) return null;

                String resp = response.body().string();
                JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();

                String reply = obj.getAsJsonArray("candidates")
                                  .get(0).getAsJsonObject()
                                  .getAsJsonObject("content")
                                  .getAsJsonArray("parts")
                                  .get(0).getAsJsonObject()
                                  .get("text").getAsString();

                return new AIMessage("assistant", reply);
            }

        } catch (Exception e) {
            log.error("Gemini Error: {}", e.toString());
            return null;
        }
    }
}