package cc.thonly.reverie_dreams.openai.impl;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.entity.type.ChatAIEntity;
import cc.thonly.reverie_dreams.openai.AIMessage;
import cc.thonly.reverie_dreams.openai.ChatProvider;
import cc.thonly.reverie_dreams.server.ChatAIManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.server.MinecraftServer;
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

@SuppressWarnings("unchecked")
@Slf4j
public class OpenAIProvider implements ChatProvider {

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

            List<AIMessage> copyHistory = AIMessage.copy(realHistory);
            List<Map<String, String>> messages = new ArrayList<>();

            for (int i = 0; i < copyHistory.size() - 1; i++) {
                AIMessage m = copyHistory.get(i);
                messages.add(Map.of(
                        "role", m.getRole(),
                        "content", m.getContent()
                ));
            }

            messages.add(Map.of(
                    "role", "system",
                    "content", """
                            Current NPC internal state (you feel this naturally, not as numbers).
                            This information is NOT necessarily known by the player.
                            
                            Rules:
                            - Do NOT mention numbers or raw data
                            - Do NOT question how the player knows anything
                            - Do NOT say things like "how do you know that"
                            - Only express it naturally (tired, injured, fine, etc.)
                            
                            """ + npcState
            ));

            messages.add(Map.of(
                    "role", "user",
                    "content", msgStr
            ));

            Map<String, Object> body = new HashMap<>();
            body.put("model", ChatAIManager.model());
            body.put("messages", messages);
            body.put("temperature", 0.7);

            String json = new Gson().toJson(body);

            Request request = new Request.Builder()
                    .url(ChatAIManager.apiUrl())
                    .post(RequestBody.create(
                            json,
                            MediaType.parse("application/json")
                    ))
                    .addHeader("Authorization", "Bearer " + ChatAIManager.apiKey())
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = CLIENT.newCall(request).execute()) {

                if (!response.isSuccessful()) {
                    log.error("ChatAI HTTP Error: code={}, body={}",
                            response.code(),
                            response.body() != null ? response.body().string() : "null"
                    );
                    return null;
                }

                if (response.body() == null) {
                    log.error("ChatAI response body is null");
                    return null;
                }

                String resp = response.body().string();

                JsonObject obj = JsonParser.parseString(resp).getAsJsonObject();
                String reply = obj.getAsJsonArray("choices")
                                  .get(0).getAsJsonObject()
                                  .getAsJsonObject("message")
                                  .get("content").getAsString();

                return new AIMessage("assistant", reply);
            }

        } catch (Exception e) {
            log.error("Called ChatAI Error: {}", e.toString());
            return null;
        }
    }
}