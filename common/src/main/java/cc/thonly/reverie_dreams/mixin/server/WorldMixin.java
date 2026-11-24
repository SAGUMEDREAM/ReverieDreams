package cc.thonly.reverie_dreams.mixin.server;

import cc.thonly.reverie_dreams.api.level.WorldExtension;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Level.class)
public abstract class WorldMixin implements LevelAccessor, AutoCloseable, WorldExtension {

}
