package cc.thonly.reverie_dreams.item;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.npc.NPCRoles;
import cc.thonly.reverie_dreams.item.builder.RoleCard;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class RoleCards {
    private static final IntrinsicalRegister<RoleCard> REGISTER = RegistryManager.ROLE_CARD;
    public static final RoleCard PROTAGONIST_GROUP = register(new RoleCard(ReverieDreams.id("protagonist_group"),
            16727357L,
            List.of(NPCRoles.REIMU,
                    NPCRoles.CYAN_REIMU,
                    NPCRoles.MARISA)
    ).build());
    public static final RoleCard KOUMAKYOU = register(new RoleCard(ReverieDreams.id("koumakyou"),
            16716820L,
            List.of(NPCRoles.RUMIA,
                    NPCRoles.CIRNO,
                    NPCRoles.MEIRIN,
                    NPCRoles.PATCHOULI,
                    NPCRoles.SAKUYA,
                    NPCRoles.REMILIA,
                    NPCRoles.FLANDRE)
    ).build());
    public static final RoleCard YOUYOUMU = register(new RoleCard(ReverieDreams.id("youyoumu"),
            16717035L,
            List.of(NPCRoles.LETTY_WHITEROCK,
                    NPCRoles.CHEN,
                    NPCRoles.ALICE,
                    NPCRoles.LILY_WHITE,
                    NPCRoles.LUNASA_PRISMRIVER,
                    NPCRoles.MERLIN_PRISMRIVER,
                    NPCRoles.LYRICA_PRISMRIVER,
                    NPCRoles.RAN,
                    NPCRoles.YOUMU,
                    NPCRoles.YUYUKO,
                    NPCRoles.YUKARI)
    ).build());
    public static final RoleCard EIYASHOU = register(new RoleCard(ReverieDreams.id("eiyashou"),
            6160593L,
            List.of(NPCRoles.MYSTIA_LORELEI,
                    NPCRoles.WRIGGLE_NIGHTBUG,
                    NPCRoles.KAMISHIRASAWA_KEINE,
                    NPCRoles.REISEN,
                    NPCRoles.ERIN,
                    NPCRoles.HOURAISAN_KAGUYA,
                    NPCRoles.HUZIWARA_NO_MOKOU)
    ).build());
    public static final RoleCard KAEIZUKA = register(new RoleCard(ReverieDreams.id("kaeizuka"),
            53595L,
            List.of(NPCRoles.SHIKIEIKI_YAMAXANADU,
                    NPCRoles.KAZAMI_YUKA)
    ).build());
    public static final RoleCard FUUJINROKU = register(new RoleCard(ReverieDreams.id("fuujinroku"),
            6934784L,
            List.of(NPCRoles.KAGIYAMA_HINA,
                    NPCRoles.INUBASHIRI_MOMIZI,
                    NPCRoles.KAWASIRO_NITORI,
                    NPCRoles.AYA,
                    NPCRoles.KOCHIYA_SANAE,
                    NPCRoles.YASAKA_KANAKO,
                    NPCRoles.MORIYA_SUWAKO)
    ).build());
    public static final RoleCard CHIREIDEN = register(new RoleCard(ReverieDreams.id("chireiden"),
            41777L,
            List.of(NPCRoles.KISUME,
                    NPCRoles.KURODANI_YAMAME,
                    NPCRoles.MIZUHASHI_PARSEE,
                    NPCRoles.HOSHIGUMA_YUGI,
                    NPCRoles.KAENBYOU_RIN,
                    NPCRoles.KOMEIJI_SATORI,
                    NPCRoles.REIUJI_UTSUH,
                    NPCRoles.KOMEIJI_KOISHI,
                    NPCRoles.WHITE_KOMEIJI_KOISHI)
    ).build());
    public static final RoleCard SEIRENSEN = register(new RoleCard(ReverieDreams.id("seirensen"),
            1506915L,
            List.of(NPCRoles.NAZRIN,
                    NPCRoles.TATARA_KOGASA,
                    NPCRoles.NUE)
    ).build());
    public static final RoleCard SHINREIBYOU = register(new RoleCard(ReverieDreams.id("shinreibyou"),
            16775603L,
            List.of(NPCRoles.KASODANI_KYOUKO,
                    NPCRoles.MIYAKO_YOSHIKA,
                    NPCRoles.KAKU_SEIGA,
                    NPCRoles.SOGA_NO_TOZIKO,
                    NPCRoles.MONONOBE_NO_FUTO,
                    NPCRoles.TOYOSATOMIMI_NO_MIKO,
                    NPCRoles.HOUJUU_NUE,
                    NPCRoles.HUTATSUIWA_MAMIZOU)
    ).build());
    public static final RoleCard KISHINJOU = register(new RoleCard(ReverieDreams.id("kishinjou"),
            16761692L,
            List.of(
                    NPCRoles.WAKASAGIHIME,
                    NPCRoles.SEKIBANKI,
                    NPCRoles.IMAIZUMI_KAGEROU,
                    NPCRoles.KIJIN_SEIJIA,
                    NPCRoles.SUKUNA_SHINMYOUMARU,
                    NPCRoles.HORIKAWA_RAIKO
            )
    ).build());
    public static final RoleCard KANJUDEN = register(new RoleCard(ReverieDreams.id("kanjuden"),
            14024704L,
            List.of(
                    NPCRoles.SEIRAN,
                    NPCRoles.RINGO,
                    NPCRoles.DOREMY_SWEET,
                    NPCRoles.KISIN_SAGUME,
                    NPCRoles.CLOWNPIECE,
                    NPCRoles.JUNKO,
                    NPCRoles.HECATIA_LAPISLAZULI
            )
    ).build());
    public static final RoleCard TENKUUSHOU = register(new RoleCard(ReverieDreams.id("tenkuushou"),
            6750023L,
            List.of(
                    NPCRoles.ETERNITY_LARVA,
                    NPCRoles.SAKUTA_NEMUNO,
                    NPCRoles.KOMANO_AUNN,
                    NPCRoles.YATADERA_NARUMI,
                    NPCRoles.NISHIDA_SATONO,
                    NPCRoles.TEIREIDA_MAI,
                    NPCRoles.MATARA_OKINA
            )
    ).build());
    public static final RoleCard KIKEIJUU = register(new RoleCard(ReverieDreams.id("kikeijuu"),
            12386304L,
            List.of(
                    NPCRoles.EBISU_EIKA,
                    NPCRoles.USHIZAKI_URUMI,
                    NPCRoles.NIWATARI_KUTAKA,
                    NPCRoles.KITCHO_YACHIE,
                    NPCRoles.JOUTOUGU_MAYUMI,
                    NPCRoles.HANIYASUSHIN_KEIKI,
                    NPCRoles.KUROKOMA_SAKI
            )
    ).build());
    public static final RoleCard KOURYUUDOU = register(new RoleCard(ReverieDreams.id("kouryuudou"),
            15853109L,
            List.of(
                    NPCRoles.GOUTOKUZI_MIKE,
                    NPCRoles.YAMASHIRO_TAKANE,
                    NPCRoles.KOMAKUSA_SANNYO,
                    NPCRoles.TAMATSUKURI_MISUMARU,
                    NPCRoles.KUDAMAKI_TSUKASA,
                    NPCRoles.IIZUNAMARU_MEGUMU,
                    NPCRoles.TENKYU_CHIMATA,
                    NPCRoles.HIMEMUSHI_MOMOYO
            )
    ).build());
    public static final RoleCard JUUOUEN = register(new RoleCard(ReverieDreams.id("juuouen"),
            7252587L,
            List.of()
    ).build());
    public static final RoleCard KINJOUKYOU = register(new RoleCard(ReverieDreams.id("kinjoukyou"),
            12969971L,
            List.of()
    ).build());

    // 其他作品
    public static final RoleCard SANGETSUSEI = register(new RoleCard(ReverieDreams.id("sangetsusei"),
            16770140L,
            List.of(
                    NPCRoles.STAR,
                    NPCRoles.LUNAR,
                    NPCRoles.SUNNY
            )
    ).build());
    public static final RoleCard HIFUU = register(new RoleCard(ReverieDreams.id("hifuu"),
            8931582L,
            List.of(
                    NPCRoles.USAMI_RENKO,
                    NPCRoles.MARIBEL_HEARN
            )
    ).build());
    public static final RoleCard TASOGARE_FURONTIA = register(new RoleCard(ReverieDreams.id("tasogare_furontia"),
            16683336L,
            List.of(
                    NPCRoles.SUIKA,
                    NPCRoles.TENSHI
            )
    ).build());

    public static RoleCard register(RoleCard roleCard) {
        return register(roleCard.getId(), roleCard);
    }

    public static RoleCard register(String name, RoleCard roleCard) {
        return register(ReverieDreams.id(name), roleCard);
    }

    public static RoleCard register(ResourceLocation key, RoleCard roleCard) {
        return RegistryManager.register(REGISTER, key, roleCard);
    }

    public static void bootstrap(IntrinsicalRegister<RoleCard> registry) {

    }
}
