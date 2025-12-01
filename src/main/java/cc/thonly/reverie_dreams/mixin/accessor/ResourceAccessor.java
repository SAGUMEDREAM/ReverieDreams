package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.InputStream;

@Mixin(Resource.class)
public interface ResourceAccessor {
    @Accessor("streamSupplier")
    IoSupplier<InputStream> getInputSupplier();
}