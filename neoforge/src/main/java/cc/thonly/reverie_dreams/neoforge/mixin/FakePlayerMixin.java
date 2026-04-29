package cc.thonly.reverie_dreams.neoforge.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(FakePlayer.class)
public abstract class FakePlayerMixin extends ServerPlayer {
    public FakePlayerMixin(MinecraftServer server, ServerLevel level, GameProfile gameProfile, ClientInformation clientInformation) {
        super(server, level, gameProfile, clientInformation);
    }

    @SuppressWarnings("ConstantValue")
    @Inject(method = "isFakePlayer", at = @At("HEAD"), cancellable = true)
    public void testFakePlayer(CallbackInfoReturnable<Boolean> cir) {
        Object pThis = this;
        if (pThis instanceof cc.thonly.reverie_dreams.entity.base.FakePlayer) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
