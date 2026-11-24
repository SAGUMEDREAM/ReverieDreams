package cc.thonly.reverie_dreams.openai;

import cc.thonly.reverie_dreams.ai.npc.NPCWorldTools;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class NPCTaskEventToolsTest {
    @Test void roleAndWorkSuggestionToolsAreReadOnlyEvenDuringBackgroundAnalysis() {
        var names = NPCWorldTools.readOnlyDefinitions().stream().map(tool -> ((Map<?, ?>) tool.get("function")).get("name")).toList();
        assertTrue(names.contains("behavior_profile"));
        assertTrue(names.contains("suggest_work"));
        assertTrue(NPCWorldTools.executeReadOnly(null, null, "suggest_work", new JsonObject()).startsWith("Failed:"));
    }
    @Test void eventExecutionRejectsEveryMutatingToolBeforeWorldAccess() {
        for (String name : new String[]{"follow", "stay", "work", "move_to", "start_skill", "start_plan", "cancel_task",
                "authorize_container", "transfer_items", "equip_item", "remember", "forget_memory", "pause_task", "continue_task"}) {
            assertTrue(NPCWorldTools.executeReadOnly(null, null, name, new JsonObject()).startsWith("Denied:"), name);
        }
    }
}
