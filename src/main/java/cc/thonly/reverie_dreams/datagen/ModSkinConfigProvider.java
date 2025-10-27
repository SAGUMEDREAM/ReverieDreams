package cc.thonly.reverie_dreams.datagen;

import cc.thonly.reverie_dreams.datagen.generator.SkinConfigProvider;
import cc.thonly.reverie_dreams.entity.skin.GensokyoSkinTypes;
import cc.thonly.reverie_dreams.entity.skin.MobSkinTypes;
import cc.thonly.reverie_dreams.entity.skin.SkinConfig;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.registry.RegistryWrapper;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 *
 * 1.21.9+
 *
 **/

public class ModSkinConfigProvider extends SkinConfigProvider {

    public ModSkinConfigProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> future) {
        super(output, future);
    }

    @Override
    public void configured() {
        this.addConfig(GensokyoSkinTypes.REIMU, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.CYAN_REIMU, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.MARISA, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.RUMIA, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.CIRNO, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.HOAN_MEIRIN, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.PATCHOULI, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.SAKUYA, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.REMILA, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.FLANDRE, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.LETTY_WHITEROCK, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.CHEN, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.ALICE, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.LILY_WHITE, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.LUNASA_PRISMRIVER, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.MERLIN_PRISMRIVER, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.LYRICA_PRISMRIVER, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.RAN, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.YOUMU, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.YUYUKO, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.YAKUMO_YUKAI, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.USAMI_RENKO, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.MARIBEL_HEARN, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.MYSTIA_LORELEI, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.WRIGGLE_NIGHTBUG, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.KAMISHIRASAWA_KEINE, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.REISEN, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.ERIN, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.HOURAISAN_KAGUYA, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.HUZIWARA_NO_MOKOU, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.SHIKIEIKI_YAMAXANADU, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.KAZAMI_YUKA, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.KAGIYAMA_HINA, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.INUBASHIRI_MOMIZI, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.KAWASIRO_NITORI, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.AYA, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.KOCHIYA_SANAE, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.YASAKA_KANAKO, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.MORIYA_SUWAKO, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.KISUME, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.KURODANI_YAMAME, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.MIZUHASHI_PARSEE, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.HOSHIGUMA_YUGI, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.KAENBYOU_RIN, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.KOMEIJI_SATORI, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.REIUJI_UTSUH, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.KOMEIJI_KOISHI, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.WHITE_KOMEIJI_KOISHI, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.NAZRIN, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.NUE, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.TATARA_KOGASA, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.KASODANI_KYOUKO, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.MIYAKO_YOSHIKA, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.KAKU_SEIGA, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.SOGA_NO_TOZIKO, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.MONONOBE_NO_FUTO, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.TOYOSATOMIMI_NO_MIKO, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.HOUJUU_NUE, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.HUTATSUIWA_MAMIZOU, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.WAKASAGIHIME, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.SEKIBANKI, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.IMAIZUMI_KAGEROU, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.KIJIN_SEIJIA, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.SUKUNA_SHINMYOUMARU, new SkinConfig(SkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.HORIKAWA_RAIKO, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.SEIRAN, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.RINGO, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.DOREMY_SWEET, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.KISIN_SAGUME, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.CLOWNPIECE, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.JUNKO, new SkinConfig(SkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.HECATIA_LAPISLAZULI, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.ETERNITY_LARVA, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.SAKUTA_NEMUNO, new SkinConfig(SkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.KOMANO_AUNN, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.YATADERA_NARUMI, new SkinConfig(SkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.NISHIDA_SATONO, new SkinConfig(SkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.TEIREIDA_MAI, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.MATARA_OKINA, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.EBISU_EIKA, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.USHIZAKI_URUMI, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.NIWATARI_KUTAKA, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.KITCHO_YACHIE, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.JOUTOUGU_MAYUMI, new SkinConfig(SkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.HANIYASUSHIN_KEIKI, new SkinConfig(SkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.KUROKOMA_SAKI, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.GOUTOKUZI_MIKE, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.YAMASHIRO_TAKANE, new SkinConfig(SkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.KOMAKUSA_SANNYO, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.TAMATSUKURI_MISUMARU, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.KUDAMAKI_TSUKASA, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.IIZUNAMARU_MEGUMU, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.TENKYU_CHIMATA, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.HIMEMUSHI_MOMOYO, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.SUIKA, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.TENSHI, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.STAR, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.SUNNY, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(GensokyoSkinTypes.LUNAR, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));

        this.addConfig(MobSkinTypes.DEFAULT, new SkinConfig(SkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
        this.addConfig(MobSkinTypes.GHOST, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(MobSkinTypes.YOUSEI01, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(MobSkinTypes.YOUSEI02, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(MobSkinTypes.YOUSEI03, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(MobSkinTypes.SUNFLOWER_YOUSEI, new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(MobSkinTypes.GOBLIN, new SkinConfig(SkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
        this.addConfig(MobSkinTypes.WATER_ELEMENTAL, new SkinConfig(SkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
        this.addConfig(MobSkinTypes.FIRE_ELEMENTAL, new SkinConfig(SkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
        this.addConfig(MobSkinTypes.ICE_ELEMENTAL, new SkinConfig(SkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
    }
}
