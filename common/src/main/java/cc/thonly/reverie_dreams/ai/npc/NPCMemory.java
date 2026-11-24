package cc.thonly.reverie_dreams.ai.npc;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Player statements and server-observed events are stored separately and never promoted to instructions.
 */
public final class NPCMemory {
    private static final Logger LOGGER = LoggerFactory.getLogger(NPCMemory.class);
    private final Map<UUID, JsonObject> players = new LinkedHashMap<>();
    private JsonObject retainedRecords = new JsonObject();
    private String unreadableData;

    private void requireWritable(UUID id) {
        if (unreadableData != null || retainedRecords.has(id.toString())) {
            throw new IllegalStateException("NPC memory data is unreadable and preserved; repair the stored record before changing it");
        }
    }

    private JsonObject player(UUID id) {
        var data = players.get(id);
        if (data != null)
            return data;
        if (players.size() >= 32)
            players.remove(players.keySet().iterator().next());
        data = new JsonObject();
        data.add("statements", new JsonObject());
        data.add("game_events", new JsonArray());
        players.put(id, data);
        return data;
    }

    public void remember(UUID id, String key, String text, long time) {
        requireWritable(id);
        if (key == null || !key.matches("[a-zA-Z0-9_.-]{1,64}") || text == null || text.isBlank() || text.length() > 256) {
            throw new IllegalArgumentException("Memory needs a short key and 1..256 characters");
        }
        var statements = player(id).getAsJsonObject("statements");
        statements.remove(key);
        if (statements.size() >= 16)
            statements.remove(statements.keySet().iterator().next());
        JsonObject entry = new JsonObject();
        entry.addProperty("text", text);
        entry.addProperty("game_time", time);
        entry.addProperty("source", "player_statement");
        statements.add(key, entry);
    }

    public void forget(UUID id, String key) {
        requireWritable(id);
        if ("*".equals(key)) {
            players.remove(id);
            return;
        }
        var data = players.get(id);
        if (data != null)
            data.getAsJsonObject("statements").remove(key);
    }

    public void event(UUID id, String kind, String text, long time) {
        // Automatic observations must not overwrite quarantined data or interrupt gameplay.
        if (unreadableData != null || retainedRecords.has(id.toString()))
            return;
        JsonObject entry = new JsonObject();
        entry.addProperty("kind", kind);
        entry.addProperty("text", text.substring(0, Math.min(text.length(), 256)));
        entry.addProperty("game_time", time);
        entry.addProperty("source", "server_observed");
        var events = player(id).getAsJsonArray("game_events");
        events.add(entry);
        while (events.size() > 16)
            events.remove(0);
    }

    public JsonObject recall(UUID id) {
        var data = players.get(id);
        return data == null ? new JsonObject() : data.deepCopy();
    }

    public JsonObject save() {
        if (unreadableData != null)
            throw new IllegalStateException("Use saveEncoded to preserve unreadable NPC memory");
        JsonObject data = retainedRecords.deepCopy();
        players.forEach((id, value) -> data.add(id.toString(), value.deepCopy()));
        return data;
    }

    public String saveEncoded() {
        return unreadableData != null ? unreadableData : save().toString();
    }

    public void loadEncoded(String data) {
        try {
            load(JsonParser.parseString(data).getAsJsonObject());
        } catch (RuntimeException failure) {
            players.clear();
            retainedRecords = new JsonObject();
            unreadableData = data;
            LOGGER.error("NPC memory root is unreadable; preserving the original field without activation", failure);
        }
    }

    public void load(JsonObject data) {
        players.clear();
        retainedRecords = data.deepCopy();
        unreadableData = null;
        for (var player : data.entrySet()) {
            if (players.size() >= 32)
                break;
            try {
                UUID id = UUID.fromString(player.getKey());
                var object = player.getValue().getAsJsonObject();
                // Decode into a separate instance so a bad field cannot leave half a player's memory active.
                NPCMemory decoded = new NPCMemory();
                decoded.player(id);
                if (object.has("statements"))
                    for (var statement : object.getAsJsonObject("statements").entrySet()) {
                        var entry = statement.getValue().getAsJsonObject();
                        decoded.remember(id, statement.getKey(), entry.get("text").getAsString(), entry.get("game_time").getAsLong());
                    }
                if (object.has("game_events"))
                    for (var value : object.getAsJsonArray("game_events")) {
                        var entry = value.getAsJsonObject();
                        decoded.event(id, entry.get("kind").getAsString(), entry.get("text").getAsString(), entry.get("game_time").getAsLong());
                    }
                players.put(id, decoded.players.get(id));
                retainedRecords.remove(player.getKey());
            } catch (RuntimeException failure) {
                LOGGER.error("NPC memory record {} is unreadable; preserving it without activation", player.getKey(), failure);
            }
        }
    }
}
