package cc.thonly.reverie_dreams.client.component;

import cc.thonly.reverie_dreams.api.player.BasePlayerComponentManager;
import cc.thonly.reverie_dreams.server.component.ComponentEntry;
import cc.thonly.reverie_dreams.server.component.ServerPlayerComponentManager;
import cc.thonly.reverie_dreams.server.player.PlayerComponent;
import lombok.extern.slf4j.Slf4j;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.platform.BalmSafeClientAccess;
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
        BalmSafeClientAccess balmSafeClientAccess = Balm.safeClientAccess();
        Player clientPlayer = balmSafeClientAccess.getClientPlayer();
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
