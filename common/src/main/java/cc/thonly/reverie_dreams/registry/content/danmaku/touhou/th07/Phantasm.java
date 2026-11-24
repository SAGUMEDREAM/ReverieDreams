package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuLaser;
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

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.implemented;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.ARROWHEAD;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.BALL;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.BLUE;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.BUBBLE;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.CYAN;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.DARK_BLUE;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.GREEN;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.MAGENTA;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.PURPLE;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.RED;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.RICE;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.WHITE;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.YELLOW;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.fire;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.shot;

/**
 * 妖妖梦 Phantasm（ecldata8，卡 #128–#140）。
 *
 * <p>道中八云蓝两张，关底八云紫十一张。全部图案按 ECL 入口、THBWiki 卡面描述和现有
 * STG 原语近似：使魔、分身、精确随机出生点与部分激光变向机制未逐帧还原，均以注释标明。
 */
final class Phantasm {
    private Phantasm() {}

    private static final Identifier BUTTERFLY = ReverieDreams.id("butterfly");
    private static final Vec3 UP = new Vec3(0.0D, 1.0D, 0.0D);

    static List<TouhouSpell> spells() {
        return List.of(
                implemented("touhou/th07/s08_001_phantasm", "式神「前鬼后鬼的守护」", 3600, Phantasm::zenkiGoki),
                implemented("touhou/th07/s08_002_phantasm", "式神「凭依荼吉尼天」", 3300, Phantasm::dakiniten),
                implemented("touhou/th07/s08_003_phantasm", "结界「梦境与现实的咒术」", 3600, Phantasm::curseOfDreamsAndReality),
                implemented("touhou/th07/s08_004_phantasm", "结界「动与静的均衡」", 3900, Phantasm::balanceOfMotionAndStillness),
                implemented("touhou/th07/s08_005_phantasm", "结界「光明与黑暗的网目」", 4500, Phantasm::meshOfLightAndDarkness),
                implemented("touhou/th07/s08_006_phantasm", "罔两「直球与曲球的梦乡」", 3900, Phantasm::dreamlandOfStraightAndCurve),
                implemented("touhou/th07/s08_007_phantasm", "罔两「八云紫的神隐」", 4500, Phantasm::yukarisSpiritingAway),
                implemented("touhou/th07/s08_008_phantasm", "罔两「栖息于禅寺的妖蝶」", 4800, Phantasm::butterflyInZenTemple),
                implemented("touhou/th07/s08_009_phantasm", "魍魉「二重黑死蝶」", 4800, Phantasm::doubleBlackDeathButterfly),
                implemented("touhou/th07/s08_010_phantasm", "式神「八云蓝」", 4800, Phantasm::ranYakumo),
                implemented("touhou/th07/s08_011_phantasm", "「人类与妖怪的界线」", 4560, Phantasm::boundaryOfHumansAndYoukai),
                implemented("touhou/th07/s08_012_phantasm", "结界「生与死的界线」", 7200, Phantasm::boundaryOfLifeAndDeath),
                implemented("touhou/th07/s08_013_phantasm", "紫奥义「弹幕结界」", 4440, Phantasm::danmakuBarrier)
        );
    }

    /**
     * #128（Sub60–62）：与橙「青鬼赤鬼」同族，黄绿双柱泡弹改为瞄准，并追加三向箭弹。
     */
    private static DanmakuPattern zenkiGoki() {
        DanmakuMotion settle = DanmakuMotion.of(Vec3.ZERO, 0f, 0f, List.of(
                MotionSegment.setSpeed(15, 0.05f),
                MotionSegment.accelerate(15, 0.012f)));
        DanmakuEmitter yellow = fire(EmitShape.fan(2, 18f), shot(BUBBLE, 0.4f, YELLOW, settle),
                0, 330, 44, EmitAim.TARGET, 0f, 0f,
                new Vec3(-2.2D, 1.2D, 0.0D), KeyframeRange.empty(0f, 0f));
        DanmakuEmitter green = fire(EmitShape.fan(2, 18f), shot(BUBBLE, 0.4f, GREEN, settle),
                22, 330, 44, EmitAim.TARGET, 0f, 0f,
                new Vec3(2.2D, 1.2D, 0.0D), KeyframeRange.empty(0f, 0f));
        DanmakuEmitter aimed = fire(EmitShape.fan(3, 32f), shot(ARROWHEAD, 0.5f, WHITE),
                60, 300, 36, EmitAim.TARGET, 0f, 0f);
        return DanmakuPattern.of(yellow, green, aimed);
    }

