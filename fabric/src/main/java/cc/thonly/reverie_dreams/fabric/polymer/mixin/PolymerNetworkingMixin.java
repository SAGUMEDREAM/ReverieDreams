package cc.thonly.reverie_dreams.fabric.polymer.mixin;

import eu.pb4.polymer.networking.api.PolymerNetworking;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.network.Connection;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(PolymerNetworking.class)
public class PolymerNetworkingMixin {
    @Inject(method = "getMetadata", at = @At("HEAD"), cancellable = true)
    private static <T extends Tag> @Nullable void injectMetadata(Connection handler, Identifier identifier, TagType<T> type, CallbackInfoReturnable<T> cir) {
        if (handler == null) {
            Thread.dumpStack();
            cir.setReturnValue(null);
            cir.cancel();
        }
    }
}
