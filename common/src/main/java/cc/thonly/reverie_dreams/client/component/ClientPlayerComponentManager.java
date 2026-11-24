package cc.thonly.reverie_dreams.client.component;

import cc.thonly.reverie_dreams.component.BasePlayerComponentManager;
import cc.thonly.reverie_dreams.api.player.PlayerComponent;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@Slf4j
@SuppressWarnings("rawtypes")
public final class ClientPlayerComponentManager extends BasePlayerComponentManager {
    private static final ClientPlayerComponentManager CLIENT_INSTANCE = new ClientPlayerComponentManager();

    public static void tickByClient(Minecraft client) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        for (Map.Entry<UUID, Map<Identifier, PlayerComponent>> playerEntry : new ArrayList<>(CLIENT_INSTANCE.dataList.entrySet())) {
            Player player = resolvePlayer(playerEntry.getKey());
            if (player == null) {
                continue;
            }
            for (PlayerComponent<?> component : new ArrayList<>(playerEntry.getValue().values())) {
                try {
                    if (component.getPlayer() != player) {
                        component.setPlayer(player);
                    }
                    component.tick(null, true);
                } catch (Exception err) {
                    log.error("Player Data Component Tick task execution failed: ", err);
                }
            }
        }
    }

    /**
     * Finds a player of the current client level by uuid, returns {@code null} when not tracked yet.
     */
    public static Player resolvePlayer(UUID playerUuid) {
        if (Minecraft.getInstance().level == null) {
            return null;
        }
        for (Player player : Minecraft.getInstance().level.players()) {
            if (player.getUUID().equals(playerUuid)) {
                return player;
            }
        }
        return null;
    }

    public static void clearConnection() {
        CLIENT_INSTANCE.dataList.clear();
        CLIENT_INSTANCE.dirtyPlayers.clear();
    }

    public static ClientPlayerComponentManager clientAccess() {
        return CLIENT_INSTANCE;
    }
}
