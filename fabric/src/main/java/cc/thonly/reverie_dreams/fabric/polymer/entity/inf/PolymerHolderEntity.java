package cc.thonly.reverie_dreams.fabric.polymer.entity.inf;

import cc.thonly.reverie_dreams.api.polymer.CommonPolymerHolderEntity;
import net.minecraft.server.level.ServerPlayer;

public interface PolymerHolderEntity extends CommonPolymerHolderEntity {
    default void onCreated() {

    }

    default void onTrackingStopped(ServerPlayer player) {

    }
}
