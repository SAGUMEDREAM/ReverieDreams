package cc.thonly.reverie_dreams.registry.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.npc.NPCRole;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.skin.GensokyoSkinTypes;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

@SuppressWarnings("SpellCheckingInspection")
public class NPCRoles {
    // 主角组
    public static final NPCRole REIMU = registerRole(new NPCRole(ReverieDreams.id("reimu"), GensokyoSkinTypes.REIMU));
    public static final NPCRole CYAN_REIMU = registerRole(new NPCRole(ReverieDreams.id("cyan_reimu"), GensokyoSkinTypes.CYAN_REIMU));
    public static final NPCRole MARISA = registerRole(new NPCRole(ReverieDreams.id("marisa"), GensokyoSkinTypes.MARISA));

    // 红魔乡
    public static final NPCRole RUMIA = registerRole(new NPCRole(ReverieDreams.id("rumia"), GensokyoSkinTypes.RUMIA));
    public static final NPCRole CIRNO = registerRole(new NPCRole(ReverieDreams.id("cirno"), GensokyoSkinTypes.CIRNO));
    public static final NPCRole MEIRIN = registerRole(new NPCRole(ReverieDreams.id("meirin"), GensokyoSkinTypes.HOAN_MEIRIN));
    public static final NPCRole PATCHOULI = registerRole(new NPCRole(ReverieDreams.id("patchouli"), GensokyoSkinTypes.PATCHOULI));
    public static final NPCRole SAKUYA = registerRole(new NPCRole(ReverieDreams.id("sakuya"), GensokyoSkinTypes.SAKUYA));
    public static final NPCRole REMILIA = registerRole(new NPCRole(ReverieDreams.id("remilia"), GensokyoSkinTypes.REMILA));
    public static final NPCRole FLANDRE = registerRole(new NPCRole(ReverieDreams.id("flandre"), GensokyoSkinTypes.FLANDRE));

    // 妖妖梦
    public static final NPCRole LETTY_WHITEROCK = registerRole(new NPCRole(ReverieDreams.id("letty_whiterock"), GensokyoSkinTypes.LETTY_WHITEROCK));
    public static final NPCRole CHEN = registerRole(new NPCRole(ReverieDreams.id("chen"), GensokyoSkinTypes.CHEN));
    public static final NPCRole ALICE = registerRole(new NPCRole(ReverieDreams.id("alice"), GensokyoSkinTypes.ALICE));
    public static final NPCRole LILY_WHITE = registerRole(new NPCRole(ReverieDreams.id("lily_white"), GensokyoSkinTypes.LILY_WHITE));
    public static final NPCRole LUNASA_PRISMRIVER = registerRole(new NPCRole(ReverieDreams.id("lunasa_prismriver"), GensokyoSkinTypes.LUNASA_PRISMRIVER));
    public static final NPCRole MERLIN_PRISMRIVER = registerRole(new NPCRole(ReverieDreams.id("merlin_prismriver"), GensokyoSkinTypes.MERLIN_PRISMRIVER));
    public static final NPCRole LYRICA_PRISMRIVER = registerRole(new NPCRole(ReverieDreams.id("lyrica_prismriver"), GensokyoSkinTypes.LYRICA_PRISMRIVER));
    public static final NPCRole RAN = registerRole(new NPCRole(ReverieDreams.id("ran"), GensokyoSkinTypes.RAN));
    public static final NPCRole YOUMU = registerRole(new NPCRole(ReverieDreams.id("youmu"), GensokyoSkinTypes.YOUMU));
    public static final NPCRole YUYUKO = registerRole(new NPCRole(ReverieDreams.id("yuyuko"), GensokyoSkinTypes.YUYUKO));
    public static final NPCRole YUKARI = registerRole(new NPCRole(ReverieDreams.id("yakumo_yukai"), GensokyoSkinTypes.YAKUMO_YUKAI));

