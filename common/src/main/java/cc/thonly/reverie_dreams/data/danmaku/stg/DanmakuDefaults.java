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

    public static Component itemName(Identifier id) {
        String path = id.getPath();
        if (id.getNamespace().equals(ReverieDreams.MOD_ID) && path.startsWith("touhou/")) {
            for (String difficulty : new String[]{"normal", "hard", "extra", "phantasm"}) {
                if (path.endsWith("_" + difficulty)) {
                    return Component.translatableWithFallback("item.reverie_dreams.spellcard.named", "%s (%s)",
                            displayName(id), Component.translatableWithFallback(
                                    "item.reverie_dreams.spellcard.difficulty." + difficulty, difficulty));
                }
            }
        }
        return displayName(id);
    }

    /**
     * 符卡物品的默认染色：Normal 蓝、Hard 红、Extra 橙；其他难度保留白色。
     */
    public static int difficultyColor(Identifier id) {
        String path = id.getPath();
        if (path.endsWith("_normal")) {
            return 0x4C7BFF;
        }
        if (path.endsWith("_hard")) {
            return 0xD94444;
        }
        if (path.endsWith("_extra")) {
            return 0xE69D47;
        }
        return 0xFFFFFF;
    }

    public static Component displayName(Identifier patternId) {
        return Component.translatableWithFallback(translationKey(patternId), patternId.toString());
    }
}
