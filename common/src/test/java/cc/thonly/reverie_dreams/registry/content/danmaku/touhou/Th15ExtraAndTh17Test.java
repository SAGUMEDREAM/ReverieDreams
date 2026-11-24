package cc.thonly.reverie_dreams.registry.content.danmaku.touhou;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import net.minecraft.SharedConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class Th15ExtraAndTh17Test {
    private static final Map<Identifier, DanmakuPattern> PATTERNS = new LinkedHashMap<>();

    @BeforeAll static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        TouhouSpells.register(PATTERNS::put);
    }

    @Test void th15ExtraRetainsAllSpellExPhases() {
        var ids = ids("th15/s07_");
        assertEquals(IntStream.rangeClosed(106, 118).mapToObj(n -> path("th15", 7, n, "extra")).toList(), ids);
        assertEquals(List.of(1000, 1000, 1000, 1400, 1400, 1400, 1600, 1600, 1600, 1600, 1600, 1800, 2400),
                ids.stream().map(id -> PATTERNS.get(id).timing().duration()).toList());
        assertEquals("entity.reverie_dreams.doremy_sweet", TouhouSpells.characterKeyOf(ids.get(2)));
        assertEquals("entity.reverie_dreams.hecatia_lapislazuli", TouhouSpells.characterKeyOf(ids.get(3)));
        assertEquals("entity.reverie_dreams.junko", TouhouSpells.characterKeyOf(ids.get(10)));
        // Nine rainbow scale lanes, plus the slightly different red of the medium orb rings.
        assertEquals(10, colors(PATTERNS.get(ids.get(1))).size());
        assertEquals(Set.of(0xFF6CE8, 0xFF4747, 0x597CFF), colors(PATTERNS.get(ids.getLast())));
    }

    @Test void th17FollowsOriginalMainAndExtraOrder() {
        var expected = new ArrayList<Identifier>();
        int[][] original = {{0,4}, {8,12,16}, {20,24,28}, {32,36,40}, {44,48,52,56}, {60,64,68,72,76,80,84}};
        for (int stage = 0; stage < original.length; stage++) {
            for (int slot : original[stage]) {
                expected.add(path("th17", stage + 1, slot, "normal"));
                expected.add(path("th17", stage + 1, slot, "hard"));
            }
        }
        IntStream.rangeClosed(88, 100).mapToObj(n -> path("th17", 7, n, "extra")).forEach(expected::add);
        assertEquals(expected, ids("th17/"));
        assertEquals("entity.reverie_dreams.niwatari_kutaka", TouhouSpells.characterKeyOf(path("th17", 7, 90, "extra")));
        assertEquals("entity.reverie_dreams.kurokoma_saki", TouhouSpells.characterKeyOf(path("th17", 7, 91, "extra")));
    }

    @Test void originalSpritesAndMovingBeamReplaceTheTemporaryTypes() {
        var mirror = PATTERNS.get(path("th15", 7, 111, "extra")).emitters().getFirst().bullet();
        var reflection = mirror.motion().segments().getFirst();
        assertTrue(reflection.is(MotionSegment.BOUNCE));
        assertNotNull(reflection.beam());
        assertEquals(0x597CFF, reflection.beam().color());
        assertEquals(0.35f, reflection.beam().speed());
        assertEquals(96f / 30f, reflection.beam().length());
        assertEquals(16f / 30f, reflection.beam().width());
        assertTrue(reflection.beam().consumeSource());

        var enclosure = PATTERNS.get(path("th15", 7, 112, "extra"));
        assertEquals(Set.of("oval", "light_orb"), enclosure.emitters().stream()
                .map(e -> e.bullet().type().getPath()).collect(Collectors.toSet()));
        var archers = PATTERNS.get(path("th17", 5, 44, "normal"));
        assertTrue(archers.emitters().stream().allMatch(e -> e.bullet().type().getPath().equals("arrow")));
        var lunaticImpact = PATTERNS.get(path("th15", 7, 115, "extra"));
        var moon = lunaticImpact.emitters().getFirst().bullet();
        assertEquals("lunatic_moon", moon.type().getPath());
        assertEquals("star", moon.motion().segments().getFirst().payload().type().getPath());
    }

    @Test void th17CarriesDifficultyAndPhaseColorsThroughThePattern() {
        var normal = PATTERNS.get(path("th17", 1, 0, "normal"));
        var hard = PATTERNS.get(path("th17", 1, 0, "hard"));
        assertEquals(Set.of(0xFF5356), colors(normal));
        assertEquals(Set.of(0x50D956), colors(hard));
        var offspring = hard.emitters().getFirst().bullet().motion().segments().stream()
                .filter(segment -> segment.type().equals(MotionSegment.SPLIT)).findFirst().orElseThrow();
        assertEquals(0x47E9FF, offspring.payload().color());

        var normalLines = PATTERNS.get(path("th17", 6, 68, "normal"));
        var hardLines = PATTERNS.get(path("th17", 6, 68, "hard"));
        assertEquals(0xC3A22B, normalLines.lasers().getFirst().color());
        assertEquals(0x597CFF, hardLines.lasers().getFirst().color());

        var finale = PATTERNS.get(path("th17", 7, 100, "extra"));
        assertEquals(2400, finale.timing().duration());
        var atEachGate = new LinkedHashMap<Integer, Integer>();
        for (DanmakuEmitter e : finale.emitters()) atEachGate.putIfAbsent(e.delay(), e.bullet().color());
        assertEquals(List.of(0xFF6CE8, 0x597CFF, 0xFF4747, 0xC3A22B, 0xFF6CE8), List.copyOf(atEachGate.values()));
    }

    private static List<Identifier> ids(String prefix) {
        return PATTERNS.keySet().stream().filter(id -> id.getPath().startsWith("touhou/" + prefix)).toList();
    }

    private static Identifier path(String game, int stage, int number, String difficulty) {
        return Identifier.fromNamespaceAndPath("reverie_dreams",
                String.format(Locale.ROOT, "touhou/%s/s%02d_%03d_%s", game, stage, number, difficulty));
    }

    private static Set<Integer> colors(DanmakuPattern pattern) {
        return pattern.emitters().stream().map(e -> e.bullet().color()).collect(Collectors.toSet());
    }
}
