package cc.thonly.reverie_dreams.server.player;

import com.mojang.serialization.Codec;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface PlayerComponentInitializer<T> {

    PlayerComponent<T> create(ServerPlayerEntity player);

    default PlayerComponent<T> createAndLoad(ServerPlayerEntity player) {
        Logger log = LoggerFactory.getLogger(PlayerComponentInitializer.class);
        PlayerComponent<T> playerComponent = this.create(player);
        playerComponent.setPlayer(player);
        try {
            playerComponent.onLoad();
        } catch (Exception err) {
            log.error("Can't load Player Data Component {}: {}", player.getGameProfile().getName(), player.getUuidAsString());
        }
        return playerComponent;
    }

    Codec<T> getCodec();
}
