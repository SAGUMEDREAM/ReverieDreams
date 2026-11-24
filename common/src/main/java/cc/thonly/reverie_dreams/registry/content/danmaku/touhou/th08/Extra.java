package cc.thonly.reverie_dreams.registry.content.danmaku.touhou.th08;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuBulletSpec;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuEmitter;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuPattern;
import cc.thonly.reverie_dreams.data.danmaku.stg.EmitAim;
import cc.thonly.reverie_dreams.data.danmaku.stg.SpellTiming;
import cc.thonly.reverie_dreams.data.danmaku.stg.shape.EmitShape;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.catalogOnly;
import static cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpell.implemented;

final class Extra {
    private Extra() {}

    private static final Identifier TYPE_BALL = ReverieDreams.id("ball");
    private static final Identifier TYPE_RICE = ReverieDreams.id("rice");
    private static final Identifier TYPE_STAR = ReverieDreams.id("star");

    // Extra · 上白泽慧音（道中）+ 藤原妹红（ecldata8.ecl，统一 s07）
    static List<TouhouSpell> spells() {
        return List.of(
                implemented("touhou/th08/s07_001_extra", "旧史「旧秘境史　-旧历史-」", 3600, () -> keineHistory(false)),
                catalogOnly("touhou/th08/s07_002_extra", "转世「一条归桥」", 3600),
                implemented("touhou/th08/s07_003_extra", "新史「新幻想史　-新历史-」", 3600, () -> keineHistory(true)),

                catalogOnly("touhou/th08/s07_005_extra", "时效「月之岩座之咒」", 3600),
                catalogOnly("touhou/th08/s07_006_extra", "不死「火之鸟　-凤翼天翔-」", 4620),
                catalogOnly("touhou/th08/s07_007_extra", "藤原「灭罪寺院伤」", 3720),
                catalogOnly("touhou/th08/s07_008_extra", "不死「徐福时空」", 5400),
                catalogOnly("touhou/th08/s07_009_extra", "灭罪「正直者之死」", 4200),
                catalogOnly("touhou/th08/s07_010_extra", "虚人「呜」", 4200),
                catalogOnly("touhou/th08/s07_011_extra", "不灭「不死鸟之尾」", 4200),
                catalogOnly("touhou/th08/s07_012_extra", "蓬莱「凯风快晴　-富士山火山-」", 4200),
                catalogOnly("touhou/th08/s07_013_extra", "「被不死鸟凭附」", 4620),
                catalogOnly("touhou/th08/s07_014_extra", "「蓬莱人偶」", 5400),
                catalogOnly("touhou/th08/s07_015_extra", "「不灭弹幕」", 4740)
        );
    }

    // Extra, ecldata8.ecl.raw L1051–1192 / L1320–1463. Old History alternates
    // two four-source layouts 440 frames apart. Next History starts with all eight.
    // Static world offsets replace screen coordinates; the source attackability and
    // secondary etEx transforms are encounter details, not part of the item cast.
    private static DanmakuPattern keineHistory(boolean next) {
        int intro = SpellTiming.framesToTicks(110);
        int swap = SpellTiming.framesToTicks(440);
        List<DanmakuEmitter> sources = new ArrayList<>();
        double[][] screen = {{64, 128}, {320, 128}, {152, 220}, {232, 220},
                {32, 188}, {352, 188}, {162, 150}, {222, 150}};
        int[] turns = {9, -9, -9, 9, -6, 6, 6, -6};
        for (int i = 0; i < screen.length; i++) {
            double x = (screen[i][0] - 192) / 18;
            double z = (screen[i][1] - 128) / 18 + 2;
            int spawn = intro + (i >= 4 && !next ? swap : 0);
            int start = spawn + SpellTiming.framesToTicks(60);
            Vec3 offset = new Vec3(x, 0, z);
            var rice = DanmakuBulletSpec.of(TYPE_RICE, next ? 0.27f : 0.48f)
                    .withColor(i % 2 == 0 ? 0xF47D94 : 0x7DBAF4).withLifetime(75);
            for (int pulse = 0; pulse < (next ? 4 : 9); pulse++) {
                int at = start + pulse * (next ? 5 : 3);
                float yaw = turns[i] * Math.max(0, pulse - (next ? 1 : 2));
                sources.add(DanmakuEmitter.of(EmitShape.fan(4, 45), rice)
                        .withOffset(offset).withAttached(false).withAim(EmitAim.FIXED, 0, yaw)
                        .withTiming(at, 0, 1));
            }
        }
        // Sub52 / Sub61: nine directions from -90° to +90° in steps of 22.5°.
        // Subsample the original 4-frame cadence; stagger phase over each short run.
        var spokes = DanmakuBulletSpec.of(next ? TYPE_STAR : TYPE_BALL, 0.65f)
                .withColor(next ? 0xB0E9FF : 0xEAA4F8).withLifetime(70);
        int cycleLength = next ? swap : 2 * swap;
        int support = intro + SpellTiming.framesToTicks(200);
        for (int at = support; at < intro + cycleLength; at += 8) {
            sources.add(DanmakuEmitter.of(EmitShape.fan(9, 180), spokes)
                    .withAttached(false).withAim(EmitAim.FIXED, 0, (at - support) * 0.55f)
                    .withTiming(at, 0, 1));
        }
        return new DanmakuPattern(sources).withTiming(new SpellTiming(1200, intro, cycleLength));
    }
}
