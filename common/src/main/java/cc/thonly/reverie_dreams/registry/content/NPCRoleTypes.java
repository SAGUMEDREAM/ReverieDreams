package cc.thonly.reverie_dreams.registry.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.npc.NPCRoleType;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.skin.GensokyoSkinTypes;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import net.minecraft.resources.Identifier;

@SuppressWarnings("SpellCheckingInspection")
public class NPCRoleTypes {
    // 主角组
    public static final NPCRoleType REIMU = registerRole(new NPCRoleType(ReverieDreams.id("reimu"), GensokyoSkinTypes.REIMU));
    public static final NPCRoleType CYAN_REIMU = registerRole(new NPCRoleType(ReverieDreams.id("cyan_reimu"), GensokyoSkinTypes.CYAN_REIMU));
    public static final NPCRoleType MARISA = registerRole(new NPCRoleType(ReverieDreams.id("marisa"), GensokyoSkinTypes.MARISA));

    // 红魔乡
    public static final NPCRoleType RUMIA = registerRole(new NPCRoleType(ReverieDreams.id("rumia"), GensokyoSkinTypes.RUMIA));
    public static final NPCRoleType CIRNO = registerRole(new NPCRoleType(ReverieDreams.id("cirno"), GensokyoSkinTypes.CIRNO));
    public static final NPCRoleType MEIRIN = registerRole(new NPCRoleType(ReverieDreams.id("meirin"), GensokyoSkinTypes.HOAN_MEIRIN));
    public static final NPCRoleType PATCHOULI = registerRole(new NPCRoleType(ReverieDreams.id("patchouli"), GensokyoSkinTypes.PATCHOULI));
    public static final NPCRoleType SAKUYA = registerRole(new NPCRoleType(ReverieDreams.id("sakuya"), GensokyoSkinTypes.SAKUYA));
    public static final NPCRoleType REMILIA = registerRole(new NPCRoleType(ReverieDreams.id("remilia"), GensokyoSkinTypes.REMILA));
    public static final NPCRoleType FLANDRE = registerRole(new NPCRoleType(ReverieDreams.id("flandre"), GensokyoSkinTypes.FLANDRE));

    // 妖妖梦
    public static final NPCRoleType LETTY_WHITEROCK = registerRole(new NPCRoleType(ReverieDreams.id("letty_whiterock"), GensokyoSkinTypes.LETTY_WHITEROCK));
    public static final NPCRoleType CHEN = registerRole(new NPCRoleType(ReverieDreams.id("chen"), GensokyoSkinTypes.CHEN));
    public static final NPCRoleType ALICE = registerRole(new NPCRoleType(ReverieDreams.id("alice"), GensokyoSkinTypes.ALICE));
    public static final NPCRoleType LILY_WHITE = registerRole(new NPCRoleType(ReverieDreams.id("lily_white"), GensokyoSkinTypes.LILY_WHITE));
    public static final NPCRoleType LUNASA_PRISMRIVER = registerRole(new NPCRoleType(ReverieDreams.id("lunasa_prismriver"), GensokyoSkinTypes.LUNASA_PRISMRIVER));
    public static final NPCRoleType MERLIN_PRISMRIVER = registerRole(new NPCRoleType(ReverieDreams.id("merlin_prismriver"), GensokyoSkinTypes.MERLIN_PRISMRIVER));
    public static final NPCRoleType LYRICA_PRISMRIVER = registerRole(new NPCRoleType(ReverieDreams.id("lyrica_prismriver"), GensokyoSkinTypes.LYRICA_PRISMRIVER));
    public static final NPCRoleType RAN = registerRole(new NPCRoleType(ReverieDreams.id("ran"), GensokyoSkinTypes.RAN));
    public static final NPCRoleType YOUMU = registerRole(new NPCRoleType(ReverieDreams.id("youmu"), GensokyoSkinTypes.YOUMU));
    public static final NPCRoleType YUYUKO = registerRole(new NPCRoleType(ReverieDreams.id("yuyuko"), GensokyoSkinTypes.YUYUKO));
    public static final NPCRoleType YUKARI = registerRole(new NPCRoleType(ReverieDreams.id("yakumo_yukai"), GensokyoSkinTypes.YAKUMO_YUKAI));

