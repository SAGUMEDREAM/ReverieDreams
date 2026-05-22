package cc.thonly.reverie_dreams.networking;

import cc.thonly.reverie_dreams.networking.payload.HelloPacket;
import cc.thonly.reverie_dreams.networking.payload.PlayerJoinVersionPacket;
import cc.thonly.reverie_dreams.networking.payload.ScreenshotMapPacket;
import cc.thonly.reverie_dreams.server.SessionManager;
import cc.thonly.reverie_dreams.util.PlatformContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.util.*;

@SuppressWarnings("resource")
public class ServerNetworkingHandlers {
    public static final Set<ServerPlayer> PLAYER_WITH_MOD = new HashSet<>();
    public static final Map<ServerPlayer, String> PLAYER_SIDE_VERSION = new WeakHashMap<>();

    public static void onReceiveHelloPacket(ServerPlayer player, HelloPacket packet) {
        ServerNetworkingHandlers.PLAYER_WITH_MOD.add(player);
    }

    public static void onReceiveHelloPacket(ServerPlayer player, PlayerJoinVersionPacket packet) {
        String version = packet.version();
        ServerNetworkingHandlers.PLAYER_SIDE_VERSION.put(player, version);
    }

    public static void onReceiveScreenshotMapPacket(ServerPlayer player, ScreenshotMapPacket packet) {
        UUID clientSessionId = packet.sessionId();
        UUID sessionId = SessionManager.getSession(player.getUUID());
        if (!Objects.equals(clientSessionId, sessionId)) {
            return;
        }

        SessionManager.clear(player.getUUID());
        byte[] pixels = packet.pixels();

        if (pixels.length == 0) {
            return;
        }
        if (pixels.length != 128 * 128) {
            return;
        }

        ServerLevel level = player.level();
        MapId freeMapId = level.getFreeMapId();
        MapItemSavedData mapData = MapItemSavedData.createFresh(
                0,
                0,
                (byte) 0,
                false,
                false,
                level.dimension()
        );
        System.arraycopy(pixels, 0, mapData.colors, 0, pixels.length);
        MapItemSavedData locked = mapData.locked();
        level.setMapData(freeMapId, locked);
        ItemStack mapItem = new ItemStack(Items.FILLED_MAP);
        mapItem.set(DataComponents.MAP_ID, freeMapId);
        player.getInventory().add(mapItem);
    }

    public static boolean hasModOnClient(ServerPlayer player) {
        return PLAYER_WITH_MOD.contains(player);
    }

    public static boolean hasModWithVersion(ServerPlayer player) {
        return hasModOnClient(player) && Objects.equals(PLAYER_SIDE_VERSION.get(player), PlatformContext.VERSION.get());
    }
}
