package cc.thonly.reverie_dreams.mixin.registry;

import cc.thonly.reverie_dreams.inf.SimpleRegistrySetter;
import cc.thonly.reverie_dreams.server.ServerContentRegistry;
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
public abstract class MappedRegistryMixin<T> implements SimpleRegistrySetter, WritableRegistry<T> {

    @Shadow
    private boolean frozen;

    @Shadow
    private @Nullable Map<T, Holder.Reference<T>> unregisteredIntrusiveHolders;

    @Shadow @Final public Map<Identifier, Holder.Reference<T>> byLocation;

    @Shadow @Final public Map<T, Holder.Reference<T>> byValue;

    @Unique
    private boolean reverie_dreams$injected = false;

    @Override
    public void reverie_dreams$setFrozen(boolean value) {
        this.frozen = value;
    }

    @SuppressWarnings({"unchecked", "ConstantValue"})
    @Deprecated
    @Inject(method = "freeze", at = @At("HEAD"), order = 100)
    public void reverie_dreams$onFreeze(CallbackInfoReturnable<Registry<T>> cir) {
        if (true) {
            return;
        }
        if (this.reverie_dreams$injected) {
            return;
        }
        this.reverie_dreams$injected = true;
        MappedRegistry<T> registry = (MappedRegistry<T>) (Object) this;
        ServerContentRegistry.IMPL.write(registry);
    }

}
