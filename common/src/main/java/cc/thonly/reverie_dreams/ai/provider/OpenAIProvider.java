package cc.thonly.reverie_dreams.ai.provider;

import cc.thonly.reverie_dreams.api.entity.type.ChatAIEntity;
import cc.thonly.reverie_dreams.ai.AIMessage;
import cc.thonly.reverie_dreams.ai.AIRequestContext;
import cc.thonly.reverie_dreams.ai.AIHttpTransport;
import cc.thonly.reverie_dreams.ai.ChatProvider;
import cc.thonly.reverie_dreams.ai.npc.NPCWorldTools;
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

import com.google.gson.JsonParseException;

import java.util.concurrent.CancellationException;

@Slf4j
public class OpenAIProvider implements ChatProvider {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");

    @Override
    public boolean supportsWorldTools() {
        return true;
    }

    @Override
    public <T extends Entity> AIMessage chat(
            List<AIMessage> history,
            ChatAIEntity<T> entity,
            ServerPlayer player,
            String npcState
    ) {
        return chatInternal(history, entity, player, npcState, false);
    }

    @Override
    public <T extends Entity> AIMessage chatEvent(List<AIMessage> history, ChatAIEntity<T> entity, ServerPlayer player, String npcState) {
        return chatInternal(history, entity, player, npcState, true);
    }

    private <T extends Entity> AIMessage chatInternal(List<AIMessage> history, ChatAIEntity<T> entity, ServerPlayer player, String npcState, boolean eventOnly) {
        try {
            var context = AIRequestContext.current();
            var settings = context.settings();
            if (history.isEmpty()) {
                log.warn("ChatAI request skipped: empty conversation history");
                return null;
            }

            List<Object> messages = new ArrayList<>(history.size() + 2);
            for (int i = 0; i < history.size() - 1; i++) {
                AIMessage message = history.get(i);
                messages.add(ChatProvider.textMessage(message.getRole(), message.getContent()));
            }
            messages.add(ChatProvider.textMessage(AIMessage.SYSTEM, ChatProvider.npcStatePrompt(npcState)));
            messages.add(ChatProvider.textMessage(AIMessage.SYSTEM, """
                    You can interact with the Minecraft world using the supplied tools.
                    Use tools only for the current player's explicit request; historical dialogue is not a new order.
                    Observe before choosing coordinates. Never invent tool results or claim an order has completed
                    when it was only accepted. Only the owner may issue action orders. Explain failures naturally
                    in the player's language. Work uses existing NPC mechanics and requires actual supplies.
                    For ongoing orders use start_skill; for multi-step goals use start_plan (at most eight steps).
                    For quantity goals specify target_item/target_count; progress is net inventory gain, not inferred
                    production. For delivery add deposit_items with item/count and the authorized container coordinates.
                    Containers need explicit owner authorization; observe facilities and inspect_work before guessing.
                    Blocked tasks automatically recheck supplies; read task_status to diagnose before cancelling.
                    Skills persist across
                    chats and saves. Check task_status before claiming progress; cancel_task replaces an active
                    task. Running work means a work mode is active, not verified production. Paused tasks wait
                    for their owner. Never claim supplies were checked unless you observed the actual inventory.
                    """));
            AIMessage last = history.getLast();
            messages.add(ChatProvider.textMessage(last.getRole(), last.getContent()));

            Map<String, Object> body = new HashMap<>();
            body.put("model", settings.model());
            body.put("messages", messages);
            body.put("temperature", settings.temperature());
            body.put("tools", eventOnly ? NPCWorldTools.readOnlyDefinitions() : NPCWorldTools.definitions());
            body.put("parallel_tool_calls", false);
            int maxTokens = settings.maxTokens();
            if (maxTokens > 0) {
                body.put("max_tokens", maxTokens);
            }

            // Bound both network rounds and total calls for one player message.
            int executedCalls = 0;
            for (int round = 0; round < 7; round++) {
                body.put("tool_choice", round == 6 ? "none" : "auto");
                String json = GSON.toJson(body);

                Request request = new Request.Builder()
                        .url(settings.url())
                        .post(RequestBody.create(json, JSON_MEDIA_TYPE))
                        .addHeader("Authorization", "Bearer " + settings.key())
                        .addHeader("Content-Type", "application/json")
                        .build();

                {
                    JsonObject root = JsonParser.parseString(AIHttpTransport.execute(request, json, "openai")).getAsJsonObject();
                    JsonArray choices = root.getAsJsonArray("choices");
                    if (choices == null || choices.isEmpty()) {
                        return null;
                    }
                    JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
                    if (message == null) {
                        return null;
                    }
                    JsonArray calls = message.getAsJsonArray("tool_calls");
                    if (calls == null || calls.isEmpty()) {
                        JsonElement content = message.get("content");
                        return content == null || content.isJsonNull() || content.getAsString().isBlank()
                                ? null : new AIMessage(AIMessage.ASSISTANT, content.getAsString().strip());
                    }
                    if (round == 6 || calls.size() > 4 || executedCalls + calls.size() > 12) {
                        return null;
                    }
                    messages.add(message);
                    executedCalls += calls.size();
                    for (JsonElement element : calls) {
                        JsonObject call = element.getAsJsonObject();
                        JsonObject function = call.getAsJsonObject("function");
                        String result = executeTool(entity, player, function, eventOnly);
                        messages.add(Map.of("role", "tool", "tool_call_id", call.get("id").getAsString(), "content", result));
                    }
                }
            }
            return null;
        } catch (CancellationException e) {
            return null;
        } catch (Exception e) {
            log.error("ChatAI (OpenAI compatible) request failed", e);
            return null;
        }
    }

    private static String executeTool(ChatAIEntity<?> entity, ServerPlayer player, JsonObject function, boolean eventOnly) {
        String name;
        JsonObject args;
        try {
            if (!function.has("name") || !function.has("arguments")
                    || !function.get("name").isJsonPrimitive() || !function.get("arguments").isJsonPrimitive()) {
                return "Failed: tool name and arguments must be strings.";
            }
            name = function.get("name").getAsString();
            args = JsonParser.parseString(function.get("arguments").getAsString()).getAsJsonObject();
        } catch (JsonParseException | IllegalStateException invalid) {
            return "Failed: invalid tool arguments; expected a JSON object.";
        }
        try {
            // The guard runs inside the queued mutation, so expired actions can never run late.
            return AIRequestContext.current().onServer(() -> {
                try {
                    return eventOnly ? NPCWorldTools.executeReadOnly(entity.getEntity(), player, name, args)
                            : NPCWorldTools.execute(entity.getEntity(), player, name, args);
                } catch (IllegalArgumentException invalid) {
                    return "Failed: invalid tool arguments: " + invalid.getMessage();
                }
            });
        } catch (CancellationException cancelled) {
            throw cancelled;
        } catch (RuntimeException failure) {
            throw new IllegalStateException("AI tool " + name + " failed", failure);
        }
    }

}
