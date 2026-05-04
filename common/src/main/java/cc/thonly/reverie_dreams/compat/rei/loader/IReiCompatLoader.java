package cc.thonly.reverie_dreams.compat.rei.loader;

import cc.thonly.reverie_dreams.compat.rei.CommonREIPlugin;
import me.shedaniel.rei.api.common.plugins.PluginView;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;

@SuppressWarnings("UnstableApiUsage")
public class IReiCompatLoader {
    public static void bootstrap() {
        PluginView<REICommonPlugin> pluginView = PluginView.getInstance();
        pluginView.registerPlugin(new CommonREIPlugin());
    }
}
