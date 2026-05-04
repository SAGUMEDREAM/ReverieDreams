package cc.thonly.reverie_dreams.compat;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.util.PlatformContext;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.Pattern;

@Slf4j
public class IReverieDreamsCompats {
    public static void initialize() {
        load("minecraft", "cc.thonly.reverie_dreams.compat.IVanillaCompat");
    }

    public static void load(String modId,
                            @org.intellij.lang.annotations.Subst("java.lang.String") @Pattern("[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*$") String compatClassName
    ) {
        if (!PlatformContext.isModLoaded(modId)) return;

        try {
            Class<?> clazz = Class.forName(compatClassName);
            clazz.getMethod("bootstrap").invoke(null);
            ReverieDreams.LOGGER.info("Loaded Compat for {}", modId);
        } catch (Throwable e) {
            log.warn("Can't load compat plugin {}", compatClassName, e);
        }
    }
}
