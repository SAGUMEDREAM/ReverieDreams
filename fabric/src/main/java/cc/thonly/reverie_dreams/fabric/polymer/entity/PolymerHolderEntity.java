package cc.thonly.reverie_dreams.fabric.polymer.entity;

import cc.thonly.reverie_dreams.inf.IHolderEntity;
import net.minecraft.server.level.ServerPlayer;

public interface PolymerHolderEntity extends IHolderEntity {
    default void onCreated() {

    }

    default void onTrackingStopped(ServerPlayer player) {

    }
}
