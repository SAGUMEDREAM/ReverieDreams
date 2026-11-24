package cc.thonly.reverie_dreams.data.danmaku.spellcard;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LegacySpellcardDefinitionTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void originalFrameEnvelopeStillRoundTrips() {
        var oldPayload = JsonParser.parseString("{\"frames\":[[]]}");
        var data = LegacySpellcardDefinition.CODEC.parse(JsonOps.INSTANCE, oldPayload).getOrThrow();
        assertEquals(oldPayload, LegacySpellcardDefinition.CODEC.encodeStart(JsonOps.INSTANCE, data).getOrThrow());
    }

    @Test
    void castsCannotChangeTheDefinitionOrEachOther() {
        // No bullet type is needed to verify frame copying and timing in this test.
        var source = new SpellCardFrameConfig(null).withTickDelay(6).withTickDuration(20).withDensity(96);
        var data = new LegacySpellcardDefinition(List.of(List.of(source)));
        source.withDensity(4);
        var firstCast = data.copyFrames();
        firstCast.getFirst().getFirst().withDensity(1);
        firstCast.clear();
        assertEquals(96, data.copyFrames().getFirst().getFirst().getDensity());
        assertEquals(26, data.durationTicks());
    }
}