    // 永夜抄
    public static final NPCRoleType MYSTIA_LORELEI = registerRole(new NPCRoleType(ReverieDreams.id("mystia_lorelei"), GensokyoSkinTypes.MYSTIA_LORELEI));
    public static final NPCRoleType WRIGGLE_NIGHTBUG = registerRole(new NPCRoleType(ReverieDreams.id("wriggle_nightbug"), GensokyoSkinTypes.WRIGGLE_NIGHTBUG));
    public static final NPCRoleType KAMISHIRASAWA_KEINE = registerRole(new NPCRoleType(ReverieDreams.id("kamishirasawa_keine"), GensokyoSkinTypes.KAMISHIRASAWA_KEINE));
    public static final NPCRoleType REISEN = registerRole(new NPCRoleType(ReverieDreams.id("reisen"), GensokyoSkinTypes.REISEN));
    public static final NPCRoleType ERIN = registerRole(new NPCRoleType(ReverieDreams.id("erin"), GensokyoSkinTypes.ERIN));
    public static final NPCRoleType HOURAISAN_KAGUYA = registerRole(new NPCRoleType(ReverieDreams.id("houraisan_kaguya"), GensokyoSkinTypes.HOURAISAN_KAGUYA));
    public static final NPCRoleType HUZIWARA_NO_MOKOU = registerRole(new NPCRoleType(ReverieDreams.id("huziwara_no_mokou"), GensokyoSkinTypes.HUZIWARA_NO_MOKOU));

    // 花映塚
    public static final NPCRoleType SHIKIEIKI_YAMAXANADU = registerRole(new NPCRoleType(ReverieDreams.id("shikieiki_yamaxanadu"), GensokyoSkinTypes.SHIKIEIKI_YAMAXANADU));
    public static final NPCRoleType KAZAMI_YUKA = registerRole(new NPCRoleType(ReverieDreams.id("kazami_yuka"), GensokyoSkinTypes.KAZAMI_YUKA));

    // 风神录
    public static final NPCRoleType KAGIYAMA_HINA = registerRole(new NPCRoleType(ReverieDreams.id("kagiyama_hina"), GensokyoSkinTypes.KAGIYAMA_HINA));
    public static final NPCRoleType INUBASHIRI_MOMIZI = registerRole(new NPCRoleType(ReverieDreams.id("inubashiri_momizi"), GensokyoSkinTypes.INUBASHIRI_MOMIZI));
    public static final NPCRoleType KAWASIRO_NITORI = registerRole(new NPCRoleType(ReverieDreams.id("kawasiro_nitori"), GensokyoSkinTypes.KAWASIRO_NITORI));
    public static final NPCRoleType AYA = registerRole(new NPCRoleType(ReverieDreams.id("aya"), GensokyoSkinTypes.AYA));
    public static final NPCRoleType KOCHIYA_SANAE = registerRole(new NPCRoleType(ReverieDreams.id("kochiuya_sanae"), GensokyoSkinTypes.KOCHIYA_SANAE));
    public static final NPCRoleType YASAKA_KANAKO = registerRole(new NPCRoleType(ReverieDreams.id("yasaka_kanako"), GensokyoSkinTypes.YASAKA_KANAKO));
    public static final NPCRoleType MORIYA_SUWAKO = registerRole(new NPCRoleType(ReverieDreams.id("moriya_suwako"), GensokyoSkinTypes.MORIYA_SUWAKO));