    // 永夜抄
    public static final NPCRole MYSTIA_LORELEI = registerRole(new NPCRole(ReverieDreams.id("mystia_lorelei"), GensokyoSkinTypes.MYSTIA_LORELEI));
    public static final NPCRole WRIGGLE_NIGHTBUG = registerRole(new NPCRole(ReverieDreams.id("wriggle_nightbug"), GensokyoSkinTypes.WRIGGLE_NIGHTBUG));
    public static final NPCRole KAMISHIRASAWA_KEINE = registerRole(new NPCRole(ReverieDreams.id("kamishirasawa_keine"), GensokyoSkinTypes.KAMISHIRASAWA_KEINE));
    public static final NPCRole REISEN = registerRole(new NPCRole(ReverieDreams.id("reisen"), GensokyoSkinTypes.REISEN));
    public static final NPCRole ERIN = registerRole(new NPCRole(ReverieDreams.id("erin"), GensokyoSkinTypes.ERIN));
    public static final NPCRole HOURAISAN_KAGUYA = registerRole(new NPCRole(ReverieDreams.id("houraisan_kaguya"), GensokyoSkinTypes.HOURAISAN_KAGUYA));
    public static final NPCRole HUZIWARA_NO_MOKOU = registerRole(new NPCRole(ReverieDreams.id("huziwara_no_mokou"), GensokyoSkinTypes.HUZIWARA_NO_MOKOU));

    // 花映塚
    public static final NPCRole SHIKIEIKI_YAMAXANADU = registerRole(new NPCRole(ReverieDreams.id("shikieiki_yamaxanadu"), GensokyoSkinTypes.SHIKIEIKI_YAMAXANADU));
    public static final NPCRole KAZAMI_YUKA = registerRole(new NPCRole(ReverieDreams.id("kazami_yuka"), GensokyoSkinTypes.KAZAMI_YUKA));

    // 风神录
    public static final NPCRole KAGIYAMA_HINA = registerRole(new NPCRole(ReverieDreams.id("kagiyama_hina"), GensokyoSkinTypes.KAGIYAMA_HINA));
    public static final NPCRole INUBASHIRI_MOMIZI = registerRole(new NPCRole(ReverieDreams.id("inubashiri_momizi"), GensokyoSkinTypes.INUBASHIRI_MOMIZI));
    public static final NPCRole KAWASIRO_NITORI = registerRole(new NPCRole(ReverieDreams.id("kawasiro_nitori"), GensokyoSkinTypes.KAWASIRO_NITORI));
    public static final NPCRole AYA = registerRole(new NPCRole(ReverieDreams.id("aya"), GensokyoSkinTypes.AYA));
    public static final NPCRole KOCHIYA_SANAE = registerRole(new NPCRole(ReverieDreams.id("kochiuya_sanae"), GensokyoSkinTypes.KOCHIYA_SANAE));
    public static final NPCRole YASAKA_KANAKO = registerRole(new NPCRole(ReverieDreams.id("yasaka_kanako"), GensokyoSkinTypes.YASAKA_KANAKO));
    public static final NPCRole MORIYA_SUWAKO = registerRole(new NPCRole(ReverieDreams.id("moriya_suwako"), GensokyoSkinTypes.MORIYA_SUWAKO));

    // 地灵殿
    public static final NPCRole KISUME = registerRole(new NPCRole(ReverieDreams.id("kisume"), GensokyoSkinTypes.KISUME));
    public static final NPCRole KURODANI_YAMAME = registerRole(new NPCRole(ReverieDreams.id("kurodani_yamame"), GensokyoSkinTypes.KURODANI_YAMAME));
    public static final NPCRole MIZUHASHI_PARSEE = registerRole(new NPCRole(ReverieDreams.id("mizuhashi_parsee"), GensokyoSkinTypes.MIZUHASHI_PARSEE));
    public static final NPCRole HOSHIGUMA_YUGI = registerRole(new NPCRole(ReverieDreams.id("hoshiguma_yugi"), GensokyoSkinTypes.HOSHIGUMA_YUGI));
    public static final NPCRole KAENBYOU_RIN = registerRole(new NPCRole(ReverieDreams.id("kaenbyou_rin"), GensokyoSkinTypes.KAENBYOU_RIN));
    public static final NPCRole KOMEIJI_SATORI = registerRole(new NPCRole(ReverieDreams.id("komeiji_satori"), GensokyoSkinTypes.KOMEIJI_SATORI));
    public static final NPCRole REIUJI_UTSUH = registerRole(new NPCRole(ReverieDreams.id("reiuji_utsuh"), GensokyoSkinTypes.REIUJI_UTSUH));
    public static final NPCRole KOMEIJI_KOISHI = registerRole(new NPCRole(ReverieDreams.id("komeiji_koishi"), GensokyoSkinTypes.KOMEIJI_KOISHI));
    public static final NPCRole WHITE_KOMEIJI_KOISHI = registerRole(new NPCRole(ReverieDreams.id("white_komeiji_koishi"), GensokyoSkinTypes.WHITE_KOMEIJI_KOISHI));

