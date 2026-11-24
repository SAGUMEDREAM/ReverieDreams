package cc.thonly.reverie_dreams.openai;

import cc.thonly.reverie_dreams.ai.AIMessage;
import cc.thonly.reverie_dreams.entity.npc.container.ChatAIContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import java.util.*;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import static org.junit.jupiter.api.Assertions.*;

class ChatAIHistoryBoundsTest {
    @BeforeAll static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }
    @Test void evictsLeastRecentlyUsedPlayerInsteadOfGrowingForever() {
        var history = new ChatAIContainer();
        UUID oldest = UUID.randomUUID(), recent = UUID.randomUUID();
        history.getOrCreateHistory(oldest).add(new AIMessage("user", "old"));
        history.getOrCreateHistory(recent).add(new AIMessage("user", "keep"));
        for (int i=0; i<30; i++) history.getOrCreateHistory(UUID.randomUUID());
        assertEquals("keep", history.getHistory(recent).getFirst().getContent());
        history.getOrCreateHistory(UUID.randomUUID());
        assertTrue(history.getHistory(oldest).isEmpty());
        assertFalse(history.getHistory(recent).isEmpty());
        assertEquals(32, history.getHistories().size());
    }
    @Test void legacyLoadsBoundMessagesCharactersAndPlayersWhilePreservingSystemAndNewestTurn() {
        var legacy = new LinkedHashMap<UUID, List<AIMessage>>();
        for (int p=0; p<60; p++) {
            var messages = new ArrayList<AIMessage>();
            messages.add(new AIMessage("system", "persona"));
            for (int m=0; m<100; m++) messages.add(new AIMessage("user", "x".repeat(20000)));
            messages.add(new AIMessage("assistant", "latest"));
            legacy.put(UUID.randomUUID(), messages);
        }
        var bounded = new ChatAIContainer(legacy);
        assertTrue(bounded.getHistories().size() <= ChatAIContainer.MAX_PLAYERS);
        int total = 0;
        for (var messages : bounded.getHistories().values()) {
            assertEquals("persona", messages.getFirst().getContent());
            assertEquals("latest", messages.getLast().getContent());
            assertTrue(messages.size() <= ChatAIContainer.MAX_MESSAGES);
            int chars = messages.stream().mapToInt(m -> m.getContent().length()).sum();
            assertTrue(chars <= ChatAIContainer.MAX_HISTORY_CHARS); total += chars;
        }
        assertTrue(total <= ChatAIContainer.MAX_TOTAL_CHARS);
        assertEquals(102, legacy.values().iterator().next().size());
    }
}
