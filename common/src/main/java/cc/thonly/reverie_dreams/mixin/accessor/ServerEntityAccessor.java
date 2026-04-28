package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.server.level.ServerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerEntity.class)
public interface ServerEntityAccessor {
    @Accessor("synchronizer")
    ServerEntity.Synchronizer reverie_dreams$getSynchronizer();
}