    // 星莲船
    public static final NPCRole NAZRIN = registerRole(new NPCRole(ReverieDreams.id("nazrin"), GensokyoSkinTypes.NAZRIN));
    public static final NPCRole TATARA_KOGASA = registerRole(new NPCRole(ReverieDreams.id("tatara_kogasa"), GensokyoSkinTypes.TATARA_KOGASA));
    public static final NPCRole NUE = registerRole(new NPCRole(ReverieDreams.id("nue"), GensokyoSkinTypes.NUE));

    // 神灵庙
    public static final NPCRole KASODANI_KYOUKO = registerRole(new NPCRole(ReverieDreams.id("kasodani_kyouko"), GensokyoSkinTypes.KASODANI_KYOUKO));
    public static final NPCRole MIYAKO_YOSHIKA = registerRole(new NPCRole(ReverieDreams.id("miyako_yoshika"), GensokyoSkinTypes.MIYAKO_YOSHIKA));
    public static final NPCRole KAKU_SEIGA = registerRole(new NPCRole(ReverieDreams.id("kaku_seiga"), GensokyoSkinTypes.KAKU_SEIGA));
    public static final NPCRole SOGA_NO_TOZIKO = registerRole(new NPCRole(ReverieDreams.id("soga_no_toziko"), GensokyoSkinTypes.SOGA_NO_TOZIKO));
    public static final NPCRole MONONOBE_NO_FUTO = registerRole(new NPCRole(ReverieDreams.id("mononobe_no_futo"), GensokyoSkinTypes.MONONOBE_NO_FUTO));
    public static final NPCRole TOYOSATOMIMI_NO_MIKO = registerRole(new NPCRole(ReverieDreams.id("toyosatomimi_no_miko"), GensokyoSkinTypes.TOYOSATOMIMI_NO_MIKO));
    public static final NPCRole HOUJUU_NUE = registerRole(new NPCRole(ReverieDreams.id("houjuu_nue"), GensokyoSkinTypes.HOUJUU_NUE));
    public static final NPCRole HUTATSUIWA_MAMIZOU = registerRole(new NPCRole(ReverieDreams.id("hutatsuiwa_mamizou"), GensokyoSkinTypes.HUTATSUIWA_MAMIZOU));

    // 辉针城
    public static final NPCRole WAKASAGIHIME = registerRole(new NPCRole(ReverieDreams.id("wakasagihime"), GensokyoSkinTypes.WAKASAGIHIME));
    public static final NPCRole SEKIBANKI = registerRole(new NPCRole(ReverieDreams.id("sekibanki"), GensokyoSkinTypes.SEKIBANKI));
    public static final NPCRole IMAIZUMI_KAGEROU = registerRole(new NPCRole(ReverieDreams.id("imaizumi_kagerou"), GensokyoSkinTypes.IMAIZUMI_KAGEROU));
    public static final NPCRole KIJIN_SEIJIA = registerRole(new NPCRole(ReverieDreams.id("kijin_seijia"), GensokyoSkinTypes.KIJIN_SEIJIA));
    public static final NPCRole SUKUNA_SHINMYOUMARU = registerRole(new NPCRole(ReverieDreams.id("sukuna_shinmyoumaru"), GensokyoSkinTypes.SUKUNA_SHINMYOUMARU));
    public static final NPCRole HORIKAWA_RAIKO = registerRole(new NPCRole(ReverieDreams.id("horikawa_raiko"), GensokyoSkinTypes.HORIKAWA_RAIKO));

    // 绀珠传
    public static final NPCRole SEIRAN = registerRole(new NPCRole(ReverieDreams.id("seiran"), GensokyoSkinTypes.SEIRAN));
    public static final NPCRole RINGO = registerRole(new NPCRole(ReverieDreams.id("ringo"), GensokyoSkinTypes.RINGO));
    public static final NPCRole DOREMY_SWEET = registerRole(new NPCRole(ReverieDreams.id("doremy_sweet"), GensokyoSkinTypes.DOREMY_SWEET));
    public static final NPCRole KISIN_SAGUME = registerRole(new NPCRole(ReverieDreams.id("kisin_sagume"), GensokyoSkinTypes.KISIN_SAGUME));
    public static final NPCRole CLOWNPIECE = registerRole(new NPCRole(ReverieDreams.id("clownpiece"), GensokyoSkinTypes.CLOWNPIECE));
    public static final NPCRole JUNKO = registerRole(new NPCRole(ReverieDreams.id("junko"), GensokyoSkinTypes.JUNKO));
    public static final NPCRole HECATIA_LAPISLAZULI = registerRole(new NPCRole(ReverieDreams.id("hecatia_lapislazuli"), GensokyoSkinTypes.HECATIA_LAPISLAZULI));

