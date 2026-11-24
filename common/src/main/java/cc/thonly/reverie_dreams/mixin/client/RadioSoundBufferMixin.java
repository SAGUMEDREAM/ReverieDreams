package cc.thonly.reverie_dreams.mixin.client;

import cc.thonly.reverie_dreams.client.audio.radio.RadioAudioClient;
import net.minecraft.client.sounds.*;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(SoundBufferLibrary.class)
public abstract class RadioSoundBufferMixin {
    @Inject(method = "getStream", at = @At("HEAD"), cancellable = true)
    private void reverieDreams$radioStream(Identifier path, boolean loop, CallbackInfoReturnable<CompletableFuture<AudioStream>> callback) {
        var stream = RadioAudioClient.audio(path);
        if (stream != null)
            callback.setReturnValue(CompletableFuture.completedFuture(stream));
    }
}
