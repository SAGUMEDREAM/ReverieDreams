package cc.thonly.reverie_dreams.paper;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import cc.thonly.reverie_dreams.paper.danmaku.DanmakuBootstrap;

@SuppressWarnings("UnstableApiUsage")
public final class ReverieDreamsBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(final BootstrapContext context) {
        DanmakuBootstrap.configure(context);
    }
}
