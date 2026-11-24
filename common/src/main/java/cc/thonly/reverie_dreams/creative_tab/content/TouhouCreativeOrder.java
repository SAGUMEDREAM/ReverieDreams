package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class TouhouCreativeOrder {
    // Order of released works, including PC-98, printed works and music CDs.
    // https://touhou-project.news/titles/
    // https://www.thwiki.cc/官方角色列表/纯文字列表
    private static final Map<String, Integer> CHARACTERS = ranks(List.of(
            // TH01, TH02, TH04, TH05. Alternate looks sit beside the original.
            "reimu cyan_reimu blue_reimu new_reimu",
            "marisa pc98_marisa kanmarisa marisa_hat",
            "kazami_yuka yuuka",
            "alice",
            // TH06, TH07, Ghostly Field Club.
            "rumia cirno smart_cirno tan_cirno meirin meiling patchouli sakuya remilia flandre flan",
            "letty_whiterock chen lily_white lunasa_prismriver merlin_prismriver lyrica_prismriver youmu yuyuko ran yakumo_yukai yukari",
            "usami_renko renko maribel_hearn",
            // TH08, TH07.5, Sangetsusei, Bohemian Archive in Japanese Red (book), TH09.
            "wriggle_nightbug mystia_lorelei mystia kamishirasawa_keine tewi reisen erin eirin houraisan_kaguya kaguya huziwara_no_mokou mokou",
            "suika melon",
            "sunny lunar star",
            "aya",
            "shikieiki_yamaxanadu eiki",
            // Silent Sinner in Blue, TH10, TH10.5, TH11, TH12.
            "watatsuki_toyohime",
            "kagiyama_hina kawasiro_nitori inubashiri_momizi momiji kochiuya_sanae sanae yasaka_kanako moriya_suwako suwako",
            "tenshi",
            "kisume kurodani_yamame mizuhashi_parsee parsee hoshiguma_yugi komeiji_satori satori kaenbyou_rin reiuji_utsuh utsuho komeiji_koishi koishi white_komeiji_koishi",
            "nazrin tatara_kogasa kogasa shou houjuu_nue nue",
            // TH12.5, Wild and Horned Hermit, TH13, Forbidden Scrollery, TH13.5.
            "hatate",
            "kasen",
            "kasodani_kyouko miyako_yoshika kaku_seiga soga_no_toziko mononobe_no_futo toyosatomimi_no_miko hutatsuiwa_mamizou",
            "kosuzu",
            "kokoro",
            // TH14, TH15, TH16, TH15.5, TH17, TH18, TH19, TH20.
            "wakasagihime sekibanki imaizumi_kagerou kijin_seijia seija sukuna_shinmyoumaru horikawa_raiko",
            "seiran ringo doremy_sweet kisin_sagume clownpiece junko hecatia_lapislazuli",
            "eternity_larva sakata_nemuno komano_aunn yatadera_narumi nishida_satono teireida_mai matara_okina",
            "joon shion cherries_shion",
            "ebisu_eika ushizaki_urumi niwatari_kutaka kitcho_yachie joutougu_mayumi haniyasushin_keiki kurokoma_saki",
            "goutokuzi_mike yamashiro_takane komakusa_sannyo tamatsukuri_misumaru kudamaki_tsukasa iizunamaru_megumu tenkyu_chimata chimata mcky himemushi_momoyo",
            "son_biten mitsugashira_enoko tenkajin_chiyari yomotsu_hisami nippaku_zanmu",
            "chirizuka_ubame houju_chimi michigami_nareko yuiman_asama iwanaga_ariya watari_nina"
    ));
    private static final Map<String, Integer> ROLE_CARDS = ranks(List.of(
            "protagonist_group koumakyou youyoumu hifuu eiyashou tasogare_furontia sangetsusei kaeizuka fuujinroku",
            "chireiden seirensen shinreibyou kishinjou kanjuden tenkuushou kikeijuu kouryuudou juuouen kinjoukyou"
    ));

    private TouhouCreativeOrder() {
    }

    static int character(Identifier id) {
        return rank(CHARACTERS, id);
    }

    static int roleCard(Identifier id) {
        return rank(ROLE_CARDS, id);
    }

    private static int rank(Map<String, Integer> order, Identifier id) {
        return id.getNamespace().equals(ReverieDreams.MOD_ID)
                ? order.getOrDefault(id.getPath(), Integer.MAX_VALUE)
                : Integer.MAX_VALUE;
    }

    private static Map<String, Integer> ranks(List<String> groups) {
        Map<String, Integer> order = new HashMap<>();
        for (String group : groups) {
            for (String id : group.split(" ")) {
                order.put(id, order.size());
            }
        }
        return Map.copyOf(order);
    }
}
