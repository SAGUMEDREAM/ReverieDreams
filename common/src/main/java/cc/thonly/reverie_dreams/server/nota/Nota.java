package cc.thonly.reverie_dreams.server.nota;

import cc.thonly.reverie_dreams.api.nota.NotaAPI;
import dev.architectury.event.events.common.LifecycleEvent;

public class Nota {
    public static void initialize() {
        NotaAPI.setInstance(new NotaAPI());
        LifecycleEvent.SERVER_STARTED.register(server -> {
            NotaAPI.getAPI().setDisabling(false);
            NotaAPI.getAPI().setServer(server);
        });
        LifecycleEvent.SERVER_STOPPING.register(server -> {
            NotaAPI.getAPI().setServer(null);
            NotaAPI.getAPI().setDisabling(true);
        });
    }
}
