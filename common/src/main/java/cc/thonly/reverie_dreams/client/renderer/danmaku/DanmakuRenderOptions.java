package cc.thonly.reverie_dreams.client.renderer.danmaku;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

@Config(name = "reverie_dreams-danmaku-client")
public final class DanmakuRenderOptions implements ConfigData {
    private static DanmakuRenderOptions options;

    public boolean batchedOrbRendering = true;

    public static void initialize() {
        if (options == null) {
            options = AutoConfig.register(DanmakuRenderOptions.class, GsonConfigSerializer::new).getConfig();
        }
    }

    public static boolean batchOrbs() {
        return options != null && options.batchedOrbRendering;
    }

    public static void setBatchOrbs(boolean enabled) {
        options.batchedOrbRendering = enabled;
        AutoConfig.getConfigHolder(DanmakuRenderOptions.class).save();
    }
}
