package cc.thonly.reverie_dreams.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.InputStream;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;

@Mixin(Resource.class)
public interface ResourceAccessor {
    @Accessor
    IoSupplier<InputStream> getInputSupplier();
}