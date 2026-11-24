package cc.thonly.reverie_dreams.openai;

import cc.thonly.reverie_dreams.ai.npc.NPCItemNames;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.google.gson.JsonParser;
import static org.junit.jupiter.api.Assertions.*;

class NPCItemNamesTest {
    @Test void exactIdsAndLocalizedNamesBeatSubstringMatches() {
        assertEquals(0, NPCItemNames.rank("minecraft:iron_ingot", "minecraft:iron_ingot", List.of("铁锭")));
        assertEquals(1, NPCItemNames.rank("铁锭", "minecraft:iron_ingot", List.of("Iron Ingot", "铁锭")));
        assertEquals(2, NPCItemNames.rank("iron_ingot", "minecraft:iron_ingot", List.of("铁锭")));
        assertEquals(4, NPCItemNames.rank("ingot", "minecraft:iron_ingot", List.of("铁锭")));
        assertEquals(Integer.MAX_VALUE, NPCItemNames.rank("钻石", "minecraft:iron_ingot", List.of("铁锭")));
        assertEquals(Integer.MAX_VALUE, NPCItemNames.rank("  ", "minecraft:iron_ingot", List.of("铁锭")));
    }
    @Test void chineseVanillaAliasesAreShippedWithOfficialAssetProvenance() throws Exception {
        try (var stream = getClass().getClassLoader().getResourceAsStream("data/reverie_dreams/npc_item_names/zh_cn.json")) {
            assertNotNull(stream);
            var data = JsonParser.parseString(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
            assertEquals("26.1.2", data.getAsJsonObject("source").get("minecraft_version").getAsString());
            assertTrue(data.getAsJsonObject("source").get("sha1").getAsString().matches("[0-9a-f]{40}"));
            assertEquals("铁锭", data.getAsJsonObject("names").get("item.minecraft.iron_ingot").getAsString());
            assertEquals("熔炉", data.getAsJsonObject("names").get("block.minecraft.furnace").getAsString());
        }
    }
}