    // 地灵殿
    public static final NPCRoleType KISUME = registerRole(new NPCRoleType(ReverieDreams.id("kisume"), GensokyoSkinTypes.KISUME));
    public static final NPCRoleType KURODANI_YAMAME = registerRole(new NPCRoleType(ReverieDreams.id("kurodani_yamame"), GensokyoSkinTypes.KURODANI_YAMAME));
    public static final NPCRoleType MIZUHASHI_PARSEE = registerRole(new NPCRoleType(ReverieDreams.id("mizuhashi_parsee"), GensokyoSkinTypes.MIZUHASHI_PARSEE));
    public static final NPCRoleType HOSHIGUMA_YUGI = registerRole(new NPCRoleType(ReverieDreams.id("hoshiguma_yugi"), GensokyoSkinTypes.HOSHIGUMA_YUGI));
    public static final NPCRoleType KAENBYOU_RIN = registerRole(new NPCRoleType(ReverieDreams.id("kaenbyou_rin"), GensokyoSkinTypes.KAENBYOU_RIN));
    public static final NPCRoleType KOMEIJI_SATORI = registerRole(new NPCRoleType(ReverieDreams.id("komeiji_satori"), GensokyoSkinTypes.KOMEIJI_SATORI));
    public static final NPCRoleType REIUJI_UTSUH = registerRole(new NPCRoleType(ReverieDreams.id("reiuji_utsuh"), GensokyoSkinTypes.REIUJI_UTSUH));
    public static final NPCRoleType KOMEIJI_KOISHI = registerRole(new NPCRoleType(ReverieDreams.id("komeiji_koishi"), GensokyoSkinTypes.KOMEIJI_KOISHI));
    public static final NPCRoleType WHITE_KOMEIJI_KOISHI = registerRole(new NPCRoleType(ReverieDreams.id("white_komeiji_koishi"), GensokyoSkinTypes.WHITE_KOMEIJI_KOISHI));

    // 星莲船
    public static final NPCRoleType NAZRIN = registerRole(new NPCRoleType(ReverieDreams.id("nazrin"), GensokyoSkinTypes.NAZRIN));
    public static final NPCRoleType TATARA_KOGASA = registerRole(new NPCRoleType(ReverieDreams.id("tatara_kogasa"), GensokyoSkinTypes.TATARA_KOGASA));
    public static final NPCRoleType HOUJUU_NUE = registerRole(new NPCRoleType(ReverieDreams.id("houjuu_nue"), GensokyoSkinTypes.HOUJUU_NUE));

    // 神灵庙
    public static final NPCRoleType KASODANI_KYOUKO = registerRole(new NPCRoleType(ReverieDreams.id("kasodani_kyouko"), GensokyoSkinTypes.KASODANI_KYOUKO));
    public static final NPCRoleType MIYAKO_YOSHIKA = registerRole(new NPCRoleType(ReverieDreams.id("miyako_yoshika"), GensokyoSkinTypes.MIYAKO_YOSHIKA));
    public static final NPCRoleType KAKU_SEIGA = registerRole(new NPCRoleType(ReverieDreams.id("kaku_seiga"), GensokyoSkinTypes.KAKU_SEIGA));
    public static final NPCRoleType SOGA_NO_TOZIKO = registerRole(new NPCRoleType(ReverieDreams.id("soga_no_toziko"), GensokyoSkinTypes.SOGA_NO_TOZIKO));
    public static final NPCRoleType MONONOBE_NO_FUTO = registerRole(new NPCRoleType(ReverieDreams.id("mononobe_no_futo"), GensokyoSkinTypes.MONONOBE_NO_FUTO));
    public static final NPCRoleType TOYOSATOMIMI_NO_MIKO = registerRole(new NPCRoleType(ReverieDreams.id("toyosatomimi_no_miko"), GensokyoSkinTypes.TOYOSATOMIMI_NO_MIKO));
    public static final NPCRoleType HUTATSUIWA_MAMIZOU = registerRole(new NPCRoleType(ReverieDreams.id("hutatsuiwa_mamizou"), GensokyoSkinTypes.HUTATSUIWA_MAMIZOU));

