package cc.thonly.reverie_dreams.server.player;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface PlayerComponentInitializer<T> {

    PlayerComponent<T> create(ServerPlayer player);

    default PlayerComponent<T> createAndLoad(ServerPlayer player) {
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

    Codec<T> getCodec();
}
