package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.spellcard.KeyframeRange;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.DanmakuMotion;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.implemented;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.RICE;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.bullet;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.sp;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th06.Th06PatternSupport.stream;

final class Extra {
    private Extra() {}

    private static final Identifier STAR = ReverieDreams.id("star");

    // 7 面 · Extra：帕秋莉（道中）→ 芙兰朵露
    static List<TouhouSpell> spells() {
        return List.of(
                // 月符：Sub22(d7) L545-586。场内随机位近直下狭角带零速弹 + 向量加速（flags 24），
                // 后接固定向上扇（L582，12-16 路速度 2.7）。随机落点用旋转环位近似并标注。
                implemented("touhou/th06/s07_051_extra", "月符「Silent Selene」", 2100, () -> silentSelene()),
                // 日符：Sub23(d7) L590-667。EX13 圆周 3/5/6 等分点多源二次发射——多源缺失，阻塞。
                catalogOnly("touhou/th06/s07_052_extra", "日符「Royal Flare」", 2880),
                // 賢者の石：Sub24(d7) L676-697。五个位置固定的辅助敌人 Sub25-29——使魔/多源缺失，阻塞。
                catalogOnly("touhou/th06/s07_053_extra", "火水木金土符「贤者之石」", 2400),
                // 禁忌系列：多源分时（Sub34）、扫动激光（EX14）、克隆多源、边缘源+邻弹改向（EX15）等——逐卡阻塞。
                catalogOnly("touhou/th06/s07_054_extra", "禁忌「Cranberry Trap」", 3300),
                catalogOnly("touhou/th06/s07_055_extra", "禁忌「Laevatein」", 3300),
                catalogOnly("touhou/th06/s07_056_extra", "禁忌「Four of a Kind」", 3900),
                catalogOnly("touhou/th06/s07_057_extra", "禁忌「Kagome Kagome」", 3900),
                // 恋の迷路：Sub48(d7) L1635-1698。4 路固定扇高速旋转（15.56°/波）+ 24 路 ×3 速度层镜像扇（3.4/2.87/2.33 px/f）。
                implemented("touhou/th06/s07_058_extra", "禁忌「恋之迷宫」", 3600, () -> loveLabyrinth()),
                catalogOnly("touhou/th06/s07_059_extra", "禁弹「Starbow Break」", 4200),
                catalogOnly("touhou/th06/s07_060_extra", "禁弹「Catadioptric」", 4200),
                catalogOnly("touhou/th06/s07_061_extra", "禁弹「刻着过去的钟表」", 4200),
                // 生存卡：L2356 ins_133 + 5160 帧无死亡回调，中心与四角辅助源接力（Sub66/Sub61-65）——生存+多源缺失，阻塞。
                catalogOnly("touhou/th06/s07_062_extra", "秘弹「之后就一个人都没有了吗？」", 5160),
                // ＱＥＤ：Sub68(d7) L2891-2929。EX16 按 HP/时间缩放、88 路环 + TLR 反弹——反弹与血量缩放缺失，阻塞。
                catalogOnly("touhou/th06/s07_063_extra", "QED「495年的波纹」", 9000)
        );
    }

    /**
     * 月符「サイレントセレナ」：场内散布落弹（近直下狭角带、零速起 + 向量加速）
     * + 固定向上扇（14 路，速度 2.7 px/f）。随机落点以旋转环位近似并标注。ECL Sub22(d7) L545-586。
     */
    private static DanmakuPattern silentSelene() {
        DanmakuBulletSpec rain = bullet(RICE, 0.3f, 65)
                .withMotion(DanmakuMotion.of(Vec3.ZERO, 0.01f, 0f, List.of()));
        DanmakuEmitter rainA = DanmakuEmitter.of(EmitShape.ring(7, 4.5f, false), rain)
                .withTiming(0, 130, 4)
                .withAim(EmitAim.FIXED, 90f, 0f)
                .withRotation(KeyframeRange.empty(0f, 360f));
        DanmakuEmitter rainB = DanmakuEmitter.of(EmitShape.ring(7, 2.5f, false), rain)
                .withTiming(2, 130, 4)
                .withAim(EmitAim.FIXED, 90f, 180f)
                .withRotation(KeyframeRange.empty(0f, -360f));
        DanmakuEmitter flare = stream(EmitShape.fan(14, 195f), bullet(RICE, 2.7f, 45),
                20, 130, 20, EmitAim.FIXED, -85f, 0f, null);
        return DanmakuPattern.of(rainA, rainB, flare);
    }

    /**
     * 禁忌「恋の迷路」：4 路固定扇高速旋转（原 15.56°/波，此处 720°/100 tick 降档）
     * + 24 路 ×3 速度层镜像大扇（3.4/2.87/2.33 px/f，每 40 tick）。ECL Sub48(d7) L1635-1698。
     */
    private static DanmakuPattern loveLabyrinth() {
        float[] speeds = {3.4f, 2.867f, 2.333f};
        List<DanmakuEmitter> emitters = new java.util.ArrayList<>();
        emitters.add(stream(EmitShape.fan(4, 11.25f), bullet(RICE, 1.4f, 55),
                0, 100, 1, EmitAim.FIXED, 0f, 0f, KeyframeRange.empty(90f, 810f)));
        for (int layer = 0; layer < 3; layer++) {
            emitters.add(stream(EmitShape.fan(24, 258.75f), bullet(STAR, speeds[layer], 65),
                    0, 160, 40, EmitAim.FIXED, 0f, 0f, KeyframeRange.empty(270f, 990f)));
        }
        return DanmakuPattern.of(emitters.toArray(new DanmakuEmitter[0]));
    }
}
