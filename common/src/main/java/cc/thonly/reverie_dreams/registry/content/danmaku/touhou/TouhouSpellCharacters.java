package cc.thonly.reverie_dreams.registry.content.danmaku.touhou;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * 符卡目录的角色归属，用于物品提示。
 *
 * <p>按各作 ECL 的关卡流程和卡号区间归属到角色实体翻译键；三骚灵共用卡与
 * 风神录一面道中使用独立文案键。卡号顺序与目录登记顺序一致。
 */
public final class TouhouSpellCharacters {
    private static final String ENTITY = "entity.reverie_dreams.";
    private static final String CUSTOM = "item.reverie_dreams.spellcard.character.";

    private TouhouSpellCharacters() {
    }

    @Nullable
    public static String translationKey(Identifier patternId) {
        if (!patternId.getNamespace().equals(ReverieDreams.MOD_ID)) {
            return null;
        }
        String declared = TouhouSpells.characterKey(patternId);
        if (declared != null) {
            return declared;
        }
        String[] path = patternId.getPath().split("/");
        if (path.length != 3 || !path[0].equals("touhou")) {
            return null;
        }
        String[] card = path[2].split("_");
        if (card.length < 2) {
            return null;
        }
        int number;
        try {
            number = Integer.parseInt(card[1]);
        } catch (NumberFormatException error) {
            return null;
        }
        String difficulty = card.length > 2 ? card[2] : "";
        return switch (path[1]) {
            case "th06" -> th06(number);
            case "th07" -> th07(card[0], number, difficulty);
            case "th08" -> th08(card[0], number);
            case "th10" -> th10(card[0], number);
            default -> null;
        };
    }

    private static String th06(int number) {
        if (number <= 2) {
            return ENTITY + "rumia";
        }
        if (number <= 6) {
            return ENTITY + "cirno";
        }
        if (number <= 13) {
            return ENTITY + "meirin";
        }
        if (number <= 31) {
            return ENTITY + "patchouli";
        }
        if (number <= 40) {
            return ENTITY + "sakuya";
        }
        if (number <= 50) {
            return ENTITY + "remilia";
        }
        if (number <= 53) {
            return ENTITY + "patchouli";
        }
        return ENTITY + "flandre";
    }

    private static String th07(String stage, int number, String difficulty) {
        return switch (stage) {
            // 一面道中霜符属于琪露诺，其余为蕾蒂。
            case "s01" -> number == 1 && difficulty.equals("hard")
                    ? ENTITY + "cirno" : ENTITY + "letty_whiterock";
            case "s02" -> ENTITY + "chen";
            case "s03" -> ENTITY + "alice";
            // 四面共用卡归三骚灵，2/3/4 为露娜萨/梅露兰/莉莉卡独奏。
            case "s04" -> switch (number) {
                case 2 -> ENTITY + "lunasa_prismriver";
                case 3 -> ENTITY + "merlin_prismriver";
                case 4 -> ENTITY + "lyrica_prismriver";
                default -> CUSTOM + "prismriver";
            };
            case "s05" -> ENTITY + "youmu";
            // 六面妖梦道中两张，其后为幽幽子。
            case "s06" -> number <= 2 ? ENTITY + "youmu" : ENTITY + "yuyuko";
            // Extra 道中橙两张，其后为八云蓝。
            case "s07" -> number <= 2 ? ENTITY + "chen" : ENTITY + "ran";
            // Phantasm 道中蓝两张，其后为八云紫。
            case "s08" -> number <= 2 ? ENTITY + "ran" : ENTITY + "yakumo_yukai";
            default -> null;
        };
    }

    private static String th08(String stage, int number) {
        return switch (stage) {
            case "s01" -> ENTITY + "wriggle_nightbug";
            case "s02" -> ENTITY + "mystia_lorelei";
            case "s03" -> ENTITY + "kamishirasawa_keine";
            case "s04a" -> ENTITY + "reimu";
            case "s04b" -> ENTITY + "marisa";
            case "s05" -> ENTITY + "reisen";
            case "s06a" -> ENTITY + "erin";
            case "s06b" -> number <= 1 ? ENTITY + "erin" : ENTITY + "houraisan_kaguya";
            case "s07" -> number <= 3 ? ENTITY + "kamishirasawa_keine" : ENTITY + "huziwara_no_mokou";
            default -> null;
        };
    }

    private static String th10(String stage, int number) {
        return switch (stage) {
            case "s01" -> number == 0 ? CUSTOM + "aki_shizuha" : CUSTOM + "aki_minoriko";
            case "s02" -> ENTITY + "kagiyama_hina";
            case "s03" -> ENTITY + "kawasiro_nitori";
            case "s04" -> ENTITY + "aya";
            case "s05" -> ENTITY + "kochiuya_sanae";
            case "s06" -> ENTITY + "yasaka_kanako";
            case "s07" -> number <= 99 ? ENTITY + "yasaka_kanako" : ENTITY + "moriya_suwako";
            default -> null;
        };
    }
}
