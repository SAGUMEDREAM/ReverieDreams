package cc.thonly.reverie_dreams.data.danmaku.stg.runtime;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuDefaults;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpells;
import cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

class SpellTimingTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void windGodTimerUsesOriginalWallClockAtTwentyTps() {
        assertEquals(900, SpellTiming.framesToTicks(2700));
        assertEquals(1201, SpellTiming.framesToTicks(3602));
    }

    @Test
    void introDoesNotRepeatAndLoopDoesNotExtendDeadline() {
        var timing = new SpellTiming(440, 40, 71);
        assertEquals(39, timing.attackTick(39));
        assertEquals(40, timing.attackTick(40));
        assertEquals(110, timing.attackTick(110));
        assertEquals(40, timing.attackTick(111));
        assertFalse(timing.hasExpired(439));
        assertTrue(timing.hasExpired(440));
    }

    @Test
    void runtimeDoesNotEndInAQuietWindowAndStopsAtDeadline() {
        var timing = new SpellTiming(3, 0, 0);
        // No world is required to advance a quiet timeline with no owned entities.
        var runtime = new DanmakuPatternRuntime(new DanmakuPattern(List.of()).withTiming(timing),
                null, null, Vec3.ZERO);
        for (int t = 0; t < 3; t++) {
            runtime.tick();
            assertEquals(t, runtime.getTick());
            assertFalse(runtime.isDone());
        }
        runtime.tick();
        assertTrue(runtime.isDone());
    }

    @Test
    void registeredCardsUseOriginalDeadlinesAndLocalizedItemNames() {
        var patterns = new LinkedHashMap<Identifier, DanmakuPattern>();
        TouhouSpells.register(patterns::put);
        String namespace = "reverie_dreams:";
        Identifier moonlight = Identifier.parse(namespace + "touhou/th06/s01_000_hard");
        Identifier pyramid = Identifier.parse(namespace + "touhou/th08/s03_001_hard");
        Identifier autumn = Identifier.parse(namespace + "touhou/th10/s01_003_normal");
        assertEquals(440, patterns.get(moonlight).timing().duration());
        assertFalse(patterns.get(moonlight).lasers().isEmpty());
        assertEquals(760, patterns.get(pyramid).timing().duration());
        assertEquals(9, patterns.get(pyramid).familiars().size());
        assertEquals(900, patterns.get(autumn).timing().duration());
        Identifier spring = Identifier.parse(namespace + "touhou/th10/s07_097_extra");
        assertEquals(1400, patterns.get(spring).timing().duration());
        assertEquals(30, patterns.get(spring).timing().loopStart());
        var item = assertInstanceOf(TranslatableContents.class, DanmakuDefaults.itemName(moonlight).getContents());
        assertEquals("item.reverie_dreams.spellcard.named", item.getKey());
        assertEquals(2, item.getArgs().length);
        var name = assertInstanceOf(Component.class, item.getArgs()[0]);
        assertEquals(DanmakuDefaults.translationKey(moonlight),
                assertInstanceOf(TranslatableContents.class, name.getContents()).getKey());
    }

    @Test
    void keineExtraKeepsTheDifferenceBetweenSequentialAndSimultaneousSources() {
        var patterns = new LinkedHashMap<Identifier, DanmakuPattern>();
        TouhouSpells.register(patterns::put);
        var oldHistory = patterns.get(Identifier.parse("reverie_dreams:touhou/th08/s07_001_extra"));
        var nextHistory = patterns.get(Identifier.parse("reverie_dreams:touhou/th08/s07_003_extra"));
        assertEquals(1200, oldHistory.timing().duration());
        assertEquals(1200, nextHistory.timing().duration());
        int firstWave = SpellTiming.framesToTicks(110) + SpellTiming.framesToTicks(60);
        assertEquals(4, oldHistory.emitters().stream().filter(emitter -> emitter.delay() == firstWave).count());
        assertEquals(8, nextHistory.emitters().stream().filter(emitter -> emitter.delay() == firstWave).count());
    }

    @Test
    void meilingPrismaticCardsKeepTheirOwnTimelineAndDifficulty() {
        var patterns = new LinkedHashMap<Identifier, DanmakuPattern>();
        TouhouSpells.register(patterns::put);
        String prefix = "reverie_dreams:touhou/th06/";
        var rain = patterns.get(Identifier.parse(prefix + "s03_011_normal"));
        var dance = patterns.get(Identifier.parse(prefix + "s03_012_hard"));
        var normalTyphoon = patterns.get(Identifier.parse(prefix + "s03_013_normal"));
        var hardTyphoon = patterns.get(Identifier.parse(prefix + "s03_013_hard"));
        for (var card : List.of(rain, dance, normalTyphoon, hardTyphoon)) {
            assertEquals(720, card.timing().duration());
        }
        assertEquals(4, shotsAt(rain, 40));
        assertEquals(0, shotsAt(rain, 67)); // Last shot of the first burst was rounded to tick 66.
        assertEquals(4, shotsAt(rain, 74)); // 120 + 80 + 20 ECL frames, rounded to tick 74.
        assertEquals(0, shotsAt(dance, 54));
        assertEquals(4, shotsAt(dance, 60));
        assertEquals(2, shotsAt(normalTyphoon, 60));
        assertEquals(4, shotsAt(normalTyphoon, 61));
        assertEquals(6, shotsAt(hardTyphoon, 61));
        assertEquals(4, shotsAt(normalTyphoon, 401));
        assertEquals(4, shotsAt(normalTyphoon, 403));
        assertEquals(5, shotsAt(normalTyphoon, 405)); // The wave after the script updates the count.
        assertEquals(7, shotsAt(hardTyphoon, 405));
    }

    private static int shotsAt(DanmakuPattern pattern, int tick) {
        return pattern.emitters().stream()
                .filter(e -> tick >= e.delay() && tick <= e.delay() + e.duration()
                        && (tick - e.delay()) % e.interval() == 0)
                .mapToInt(e -> e.shape().emit(e.pitch(), e.yaw()).size())
                .sum();
    }

    @Test
    void patternKeepsTimingWhenSerialized() {
        var pattern = new DanmakuPattern(List.of()).withTiming(new SpellTiming(900, 50, 120));
        var json = DanmakuPattern.CODEC.encodeStart(JsonOps.INSTANCE, pattern).getOrThrow();
        assertEquals(pattern, DanmakuPattern.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
    }
}
