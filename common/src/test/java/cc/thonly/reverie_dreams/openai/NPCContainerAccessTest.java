package cc.thonly.reverie_dreams.openai;

import cc.thonly.reverie_dreams.ai.npc.NPCContainerAccess;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class NPCContainerAccessTest {
    @Test void unreadableRootRoundTripsVerbatimAndCanBeReloadedAfterRepair() {
        var access = new NPCContainerAccess();
        access.loadEncoded("{invalid authorization");
        assertEquals("{invalid authorization", access.saveEncoded());
        access.loadEncoded("{}");
        assertEquals("{}", access.saveEncoded());
    }

    @Test void badAuthorizationDoesNotDiscardValidOrOpaqueRecords() {
        UUID owner = UUID.randomUUID();
        String data = "{\"minecraft:overworld|1\":\"" + owner + "\",\"minecraft:overworld|2\":\"bad-uuid\"}";
        var access = new NPCContainerAccess();
        access.loadEncoded(data);
        assertEquals(JsonParser.parseString(data), JsonParser.parseString(access.saveEncoded()));
        var reloaded = new NPCContainerAccess();
        reloaded.loadEncoded(access.saveEncoded());
        assertEquals(JsonParser.parseString(data), JsonParser.parseString(reloaded.saveEncoded()));
    }
}
