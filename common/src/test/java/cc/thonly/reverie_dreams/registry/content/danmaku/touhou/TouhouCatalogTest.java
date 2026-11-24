package cc.thonly.reverie_dreams.registry.content.danmaku.touhou;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuDefaults;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TouhouCatalogTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void translationsAndPlayablePatternsShareTheirEncounterOrder() throws IOException {
        var chinese = chineseNames();
        for (String locale : List.of("en_us", "ja_jp", "zh_tw")) {
            String file = "/assets/reverie_dreams/lang/" + locale + ".json";
            try (var stream = getClass().getResourceAsStream(file)) {
                assertNotNull(stream, file);
                var names = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                assertEquals(chinese.keySet(), names.keySet().stream()
                        .filter(key -> key.startsWith("danmaku_pattern.reverie_dreams.touhou."))
                        .collect(Collectors.toSet()), locale);
                chinese.keySet().forEach(key -> assertFalse(names.get(key).getAsString().isBlank(), locale + ": " + key));
            }
        }
        var patterns = new LinkedHashMap<Identifier, DanmakuPattern>();
        TouhouSpells.register((id, pattern) -> assertNull(patterns.put(id, pattern), id.toString()));
        var playableKeys = patterns.keySet().stream().map(DanmakuDefaults::translationKey).toList();
        assertEquals(playableKeys, chinese.keySet().stream().filter(playableKeys::contains).toList());

        // TH08 has two route pairs. Their relative positions matter even while some cards are catalog only.
        var routes = chinese.keySet().stream().filter(key -> key.contains(".touhou.th08."))
                .map(key -> key.split("\\.")[4].split("_")[0]).distinct().toList();
        assertEquals(List.of("s01", "s02", "s03", "s04a", "s04b", "s05", "s06a", "s06b", "s07"), routes);
        assertFalse(patterns.isEmpty());
    }

    @Test
    void allPlayablePatternsCanRoundTripAsDataPackPatterns() {
        TouhouSpells.register((id, pattern) -> {
            var encoded = DanmakuPattern.CODEC.encodeStart(JsonOps.INSTANCE, pattern);
            assertTrue(encoded.isSuccess(), () -> id + ": " + encoded.error().orElseThrow().message());
            var decoded = DanmakuPattern.CODEC.parse(JsonOps.INSTANCE, encoded.getOrThrow());
            assertTrue(decoded.isSuccess(), () -> id + ": " + decoded.error().orElseThrow().message());
        });
    }

    @Test
    void woodCardsRetainTheirDistinctSourceLayoutsAndCycles() {
        var patterns = new LinkedHashMap<Identifier, DanmakuPattern>();
        TouhouSpells.register(patterns::put);
        String prefix = "reverie_dreams:touhou/th06/";
        var horn = patterns.get(Identifier.parse(prefix + "s04_016_normal"));
        var upper = patterns.get(Identifier.parse(prefix + "s04_020_normal"));
        var upperHard = patterns.get(Identifier.parse(prefix + "s04_020_hard"));
        var storm = patterns.get(Identifier.parse(prefix + "s04_024_hard"));
        for (var pattern : List.of(horn, upper, upperHard, storm)) {
            assertEquals(700, pattern.timing().duration());
            assertEquals(40, pattern.timing().loopStart());
        }
        assertEquals(12, horn.timing().loopLength());
        assertEquals(40, storm.timing().loopLength());
        assertEquals(6, shotsAt(horn, 40));
        assertEquals(9, shotsAt(upper, 40));
        assertEquals(19, shotsAt(upperHard, 40));
        assertEquals(16, shotsAt(storm, 40));
        assertTrue(horn.emitters().stream().anyMatch(e -> e.delay() == 41 && e.offset().x > 0));
        assertTrue(upper.emitters().stream().anyMatch(e -> e.delay() == 41 && e.offset().x < 0
                && e.bullet().motion().segments().getFirst().duration() == 20));
        assertTrue(storm.emitters().stream().anyMatch(e -> e.delay() == 40 && e.offset().x > 0));
        assertTrue(storm.emitters().stream().anyMatch(e -> e.delay() == 42 && e.offset().x < 0));
    }

    @Test
    void characterTooltipsFollowEncounterOwners() {
        assertEquals("entity.reverie_dreams.letty_whiterock", character("th07/s01_001_normal"));
        assertEquals("entity.reverie_dreams.cirno", character("th07/s01_001_hard"));
        assertEquals("entity.reverie_dreams.chen", character("th07/s07_002_extra"));
        assertEquals("entity.reverie_dreams.ran", character("th07/s08_001_phantasm"));
        assertEquals("entity.reverie_dreams.yakumo_yukai", character("th07/s08_013_phantasm"));
        assertEquals("entity.reverie_dreams.erin", character("th08/s06b_001_normal"));
        assertEquals("entity.reverie_dreams.houraisan_kaguya", character("th08/s06b_002_normal"));
        assertEquals("item.reverie_dreams.spellcard.character.aki_shizuha", character("th10/s01_000_hard"));
        assertEquals("item.reverie_dreams.spellcard.character.aki_minoriko", character("th10/s01_003_normal"));
        assertEquals("entity.reverie_dreams.moriya_suwako", character("th10/s07_100_extra"));
    }

    @Test
    void phantasmCardsFollowExtraInEncounterOrder() {
        var patterns = new LinkedHashMap<Identifier, DanmakuPattern>();
        TouhouSpells.register(patterns::put);
        var ids = patterns.keySet().stream().map(Identifier::toString).toList();
        var phantasm = ids.stream().filter(id -> id.contains("touhou/th07/s08_")).toList();
        var expected = java.util.stream.IntStream.rangeClosed(1, 13)
                .mapToObj(i -> "reverie_dreams:touhou/th07/s08_%03d_phantasm".formatted(i))
                .toList();
        assertEquals(expected, phantasm);
        assertTrue(ids.indexOf(expected.getFirst()) > ids.indexOf("reverie_dreams:touhou/th07/s07_001_extra"));
        assertTrue(ids.indexOf(expected.getFirst()) > ids.indexOf("reverie_dreams:touhou/th07/s07_003_extra"));
    }

    @Test
    void spellcardTintFollowsDifficulty() {
        assertEquals(0x4C7BFF, DanmakuDefaults.difficultyColor(Identifier.parse("reverie_dreams:touhou/th07/s01_001_normal")));
        assertEquals(0xD94444, DanmakuDefaults.difficultyColor(Identifier.parse("reverie_dreams:touhou/th07/s01_001_hard")));
        assertEquals(0xE69D47, DanmakuDefaults.difficultyColor(Identifier.parse("reverie_dreams:touhou/th07/s07_001_extra")));
        assertEquals(0xFFFFFF, DanmakuDefaults.difficultyColor(Identifier.parse("reverie_dreams:touhou/th07/s08_001_phantasm")));
    }

    @Test
    void barrierUsesAllOfItsSourceWavesWithoutLoopingOrInventedLasers() {
        var patterns = new LinkedHashMap<Identifier, DanmakuPattern>();
        TouhouSpells.register(patterns::put);
        var barrier = patterns.get(Identifier.parse("reverie_dreams:touhou/th07/s08_013_phantasm"));
        assertEquals(1480, barrier.timing().duration());
        assertEquals(0, barrier.timing().loopLength());
        assertEquals(List.of(134, 400, 667, 674, 950, 957, 964, 1261),
                barrier.emitters().stream().map(DanmakuEmitter::delay).distinct().toList());
        assertTrue(barrier.lasers().isEmpty());
        for (String path : List.of("s08_003_phantasm", "s08_008_phantasm", "s08_013_phantasm")) {
            var pattern = patterns.get(Identifier.parse("reverie_dreams:touhou/th07/" + path));
            var motion = pattern.emitters().stream().flatMap(emitter -> emitter.bullet().motion().segments().stream()).toList();
            assertFalse(motion.stream().anyMatch(segment -> segment.is(MotionSegment.HOMING)), path);
            assertTrue(motion.stream().anyMatch(segment -> segment.is(MotionSegment.TURN)), path);
        }
    }

    @Test
    void th11FirstTwoStagesFollowTheSourceOrderOwnersAndDeadlines() {
        var patterns = new LinkedHashMap<Identifier, DanmakuPattern>();
        TouhouSpells.register(patterns::put);
        var ids = patterns.keySet().stream().filter(id -> id.getPath().startsWith("touhou/th11/s01_")
                || id.getPath().startsWith("touhou/th11/s02_")).toList();
        assertEquals(List.of("s01_001_hard", "s01_004_normal", "s01_005_hard", "s01_008_normal", "s01_009_hard",
                        "s02_012_normal", "s02_013_hard", "s02_016_normal", "s02_017_hard",
                        "s02_020_normal", "s02_021_hard", "s02_024_normal", "s02_025_hard"),
                ids.stream().map(id -> id.getPath().substring("touhou/th11/".length())).toList());
        assertEquals(List.of(720, 700, 700, 800, 800, 680, 680, 800, 700, 800, 800, 860, 860),
                ids.stream().map(id -> patterns.get(id).timing().duration()).toList());
        assertEquals("entity.reverie_dreams.kisume", TouhouSpellCharacters.translationKey(ids.getFirst()));
        assertEquals("entity.reverie_dreams.kurodani_yamame", TouhouSpellCharacters.translationKey(ids.get(4)));
        assertEquals("entity.reverie_dreams.mizuhashi_parsee", TouhouSpellCharacters.translationKey(ids.getLast()));
        assertTrue(patterns.get(ids.getFirst()).lasers().size() > 1);
        assertTrue(patterns.get(ids.get(4)).emitters().size() > patterns.get(ids.get(3)).emitters().size());
        assertTrue(patterns.get(ids.getLast()).emitters().size() > patterns.get(ids.get(11)).emitters().size());
    }

    @Test
    void th18StageOneKeepsTheNormalAndHardCountsAndPhaseOrder() {
        var patterns = new LinkedHashMap<Identifier, DanmakuPattern>();
        TouhouSpells.register(patterns::put);
        var ids = patterns.keySet().stream().filter(id -> id.getPath().startsWith("touhou/th18/s01_")).toList();
        assertEquals(List.of("s01_002_normal", "s01_003_hard", "s01_006_normal", "s01_007_hard"),
                ids.stream().map(id -> id.getPath().substring("touhou/th18/".length())).toList());
        assertTrue(ids.stream().allMatch(id -> patterns.get(id).timing().duration() == 700
                && "entity.reverie_dreams.goutokuzi_mike".equals(TouhouSpellCharacters.translationKey(id))));
        assertEquals(5, patterns.get(ids.get(0)).emitters().getFirst().shape().count());
        assertEquals(10, patterns.get(ids.get(1)).emitters().getFirst().shape().count());
        assertEquals(3 * 7 + 1, patterns.get(ids.get(2)).emitters().size());
        assertEquals(6 * 7 + 1, patterns.get(ids.get(3)).emitters().size());
    }

    @Test
    void th18StageSixRetainsAllSixAttacksAndTheirDeadlines() {
        var patterns = new LinkedHashMap<Identifier, DanmakuPattern>();
        TouhouSpells.register(patterns::put);
        var ids = patterns.keySet().stream().filter(id -> id.getPath().startsWith("touhou/th18/s06_")).toList();
        assertEquals(List.of(1400, 1400, 1400, 1400, 1100, 1100, 1000, 1000, 1200, 1200, 2400, 2400),
                ids.stream().map(id -> patterns.get(id).timing().duration()).toList());
        assertTrue(ids.stream().allMatch(id -> "entity.reverie_dreams.tenkyu_chimata".equals(TouhouSpellCharacters.translationKey(id))));
        var normalMarket = patterns.get(ids.get(4));
        var hardMarket = patterns.get(ids.get(5));
        assertEquals(16, normalMarket.emitters().getFirst().shape().count());
        assertEquals(24, hardMarket.emitters().getFirst().shape().count());
        assertTrue(patterns.get(ids.getLast()).emitters().stream().anyMatch(emitter -> emitter.delay() > 1750));
    }

    @Test
    void th15StageSixFollowsJunkoSevenCardFinale() {
        var patterns = new LinkedHashMap<Identifier, DanmakuPattern>();
        TouhouSpells.register(patterns::put);
        var ids = patterns.keySet().stream().filter(id -> id.getPath().startsWith("touhou/th15/s06_")).toList();
        assertEquals(List.of("s06_078_normal", "s06_078_hard", "s06_082_normal", "s06_082_hard",
                        "s06_086_normal", "s06_086_hard", "s06_090_normal", "s06_090_hard",
                        "s06_094_normal", "s06_094_hard", "s06_098_normal", "s06_098_hard",
                        "s06_102_normal", "s06_102_hard"),
                ids.stream().map(id -> id.getPath().substring("touhou/th15/".length())).toList());
        assertEquals(List.of(1100, 1100, 1100, 1100, 1200, 1200, 1000, 1000, 1200, 1200, 600, 600, 2400, 2400),
                ids.stream().map(id -> patterns.get(id).timing().duration()).toList());
        assertTrue(ids.stream().allMatch(id -> "entity.reverie_dreams.junko".equals(TouhouSpellCharacters.translationKey(id))));
        assertEquals(96, patterns.get(ids.get(0)).emitters().getFirst().shape().count());
        assertEquals(108, patterns.get(ids.get(1)).emitters().getFirst().shape().count());
        assertTrue(patterns.get(ids.getLast()).emitters().stream().anyMatch(emitter -> emitter.delay() > 750));
    }

    @Test
    void th15StageOwnersFollowTheEncounterOrder() {
        var patterns = new LinkedHashMap<Identifier, DanmakuPattern>();
        TouhouSpells.register(patterns::put);
        assertEquals("entity.reverie_dreams.seiran",
                TouhouSpellCharacters.translationKey(Identifier.parse("reverie_dreams:touhou/th15/s01_002_normal")));
        assertEquals("entity.reverie_dreams.ringo",
                TouhouSpellCharacters.translationKey(Identifier.parse("reverie_dreams:touhou/th15/s02_010_hard")));
        assertEquals("entity.reverie_dreams.doremy_sweet",
                TouhouSpellCharacters.translationKey(Identifier.parse("reverie_dreams:touhou/th15/s03_022_normal")));
        assertEquals("entity.reverie_dreams.kisin_sagume",
                TouhouSpellCharacters.translationKey(Identifier.parse("reverie_dreams:touhou/th15/s04_042_hard")));
        assertEquals("entity.reverie_dreams.clownpiece",
                TouhouSpellCharacters.translationKey(Identifier.parse("reverie_dreams:touhou/th15/s05_074_hard")));
    }

    @Test
    void dreamVineSamplesTheInteriorOfFiveStarEdges() {
        var patterns = new LinkedHashMap<Identifier, DanmakuPattern>();
        TouhouSpells.register(patterns::put);
        var vine = patterns.get(Identifier.parse("reverie_dreams:touhou/th06/s03_010_hard"));
        assertNotNull(vine);
        assertEquals(800, vine.timing().duration());
        assertEquals(60, vine.timing().loopLength());
        var vertex = vine.emitters().stream().filter(e -> e.delay() == 0).toList();
        var next = vine.emitters().stream().filter(e -> e.delay() == 2).toList();
        assertEquals(5, vertex.size());
        assertEquals(5, next.size());
        var expected = vertex.get(0).offset().lerp(vertex.get(1).offset(), 0.2)
                .add(0, 0, 6 / 160f * 0.85);
        assertTrue(next.getFirst().offset().distanceTo(expected) < 1.0E-6);
    }

    @Test
    void th18ExtraRegistersTheMidbossBeforeMomoyoAndKeepsTheFinalePhases() {
        var patterns = new LinkedHashMap<Identifier, DanmakuPattern>();
        TouhouSpells.register(patterns::put);
        var ids = patterns.keySet().stream().filter(id -> id.getPath().startsWith("touhou/th18/s07_")).toList();
        assertEquals(13, ids.size());
        assertTrue(ids.subList(0, 3).stream().allMatch(id ->
                "entity.reverie_dreams.kudamaki_tsukasa".equals(TouhouSpellCharacters.translationKey(id))));
        assertTrue(ids.subList(3, 13).stream().allMatch(id ->
                "entity.reverie_dreams.himemushi_momoyo".equals(TouhouSpellCharacters.translationKey(id))));
        assertEquals(12, patterns.get(ids.get(1)).lasers().stream()
                .filter(laser -> laser.at() == 30).count());
        var finale = patterns.get(ids.getLast());
        assertEquals(3000, finale.timing().duration());
        assertTrue(finale.emitters().stream().anyMatch(emitter -> emitter.delay() == 2600 && emitter.shape().rings() == 12));
    }

    @Test
    void byakurenUsesTheHardInterruptAndFourIndependentBeamOrigins() {
        var patterns = new LinkedHashMap<Identifier, DanmakuPattern>();
        TouhouSpells.register(patterns::put);
        var ids = patterns.keySet().stream().filter(id -> id.getPath().startsWith("touhou/th12/s06_")).toList();
        assertEquals(12, ids.size());
        assertEquals(900, patterns.get(ids.get(0)).timing().duration());
        assertEquals(700, patterns.get(ids.get(1)).timing().duration());
        var recitation = patterns.get(Identifier.parse("reverie_dreams:touhou/th12/s06_091_normal"));
        assertEquals(1400, recitation.timing().duration());
        assertEquals(4, recitation.lasers().stream().map(laser -> laser.offset()).distinct().count());
        assertEquals(4, recitation.lasers().stream().filter(laser -> laser.sweepHalfAngle() == 3.75f).count(),
                () -> "Recitation beam sequence: " + recitation.lasers());
    }

    @Test
    void th13TenDesiresKeepsTheTenDesiresEncounterOwnersAndOverdrive() {
        var patterns = new LinkedHashMap<Identifier, DanmakuPattern>();
        TouhouSpells.register(patterns::put);
        var ids = patterns.keySet().stream().filter(id -> id.getPath().startsWith("touhou/th13/")).toList();
        assertEquals(76, ids.size());
        assertEquals("entity.reverie_dreams.yuyuko",
                TouhouSpellCharacters.translationKey(ids.getFirst()));
        assertEquals("entity.reverie_dreams.tatara_kogasa",
                TouhouSpellCharacters.translationKey(Identifier.parse("reverie_dreams:touhou/th13/s02_018_normal")));
        assertEquals("entity.reverie_dreams.miyako_yoshika",
                TouhouSpellCharacters.translationKey(Identifier.parse("reverie_dreams:touhou/th13/s03_036_normal")));
        assertEquals("entity.reverie_dreams.kaku_seiga",
                TouhouSpellCharacters.translationKey(Identifier.parse("reverie_dreams:touhou/th13/s04_047_hard")));
        assertEquals("entity.reverie_dreams.mononobe_no_futo",
                TouhouSpellCharacters.translationKey(Identifier.parse("reverie_dreams:touhou/th13/s05_062_normal")));
        assertEquals("entity.reverie_dreams.toyosatomimi_no_miko",
                TouhouSpellCharacters.translationKey(Identifier.parse("reverie_dreams:touhou/th13/s06_102_hard")));
        // timing().duration() is in ticks (frames / 3).
        assertEquals(1100, patterns.get(Identifier.parse("reverie_dreams:touhou/th13/s06_078_normal")).timing().duration());
        assertEquals(2400, patterns.get(Identifier.parse("reverie_dreams:touhou/th13/s06_102_normal")).timing().duration());
        // 7 Overdrive (O67 rank) variants + 14 Extra-stage cards all share the _extra suffix.
        var extra = ids.stream().filter(id -> id.getPath().contains("_extra")).toList();
        assertEquals(21, extra.size());
        assertEquals("entity.reverie_dreams.yuyuko",
                TouhouSpellCharacters.translationKey(extra.getFirst()));
        assertEquals("entity.reverie_dreams.houjuu_nue",
                TouhouSpellCharacters.translationKey(Identifier.parse("reverie_dreams:touhou/th13/s07_106_extra")));
        assertEquals("entity.reverie_dreams.hutatsuiwa_mamizou",
                TouhouSpellCharacters.translationKey(Identifier.parse("reverie_dreams:touhou/th13/s07_109_extra")));
    }

    @Test
    void th16HiddenStarKeepsTheSeasonOwnersAndExtraRematch() {
        var patterns = new LinkedHashMap<Identifier, DanmakuPattern>();
        TouhouSpells.register(patterns::put);
        var ids = patterns.keySet().stream().filter(id -> id.getPath().startsWith("touhou/th16/")).toList();
        assertEquals(62, ids.size());
        assertEquals("entity.reverie_dreams.eternity_larva",
                TouhouSpellCharacters.translationKey(ids.getFirst()));
        assertEquals("entity.reverie_dreams.sakata_nemuno",
                TouhouSpellCharacters.translationKey(Identifier.parse("reverie_dreams:touhou/th16/s02_008_normal")));
        assertEquals("entity.reverie_dreams.komano_aunn",
                TouhouSpellCharacters.translationKey(Identifier.parse("reverie_dreams:touhou/th16/s03_030_normal")));
        assertEquals("entity.reverie_dreams.yatadera_narumi",
                TouhouSpellCharacters.translationKey(Identifier.parse("reverie_dreams:touhou/th16/s04_042_normal")));
        assertEquals("entity.reverie_dreams.teireida_mai",
                TouhouSpellCharacters.translationKey(Identifier.parse("reverie_dreams:touhou/th16/s05_066_normal")));
        assertEquals("entity.reverie_dreams.matara_okina",
                TouhouSpellCharacters.translationKey(Identifier.parse("reverie_dreams:touhou/th16/s06_102_normal")));
        assertEquals(2400, patterns.get(Identifier.parse("reverie_dreams:touhou/th16/s06_102_normal")).timing().duration());
        assertTrue(ids.stream().anyMatch(id -> id.getPath().startsWith("touhou/th16/s07_")),
                "the full-power Okina rematch keeps its Extra stage");
    }

    @Test
    void th09PhantasmagoriaKeepsAllSixteenDuelistsInShotTableOrder() {
        var patterns = new LinkedHashMap<Identifier, DanmakuPattern>();
        TouhouSpells.register(patterns::put);
        var ids = patterns.keySet().stream().filter(id -> id.getPath().startsWith("touhou/th09/")).toList();
        assertEquals(32, ids.size());
        assertEquals("entity.reverie_dreams.reimu",
                TouhouSpellCharacters.translationKey(ids.getFirst()));
        assertEquals("entity.reverie_dreams.sakuya",
                TouhouSpellCharacters.translationKey(Identifier.parse("reverie_dreams:touhou/th09/pl02_001_normal")));
        assertEquals("entity.reverie_dreams.cirno",
                TouhouSpellCharacters.translationKey(Identifier.parse("reverie_dreams:touhou/th09/pl05_001_normal")));
        assertEquals("item.reverie_dreams.spellcard.character.inaba_tewi",
                TouhouSpellCharacters.translationKey(Identifier.parse("reverie_dreams:touhou/th09/pl08_001_normal")));
        assertEquals("entity.reverie_dreams.kazami_yuka",
                TouhouSpellCharacters.translationKey(Identifier.parse("reverie_dreams:touhou/th09/pl09_001_normal")));
        assertEquals("item.reverie_dreams.spellcard.character.onozuka_komachi",
                TouhouSpellCharacters.translationKey(Identifier.parse("reverie_dreams:touhou/th09/pl12_001_normal")));
        assertEquals("entity.reverie_dreams.shikieiki_yamaxanadu",
                TouhouSpellCharacters.translationKey(Identifier.parse("reverie_dreams:touhou/th09/pl13_002_hard")));
        assertEquals("entity.reverie_dreams.lunasa_prismriver",
                TouhouSpellCharacters.translationKey(ids.getLast()));
        assertTrue(ids.stream().allMatch(id -> patterns.get(id).timing().duration() == SpellTiming.framesToTicks(720)));
    }

    private static String character(String path) {
        String key = TouhouSpellCharacters.translationKey(Identifier.parse("reverie_dreams:touhou/" + path));
        assertNotNull(key, path);
        return key;
    }

    private static int shotsAt(DanmakuPattern pattern, int tick) {
        return pattern.emitters().stream().filter(e -> e.delay() == tick).mapToInt(e -> e.shape().count()).sum();
    }

    private static Map<String, String> chineseNames() {
        var names = new LinkedHashMap<String, String>();
        TouhouSpells.addChineseSpellNames((key, name) -> {
            assertFalse(name.isBlank(), key);
            assertNull(names.put(key, name), key);
        });
        return names;
    }
}
