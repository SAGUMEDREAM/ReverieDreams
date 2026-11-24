package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.DanmakuMotion;
import cc.thonly.reverie_dreams.data.danmaku.stg.motion.MotionSegment;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.implemented;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.ARROWHEAD;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.BLUE;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.RED;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.fire;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th07.Th07PatternSupport.shot;

final class Stage02 {
    private Stage02() {}

    // 二面（橙道中：Normal 凤凰卵 / Hard 凤凰展翅；关底按难度各三张）
    static List<TouhouSpell> spells() {
        return List.of(
                catalogOnly("touhou/th07/s02_001_normal", "仙符「凤凰卵」", 2400),
                catalogOnly("touhou/th07/s02_002_normal", "式符「飞翔晴明」", 2700),
                catalogOnly("touhou/th07/s02_003_normal", "天符「天仙鸣动」", 3600),
                implemented("touhou/th07/s02_004_normal", "仙符「尸解永远」", 2700, () -> shikaiEien(false)),
                catalogOnly("touhou/th07/s02_001_hard", "仙符「凤凰展翅」", 2400),
                catalogOnly("touhou/th07/s02_002_hard", "陰陽「道满晴明」", 3000),
                catalogOnly("touhou/th07/s02_003_hard", "翔符「飞翔韦驮天」", 3600),
                implemented("touhou/th07/s02_004_hard", "鬼符「鬼门金神」", 2700, () -> kimonKonjin())
        );
    }

    /**
     * 二面橙 Boss 卡四 #23（Sub68）：对角纹样弹，红蓝两向交替。
     * 原作两色发射顺序随机，此处改为规则交替并标注遗漏；速度取 N 档局部值折算。
     */
    private static DanmakuPattern shikaiEien(boolean hard) {
        float speed = hard ? 0.55f : 0.5f;
        DanmakuEmitter downLeft = fire(EmitShape.fan(6, 50f), shot(ARROWHEAD, speed, RED),
                0, 380, 24, EmitAim.FIXED, 8f, -135f);
        DanmakuEmitter upRight = fire(EmitShape.fan(6, 50f), shot(ARROWHEAD, speed, BLUE),
                12, 380, 24, EmitAim.FIXED, 8f, 45f);
        return DanmakuPattern.of(downLeft, upRight);
    }

    /**
     * 二面橙 Boss 卡四 #24（Sub70，仅 Hard 到达）：蓝纹顺时针旋转，数秒后红纹反向。
     * 旋转用子弹 ROTATE 段还原（原作纹样弹角速度参数见 BulletManager 角速度指令）。
     */
    private static DanmakuPattern kimonKonjin() {
        DanmakuEmitter blue = fire(EmitShape.fan(7, 55f),
                shot(ARROWHEAD, 0.42f, BLUE, DanmakuMotion.of(Vec3.ZERO, 0f, 0f,
                        List.of(MotionSegment.rotate(3, 2.4f)))),
                0, 200, 24, EmitAim.FIXED, 6f, -30f);
        DanmakuEmitter red = fire(EmitShape.fan(7, 55f),
                shot(ARROWHEAD, 0.44f, RED, DanmakuMotion.of(Vec3.ZERO, 0f, 0f,
                        List.of(MotionSegment.rotate(3, -2.4f)))),
                200, 180, 22, EmitAim.FIXED, 6f, 150f);
        return DanmakuPattern.of(blue, red);
    }
}
