package cc.thonly.reverie_dreams.fabric.mixin;

import cc.thonly.reverie_dreams.entity.base.FakePlayer;
import net.blay09.mods.balm.fabric.platform.internal.FabricBalmHooks;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(FabricBalmHooks.class)
public class FabricBalmHooksMixin {
    @Inject(method = "isFakePlayer", at = @At("HEAD"), cancellable = true)
    public void testFakePlayer(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player instanceof FakePlayer) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