    // 辉针城
    public static final NPCRoleType WAKASAGIHIME = registerRole(new NPCRoleType(ReverieDreams.id("wakasagihime"), GensokyoSkinTypes.WAKASAGIHIME));
    public static final NPCRoleType SEKIBANKI = registerRole(new NPCRoleType(ReverieDreams.id("sekibanki"), GensokyoSkinTypes.SEKIBANKI));
    public static final NPCRoleType IMAIZUMI_KAGEROU = registerRole(new NPCRoleType(ReverieDreams.id("imaizumi_kagerou"), GensokyoSkinTypes.IMAIZUMI_KAGEROU));
    public static final NPCRoleType KIJIN_SEIJIA = registerRole(new NPCRoleType(ReverieDreams.id("kijin_seijia"), GensokyoSkinTypes.KIJIN_SEIJIA));
    public static final NPCRoleType SUKUNA_SHINMYOUMARU = registerRole(new NPCRoleType(ReverieDreams.id("sukuna_shinmyoumaru"), GensokyoSkinTypes.SUKUNA_SHINMYOUMARU));
    public static final NPCRoleType HORIKAWA_RAIKO = registerRole(new NPCRoleType(ReverieDreams.id("horikawa_raiko"), GensokyoSkinTypes.HORIKAWA_RAIKO));

    // 绀珠传
    public static final NPCRoleType SEIRAN = registerRole(new NPCRoleType(ReverieDreams.id("seiran"), GensokyoSkinTypes.SEIRAN));
    public static final NPCRoleType RINGO = registerRole(new NPCRoleType(ReverieDreams.id("ringo"), GensokyoSkinTypes.RINGO));
    public static final NPCRoleType DOREMY_SWEET = registerRole(new NPCRoleType(ReverieDreams.id("doremy_sweet"), GensokyoSkinTypes.DOREMY_SWEET));
    public static final NPCRoleType KISIN_SAGUME = registerRole(new NPCRoleType(ReverieDreams.id("kisin_sagume"), GensokyoSkinTypes.KISIN_SAGUME));
    public static final NPCRoleType CLOWNPIECE = registerRole(new NPCRoleType(ReverieDreams.id("clownpiece"), GensokyoSkinTypes.CLOWNPIECE));
    public static final NPCRoleType JUNKO = registerRole(new NPCRoleType(ReverieDreams.id("junko"), GensokyoSkinTypes.JUNKO));
    public static final NPCRoleType HECATIA_LAPISLAZULI = registerRole(new NPCRoleType(ReverieDreams.id("hecatia_lapislazuli"), GensokyoSkinTypes.HECATIA_LAPISLAZULI));

    // 天空璋
    public static final NPCRoleType ETERNITY_LARVA = registerRole(new NPCRoleType(ReverieDreams.id("eternity_larva"), GensokyoSkinTypes.ETERNITY_LARVA));
    public static final NPCRoleType SAKUTA_NEMUNO = registerRole(new NPCRoleType(ReverieDreams.id("sakata_nemuno"), GensokyoSkinTypes.SAKUTA_NEMUNO));
    public static final NPCRoleType KOMANO_AUNN = registerRole(new NPCRoleType(ReverieDreams.id("komano_aunn"), GensokyoSkinTypes.KOMANO_AUNN));
    public static final NPCRoleType YATADERA_NARUMI = registerRole(new NPCRoleType(ReverieDreams.id("yatadera_narumi"), GensokyoSkinTypes.YATADERA_NARUMI));
    public static final NPCRoleType NISHIDA_SATONO = registerRole(new NPCRoleType(ReverieDreams.id("nishida_satono"), GensokyoSkinTypes.NISHIDA_SATONO));
    public static final NPCRoleType TEIREIDA_MAI = registerRole(new NPCRoleType(ReverieDreams.id("teireida_mai"), GensokyoSkinTypes.TEIREIDA_MAI));
    public static final NPCRoleType MATARA_OKINA = registerRole(new NPCRoleType(ReverieDreams.id("matara_okina"), GensokyoSkinTypes.MATARA_OKINA));

