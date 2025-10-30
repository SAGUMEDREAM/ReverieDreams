package cc.thonly.reverie_dreams.mixin;

import net.fabricmc.fabric.impl.registry.sync.DynamicRegistriesImpl;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = DynamicRegistriesImpl.class, remap = false)
public class DynamicRegistriesImplMixin {

}
