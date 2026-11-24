package cc.thonly.reverie_dreams.openai;

import java.util.UUID;

import cc.thonly.reverie_dreams.ai.npc.NPCMemory;
import org.junit.jupiter.api.Test;
import com.google.gson.JsonParser;
import static org.junit.jupiter.api.Assertions.*;

class NPCMemoryTest {
    @Test void unreadableRootSurvivesAutomaticEventsAndSavingVerbatim() {
        var memory = new NPCMemory();
        String damaged = "{broken memory";
        UUID id = UUID.randomUUID();
        memory.loadEncoded(damaged);
        memory.event(id, "task", "New automatic event", 1);
        assertEquals(damaged, memory.saveEncoded());
        assertThrows(IllegalStateException.class, () -> memory.remember(id, "name", "Alex", 2));
        assertThrows(IllegalStateException.class, () -> memory.forget(id, "*"));
        memory.loadEncoded("{}");
        assertDoesNotThrow(() -> memory.remember(id, "name", "Alex", 3));
    }

    @Test void brokenPlayerRecordIsRetainedWithoutLosingOtherPlayers() {
        UUID good = UUID.randomUUID(), bad = UUID.randomUUID();
        var initial = new NPCMemory();
        initial.remember(good, "name", "Alex", 1);
        var data = initial.save();
        var broken = JsonParser.parseString("{\"statements\":{\"name\":{\"text\":\"partial\"}}}");
        data.add(bad.toString(), broken);
        var loaded = new NPCMemory();
        loaded.load(data);
        loaded.remember(good, "drink", "tea", 2);
        loaded.event(bad, "task", "Must not replace damaged record", 3);
        assertEquals(broken, loaded.save().get(bad.toString()));
        assertEquals(0, loaded.recall(bad).size());
        assertTrue(loaded.recall(good).getAsJsonObject("statements").has("drink"));
        assertThrows(IllegalStateException.class, () -> loaded.remember(bad, "name", "Replacement", 4));
    }

    @Test void isolatesPlayersAndKeepsStatementsSeparateFromVerifiedEvents() {
        var memory = new NPCMemory();
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        memory.remember(a, "favorite", "I like tea", 20);
        memory.event(a, "task_completed", "Collected wheat", 30);
        assertTrue(memory.recall(b).entrySet().isEmpty());
        var data = memory.recall(a);
        assertEquals("player_statement", data.getAsJsonObject("statements").getAsJsonObject("favorite").get("source").getAsString());
        assertEquals("server_observed", data.getAsJsonArray("game_events").get(0).getAsJsonObject().get("source").getAsString());
        data.getAsJsonObject("statements").remove("favorite");
        assertTrue(memory.recall(a).getAsJsonObject("statements").has("favorite"));
    }

    @Test void savesLoadsAndForgetsWithoutLosingOtherMemories() {
        var memory = new NPCMemory();
        UUID id = UUID.randomUUID();
        memory.remember(id, "name", "Alex", 1);
        memory.event(id, "task", "Verified arrival", 2);
        var loaded = new NPCMemory();
        loaded.load(memory.save());
        assertEquals(memory.recall(id), loaded.recall(id));
        loaded.forget(id, "name");
        assertFalse(loaded.recall(id).getAsJsonObject("statements").has("name"));
        assertEquals(1, loaded.recall(id).getAsJsonArray("game_events").size());
    }

    @Test void boundsPersistentMemoryAndRejectsOversizedStatements() {
        var memory = new NPCMemory();
        UUID id = UUID.randomUUID();
        for (int i=0; i<30; i++) { memory.remember(id, "k"+i, "text", i); memory.event(id, "task", "event", i); }
        assertEquals(16, memory.recall(id).getAsJsonObject("statements").size());
        assertEquals(16, memory.recall(id).getAsJsonArray("game_events").size());
        assertThrows(IllegalArgumentException.class, () -> memory.remember(id, "key", "x".repeat(257), 0));
    }

    @Test void forgetAllRemovesOnlyTheCurrentPlayersRecords() {
        var memory = new NPCMemory();
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        memory.remember(a, "name", "Alex", 1);
        memory.event(a, "task", "Arrival", 2);
        memory.remember(b, "name", "Steve", 3);
        memory.forget(a, "*");
        assertEquals(0, memory.recall(a).size());
        assertTrue(memory.recall(b).getAsJsonObject("statements").has("name"));
    }
}