    /**
     * #129（Sub63–65）：与橙「飛翔毘沙門天」同族，红蓝两组五向箭弹镜像交错。
     */
    private static DanmakuPattern dakiniten() {
        DanmakuMotion rightBend = DanmakuMotion.of(Vec3.ZERO, 0f, 0f, List.of(
                MotionSegment.turn(12, 16f, UP)));
        DanmakuMotion leftBend = DanmakuMotion.of(Vec3.ZERO, 0f, 0f, List.of(
                MotionSegment.turn(12, -16f, UP)));
        DanmakuEmitter red = fire(EmitShape.fan(5, 34f), shot(ARROWHEAD, 0.5f, RED, rightBend),
                0, 300, 12, EmitAim.FIXED, 6f, -28f);
        DanmakuEmitter blue = fire(EmitShape.fan(5, 34f), shot(ARROWHEAD, 0.5f, BLUE, leftBend),
                6, 300, 12, EmitAim.FIXED, 6f, 208f);
        return DanmakuPattern.of(red, blue);
    }

    /**
     * #130（Sub88–92）：瞄准泡弹、改变方向的青米弹与后续爆发。
     * 原作二泡分裂与随机螺旋简化为规则螺旋与固定方阵。
     */
    private static DanmakuPattern curseOfDreamsAndReality() {
        DanmakuEmitter bubble = fire(EmitShape.fan(2, 14f), shot(BUBBLE, 0.42f, PURPLE),
                0, 320, 55, EmitAim.TARGET, 0f, 0f);
        DanmakuEmitter left = turnedRice(1, 10);
        DanmakuEmitter right = turnedRice(-1, 12);
        DanmakuEmitter matrix = fire(EmitShape.matrix(List.of(
                "##.##",
                "#####",
                "##.##")),
                shot(ARROWHEAD, 0.4f, DARK_BLUE),
                70, 280, 50, EmitAim.TARGET, 0f, 0f);
        return DanmakuPattern.of(bubble, left, right, matrix);
    }

    private static DanmakuEmitter turnedRice(int side, int delay) {
        DanmakuMotion redirected = DanmakuMotion.of(Vec3.ZERO, 0f, 0f, List.of(
                MotionSegment.freeze(7, 6),
                MotionSegment.turn(13, side * 35f, UP),
                MotionSegment.setSpeed(13, 0.26f)));
        return fire(EmitShape.aim(), shot(RICE, 0.42f, CYAN, redirected), delay, 304, 8,
                        EmitAim.SOURCE_FACING, 0f, 0f)
                .withOffset(new Vec3(side * 0.6, 0.25, 0))
                .withRanges(KeyframeRange.empty(0f, 5f), KeyframeRange.empty(0f, side * 720f));
    }

    /**
     * #131（Sub93–96）：三向瞄准泡弹与头顶大弹；中弹处的隐形使魔螺旋以中心密集螺旋近似。
     */
    private static DanmakuPattern balanceOfMotionAndStillness() {
        DanmakuEmitter aimed = fire(EmitShape.fan(3, 26f), shot(BUBBLE, 0.42f, PURPLE),
                0, 300, 55, EmitAim.TARGET, 0f, 0f);
        DanmakuEmitter overhead = fire(EmitShape.ring(5, 1.6f, true), shot(BALL, 0.35f, DARK_BLUE),
                40, 260, 60, EmitAim.FIXED, 10f, 90f,
                new Vec3(0.0D, 2.4D, 0.0D), KeyframeRange.empty(0f, 0f));
        DanmakuEmitter spiral = fire(EmitShape.fan(1, 0f), shot(BALL, 0.5f, BLUE),
                30, 250, 3, EmitAim.OUTWARD, 0f, 0f,
                new Vec3(0.0D, 0.8D, 0.0D), KeyframeRange.empty(0f, 1080f));
        return DanmakuPattern.of(aimed, overhead, spiral);
    }

