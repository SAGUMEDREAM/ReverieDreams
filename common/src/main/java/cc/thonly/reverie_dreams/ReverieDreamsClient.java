package cc.thonly.reverie_dreams;

import cc.thonly.reverie_dreams.api.client.midi.Midi2Sound;
import cc.thonly.reverie_dreams.client.ClientEventHandler;
import cc.thonly.reverie_dreams.client.component.ClientPlayerComponentManager;
import cc.thonly.reverie_dreams.client.registry.RDBlockEntityRenderers;
import cc.thonly.reverie_dreams.client.registry.RDEntityRenderers;
import lombok.extern.slf4j.Slf4j;
import net.blay09.mods.balm.client.BalmClientRegistrars;
import net.blay09.mods.balm.client.platform.event.callback.ClientLifecycleCallback;
import net.blay09.mods.balm.client.platform.event.callback.ClientTickCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class ReverieDreamsClient {
    public static final Logger LOGGER = LoggerFactory.getLogger(ReverieDreams.MOD_ID);

    public static void initialize(BalmClientRegistrars registrars, Runnable lateInit) {
        registrars.blockEntityRenderers(RDBlockEntityRenderers::initialize);
        registrars.entityRenderers(RDEntityRenderers::initialize);
        ReverieDreamsClient.initializeClientEvent(registrars);
        Midi2Sound.register();
        ReverieDreams.LATE_INIT_CLIENT.forEach(Runnable::run);
        ReverieDreams.LATE_INIT_CLIENT.clear();
        lateInit.run();
    }

    public static void initializeClientEvent(BalmClientRegistrars registrars) {
        ClientLifecycleCallback.ConnectedToServer.EVENT.register(ClientEventHandler::onPlayerConnectedToServer);
        ClientLifecycleCallback.ConnectedToServer.EVENT.register(ClientEventHandler::onInitMidiDevice);
        ClientLifecycleCallback.DisconnectedFromServer.EVENT.register(ClientEventHandler::onStopMidiDevice);
        ClientTickCallback.AFTER.register(ClientPlayerComponentManager::tickByClient);
    }

    public static Logger logger() {
        return LOGGER;
    }
}
