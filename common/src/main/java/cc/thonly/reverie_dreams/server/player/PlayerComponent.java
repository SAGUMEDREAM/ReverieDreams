package cc.thonly.reverie_dreams.server.player;

import cc.thonly.reverie_dreams.api.player.BasePlayerComponentManager;
import cc.thonly.reverie_dreams.server.component.ServerPlayerComponentManager;
import com.mojang.serialization.Codec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unchecked", "rawtypes"})
public interface PlayerComponent<T extends PlayerComponent> {
    void onLoad();

    void setPlayer(Player player);

    Player getPlayer();

    Codec<T> getCodec();

    default void tick(@Nullable MinecraftServer server, boolean isClient) {

    }

    default void markDirty() {
        BasePlayerComponentManager playerComponentManager = ServerPlayerComponentManager.serverAccess();
        playerComponentManager.saveAll();
        if (playerComponentManager instanceof ServerPlayerComponentManager serverPlayerComponentManager) {
            serverPlayerComponentManager.updatePlayerData();
        }
    }

    default T get() {
        return (T) this;
    }

}
