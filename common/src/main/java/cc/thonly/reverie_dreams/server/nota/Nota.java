package cc.thonly.reverie_dreams.server.nota;

import cc.thonly.reverie_dreams.api.nota.NotaAPI;
import net.blay09.mods.balm.platform.event.callback.ServerLifecycleCallback;

public class Nota {
    public static void initialize() {
        NotaAPI.setInstance(new NotaAPI());
        ServerLifecycleCallback.Started.EVENT.register(server -> {
            NotaAPI.getAPI().setDisabling(false);
            NotaAPI.getAPI().setServer(server);
        });
        ServerLifecycleCallback.Stopping.EVENT.register(server-> {
            NotaAPI.getAPI().setServer(null);
            NotaAPI.getAPI().setDisabling(true);
        });
    }
}
