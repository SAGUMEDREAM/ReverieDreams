package cc.thonly.reverie_dreams.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.network.ServerPlayerConnection;

@Mixin(ChunkMap.TrackedEntity.class)
public interface EntityTrackerAccessor {
    @Accessor("seenBy")
    Set<ServerPlayerConnection> getListenerSet();

    @Accessor("serverEntity")
    ServerEntity getTrackEntry();
}
