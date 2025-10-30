package cc.thonly.reverie_dreams.server.player;

import com.mojang.serialization.Codec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

@SuppressWarnings("unchecked")
public interface PlayerComponent<T> {
    void onLoad();

    void setPlayer(ServerPlayer player);

    Codec<T> getCodec();

    default void tick(MinecraftServer server) {

    }

    default void markDirty() {
        PlayerDataComponentManager playerDataComponentManager = PlayerDataComponentManager.getInstance();
        playerDataComponentManager.saveAll();
    }

    default T get() {
        return (T) this;
    }

}
