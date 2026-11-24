package cc.thonly.reverie_dreams.api.player;

import com.mojang.serialization.Codec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"unchecked", "rawtypes"})
public interface PlayerComponent<T extends PlayerComponent> {
    /**
     * Called whenever the component gets attached to a (possibly new) player instance, both for
     * freshly created components and for components decoded from disk or network.
     */
    void onLoad();

    /**
     * Called after the data of this component was decoded from disk or network. Newly created
     * components never receive this call.
     */
    default void onDataLoaded() {
    }

    void setPlayer(Player player);

    Player getPlayer();

    Codec<T> getCodec();

    default void tick(@Nullable MinecraftServer server, boolean isClient) {

    }

    /**
     * Marks the owning player as changed: only that player's file is rewritten and only that
     * player's data is synchronized on the next server tick.
     *
     * <p>Changes never get lost even if this is skipped, the world save handler still flushes all
     * component data — this method only makes the flush happen immediately.</p>
     */
    default void markDirty() {
        Player player = this.getPlayer();
        if (player == null) {
            return;
        }
        PlayerComponentManager.serverAccess().markDirty(player.getUUID());
    }

    default T get() {
        return (T) this;
    }

}
