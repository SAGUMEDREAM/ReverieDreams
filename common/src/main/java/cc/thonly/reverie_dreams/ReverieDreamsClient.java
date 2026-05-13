package cc.thonly.reverie_dreams;

import cc.thonly.reverie_dreams.client.component.ClientPlayerComponentManager;
import cc.thonly.reverie_dreams.client.networking.ClientNetworkingHandlers;
import cc.thonly.reverie_dreams.client.registry.RDBlockEntityRenderers;
import cc.thonly.reverie_dreams.client.registry.RDEntityRenderers;
import cc.thonly.reverie_dreams.networking.payload.*;
import cc.thonly.reverie_dreams.util.PlatformContext;
import lombok.extern.slf4j.Slf4j;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.client.BalmClientRegistrars;
import net.blay09.mods.balm.client.platform.event.callback.ClientLifecycleCallback;
import net.blay09.mods.balm.client.platform.event.callback.ClientTickCallback;
import net.blay09.mods.balm.network.BalmNetworking;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class ReverieDreamsClient {
    public static final Logger LOGGER = LoggerFactory.getLogger(ReverieDreams.MOD_ID);

    public static void initialize(BalmClientRegistrars registrars, Runnable lateInit) {
//        registrars.blockRenderTypes(RDBlockRenderTypes::initialize);
        registrars.blockEntityRenderers(RDBlockEntityRenderers::initialize);
        registrars.entityRenderers(RDEntityRenderers::initialize);
        ReverieDreamsClient.initializeClientEvent(registrars);
//        ReverieDreamsClient.initializeNetworking(registrars);
        ReverieDreams.LATE_INIT_CLIENT.forEach(Runnable::run);
        ReverieDreams.LATE_INIT_CLIENT.clear();
        lateInit.run();
    }

    public static void initializeClientEvent(BalmClientRegistrars registrars) {
        ClientLifecycleCallback.ConnectedToServer.EVENT.register(client -> {
            ClientPlayerComponentManager.clearConnection();
            Balm.networking().sendToServer(new HelloPacket());
            Balm.networking().sendToServer(new PlayerJoinVersionPacket(PlatformContext.VERSION.get()));
        });
        ClientTickCallback.AFTER.register(ClientPlayerComponentManager::tickByClient);
    }

    public static void initializeNetworking(BalmClientRegistrars registrars) {
        BalmNetworking networking = Balm.networking();
        networking.registerClientboundPacket(
                RecipeManagerSyncPacket.PACKET_ID,
                RecipeManagerSyncPacket.class,
                RecipeManagerSyncPacket.CODEC,
                (player, payload) -> clientThreadBySync(() -> ClientNetworkingHandlers.onReceiveRecipeManagerSyncPacket(player, payload))
        );
        networking.registerClientboundPacket(
                RegistryImpSyncPacket.PACKET_ID,
                RegistryImpSyncPacket.class,
                RegistryImpSyncPacket.CODEC,
                (player, payload) -> clientThreadBySync(() -> ClientNetworkingHandlers.onReceiveRegistryImpSyncPacket(player, payload))
        );
        networking.registerClientboundPacket(
                SyncEntityPacket.PACKET_ID,
                SyncEntityPacket.class,
                SyncEntityPacket.CODEC,
                ClientNetworkingHandlers::onReceiveSyncEntityPacket
        );
        networking.registerClientboundPacket(
                StartScreenshotPacket.PACKET_ID,
                StartScreenshotPacket.class,
                StartScreenshotPacket.CODEC,
                (player, packet) -> clientThreadBySync(() -> ClientNetworkingHandlers.onReceiveStartScreenshotPacket(player, packet))
        );
        networking.registerClientboundPacket(
                PlayerComponentUpdatePacket.PACKET_ID,
                PlayerComponentUpdatePacket.class,
                PlayerComponentUpdatePacket.CODEC,
                (player, packet) -> clientThreadBySync(() -> ClientNetworkingHandlers.onReceivePlayerComponentUpdatePacket(player, packet))
        );
    }

    private static void clientThreadBySync(Runnable action) {
        Minecraft.getInstance().execute(action);
    }

    public static Logger logger() {
        return LOGGER;
    }
}
