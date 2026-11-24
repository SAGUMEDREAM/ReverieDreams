package cc.thonly.reverie_dreams;

import cc.thonly.reverie_dreams.client.networking.ClientNetworkingHandlers;
import cc.thonly.reverie_dreams.client.audio.radio.RadioAudioClient;

import cc.thonly.reverie_dreams.api.client.midi.Midi2Sound;
import cc.thonly.reverie_dreams.api.client.midi.MidiListenerAPI;
import cc.thonly.reverie_dreams.api.proxy.SafeClientAccess;
import cc.thonly.reverie_dreams.api.player.PlayerComponentManagerAccess;
import cc.thonly.reverie_dreams.block.bundle.WoodBundle;
import cc.thonly.reverie_dreams.event.client.ClientEventHandler;
import cc.thonly.reverie_dreams.client.audio.fumo.FumoAudioClient;
import cc.thonly.reverie_dreams.client.RDKeyMappings;
import cc.thonly.reverie_dreams.client.SafeClientAccessImpl;
import cc.thonly.reverie_dreams.client.component.ClientPlayerComponentManager;
import cc.thonly.reverie_dreams.client.midi.MidiListenerAPIImpl;
import cc.thonly.reverie_dreams.client.registry.RDBlockEntityRenderers;
import cc.thonly.reverie_dreams.client.registry.RDEntityRenderers;
import cc.thonly.reverie_dreams.client.renderer.danmaku.DanmakuRenderOptions;
import cc.thonly.reverie_dreams.client.settings.ReverieSettingsScreen;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.platform.Platform;
import dev.architectury.platform.client.ConfigurationScreenRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class ReverieDreamsClient {
    public static final Logger LOGGER = LoggerFactory.getLogger(ReverieDreams.MOD_ID);
    public static final List<Runnable> LATE_INIT = new ArrayList<>();

    public static void initialize(Runnable lateInit) {
        SafeClientAccess.ref.set(new SafeClientAccessImpl());
        MidiListenerAPI.setImplementation(MidiListenerAPIImpl.instance());
        PlayerComponentManagerAccess.registerClient(ClientPlayerComponentManager::clientAccess);
        FumoAudioClient.initialize();
        RadioAudioClient.initialize();
        DanmakuRenderOptions.initialize();
        RDBlockEntityRenderers.initialize();
        RDEntityRenderers.initialize();
        RDKeyMappings.initialize();
        ReverieDreamsClient.initializeClientEvent();
        Midi2Sound.register();
        ReverieDreams.LATE_INIT_CLIENT.forEach(Runnable::run);
        ReverieDreams.LATE_INIT_CLIENT.clear();
        ConfigurationScreenRegistry.register(Platform.getMod(ReverieDreams.MOD_ID), parent -> {
            return new ReverieSettingsScreen(parent);
        });
        // NeoForge's color registry needs the initialized Minecraft client.
        LATE_INIT.add(ReverieDreamsClient::registerLeafTints);
        lateInit.run();
    }

    public static void runLateInit() {
        Iterator<Runnable> pending = LATE_INIT.iterator();
        while (pending.hasNext()) {
            pending.next().run();
            pending.remove();
        }
    }

    private static void registerLeafTints() {
        for (WoodBundle bundle : WoodBundle.INSTANCES) {
            BlockDelegate leaves = bundle.leaves();
            if (leaves != null && bundle.isEnableLeafTintColor()) {
                int color = bundle.leafTintColor();
                ColorHandlerRegistry.registerBlockColors(state -> color, leaves);
            }
        }
    }

    public static void initializeClientEvent() {
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(ClientEventHandler::onPlayerConnectedToServer);
        ClientPlayerEvent.CLIENT_PLAYER_JOIN.register(ClientEventHandler::onInitMidiDevice);
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(ClientEventHandler::onStopMidiDevice);
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> ClientNetworkingHandlers.clearRecipeSync());
        ClientTickEvent.CLIENT_POST.register(ClientPlayerComponentManager::tickByClient);
        ClientTickEvent.CLIENT_POST.register(FumoAudioClient::tick);
        ClientTickEvent.CLIENT_POST.register(RadioAudioClient::tick);
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> RadioAudioClient.reset());
        ClientLifecycleEvent.CLIENT_STOPPING.register(client -> RadioAudioClient.reset());
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> FumoAudioClient.disconnected());
        ClientLifecycleEvent.CLIENT_STOPPING.register(client -> FumoAudioClient.shutdown());
    }

    public static Logger logger() {
        return LOGGER;
    }
}