    // 天空璋
    public static final NPCRole ETERNITY_LARVA = registerRole(new NPCRole(ReverieDreams.id("eternity_larva"), GensokyoSkinTypes.ETERNITY_LARVA));
    public static final NPCRole SAKUTA_NEMUNO = registerRole(new NPCRole(ReverieDreams.id("sakata_nemuno"), GensokyoSkinTypes.SAKUTA_NEMUNO));
    public static final NPCRole KOMANO_AUNN = registerRole(new NPCRole(ReverieDreams.id("komano_aunn"), GensokyoSkinTypes.KOMANO_AUNN));
    public static final NPCRole YATADERA_NARUMI = registerRole(new NPCRole(ReverieDreams.id("yatadera_narumi"), GensokyoSkinTypes.YATADERA_NARUMI));
    public static final NPCRole NISHIDA_SATONO = registerRole(new NPCRole(ReverieDreams.id("nishida_satono"), GensokyoSkinTypes.NISHIDA_SATONO));
    public static final NPCRole TEIREIDA_MAI = registerRole(new NPCRole(ReverieDreams.id("teireida_mai"), GensokyoSkinTypes.TEIREIDA_MAI));
    public static final NPCRole MATARA_OKINA = registerRole(new NPCRole(ReverieDreams.id("matara_okina"), GensokyoSkinTypes.MATARA_OKINA));

    // 鬼形兽
    public static final NPCRole EBISU_EIKA = registerRole(new NPCRole(ReverieDreams.id("ebisu_eika"), GensokyoSkinTypes.EBISU_EIKA));
    public static final NPCRole USHIZAKI_URUMI = registerRole(new NPCRole(ReverieDreams.id("ushizaki_urumi"), GensokyoSkinTypes.USHIZAKI_URUMI));
    public static final NPCRole NIWATARI_KUTAKA = registerRole(new NPCRole(ReverieDreams.id("niwatari_kutaka"), GensokyoSkinTypes.NIWATARI_KUTAKA));
    public static final NPCRole KITCHO_YACHIE = registerRole(new NPCRole(ReverieDreams.id("kitcho_yachie"), GensokyoSkinTypes.KITCHO_YACHIE));
    public static final NPCRole JOUTOUGU_MAYUMI = registerRole(new NPCRole(ReverieDreams.id("joutougu_mayumi"), GensokyoSkinTypes.JOUTOUGU_MAYUMI));
    public static final NPCRole HANIYASUSHIN_KEIKI = registerRole(new NPCRole(ReverieDreams.id("haniyasushin_keiki"), GensokyoSkinTypes.HANIYASUSHIN_KEIKI));
    public static final NPCRole KUROKOMA_SAKI = registerRole(new NPCRole(ReverieDreams.id("kurokoma_saki"), GensokyoSkinTypes.KUROKOMA_SAKI));

    // 虹龙洞
    public static final NPCRole GOUTOKUZI_MIKE = registerRole(new NPCRole(ReverieDreams.id("goutokuzi_mike"), GensokyoSkinTypes.GOUTOKUZI_MIKE));
    public static final NPCRole YAMASHIRO_TAKANE = registerRole(new NPCRole(ReverieDreams.id("yamashiro_takane"), GensokyoSkinTypes.YAMASHIRO_TAKANE));
    public static final NPCRole KOMAKUSA_SANNYO = registerRole(new NPCRole(ReverieDreams.id("komakusa_sannyo"), GensokyoSkinTypes.KOMAKUSA_SANNYO));
    public static final NPCRole TAMATSUKURI_MISUMARU = registerRole(new NPCRole(ReverieDreams.id("tamatsukuri_misumaru"), GensokyoSkinTypes.TAMATSUKURI_MISUMARU));
    public static final NPCRole KUDAMAKI_TSUKASA = registerRole(new NPCRole(ReverieDreams.id("kudamaki_tsukasa"), GensokyoSkinTypes.KUDAMAKI_TSUKASA));
    public static final NPCRole IIZUNAMARU_MEGUMU = registerRole(new NPCRole(ReverieDreams.id("iizunamaru_megumu"), GensokyoSkinTypes.IIZUNAMARU_MEGUMU));
    public static final NPCRole TENKYU_CHIMATA = registerRole(new NPCRole(ReverieDreams.id("tenkyu_chimata"), GensokyoSkinTypes.TENKYU_CHIMATA));
    public static final NPCRole HIMEMUSHI_MOMOYO = registerRole(new NPCRole(ReverieDreams.id("himemushi_momoyo"), GensokyoSkinTypes.HIMEMUSHI_MOMOYO));

