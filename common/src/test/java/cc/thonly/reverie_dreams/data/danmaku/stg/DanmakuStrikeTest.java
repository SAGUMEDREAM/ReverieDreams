package cc.thonly.reverie_dreams.data.danmaku.stg;

import cc.thonly.reverie_dreams.registry.content.danmaku.soku.SokuMoves;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DanmakuStrikeTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void twoFrameAttackBetweenTickBoundariesIsNotLost() {
        var strike = strike(7, 9);
        assertFalse(strike.activeAt(-1));
        assertFalse(strike.activeAt(1));
        assertTrue(strike.activeAt(2));
        assertFalse(strike.activeAt(3));
        assertFalse(strike.activeAt(Integer.MAX_VALUE));
        assertTrue(strike(8, 10).activeAt(2));
        assertTrue(strike(8, 10).activeAt(3));
        assertFalse(strike(8, 10).activeAt(4));
    }

    @Test
    void volumeTurnsWithFacingAndScalesOnce() {
        var strike = strike(0, 3);
        var origin = new Vec3(7, 10, 13);
        assertTrue(strike.volume(origin, 0, 0, 1).intersects(new AABB(6.9, 9.9, 13.9, 7.1, 10.1, 14.1)));
        assertFalse(strike.volume(origin, 0, 90, 1).intersects(new AABB(6.9, 9.9, 13.9, 7.1, 10.1, 14.1)));
        assertTrue(strike.volume(origin, 0, 90, 1).intersects(new AABB(5.9, 9.9, 12.9, 6.1, 10.1, 13.1)));
        assertTrue(strike.volume(origin, -90, 0, 1).intersects(new AABB(6.9, 10.9, 12.9, 7.1, 11.1, 13.1)));
        assertTrue(strike.volume(origin, 0, 0, 2).intersects(new AABB(6.9, 9.9, 16.8, 7.1, 10.1, 16.9)));
        assertFalse(strike.volume(origin, 0, 0, 1).intersects(new AABB(6.9, 9.9, 16.8, 7.1, 10.1, 16.9)));
    }

    @Test
    void patternCodecKeepsOldDataAndAllBuildersPreserveStrikes() {
        var old = DanmakuPattern.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{\"emitters\":[]}")).getOrThrow();
        assertTrue(old.strikes().isEmpty());
        var pattern = old.withStrikes(List.of(strike(7, 9))).withTiming(new SpellTiming(6, 0, 0))
                .withFamiliars(List.of()).withLasers(List.of()).withOps(List.of());
        assertEquals(1, pattern.strikes().size());
        assertTrue(pattern.requiresOwnership());
        assertEquals(2, pattern.maxDuration());
        var json = DanmakuPattern.CODEC.encodeStart(JsonOps.INSTANCE, pattern).getOrThrow();
        assertEquals(pattern, DanmakuPattern.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
        assertThrows(IllegalArgumentException.class, () -> strike(9, 9));
    }

    @Test
    void reviewedReimuActionsUseFinalReplacementData() {
        for (String path : List.of("th105/reimu/5a", "th105/reimu/5aa", "th123/reimu/5a", "th123/reimu/5aa")) {
            var move = SokuMoves.find(Identifier.parse("reverie_dreams:soku/" + path));
            assertNotNull(move);
            var pattern = move.factory().get();
            assertEquals(6, pattern.timing().duration());
            assertEquals(0, pattern.timing().loopLength());
            assertEquals(2, pattern.strikes().size());
            for (var strike : pattern.strikes()) {
                assertEquals(2f, strike.damage());
                assertEquals(0, strike.hitGroup());
                assertEquals(move.action() == 300 ? 7 : 6, strike.fromFrame());
                assertEquals(2, strike.untilFrame() - strike.fromFrame());
            }
        }
    }

    private static DanmakuStrike strike(int from, int until) {
        return new DanmakuStrike(from, until, 0, 2, new Vec3(-0.4, -0.5, 0.5), new Vec3(0.4, 0.5, 2));
    }
}
