package cc.thonly.reverie_dreams.openai;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/** Offline checks: coverage and provenance structure, not a substitute for reviewing canon. */
class NPCPersonaResourcesTest {
    private static final String PREFIX = "data/reverie_dreams/npc_persona/";
    private static final Map<String, String> VARIANTS = Map.of(
            "cyan_reimu", "reimu", "white_komeiji_koishi", "komeiji_koishi");

    private static Path commonDirectory() {
        Path current = Path.of("").toAbsolutePath();
        return Files.isDirectory(current.resolve("src/main/java")) ? current : current.resolve("common");
    }

    private static String source(String relative) throws IOException {
        return Files.readString(commonDirectory().resolve(relative), StandardCharsets.UTF_8);
    }

    private static JsonObject packagedJson(String path) throws IOException {
        try (var stream = NPCPersonaResourcesTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(stream, "Missing packaged resource: " + path);
            return JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    private static Set<String> registeredRoles() throws IOException {
        var matcher = Pattern.compile("registerRole\\(new NPCRoleType\\(ReverieDreams\\.id\\(\"([^\"]+)\"\\)")
                .matcher(source("src/main/java/cc/thonly/reverie_dreams/registry/content/NPCRoleTypes.java"));
        Set<String> roles = new HashSet<>();
        while (matcher.find()) assertTrue(roles.add(matcher.group(1)), "Duplicate registered role");
        assertFalse(roles.isEmpty(), "Registry parser must find actual registrations");
        return roles;
    }

    private static Map<String, JsonObject> cards() throws IOException {
        Map<String, JsonObject> result = new TreeMap<>();
        try (var files = Files.list(commonDirectory().resolve("src/main/resources/" + PREFIX))) {
            for (var file : files.filter(p -> p.toString().endsWith(".json")).toList()) {
                String id = file.getFileName().toString().replaceFirst("\\.json$", "");
                result.put(id, packagedJson(PREFIX + id + ".json"));
            }
        }
        return result;
    }

    @Test void everyRegisteredRoleHasExactlyOnePackagedPersona() throws Exception {
        assertEquals(registeredRoles(), cards().keySet(), "Missing or obsolete built-in persona IDs");
    }

    @Test void runtimePromptsContainReviewedFactsGuidanceAndKnowledgeBoundaries() throws Exception {
        Set<String> officialNames = new HashSet<>();
        for (var entry : cards().entrySet()) {
            String id = entry.getKey();
            var card = entry.getValue();
            assertEquals(1, card.get("schema_version").getAsInt(), id);
            assertEquals("reverie_dreams:" + id, card.get("role").getAsString(), id);
            String persona = card.get("persona").getAsString();
            assertTrue(!persona.isBlank() && persona.length() <= 4096, "Loader length limit: " + id);
            for (String field : Set.of("character_name", "portrayal_guidance", "knowledge_boundary")) {
                String value = card.get(field).getAsString();
                assertFalse(value.isBlank(), id + "/" + field);
                assertTrue(persona.contains(value), "Runtime prompt omits " + id + "/" + field);
            }
            var facts = card.getAsJsonArray("canon_facts");
            assertFalse(facts.isEmpty(), id);
            for (var fact : facts) {
                assertFalse(fact.getAsString().isBlank(), id);
                assertTrue(persona.contains(fact.getAsString()), "Runtime prompt omits facts for " + id);
            }
            assertEquals(VARIANTS.containsKey(id) ? "mod_visual_variant" : "official_character",
                    card.get("canon_status").getAsString(), id);
            if (!VARIANTS.containsKey(id)) assertTrue(officialNames.add(card.get("character_name").getAsString()),
                    "Duplicate official identity: " + id);
        }
    }

    @Test void everyPersonaCitesAReviewableDatedWikiRevision() throws Exception {
        for (var entry : cards().entrySet()) {
            var sources = entry.getValue().getAsJsonArray("sources");
            assertFalse(sources.isEmpty(), entry.getKey());
            for (var element : sources) {
                var citation = element.getAsJsonObject();
                URI uri = URI.create(citation.get("url").getAsString());
                assertEquals("https", uri.getScheme());
                assertEquals("thwiki.cc", uri.getHost());
                assertEquals("/" + citation.get("title").getAsString(), uri.getPath());
                assertTrue(citation.get("revision_id").getAsLong() > 0, entry.getKey());
                assertFalse(citation.getAsJsonArray("sections").isEmpty(), entry.getKey());
                assertNotNull(LocalDate.parse(citation.get("verified_on").getAsString()));
                assertEquals("mediawiki_api", citation.get("access_method").getAsString());
                assertEquals("thbwiki_cited_official_materials", citation.get("source_kind").getAsString());
            }
        }
    }

    @Test void visualVariantsInheritCanonWithoutInventingSeparateCharacters() throws Exception {
        var all = cards();
        for (var entry : VARIANTS.entrySet()) {
            var variant = all.get(entry.getKey());
            var original = all.get(entry.getValue());
            assertEquals(original.get("role"), variant.get("variant_of"));
            for (String field : Set.of("character_name", "canon_facts", "portrayal_guidance", "knowledge_boundary", "sources")) {
                assertEquals(original.get(field), variant.get(field), entry.getKey() + "/" + field);
            }
            assertTrue(variant.get("persona").getAsString().startsWith(original.get("persona").getAsString()));
            assertFalse(variant.get("variant_note").getAsString().isBlank());
        }
    }

    @Test void satonoAndMaiDisplayNamesMatchTheirPersonasAndDatagen() throws Exception {
        Map<String, String[]> expected = Map.of(
                "zh_cn", new String[]{"尔子田里乃", "丁礼田舞", "刷怪蛋"},
                "zh_tw", new String[]{"爾子田裡乃", "丁禮田舞", "刷怪蛋"},
                "ja_jp", new String[]{"爾子田里乃", "丁礼田舞", "のスポーンエッグ"});
        for (var entry : expected.entrySet()) {
            // Generated resources are consumed by loader modules, not common's test runtime.
            var language = entry.getKey().equals("zh_cn")
                    ? JsonParser.parseString(source("src/generated/resources/assets/reverie_dreams/lang/zh_cn.json")).getAsJsonObject()
                    : packagedJson("assets/reverie_dreams/lang/" + entry.getKey() + ".json");
            String[] names = entry.getValue();
            String[] roles = {"nishida_satono", "teireida_mai"};
            for (int i = 0; i < roles.length; i++) {
                assertEquals(names[i], language.get("entity.reverie_dreams." + roles[i]).getAsString());
                assertEquals(names[i] + names[2], language.get("item.reverie_dreams." + roles[i] + "_spawn_egg").getAsString());
            }
        }
        var all = cards();
        assertEquals("尔子田里乃", all.get("nishida_satono").get("character_name").getAsString());
        assertEquals("丁礼田舞", all.get("teireida_mai").get("character_name").getAsString());

    }
}
