package cc.thonly.reverie_dreams.openai;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class NPCAgentResourcesTest {
    private JsonObject language(String code) throws Exception {
        if (code.equals("zh_cn")) {
            Path common = Files.isDirectory(Path.of("src/main/java")) ? Path.of("") : Path.of("common");
            return JsonParser.parseString(Files.readString(common.resolve("src/generated/resources/assets/reverie_dreams/lang/zh_cn.json"))).getAsJsonObject();
        }
        try (var stream = getClass().getClassLoader().getResourceAsStream("assets/reverie_dreams/lang/" + code + ".json")) {
            assertNotNull(stream);
            return JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
    @Test void taskPanelLanguagesHaveMatchingKeysAndFormattingArguments() throws Exception {
        var english = language("en_us");
        Set<String> keys = english.keySet().stream().filter(key -> key.startsWith("gui.reverie_dreams.agent.")).collect(Collectors.toSet());
        assertTrue(keys.size() >= 50);
        assertTrue(keys.contains("gui.reverie_dreams.agent.pause"));
        assertTrue(keys.contains("gui.reverie_dreams.agent.continue"));
        assertTrue(keys.contains("gui.reverie_dreams.agent.remaining"));
        for (String locale : Set.of("zh_cn", "zh_tw", "ja_jp")) {
            var language = language(locale);
            assertEquals(keys, language.keySet().stream().filter(key -> key.startsWith("gui.reverie_dreams.agent.")).collect(Collectors.toSet()), locale);
            for (String key : keys) {
                assertFalse(language.get(key).getAsString().isBlank(), locale + "/" + key);
                assertEquals(english.get(key).getAsString().split("%s", -1).length,
                        language.get(key).getAsString().split("%s", -1).length, locale + "/" + key);
            }
            for (String reason : Set.of("manually_paused", "waiting_for_offspring", "produced_items_unavailable")) {
                assertTrue(language.has("gui.reverie_dreams.task_reason." + reason), locale + "/" + reason);
            }
        }
    }
}
