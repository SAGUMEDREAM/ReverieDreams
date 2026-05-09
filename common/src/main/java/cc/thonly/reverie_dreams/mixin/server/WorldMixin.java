package cc.thonly.reverie_dreams.mixin.server;

import cc.thonly.reverie_dreams.inf.IWorld;
import cc.thonly.reverie_dreams.world.dimension.WorldInit;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Level.class)
public abstract class WorldMixin implements LevelAccessor,
        AutoCloseable, IWorld {

    @Override
    public ResourceKey<Level> reverie_dreams$getDreamWorldKey() {
        return WorldInit.getDreamWorld();
    }

    @Override
    public ResourceKey<Level> reverie_dreams$getMoonKey() {
        return WorldInit.getMoon();
    }
}
