package cc.thonly.reverie_dreams.openai;

import cc.thonly.reverie_dreams.ai.AIUsageBudget;
import cc.thonly.reverie_dreams.ai.AIUsageSavedData;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonPrimitive;
import net.minecraft.nbt.*;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import java.io.ByteArrayOutputStream;
import static org.junit.jupiter.api.Assertions.*;

class AIUsageSavedDataTest {
    @Test void largeServerLedgerUsesStructuredNbtAndCanActuallyBeWritten() throws Exception {
        var state = new AIUsageSavedData();
        var limits = new AIUsageBudget.Limits(10000, 2, 0, 10000000, 10000);
        for (int i=0; i<1000; i++) assertNotNull(state.budget().reserve("2026-11-05", new UUID(0,i), false, 10, limits));
        var encoded = AIUsageSavedData.CODEC.encodeStart(NbtOps.INSTANCE,state).getOrThrow();
        assertInstanceOf(CompoundTag.class,encoded);
        var root = new CompoundTag(); root.put("ledger",encoded);
        var bytes = new ByteArrayOutputStream();
        NbtIo.writeCompressed(root,bytes);
        assertTrue(bytes.size()>0);
        var restored = AIUsageSavedData.CODEC.parse(NbtOps.INSTANCE,encoded).getOrThrow();
        assertEquals(state.budget().save(),restored.budget().save());
    }
    @Test void initialStringLedgerMigratesWithoutResettingUsage() {
        var state = new AIUsageSavedData();
        state.budget().reserve("2026-11-05",UUID.randomUUID(),false,25,new AIUsageBudget.Limits(10,10,0,1000,1000));
        var loaded = AIUsageSavedData.CODEC.parse(JsonOps.INSTANCE,new JsonPrimitive(state.budget().save().toString())).getOrThrow();
        assertEquals(state.budget().save(),loaded.budget().save());
        assertTrue(AIUsageSavedData.CODEC.encodeStart(JsonOps.INSTANCE,loaded).getOrThrow().isJsonObject());
    }
}
