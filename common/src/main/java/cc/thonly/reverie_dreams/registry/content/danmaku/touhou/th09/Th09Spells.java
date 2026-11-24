package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th09;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.DanmakuMotion;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * TH09 (Phantasmagoria of Flower View) versus-game spell cards. PoFV has no boss ECL: the two
 * spell names per character live in the per-character shot tables (plNN.sht) — the last two
 * name strings are used (the first string in three-string tables is the basic shot name).
 * PoFV spell windows are roughly 720 frames; patterns are theme ports of the versus attacks
 * (the old-format plNN.ecl AI scripts are not 1:1 expressible).
 * Source: E:/Temp/rd-ecl-continuation-20261116/th09/dat/plNN.sht.
 */
public final class Th09Spells {
    private static final int FRAMES = 720;
    private static final int WHITE = 0xEDEDF5;
    private static final int RED = 0xE86F5A;
    private static final int GOLD = 0xE8C96F;
    private static final int PURPLE = 0xB8A0E8;
    private static final int BLUE = 0x8FB8E8;
    private static final int ICE = 0xA8D8F0;
    private static final int PINK = 0xF2C6D8;
    private static final int GREEN = 0x9FE8A8;
    private static final int DARK = 0x6F5F8F;
    private static final int YELLOW = 0xE8E89F;

    private static final Identifier BALL = ReverieDreams.id("ball");
    private static final Identifier AMULET = ReverieDreams.id("amulet");
    private static final Identifier STAR = ReverieDreams.id("star");
    private static final Identifier BULLET = ReverieDreams.id("bullet");
    private static final Identifier KUNAI = ReverieDreams.id("kunai");
    private static final Identifier RICE = ReverieDreams.id("rice");
    private static final Identifier BUBBLE = ReverieDreams.id("bubble");
    private static final Identifier ICE_BALL = ReverieDreams.id("icicle");
    private static final Identifier NOTE = ReverieDreams.id("note");

    private static int ticks(int frames) {
        return SpellTiming.framesToTicks(frames);
    }

    private Th09Spells() {}

    private static TouhouSpell spell(int player, int slot, String owner, String name,
                                     java.util.function.Supplier<DanmakuPattern> pattern) {
        String path = String.format(Locale.ROOT, "touhou/th09/pl%02d_%03d_%s", player, slot, slot == 1 ? "normal" : "hard");
        return TouhouSpell.implemented(path, name, FRAMES, pattern)
                .withCharacter(owner.startsWith("item.") ? owner : "entity.reverie_dreams." + owner);
    }

    private static DanmakuBulletSpec shot(Identifier type, float speed, int color, int lifetime, MotionSegment... motion) {
        return DanmakuBulletSpec.of(type, speed, DanmakuMotion.of(Vec3.ZERO, 0f, 0f, List.of(motion)))
                .withColor(color).withLifetime(lifetime);
    }

    private static DanmakuEmitter fire(EmitShape shape, DanmakuBulletSpec bullet, int at) {
        return DanmakuEmitter.of(shape, bullet).withTiming(SpellTiming.framesToTicks(at), 0, 1)
                .withAim(EmitAim.SOURCE_FACING, 0, 0);
    }

    private static DanmakuEmitter sweep(DanmakuEmitter emitter, float fromYaw, float toYaw) {
        return emitter.withRanges(KeyframeRange.empty(0f, 0f), KeyframeRange.empty(fromYaw, toYaw));
    }

    private static DanmakuPattern timing(List<DanmakuEmitter> emitters) {
        return new DanmakuPattern(emitters)
                .withTiming(new SpellTiming(SpellTiming.framesToTicks(FRAMES),
                        SpellTiming.framesToTicks(60), SpellTiming.framesToTicks(120)));
    }

    private static List<TouhouSpell> allSpells;

    public static List<TouhouSpell> spells() {
        if (allSpells == null) {
            allSpells = build();
        }
        return allSpells;
    }