    /**
     * #132（Sub97–99）：青紫泡弹近同帧交替，配合四向细激光网。
     * 原作激光变向与泡弹绕玩家角度差以扫掠激光近似。
     */
    private static DanmakuPattern meshOfLightAndDarkness() {
        DanmakuEmitter cyan = fire(EmitShape.fan(4, 44f), shot(BUBBLE, 0.4f, CYAN),
                0, 300, 50, EmitAim.TARGET, 0f, 0f);
        DanmakuEmitter magenta = fire(EmitShape.fan(4, 44f), shot(BUBBLE, 0.4f, MAGENTA),
                5, 300, 50, EmitAim.TARGET, 0f, 0f);
        DanmakuEmitter arrows = fire(EmitShape.ring(6, 1.2f, true), shot(ARROWHEAD, 0.55f, WHITE),
                20, 280, 40, EmitAim.TARGET, 0f, 0f);
        float sweep = 0.55f;
        return DanmakuPattern.of(cyan, magenta, arrows).withLasers(List.of(
                new DanmakuLaser(60, 4f, 45f, 22f, 0.35f, 20, 50, sweep, 0x60A8FF),
                new DanmakuLaser(60, 4f, 135f, 22f, 0.35f, 20, 50, -sweep, 0x60A8FF),
                new DanmakuLaser(220, 4f, 135f, 22f, 0.35f, 20, 50, sweep, 0xB060F0),
                new DanmakuLaser(220, 4f, 45f, 22f, 0.35f, 20, 50, -sweep, 0xB060F0)));
    }

    /**
     * #133（Sub100–101）：泡弹跳跃后自旋，并夹细激光缓慢收束。
     * 原作“大跳”由赋速段与绕出膛点公转近似。
     */
    private static DanmakuPattern dreamlandOfStraightAndCurve() {
        DanmakuMotion jumpAndSpin = DanmakuMotion.of(Vec3.ZERO, 0f, 0f, List.of(
                MotionSegment.setSpeed(14, 0.05f),
                MotionSegment.orbitAroundOrigin(14, 3.2f, new Vec3(1.2D, 0.0D, 0.0D))));
        DanmakuEmitter cyan = fire(EmitShape.fan(3, 30f), shot(BUBBLE, 0.42f, CYAN, jumpAndSpin),
                0, 320, 48, EmitAim.TARGET, 0f, 0f);
        DanmakuEmitter magenta = fire(EmitShape.fan(3, 30f), shot(BUBBLE, 0.42f, MAGENTA, jumpAndSpin),
                24, 320, 48, EmitAim.TARGET, 0f, 0f);
        return DanmakuPattern.of(cyan, magenta).withLasers(List.of(
                new DanmakuLaser(80, 0f, 15f, 24f, 0.25f, 25, 45, 0.35f, 0xB060F0),
                new DanmakuLaser(180, 0f, 165f, 24f, 0.25f, 25, 45, -0.35f, 0xB060F0)));
    }

    /**
     * #134（Sub102–103）：三角红泡 + 紫色蝶弹，六向细激光。
     * 原作瞬移与蝶弹加速未还原。
     */
    private static DanmakuPattern yukarisSpiritingAway() {
        DanmakuEmitter triangle = fire(EmitShape.fan(3, 26f), shot(BUBBLE, 0.42f, RED),
                0, 300, 55, EmitAim.TARGET, 0f, 0f);
        DanmakuMotion trail = DanmakuMotion.of(Vec3.ZERO, 0f, 0f, List.of(
                MotionSegment.accelerate(1, 0.01f)));
        DanmakuEmitter butterflies = fire(EmitShape.fan(6, 70f), shot(BUTTERFLY, 0.5f, PURPLE, trail),
                30, 300, 35, EmitAim.TARGET, 0f, 0f);
        return DanmakuPattern.of(triangle, butterflies).withLasers(List.of(
                new DanmakuLaser(100, 2f, 0f, 22f, 0.3f, 20, 40, 0.25f, 0xB060F0),
                new DanmakuLaser(100, 2f, 60f, 22f, 0.3f, 20, 40, 0.25f, 0xB060F0),
                new DanmakuLaser(100, 2f, 120f, 22f, 0.3f, 20, 40, 0.25f, 0xB060F0),
                new DanmakuLaser(100, 2f, 180f, 22f, 0.3f, 20, 40, 0.25f, 0xB060F0),
                new DanmakuLaser(100, 2f, 240f, 22f, 0.3f, 20, 40, 0.25f, 0xB060F0),
                new DanmakuLaser(100, 2f, 300f, 22f, 0.3f, 20, 40, 0.25f, 0xB060F0)));
    }

