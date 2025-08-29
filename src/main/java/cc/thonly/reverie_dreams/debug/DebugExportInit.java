package cc.thonly.reverie_dreams.debug;

import cc.thonly.reverie_dreams.Touhou;
import net.fabricmc.api.ModInitializer;

import java.util.List;

public class DebugExportInit implements ModInitializer {
    @Override
    public void onInitialize() {
        if (Touhou.isDevMode()) {
            List<String> filenames = List.of(DebugExportWriter.RDDE);
            for (String filename : filenames) {
                DebugExportWriter instance = DebugExportWriter.getInstance(filename);
                instance.export();
            }
        }
    }
}