    private static List<TouhouSpell> build() {
        return List.of(
                // pl00 Reimu: 霊符「夢想封印」 yin-yang orb ring cascade; 霊符「博麗幻影」 homing amulet veil.
                spell(0, 1, "reimu", "灵符「梦想封印」", Th09Spells::fantasySeal),
                spell(0, 2, "reimu", "灵符「博丽幻影」", Th09Spells::hakureiPhantom),
                // pl01 Marisa: 魔符「スターダストレヴァリエ」 star-ring spiral; 魔符「イリュージョンスター」 laser stars.
                spell(1, 1, "marisa", "魔符「星尘华丽」", Th09Spells::stardustReverie),
                spell(1, 2, "marisa", "魔符「幻影星」", Th09Spells::illusionStar),
                // pl02 Sakuya: 時符「プライベートヴィジョン」 knife wall; 時符「ミステリアスジャック」 time-stop knives.
                spell(2, 1, "sakuya", "时符「私人幻景」", Th09Spells::privateVision),
                spell(2, 2, "sakuya", "时符「神秘杰克」", Th09Spells::mysteriousJack),
                // pl03 Youmu: 迷符「纏縛剣」 binding sword waves; 迷符「半身大悟」 phantom half barrage.
                spell(3, 1, "youmu", "迷符「缠缚剑」", Th09Spells::bindingSword),
                spell(3, 2, "youmu", "迷符「半身大悟」", Th09Spells::halfBodyEnlightenment),
                // pl04 Luna Child: 波符「月面波紋 ルナウェーブ」 moon ripples; 散符「栄華之夢 ルナメガロポリス」 glittering spray.
                spell(4, 1, "lunar", "波符「月面波纹 Luna Wave」", Th09Spells::lunaWave),
                spell(4, 2, "lunar", "散符「荣华之梦 Luna Megalopolis」", Th09Spells::lunaMegalopolis),
                // pl05 Cirno: 凍符「パーフェクトフリーズ」 freeze burst; 凍符「コールドディヴィニティー」 ice pillars.
                spell(5, 1, "cirno", "冻符「完美冻结」", Th09Spells::perfectFreeze),
                spell(5, 2, "cirno", "冻符「冰之神性」", Th09Spells::coldDivinity),
                // pl06 Lyrica: 騒符「ソウルノイズフロー」 note flow; 騒符「リリカ・ソロライブ」 solo concert.
                spell(6, 1, "lyrica_prismriver", "骚符「灵魂噪声流」", Th09Spells::soulNoiseFlow),
                spell(6, 2, "lyrica_prismriver", "骚符「莉莉卡独奏会」", Th09Spells::lyricaSoloLive),
                // pl07 Mystia: 鳥符「ヒューマンケージダブル」 cage bars; 鳥符「ミステリアスソング」 night song.
                spell(7, 1, "mystia_lorelei", "鸟符「人类牢笼·双」", Th09Spells::humanCageDouble),
                spell(7, 2, "mystia_lorelei", "鸟符「神秘之歌」", Th09Spells::mysteriousSong),
                // pl08 Tewi: 兎符「開運大紋」 luck coins; 兎符「因幡の素兎」 rabbit scatter.
                spell(8, 1, "item.reverie_dreams.spellcard.character.inaba_tewi", "兔符「开运大纹」", Th09Spells::luckCoin),
                spell(8, 2, "item.reverie_dreams.spellcard.character.inaba_tewi", "兔符「因幡的白兔」", Th09Spells::inabaWhiteRabbit),
                // pl09 Yuuka: 花符「幻想郷の開花」 flower bloom; 幻想「花鳥風月 嘯風弄月」 flower storm.
                spell(9, 1, "kazami_yuka", "花符「幻想乡开花」", Th09Spells::gensokyoBlossom),
                spell(9, 2, "kazami_yuka", "幻想「花鸟风月 啸风弄月」", Th09Spells::kachoFugetsu),
                // pl10 Aya: 風符「風神一扇」 wind gust; 疾風「風神少女」 wind girls.
                spell(10, 1, "aya", "风符「风神一扇」", Th09Spells::fuujinIssen),
                spell(10, 2, "aya", "疾风「风神少女」", Th09Spells::fuujinShoujo),
                // pl11 Medicine: 毒符「神経の毒」 poison fog; 毒符「憂鬱の毒」 melancholy poison.
                spell(11, 1, "item.reverie_dreams.spellcard.character.medicine_melancholy", "毒符「神经之毒」", Th09Spells::nervePoison),
                spell(11, 2, "item.reverie_dreams.spellcard.character.medicine_melancholy", "毒符「忧郁之毒」", Th09Spells::melancholyPoison),
                // pl12 Komachi: 投銭「宵越しの銭」 coin toss; 死神「ヒガンルトゥール」 crossing waves.
                spell(12, 1, "item.reverie_dreams.spellcard.character.onozuka_komachi", "投钱「隔夜之钱」", Th09Spells::yoigoshiNoZeni),
                spell(12, 2, "item.reverie_dreams.spellcard.character.onozuka_komachi", "死神「彼岸回归」", Th09Spells::higanRetour),
                // pl13 Eiki: 罪符「彷徨える大罪」 wandering sins; 審判「ラストジャッジメント」 judgment lasers.
                spell(13, 1, "shikieiki_yamaxanadu", "罪符「彷徨的大罪」", Th09Spells::wanderingCrimes),
                spell(13, 2, "shikieiki_yamaxanadu", "审判「最终裁决」", Th09Spells::lastJudgment),
                // pl14 Merlin: 騒符「ソウルゴーハッピー」 happy horns; 騒符「メルラン・ハッピーライブ」 solo concert.
                spell(14, 1, "merlin_prismriver", "骚符「灵魂高奏」", Th09Spells::soulGoHappy),
                spell(14, 2, "merlin_prismriver", "骚符「梅尔兰快乐独奏会」", Th09Spells::merlinHappyLive),
                // pl15 Lunasa: 騒符「ノイズメランコリー」 violin noise; 騒符「ルナサ・ソロライブ」 solo concert.
                spell(15, 1, "lunasa_prismriver", "骚符「噪声忧郁曲」", Th09Spells::noiseMelancholy),
                spell(15, 2, "lunasa_prismriver", "骚符「露娜萨独奏会」", Th09Spells::lunasaSoloLive));
    }

