package cc.thonly.reverie_dreams.fabric.datagen.lang;

import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTrajectories;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.danmaku.touhou.TouhouSpells;
import cc.thonly.reverie_dreams.registry.content.danmaku.soku.SokuMoves;
import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuDefaults;
import cc.thonly.reverie_dreams.data.danmaku.trajectory.PlayerTrajectoryType;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider.TranslationBuilder;

public interface DanmakuLang {

    @SuppressWarnings("DataFlowIssue")
    static void add(TranslationBuilder builder) {
        for (PlayerTrajectoryType type : PlayerTrajectoryType.values()) {
            builder.add(type.id(), type.chineseName());
        }
        builder.add("item.tooltip.danmaku_volley_multiplier", "每轮伤害倍率：×%s");
        builder.add(BuiltInRegistryProviders.DANMAKU_TRAJECTORY.getKey(DanmakuTrajectories.SINGLE), "线性");
        builder.add(BuiltInRegistryProviders.DANMAKU_TRAJECTORY.getKey(DanmakuTrajectories.TRIPLE), "三线");
        builder.add(BuiltInRegistryProviders.DANMAKU_TRAJECTORY.getKey(DanmakuTrajectories.FAN), "扇形");
        builder.add(BuiltInRegistryProviders.DANMAKU_TRAJECTORY.getKey(DanmakuTrajectories.BULLET), "子弹");
        builder.add(BuiltInRegistryProviders.DANMAKU_TRAJECTORY.getKey(DanmakuTrajectories.HEART), "心形");
        builder.add(BuiltInRegistryProviders.DANMAKU_TRAJECTORY.getKey(DanmakuTrajectories.X), "十字");
        builder.add(BuiltInRegistryProviders.DANMAKU_TRAJECTORY.getKey(DanmakuTrajectories.STAR), "星星");
        builder.add(BuiltInRegistryProviders.DANMAKU_TRAJECTORY.getKey(DanmakuTrajectories.ROUND), "圆");
        builder.add(BuiltInRegistryProviders.DANMAKU_TRAJECTORY.getKey(DanmakuTrajectories.RING), "环");
        builder.add(BuiltInRegistryProviders.DANMAKU_TRAJECTORY.getKey(DanmakuTrajectories.RING_VIEW), "视向环");
        builder.add(BuiltInRegistryProviders.DANMAKU_TRAJECTORY.getKey(DanmakuTrajectories.RING_HORIZONTAL), "水平环");
        builder.add(DanmakuTypes.AMULET.getItemHolder().asItem(), "札弹");
        builder.add(DanmakuTypes.ARROWHEAD.getItemHolder().asItem(), "鳞弹");
        builder.add(DanmakuTypes.BALL.getItemHolder().asItem(), "小玉");
        builder.add(DanmakuTypes.BUBBLE.getItemHolder().asItem(), "大玉");
        builder.add(DanmakuTypes.BULLET.getItemHolder().asItem(), "铳弹");
        builder.add(DanmakuTypes.FIREBALL.getItemHolder().asItem(), "火光弹");
        builder.add(DanmakuTypes.FIREBALL_GLOWY.getItemHolder().asItem(), "水光弹");
        builder.add(DanmakuTypes.KUNAI.getItemHolder().asItem(), "链弹");
        builder.add(DanmakuTypes.RICE.getItemHolder().asItem(), "米弹");
        builder.add(DanmakuTypes.STAR.getItemHolder().asItem(), "星弹");
        builder.add(DanmakuTypes.NOTE.getItemHolder().asItem(), "音符");
        builder.add(DanmakuTypes.LEAF.getItemHolder().asItem(), "落叶弹");
        builder.add(DanmakuTypes.BUTTERFLY.getItemHolder().asItem(), "蝴蝶弹");
        builder.add(DanmakuTypes.ICICLE.getItemHolder().asItem(), "冰锥弹");
        builder.add(DanmakuTypes.SAKURA_PETAL.getItemHolder().asItem(), "樱花瓣弹");
        builder.add(DanmakuTypes.THROWING_KNIFE.getItemHolder().asItem(), "飞刀弹");
        builder.add(DanmakuTypes.PELLET.getItemHolder().asItem(), "点弹");
        builder.add(DanmakuTypes.NEEDLE.getItemHolder().asItem(), "针弹");
        builder.add(DanmakuTypes.WATER_DROP.getItemHolder().asItem(), "水滴弹");
        builder.add(DanmakuTypes.YIN_YANG.getItemHolder().asItem(), "阴阳玉");
        builder.add(DanmakuTypes.BACILLUS.getItemHolder().asItem(), "杆菌弹");
        builder.add(DanmakuTypes.HEART.getItemHolder().asItem(), "心弹");
        builder.add(DanmakuTypes.ARROW.getItemHolder().asItem(), "矢弹");
        builder.add(DanmakuTypes.OVAL.getItemHolder().asItem(), "椭弹");
        builder.add(DanmakuTypes.MEDIUM_ORB.getItemHolder().asItem(), "中玉");
        builder.add(DanmakuTypes.HIGHLIGHT_ORB.getItemHolder().asItem(), "高光中玉");
        builder.add(DanmakuTypes.LIGHT_ORB.getItemHolder().asItem(), "光玉");
        builder.add(DanmakuTypes.SHORT_LASER.getItemHolder().asItem(), "飞行短激光");
        builder.add(DanmakuTypes.LUNATIC_MOON.getItemHolder().asItem(), "月狂冲击的月球");
        builder.add(DanmakuTypes.LASER.getItemHolder().asItem(), "激光");
        builder.add(DanmakuTypes.BIG_LASER.getItemHolder().asItem(), "大激光");
        builder.add(DanmakuTypes.FAMILIAR.getItemHolder().asItem(), "使魔");
        TouhouSpells.addChineseSpellNames(builder::add);
        SokuMoves.catalog().forEach(move -> builder.add(DanmakuDefaults.translationKey(move.id()), move.chineseName()));
        builder.add("item.reverie_dreams.spellcard.named", "%s（%s）");
        builder.add("item.reverie_dreams.spellcard.difficulty.normal", "普通");
        builder.add("item.reverie_dreams.spellcard.difficulty.hard", "困难");
        builder.add("item.reverie_dreams.spellcard.difficulty.extra", "Extra");
        builder.add("item.reverie_dreams.spellcard.difficulty.phantasm", "幻想");
        builder.add("command.touhou.pattern_card.given", "已获得 %s");
        builder.add("item.tooltip.spellcard.character", "%s的符卡");
        builder.add("item.reverie_dreams.spellcard.character.prismriver", "普莉兹姆利巴三姐妹");
        builder.add("item.reverie_dreams.spellcard.character.aki_shizuha", "秋静叶");
        builder.add("item.reverie_dreams.spellcard.character.aki_minoriko", "秋穰子");
        builder.add("item.reverie_dreams.spellcard.character.ichirin_unzan", "云居一轮与云山");
        builder.add("item.reverie_dreams.spellcard.character.tsukumo_benben", "九十九弁弁");
        builder.add("item.reverie_dreams.spellcard.character.tsukumo_yatsuhashi", "九十九八千");
        builder.add("item.reverie_dreams.spellcard.character.mizuhashi_murasa", "村纱水蜜");
        builder.add("item.reverie_dreams.spellcard.character.toramaru_shou", "寅丸星");
        builder.add("item.reverie_dreams.spellcard.character.hijiri_byakuren", "圣白莲");
        builder.add("item.reverie_dreams.spellcard.character.inaba_tewi", "因幡帝");
        builder.add("item.reverie_dreams.spellcard.character.medicine_melancholy", "梅蒂欣·梅兰可莉");
        builder.add("item.reverie_dreams.spellcard.character.onozuka_komachi", "小野塚小町");
        builder.add("death.attack.danmaku", "%1$s 满身疮痍");
        builder.add("death.attack.danmaku.player", "%1$s 与 %2$s 对战的过程中满身疮痍");
        builder.add("death.attack.danmaku.item", "%1$s 被 %2$s 在对战的过程中使用 %3$s 被打的满身疮痍");
    }
}
