package cc.thonly.reverie_dreams.inf;

import net.minecraft.server.level.ServerPlayer;

public interface IHolderEntity {
    void onCreated();
    void onTrackingStopped(ServerPlayer player);
}
