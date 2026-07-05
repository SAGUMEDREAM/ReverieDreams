package cc.thonly.reverie_dreams.compat;

import cc.thonly.reverie_dreams.util.PlatformContext;
import cc.thonly.reverie_dreams.util.YsmModType;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Pattern;

import java.lang.reflect.Method;

@Slf4j
public class ReverieDreamsCompats {
    public static boolean HAS_ANY_YSM = false;
    public static Method SPARKLE_MORPHER_YSM_SUBMIT = null;

    public static void initialize() {
        load("minecraft", "cc.thonly.reverie_dreams.compat.VanillaCompat");
        if (YsmModType.installed()) {
            load("yes_steve_model", "cc.thonly.reverie_dreams.compat.ysm.OYSMCompat");
        }
        if (YsmModType.installed()) {
            load("sparkle_morpher", "cc.thonly.reverie_dreams.compat.ysm.SparkleMorpherCompats");
        }
    }

    public static void load(String modId,
                            @org.intellij.lang.annotations.Subst("java.lang.String") @Pattern("[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*$") String compatClassName
    ) {
        if (!PlatformContext.isModLoaded(modId)) return;

        try {
            Class<?> clazz = Class.forName(compatClassName);
            Method bootstrap = clazz.getMethod("bootstrap");
            bootstrap.setAccessible(true);
            bootstrap.invoke(null);
            log.info("Loaded Compat for {}", modId);
        } catch (Throwable e) {
            log.warn("Can't load compat plugin {}", compatClassName, e);
        }
    }
}
