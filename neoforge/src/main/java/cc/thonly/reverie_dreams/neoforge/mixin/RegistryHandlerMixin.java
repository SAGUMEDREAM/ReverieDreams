package cc.thonly.reverie_dreams.neoforge.mixin;

import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.IRegistryExtension;
import net.neoforged.neoforge.registries.callback.AddCallback;
import net.neoforged.neoforge.registries.callback.BakeCallback;
import net.neoforged.neoforge.registries.callback.ClearCallback;
import net.neoforged.neoforge.registries.callback.RegistryCallback;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;

@SuppressWarnings({"unused", "NullableProblems"})
@Mixin(RegistryHandler.class)
@Pseudo
public abstract class RegistryHandlerMixin<T> implements IRegistryExtension<T>, Registry<T> {

}