    // 兽王园
    public static final NPCRole SON_BITEN = registerRole(new NPCRole(ReverieDreams.id("son_biten"), GensokyoSkinTypes.SON_BITEN));
    public static final NPCRole MITSUGASHIRA_ENOKO = registerRole(new NPCRole(ReverieDreams.id("mitsugashira_enoko"), GensokyoSkinTypes.MITSUGASHIRA_ENOKO));
    public static final NPCRole TENKAJIN_CHIYARI = registerRole(new NPCRole(ReverieDreams.id("tenkajin_chiyari"), GensokyoSkinTypes.TENKAJIN_CHIYARI));
    public static final NPCRole YOMOTSU_HISAMI = registerRole(new NPCRole(ReverieDreams.id("yomotsu_hisami"), GensokyoSkinTypes.YOMOTSU_HISAMI));
    public static final NPCRole NIPPAKU_ZANMU = registerRole(new NPCRole(ReverieDreams.id("nippaku_zanmu"), GensokyoSkinTypes.NIPPAKU_ZANMU));

    // 锦上京
    public static final NPCRole CHIRIZUKA_UBAME = registerRole(new NPCRole(ReverieDreams.id("chirizuka_ubame"), GensokyoSkinTypes.CHIRIZUKA_UBAME));
    public static final NPCRole HOUJU_CHIMI = registerRole(new NPCRole(ReverieDreams.id("houju_chimi"), GensokyoSkinTypes.HOUJU_CHIMI));
    public static final NPCRole MICHIGAMI_NAREKO = registerRole(new NPCRole(ReverieDreams.id("michigami_nareko"), GensokyoSkinTypes.MICHIGAMI_NAREKO));
    public static final NPCRole YUIMAN_ASAMA = registerRole(new NPCRole(ReverieDreams.id("yuiman_asama"), GensokyoSkinTypes.YUIMAN_ASAMA));
    public static final NPCRole WATATSUKI_TOYOHIME = registerRole(new NPCRole(ReverieDreams.id("watatsuki_toyohime"), GensokyoSkinTypes.WATATSUKI_TOYOHIME));
    public static final NPCRole IWANAGA_ARIYA = registerRole(new NPCRole(ReverieDreams.id("iwanaga_ariya"), GensokyoSkinTypes.IWANAGA_ARIYA));
    public static final NPCRole WATARI_NINA = registerRole(new NPCRole(ReverieDreams.id("watari_nina"), GensokyoSkinTypes.WATARI_NINA));

    // 三月精
    public static final NPCRole STAR = registerRole(new NPCRole(ReverieDreams.id("star"), GensokyoSkinTypes.STAR));
    public static final NPCRole LUNAR = registerRole(new NPCRole(ReverieDreams.id("lunar"), GensokyoSkinTypes.LUNAR));
    public static final NPCRole SUNNY = registerRole(new NPCRole(ReverieDreams.id("sunny"), GensokyoSkinTypes.SUNNY));

    // 秘封
    public static final NPCRole USAMI_RENKO = registerRole(new NPCRole(ReverieDreams.id("usami_renko"), GensokyoSkinTypes.USAMI_RENKO));
    public static final NPCRole MARIBEL_HEARN = registerRole(new NPCRole(ReverieDreams.id("maribel_hearn"), GensokyoSkinTypes.MARIBEL_HEARN));

    // 黄昏边境
    public static final NPCRole SUIKA = registerRole(new NPCRole(ReverieDreams.id("suika"), GensokyoSkinTypes.SUIKA));
    public static final NPCRole TENSHI = registerRole(new NPCRole(ReverieDreams.id("tenshi"), GensokyoSkinTypes.TENSHI));

    // ...

    public static void bootstrap(RegistryImpl<NPCRole> registry) {

    }

    public static NPCRole registerRole(NPCRole role) {
        return registerRole(role.getId(), role);
    }

    public static NPCRole registerRole(String name, NPCRole role) {
        return registerRole(ReverieDreams.id(name), role);
    }

    public static NPCRole registerRole(Identifier id, NPCRole role) {
        NPCRole entry = RegistryImpls.register(RegistryImpls.NPC_ROLE, id, role);
        return entry.build();
    }
}
