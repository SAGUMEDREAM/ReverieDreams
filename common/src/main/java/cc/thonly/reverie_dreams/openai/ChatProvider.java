package cc.thonly.reverie_dreams.openai;

import cc.thonly.reverie_dreams.api.entity.type.ChatAIEntity;
import com.google.gson.Gson;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import okhttp3.OkHttpClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public interface ChatProvider {
    /**
     * Shared HTTP client for all AI providers. The default OkHttp timeouts are far too
     * short for LLM responses (a non-streaming reply can take a long time to arrive),
     * so every timeout is set explicitly.
     */
    OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .callTimeout(150, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    /**
     * Dedicated (daemon) executor for AI requests. Never block the
     * ForkJoinPool#commonPool with network I/O.
     */
    ExecutorService EXECUTOR = Executors.newFixedThreadPool(4, runnable -> {
        Thread thread = new Thread(runnable, "ReverieDreams-ChatAI");
        thread.setDaemon(true);
        return thread;
    });

    Gson GSON = new Gson();

    /**
     * Sends a complete conversation snapshot (including the newest user message) to
     * the AI backend and returns the assistant reply, or {@code null} on failure.
     *
     * @param history  immutable snapshot of the conversation, newest message last
     * @param entity   the NPC that is talking
     * @param player   the player that is talking to the NPC
     * @param npcState human readable description of the NPC's current state
     */
    <T extends Entity> AIMessage chat(List<AIMessage> history, ChatAIEntity<T> entity, ServerPlayer player, String npcState);

    /**
     * Wraps the NPC state into the system instructions shared by every provider.
     */
    static String npcStatePrompt(String npcState) {
        return """
                Current NPC internal state (you feel this naturally, not as numbers).
                This information is NOT necessarily known by the player.

                Rules:
                - Do NOT mention numbers or raw data
                - Do NOT question how the player knows anything
                - Do NOT say things like "how do you know that"
                - Only express it naturally (tired, injured, fine, etc.)

                """ + npcState;
    }

    /**
     * Builds a role/content message map that tolerates null values.
     */
    static Map<String, String> textMessage(String role, String content) {
        Map<String, String> message = new LinkedHashMap<>(2);
        message.put("role", role == null ? AIMessage.USER : role);
        message.put("content", content == null ? "" : content);
        return message;
    }
}
