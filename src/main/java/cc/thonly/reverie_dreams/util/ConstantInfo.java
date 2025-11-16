package cc.thonly.reverie_dreams.util;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.config.ReverieDreamsConfiguration;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

import java.util.Locale;

public class ConstantInfo {
    public static final FabricLoader FABRIC = FabricLoader.getInstance();
    public static final String VERSION = FABRIC
            .getModContainer(ReverieDreams.MOD_ID)
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");
    public static final MappingResolver MAPPING_RESOLVER = FABRIC.getMappingResolver();
    public static final EnvType ENV_TYPE = FabricLoader.getInstance().getEnvironmentType();
    private static final boolean DEV_ENV = FabricLoader.getInstance().isDevelopmentEnvironment();
    private static final boolean DEV_MODE = VERSION.contains("-dev.") || VERSION.contains("alpha") || DEV_ENV;
    private static final boolean HAS_BUKKIT_API = isModLoaded("arclight") || isModLoaded("cardboard") || isModLoaded("banner");
    private static final boolean HAS_CONNECTOR = isModLoaded("connector");
    private static final boolean HAS_FORGE_API = isModLoaded("kilt");
    private static final boolean HAS_OPTIFINE = isModLoaded("optifabric");
    public static String SYSTEM_LANGUAGE = null;
    public static boolean IS_DATAGEN_MODE = System.getProperty("fabric-api.datagen.output-dir") != null;
    public static String LATEST_VERSION = null;

    static {
        Locale locale = Locale.getDefault();
        ConstantInfo.SYSTEM_LANGUAGE = (locale.getLanguage() + "_" + locale.getCountry()).toLowerCase();
    }

    public static String getLangCode() {
        return SYSTEM_LANGUAGE;
    }

    public static boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }

    public static boolean isDevMode() {
        return DEV_MODE || ReverieDreamsConfiguration.DEBUG_MODE;
    }

    public static boolean isHasConnector() {
        return HAS_CONNECTOR;
    }

    public static boolean hasBukkitApi() {
        return HAS_BUKKIT_API;
    }

    public static boolean hasForgeApi() {
        return HAS_FORGE_API;
    }

    public static boolean hasOptifine() {
        return HAS_OPTIFINE;
    }
}
