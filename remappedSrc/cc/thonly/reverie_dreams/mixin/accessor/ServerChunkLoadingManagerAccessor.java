package cc.thonly.reverie_dreams.mixin.accessor;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkMap.class)
public interface ServerChunkLoadingManagerAccessor {
    @Accessor("entityTrackers")
    Int2ObjectMap<ChunkMap.TrackedEntity> getEntityTrackerMap();
}
