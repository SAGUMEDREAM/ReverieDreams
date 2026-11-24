package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.server.player.FactionReputationComponent;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class YoukaiRabbitReputationTest {
    private FactionReputationComponent component() {
        return new FactionReputationComponent(2, 3, 4, 5, 6, 7, 8, 9, 0);
    }
    @Test void tradesOnlyChangeEternityAndClampAtItsMaximum() {
        var rep = this.component();
        rep.addHouseOfEternity(1); assertEquals(1, rep.getHouseOfEternity());
        rep.addHouseOfEternity(Integer.MAX_VALUE); assertEquals(200, rep.getHouseOfEternity());
        rep.addHouseOfEternity(Integer.MIN_VALUE); assertEquals(-100, rep.getHouseOfEternity());
        assertEquals(2, rep.getHuman()); assertEquals(3, rep.getFairy()); assertEquals(4, rep.getYokai());
        assertEquals(5, rep.getDeity()); assertEquals(6, rep.getOni()); assertEquals(7, rep.getImmortal());
        assertEquals(8, rep.getCelestial()); assertEquals(9, rep.getLunarian());
    }
    @Test void olderSavesWithoutEternityLoadAsZeroAndNewValueRoundTrips() {
        var encoded = FactionReputationComponent.CODEC.encodeStart(JsonOps.INSTANCE, this.component()).getOrThrow().getAsJsonObject();
        encoded.remove("house_of_eternity");
        var decoded = FactionReputationComponent.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(0, decoded.getHouseOfEternity());
        decoded.addHouseOfEternity(11);
        var saved = FactionReputationComponent.CODEC.encodeStart(JsonOps.INSTANCE, decoded).getOrThrow();
        assertEquals(11, FactionReputationComponent.CODEC.parse(JsonOps.INSTANCE, saved).getOrThrow().getHouseOfEternity());
    }
}
