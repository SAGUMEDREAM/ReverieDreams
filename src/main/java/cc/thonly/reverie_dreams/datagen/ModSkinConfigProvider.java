package cc.thonly.reverie_dreams.datagen;

import cc.thonly.reverie_dreams.datagen.generator.SkinConfigProvider;
import cc.thonly.reverie_dreams.entity.skin.MobSkins;
import cc.thonly.reverie_dreams.entity.skin.NPCSkinConfig;
import cc.thonly.reverie_dreams.entity.skin.RoleSkins;
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
        this.addConfig(RoleSkins.REIMU, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.CYAN_REIMU, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.MARISA, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.RUMIA, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.CIRNO, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.HOAN_MEIRIN, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.PATCHOULI, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.SAKUYA, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.REMILA, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.FLANDRE, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.LETTY_WHITEROCK, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.CHEN, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.ALICE, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.LILY_WHITE, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.LUNASA_PRISMRIVER, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.MERLIN_PRISMRIVER, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.LYRICA_PRISMRIVER, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.RAN, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.YOUMU, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.YUYUKO, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.YAKUMO_YUKAI, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.USAMI_RENKO, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.MARIBEL_HEARN, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.MYSTIA_LORELEI, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.WRIGGLE_NIGHTBUG, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.KAMISHIRASAWA_KEINE, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.REISEN, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.ERIN, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.HOURAISAN_KAGUYA, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.HUZIWARA_NO_MOKOU, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.SHIKIEIKI_YAMAXANADU, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.KAZAMI_YUKA, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.KAGIYAMA_HINA, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.INUBASHIRI_MOMIZI, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.KAWASIRO_NITORI, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.AYA, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.KOCHIYA_SANAE, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.YASAKA_KANAKO, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.MORIYA_SUWAKO, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.KISUME, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.KURODANI_YAMAME, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.MIZUHASHI_PARSEE, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.HOSHIGUMA_YUGI, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.KAENBYOU_RIN, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.KOMEIJI_SATORI, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.REIUJI_UTSUH, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.KOMEIJI_KOISHI, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.WHITE_KOMEIJI_KOISHI, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.NAZRIN, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.NUE, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.TATARA_KOGASA, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.KASODANI_KYOUKO, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.MIYAKO_YOSHIKA, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.KAKU_SEIGA, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.SOGA_NO_TOZIKO, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.MONONOBE_NO_FUTO, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.TOYOSATOMIMI_NO_MIKO, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.HOUJUU_NUE, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.HUTATSUIWA_MAMIZOU, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.WAKASAGIHIME, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.SEKIBANKI, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.IMAIZUMI_KAGEROU, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.KIJIN_SEIJIA, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.SUKUNA_SHINMYOUMARU, new NPCSkinConfig(NPCSkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.HORIKAWA_RAIKO, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.SEIRAN, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.RINGO, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.DOREMY_SWEET, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.KISIN_SAGUME, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.CLOWNPIECE, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.JUNKO, new NPCSkinConfig(NPCSkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.HECATIA_LAPISLAZULI, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.ETERNITY_LARVA, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.SAKUTA_NEMUNO, new NPCSkinConfig(NPCSkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.KOMANO_AUNN, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.YATADERA_NARUMI, new NPCSkinConfig(NPCSkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.NISHIDA_SATONO, new NPCSkinConfig(NPCSkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.TEIREIDA_MAI, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.MATARA_OKINA, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.EBISU_EIKA, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.USHIZAKI_URUMI, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.NIWATARI_KUTAKA, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.KITCHO_YACHIE, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.JOUTOUGU_MAYUMI, new NPCSkinConfig(NPCSkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.HANIYASUSHIN_KEIKI, new NPCSkinConfig(NPCSkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.KUROKOMA_SAKI, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.GOUTOKUZI_MIKE, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.YAMASHIRO_TAKANE, new NPCSkinConfig(NPCSkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.KOMAKUSA_SANNYO, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.TAMATSUKURI_MISUMARU, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.KUDAMAKI_TSUKASA, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.IIZUNAMARU_MEGUMU, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.TENKYU_CHIMATA, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.HIMEMUSHI_MOMOYO, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.SUIKA, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.TENSHI, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.STAR, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.SUNNY, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(RoleSkins.LUNAR, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));

        this.addConfig(MobSkins.DEFAULT, new NPCSkinConfig(NPCSkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
        this.addConfig(MobSkins.GHOST, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(MobSkins.YOUSEI01, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(MobSkins.YOUSEI02, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(MobSkins.YOUSEI03, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(MobSkins.SUNFLOWER_YOUSEI, new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        this.addConfig(MobSkins.GOBLIN, new NPCSkinConfig(NPCSkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
        this.addConfig(MobSkins.WATER_ELEMENTAL, new NPCSkinConfig(NPCSkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
        this.addConfig(MobSkins.FIRE_ELEMENTAL, new NPCSkinConfig(NPCSkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
        this.addConfig(MobSkins.ICE_ELEMENTAL, new NPCSkinConfig(NPCSkinConfig.ModelType.WIDE, Optional.empty(), Optional.empty()));
    }
}