    // 鬼形兽
    public static final NPCRoleType EBISU_EIKA = registerRole(new NPCRoleType(ReverieDreams.id("ebisu_eika"), GensokyoSkinTypes.EBISU_EIKA));
    public static final NPCRoleType USHIZAKI_URUMI = registerRole(new NPCRoleType(ReverieDreams.id("ushizaki_urumi"), GensokyoSkinTypes.USHIZAKI_URUMI));
    public static final NPCRoleType NIWATARI_KUTAKA = registerRole(new NPCRoleType(ReverieDreams.id("niwatari_kutaka"), GensokyoSkinTypes.NIWATARI_KUTAKA));
    public static final NPCRoleType KITCHO_YACHIE = registerRole(new NPCRoleType(ReverieDreams.id("kitcho_yachie"), GensokyoSkinTypes.KITCHO_YACHIE));
    public static final NPCRoleType JOUTOUGU_MAYUMI = registerRole(new NPCRoleType(ReverieDreams.id("joutougu_mayumi"), GensokyoSkinTypes.JOUTOUGU_MAYUMI));
    public static final NPCRoleType HANIYASUSHIN_KEIKI = registerRole(new NPCRoleType(ReverieDreams.id("haniyasushin_keiki"), GensokyoSkinTypes.HANIYASUSHIN_KEIKI));
    public static final NPCRoleType KUROKOMA_SAKI = registerRole(new NPCRoleType(ReverieDreams.id("kurokoma_saki"), GensokyoSkinTypes.KUROKOMA_SAKI));

    // 虹龙洞
    public static final NPCRoleType GOUTOKUZI_MIKE = registerRole(new NPCRoleType(ReverieDreams.id("goutokuzi_mike"), GensokyoSkinTypes.GOUTOKUZI_MIKE));
    public static final NPCRoleType YAMASHIRO_TAKANE = registerRole(new NPCRoleType(ReverieDreams.id("yamashiro_takane"), GensokyoSkinTypes.YAMASHIRO_TAKANE));
    public static final NPCRoleType KOMAKUSA_SANNYO = registerRole(new NPCRoleType(ReverieDreams.id("komakusa_sannyo"), GensokyoSkinTypes.KOMAKUSA_SANNYO));
    public static final NPCRoleType TAMATSUKURI_MISUMARU = registerRole(new NPCRoleType(ReverieDreams.id("tamatsukuri_misumaru"), GensokyoSkinTypes.TAMATSUKURI_MISUMARU));
    public static final NPCRoleType KUDAMAKI_TSUKASA = registerRole(new NPCRoleType(ReverieDreams.id("kudamaki_tsukasa"), GensokyoSkinTypes.KUDAMAKI_TSUKASA));
    public static final NPCRoleType IIZUNAMARU_MEGUMU = registerRole(new NPCRoleType(ReverieDreams.id("iizunamaru_megumu"), GensokyoSkinTypes.IIZUNAMARU_MEGUMU));
    public static final NPCRoleType TENKYU_CHIMATA = registerRole(new NPCRoleType(ReverieDreams.id("tenkyu_chimata"), GensokyoSkinTypes.TENKYU_CHIMATA));
    public static final NPCRoleType HIMEMUSHI_MOMOYO = registerRole(new NPCRoleType(ReverieDreams.id("himemushi_momoyo"), GensokyoSkinTypes.HIMEMUSHI_MOMOYO));

    // 兽王园
    public static final NPCRoleType SON_BITEN = registerRole(new NPCRoleType(ReverieDreams.id("son_biten"), GensokyoSkinTypes.SON_BITEN));
    public static final NPCRoleType MITSUGASHIRA_ENOKO = registerRole(new NPCRoleType(ReverieDreams.id("mitsugashira_enoko"), GensokyoSkinTypes.MITSUGASHIRA_ENOKO));
    public static final NPCRoleType TENKAJIN_CHIYARI = registerRole(new NPCRoleType(ReverieDreams.id("tenkajin_chiyari"), GensokyoSkinTypes.TENKAJIN_CHIYARI));
    public static final NPCRoleType YOMOTSU_HISAMI = registerRole(new NPCRoleType(ReverieDreams.id("yomotsu_hisami"), GensokyoSkinTypes.YOMOTSU_HISAMI));
    public static final NPCRoleType NIPPAKU_ZANMU = registerRole(new NPCRoleType(ReverieDreams.id("nippaku_zanmu"), GensokyoSkinTypes.NIPPAKU_ZANMU));

