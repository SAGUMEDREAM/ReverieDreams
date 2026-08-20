package cc.thonly.reverie_dreams.mixin.registry;

import cc.thonly.reverie_dreams.api.registry.SimpleRegistryFrozenModifier;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> implements SimpleRegistryFrozenModifier, WritableRegistry<T> {

    @Shadow
    private boolean frozen;

    @Shadow
    private @Nullable Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;

    @Shadow @Final public Map<Identifier, Holder.Reference<T>> byLocation;

    @Shadow @Final public Map<T, Holder.Reference<T>> byValue;

    @Override
    public void reverie_dreams$setFrozen(boolean value) {
        this.frozen = value;
    }


}