    /**
     * #135（Sub104–108）：蓝蝶停顿后回返、红蝶横展，并夹紫激光。
     * 子弹以定时减速和转弯重现回卷；发射后不再读取玩家位置。
     */
    private static DanmakuPattern butterflyInZenTemple() {
        DanmakuMotion stopAndReturn = DanmakuMotion.of(Vec3.ZERO, 0f, 0f, List.of(
                MotionSegment.setSpeed(12, 0.22f),
                MotionSegment.freeze(30, 10),
                MotionSegment.turn(40, 135f, UP),
                MotionSegment.setSpeed(40, 0.30f),
                MotionSegment.rotate(40, 1.6f),
                MotionSegment.linear(60)));
        DanmakuEmitter blue = fire(EmitShape.ring(6, 1.2f, true), shot(BUTTERFLY, 0.42f, DARK_BLUE, stopAndReturn),
                0, 320, 45, EmitAim.TARGET, 0f, 0f);
        DanmakuMotion spread = DanmakuMotion.of(Vec3.ZERO, 0f, 0f, List.of(
                MotionSegment.accelerateVector(0, new Vec3(0.006D, 0.0D, 0.0D), 60)));
        DanmakuEmitter redLines = fire(EmitShape.fan(5, 130f), shot(BUTTERFLY, 0.48f, RED, spread),
                20, 300, 40, EmitAim.TARGET, 0f, 0f);
        return DanmakuPattern.of(blue, redLines).withLasers(List.of(
                new DanmakuLaser(140, 0f, 30f, 20f, 0.25f, 18, 35, 0.3f, 0xB060F0),
                new DanmakuLaser(140, 0f, 150f, 20f, 0.25f, 18, 35, -0.3f, 0xB060F0),
                new DanmakuLaser(140, 0f, 270f, 20f, 0.25f, 18, 35, 0.3f, 0xB060F0)));
    }

    /**
     * #136（Sub109–113）：蓝红与青紫蝶弹反向旋流，交替波次。
     */
    private static DanmakuPattern doubleBlackDeathButterfly() {
        DanmakuMotion clockwise = DanmakuMotion.of(Vec3.ZERO, 0f, 0f, List.of(
                MotionSegment.rotate(6, 2.2f)));
        DanmakuMotion counterClockwise = DanmakuMotion.of(Vec3.ZERO, 0f, 0f, List.of(
                MotionSegment.rotate(6, -2.2f)));
        DanmakuEmitter odd = fire(EmitShape.ring(8, 0f, true), shot(BUTTERFLY, 0.5f, BLUE, clockwise),
                0, 300, 26, EmitAim.TARGET, 0f, 0f);
        DanmakuEmitter even = fire(EmitShape.ring(8, 0f, true), shot(BUTTERFLY, 0.5f, RED, counterClockwise),
                13, 300, 26, EmitAim.TARGET, 0f, 0f);
        DanmakuEmitter blades = fire(EmitShape.ring(6, 1.0f, true), shot(ARROWHEAD, 0.55f, MAGENTA),
                100, 220, 30, EmitAim.OUTWARD, 0f, 0f);
        return DanmakuPattern.of(odd, even, blades);
    }

    /**
     * #137（Sub114–118）：召唤八云蓝的蝶弹环。使魔本体与墙面反弹未还原，
     * 以两侧偏移锚点的反向旋转蝶环近似。
     */
    private static DanmakuPattern ranYakumo() {
        DanmakuEmitter left = fire(EmitShape.ring(5, 1.2f, true), shot(BUTTERFLY, 0.45f, BLUE),
                0, 300, 40, EmitAim.OUTWARD, 0f, 0f,
                new Vec3(-2.4D, 1.0D, 0.0D), KeyframeRange.empty(0f, 720f));
        DanmakuEmitter right = fire(EmitShape.ring(5, 1.2f, true), shot(BUTTERFLY, 0.45f, RED),
                20, 300, 40, EmitAim.OUTWARD, 0f, 0f,
                new Vec3(2.4D, 1.0D, 0.0D), KeyframeRange.empty(0f, -720f));
        DanmakuEmitter aimed = fire(EmitShape.fan(3, 24f), shot(ARROWHEAD, 0.5f, WHITE),
                40, 280, 45, EmitAim.TARGET, 0f, 0f);
        return DanmakuPattern.of(left, right, aimed);
    }

