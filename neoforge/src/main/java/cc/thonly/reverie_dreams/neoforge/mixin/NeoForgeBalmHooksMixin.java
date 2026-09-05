package cc.thonly.reverie_dreams.neoforge.mixin;

import cc.thonly.reverie_dreams.entity.base.FakePlayer;
import net.blay09.mods.balm.neoforge.platform.internal.NeoForgeBalmHooks;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(NeoForgeBalmHooks.class)
public class NeoForgeBalmHooksMixin {
    @Inject(method = "isFakePlayer", at = @At("HEAD"), cancellable = true)
    public void testFakePlayer(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player instanceof FakePlayer) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}