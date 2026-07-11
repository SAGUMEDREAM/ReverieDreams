package cc.thonly.reverie_dreams.client.component;

import cc.thonly.reverie_dreams.api.player.BasePlayerComponentManager;
import cc.thonly.reverie_dreams.server.component.ComponentEntry;
import cc.thonly.reverie_dreams.server.component.ServerPlayerComponentManager;
import cc.thonly.reverie_dreams.server.player.PlayerComponent;
import dev.architectury.platform.Platform;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.List;

@Slf4j
public final class ClientPlayerComponentManager extends BasePlayerComponentManager {
    private static final ClientPlayerComponentManager CLIENT_INSTANCE = new ClientPlayerComponentManager();

    @SuppressWarnings("resource")
    public static void tickByClient(Minecraft client) {
        Minecraft mc = Minecraft.getInstance();
        Player clientPlayer = mc.player;
        if (clientPlayer == null) {
            return;
        }
        Level level = clientPlayer.level();
        List<? extends Player> players = level.players();
        BasePlayerComponentManager componentManager = ServerPlayerComponentManager.serverAccess();
        for (Player player : players) {
            try {
                String uuid = player.getStringUUID();
                List<ComponentEntry> componentEntries =
                        componentManager.getDataList().getOrDefault(uuid, Collections.emptyList());

                for (ComponentEntry entry : componentEntries) {
                    PlayerComponent<?> component = componentManager.getOrCreatePlayerComponent(player, entry.key());
                    component.tick(null, true);
                }
            } catch (Exception err) {
                log.error("Player Data Component Tick task execution failed: ", err);
            }
        }
    }

    public static void clearConnection() {
        clientAccess().getDataList().clear();
    }

    public static ClientPlayerComponentManager clientAccess() {
        return CLIENT_INSTANCE;
    }
}
