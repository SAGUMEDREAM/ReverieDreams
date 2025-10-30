package cc.thonly.reverie_dreams.mixin.authlib;

import com.mojang.authlib.SignatureState;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MinecraftProfileTextures.class, remap = false)
public class MinecraftProfileTexturesMixin {
    @Mutable
    @Shadow @Final private SignatureState signatureState;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void modifySigneState(MinecraftProfileTexture skin, MinecraftProfileTexture cape, MinecraftProfileTexture elytra, SignatureState signatureState, CallbackInfo ci) {
        this.signatureState = SignatureState.SIGNED;
    }
}