    private static DanmakuPattern fantasySeal() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var orb = shot(BALL, 0.2f, RED, 160);
        for (int v = 0; v < 8; v++) {
            emitters.add(sweep(fire(EmitShape.ring(8, 0f, true), orb, 60 + v * 12), 45f * v, 45f * v + 360f));
        }
        return timing(emitters);
    }

    private static DanmakuPattern hakureiPhantom() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var amulet = shot(AMULET, 0.14f, WHITE, 170, MotionSegment.homing(ticks(10), 2.2f, 60));
        emitters.add(fire(EmitShape.fan(5, 30f), amulet, 60));
        emitters.add(fire(EmitShape.fan(5, 30f), amulet, 150));
        emitters.add(fire(EmitShape.fan(7, 40f), amulet, 240));
        return timing(emitters);
    }

    private static DanmakuPattern stardustReverie() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var star = shot(STAR, 0.22f, GOLD, 150, MotionSegment.rotate(0, 1.2f));
        for (int v = 0; v < 10; v++) {
            emitters.add(sweep(fire(EmitShape.ring(6, 0f, true), star, 60 + v * 9), 60f * v, 60f * v));
        }
        return timing(emitters);
    }

    private static DanmakuPattern illusionStar() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var beam = shot(BULLET, 0.45f, YELLOW, 90);
        for (int v = 0; v < 12; v++) {
            var cue = fire(EmitShape.aim(), beam, 60 + v * 9);
            emitters.add(sweep(cue, -160f + 32f * v, -160f + 32f * v + 24f));
        }
        return timing(emitters);
    }

    private static DanmakuPattern privateVision() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var knife = shot(KUNAI, 0.3f, WHITE, 130);
        for (int v = 0; v < 10; v++) {
            emitters.add(sweep(fire(EmitShape.fan(3, 14f), knife, 60 + v * 10), -20f + 4f * v, -20f + 4f * v));
        }
        return timing(emitters);
    }

    private static DanmakuPattern mysteriousJack() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var knife = shot(KUNAI, 0.34f, PURPLE, 120);
        for (int w = 0; w < 3; w++) {
            for (int k = 0; k < 8; k++) {
                emitters.add(sweep(fire(EmitShape.fan(1, 0f), knife, 60 + w * 60 + k), -180f + 45f * k, -180f + 45f * k));
            }
        }
        return timing(emitters);
    }

    private static DanmakuPattern bindingSword() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var wave = shot(BULLET, 0.3f, WHITE, 120, MotionSegment.rotate(0, 0.9f));
        for (int v = 0; v < 12; v++) {
            emitters.add(sweep(fire(EmitShape.fan(2, 8f), wave, 60 + v * 8), 30f * v, 30f * v));
        }
        return timing(emitters);
    }

    private static DanmakuPattern halfBodyEnlightenment() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var slash = shot(BULLET, 0.36f, PINK, 110, MotionSegment.rotate(ticks(20), -1.6f));
        var phantom = shot(BULLET, 0.24f, WHITE, 140, MotionSegment.homing(ticks(30), 1.8f, 50));
        for (int v = 0; v < 8; v++) {
            emitters.add(sweep(fire(EmitShape.fan(4, 10f), slash, 60 + v * 10), 45f * v, 45f * v));
            emitters.add(fire(EmitShape.fan(3, 20f), phantom, 100 + v * 10));
        }
        return timing(emitters);
    }

    private static DanmakuPattern lunaWave() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var ripple = shot(BALL, 0.16f, ICE, 180, MotionSegment.rotate(0, 0.7f));
        for (int v = 0; v < 9; v++) {
            emitters.add(sweep(fire(EmitShape.ring(10, 0f, true), ripple, 60 + v * 10), 36f * v, 36f * v));
        }
        return timing(emitters);
    }

    private static DanmakuPattern lunaMegalopolis() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var glimmer = shot(STAR, 0.2f, YELLOW, 150,
                MotionSegment.split(ticks(16), new cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionPayload(
                        STAR, ICE, 0.8f, 0.14f, 2, 30f, 100)));
        emitters.add(fire(EmitShape.ring(24, 0f, true), glimmer, 60));
        emitters.add(fire(EmitShape.ring(24, 0f, true), glimmer, 140));
        emitters.add(fire(EmitShape.ring(28, 0f, true), glimmer, 220));
        return timing(emitters);
    }

    private static DanmakuPattern perfectFreeze() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var frozen = shot(ICE_BALL, 0.2f, ICE, 200, MotionSegment.freeze(ticks(30), 40));
        emitters.add(fire(EmitShape.ring(20, 0f, true), frozen, 60));
        emitters.add(fire(EmitShape.ring(24, 0f, true), frozen, 160));
        return timing(emitters);
    }

    private static DanmakuPattern coldDivinity() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var pillar = shot(ICE_BALL, 0.14f, BLUE, 200, MotionSegment.setSpeed(ticks(40), 0.4f));
        for (int k = 0; k < 8; k++) {
            emitters.add(sweep(fire(EmitShape.fan(3, 6f), pillar, 60 + k * 8), -90f + 24f * k, -90f + 24f * k));
        }
        return timing(emitters);
    }

    private static DanmakuPattern soulNoiseFlow() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var note = shot(NOTE, 0.18f, PURPLE, 170, MotionSegment.rotate(0, 0.6f));
        for (int v = 0; v < 10; v++) {
            emitters.add(sweep(fire(EmitShape.fan(4, 16f), note, 60 + v * 10), 40f * v, 40f * v));
        }
        return timing(emitters);
    }

    private static DanmakuPattern lyricaSoloLive() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var solo = shot(NOTE, 0.22f, YELLOW, 160);
        for (int v = 0; v < 8; v++) {
            emitters.add(sweep(fire(EmitShape.ring(12, 0f, true), solo, 60 + v * 12), 30f * v, 30f * v));
        }
        return timing(emitters);
    }

    private static DanmakuPattern humanCageDouble() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var bar = shot(BULLET, 0.26f, DARK, 180);
        for (int v = 0; v < 10; v++) {
            emitters.add(sweep(fire(EmitShape.fan(4, 20f), bar, 60 + v * 9), 36f * v, 36f * v));
        }
        return timing(emitters);
    }

    private static DanmakuPattern mysteriousSong() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var song = shot(NOTE, 0.2f, RED, 160, MotionSegment.homing(ticks(20), 1.6f, 50));
        for (int v = 0; v < 6; v++) {
            emitters.add(fire(EmitShape.fan(5, 24f), song, 60 + v * 16));
        }
        return timing(emitters);
    }

    private static DanmakuPattern luckCoin() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var coin = shot(RICE, 0.24f, GOLD, 140);
        for (int v = 0; v < 10; v++) {
            emitters.add(sweep(fire(EmitShape.ring(8, 0f, true), coin, 60 + v * 8), 45f * v, 45f * v));
        }
        return timing(emitters);
    }

    private static DanmakuPattern inabaWhiteRabbit() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var rabbit = shot(BULLET, 0.3f, WHITE, 120, MotionSegment.homing(ticks(16), 2.4f, 60));
        for (int v = 0; v < 8; v++) {
            emitters.add(fire(EmitShape.aim(), rabbit, 60 + v * 10));
        }
        return timing(emitters);
    }

    private static DanmakuPattern gensokyoBlossom() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var petal = shot(BULLET, 0.16f, PINK, 200, MotionSegment.rotate(0, 0.8f));
        for (int v = 0; v < 10; v++) {
            emitters.add(sweep(fire(EmitShape.ring(10, 0f, true), petal, 60 + v * 10), 36f * v, 36f * v));
        }
        return timing(emitters);
    }

    private static DanmakuPattern kachoFugetsu() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var bloom = shot(BALL, 0.14f, YELLOW, 200, MotionSegment.setSpeed(ticks(40), 0.34f));
        for (int v = 0; v < 6; v++) {
            emitters.add(fire(EmitShape.ring(16, 0f, true), bloom, 60 + v * 18));
        }
        return timing(emitters);
    }

    private static DanmakuPattern fuujinIssen() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var gust = shot(BULLET, 0.5f, GREEN, 90);
        for (int v = 0; v < 6; v++) {
            emitters.add(sweep(fire(EmitShape.fan(8, 24f), gust, 60 + v * 8), -40f + 16f * v, -40f + 16f * v));
        }
        return timing(emitters);
    }

    private static DanmakuPattern fuujinShoujo() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var wind = shot(STAR, 0.34f, GREEN, 140, MotionSegment.rotate(0, 1.1f));
        for (int v = 0; v < 12; v++) {
            emitters.add(sweep(fire(EmitShape.ring(6, 0f, true), wind, 60 + v * 7), 60f * v, 60f * v));
        }
        return timing(emitters);
    }

    private static DanmakuPattern nervePoison() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var fog = shot(BUBBLE, 0.1f, GREEN, 220, MotionSegment.rotate(0, 0.5f));
        emitters.add(fire(EmitShape.ring(12, 0f, true), fog, 60));
        emitters.add(fire(EmitShape.ring(16, 0f, true), fog, 160));
        return timing(emitters);
    }

    private static DanmakuPattern melancholyPoison() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var drop = shot(BULLET, 0.22f, PURPLE, 160, MotionSegment.rotate(0, -0.7f));
        for (int v = 0; v < 12; v++) {
            emitters.add(sweep(fire(EmitShape.fan(3, 10f), drop, 60 + v * 9), 40f * v, 40f * v));
        }
        return timing(emitters);
    }

    private static DanmakuPattern yoigoshiNoZeni() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var coin = shot(RICE, 0.28f, GOLD, 130);
        for (int v = 0; v < 12; v++) {
            emitters.add(sweep(fire(EmitShape.fan(5, 18f), coin, 60 + v * 8), 30f * v, 30f * v));
        }
        return timing(emitters);
    }

    private static DanmakuPattern higanRetour() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var crossing = shot(BULLET, 0.32f, WHITE, 150, MotionSegment.rotate(0, 0.9f));
        for (int v = 0; v < 10; v++) {
            emitters.add(sweep(fire(EmitShape.fan(4, 12f), crossing, 60 + v * 9), 40f * v, 40f * v + 90f));
        }
        return timing(emitters);
    }

    private static DanmakuPattern wanderingCrimes() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var sin = shot(BALL, 0.18f, DARK, 180, MotionSegment.homing(ticks(24), 1.4f, 60));
        for (int v = 0; v < 8; v++) {
            emitters.add(fire(EmitShape.fan(4, 20f), sin, 60 + v * 12));
        }
        return timing(emitters);
    }

    private static DanmakuPattern lastJudgment() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var ray = shot(BULLET, 0.4f, WHITE, 110);
        for (int v = 0; v < 8; v++) {
            emitters.add(sweep(fire(EmitShape.fan(1, 0f), ray, 60 + v * 10), 360f * v / 8f, 360f * v / 8f));
            emitters.add(sweep(fire(EmitShape.fan(1, 0f), ray, 65 + v * 10), 360f * v / 8f + 22.5f, 360f * v / 8f + 22.5f));
        }
        return timing(emitters);
    }

    private static DanmakuPattern soulGoHappy() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var horn = shot(NOTE, 0.2f, YELLOW, 170, MotionSegment.rotate(0, 0.8f));
        for (int v = 0; v < 10; v++) {
            emitters.add(sweep(fire(EmitShape.fan(4, 14f), horn, 60 + v * 9), 36f * v, 36f * v));
        }
        return timing(emitters);
    }

    private static DanmakuPattern merlinHappyLive() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var solo = shot(NOTE, 0.22f, PINK, 160);
        for (int v = 0; v < 8; v++) {
            emitters.add(sweep(fire(EmitShape.ring(12, 0f, true), solo, 60 + v * 12), 30f * v, 30f * v));
        }
        return timing(emitters);
    }

    private static DanmakuPattern noiseMelancholy() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var noise = shot(NOTE, 0.2f, PURPLE, 170, MotionSegment.rotate(0, -0.8f));
        for (int v = 0; v < 10; v++) {
            emitters.add(sweep(fire(EmitShape.fan(4, 14f), noise, 60 + v * 9), -36f * v, -36f * v));
        }
        return timing(emitters);
    }

    private static DanmakuPattern lunasaSoloLive() {
        var emitters = new ArrayList<DanmakuEmitter>();
        var solo = shot(NOTE, 0.22f, WHITE, 160);
        for (int v = 0; v < 8; v++) {
            emitters.add(sweep(fire(EmitShape.ring(12, 0f, true), solo, 60 + v * 12), -30f * v, -30f * v));
        }
        return timing(emitters);
    }
}
