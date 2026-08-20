package cc.thonly.reverie_dreams.neoforge.mixin;

import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import net.minecraft.core.Registry;
import net.neoforged.neoforge.registries.IRegistryExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@SuppressWarnings({"unused", "NullableProblems"})
@Mixin(RegistryProvider.class)
@Pseudo
public abstract class RegistryHandlerMixin<T> implements IRegistryExtension<T>, Registry<T> {

}
