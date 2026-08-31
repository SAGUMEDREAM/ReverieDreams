package cc.thonly.reverie_dreams.server.player;

import com.mojang.serialization.Codec;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public interface PlayerComponentInitializer<T extends PlayerComponent> {

    Codec<T> getCodec();

    default PlayerComponent<T> create(Player player) {
        PlayerComponent<T> playerComponent = this.create();
        playerComponent.setPlayer(player);
        return playerComponent;
    }

    PlayerComponent<T> create();

    default PlayerComponent<T> createAndLoad(Player player) {
        Logger log = LoggerFactory.getLogger(PlayerComponentInitializer.class);
        PlayerComponent<T> playerComponent = this.create(player);
        playerComponent.setPlayer(player);
        try {
            playerComponent.onLoad();
        } catch (Exception err) {
            log.error("Can't load Player Data Component {}: {}", player.getGameProfile().name(), player.getStringUUID());
        }
        return playerComponent;
    }
}
