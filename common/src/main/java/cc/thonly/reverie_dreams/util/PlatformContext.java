package cc.thonly.reverie_dreams.util;

import cc.thonly.reverie_dreams.ReverieDreams;
import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.world.level.block.Block;

import java.awt.*;
import java.util.Locale;
import java.util.Optional;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class PlatformContext {

    public static final LazySupplier<String> VERSION =
            LazySupplier.of(() -> {
                        Optional<Mod> optionalMod = Platform.getOptionalMod(ReverieDreams.MOD_ID);
                        if (optionalMod.isPresent()) {
                            return optionalMod.get().getVersion();
                        }
                        return "unknown";
                    }
            );
    private static final LazySupplier<Boolean> DEV_ENV =
            LazySupplier.of(Platform::isDevelopmentEnvironment);
    private static final LazySupplier<Boolean> DEV_MODE =
            LazySupplier.of(() ->
                    VERSION.get().contains("-dev.")
                            || VERSION.get().contains("alpha")
                            || DEV_ENV.get()
            );
    private static final LazySupplier<Boolean> HAS_BUKKIT_API =
            LazySupplier.of(() ->
                    isModLoaded("arclight")
                            || isModLoaded("cardboard")
                            || isModLoaded("banner")
            );
    private static final LazySupplier<Boolean> HAS_CONNECTOR = LazySupplier.of(() -> isModLoaded("connector"));
    private static final LazySupplier<Boolean> HAS_FORGE_API = LazySupplier.of(() -> isModLoaded("kilt"));
    private static final LazySupplier<Boolean> HAS_OPTIFINE = LazySupplier.of(() -> isModLoaded("optifabric"));
    private static final LazySupplier<Boolean> HAS_CREATE_FLY = LazySupplier.of(() -> isModLoaded("create"));
    private static final LazySupplier<Boolean> HAS_POLYFACTORY = LazySupplier.of(() -> isModLoaded("polyfactory"));
    public static Block FABRIC_POLYFACTORY_HAND_CRANK = null;
    public static Block FABRIC_CREATE_FLY_HAND_CRANK = null;
    public static String SYSTEM_LANGUAGE = null;
    public static boolean IS_DATAGEN_MODE =
            System.getProperty("fabric-api.datagen.output-dir") != null;
    public static String LATEST_VERSION = null;


    static {
        Locale locale = Locale.getDefault();
        SYSTEM_LANGUAGE =
                (locale.getLanguage() + "_" + locale.getCountry())
                        .toLowerCase();
    }


    public static boolean isFabric() {
        return Platform.isFabric();
    }


    public static boolean isNeoForge() {
        return Platform.isNeoForge();
    }


    public static boolean isForge() {
        return Platform.isNeoForge();
    }


    public static boolean isForgeLike() {
        return isNeoForge() || isForge();
    }


    public static boolean isClientSide() {
        Env env = Platform.getEnvironment();
        return env == Env.CLIENT;
    }


    public static boolean isDedicatedServer() {
        Env env = Platform.getEnvironment();
        return env == Env.SERVER;
    }


    public static String getLangCode() {
        return SYSTEM_LANGUAGE;
    }

    public static boolean isChinaEnv() {
        String langCode = getLangCode();

        return langCode.equalsIgnoreCase("zh_cn")
                || langCode.equalsIgnoreCase("zh_tw")
                || langCode.equalsIgnoreCase("zh_hk");
    }


    public static boolean isModLoaded(String id) {
        return Platform.isModLoaded(id);
    }


    public static boolean isDevMode() {
        return DEV_MODE.get()
                || ReverieDreams.config().debugMode;
    }

    public static boolean isDevModeByIDEA() {
        return DEV_MODE.get();
    }


    public static boolean isHasConnector() {
        return HAS_CONNECTOR.get();
    }


    public static boolean hasBukkitApi() {
        return HAS_BUKKIT_API.get();
    }


    public static boolean hasForgeApi() {
        return HAS_FORGE_API.get();
    }


    public static boolean hasOptifine() {
        return HAS_OPTIFINE.get();
    }


    public static boolean hasCreateFly() {
        return HAS_CREATE_FLY.get();
    }


    public static boolean hasPolyfactory() {
        return HAS_POLYFACTORY.get();
    }


    public static boolean hasPolymer() {
        return isModLoaded("polymer-core")
                && isModLoaded("reverie_dreams_polymerify");
    }


    public static boolean isServerEnvironment() {

        if (GraphicsEnvironment.isHeadless()) {
            return true;
        }

        try {
            return GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getScreenDevices()
                    .length == 0;

        } catch (Throwable ignored) {
            return true;
        }
    }
}