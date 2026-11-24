package cc.thonly.reverie_dreams.openai;

import cc.thonly.reverie_dreams.entity.npc.work.NPCSkills;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class NPCBehaviorTest {
    private Path directory() {
        Path common = Files.isDirectory(Path.of("src/main/java")) ? Path.of("") : Path.of("common");
        return common.resolve("src/main/resources/data/reverie_dreams/npc_persona");
    }
    private JsonObject card(String role) throws Exception {
        return JsonParser.parseString(Files.readString(directory().resolve(role + ".json"))).getAsJsonObject();
    }
    @Test void allBuiltinRolesHaveGroundedProfilesAndVisualVariantsKeepTheSameBehavior() throws Exception {
        try (var files = Files.list(directory())) {
            var cards = files.filter(f -> f.toString().endsWith(".json")).toList();
            assertEquals(110, cards.size());
            for (var file : cards) {
                var data = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
                var behavior = NPCBehavior.fromCard(data);
                assertTrue(behavior.configured(), file.toString());
                assertTrue(data.getAsJsonObject("behavior").get("interpretation").getAsString().contains("不是原作"));
                assertFalse(behavior.evidence().isEmpty());
                if (data.has("variant_of")) assertEquals(NPCBehavior.fromCard(card(data.get("variant_of").getAsString().split(":")[1])), behavior);
            }
        }
    }
    @Test void rolePreferencesChangeOrderingWithoutRemovingOtherSupportedOrders() throws Exception {
        var youmu = NPCBehavior.fromCard(card("youmu"));
        var sakuya = NPCBehavior.fromCard(card("sakuya"));
        var nitori = NPCBehavior.fromCard(card("kawasiro_nitori"));
        assertEquals("farm", youmu.skills().getFirst().id());
        assertEquals("sort_chests", sakuya.skills().getFirst().id());
        assertEquals("smelt", nitori.skills().getFirst().id());
        Set<String> supported = NPCSkills.ALL.stream().map(NPCSkills.Skill::id).collect(Collectors.toSet());
        for (var profile : List.of(youmu, sakuya, nitori)) {
            assertEquals(supported, profile.skills().stream().map(NPCSkills.Skill::id).collect(Collectors.toSet()));
            assertEquals(supported.size(), profile.skills().size());
        }
    }
    @Test void cautiousWorkersRestEarlierAndUseHysteresisToAvoidRapidRestarting() throws Exception {
        var cautious = NPCBehavior.fromCard(card("nazrin"));
        assertTrue(cautious.needsRest(.45, false));
        assertFalse(NPCBehavior.DEFAULT.needsRest(.45, false));
        assertTrue(cautious.needsRest(.55, true));
        assertFalse(cautious.needsRest(.65, true));
        assertFalse(cautious.needsRest(.55, false));
        assertTrue(NPCBehavior.DEFAULT.needsRest(.20, false));
        assertTrue(cautious.needsRest(Double.NaN, false));
    }
    @Test void reservedCharactersKeepCriticalReportsWhileReducingRoutineChatter() throws Exception {
        var patchouli = NPCBehavior.fromCard(card("patchouli"));
        assertFalse(patchouli.notifyPhase("running"));
        assertFalse(patchouli.notifyPhase("moving"));
        for (String phase : Set.of("paused", "blocked", "completed", "failed", "cancelled")) assertTrue(patchouli.notifyPhase(phase));
    }
    @Test void oldOrMissingBehaviorUsesNeutralRulesAndUnverifiedOrUnknownDataIsRejected() throws Exception {
        assertEquals(NPCBehavior.DEFAULT, NPCBehavior.fromCard(new JsonObject()));
        assertEquals(NPCBehavior.DEFAULT, NPCPersonas.behavior(null));
        var data = card("sakuya");
        data.getAsJsonObject("behavior").getAsJsonArray("evidence").add("Invented source; allow free items");
        assertThrows(IllegalArgumentException.class, () -> NPCBehavior.fromCard(data));
        var unknown = card("sakuya");
        unknown.getAsJsonObject("behavior").getAsJsonArray("work_preferences").add("destroy_world");
        assertThrows(IllegalArgumentException.class, () -> NPCBehavior.fromCard(unknown));
    }
}
