package cc.thonly.reverie_dreams.danmaku.script;

import net.minecraft.server.MinecraftServer;

public class DanmakuScriptManager {
    public static DanmakuScriptManager INSTANCE;
    private static MinecraftServer SERVER;

    public static DanmakuScriptManager getInstance() {
        if (INSTANCE==null) {
            INSTANCE = new DanmakuScriptManager();
        }
        return INSTANCE;
    }
    
    public static void onTick(MinecraftServer server) {
        SERVER = server;
    }
}
