package cc.thonly.reverie_dreams.fabric.api;

import cc.thonly.reverie_dreams.util.PlatformContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

@Slf4j
public class ReverieDreamsPolymerBridge {
    private static boolean LOADED = false;
    private static boolean LOADED2 = false;

    public static void tryPolymerify() {
        if (LOADED) {
            return;
        }
        boolean modLoaded = PlatformContext.hasPolymer();
        if (!modLoaded) {
            return;
        }
        try {
            Class<?> clazz = Class.forName("cc.thonly.reverie_dreams.fabric.PolymerInitializer");
            Method method = clazz.getDeclaredMethod("bootstrap");
            method.invoke(null);
        } catch (Exception e) {
            log.error("Can't load polymer patch", e);
            String text = "An error occurred during Polymerify that failed to load.\n Please try checking that the versions of the base mod and the polymer patch are the same, and report the error.";
            if (PlatformContext.isChinaEnv()) {
                text = "Polymerify 加载失败，导致出现错误。\n请检查本体和 Polymerify 补丁的版本是否一致，\n报告错误信息。";
            }
            log.error(text);
            throw new RuntimeException(e);
        }
        LOADED = true;
    }

    public static void tryReplaceGuidebook() {
        if (LOADED2) {
            return;
        }
        boolean modLoaded = PlatformContext.hasPolymer();
        if (!modLoaded) {
            return;
        }
        try {
            Class<?> clazz = Class.forName("cc.thonly.reverie_dreams.fabric.PolymerInitializer");
            Method method = clazz.getDeclaredMethod("replaceGuidebook");
            method.invoke(null);
        } catch (Exception e) {
            log.error("Can't load polymer patch", e);
            throw new RuntimeException(e);
        }
        LOADED2 = true;
    }
}
