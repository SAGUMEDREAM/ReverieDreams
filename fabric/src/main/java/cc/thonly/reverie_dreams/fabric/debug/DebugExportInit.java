package cc.thonly.reverie_dreams.fabric.debug;

import cc.thonly.reverie_dreams.util.PlatformContext;
import net.fabricmc.api.ModInitializer;

import java.util.List;

public class DebugExportInit implements ModInitializer {
    @Override
    public void onInitialize() {
        if (PlatformContext.isDevMode()) {
            List<String> filenames = List.of(DebugExportWriter.RDDE);
            for (String filename : filenames) {
                DebugExportWriter instance = DebugExportWriter.getInstance(filename);
                instance.export();
            }
        }
    }
}
