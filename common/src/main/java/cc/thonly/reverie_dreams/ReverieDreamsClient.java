package cc.thonly.reverie_dreams;

import cc.thonly.reverie_dreams.api.client.midi.Midi2Sound;
import cc.thonly.reverie_dreams.api.proxy.SafeClientAccess;
import cc.thonly.reverie_dreams.client.ClientEventHandler;
import cc.thonly.reverie_dreams.client.SafeClientAccessImpl;
import cc.thonly.reverie_dreams.client.component.ClientPlayerComponentManager;
import cc.thonly.reverie_dreams.client.registry.RDBlockEntityRenderers;
import cc.thonly.reverie_dreams.client.registry.RDBlockRenderTypes;
import cc.thonly.reverie_dreams.client.registry.RDEntityRenderers;
import lombok.extern.slf4j.Slf4j;
import net.blay09.mods.balm.client.platform.event.callback.ClientLifecycleCallback;
import net.blay09.mods.balm.client.platform.event.callback.ClientTickCallback;
import net.minecraft.client.player.LocalPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ReverieDreamsClient {
    public static final Logger LOGGER = LoggerFactory.getLogger(ReverieDreams.MOD_ID);
    public static final List<Runnable> LATE_INIT = new ArrayList<>();

    public static void initialize(Runnable lateInit) {
        SafeClientAccess.ref.set(new SafeClientAccessImpl());
        RDBlockEntityRenderers.initialize();
        RDBlockRenderTypes.initialize();
        RDEntityRenderers.initialize();
        ReverieDreamsClient.initializeClientEvent();
        Midi2Sound.register();
        ReverieDreams.LATE_INIT_CLIENT.forEach(Runnable::run);
        ReverieDreams.LATE_INIT_CLIENT.clear();
        lateInit.run();
    }

    public static void initializeClientEvent() {
        ClientLifecycleCallback.ConnectedToServer.EVENT.register(minecraft -> {
            LocalPlayer player = minecraft.player;
            minecraft.execute(() -> {
                ClientEventHandler.onInitMidiDevice(player);
                ClientEventHandler.onPlayerConnectedToServer(player);
            });
        });
        ClientLifecycleCallback.DisconnectedFromServer.EVENT.register(minecraft -> {
            LocalPlayer player = minecraft.player;
            ClientEventHandler.onStopMidiDevice(player);
        });
        ClientTickCallback.AFTER.register(ClientPlayerComponentManager::tickByClient);
    }

    public static Logger logger() {
        return LOGGER;
    }
}
