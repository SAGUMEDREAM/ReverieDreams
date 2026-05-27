package cc.thonly.reverie_dreams.api.polymer;

import net.minecraft.server.level.ServerPlayer;

public interface CommonPolymerHolderEntity {
    void onCreated();

    void onTrackingStopped(ServerPlayer player);
}