    // 锦上京
    public static final NPCRoleType CHIRIZUKA_UBAME = registerRole(new NPCRoleType(ReverieDreams.id("chirizuka_ubame"), GensokyoSkinTypes.CHIRIZUKA_UBAME));
    public static final NPCRoleType HOUJU_CHIMI = registerRole(new NPCRoleType(ReverieDreams.id("houju_chimi"), GensokyoSkinTypes.HOUJU_CHIMI));
    public static final NPCRoleType MICHIGAMI_NAREKO = registerRole(new NPCRoleType(ReverieDreams.id("michigami_nareko"), GensokyoSkinTypes.MICHIGAMI_NAREKO));
    public static final NPCRoleType YUIMAN_ASAMA = registerRole(new NPCRoleType(ReverieDreams.id("yuiman_asama"), GensokyoSkinTypes.YUIMAN_ASAMA));
    public static final NPCRoleType WATATSUKI_TOYOHIME = registerRole(new NPCRoleType(ReverieDreams.id("watatsuki_toyohime"), GensokyoSkinTypes.WATATSUKI_TOYOHIME));
    public static final NPCRoleType IWANAGA_ARIYA = registerRole(new NPCRoleType(ReverieDreams.id("iwanaga_ariya"), GensokyoSkinTypes.IWANAGA_ARIYA));
    public static final NPCRoleType WATARI_NINA = registerRole(new NPCRoleType(ReverieDreams.id("watari_nina"), GensokyoSkinTypes.WATARI_NINA));

    // 三月精
    public static final NPCRoleType STAR = registerRole(new NPCRoleType(ReverieDreams.id("star"), GensokyoSkinTypes.STAR));
    public static final NPCRoleType LUNAR = registerRole(new NPCRoleType(ReverieDreams.id("lunar"), GensokyoSkinTypes.LUNAR));
    public static final NPCRoleType SUNNY = registerRole(new NPCRoleType(ReverieDreams.id("sunny"), GensokyoSkinTypes.SUNNY));

    // 秘封
    public static final NPCRoleType USAMI_RENKO = registerRole(new NPCRoleType(ReverieDreams.id("usami_renko"), GensokyoSkinTypes.USAMI_RENKO));
    public static final NPCRoleType MARIBEL_HEARN = registerRole(new NPCRoleType(ReverieDreams.id("maribel_hearn"), GensokyoSkinTypes.MARIBEL_HEARN));

    // 黄昏边境
    public static final NPCRoleType SUIKA = registerRole(new NPCRoleType(ReverieDreams.id("suika"), GensokyoSkinTypes.SUIKA));
    public static final NPCRoleType TENSHI = registerRole(new NPCRoleType(ReverieDreams.id("tenshi"), GensokyoSkinTypes.TENSHI));

    // ...

    public static void bootstrap(RegistryProvider<NPCRoleType> registry) {
        registry.addAliaName(ReverieDreams.id("nue"), ReverieDreams.id("houjuu_nue"));
    }

    public static NPCRoleType registerRole(NPCRoleType role) {
        return registerRole(role.getId(), role);
    }

    public static NPCRoleType registerRole(String name, NPCRoleType role) {
        return registerRole(ReverieDreams.id(name), role);
    }

    public static NPCRoleType registerRole(Identifier id, NPCRoleType role) {
        NPCRoleType entry = BuiltInRegistryProviders.register(BuiltInRegistryProviders.NPC_ROLE_TYPE, id, role);
        return entry.build();
    }
}
