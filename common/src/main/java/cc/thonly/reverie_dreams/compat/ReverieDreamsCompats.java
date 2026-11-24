package cc.thonly.reverie_dreams.compat;

import cc.thonly.reverie_dreams.compat.ysm.NPCModelSelection;
import cc.thonly.reverie_dreams.util.PlatformContext;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.Pattern;

import java.lang.reflect.Method;

import org.intellij.lang.annotations.Subst;

@Slf4j
public class ReverieDreamsCompats {
    public static boolean HAS_ANY_YSM = false;
    public static Method SPARKLE_MORPHER_YSM_SUBMIT = null;

    public static void initialize() {
        load("minecraft", "cc.thonly.reverie_dreams.compat.VanillaCompat");
        load("distanthorizons", "cc.thonly.reverie_dreams.compat.distanthorizons.DistantHorizonsCompat");
    }

    protected static void initializeNPCModels(@Subst("java.lang.String") String clientInvoker) {
        String modId = NPCModelSelection.findModelLoader();
        if (modId == null) {
            if (PlatformContext.isModLoaded(NPCModelSelection.FOX_ID) && PlatformContext.isModLoaded(NPCModelSelection.SPARKLE_ID)) {
                log.warn("Fox Model Loader and Sparkle Morpher share Java classes. NPC model integration requires one model loader.");
            }
            return;
        }
        if (!NPCModelSelection.usesSparkleClientCatalog()
                && !tryLoad(modId, "cc.thonly.reverie_dreams.compat.ysm.ClassicYsmModelCatalog")) {
            return;
        }
        if (PlatformContext.isClientSide() && !tryLoad(modId, clientInvoker)) {
            return;
        }
        NPCModelSelection.registerMenus();
    }

    public static void load(String modId,
                            @Subst("java.lang.String") @Pattern("[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*$") String compatClassName
    ) {
        tryLoad(modId, compatClassName);
    }

    private static boolean tryLoad(String modId, @Subst("java.lang.String") String compatClassName) {
        if (!PlatformContext.isModLoaded(modId))
            return false;

        try {
            Class<?> clazz = Class.forName(compatClassName);
            Method bootstrap = clazz.getMethod("bootstrap");
            bootstrap.setAccessible(true);
            bootstrap.invoke(null);
            log.info("Loaded Compat for {}", modId);
            return true;
        } catch (Throwable e) {
            log.warn("Can't load compat plugin {}", compatClassName, e);
            return false;
        }
    }
}
