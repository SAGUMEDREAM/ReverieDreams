package cc.thonly.reverie_dreams.registry.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.FumoType;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import net.minecraft.resources.Identifier;

import java.util.Collection;

public class FumoTypes {
    public static final RegistryProvider<FumoType> REGISTRY_KEY = BuiltInRegistryProviders.FUMO;
    public static final FumoType ALICE = register(new FumoType(ReverieDreams.id("alice")));
    public static final FumoType AYA = register(new FumoType(ReverieDreams.id("aya")));
    public static final FumoType BLUE_REIMU = register(new FumoType(ReverieDreams.id("blue_reimu")));
    public static final FumoType CHEN = register(new FumoType(ReverieDreams.id("chen")));
    public static final FumoType CHERRIES_SHION = register(new FumoType(ReverieDreams.id("cherries_shion")));
    public static final FumoType CHIMATA = register(new FumoType(ReverieDreams.id("chimata")));
    public static final FumoType CIRNO = register(new FumoType(ReverieDreams.id("cirno")));
    public static final FumoType CLOWNPIECE = register(new FumoType(ReverieDreams.id("clownpiece")));
    public static final FumoType EIKI = register(new FumoType(ReverieDreams.id("eiki")));
    public static final FumoType EIRIN = register(new FumoType(ReverieDreams.id("eirin")));
    public static final FumoType FLAN = register(new FumoType(ReverieDreams.id("flan")));
    public static final FumoType FLANDRE = register(new FumoType(ReverieDreams.id("flandre")));
    public static final FumoType HATATE = register(new FumoType(ReverieDreams.id("hatate")));
    public static final FumoType JOON = register(new FumoType(ReverieDreams.id("joon")));
    public static final FumoType KAGUYA = register(new FumoType(ReverieDreams.id("kaguya")));
    public static final FumoType KANMARISA = register(new FumoType(ReverieDreams.id("kanmarisa")));
    public static final FumoType KASEN = register(new FumoType(ReverieDreams.id("kasen")));
    public static final FumoType KOGASA = register(new FumoType(ReverieDreams.id("kogasa")));
    public static final FumoType KOISHI = register(new FumoType(ReverieDreams.id("koishi")));
    public static final FumoType KOKORO = register(new FumoType(ReverieDreams.id("kokoro")));
    public static final FumoType KOSUZU = register(new FumoType(ReverieDreams.id("kosuzu")));
    public static final FumoType MARISA = register(new FumoType(ReverieDreams.id("marisa")));
    public static final FumoType MARISA_HAT = register(new FumoType(ReverieDreams.id("marisa_hat")));
    public static final FumoType MCKY = register(new FumoType(ReverieDreams.id("mcky")));
    public static final FumoType MEILING = register(new FumoType(ReverieDreams.id("meiling")));
    public static final FumoType MELON = register(new FumoType(ReverieDreams.id("melon")));
    public static final FumoType MOKOU = register(new FumoType(ReverieDreams.id("mokou")));
    public static final FumoType MOMIJI = register(new FumoType(ReverieDreams.id("momiji")));
    public static final FumoType MYSTIA = register(new FumoType(ReverieDreams.id("mystia")));
    public static final FumoType NAZRIN = register(new FumoType(ReverieDreams.id("nazrin")));
    public static final FumoType NEW_REIMU = register(new FumoType(ReverieDreams.id("new_reimu")));
    public static final FumoType NUE = register(new FumoType(ReverieDreams.id("nue")));
    public static final FumoType PARSEE = register(new FumoType(ReverieDreams.id("parsee")));
    public static final FumoType PATCHOULI = register(new FumoType(ReverieDreams.id("patchouli")));
    public static final FumoType PC98_MARISA = register(new FumoType(ReverieDreams.id("pc98_marisa")));
    public static final FumoType RAN = register(new FumoType(ReverieDreams.id("ran")));
    public static final FumoType REIMU = register(new FumoType(ReverieDreams.id("reimu")));
    public static final FumoType REISEN = register(new FumoType(ReverieDreams.id("reisen")));
    public static final FumoType REMILIA = register(new FumoType(ReverieDreams.id("remilia")));
    public static final FumoType RENKO = register(new FumoType(ReverieDreams.id("renko")));
    public static final FumoType RUMIA = register(new FumoType(ReverieDreams.id("rumia")));
    public static final FumoType SAKUYA = register(new FumoType(ReverieDreams.id("sakuya")));
    public static final FumoType SANAE = register(new FumoType(ReverieDreams.id("sanae")));
    public static final FumoType SATORI = register(new FumoType(ReverieDreams.id("satori")));
    public static final FumoType SEIJA = register(new FumoType(ReverieDreams.id("seija")));
    public static final FumoType SHION = register(new FumoType(ReverieDreams.id("shion")));
    public static final FumoType SHOU = register(new FumoType(ReverieDreams.id("shou")));
    public static final FumoType SMART_CIRNO = register(new FumoType(ReverieDreams.id("smart_cirno")));
    public static final FumoType SUIKA = register(new FumoType(ReverieDreams.id("suika")));
    public static final FumoType SUWAKO = register(new FumoType(ReverieDreams.id("suwako")));
    public static final FumoType TAN_CIRNO = register(new FumoType(ReverieDreams.id("tan_cirno")));
    public static final FumoType TENSHI = register(new FumoType(ReverieDreams.id("tenshi")));
    public static final FumoType TEWI = register(new FumoType(ReverieDreams.id("tewi")));
    public static final FumoType UTSUHO = register(new FumoType(ReverieDreams.id("utsuho")));
    public static final FumoType WAKASAGIHIME = register(new FumoType(ReverieDreams.id("wakasagihime")));
    public static final FumoType YOUMU = register(new FumoType(ReverieDreams.id("youmu")));
    public static final FumoType YUKARI = register(new FumoType(ReverieDreams.id("yukari")));
    public static final FumoType YUUKA = register(new FumoType(ReverieDreams.id("yuuka")));
    public static final FumoType YUYUKO = register(new FumoType(ReverieDreams.id("yuyuko")));

    public static FumoType register(FumoType fumo) {
        return register(fumo.getId(), fumo);
    }

    public static FumoType register(String name, FumoType fumo) {
        return register(ReverieDreams.id(name), fumo);
    }

    public static FumoType register(Identifier id, FumoType fumo) {
        return BuiltInRegistryProviders.register(REGISTRY_KEY, id, fumo.build());
    }

    public static Collection<FumoType> getView() {
        return REGISTRY_KEY.values();
    }

    public static void bootstrap(RegistryProvider<FumoType> registry) {

    }
}