    /**
     * #138（Sub119–126）：紫泡环 + 旋转米弹 + 收尾蓝蝶。
     * 原作圆形轨迹与蓝色蝶弹终段以旋转发射器与延迟蝶环近似。
     */
    private static DanmakuPattern boundaryOfHumansAndYoukai() {
        DanmakuEmitter outer = fire(EmitShape.ring(6, 1.8f, true), shot(BUBBLE, 0.38f, PURPLE),
                0, 300, 50, EmitAim.TARGET, 0f, 0f);
        DanmakuEmitter rotating = fire(EmitShape.fan(1, 0f), shot(RICE, 0.45f, BLUE),
                20, 280, 4, EmitAim.OUTWARD, 0f, 0f,
                Vec3.ZERO, KeyframeRange.empty(0f, 960f));
        DanmakuEmitter butterflies = fire(EmitShape.ring(5, 1.4f, true), shot(BUTTERFLY, 0.42f, DARK_BLUE),
                260, 180, 35, EmitAim.TARGET, 0f, 0f);
        return DanmakuPattern.of(outer, rotating, butterflies);
    }

    /**
     * #139（Sub127–134）：多层蝶环与快速瞄准蝶群，模拟原作积累阶段。
     */
    private static DanmakuPattern boundaryOfLifeAndDeath() {
        DanmakuEmitter rings = fire(EmitShape.multiRing(3, 8, 0.8f, true, 12f, 0.4f, 0.12f),
                shot(BUTTERFLY, 0.45f, DARK_BLUE), 0, 300, 40, EmitAim.TARGET, 0f, 0f);
        DanmakuEmitter aimed = fire(EmitShape.fan(7, 80f), shot(BUTTERFLY, 0.6f, BLUE),
                120, 220, 30, EmitAim.TARGET, 0f, 0f);
        DanmakuEmitter finalWave = fire(EmitShape.ring(12, 1.6f, true), shot(BUTTERFLY, 0.7f, CYAN),
                320, 140, 25, EmitAim.TARGET, 0f, 0f);
        return DanmakuPattern.of(rings, aimed, finalWave);
    }

    /**
     * #140（Sub129–134）：五段成对的铳弹源，前置 120/121 帧移动后依次放出。
     * 保留各组的时间、镜像与停止后释放；二维静止墙面改为有限宽度的斜向弹列。
     */
    private static DanmakuPattern danmakuBarrier() {
        var waves = new ArrayList<DanmakuEmitter>();
        addBarrierPair(waves, 280, 0, 3, 4, 18f, MAGENTA, CYAN);
        addBarrierPair(waves, 1080, 0, 2, 3, -31f, BLUE, GREEN);
        addBarrierPair(waves, 1880, 1, 2, 3, 23f, BLUE, RED);
        addBarrierPair(waves, 1900, 2, 2, 3, -23f, RED, BLUE);
        addBarrierPair(waves, 2730, 0, 2, 3, 37f, GREEN, PURPLE);
        addBarrierPair(waves, 2750, 1, 2, 3, 27f, YELLOW, MAGENTA);
        addBarrierPair(waves, 2770, 2, 2, 3, 17f, CYAN, BLUE);
        addBarrierPair(waves, 3660, 0, 2, 2, 48f, RED, PURPLE);
        return new DanmakuPattern(waves).withTiming(new SpellTiming(SpellTiming.framesToTicks(4440), 0, 0));
    }

    private static void addBarrierPair(List<DanmakuEmitter> waves, int frame, int layer, int count, int stepFrames,
                                       float exitAngle, int leftColor, int rightColor) {
        int start = SpellTiming.framesToTicks(frame + (frame == 3660 ? 121 : 120));
        Identifier shotType = ReverieDreams.id("bullet");
        for (int side : new int[]{-1, 1}) {
            int color = side < 0 ? leftColor : rightColor;
            var motion = DanmakuMotion.of(Vec3.ZERO, 0f, 0f, List.of(
                    MotionSegment.freeze(14, 45),
                    MotionSegment.turn(59, side * exitAngle, UP),
                    MotionSegment.setSpeed(59, 0.34f)));
            waves.add(fire(EmitShape.fan(count, 10f), shot(shotType, 0.21f, color, motion).withLifetime(115),
                    start, 42, SpellTiming.framesToTicks(stepFrames), EmitAim.SOURCE_FACING, 0f, 0f)
                    .withOffset(new Vec3(side * (2.5 + 0.4 * layer), 0.35 + 0.6 * layer, 0))
                    .withRanges(KeyframeRange.empty(layer * 5f, layer * 5f - 6f),
                            KeyframeRange.empty(side * 50f, side * 5f)));
        }
    }
}
