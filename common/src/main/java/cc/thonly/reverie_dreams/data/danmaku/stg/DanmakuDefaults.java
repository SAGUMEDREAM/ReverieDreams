package cc.thonly.reverie_dreams.data.danmaku.stg;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * STG 弹幕引擎的公共常量与展示工具。
 */
public final class DanmakuDefaults {
    /**
     * 缺省弹幕类型：护符弹（reverie_dreams:amulet）。
     */
    public static final Identifier DEFAULT_TYPE = ReverieDreams.id("amulet");

    private DanmakuDefaults() {
    }

    /**
     * 图案（符卡）名称翻译键：{@code danmaku_pattern.<namespace>.<path>}。
     */
    public static String translationKey(Identifier patternId) {
        return "danmaku_pattern." + patternId.getNamespace() + "." + patternId.getPath().replace('/', '.');
    }

    public static Component displayName(Identifier patternId) {
        return Component.translatable(translationKey(patternId));
    }
}
