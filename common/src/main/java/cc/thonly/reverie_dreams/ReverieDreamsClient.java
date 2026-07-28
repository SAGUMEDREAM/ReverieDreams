package cc.thonly.reverie_dreams;

import cc.thonly.reverie_dreams.api.client.midi.Midi2Sound;
import cc.thonly.reverie_dreams.api.proxy.SafeClientAccess;
import cc.thonly.reverie_dreams.client.ClientEventHandler;
import cc.thonly.reverie_dreams.client.SafeClientAccessImpl;
import cc.thonly.reverie_dreams.client.component.ClientPlayerComponentManager;
import cc.thonly.reverie_dreams.client.registry.RDBlockEntityRenderers;
import cc.thonly.reverie_dreams.client.registry.RDEntityRenderers;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.platform.Platform;
import dev.architectury.platform.client.ConfigurationScreenRegistry;
import lombok.extern.slf4j.Slf4j;
import me.shedaniel.autoconfig.AutoConfigClient;
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
        RDEntityRenderers.initialize();
        ReverieDreamsClient.initializeClientEvent();
        Midi2Sound.register();
        ReverieDreams.LATE_INIT_CLIENT.forEach(Runnable::run);
        ReverieDreams.LATE_INIT_CLIENT.clear();
        ConfigurationScreenRegistry.register(Platform.getMod(ReverieDreams.MOD_ID), parent -> {
            return AutoConfigClient.getConfigScreen(ReverieDreamsConfiguration.class, parent).get();
        });
        lateInit.run();
    }

    public static void initializeClientEvent() {
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(ClientEventHandler::onPlayerConnectedToServer);
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(ClientEventHandler::onInitMidiDevice);
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(ClientEventHandler::onStopMidiDevice);
        ClientTickEvent.CLIENT_POST.register(ClientPlayerComponentManager::tickByClient);
    }

    public static Logger logger() {
        return LOGGER;
    }
}
